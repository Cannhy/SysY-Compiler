package Parser.Decl;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.*;
import Parser.Exp.Exp;
import Parser.TokenParent;

import java.util.ArrayList;

public class InitVal extends TokenParent {
    public int initNum = 0;
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("{")){
            add(Lexer.getNextSym());
            this.astLeaf = new ASTLeaf(LeafType.InitVal);
            if(Lexer.symValIs("}")) add(Lexer.getNextSym());// }
            else {
                initNum++;
                InitVal initVal = new InitVal();
                addAll(initVal.parse());
                this.astLeaf.addLeaf(initVal.getAstLeaf());
                while (Lexer.symValIs(",")){
                    initNum++;
                    add(Lexer.getNextSym());//,
                    InitVal initVal1 = new InitVal();
                    addAll(initVal1.parse());
                    this.astLeaf.addLeaf(initVal1.getAstLeaf());
                }
                add(Lexer.getNextSym());// }
            }
        } else {
            Exp exp = new Exp();
            addAll(exp.parse());
            this.astLeaf = exp.getAstLeaf();
        }
        add("<InitVal>");
        return sublist;
    }
}
