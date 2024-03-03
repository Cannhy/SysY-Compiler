package Parser.Func;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.Lexer;
import Parser.Decl.Decl;
import Parser.TokenParent;

import java.util.ArrayList;

public class BlockItem extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("const") || Lexer.symValIs("int")) {
            this.astLeaf = new ASTLeaf(LeafType.BlockItem_Decl);
            Decl decl = new Decl();
            addAll(decl.parse());
            this.astLeaf.setL(decl.getAstLeaf());
        } else {
            this.astLeaf = new ASTLeaf(LeafType.BlockItem_Stmt);
            Stmt stmt = new Stmt();
            addAll(stmt.parse());
            this.astLeaf.setL(stmt.getAstLeaf());
        }
        return sublist;
    }
}
