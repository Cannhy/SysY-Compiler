package Error;

import Lexer.Word;

public class Error implements Comparable<Error>{
    private Word word;
    private String errorType;

    public Error(Word word, String errorType) {
        this.word = word;
        this.errorType = errorType;
    }

    public Word getWord() {
        return this.word;
    }


    public String toString(){
        return  word.getLineNumber() + " " + this.errorType;
    }

    @Override
    public int compareTo(Error o) {
        return this.word.getLineNumber() - o.getWord().getLineNumber();
    }
}
