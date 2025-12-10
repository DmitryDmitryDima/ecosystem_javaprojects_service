package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ProjectLifecycleUtils {

    // создать директорию по заданному пути
    public static  void createDirectory(Path path) throws Exception{
        Files.createDirectories(path);
    }

    // добавляем зависимость между двумя директориями - сущностями
    public static void injectChildToParent(Directory child, Directory parent) {
        parent.getChildren().add(child);
        child.setParent(parent);
    }

    // добавляем зависимость между директорией и файлом сущностями
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

    // записываем файл на диск, при этом (при наличии) пишем в него уже готовый template; кешируем путь до файла
    // В данном случае шаблон берется из системной папки (будущие альтернатива - ИИ)
    public static void writeFileFromSystemTemplate(Directory parent, File file, String templatePath, String templateName) throws IOException {
        String fileName = file.getName();

        if (file.getExtension()!=null){
            fileName  =fileName+"."+file.getExtension();
        }
        Path filepath = Path.of(parent.getConstructedPath(), fileName);
        file.setConstructedPath(filepath.toString());

        Files.createFile(filepath);

        // если присутствует шаблон

        if (templateName!=null){
            Path fullTemplatePath = Path.of(templatePath, templateName);
            try (InputStream stream = Files.newInputStream(fullTemplatePath)){
                String templateContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                Files.writeString(filepath, templateContent);
            }
            catch (Exception e){
                throw new IOException("ошибка записи шаблона");
            }

        }



    }
    // todo common method
    public static void setArtifactIdInsidePomXML(String pomXML, String artifactID) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = factory
                .newDocumentBuilder()
                .parse(pomXML);

        document.getDocumentElement().normalize();

        NodeList nodes = document.getElementsByTagName("artifactId");



        Node artifactidNode = nodes.item(0).getFirstChild();
        artifactidNode.setNodeValue(artifactID);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new java.io.File(pomXML));
        Source input = new DOMSource(document);
        transformer.transform(input, output);

    }

    // todo common method
    public static void setMainClassInsidePomXML(String pomXML, String className) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = factory
                .newDocumentBuilder()
                .parse(pomXML);

        document.getDocumentElement().normalize();

        NodeList nodes = document.getElementsByTagName("mainClass");

        if (nodes.getLength()!=1){
            throw new IllegalStateException("XML файл поврежден");
        }
        Node mainClassNode = nodes.item(0).getFirstChild();
        mainClassNode.setNodeValue(className);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new java.io.File(pomXML));
        Source input = new DOMSource(document);
        transformer.transform(input, output);

    }



    public static void generateEntryPointForMavenProject(Project project) throws Exception{

        // сначала мы должны добраться до папки com (сущности)
        List<String> classicalMavenFolderStructure = List.of("src", "main","java","com");

        Directory currentDirectory = project.getRoot();

        for (String folder:classicalMavenFolderStructure){

            Optional<Directory> match = currentDirectory.getChildren().stream().filter(dir->dir.getName().equals(folder)).findFirst();
            if (match.isEmpty()) throw new IllegalStateException("Неверная структура проекта");
            currentDirectory = match.get();
        }

        // создаем сущность Main файла, организуем зависимость

        File main = new File();
        main.setCreatedAt(Instant.now());
        main.setUpdatedAt(Instant.now());
        main.setName("Main");
        main.setExtension("java");
        main.setStatus(FileStatus.AVAILABLE);
        main.setConstructedPath(Path.of(currentDirectory.getConstructedPath(), "Main.java" ).toString());

        injectChildToParent(main, currentDirectory);


        project.setEntryPoint(main);







        // пишем в файл на диске
        String formattedTemplate = """
                        %s
                       \s
                        public class %s{
                          public static void main(String...args){
                           \s
                          }
                       \s
                        }
                       \s"""
                .formatted("package com;","Main");

        Files.writeString(Path.of(currentDirectory.getConstructedPath(), "Main.java"), formattedTemplate);

        // изменяем pom xml
        setMainClassInsidePomXML(Path.of(project.getRoot().getConstructedPath(), "pom.xml").toString(), "com.Main");






    }
}
