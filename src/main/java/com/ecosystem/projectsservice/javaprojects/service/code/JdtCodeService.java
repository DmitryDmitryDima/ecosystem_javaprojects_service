package com.ecosystem.projectsservice.javaprojects.service.code;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class JdtCodeService implements CodeService{





    @Override
    public String transformPackage(String javaContent, String newPackageName) throws Exception {

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        Document document = new Document(javaContent);

        parser.setSource(document.get().toCharArray());
        CompilationUnit ast = (CompilationUnit) parser.createAST(null);


        ast.recordModifications();
        ast.setPackage(ast.getAST().newPackageDeclaration());
        ast.getPackage().setName(ast.getAST().newName(newPackageName));

        ast.rewrite(document, null).apply(document);


        return document.get();
    }
}
