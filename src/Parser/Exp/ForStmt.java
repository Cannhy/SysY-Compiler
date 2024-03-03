package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class ForStmt extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        LVal lVal = new LVal();
        addAll(lVal.parse());
        ASTLeaf lValLeaf = lVal.getAstLeaf();
        add(Lexer.getNextSym());
        Exp exp = new Exp();
        addAll(exp.parse());
        this.astLeaf = new ASTLeaf(LeafType.ForStmt, lValLeaf, exp.getAstLeaf());
        add("<ForStmt>");
        return sublist;
    }
}
