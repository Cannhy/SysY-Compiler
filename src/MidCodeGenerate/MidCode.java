package MidCodeGenerate;

import AST.LeafKind;
import Table.*;

import java.util.ArrayList;

public class MidCode {
    private MidType type;
    private String str;
    private String name;
    private String IRString;
    private String operator;
    private int num = 0;
    private Var var;
    private Var dst;
    private Var op1;
    private Var op2;
    private LeafKind kind;
    private int array1 = 0, array2 = 0;
    private String ins, jmpPos;
    private Symbol symbol;
    private boolean isInit = false;
    private boolean global;
    private boolean retVoid;
    private Table scope;
    private final ArrayList<Integer> initList = new ArrayList<>();

    public MidCode(MidType type, String IRString) {
        this.type = type;
        this.IRString = IRString;
    }

    public MidCode(MidType type, Var var) {
        this.type = type;
        this.var = var;
    }

    public MidCode(MidType type, LeafKind kind, String name) {
        this.type = type;
        this.kind = kind;
        this.name = name;
    }

    public MidCode(MidType type, String name, int array1, int array2) {
        this.type = type;
        this.name = name;
        this.array1 = array1;
        this.array2 = array2;
    }

    public MidCode(MidType type, Var dst, Var op1) {
        this.type = type;
        this.dst = dst;
        this.op1 = op1;
    }

    public MidCode(MidType type, String operator, Var dest, Var op1, Var op2) {
        this.type = type;
        this.operator = operator;
        this.dst = dest;
        this.op1 = op1;
        this.op2 = op2;
    }

    public MidCode(MidType type, String instr, String jumploc, Var op1, Var op2) {
        this.type = type;
        this.ins = instr;
        this.jmpPos = jumploc;
        this.op1 = op1;
        this.op2 = op2;
    }

    public void initStr() {
        StringBuilder sb = new StringBuilder();
        if (type.equals(MidType.Note) || type.equals(MidType.funcPara)) sb.append(IRString);
        else if (type.equals(MidType.Label)) sb.append(IRString).append(":");
        else if (type.equals(MidType.call)) sb.append(type).append(" ").append(IRString);
        else if (type.equals(MidType.Return)) {
            sb.append("RETURN");
            if (!retVoid) sb.append(" ").append(var.toString());
        }
        else if (type.equals(MidType.Printf) || type.equals(MidType.Push) || type.equals(MidType.Getint))
            sb.append(type).append(" ").append(var.getType()).append(" ").append(var.toString());
        else if (type.equals(MidType.assign_ret)) sb.append(var.toString()).append(" = RETURN");
        else if (type.equals(MidType.assign)) sb.append(dst.toString()).append(" = ").append(op1.toString()).append(" ")
                .append(operator).append(" ").append(op2.toString());
        else if (type.equals(MidType.assign2)) sb.append(dst.toString()).append(" = ").append(op1.toString());
        else if (type.equals(MidType.intDecl)) {
            sb.append(kind).append(" ").append(name);
            if (isInit) sb.append(" = ").append(name);
        }
        else if (type.equals(MidType.funcDecl)) sb.append(kind).append(" ").append(name).append("()");
        else if (type.equals(MidType.arrayDecl)) {
            sb.append("array int ").append(name).append("[").append(array1).append("]");
            if (array2 > 0) sb.append("[").append(array2).append("]");
            if (isInit) {
                sb.append(" = {");
                for (int ini : initList) sb.append(ini).append(",");
                sb.deleteCharAt(sb.length() - 1);
                sb.append("}");
            }
        }
        else if (type.equals(MidType.jump)) sb.append("j ").append(IRString);
        else if (type.equals(MidType.branch)) sb.append(ins).append(" ").append(op1.toString()).append(", ")
                .append(op2.toString()).append(", ").append(jmpPos);
        else if (type.equals(MidType.setcmp)) sb.append(operator).append(" ").append(dst.toString()).append(", ")
                .append(op1.toString()).append(", ").append(op2.toString());
        str = sb.toString();
    }

    public void setGlobal(boolean globalBool) {
        this.global = globalBool;
    }

    public boolean isGlobal() {
        return global;
    }

    public void addInitList(ArrayList<Integer> lst) {
        initList.addAll(lst);
    }

    public int getArrSize() {
        return array2 == 0 ? array1 : array1 * array2;
    }

    public MidType getType() {
        return type;
    }

    public void setType(MidType type) {
        this.type = type;
    }

    public String getStr() {
        return str;
    }

    public String getName() {
        return name;
    }

    public String getIRString() {
        return IRString;
    }

    public String getOperator() {
        return operator;
    }

    public Var getVar() {
        return var;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Var getDst() {
        return dst;
    }

    public Var getOp1() {
        return op1;
    }

    public Var getOp2() {
        return op2;
    }

    public String getIns() {
        return ins;
    }

    public String getJmpPos() {
        return jmpPos;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit() {
        isInit = true;
    }

    public ArrayList<Integer> getInitList() {
        return initList;
    }

    public boolean isVoidReturn() {
        return retVoid;
    }

    public void setVoidReturn(boolean voidreturn) {
        this.retVoid = voidreturn;
    }

    public Table getScope() {
        return scope;
    }

    public void setScope(Table scope) {
        this.scope = scope;
    }
}
