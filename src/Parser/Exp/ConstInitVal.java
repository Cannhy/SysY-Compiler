package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class ConstInitVal extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("{")) {
            add(Lexer.getNextSym());
            this.astLeaf = new ASTLeaf(LeafType.ConstInitVal);
            if (Lexer.symValIs("}")) add(Lexer.getNextSym());
            else {
                ConstInitVal constInitVal = new ConstInitVal();
                addAll(constInitVal.parse());
                astLeaf.addLeaf(constInitVal.getAstLeaf());
                while (Lexer.symValIs(",")) {
                    add(Lexer.getNextSym());
                    ConstInitVal constInitVal1 = new ConstInitVal();
                    addAll(constInitVal1.parse());
                    astLeaf.addLeaf(constInitVal1.getAstLeaf());
                }
                add(Lexer.getNextSym());
            }
        } else {
            ConstExp constExp = new ConstExp();
            addAll(constExp.parse());
            this.astLeaf = constExp.getAstLeaf();
        }
        add("<ConstInitVal>");
        return sublist;
    }
}
