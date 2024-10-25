package parser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class VisitorAdapter extends VoidVisitorAdapter {

    @Override
    public void visit(VariableDeclarationExpr n, Object arg) {
        if (n.getVariables().size() > 1) {
            Optional<Node> parentNodeOp = n.getParentNode();
            if (parentNodeOp.isPresent()) {
                Node parentNode = parentNodeOp.get();
                if (!(parentNode instanceof ForStmt)) {
                    System.out.println("Smell detected! Too many variables declared at once here: " + n);
                }
            } else {
                System.out.println("Smell detected! Too many variables declared at once here: " + n);
            }
        } else if (StringUtils.countMatches(n.toString(), '=') == 0) {
            System.out.println("Smell detected! Variable not initialised on the same line it is declared: " + n);
        } else if (StringUtils.countMatches(n.toString(), '=') > 1) {
            System.out.println("Smell detected! Several variables assigned in a single statement: " + n);
        }
        super.visit(n, arg);
    }

    //@Override
/*    public void visit(MethodDeclaration n, Object arg) {
        //doubleDeclaration(n, arg);
        String variableName = getGetterOrSetterVariableNames(n);
        if (!variableName.isEmpty()) {
            String methodName = n.getNameAsString();
            if (variableName.charAt(0) == 'g' && !(methodName.equalsIgnoreCase("get" + variableName.substring(1)))) {
                System.out.println("Smell detected! Accessor method named incorrectly: " + n.getNameAsString());
            } else if (variableName.charAt(0) == 's'
                    && !(methodName.equalsIgnoreCase("set" + variableName.substring(1)))) {
                System.out.println("Smell detected! Mutator method named incorrectly: " + n.getNameAsString());
            }
        }
        super.visit(n, arg);
    }*/

    private String getGetterOrSetterVariableNames(MethodDeclaration n) {
        Optional<BlockStmt> body = n.getBody();
        if (body.isPresent() && body.get().getStatements().size() == 1) {
            Optional<Statement> firstStmt = body.get().getStatements().getFirst();
            if (firstStmt.isPresent()) {
                Statement stmt = firstStmt.get();
                if (stmt.isReturnStmt()) {
                    Optional<Expression> opStmt = stmt.asReturnStmt().getExpression();
                    if (opStmt.isPresent()) {
                        return "g" + opStmt.get().asNameExpr().getName();
                    }
                } else if (stmt.isExpressionStmt()) {
                    return "s" + stmt.asExpressionStmt().getExpression().asAssignExpr().getTarget();
                }
            }
        }
        return "";
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        /*
         * If there are methods in a class it will consider a public variable a smell.
         * Otherwise, the class is considered
         * a data structure and it gets passed over
         */
        List<FieldDeclaration> fields = n.getFields();
        List<String> passedFields = new ArrayList<>();
        for (FieldDeclaration fieldDeclaration : fields) {
            if (!fieldDeclaration.isPublic()) {
                Node field = fieldDeclaration.removeComment();
                String[] fieldSlice = field.toString().split(" ");
                for (int i = 2; i < fieldSlice.length; i++) {
                    passedFields.add(fieldSlice[i].replace(',', ' ').strip());
                }
            }
        }
        NodeList<BodyDeclaration<?>> nestedClasses = n.getMembers();
        nestedClasses.forEach(x -> {
            if (x.isClassOrInterfaceDeclaration()) {
                ClassOrInterfaceDeclaration fun = x.asClassOrInterfaceDeclaration();
                String v = fun.getAccessSpecifier().asString();
                if (v == "public") {
                    checkForPrivateFields(fun, passedFields);
                }
            }
        });

        List<MethodDeclaration> methods = n.getMethods();
        if (!methods.isEmpty()) {
            n.getFields().stream()
                    .filter(FieldDeclaration::isPublic)
                    .forEach(o -> System.out.println("Smell detected! Unnecessary public variable: " + o));
        }
        super.visit(n, arg);
    }

    void checkForPrivateFields(ClassOrInterfaceDeclaration selClass, List<String> fields) {
        List<MethodDeclaration> methods = selClass.getMethods();
        for (MethodDeclaration methodDeclaration : methods) {
            NodeList<Statement> statements = methodDeclaration.getBody().get().getStatements();
            NodeList<Statement> checkStatements = new NodeList<>();
            while (statements.isNonEmpty()) {
                Statement statement = statements.get(0);
                if (statement.isReturnStmt()) {
                    ReturnStmt rStmt = statement.asReturnStmt();
                    Expression nameExp = rStmt.getExpression().get();
                    String name = nameExp.asNameExpr().getNameAsString();
                    if (!fields.contains(name)){
                        System.out.println("Smell detected! Public nested class " + selClass.getNameAsString()
                                    + " is accessing private variable " + name);
                    }
                }
                if (statement.isExpressionStmt()) {
                    checkStatements.add(statement);
                } else {
                    NodeList<Statement> retStatements = getStatementsFromStatement(statement);
                    if (retStatements != null) {
                        statements.addAll(retStatements);
                        checkStatements.addAll(retStatements);
                    }
                }
                statements.remove(0);
            }
            for (Statement statement : checkStatements) {
                if (statement.isExpressionStmt()) {
                    Node nameNode = getVariable(statement);
                    if (nameNode != null && fields.contains(nameNode.toString())) {
                        System.out.println("Smell detected! Public nested class " + selClass.getNameAsString()
                                + " is accessing private variable " + nameNode.toString());
                    }
                }
            }
        }
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
    public void visit(LongLiteralExpr n, Object arg) {
        System.out.println("Smell detected! Unnecessary decimal literal: " + n);
        super.visit(n, arg);
    }

    @Override
    public void visit(DoubleLiteralExpr n, Object arg) {
        System.out.println("Smell detected! Unnecessary decimal literal: " + n);
        super.visit(n, arg);
    }

    @Override
    public void visit(CharLiteralExpr n, Object arg) {
        System.out.println("Smell detected! Unnecessary char literal: " + n);
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Object arg) {
        ArrayList<String> variableNames = new ArrayList<>();
        for (Expression e : n.getUpdate()) {
            variableNames.add(getTargetVariableName(e));
        }
        NodeList<Statement> statements = n.getBody().asBlockStmt().getStatements();
        NodeList<Statement> checkStatements = new NodeList<>();
        while (statements.isNonEmpty()) {
            Statement statement = statements.get(0);
            if (statement.isExpressionStmt() && !checkStatements.contains(statement)) {
                checkStatements.add(statement);
            } else {
                if (statement.isForStmt()) {
                    ForStmt fStmt = statement.asForStmt();
                    for (Expression e : fStmt.getUpdate()) {
                        variableNames.add(getTargetVariableName(e));
                    }
                }
                NodeList<Statement> retStatements = getStatementsFromStatement(statement);
                if (retStatements != null) {
                    statements.addAll(retStatements);
                    checkStatements.addAll(retStatements);
                }
            }
            statements.remove(0);
        }
        for (Statement statement : checkStatements) {
            if (statement.isExpressionStmt()) {
                String variable = getTargetVariableName(statement);
                if (variable != "" && variableNames.contains(variable)) {
                    System.out.println(
                            "Smell detected! Loop iteration variable changed in the body of a loop: " + variable);
                }
            }
        }
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

    @Override
    public void visit(SwitchStmt n, Object arg) {
        NodeList<SwitchEntry> entries = n.getEntries();
        // Loops through every entry
        for (int i = 0; i < entries.size(); i++) {
            // Gets the current entry
            SwitchEntry currentEntry = entries.get(i);
            //Checks if there is a default statement included in the switch statement
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
                            if (!comment.get().getContent().toLowerCase().contains("fall through")) {
                                System.out.println("Smell detected! Switch statement case not specified as a fall through: " + entries.get(i));
                            }
                        } else {
                            System.out.println("Smell detected! Switch statement case not specified as a fall through: " + entries.get(i));
                        }
                    }
                }
            }
        }
        Optional<SwitchEntry> entry = entries.getLast();
        boolean isDefault = false;
        if (entry.isPresent()) {
            SwitchEntry currentEntry = entry.get();
            if (currentEntry.isDefault()) {
                isDefault = true;
            }
        }
        if (!isDefault) {
            n.getTokenRange().ifPresent(o -> System.out.println("Smell detected! The last statement in this switch statement is not a default statement: " + o.getBegin().toString()));
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

    @Override
    public void visit(TryStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, Object arg) {
        NodeList<Statement> statements = n.getBody().getStatements();
        if (statements.isEmpty()) {
            if (!n.getParameter().toString().contains("expected")) {
                if (n.getAllContainedComments().isEmpty()) {
                    System.out.println("Smell detected! No response to caught exception and no comment provided! : " + n);
                }
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        List<String> declaredVariables = new ArrayList<>();
        List<String> detectedVariables = new ArrayList<>();
        NodeList<Statement> statements = n.getBody().get().getStatements();
        ArrayList<Statement> statements1 = new ArrayList<>(statements);
        NodeList<Statement> checkStatements = new NodeList<>();
        while (!statements1.isEmpty()) {
            Statement statement = statements1.get(0);
            if (statement.isExpressionStmt()) {
                checkStatements.add(statement);
            } else {
                NodeList<Statement> retStatements = getStatementsFromStatement(statement);
                if (retStatements != null) {
                    statements1.addAll(retStatements);
                    checkStatements.addAll(retStatements);
                }
            }

            statements1.remove(0);
        }
        for (Statement statement : checkStatements) {
            if (statement.isExpressionStmt()) {
                Node nameNode = getVariable(statement);
                if (nameNode != null && !declaredVariables.contains(nameNode.toString())) {
                    declaredVariables.add(nameNode.toString());
                } else if (nameNode != null && !detectedVariables.contains(nameNode.toString())) {
                    detectedVariables.add(nameNode.toString());
                    System.out.println("Smell detected! Variable " + nameNode.toString()
                            + " declared more than once!");
                }
            }
        }
        String variableName = getGetterOrSetterVariableNames(n);
        if (!variableName.isEmpty()) {
            String methodName = n.getNameAsString();
            if (variableName.charAt(0) == 'g' && !(methodName.equalsIgnoreCase("get" + variableName.substring(1)))) {
                System.out.println("Smell detected! Accessor method named incorrectly: " + n.getNameAsString());
            } else if (variableName.charAt(0) == 's'
                    && !(methodName.equalsIgnoreCase("set" + variableName.substring(1)))) {
                System.out.println("Smell detected! Mutator method named incorrectly: " + n.getNameAsString());
            }
        }
        super.visit(n, arg);
    }

/*    void doubleDeclaration(MethodDeclaration n, Object arg) {
        List<String> declaredVariables = new ArrayList<>();
        List<String> detectedVariables = new ArrayList<>();
        NodeList<Statement> statements = n.getBody().get().getStatements();
        NodeList<Statement> checkStatements = new NodeList<>();
        while (statements.isNonEmpty()) {
            Statement statement = statements.get(0);
            *//*if (statement.isForStmt()) {
                visitForStmt(statement.asForStmt());
            }*//*
            if (statement.isExpressionStmt()) {
                checkStatements.add(statement);
            } else {
                NodeList<Statement> retStatements = getStatementsFromStatement(statement);
                if (retStatements != null) {
                    statements.addAll(retStatements);
                    checkStatements.addAll(retStatements);
                }
            }

            statements.remove(0);
        }
        for (Statement statement : checkStatements) {
            if (statement.isExpressionStmt()) {
                Node nameNode = getVariable(statement);
                if (nameNode != null && !declaredVariables.contains(nameNode.toString())) {
                    declaredVariables.add(nameNode.toString());
                } else if (nameNode != null && !detectedVariables.contains(nameNode.toString())) {
                    detectedVariables.add(nameNode.toString());
                    System.out.println("Smell detected! Variable " + nameNode.toString()
                            + " declared more than once!");
                }
            }
        }
    }*/

    private Node getVariable(Statement statement) {
        ExpressionStmt expStmt = statement.asExpressionStmt();
        Expression expression = expStmt.getExpression();
        VariableDeclarationExpr variable = null;
        if (expression.isVariableDeclarationExpr()) {
            variable = expression.asVariableDeclarationExpr();
            NodeList<VariableDeclarator> children = variable.getVariables();
            for (VariableDeclarator chVariableDeclarator : children) {
                List<Node> chNodes = chVariableDeclarator.getChildNodes();
                Node nameNode = chNodes.get(1);
                return nameNode;
            }
        }
        return null;
    }

    private NodeList<Statement> getStatementsFromStatement(Statement statement) {
        if (statement.isIfStmt()) {
            IfStmt stmt = statement.asIfStmt();
            BlockStmt thenStmt = stmt.getThenStmt().asBlockStmt();
            return thenStmt.getStatements();
        } else if (statement.isWhileStmt()) {
            BlockStmt whileStmt = statement.asWhileStmt().getBody().asBlockStmt();
            return whileStmt.getStatements();
        } else if (statement.isForStmt()) {
            BlockStmt forStmt = statement.asForStmt().getBody().asBlockStmt();
            return forStmt.getStatements();
        }
        return null;
    }

}
