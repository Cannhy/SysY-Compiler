package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class UnaryOp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        this.astLeaf = new ASTLeaf(LeafType.OPE);
        this.astLeaf.setOpName(Lexer.sym.getVal());
        add(Lexer.getNextSym());
        add("<UnaryOp>");
        return sublist;
    }
}
