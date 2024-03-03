package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Lexer.Type;
import Parser.TokenParent;

import java.util.ArrayList;

public class LOrExp extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        LAndExp lAndExp = new LAndExp();
        addAll(lAndExp.parse());
        this.astLeaf = lAndExp.getAstLeaf();
        while (Lexer.wordTypeIs(0, Type.OR)) {
            add("<LOrExp>");
            add(Lexer.getNextSym());
            LAndExp lAndExp1 = new LAndExp();
            addAll(lAndExp1.parse());
            astLeaf = new ASTLeaf(LeafType.OR, astLeaf, lAndExp1.getAstLeaf());
        }
        add("<LOrExp>");
        return sublist;
    }
}
