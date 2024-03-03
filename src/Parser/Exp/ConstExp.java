package Parser.Exp;

import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class ConstExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        AddExp addExp = new AddExp();
        addAll(addExp.parse());
        this.astLeaf = addExp.getAstLeaf();
        add("<ConstExp>");
        return sublist;
    }
}
