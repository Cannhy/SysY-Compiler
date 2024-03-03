package MidCodeGenerate;

import MipsGenerate.RegisterEnum;
import Table.Symbol;
import Table.Table;

public class Var {
    private String type;
    private String name;
    private int num;
    private int off = 0;
    private int spOffset = 0;
    private boolean outFlag = false;
    private boolean kindFlag;
    private boolean hasBeenKicked = false;
    private Var var = null;
    private Symbol symbol = null;
    public Table table;
    private RegisterEnum curReg = RegisterEnum.wrong;

    public Var(String type, String name) {
        this.type = type;
        this.name = name;
        this.kindFlag = false;
    }

    public Var(String type, int num) {
        this.type = type;
        this.num = num;
        this.kindFlag = false;
    }

    public Var(String type, String name, Var var) {
        this.type = type;
        this.name = name;
        this.kindFlag = false;
        this.var = var;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public Table getTable() {
        return table;
    }

    public int getNum() {
        return num;
    }

    public void setKindTrue() {
        kindFlag = true; ////是临时自定义变量(如ti) 或者局部、全局变量
    }

    public void setVar(Var var) {
        this.var = var;
    }

    public String getName() {
        return name;
    }

    public RegisterEnum getCurReg() {
        return curReg;
    }

    public void setCurReg(RegisterEnum curReg) {
        this.curReg = curReg;
    }

    public boolean isHasBeenKicked() {
        return hasBeenKicked;
    }

    public void setHasBeenKicked(boolean hasBeenKicked) {
        this.hasBeenKicked = hasBeenKicked;
    }

    public boolean isKindFlag() {
        return kindFlag;
    }

    public int getSpOffset() {
        return spOffset;
    }

    public void setSpOffset(int spOffset) {
        this.spOffset = spOffset;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public String toString() {
        if (type.equals("var") || type.equals("str") || type.equals("null")) return name;
        if (type.equals("array")) {
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            if (var != null) sb.append("[").append(var).append("]");
            return sb.toString();
        }
        return String.valueOf(num);
    }
}
