package Table;

import Lexer.Word;

import java.util.ArrayList;

public class Symbol {
    private String name;
    private Word word;

    private TableType tableType;
    private ConstType constType = null;

    // 数组
    private int dimension = 0;
    private int dimen1 = 0;
    private int dimen2 = 0;
    private int spOff = 0;
    private boolean global;
    private final ArrayList<Integer> arrayVal = new ArrayList<>();

    public Symbol(String name, TableType tableType) {
        this.name = name;
        this.tableType = tableType;
    }

    public Symbol(String name, Word word, TableType tableType, ConstType constType) {
        this.name = name;
        this.word = word;
        this.tableType = tableType;
        this.constType = constType;
    }


    public String getName() {
        return this.name;
    }

    public Word getWord() {
        return this.word;
    }

    public int getDimension() {
        return this.dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    // 函数
    private ArrayList<Symbol> params = new ArrayList<>();
    private FuncKind funcKind = null;
    private int num;

    public void setFuncKind(FuncKind funcKind) {
        this.funcKind = funcKind;
    }

    public FuncKind getFuncKind() {
        return funcKind;
    }

    public void setDimension(int dimension,int d1,int d2) {
        this.dimension = dimension;
        this.dimen1 = d1;
        this.dimen2 = d2;
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }

    public void setParams(ArrayList<Symbol> params) {
        this.params = params;
    }

    public void addParam(Symbol param){
        params.add(param);
    }

    public int getParamsLen(){
        return params.size();
    }

    public ArrayList<Integer> getRightDimensions() {
        ArrayList<Integer> ans = new ArrayList<>();
        for (Symbol param : params) {
            ans.add(param.getDimension());
        }
        return ans;
    }

    public ArrayList<Integer> haveThePara(String name) {
        ArrayList<Integer> ans = new ArrayList<>();
        for (int i = 0; i < params.size(); i++)
            if (params.get(i).getName().equals(name)) {
                ans.add(1); ans.add(i + 1);
                return ans;
            }
        ans.add(0);
        return ans;
    }

    public int getSpOff() {
        return spOff;
    }

    public void setSpOff(int spOff) {
        this.spOff = spOff;
    }
// 值

    public ConstType getConstType() {
        return constType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public ArrayList<Integer> getArrayValue() { //数组的初始值
        return arrayVal;
    }

    public void addArrayNum(int a){
        arrayVal.add(a);
    }

    public int getDimen2() {
        return dimen2;
    }

    public void setConstType(ConstType constType) {
        this.constType = constType;
    }

    public void setDimen1(int dimen1) {
        this.dimen1 = dimen1;
    }

    public void setDimen2(int dimen2) {
        this.dimen2 = dimen2;
    }

    public TableType getTableType() {
        return tableType;
    }
}
