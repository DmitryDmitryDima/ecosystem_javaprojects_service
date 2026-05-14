package com.ecosystem.projectsservice.javaprojects.service.projects.constructors;


import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.BuildProperties;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.FileTemplateEnvelope;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;


/*
операции, связанные со структурой базовых компонентов maven проектов
 */
@Service
public class MavenPreparer {

    private final List<String> classicalMavenFolderStructure
            = List.of("src", "main","java","com");


    private final String mainTemplate = """
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




    public void prepare(BuildProperties properties,
                        List<FileTemplateEnvelope> templates) throws ConstructorException{

        setArtifactID(properties, templates);

        if (properties.isNeedEntryPoint()){
            addEntryPoint(properties, templates);
        }

    }


    private void addEntryPoint(BuildProperties properties,
                               List<FileTemplateEnvelope> templates){


        // проверяем структуру, извлекая com директорию
        Directory com = getComDirectory(properties.getProject().getRoot());


        // для директории com создаем файл Main, вставляя зависимость и добавляя в список templates
        File main = new File();
        main.setCreatedAt(Instant.now());
        main.setUpdatedAt(Instant.now());
        main.setName("Main");
        main.setExtension("java");
        main.setStatus(FileStatus.AVAILABLE);
        main.setConstructedPath(Path.of(com.getConstructedPath(), "Main.java" ).toString());

        main.setParent(com);
        com.getFiles().add(main);

        properties.getProject().setEntryPoint(main);

        FileTemplateEnvelope fileTemplateEnvelope = new FileTemplateEnvelope(main, mainTemplate);

        templates.add(fileTemplateEnvelope);


        // обновляем главный файл в pom.xml (редактируем template)
        setEntryPointInsidePom(properties, templates, "com.Main");






    }


    private void setEntryPointInsidePom(BuildProperties properties,
                                        List<FileTemplateEnvelope> templateEnvelopes, String className){


        var pomTemplate = extractPomTemplate(properties, templateEnvelopes);

        var pomContent = pomTemplate.getTemplateContent();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(pomContent)));

            document.getDocumentElement().normalize();

            NodeList nodes = document.getElementsByTagName("mainClass");

            if (nodes.getLength()!=1){
                throw new IllegalStateException("XML файл поврежден");
            }
            Node mainClassNode = nodes.item(0).getFirstChild();
            mainClassNode.setNodeValue(className);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            StringWriter writer = new StringWriter();
            Result output = new StreamResult(writer);
            Source input = new DOMSource(document);
            transformer.transform(input, output);

            // обновляем контент в pom template
            pomTemplate.setTemplateContent(writer.toString());
        }

        catch (Exception e){
            throw new ConstructorException("ошибка построения проекта при подготовке pom.xml (entry_point): "+e.getMessage());
        }



    }


    private FileTemplateEnvelope extractPomTemplate(BuildProperties properties, List<FileTemplateEnvelope> templateEnvelopes){
        FileTemplateEnvelope pomTemplate = null;

        // ищем шаблон, соотнесенный с pom. Он должен быть в root директории
        for (var envelope:templateEnvelopes){
            File file = envelope.getFile();
            if (file.getName().equals("pom")
                    && file.getExtension().equals("xml")
                    && file.getParent() == properties.getProject().getRoot())
            {
                pomTemplate = envelope;

            }
        }


        if (pomTemplate == null) {
            throw new ConstructorException("Ошибка построения проекта - неправильная структура maven проекта");
        }

        if (pomTemplate.getTemplateContent().isBlank()){
            throw new ConstructorException("Ошибка построения проекта - пустой шаблон для pom.xml");
        }

        return pomTemplate;


    }





    private void setArtifactID(BuildProperties properties,
                               List<FileTemplateEnvelope> templateEnvelopes){


        var pomTemplate =extractPomTemplate(properties, templateEnvelopes);



        String artifactId = properties.getProject().getName()+"-project";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory
                    .newDocumentBuilder()

                    .parse(new InputSource(new StringReader(pomTemplate.getTemplateContent())));


            document.getDocumentElement().normalize();

            NodeList nodes = document.getElementsByTagName("artifactId");



            Node artifactidNode = nodes.item(0).getFirstChild();
            artifactidNode.setNodeValue(artifactId);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            Result output = new StreamResult(writer);
            Source input = new DOMSource(document);
            
            transformer.transform(input, output);

            pomTemplate.setTemplateContent(writer.toString());
            
            
            
            
        }

        catch (Exception e){
            throw new ConstructorException("ошибка построения проекта при подготовке pom.xml (artifact id): "+e.getMessage());
        }





    }


    private Directory getComDirectory(Directory root){


        Directory current = root;

        for (String name:classicalMavenFolderStructure){

            List<Directory> children = current.getChildren();

            List<Directory> filteredChildren = children
                    .stream().filter(member->member.getName().equals(name)).toList();

            if (filteredChildren.size()!=1) throw new ConstructorException("ошибка построения проекта " +
                    "- обнаружена неверная maven структура ");


            current = filteredChildren.getFirst();



        }

        return current;
    }



}
