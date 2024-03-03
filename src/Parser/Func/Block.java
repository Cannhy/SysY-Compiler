package Parser.Func;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.*;
import Parser.Parser;
import Parser.TokenParent;

import java.util.ArrayList;

public class Block extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        ASTLeaf blockLeaf = new ASTLeaf(LeafType.Block);
        add(Lexer.getNextSym());
        while (!Lexer.symValIs("}")) {
            Parser.lastIsReturn = false;
            BlockItem blockItem = new BlockItem();
            addAll(blockItem.parse());
            blockLeaf.addLeaf(blockItem.getAstLeaf());
        }
        add(Lexer.getNextSym());
        this.astLeaf = blockLeaf;
        add("<Block>");
        return sublist;
    }
}
