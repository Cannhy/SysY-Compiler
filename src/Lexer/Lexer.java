package Lexer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private String source;
    private int lineNumber = 1;
    private int pos = 0;
    private final HashMap<String, Type> word2type;
    public static ArrayList<Word> word_list = new ArrayList<>();
    public static Word sym = null;
    public static int index = 0;

    public Lexer(String str) {
        this.source = str;
        this.word2type = new HashMap<>();
        init_word2type();
    }

    public void lexSource() {
        while (parseWord());
        /*try {
            parseOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    public void parseOutput() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("output.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (Word word : word_list) {
            byte[] bytes = (word.getStr()+"\n").getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    public static boolean lookAfterAssign() {
        int off = 1, cur_line = word_list.get(index).getLineNumber();
            while (index + off < word_list.size()) {
                Word word = word_list.get(index + off);
                if (word.getVal().equals(";") || word.getLineNumber() > cur_line) break;
                else if (word.getVal().equals("=")) return true;
                off++;
            }
            return false;
    }

    public static boolean lookAfter2Add() {
        int off = 1, cur_line = word_list.get(index).getLineNumber();
        int cnt = 0;
        while (index + off+1 < word_list.size()) {
            Word word = word_list.get(index + off);
            if (word.getVal().equals(";") || word.getLineNumber() > cur_line) break;
            else if (word.getVal().equals("+") && word_list.get(index + off+1).getVal().equals("+")) {
                return true;
            }
            off++;
        }
        return false;
    }

    public void init_word2type() {
        word2type.put("main", Type.MAINTK);
        word2type.put("const", Type.CONSTTK);
        word2type.put("int", Type.INTTK);
        word2type.put("break", Type.BREAKTK);
        word2type.put("continue", Type.CONTINUETK);
        word2type.put("if", Type.IFTK);
        word2type.put("else", Type.ELSETK);
        word2type.put("for", Type.FORTK);
        word2type.put("getint", Type.GETINTTK);
        word2type.put("printf", Type.PRINTFTK);
        word2type.put("return", Type.RETURNTK);
        word2type.put("void", Type.VOIDTK);
        word2type.put("!", Type.NOT);
        word2type.put("!=", Type.NEQ);
        word2type.put("<", Type.LSS);
        word2type.put(">", Type.GRE);
        word2type.put(">=", Type.GEQ);
        word2type.put("<=", Type.LEQ);
        word2type.put("==", Type.EQL);
        word2type.put("=", Type.ASSIGN);
        word2type.put("&&", Type.AND);
        word2type.put("||", Type.OR);
        word2type.put("+", Type.PLUS);
        word2type.put("-", Type.MINU);
        word2type.put("*", Type.MULT);
        word2type.put("/", Type.DIV);
        word2type.put("%", Type.MOD);
        word2type.put(";", Type.SEMICN);
        word2type.put(",", Type.COMMA);
        word2type.put("(", Type.LPARENT);
        word2type.put(")", Type.RPARENT);
        word2type.put("[", Type.LBRACK);
        word2type.put("]", Type.RBRACK);
        word2type.put("{", Type.LBRACE);
        word2type.put("}", Type.RBRACE);
        word2type.put("bitand", Type.BITAND);
    }

    public char getChar() {
        return source.charAt(pos++);
    }

    public void ungetChar() {
        pos--;
    }

    public boolean isSeperater(char c) {
        return "!&|+-*/%<>=;,()[]{}".indexOf(c) != -1;
    }

    public boolean parseWord() {
        char c = getChar();
        StringBuilder sb = new StringBuilder();
        while (Character.isWhitespace(c)) {
            if (c == '\n') lineNumber++;
            c = getChar();
        }
        if (c == '_' || Character.isLetter(c)) {
            do {
                sb.append(c);
                c = getChar();
            } while (c == '_' || Character.isLetterOrDigit(c));
            ungetChar();
            word_list.add(new Word(sb.toString(), word2type.getOrDefault(sb.toString(), Type.IDENFR), lineNumber));
        } else if (Character.isDigit(c)) {
            do {
                sb.append(c);
                c = getChar();
            } while (Character.isDigit(c));
            ungetChar();
            word_list.add(new Word(sb.toString(), Type.INTCON, lineNumber));
        } else if (c == '"') {
            do {
                sb.append(c);
                c = getChar();
            } while (c != '"');
            sb.append(c);
            word_list.add(new Word(sb.toString(), Type.STRCON, lineNumber));
        } else if (isSeperater(c)) {
            if (c == '&') {
                c = getChar();
                word_list.add(new Word("&&", Type.AND, lineNumber));
            } else if (c == '|') {
                c = getChar();
                word_list.add(new Word("||", Type.OR, lineNumber));
            } else if (">=<!".indexOf(c) != -1) {
                sb.append(c);
                c = getChar();
                if (c == '=') {
                    sb.append(c);
                    word_list.add(new Word(sb.toString(), word2type.get(sb.toString()), lineNumber));
                } else {
                    ungetChar();
                    word_list.add(new Word(sb.toString(), word2type.get(sb.toString()), lineNumber));
                }
            } else if (c == '*') {
                word_list.add(new Word("*", Type.MULT, lineNumber));
            } else if (c == '/') {
                c = getChar();
                if (c == '/') {
                    while (getChar() != '\n');
                    ungetChar();
                } else if (c == '*') {
                    do {
                        do {
                            c = getChar();
                            if (c == '\n') lineNumber++;
                        } while (c != '*');
                        do {
                            c = getChar();
                            if (c == '\n') lineNumber++;
                            else if (c == '/') return true;
                        } while (c == '*');
                    } while (true);
                } else {
                    ungetChar();
                    word_list.add(new Word("/", Type.DIV, lineNumber));
                }
            } else {
                sb.append(c);
                word_list.add(new Word(sb.toString(), word2type.get(sb.toString()), lineNumber));
            }
        } else return c != '`';
        return true;
    }

    public static String getNextSym() {
        String preSym = sym.getStr();
        if (index < word_list.size() - 1) sym = word_list.get(++index);
        return preSym;
    }

    public static boolean symValIs(String val) {
        return sym.getVal().equals(val);
    }

    public static boolean wordTypeIs(int off, Type type) {
        if (index + off > word_list.size()) return false;
        return word_list.get(index + off).getType().equals(type);
    }

    public static Word getLastToken() {    //获取上一个 符
        return word_list.get(index - 1);
    }

    public static void dealGetInt(int line, int index, String name) {
        int pos = -1;
        for (int i = index; i < word_list.size(); i++) {
            if (word_list.get(i).getVal().equals(";")) {
                pos = i;
                break;
            }
        }
        ArrayList<Word> newList = new ArrayList<>();
        for (int i = 0; i <= pos; i++) {
            //System.out.println(word_list.get(i).getVal());
            newList.add(word_list.get(i));
        }
        newList.add(new Word(name, Type.IDENFR, line + 1));
        newList.add(new Word("=", Type.ASSIGN, line + 1));
        newList.add(new Word("getint", Type.GETINTTK, line + 1));
        newList.add(new Word("(", Type.LPARENT, line + 1));
        newList.add(new Word(")", Type.RPARENT, line + 1));
        newList.add(new Word(";", Type.SEMICN, line + 1));
        for (int i = pos + 1; i < word_list.size(); i++) {
            Word w = word_list.get(i);
            w.setLineNumber(w.getLineNumber() + 1);
            newList.add(w);
        }
        word_list = newList;
//        for (Word w:word_list){
//            System.out.println(w.getVal());
//        }
    }
    public static void dealipp(int line, int index, String name) {
        int pos = -1;
        for (int i = index; i < word_list.size(); i++) {
            if (word_list.get(i).getVal().equals(";")) {
                pos = i;
                break;
            }
        }
        ArrayList<Word> newList = new ArrayList<>();
        for (int i = 0; i <= pos; i++) {
            //System.out.println(word_list.get(i).getVal());
            newList.add(word_list.get(i));
        }
        newList.add(new Word(name, Type.IDENFR, line + 1));
        newList.add(new Word("=", Type.ASSIGN, line + 1));
        newList.add(new Word(name, Type.IDENFR, line + 1));
        newList.add(new Word("+", Type.PLUS, line + 1));
        newList.add(new Word("1", Type.INTCON, line + 1));
        newList.add(new Word(";", Type.SEMICN, line + 1));
        for (int i = pos + 1; i < word_list.size(); i++) {
            Word w = word_list.get(i);
            w.setLineNumber(w.getLineNumber() + 1);
            newList.add(w);
        }
        word_list = newList;
//        for (Word w:word_list){
//            System.out.println(w.getVal());
//        }
    }
}
