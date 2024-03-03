package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class Number extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        this.astLeaf = new ASTLeaf(LeafType.Number, Integer.parseInt(Lexer.sym.getVal()));
        add(Lexer.getNextSym());
        add("<Number>");
        return sublist;
    }
}
