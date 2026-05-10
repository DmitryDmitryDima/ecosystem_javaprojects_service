package com.ecosystem.projectsservice.javaprojects.service.projects.constructors;


import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.*;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageException;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.nio.file.Path;
import java.util.*;


// сервис для построения проекта на основе yaml инструкций и файлов шаблонов
@Service
public class ProjectYamlConstructor {


    @Autowired
    private StorageService storageService;


    @Autowired
    private MavenPreparer mavenPreparer;



    @Autowired
    private StorageExternals storageExternals;


    // создаем объекты в хранилище, используя загруженные в контекст id
    public void createStorageObjects(List<FileTemplateEnvelope> files)
            throws ConstructorException {



        for (FileTemplateEnvelope fileTemplateEnvelope:files){

            File dbEntity = fileTemplateEnvelope.getFile();
            if (dbEntity.getId()==null) {
                throw new ConstructorException("ошибка построения проекта. Отсутствует id файла");
            }


            try {
                storageService.saveOrUpdate(storageExternals.getStorageUserBucket(),
                        dbEntity.getId().toString(), fileTemplateEnvelope.getTemplateContent()
                );
            }

            catch (Exception e){
                throw new ConstructorException("Ошибка построения проекта" +
                        " - ошибка создания файла в хранилище. Причина: "
                        +e.getMessage());
            }


        }



    }





    public List<FileTemplateEnvelope> buildDatabaseStructureAndPrepareFileTemplates(BuildProperties properties)
            throws ConstructorException{


        String instructionName = switch (properties.getProjectType()){
            case MAVEN_CLASSIC -> "maven_classic.yaml";
            case GRADLE_CLASSIC -> "gradle_classic.yaml";
        };

        YamlInstruction instruction = readInstruction(instructionName);

        return runStructureAndExtractTemplates(instruction, properties);










    }






    /*

    задача - создать структуру и вернуть сущности файлов, связанные с названием template
    После коммита в базу сущности обзаведутся id, после чего можно будет,
    базируясь на template, создать записи с контентом

    template content загружаем сразу


    prepare производим на основании списка и структуры, добавляя или изменяя содержимое списка файлов
     */
    private List<FileTemplateEnvelope> runStructureAndExtractTemplates(YamlInstruction yamlInstruction,
                                                                       BuildProperties properties
                                                 ){


        Directory root = properties.getProject().getRoot();

        List<DirectoryInstruction> directoryInstructions
                = yamlInstruction.getDirectories();

        List<FileInstruction> fileInstructions
                = yamlInstruction.getFiles();


        List<FileTemplateEnvelope> fileTemplateEnvelopes = new ArrayList<>();


        HashMap<Long, Directory> directoriesBase = new HashMap<>();




        Set<Long> higherLevel = new HashSet<>();

        higherLevel.add(null);

        directoriesBase.put(null, root);

        int iteration = 0;


        while (!directoryInstructions.isEmpty()){

            Set<Long> nextLevel = new HashSet<>();

            for (DirectoryInstruction instruction:directoryInstructions){


                if (higherLevel.contains(instruction.getParent())){
                    nextLevel.add(instruction.getId());


                    Directory parent = directoriesBase.get(instruction.getParent());

                    if (parent == null){
                        throw new ConstructorException("Ошибка построения проекта. Некорректная инструкция");
                    }

                    // обновляем сущности
                    Directory child = instruction.prepareDirectoryEntity();

                    addChildToParent(child, parent);


                    directoriesBase.put(instruction.getId(), child);
                }
            }

            // очищаем список директорий от элементов нового верхнего уровня
            directoryInstructions.removeIf(directoryInstruction
                    -> nextLevel.contains(directoryInstruction.getId()));

            higherLevel = nextLevel;


            iteration++;

            if (iteration>20){
                throw new ConstructorException("Ошибка построения проекта - некорректная инструкция");
            }



        }

        for (FileInstruction fileInstruction:fileInstructions){

            Directory parent = directoriesBase.get(fileInstruction.getParent());

            if (parent == null){
                throw new ConstructorException("Ошибка построения проекта - некорректная инструкция");
            }

            // формируем зависимость
            File file = fileInstruction.prepareFile();

            addChildToParent(file, parent);

            String content = "";

            if (fileInstruction.getTemplate()!=null){
                // загружаем контент
                try {
                    content =  storageService.downloadContent(storageExternals.getStorageSystemBucket(),
                            fileInstruction.getTemplate());


                }

                catch (Exception e){
                    throw new ConstructorException("Ошибка построения проекта " +
                            "- невозможно чтение шаблона из хранилища. Причина: "
                            +e.getMessage());
                }
            }

            FileTemplateEnvelope envelope = new FileTemplateEnvelope(file, content);

            fileTemplateEnvelopes.add(envelope);






        }


        // специфичная донастройка структуры в зависимости от типа проекта и параметров,
        // заданных пользователем
        prepareStructure(properties, fileTemplateEnvelopes);





        return fileTemplateEnvelopes;




    }


    private void prepareStructure(BuildProperties properties, List<FileTemplateEnvelope> templates){
        if (properties.getProjectType().equals(ProjectType.MAVEN_CLASSIC)){
            mavenPreparer.prepare(properties, templates);
        }
    }


    private YamlInstruction readInstruction(String name){

        String instructionContent;

        try {
            instructionContent = storageService.downloadContent(
              storageExternals.getStorageSystemBucket(), name
            );
        }
        catch (StorageException storageException){
            throw new ConstructorException("Ошибка построения проекта - ошибка загрузки инструкции: "
                    +storageException.getMessage());
        }

        try {
            Yaml yaml
                    = new Yaml(new Constructor(YamlInstruction.class, new LoaderOptions()));

            return yaml.load(instructionContent);
        }

        catch (Exception e){
            throw new ConstructorException("Ошибка построения проекта" +
                    " - ошибка чтения инструкции: "+e.getMessage());
        }



    }





    private void addChildToParent(File child, Directory parent){
        parent.getFiles().add(child);
        child.setParent(parent);

        String fullFileName = child.getName();

        if (child.getExtension()!=null){
            fullFileName = fullFileName+"."+child.getExtension();
        }

        child
                .setConstructedPath(Path.of(parent.getConstructedPath(), fullFileName).normalize().toString());
    }


    private void addChildToParent(Directory child, Directory parent){
        parent.getChildren().add(child);

        child.setParent(parent);

        Path childPath = Path.of(parent.getConstructedPath(), child.getName());

        child.setConstructedPath(childPath.normalize().toString());

    }


}
