package com.ecosystem.projectsservice.javaprojects.service.projects.state.code;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class JdtCodeService implements CodeService {





    @Override
    public String transformPackage(String javaContent, String newPackageName)  {

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        Document document = new Document(javaContent);

        parser.setSource(document.get().toCharArray());


        CompilationUnit ast = (CompilationUnit) parser.createAST(null);



        ast.recordModifications();
        ast.setPackage(ast.getAST().newPackageDeclaration());
        ast.getPackage().setName(ast.getAST().newName(newPackageName));

        try {
            ast.rewrite(document, null).apply(document);
        } catch (BadLocationException e) {
            throw new CodeProcessingException("ошибка изменения package", "CODE_PROCESSING_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return document.get();
    }

    @Override
    public String transformFileConstructedPathToPackage(String constructedPath) {

        String[] fullPath = constructedPath.split("\\\\");



        // если проект называется com, то мы должны искать com директорию дальше
        try {

            int pointer = fullPath[0].equals("java")?1:0;
            boolean javaVisited = false;

            StringBuilder nameBuilder = new StringBuilder();
            for (int i = pointer; i<fullPath.length-1; i++){
                String folder = fullPath[i];

                if (javaVisited){

                    nameBuilder.append(folder);

                    if (i != fullPath.length-2){
                        nameBuilder.append(".");
                    }
                }

                else {
                    if (folder.equals("java")) {
                        javaVisited = true;
                    }
                }



            }



            return nameBuilder.toString();

        }
        catch (Exception e){
            throw
                    new CodeProcessingException("ошибка вычисления package",
                            "CODE_PROCESSING_ERROR",
                            HttpStatus.INTERNAL_SERVER_ERROR);
        }




    }

    @Override
    public String createEmptyPublicClass(String packagePath, String name) {

        String template = """
                package %s;
                
                public class %s {}
                
                """;








        return String.format(template, packagePath, name);
    }


}
