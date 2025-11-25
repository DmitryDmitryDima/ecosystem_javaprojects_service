package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectUtils {


    public static  void createDirectory(Path path) throws Exception{
        Files.createDirectories(path);
    }

    public static void injectChildToParent(Directory child, Directory parent) {
        parent.getChildren().add(child);
        child.setParent(parent);
    }

    public static void injectChildToParent(File file, Directory parent){
        parent.getFiles().add(file);
        file.setParent(parent);
    }
    // пишем директории на диск и кешируем путь для child
    public static void writeDirectoriesAndCachePath(Directory parent, Directory child) throws Exception{

        Path childPath = Path.of(parent.getConstructedPath(), child.getName());
        child.setConstructedPath(childPath.toString());

        Files.createDirectories(childPath);
    }

    public static void writeFile(Directory parent, File file, String templatePath, String templateName) throws IOException {
        String fileName = file.getName();

        if (file.getExtension()!=null){
            fileName  =fileName+"."+file.getExtension();
        }
        Path filepath = Path.of(parent.getConstructedPath(), fileName);
        file.setConstructedPath(filepath.toString());

        Files.createFile(filepath);

        // если присутствует шаблон

        if (templateName!=null){

        }



    }
}
