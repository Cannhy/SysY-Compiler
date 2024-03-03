package MidCodeGenerate;

import java.util.HashSet;

public class Operate {
    public static HashSet<String> operator;
    public static HashSet<String> opCmp;

    static {
        operator = new HashSet<>();
        operator.add("+");
        operator.add("-");
        operator.add("*");
        operator.add("/");
        operator.add("%");
        operator.add("bitand");
    }

    static {
        opCmp = new HashSet<>();
        opCmp.add(">");
        opCmp.add("<");
        opCmp.add(">=");
        opCmp.add("<=");
        opCmp.add("==");
        opCmp.add("!=");
        opCmp.add("&&");
        opCmp.add("||");
    }

    public static boolean hasOperator(String s){
        return operator.contains(s);
    }

    public static boolean hasCmp(String s){
        return opCmp.contains(s);
    }

    public static boolean isNumCmp(String s){
        return s.equals(">") || s.equals("<") || s.equals(">=") || s.equals("<=");
    }

    public static boolean isEqCmp(String s){
        return s.equals("==") || s.equals("!=");
    }
}
