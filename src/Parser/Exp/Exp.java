package Parser.Exp;

import Parser.Parser;
import Parser.TokenParent;

import java.util.ArrayList;

public class Exp extends TokenParent {
    private int dimension = 114514;
    @Override
    public ArrayList<String> parse() {
        Parser.dimension = 0;
        AddExp addExp = new AddExp();
        addAll(addExp.parse());
        this.astLeaf = addExp.getAstLeaf();
        this.dimension = Parser.dimension;
        add("<Exp>");
        return sublist;
    }

    public int getAddressDimension() {
        return this.dimension;
    }
}
