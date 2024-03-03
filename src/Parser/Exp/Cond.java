package Parser.Exp;

import Parser.TokenParent;

import java.util.ArrayList;

public class Cond extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        LOrExp lOrExp = new LOrExp();
        addAll(lOrExp.parse());
        this.astLeaf = lOrExp.getAstLeaf();
        add("<Cond>");
        return sublist;
    }
}
