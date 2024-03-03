package Parser;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.*;
import Parser.Decl.Decl;
import Parser.Func.FuncDef;
import Error.*;

import java.util.ArrayList;

public class CompUnit extends TokenParent{
    @Override
    public ArrayList<String> parse() {
        ErrorMaker.GlobalTable = Parser.table;
        ASTLeaf decl_leaf = new ASTLeaf(LeafType.Decl);
        ASTLeaf func_leaf = new ASTLeaf(LeafType.Func);

        while (isDecl()) {
            Decl decl = new Decl();
            addAll(decl.parse());
            decl_leaf.addLeaf(decl.astLeaf);
        }
        while (isFuncDef()) {
            FuncDef funcDef = new FuncDef();
            addAll(funcDef.parse());
            func_leaf.addLeaf(funcDef.astLeaf);
        }
        MainFuncDef mainFuncDef = new MainFuncDef();
        addAll(mainFuncDef.parse());

        ASTLeaf main_leaf = mainFuncDef.astLeaf;
        this.astLeaf = new ASTLeaf(LeafType.CompUnit, decl_leaf, func_leaf, main_leaf);

        add("<CompUnit>");
        return sublist;
    }

    private boolean isDecl() {
        return (Lexer.wordTypeIs(0, Type.CONSTTK)
                && Lexer.wordTypeIs(1, Type.INTTK) && Lexer.wordTypeIs(2,Type.IDENFR)) ||
                ((Lexer.wordTypeIs(0, Type.INTTK) && Lexer.wordTypeIs(1, Type.IDENFR)) &&
                        (Lexer.wordTypeIs(2, Type.LBRACK) || Lexer.wordTypeIs(2, Type.ASSIGN)
                        || Lexer.wordTypeIs(2, Type.COMMA) || Lexer.wordTypeIs(2, Type.SEMICN)
                        || !Lexer.wordTypeIs(2, Type.LPARENT)));
    }

    private boolean isFuncDef() {
        return ((Lexer.wordTypeIs(0, Type.INTTK) || Lexer.wordTypeIs(0, Type.VOIDTK)) &&
                Lexer.wordTypeIs(1, Type.IDENFR) && Lexer.wordTypeIs(2, Type.LPARENT));
    }
}
