package parser;

import com.github.javaparser.Range;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

public class VisitorAdapter extends VoidVisitorAdapter {
    /**
     * This method visits each variable in the AST
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
    }
}
