package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class EqExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        RelExp relExp = new RelExp();
        addAll(relExp.parse());
        this.astLeaf = relExp.getAstLeaf();
        while (Lexer.symValIs("==") || Lexer.symValIs("!=")) {
            String operator = Lexer.sym.getVal();
            add("<EqExp>");
            add(Lexer.getNextSym());
            RelExp relExp1 = new RelExp();
            addAll(relExp1.parse());
            this.astLeaf = new ASTLeaf(LeafType.OPE, this.astLeaf, relExp1.getAstLeaf());
            this.astLeaf.setOpName(operator);
        }
        add("<EqExp>");
        return sublist;
    }
}
