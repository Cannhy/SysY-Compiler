package Parser.Func;

import AST.ASTLeaf;
import AST.LeafType;
import Lexer.*;
import Parser.Exp.*;
import Parser.Parser;
import Parser.TokenParent;
import Table.BlockType;

import java.util.ArrayList;
import Error.ErrorMaker;
import Error.Error;

public class Stmt extends TokenParent {
    @Override
    public ArrayList<String> parse() {
        if (Lexer.symValIs("if")) {
            add(Lexer.getNextSym());
            add(Lexer.getNextSym());

            Cond cond = new Cond();
            addAll(cond.parse());//cond
            ASTLeaf condLeaf = cond.getAstLeaf();
            match(")");

            Parser.makeTable(BlockType.IF);
            Stmt stmt = new Stmt();
            addAll(stmt.parse());
            ASTLeaf ifLeaf = stmt.getAstLeaf();
            Parser.outTable();
            ASTLeaf elseLeaf = null;
            if (Lexer.wordTypeIs(0, Type.ELSETK)) {
                add(Lexer.getNextSym());
                Parser.makeTable(BlockType.ELSE);
                Stmt stmt1 = new Stmt();
                addAll(stmt1.parse());
                elseLeaf = stmt1.getAstLeaf();
                Parser.outTable();
            }
            this.astLeaf = new ASTLeaf(LeafType.IfStatement, condLeaf, ifLeaf, elseLeaf);
        } else if (Lexer.symValIs("for")) {
            add(Lexer.getNextSym()); //for
            add(Lexer.getNextSym()); //(
            ASTLeaf forStmtLeaf1 = null;
            if (!Lexer.symValIs(";")) {
                ForStmt forStmt = new ForStmt();
                addAll(forStmt.parse());
                forStmtLeaf1 = forStmt.getAstLeaf();
            }
            match(";");
            ASTLeaf condLeaf = null;
            if (!Lexer.symValIs(";")) {
                Cond cond = new Cond();
                addAll(cond.parse());
                condLeaf = cond.getAstLeaf();
            }
            match(";");
            ASTLeaf forStmtLeaf2 = null;
            if (!Lexer.symValIs(")")) {
                ForStmt forStmt = new ForStmt();
                addAll((forStmt.parse()));
                forStmtLeaf2 = forStmt.getAstLeaf();
            }
            match(")");
            Parser.makeTable(BlockType.For);
            Parser.enterFor++;
            Stmt stmt = new Stmt();
            addAll(stmt.parse());
            this.astLeaf = new ASTLeaf(LeafType.For, forStmtLeaf1, condLeaf, forStmtLeaf2, stmt.getAstLeaf());
            Parser.enterFor--;
            Parser.outTable();
        } else if (Lexer.symValIs("break") || Lexer.symValIs("continue")) {
            if (Lexer.symValIs("continue")) this.astLeaf = new ASTLeaf(LeafType.Continue);
            else this.astLeaf = new ASTLeaf(LeafType.Break);
            ErrorMaker.makeM(Lexer.sym);
            add(Lexer.getNextSym());//break continue
            match(";");
        } else if (Lexer.symValIs("return")) {
            Word word = Lexer.sym;
            add(Lexer.getNextSym());
            Parser.blockHasReturn = false;
            Parser.lastIsReturn = false;
            ASTLeaf returnLeaf = null;
            if (Lexer.symValIs(";")) add(Lexer.getNextSym());
            else if (Lexer.symValIs("}")) ErrorMaker.errors.add(new Error(Lexer.getLastToken(), "i"));
            else {
                Exp exp = new Exp();
                addAll(exp.parse());//exp
                Parser.blockHasReturn = true;
                Parser.lastIsReturn = true;
                returnLeaf = exp.getAstLeaf();
                match(";");
            }
            ErrorMaker.makeF(word);
            this.astLeaf = new ASTLeaf(LeafType.Return, returnLeaf);
        } else if (Lexer.symValIs("printf")) {
            Word word = Lexer.sym;

            add(Lexer.getNextSym());
            add(Lexer.getNextSym());

            String str = Lexer.sym.getVal();
            FormatString formatString = new FormatString();
            addAll(formatString.parse());
            ASTLeaf formatLeaf = formatString.getAstLeaf();
            ASTLeaf expsLeaf = new ASTLeaf(LeafType.ExpList);

            int num = 0;
            while (Lexer.symValIs(",")) {
                add(Lexer.getNextSym());
                Exp exp = new Exp();
                addAll(exp.parse());
                expsLeaf.addLeaf(exp.getAstLeaf());
                num++;
            }
            match(")");
            match(";");
            this.astLeaf = new ASTLeaf(LeafType.Printf, formatLeaf, expsLeaf);
            ErrorMaker.makeL(word, str, num);
        } else if (Lexer.symValIs("{")) {
            Parser.makeTable(BlockType.BLOCK);
            Block block = new Block();
            addAll(block.parse());
            this.astLeaf = new ASTLeaf(LeafType.Block, block.getAstLeaf());
            Parser.lastIsReturn = false;
            Parser.outTable();
        } else if (Lexer.wordTypeIs(0, Type.IDENFR) && Lexer.lookAfterAssign()) {
            Word word = Lexer.sym;

            LVal lVal = new LVal();
            addAll(lVal.parse());
            add(Lexer.getNextSym()); // =
            ASTLeaf lvalLeaf = lVal.getAstLeaf();

            if (Lexer.symValIs("getint")) {
                add(Lexer.getNextSym());
                add(Lexer.getNextSym());
                match(")");
                match(";");
                this.astLeaf = new ASTLeaf(LeafType.Assign_getint, lvalLeaf, new ASTLeaf(LeafType.Getint));
            } else {
                Exp exp = new Exp();
                addAll(exp.parse());
                match(";");
                this.astLeaf = new ASTLeaf(LeafType.Assign_value, lvalLeaf, exp.getAstLeaf());
            }
            ErrorMaker.makeH(word);
        }
        else if (Lexer.wordTypeIs(0, Type.IDENFR) && Lexer.lookAfter2Add()) {
            Lexer.dealipp(Lexer.sym.getLineNumber(), Lexer.index, Lexer.word_list.get(Lexer.index).getVal());
            add(Lexer.getNextSym());add(Lexer.getNextSym());add(Lexer.getNextSym());
            this.astLeaf = new ASTLeaf(LeafType.IPP, null, null);
        }
        else {
            ASTLeaf expLeaf = null;
            if (!Lexer.symValIs(";")) {
                Exp exp = new Exp();
                addAll(exp.parse());
                match(";");
                expLeaf = exp.getAstLeaf();
            } else add(Lexer.getNextSym());
            this.astLeaf = new ASTLeaf(LeafType.Exp, expLeaf);
        }
        add("<Stmt>");
        return sublist;
    }
}
