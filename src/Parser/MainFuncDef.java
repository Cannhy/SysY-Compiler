package Parser;

import Lexer.*;
import Parser.Func.Block;
import Table.*;
import Error.*;

import java.util.ArrayList;

public class MainFuncDef extends TokenParent{
    @Override
    public ArrayList<String> parse() {
        Parser.funckind = FuncKind.INT;
        add(Lexer.getNextSym());
        Word ident = Lexer.sym;
        add(Lexer.getNextSym());
        add(Lexer.getNextSym());
        match(")");

        Parser.makeTable(BlockType.MAIN);
        Parser.blockHasReturn = false;
        Parser.lastIsReturn = false;

        Block block = new Block();
        addAll(block.parse());
        this.astLeaf = block.getAstLeaf();

        Symbol symbol = new Symbol(ident.getVal(), ident, TableType.FUNC, ConstType.FUNCTYPE);
        symbol.setFuncKind(FuncKind.INT);
        symbol.setParams(Parser.table.getSymbols());
        Parser.table.addSymbol(symbol);

        Parser.outTable();
        ErrorMaker.makeG(Lexer.sym);

        Parser.funckind = null;
        add("<MainFuncDef>");
        return sublist;
    }
}
