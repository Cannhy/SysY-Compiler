package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class AddExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        MulExp mulExp = new MulExp();
        addAll(mulExp.parse());
        this.astLeaf = mulExp.getAstLeaf();
        while (Lexer.symValIs("+") || Lexer.symValIs("-")) {
            String operator = Lexer.sym.getVal();
            add("<AddExp>");
            add(Lexer.getNextSym());
            MulExp mulExp1 = new MulExp();
            addAll(mulExp1.parse());
            ASTLeaf OPRoot = new ASTLeaf(LeafType.OPE, this.astLeaf, mulExp1.getAstLeaf());
            OPRoot.setOpName(operator);
            this.astLeaf = OPRoot;
        }
        add("<AddExp>");
        return sublist;
    }
}
