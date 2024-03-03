import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import Lexer.Lexer;
import MidCodeGenerate.MidCode;
import MidCodeGenerate.MidGenerate;
import MipsGenerate.*;
import Parser.Parser;
import Error.ErrorMaker;

public class Compiler {
    public static void main(String[] args) throws IOException {
        try {
            System.setIn(new FileInputStream("testfile.txt"));
            Scanner scanner = new Scanner(System.in);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNext()) sb.append(scanner.nextLine()).append("\n");
            Lexer lexer = new Lexer(sb.append('`').toString());
            lexer.lexSource();
            Parser parser = new Parser();
            parser.parseSource();
            ErrorMaker.outErrors();
            boolean debug = ErrorMaker.errors.size() == 0;
            //boolean debug = true;
            if (debug) {
                MidGenerate midGenerate = new MidGenerate(parser.getAST());
                ArrayList<MidCode> midList = midGenerate.generate();
                outMidCode(midList);
                //outMidCodeBAOpt(midList);
                MipsGenerate mipsGenerate = new MipsGenerate(midList);
                mipsGenerate.optimize = true;
                ArrayList<Mips> mips = mipsGenerate.generate();
                outMipsCode(mips);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outMidCode(ArrayList<MidCode> midCodes) throws IOException{
        FileOutputStream fileOutputStream = new FileOutputStream("midcode.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (MidCode word : midCodes) {
            byte[] bytes = (word.getStr()+"\n").getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    public static void outMidCodeBAOpt(ArrayList<MidCode> midCodes) throws IOException{
        FileOutputStream fileOutputStream = new FileOutputStream("testfilei_20375173_陈浩宇_优化前中间代码.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (MidCode word : midCodes) {
            byte[] bytes = (word.getStr()+"\n").getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();

        FileOutputStream fileOutputStream1 = new FileOutputStream("testfilei_20375173_陈浩宇_优化后中间代码.txt");
        BufferedOutputStream bufferedOutputStream1 = new BufferedOutputStream(fileOutputStream1);
        for (MidCode word : midCodes) {
            byte[] bytes = (word.getStr()+"\n").getBytes();
            bufferedOutputStream1.write(bytes);
        }
        bufferedOutputStream1.flush();
        bufferedOutputStream1.close();
    }

    public static void outMipsCode(ArrayList<Mips> mips) throws IOException{
        FileOutputStream fileOutputStream = new FileOutputStream("mips.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (Mips word : mips) {
            byte[] bytes = (word.toMipsString().toString()).getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }
}
