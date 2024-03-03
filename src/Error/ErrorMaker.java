package Error;

import Lexer.*;
import Parser.Parser;
import Table.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ErrorMaker {
    public static final ArrayList<Error> errors = new ArrayList<>();
    public static Table GlobalTable = null;

    public static void outErrors() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("error.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (Error error : errors) {
            byte[] bytes = (error.toString()+"\n").getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    public static void makeA(String formatstring) {
        int pos = 0;
        while (pos < formatstring.length()) {
            int c = formatstring.charAt(pos);
            if (c == 32 || c == 33 || (c >= 40 && c != 92 && c <= 126)) pos++;
            else if (c == 92)
                if (pos + 1 < formatstring.length() && formatstring.charAt(pos + 1) == 'n') pos += 2;
                else {
                    errors.add(new Error(Lexer.sym,"a"));
                    break;
                }
            else if (c == 37)
                if (pos + 1 < formatstring.length() && formatstring.charAt(pos + 1) == 'd') pos += 2;
                else {
                    errors.add(new Error(Lexer.sym,"a"));
                    break;
                }
            else {
                errors.add(new Error(Lexer.sym,"a"));
                break;
            }
        }
    }

    public static boolean makeB(Symbol bSymbol) {
        for (Symbol symbol : Parser.table.getSymbols())
            if (symbol.getName().equals(bSymbol.getName())) {
                errors.add(new Error(bSymbol.getWord(),"b"));
                return true;
            }
        return false;
    }

    public static Symbol makeC(Word word, TableType tableType) {
        Symbol ans;
        if (tableType.equals(TableType.FUNC)) ans = GlobalTable.getSameSymbol(word.getVal());
        else ans = FindSameNameAllTable(word);
        if (ans == null) errors.add(new Error(word, "c"));
        return ans;
    }

    public static boolean makeD(Word word, Symbol symbol, int num) {
        int correctNum = symbol.getParamsLen();
        if(correctNum != num) errors.add(new Error(word,"d"));
        return correctNum != num;
    }

    public static void makeE(Word word, Symbol symbol, ArrayList<Integer> dimensions) {
        ArrayList<Integer> rightDimensions = symbol.getRightDimensions();
        for (int i = 0; i < dimensions.size(); i++) {
            if (!dimensions.get(i).equals(rightDimensions.get(i))) {
                errors.add(new Error(word, "e"));
                break;
            }
        }
    }

    public static void makeF(Word word) {
        if (Parser.funckind.equals(FuncKind.VOID) && Parser.blockHasReturn) errors.add(new Error(word, "f"));
    }

    public static void makeG(Word word) {
        if (Parser.funckind.equals(FuncKind.INT) && !Parser.lastIsReturn) errors.add(new Error(word, "g"));
    }

    public static void makeH(Word word) {
        Symbol symbol = FindSameNameAllTable(word);
        if(symbol == null){
            System.out.println("No define!");
            return;
        }
        if (symbol.getConstType().equals(ConstType.CONST)) errors.add(new Error(word, "h"));
    }

    public static void makeL(Word word, String string, int num) {
        int rightNum = string.split("%d").length - 1;
        if (rightNum != num) errors.add(new Error(word, "l"));
    }

    public static void makeM(Word word) {
        if (Parser.enterFor == 0) errors.add(new Error(word, "m"));
    }

    public static void makeTest(Word word, int l, int r) {
        if (l != r) errors.add(new Error(word, "test"));
    }

    public static Symbol FindSameNameAllTable(Word word) {
        Table table = Parser.table;
        Symbol symbol = table.getSameSymbol(word.getVal());
        if (symbol == null)
            while (table.getFatherTable() != null) {
                table = table.getFatherTable();
                symbol = table.getSameSymbol(word.getVal());
                if (symbol != null) return symbol;
            }
        return symbol;
    }
}
