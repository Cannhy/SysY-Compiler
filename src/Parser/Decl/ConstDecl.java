package Parser.Decl;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.Lexer;
import Parser.TokenParent;

import java.util.ArrayList;

public class ConstDecl extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        add(Lexer.getNextSym());
        add(Lexer.getNextSym());

        this.astLeaf = new ASTLeaf(LeafType.ConstDecl);
        this.astLeaf.setKind(LeafKind.INT);

        ConstDef constDef = new ConstDef();
        addAll(constDef.parse());
        this.astLeaf.addLeaf(constDef.getAstLeaf());

        while (Lexer.symValIs(",")) {
            add(Lexer.getNextSym());//,
            ConstDef constDef1 = new ConstDef();
            addAll(constDef1.parse());
            this.astLeaf.addLeaf(constDef1.getAstLeaf());
        }
        match(";");
        add("<ConstDecl>");
        return sublist;
    }
}
