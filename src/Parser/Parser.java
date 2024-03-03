package Parser;

import AST.ASTLeaf;
import Lexer.*;
import Table.*;
import Error.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private final ArrayList<String> grammerList;
    public static FuncKind funckind = null;
    public static boolean blockHasReturn = false;
    public static boolean lastIsReturn = false;
    public static int enterFor = 0;
    public static int dimension = 0;
    public static Table table = new Table(null, BlockType.GLOBAL);
    public ASTLeaf AST = null;

    public Parser() {
        this.grammerList = new ArrayList<>();
        Lexer.sym = Lexer.word_list.get(0);
        Lexer.index = 0;
    }
    public void parseSource() {
        CompUnit compUnit = new CompUnit();
        grammerList.addAll(compUnit.parse());
        //outGrammer();
        this.AST = compUnit.getAstLeaf();
    }

    public ASTLeaf getAST() {
        return this.AST;
    }

    public static void addIntegerOrArray(Word ident, boolean isArray, int dimen, int type) {
        TableType tableType = isArray? TableType.ARRAY:TableType.INTEGER;
        ConstType constType = type == 0 ? ConstType.VAR : ConstType.CONST;
        Symbol newSymbol = new Symbol(ident.getVal(),ident,tableType, constType);
        if(isArray) newSymbol.setDimension(dimen,0,0);
        if (!ErrorMaker.makeB(newSymbol)) Parser.table.addSymbol(newSymbol);
    }

    public static void makeTable(BlockType blockType) {
        Table newTable = new Table(Parser.table,blockType);
        Parser.table.addSon(newTable);
        Parser.table = newTable;
    }

    public static void outTable(){
        Parser.table = Parser.table.getFatherTable();
    }

    public void outGrammer() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("output.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (String grammer : grammerList) {
            byte[] bytes = (grammer+"\n").getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }
}
