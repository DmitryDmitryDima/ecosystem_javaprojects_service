package com.ecosystem.projectsservice.javaprojects.service;

import com.ecosystem.projectsservice.javaprojects.dto.projects.ProjectBuildFromSystemTemplateInfo;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.utils.projects.ProjectUtils;
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




    public void buildProjectFromSystemTemplate(ProjectBuildFromSystemTemplateInfo info) throws Exception{

        Project project = info.getProject();
        Directory root = project.getRoot();

        // создаем папки
        try {
            ProjectUtils.createDirectory(Path.of(root.getConstructedPath()));
        }
        catch (Exception e){
            throw new IllegalStateException("ошибка создания корневой папки проекта");

        }




        // загружаем и запускаем инструкцию
        try {
            YamlInstruction instruction = readInstruction(Path.of(info.getInstructionPath()));
            runInstruction(instruction, root, info.getFileTemplatesPath());
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

    private void runInstruction(YamlInstruction instruction, Directory root, String templatePath) throws Exception{

        // parent = null - значит верх иерархии, имеющий прямую зависимость с root. Иерархия строится с директорий
        List<DirectoryInstruction> directoryInstructions = instruction.getDirectories();
        HashMap<Long, Directory> directoriesBase = new HashMap<>();



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

                    // создаем зависимость в базе
                    ProjectUtils.injectChildToParent(child, parent);

                    // пишем директорию, при этом дополняя path для child
                    ProjectUtils.writeDirectoriesAndCachePath(parent, child);



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

            ProjectUtils.injectChildToParent(file, parent);







            // кешируем путь до файла
            // создаем файл
            // загружаем шаблон, если он присутствует
            // todo доделать
            ProjectUtils.writeFile(parent, file, templatePath, fileInstruction.getTemplate());









        }

    }





}
