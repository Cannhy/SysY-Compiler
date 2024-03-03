package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class RelExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        AddExp addExp = new AddExp();
        addAll(addExp.parse());
        this.astLeaf = addExp.getAstLeaf();
        while (Lexer.symValIs("<") || Lexer.symValIs(">") || Lexer.symValIs("<=") || Lexer.symValIs(">=")) {
            add("<RelExp>");
            String operator = Lexer.sym.getVal();
            add(Lexer.getNextSym());
            AddExp addExp1 = new AddExp();
            addAll(addExp1.parse());
            this.astLeaf = new ASTLeaf(LeafType.OPE, astLeaf, addExp1.getAstLeaf());
            this.astLeaf.setOpName(operator);
        }
        add("<RelExp>");
        return sublist;
    }
}
