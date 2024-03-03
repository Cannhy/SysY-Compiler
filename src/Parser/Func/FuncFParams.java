package Parser.Func;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class FuncFParams extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        FuncFParam funcFParam = new FuncFParam();
        this.astLeaf = new ASTLeaf(LeafType.FuncFParams);
        addAll(funcFParam.parse());
        astLeaf.addLeaf(funcFParam.getAstLeaf());
        while(Lexer.symValIs(",")) {
            add(Lexer.getNextSym());
            FuncFParam funcFParam1 = new FuncFParam();
            addAll(funcFParam1.parse());
            astLeaf.addLeaf(funcFParam1.getAstLeaf());
        }
        add("<FuncFParams>");
        return sublist;
    }
}