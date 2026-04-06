package com.ecosystem.projectsservice.javaprojects.service.state;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.springframework.stereotype.Service;

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

    @Override
    public String createEmptyPublicClass(String packagePath, String name) {

        String template = """
                package %s;
                
                public class %s {}
                
                """;








        return String.format(template, packagePath, name);
    }


}
