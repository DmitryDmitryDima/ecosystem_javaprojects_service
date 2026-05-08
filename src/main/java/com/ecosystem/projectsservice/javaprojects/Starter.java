package com.ecosystem.projectsservice.javaprojects;

import com.ecosystem.projectsservice.javaprojects.service.external_values.ExternalValues;
import com.ecosystem.projectsservice.javaprojects.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;


/*

временно - закачивает системные инструкции
 */
@Component
public class Starter implements CommandLineRunner {

    @Autowired
    private ExternalValues externalValues;


    @Autowired
    private StorageService storageService;



    @Override
    public void run(String... args) throws Exception {

        Path systemPath = Path.of(externalValues.getSystemStoragePath());

        SystemVisitor systemVisitor = new SystemVisitor();

        Files.walkFileTree(systemPath, systemVisitor);



    }


    class SystemVisitor implements FileVisitor<Path>{

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {






            String key = file.getFileName().toString();
            String content = Files.readString(file);

            try {
                storageService.saveOrUpdate(externalValues.getStorageSystemBucket(), key, content);
            }
            catch (Exception e){
                e.printStackTrace();
            }




            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
