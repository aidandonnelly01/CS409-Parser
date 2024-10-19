package parser;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class VisitorAdapter extends VoidVisitorAdapter {
    /**
     * This method visits each local variable in the AST
     * @param n VariableDeclarationExpr
     * @param arg Object
     */
    @Override
    public void visit(VariableDeclarationExpr n, Object arg) {
        //System.out.println("VariableDeclarationExpr visit");
        if (n.getVariables().size() > 1) {
            System.out.println("Smell detected! Too many variables declared at once here: " + n);
        } else if (StringUtils.countMatches(n.toString(), '=') == 0 ) {
            System.out.println("Smell detected! Variable not initialised on the same line it is declared: " + n);
        } else if (StringUtils.countMatches(n.toString(), '=') > 1 ) {
            System.out.println("Smell detected! Several variables assigned in a single statement: " + n);
        }
        super.visit(n, arg);
    }

    /**
     * This method visits the entire class, extracts the instance variables and prints any that are unnecessarily public
     * @param n
     * @param arg
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        /* If there are methods in a class it will consider a public method a smell. Otherwise, the class is considered
        a data structure and it gets passed over */
        List<MethodDeclaration> methods = n.getMethods();
        if (!methods.isEmpty()) {
            n.getFields().stream()
                    .filter(FieldDeclaration::isPublic)
                    .forEach(o -> System.out.println("Smell detected! Unnecessary public variable: " + o));
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(BlockStmt n, Object arg) {
        //System.out.println(n);
        super.visit(n, arg);
    }

    @Override
    public void visit(StringLiteralExpr n, Object arg) {
        System.out.println("Smell detected! Unnecessary string literal: " + n);
        super.visit(n, arg);
    }

    @Override
    public void visit(IntegerLiteralExpr n, Object arg) {
        if (n.asNumber().intValue() > 1 || n.asNumber().intValue() < -1 ) {
            System.out.println("Smell detected! Unnecessary integer literal: " + n.getParentNode());
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Object arg) {
        ArrayList<String> variableNames = new ArrayList<>();
        for (Expression e : n.getUpdate()) {
            variableNames.add(getTargetVariableName(e));
        }
        n.getBody().asBlockStmt().getStatements().stream()
                .filter(o -> o.asExpressionStmt().getExpression().isAssignExpr() || o.asExpressionStmt().getExpression().isUnaryExpr())
                .filter(o -> variableNames.contains(getTargetVariableName(o)))
                .forEach(o -> System.out.println("Smell detected! Loop iteration variable changed in the body of a loop: " + o));
        super.visit(n, arg);
    }

    private String getTargetVariableName(Statement s) {
        if (s.asExpressionStmt().getExpression().isAssignExpr()) {
            AssignExpr assignExpr = s.asExpressionStmt().getExpression().asAssignExpr();
            return assignExpr.getTarget().toString();
        } else if (s.asExpressionStmt().getExpression().isUnaryExpr()) {
            UnaryExpr unaryExpr = s.asExpressionStmt().getExpression().asUnaryExpr();
            return unaryExpr.getExpression().toString();
        }
        return "";
    }

    private String getTargetVariableName(Expression e) {
        if (e.toAssignExpr().isPresent()) {
            AssignExpr assignExpr = e.toAssignExpr().get();
            return assignExpr.getTarget().toString();
        } else if (e.toUnaryExpr().isPresent()) {
            UnaryExpr unaryExpr = e.toUnaryExpr().get();
            return unaryExpr.getExpression().toString();
        }
        return "";
    }

    public void visit(SwitchStmt n, Object arg) {
        //System.out.println(n);
        boolean isDefault = false;
        for (SwitchEntry e : n.getEntries()) {
            System.out.println("Switch entry: " + e);
            if (e.getLabels().isEmpty()) {
                System.out.println("default statement");
                isDefault = true;
            } else if (e.getStatements().isEmpty()) {
                //This works for empty statements
                System.out.println("empty statement");
            } else if (!e.getStatements().contains("break;")) {
                System.out.println("no break statement");
                //try getAllContainedComments
                //&& !e.getAllContainedComments().contains("fall through")) {
                //System.out.println("No break statement & No fall through statement here:" + e.getStatements());
            } else {
                System.out.println("Normal statement");
            }
           // for (Statement statement : e.getStatements()) {
                //System.out.println("statement : " + statement);
//                if (Statement.getStatements().contains("default")) {
//                    System.out.println("default statement");
//                } else if (e.getStatements().isEmpty()) {
//                    System.out.println("empty statement");
//                } else if (!e.getStatements().contains("break") && !e.getStatements().contains("fall through")) {
//                    System.out.println("No break statement & No fall through statement here:" + e.getStatements());
//                } else {
//                    System.out.println("Normal statement");
//                }
           // }
        }
        if (!isDefault) {
            System.out.println("No default statement: \n" + n);
        }
        //n.getEntries().stream().forEach(o -> System.out.println(o.getStatements()));
        //n.getEntries().stream().filter(o -> o.getStatements());
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        /*System.out.println(n.getBody());
        n.getBody().ifPresent(BlockStmt::getStatements);
         */
        super.visit(n, arg);
    }
}
