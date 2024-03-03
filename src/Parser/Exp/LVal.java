package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.*;
import Parser.Parser;
import Parser.TokenParent;
import Table.ConstType;
import Table.Symbol;
import Error.*;
import Table.TableType;

import java.util.ArrayList;

public class LVal extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        Symbol symbol = ErrorMaker.makeC(Lexer.sym, TableType.NOTFUNC);
        Parser.dimension = 114514;
        LeafKind leafKind = LeafKind.ARRAY;
        if (symbol != null) {
            Parser.dimension = symbol.getDimension();
            leafKind = symbol.getConstType().equals(ConstType.CONST) ? LeafKind.CONSTARRAY : LeafKind.ARRAY;
        }

        int dimension = Parser.dimension;
        ASTLeaf identLeaf = generateLeaf(Lexer.sym);
        identLeaf.setKind(LeafKind.INT);
        int dimens = 0;
        add(Lexer.getNextSym());
        while (Lexer.symValIs("[")) {
            identLeaf.setKind(leafKind);
            add(Lexer.getNextSym());
            Exp exp = new Exp();
            addAll(exp.parse());
            if (dimens == 0) identLeaf.setL(exp.getAstLeaf());
            else identLeaf.setR(exp.getAstLeaf());
            match("]");
            dimens++;
            identLeaf.setNum(dimens);
            dimension--;
        }
        Parser.dimension = dimension;
        astLeaf = identLeaf;
        add("<LVal>");
        return sublist;
    }
}
