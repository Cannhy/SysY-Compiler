package Parser;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.*;
import Error.*;
import Error.Error;

import java.util.ArrayList;
import java.util.Objects;

public class TokenParent {
    protected ArrayList<String> sublist;
    protected ASTLeaf astLeaf = null;

    public TokenParent() {
        this.sublist = new ArrayList<>();
    }

    public ArrayList<String> parse() {
        return null;
    }

    public void add(String str) {
        this.sublist.add(str);
    }

    public void addAll(ArrayList<String> strs) {
        this.sublist.addAll(strs);
    }

    public ASTLeaf getAstLeaf() {
        return this.astLeaf;
    }

    public ASTLeaf generateLeaf(Word ident) {
        return new ASTLeaf(LeafType.Ident, ident.getVal());
    }

    public boolean match(String str) {
        if (!Lexer.symValIs(str)) {
            if (str.equals(";")) ErrorMaker.errors.add(new Error(Lexer.getLastToken(),"i"));
            if (str.equals(")")) ErrorMaker.errors.add(new Error(Lexer.getLastToken(),"j"));
            if (str.equals("]")) ErrorMaker.errors.add(new Error(Lexer.getLastToken(),"k"));
            return false;
        }
        add(Lexer.getNextSym());
        return true;
    }
}
