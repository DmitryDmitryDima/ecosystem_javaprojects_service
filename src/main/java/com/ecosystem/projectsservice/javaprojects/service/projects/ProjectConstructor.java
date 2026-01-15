package com.ecosystem.projectsservice.javaprojects.service.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.ConstructorSettingsForSystemTemplateBuild;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectLifecycleUtils;
import com.ecosystem.projectsservice.javaprojects.utils.yaml.DirectoryInstruction;
import com.ecosystem.projectsservice.javaprojects.utils.yaml.FileInstruction;
import com.ecosystem.projectsservice.javaprojects.utils.yaml.YamlInstruction;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
методы класса вызываются из @Transactional контекста, поэтому любая ошибка внутри него означает откат всех записей в бд
todo как подчищать диск?
 */
@Component
public class ProjectConstructor {



    // постройка проекта на основе готового system template
    public void buildProjectFromSystemTemplate(ConstructorSettingsForSystemTemplateBuild settings) throws Exception{

        Project project = settings.getProject();
        Directory root = project.getRoot();

        // создаем папки
        /*
        try {
            ProjectUtils.createDirectory(Path.of(root.getConstructedPath()));
        }
        catch (Exception e){
            throw new IllegalStateException("ошибка создания корневой папки проекта");

        }

         */






        // загружаем и запускаем инструкцию, базируясь на projectType

        String instructionName = switch (settings.getProjectType()){
            case MAVEN_CLASSIC -> "maven_classic.yaml";
            case GRADLE_CLASSIC -> "gradle_classic.yaml";
        };

        try {
            // читаем инструкцию
            YamlInstruction instruction = readInstruction(Path.of(settings.getInstructionsPath(), instructionName));
            // запускаем инструкцию
            runInstruction(instruction, root, settings.getFileTemplatesPath(), settings.getProjectsPath());
            // выполняем дополнительные действия над готовой структурой проекта
            prepareProject(settings);


        }
        catch (Exception e){
            // подчищаем
            FileSystemUtils.deleteRecursively(Path.of(root.getConstructedPath()));
            throw new IllegalStateException("сбой на этапе создания проекта. Причина: "+e.getMessage());
        }





    }

    private YamlInstruction readInstruction(Path path) throws Exception{
        try (InputStream stream = Files.newInputStream(path)) {

            Yaml yaml = new Yaml(new Constructor(YamlInstruction.class, new LoaderOptions()));
            return yaml.load(stream);

        }
        catch (Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void runInstruction(YamlInstruction instruction, Directory root, String templatePath, String projectsPath) throws Exception{

        // parent = null - значит верх иерархии, имеющий прямую зависимость с root. Иерархия строится с директорий
        List<DirectoryInstruction> directoryInstructions = instruction.getDirectories();
        HashMap<Long, Directory> directoriesBase = new HashMap<>();

        System.out.println(root.getConstructedPath());



        directoriesBase.put(null, root);

        Set<Long> higherLayer = new HashSet<>();
        higherLayer.add(null);

        // опускаемся вглубь иерархии. Если инструкция содержит цикл - ловим это счетчиком
        int iteration = 0;
        while (!instruction.getDirectories().isEmpty()){
            // в этой коллекции собираем те директории, которые будут следующим верхним уровнем
            Set<Long> toRemove = new HashSet<>();

            // ищем детей текущего верхнего уровня, вставляем зависимости
            for (DirectoryInstruction directoryInstruction:directoryInstructions){

                if (higherLayer.contains(directoryInstruction.getParent())){

                    // найденный элемент уходит в будущий верхний уровень иерархии
                    toRemove.add(directoryInstruction.getId());

                    Directory parent = directoriesBase.get(directoryInstruction.getParent());
                    if (parent == null){
                        throw new IllegalStateException("Инструкция содержит неправильную зависимость между директориями");
                    }

                    Directory child = directoryInstruction.prepareDirectoryEntity();

                    System.out.println("parent here "+parent.getConstructedPath());

                    // создаем зависимость в базе
                    ProjectLifecycleUtils.injectChildToParent(child, parent);

                    // пишем директорию, при этом дополняя path для child
                    ProjectLifecycleUtils.writeDirectoriesAndCachePath(parent, child, projectsPath);



                    directoriesBase.put(directoryInstruction.getId(), child);





                }
            }


            // очищаем инструкции от элементов нового верхнего уровня
            instruction.getDirectories().removeIf(directoryInstruction -> toRemove
                    .contains(directoryInstruction.getId()));
            // обновляем верхний уровень
            higherLayer = toRemove;


            iteration++;
            if (iteration>=20){
                throw new IllegalStateException("превышена максимальная глубина проекта или обнаружен цикл");
            }


        }

        // работаем с файлами
        List<FileInstruction> fileInstructions = instruction.getFiles();
        for (FileInstruction fileInstruction:fileInstructions){
            Directory parent = directoriesBase.get(fileInstruction.getParent());
            if (parent == null) {
                throw new IllegalStateException("instruction contains broken dependency between file and directory");
            }


            // формируем зависимость в бд
            File file = fileInstruction.prepareFile();

            ProjectLifecycleUtils.injectChildToParent(file, parent);







            // кешируем путь до файла
            // создаем файл
            // загружаем шаблон, если он присутствует

            ProjectLifecycleUtils.writeFileFromSystemTemplate(parent, file, templatePath, fileInstruction.getTemplate(), projectsPath);









        }

    }

    private void prepareProject(ConstructorSettingsForSystemTemplateBuild info) throws Exception {
        if (info.getProjectType()== ProjectType.MAVEN_CLASSIC){
            // добавляем artefact id к pom.xml
            ProjectLifecycleUtils.setArtifactIdInsidePomXML(Path.of(info.getProjectsPath(),info.getProject().getName(), "pom.xml").toString(),
                    info.getProject().getName()+"-project"
                    );
            // генерируем точку входа, если этого желает пользователь
            if (info.isNeedEntryPoint()){

                ProjectLifecycleUtils.generateEntryPointForMavenProject(info.getProject(), info.getProjectsPath());

            }




        }
    }





}
