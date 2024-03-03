package Parser.Exp;

import Parser.TokenParent;
import Lexer.*;

import java.util.ArrayList;

public class PrimaryExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("(")) {
            add(Lexer.getNextSym());
            Exp exp = new Exp();
            addAll(exp.parse());
            this.astLeaf = exp.getAstLeaf();
            match(")");
        } else if (Lexer.wordTypeIs(0, Type.IDENFR)) {
            LVal lVal = new LVal();
            addAll(lVal.parse());
            this.astLeaf = lVal.getAstLeaf();
        } else if (Lexer.wordTypeIs(0, Type.INTCON)) {
            Number number = new Number();
            addAll(number.parse());
            this.astLeaf = number.getAstLeaf();
        }
        add("<PrimaryExp>");
        return sublist;
    }
}
