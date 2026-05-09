package com.ecosystem.projectsservice.javaprojects.service.projects.constructors;


import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.BuildProperties;
import com.ecosystem.projectsservice.javaprojects.dto.projects.lifecycle.constructors.FileTemplateEnvelope;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.File;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;


/*
операции, связанные со структурой базовых компонентов maven проектов
 */
@Service
public class MavenPreparer {

    private List<String> classicalMavenFolderStructure
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


        // обновляем главный файл в pom.xml (редактируем template)





    }





    private void setArtifactID(BuildProperties properties,
                               List<FileTemplateEnvelope> templateEnvelopes){


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

        String artifactId = properties.getProject().getName()+"-project";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory
                    .newDocumentBuilder()
                    .parse(pomTemplate.getTemplateContent());

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
            throw new ConstructorException("ошибка построения проекта при подготовке pom.xml: "+e.getMessage());
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
