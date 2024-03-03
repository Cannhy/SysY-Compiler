package Parser.Func;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.*;
import Parser.Exp.ConstExp;
import Parser.Parser;
import Parser.TokenParent;

import java.util.ArrayList;

public class FuncFParam extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        add(Lexer.getNextSym());
        Word ident = Lexer.sym;
        ASTLeaf identLeaf = new ASTLeaf(LeafType.Ident, ident.getVal());
        identLeaf.setKind(LeafKind.INT);
        add(Lexer.getNextSym());

        boolean isArray = false;
        int dimen = 0;

        if(Lexer.symValIs("[")) {
            isArray = true;
            add(Lexer.getNextSym());//[
            match("]");
            dimen += 1;
            identLeaf.setKind(LeafKind.ARRAY);
            identLeaf.setNum(dimen);
            while (Lexer.symValIs("[")) {
                add(Lexer.getNextSym());//[
                ConstExp constExp = new ConstExp();
                addAll(constExp.parse());
                identLeaf.setR(constExp.getAstLeaf());
                match("]");
                dimen += 1;
                identLeaf.setNum(dimen);
            }
        }
        Parser.addIntegerOrArray(ident, isArray, dimen, 0);
        this.astLeaf = identLeaf;
        add("<FuncFParam>");
        return sublist;
    }
}
