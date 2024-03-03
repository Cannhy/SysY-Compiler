package Parser.Func;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Error.Error;
import Lexer.*;
import Error.*;
import Parser.Parser;
import Parser.TokenParent;
import Table.*;

import java.util.ArrayList;

public class FuncDef extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        String funcKind = Lexer.sym.getVal();
        FuncKind funckind = funcKind.equals("int") ? FuncKind.INT : FuncKind.VOID;
        LeafKind leafKind = funcKind.equals("int") ? LeafKind.RETURNINT : LeafKind.VOID;
        Parser.funckind = funckind;
        FuncType funcType = new FuncType();
        addAll(funcType.parse());

        Word ident = Lexer.sym;
        ASTLeaf identLeaf = new ASTLeaf(LeafType.Ident, ident.getVal());
        identLeaf.setKind(leafKind);
        add(Lexer.getNextSym());
        add(Lexer.getNextSym());

        Symbol symbol = new Symbol(ident.getVal(), ident, TableType.FUNC, ConstType.FUNCTYPE);
        symbol.setFuncKind(funckind);
        if (!ErrorMaker.makeB(symbol)) Parser.table.addSymbol(symbol);
        // 这块在函数内部新建了一个表 函数名字是属于全局表的
        Parser.makeTable(BlockType.FUNC);
        Parser.blockHasReturn = false;
        Parser.lastIsReturn = false;

        ArrayList<Symbol> params = new ArrayList<>();
        if (Lexer.symValIs(")")) add(Lexer.getNextSym());
        else if (Lexer.symValIs("{")) ErrorMaker.errors.add(new Error(Lexer.getLastToken(), "j"));
        else {
            FuncFParams funcFParams = new FuncFParams();
            addAll(funcFParams.parse());
            identLeaf.setL(funcFParams.getAstLeaf());
            params.addAll(Parser.table.getSymbols());
            match(")");
        }
        symbol.setParams(params);

        Block block = new Block();
        addAll(block.parse());
        ASTLeaf blockLeaf = block.getAstLeaf();

        Parser.outTable();
        ErrorMaker.makeG(Lexer.getLastToken());//因为前一个才是 }
        Parser.funckind = null;

        this.astLeaf = new ASTLeaf(LeafType.FuncDef, identLeaf, blockLeaf);

        add("<FuncDef>");
        return sublist;
    }
}
