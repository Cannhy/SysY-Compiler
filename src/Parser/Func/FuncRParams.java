package Parser.Func;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.Exp.Exp;
import Parser.TokenParent;

import java.util.ArrayList;

public class FuncRParams extends TokenParent {
    private int paraNum = 0;
    private ArrayList<Integer> addressDimensions = new ArrayList<>();

    @Override
    public ArrayList<String> parse() {
        this.paraNum = 1;
        this.astLeaf = new ASTLeaf(LeafType.FuncFParams);
        Exp exp = new Exp();
        addAll(exp.parse());
        astLeaf.addLeaf(exp.getAstLeaf());

        this.addressDimensions.add(exp.getAddressDimension());

        while (Lexer.symValIs(",")){
            add(Lexer.getNextSym());
            Exp exp1 = new Exp();
            addAll(exp1.parse());
            paraNum++;
            astLeaf.addLeaf(exp1.getAstLeaf());
            addressDimensions.add(exp1.getAddressDimension());
        }
        add("<FuncRParams>");
        return sublist;
    }

    public int getParaNum() {
        return this.paraNum;
    }

    public ArrayList<Integer> getAddressDimensions() {
        return addressDimensions;
    }
}
