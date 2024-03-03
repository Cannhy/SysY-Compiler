package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Lexer.Type;
import Parser.TokenParent;

import java.util.ArrayList;

public class LAndExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        EqExp eqExp = new EqExp();
        addAll(eqExp.parse());
        this.astLeaf = eqExp.getAstLeaf();
        while (Lexer.wordTypeIs(0, Type.AND)) {
            add("<LAndExp>");
            add(Lexer.getNextSym());
            EqExp eqExp1 = new EqExp();
            addAll(eqExp1.parse());
            astLeaf = new ASTLeaf(LeafType.AND, astLeaf, eqExp1.getAstLeaf());
        }
        add("<LAndExp>");
        return sublist;
    }
}
