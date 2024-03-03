package Lexer;

public class Word {
    private String val;
    private Type type;
    private int lineNumber;

    public Word(String str,Type tp, int line) {
        this.val = str;
        this.type = tp;
        this.lineNumber = line;
    }

    public String getVal() {
        return this.val;
    }

    public Type getType() {
        return this.type;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public String getStr() {
        return type.toString() + " " + val;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
