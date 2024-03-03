package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class MulExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        UnaryExp unaryExp = new UnaryExp();
        addAll(unaryExp.parse());
        this.astLeaf = unaryExp.getAstLeaf();
        while (Lexer.symValIs("*") || Lexer.symValIs("/") || Lexer.symValIs("%") || Lexer.symValIs("bitand")) {
            String operator = Lexer.sym.getVal();
            add("<MulExp>");
            add(Lexer.getNextSym());
            UnaryExp unaryExp1 = new UnaryExp();
            addAll(unaryExp1.parse());
            ASTLeaf OPRoot = new ASTLeaf(LeafType.OPE, this.astLeaf, unaryExp1.getAstLeaf());
            OPRoot.setOpName(operator);
            this.astLeaf = OPRoot;
        }
        add("<MulExp>");
        return sublist;
    }
}
