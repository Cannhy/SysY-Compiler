package Parser.Decl;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.*;
import Parser.Exp.ConstExp;
import Parser.Exp.ConstInitVal;
import Parser.Parser;
import Parser.TokenParent;

import java.util.ArrayList;

public class ConstDef extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        Word ident = Lexer.sym;

        ASTLeaf identLeaf = generateLeaf(ident);
        identLeaf.setKind(LeafKind.CONSTINT);

        boolean isArray = false;
        int dimen = 0;

        add(Lexer.getNextSym());
        while (Lexer.symValIs("[")) {
            isArray = true;
            add(Lexer.getNextSym());
            identLeaf.setKind(LeafKind.CONSTARRAY);

            ConstExp constExp = new ConstExp();
            addAll(constExp.parse());

            if (dimen == 0) identLeaf.setL(constExp.getAstLeaf());
            else identLeaf.setR(constExp.getAstLeaf());
            match("]");
            dimen++;
            identLeaf.setNum(dimen);
        }
        add(Lexer.getNextSym()); // =
        ConstInitVal constInitVal = new ConstInitVal();
        addAll(constInitVal.parse());
        this.astLeaf = new ASTLeaf(LeafType.ConstDef, identLeaf, constInitVal.getAstLeaf());

        Parser.addIntegerOrArray(ident, isArray, dimen, 1);

        add("<ConstDef>");
        return sublist;
    }
}
