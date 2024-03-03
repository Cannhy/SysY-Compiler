package Parser.Decl;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class VarDecl extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        add(Lexer.getNextSym());
        this.astLeaf = new ASTLeaf(LeafType.VarDecl);
        this.astLeaf.setKind(LeafKind.INT);
        VarDef varDef = new VarDef();
        addAll(varDef.parse());
        this.astLeaf.addLeaf(varDef.getAstLeaf());
        while (Lexer.symValIs(",")) {
            add(Lexer.getNextSym());
            VarDef varDef1 = new VarDef();
            addAll(varDef1.parse());
            this.astLeaf.addLeaf(varDef1.getAstLeaf());
        }
        match(";");
        add("<VarDecl>");
        return sublist;
    }
}
