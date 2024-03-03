package Parser.Decl;

import Parser.TokenParent;

import java.util.ArrayList;
import Lexer.*;

public class Decl extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("const")) {
            ConstDecl constDecl = new ConstDecl();
            addAll(constDecl.parse());
            this.astLeaf = constDecl.getAstLeaf();
        } else {
            VarDecl varDecl = new VarDecl();
            addAll(varDecl.parse());
            this.astLeaf = varDecl.getAstLeaf();
        }
        return sublist;
    }
}
