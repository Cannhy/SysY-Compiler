package AST;

import MidCodeGenerate.MidGenerate;
import MidCodeGenerate.Operate;
import Table.Symbol;

import java.util.ArrayList;
import java.util.Objects;

public class ASTLeaf {
    private LeafType type;
    private LeafKind kind;
    private String name;
    private String opName = "?";
    private int num;
    private ASTLeaf l = null;
    private ASTLeaf m = null;
    private ASTLeaf r = null;
    private ASTLeaf f = null;
    private ArrayList<ASTLeaf> leafs = new ArrayList<>();

    public ASTLeaf(LeafType leafType) {
        this.type = leafType;
    }

    public ASTLeaf(LeafType type, String name) {
        this.type = type;
        this.name = name;
    }

    public ASTLeaf(LeafType type, int num) {
        this.type = type;
        this.num = num;
    }

    public ASTLeaf(LeafType type, ASTLeaf l) {
        this.type = type;
        this.l = l;
    }

    public ASTLeaf(LeafType type, ASTLeaf l, ASTLeaf r) {
        this.type = type;
        this.l = l;
        this.r = r;
    }

    public ASTLeaf(LeafType type, ASTLeaf l, ASTLeaf m, ASTLeaf r) {
        this.type = type;
        this.l = l;
        this.m = m;
        this.r = r;
    }

    public ASTLeaf(LeafType type, ASTLeaf l, ASTLeaf m, ASTLeaf r, ASTLeaf f) {
        this.type = type;
        this.l = l;
        this.m = m;
        this.r = r;
        this.f = f;
    }

    public void addLeaf(ASTLeaf astLeaf) {
        this.leafs.add(astLeaf);
    }

    public int cal() {
        if (Operate.hasOperator(opName))
            switch (opName) {
                case "+": return (r != null) ? l.cal() + r.cal() : l.cal();
                case "-": return (r != null) ? l.cal() - r.cal() : -l.cal();
                case "*": return l.cal() * r.cal();
                case "/": return l.cal() / r.cal();
                case "%": return l.cal() % r.cal();
                default: return 0;
            }
        else if (type.equals(LeafType.Number)) return num;
        else if (type.equals(LeafType.Ident)) {
            if (kind.equals(LeafKind.INT) || kind.equals(LeafKind.CONSTINT))
                return Objects.requireNonNull(MidGenerate.AllTable2Find(name)).getNum();
            else if (kind.equals(LeafKind.ARRAY) || kind.equals(LeafKind.CONSTARRAY)) {
                Symbol symbol = MidGenerate.AllTable2Find(name);
                if (symbol != null)
                    if (r == null) return symbol.getArrayValue().get(l.cal());
                    else return symbol.getArrayValue().get(l.cal() * symbol.getDimen2() + r.cal());
            }
        }
        return 114514;
    }

    public LeafType getType() {
        return type;
    }

    public void setType(LeafType type) {
        this.type = type;
    }

    public LeafKind getKind() {
        return kind;
    }

    public void setKind(LeafKind kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public ASTLeaf getL() {
        return l;
    }

    public void setL(ASTLeaf l) {
        this.l = l;
    }

    public ASTLeaf getM() {
        return m;
    }

    public void setM(ASTLeaf m) {
        this.m = m;
    }

    public ASTLeaf getR() {
        return r;
    }

    public void setR(ASTLeaf r) {
        this.r = r;
    }

    public ArrayList<ASTLeaf> getLeafs() {
        return leafs;
    }

    public void setLeafs(ArrayList<ASTLeaf> leafs) {
        this.leafs = leafs;
    }

    public ASTLeaf getF() {
        return f;
    }
}
