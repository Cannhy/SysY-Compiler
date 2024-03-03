package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;
import Error.*;

import java.util.ArrayList;

public class FormatString extends TokenParent {
    @Override
    public ArrayList<String> parse(){
        String formatString = Lexer.sym.getVal();
        ErrorMaker.makeA(formatString.substring(1, formatString.length() - 1));
        add(Lexer.getNextSym()); // string
        this.astLeaf = new ASTLeaf(LeafType.FormatString, formatString);
        return sublist;
    }
}
