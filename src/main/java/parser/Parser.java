package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Parser<A> extends VoidVisitorAdapter<A> {
    private final String FILE_PATH;
    private final String DATA_PATH;

    public Parser() {
        FILE_PATH = "src/main/test-files/CrapCode.java";
        DATA_PATH = "src/main/test-files/DataStructure.java";
    }

    public void parse() throws IOException {
        CompilationUnit cu1 = StaticJavaParser.parse(Files.newInputStream(Paths.get(FILE_PATH)));
        VisitorAdapter visitor = new VisitorAdapter();
        visitor.visit(cu1, null);
    }
}
