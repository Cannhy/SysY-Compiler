package Parser.Func;

import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class FuncType extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        add(Lexer.getNextSym());
        add("<FuncType>");
        return sublist;
    }
}
