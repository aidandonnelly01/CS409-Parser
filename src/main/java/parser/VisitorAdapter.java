package parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

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
            n.getVariables().get(0).;
            Optional<Node> parentNode = n.getParentNode();
            if (parentNode.isPresent()) {
                if (parentNode.get().is)
            }
            //SwitchEntry nextEntry = entries.get(i + 1);
            /*Optional<Comment> comment = nextEntry.getComment();
            //If there is a comment, check it is a fall through comment, if there are no comments,
            //then it must be a smell
            if (comment.isPresent()) {
                if (!comment.get().getContent().contains("fall through")) {*/
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
        /* If there are methods in a class it will consider a public variable a smell. Otherwise, the class is considered
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
        boolean isDefault = false;
        NodeList<SwitchEntry> entries = n.getEntries();
        //Loops through every entry
        for (int i = 0; i < entries.size(); i++) {
            //Gets the current entry
            SwitchEntry currentEntry = entries.get(i);
            //Checks if there is a default statement included in the switch statement
            if (currentEntry.isDefault()) {
                isDefault = true;
            } else {
                //If there are statements in the case entry, it will perform this loop. This means that the empty case
                //exception is accounted for
                if (!currentEntry.getStatements().isEmpty()) {
                    //If the statement does not have one of the termination statements, such as break, continue or a thrown
                    //exception, it will enter this statement
                    if (!hasEndingStatement(currentEntry)) {
                        //If it's not the final entry in the switch statement do this
                        if (!(i == entries.size() - 1)) {
                            //In most of the tests, the fall through comment seems to be contained on the next switch entry,
                            //Which is why this is necessary to get the next statement
                            SwitchEntry nextEntry = entries.get(i + 1);
                            Optional<Comment> comment = nextEntry.getComment();
                            //If there is a comment, check it is a fall through comment, if there are no comments,
                            //then it must be a smell
                            if (comment.isPresent()) {
                                if (!comment.get().getContent().contains("fall through")) {
                                    System.out.println("Smell detected! Switch statement case not specified as a fall through: " + entries.get(i));
                                }
                            } else {
                                System.out.println("Smell detected! Switch statement case not specified as a fall through: " + entries.get(i));
                            }
                        }
                    }
                }
            }
        }
        if (!isDefault) {
            n.getTokenRange().ifPresent(o -> System.out.println("No default statement at switch statement which is at: " + o.getBegin().toString()));
        }
        super.visit(n, arg);
    }

    private boolean hasEndingStatement(SwitchEntry entry) {
        for (Statement statement : entry.getStatements()) {
            if (statement.isBreakStmt()) {
                return true;
            } else if (statement.isContinueStmt()) {
                return true;
            } else if (statement.isReturnStmt()) {
                return true;
            } else if (statement.isThrowStmt()) {
                return true;
            }
        }
        return false;
    }

}
