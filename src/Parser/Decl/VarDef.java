package Parser.Decl;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Parser.Exp.ConstExp;
import Parser.Parser;
import Parser.TokenParent;
import Lexer.*;
import Error.*;

import java.util.ArrayList;

public class VarDef extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        Word ident = Lexer.sym;
        add(Lexer.getNextSym());
        ASTLeaf identLeaf = generateLeaf(ident);
        identLeaf.setKind(LeafKind.INT);

        boolean isArray = false;
        int dimen = 0;
        int arraySize = 0, initSize = 0;
        while (Lexer.symValIs("[")) {
            identLeaf.setKind(LeafKind.ARRAY);
            isArray = true;
            add(Lexer.getNextSym());
            arraySize = Integer.parseInt(Lexer.sym.getVal());
            //System.out.println(size);
            ConstExp constExp = new ConstExp();
            addAll(constExp.parse());
            if (dimen == 0) identLeaf.setL(constExp.getAstLeaf());
            else identLeaf.setR(constExp.getAstLeaf());
            match("]");
            dimen++;
            identLeaf.setNum(dimen);
        }
        ASTLeaf initValLeaf = null;
        if (Lexer.symValIs("=")) {
            add(Lexer.getNextSym());
            if (Lexer.symValIs("getint")) {
                int line = Lexer.sym.getLineNumber(), index = Lexer.index;
                add(Lexer.getNextSym()); add(Lexer.getNextSym()); add(Lexer.getNextSym());
                Lexer.dealGetInt(line, index, ident.getVal());
            }
            else {
                InitVal initVal = new InitVal();
                addAll(initVal.parse());
                initValLeaf = initVal.getAstLeaf();
                //System.out.println(initVal.initNum);
                initSize = initVal.initNum;
            }
        }
        ErrorMaker.makeTest(ident, arraySize, initSize);
        Parser.addIntegerOrArray(ident, isArray, dimen, 0);
        this.astLeaf = new ASTLeaf(LeafType.VarDef, identLeaf, initValLeaf);
        add("<VarDef>");
        return sublist;
    }
}
