package MidCodeGenerate;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Table.BlockType;
import Table.Symbol;
import Table.*;

import java.util.ArrayList;

public class MidGenerate {
    private final ASTLeaf AST;
    private boolean global = true;
    private final ArrayList<MidCode> midCodes = new ArrayList<>();
    private Symbol nowFunc;
    private boolean noBlock = false;
    private int ifCount = 0;
    private int forCount = 1;
    private int orCount = 0;
    private int varCount = 0;
    public static Table headTable = new Table(null, BlockType.GLOBAL);
    public static Table fgTable = headTable;

    public MidGenerate(ASTLeaf root) {
        AST = root;
    }

    public ArrayList<MidCode> generate() {
        parseAST();
        return midCodes;
    }

    public void parseAST() {
        creatMidCode(MidType.Note, "#Start Decl");
        if (AST.getL() != null) for (ASTLeaf leaf : AST.getL().getLeafs()) parseDecl(leaf);

        global = false;
        creatMidCode(MidType.Note, "#Start FuncDecl");
        if (AST.getM() != null) for (ASTLeaf leaf : AST.getM().getLeafs()) parseFuncDecl(leaf);

        creatMidCode(MidType.Note, "#Start MainFunc");
        openTable(BlockType.MAIN);
        parseBlock(AST.getR(), 114514);
        closeTable();
    }

    public void parseDecl(ASTLeaf leaf) {
        for (ASTLeaf astLeaf : leaf.getLeafs()) parseDef(astLeaf);
    }

    public void parseDef(ASTLeaf leaf) {
        ASTLeaf ident = leaf.getL();
        ASTLeaf init = leaf.getR();
        String name = ident.getName();
        LeafKind kind = ident.getKind();
        Symbol symbol = insert2SymTable(ident, init, false, false);
        if (kind.equals(LeafKind.ARRAY) || kind.equals(LeafKind.CONSTARRAY)) parseArrayDef(leaf);
        else {
            if (init != null) {
                if (kind.equals(LeafKind.CONSTINT) || global) {
                    MidCode ir = new MidCode(MidType.intDecl, kind, name);
                    ir.setNum(symbol.getNum());
                    ir.setSymbol(symbol);
                    ir.setInit();
                    iR4init(ir);
                } else {
                    MidCode numIr = new MidCode(MidType.intDecl, kind, name);
                    numIr.setSymbol(symbol);
                    iR4init(numIr);
                    Var initVar = parseExp(init);
                    Var initLVal = new Var("var", name);
                    initLVal.setSymbol(symbol);
                    initLVal.setKindTrue();
                    iR4init(new MidCode(MidType.assign2, initLVal, initVar));
                }
            } else {
                MidCode ir = new MidCode(MidType.intDecl, kind, name);
                ir.setSymbol(symbol);
                iR4init(ir);
            }
        }
    }

    public void parseArrayDef(ASTLeaf leaf) {
        ASTLeaf ident = leaf.getL();
        ASTLeaf init = leaf.getR();
        String name = ident.getName();
        Symbol symbol = AllTable2Find(name);
        ASTLeaf d1 = ident.getL();
        ASTLeaf d2 = ident.getR();
        int num1 = d1.cal();
        int num2 = (d2 == null) ? 0 : d2.cal();
        MidCode ir = new MidCode(MidType.arrayDecl, name, num1, num2);
        ir.setSymbol(symbol);
        if (init == null) {
            iR4init(ir);
            return;
        }
        ir.setInit();
        assert symbol != null;
        if (symbol.getConstType().equals(ConstType.CONST) || symbol.isGlobal()) {
            parseArrayInitNum(ir, symbol, name, init, num1, num2, true);
            iR4init(ir);
        } else {
            iR4init(ir);
            parseArrayInitNum(ir, symbol, name, init, num1, num2, false);
        }
    }

    public void parseArrayInitNum(MidCode midCode, Symbol symbol, String name,
                                  ASTLeaf init, int num1, int num2, boolean gOc) {
        ArrayList<Integer> nums = new ArrayList<>();
        if (gOc) {
            if (num2 == 0) {
                for (int i = 0; i < num1; i++) nums.add(init.getLeafs().get(i).cal());
                midCode.addInitList(nums);
            } else {
                for (int i = 0; i < num1; i++)
                    for (int j = 0; j < num2; j++) nums.add(init.getLeafs().get(i).getLeafs().get(j).cal());
                midCode.addInitList(nums);
            }
        } else {
            int len = (num2 == 0) ? num1 : num1 * num2;
            for (int i = 0; i < len; i++) {
                Var index = new Var("num", i);
                Var arrNum = (num2 == 0) ? parseExp(init.getLeafs().get(i)) :
                        parseExp(init.getLeafs().get(i / num2).getLeafs().get(i - (i / num2) * num2));
                Var arrLVal = new Var("array", name, index);
                arrLVal.setKindTrue();
                arrLVal.setSymbol(symbol);
                creatMidCode(MidType.assign2, arrLVal, arrNum);
            }
        }
    }

    public void parseFuncDecl(ASTLeaf leaf) {
        ASTLeaf ident = leaf.getL();
        nowFunc = insert2SymTable(ident, null, true, false);
        openTable(BlockType.FUNC);
        creatMidCode(MidType.funcDecl, ident.getKind(), ident.getName());
        ASTLeaf params = ident.getL();
        if (params != null)
            for (ASTLeaf leaf1 : params.getLeafs()) parseFuncFParam(leaf1);
        parseBlock(leaf.getR(), 114514);
        creatMidCode(MidType.Note, "#end func");
        closeTable();
        nowFunc = null;
    }

    public void parseFuncFParam(ASTLeaf leaf) {
        Symbol symbol = insert2SymTable(leaf, null, false, true);
        nowFunc.addParam(symbol);
        StringBuilder sb = new StringBuilder();
        sb.append("para int ");
        if (leaf.getKind().equals(LeafKind.INT) || leaf.getKind().equals(LeafKind.CONSTINT)) sb.append(leaf.getName());
        else if (leaf.getKind().equals(LeafKind.ARRAY) || leaf.getKind().equals(LeafKind.CONSTARRAY))
            if (leaf.getNum() == 1) sb.append(leaf.getName()).append("[]");
            else if (leaf.getNum() == 2) sb.append(leaf.getName()).append("[][]");
        creatMidCode(MidType.funcPara, sb.toString());
    }

    public void parseBlock(ASTLeaf leaf, int n) {
        for (ASTLeaf leaf1 : leaf.getLeafs()) parseBlockItem(leaf1, n);
    }

    public void parseBlockItem(ASTLeaf leaf, int n) {
        if (leaf.getType().equals(LeafType.BlockItem_Decl)) parseDecl(leaf.getL());
        else if (leaf.getType().equals(LeafType.BlockItem_Stmt)) parseStmt(leaf.getL(), n);
    }

    public void parseStmt(ASTLeaf leaf, int n) {
        LeafType type = leaf.getType();
        if (type.equals(LeafType.IfStatement)) parseIfStatement(leaf, n);
        else if (type.equals(LeafType.For)) parseFor(leaf);
        else if (type.equals(LeafType.Return)) {
            MidCode ir;
            if (leaf.getL() != null) {
                ir = new MidCode(MidType.Return, parseExp(leaf.getL()));
                ir.setVoidReturn(false);
            }
            else {
                ir = new MidCode(MidType.Return, "void return");
                ir.setVoidReturn(true);
            }
            iR4init(ir);
        }
        else if (type.equals(LeafType.Continue)) {
            creatMidCode(MidType.Note, "#Out Block ForCut");
            creatMidCode(MidType.jump, "end_loop_add" + n);
        }
        else if (type.equals(LeafType.Break)) {
            creatMidCode(MidType.Note, "#Out Block ForCut");
            creatMidCode(MidType.jump, "end_loop" + n);
        }
        else if (type.equals(LeafType.Printf)) {
            parsePrintf(leaf);
        }
        else if (type.equals(LeafType.Block)) {
            boolean yesBlock = true;
            if (noBlock) yesBlock = noBlock = false;
            if (yesBlock) openTable(BlockType.BLOCK);
            parseBlock(leaf.getL(), n);
            if (yesBlock) {
                creatMidCode(MidType.Note, "#Out Block");
                closeTable();
            }
        }
        else if (type.equals(LeafType.Exp)) {
            if (leaf.getL() != null) parseExp(leaf.getL());
        }
        else if (type.equals(LeafType.Assign_getint)) {
            Var t = parseLVal(leaf.getL());
            assert t != null;
            creatMidCode(MidType.Getint, t);
        }
        else if (type.equals(LeafType.Assign_value)) {
            creatMidCode(MidType.assign2, parseLVal(leaf.getL()), parseExp(leaf.getR()));
        }
    }

    public void parseFor(ASTLeaf leaf) {
        int curForCnt = forCount;
        String beginLoop_label = "begin_loop" + forCount;
        String endLoop_label = "end_loop" + forCount;
        String endLoop_Add_label = "end_loop_add" + forCount;
        String intoLoop_label = "intostmt_loop" + forCount;
        parseForStmt(leaf.getL(), beginLoop_label);
        creatMidCode(MidType.Label, beginLoop_label);
        forCount++;
        parseCond(leaf.getM(), intoLoop_label, endLoop_label);
        openTable(BlockType.For);
        noBlock = true;
        creatMidCode(MidType.Label, intoLoop_label);
        parseStmt(leaf.getF(), curForCnt);
        creatMidCode(MidType.Note, "#Out Block");
        creatMidCode(MidType.Label, endLoop_Add_label);
        parseForStmt(leaf.getR(), beginLoop_label);
        closeTable();
        noBlock = false;
        creatMidCode(MidType.Label, endLoop_label);
    }

    public void parseForStmt(ASTLeaf leaf, String jmpLabel) {
        if (leaf != null) {
            Var lval = parseLVal(leaf.getL());
            Var exp = parseExp(leaf.getR());
            creatMidCode(MidType.assign2, lval, exp);
        }
        creatMidCode(MidType.jump, jmpLabel);
    }

    public void parseIfStatement(ASTLeaf leaf, int n) {
        ifCount++;
        String intoIf = "into_if" + ifCount;
        String endIf = "end_if" + ifCount;
        String endElse = "end_else" + ifCount;
        parseCond(leaf.getL(), intoIf, endIf);
        creatMidCode(MidType.Label, intoIf);
        openTable(BlockType.IF);
        noBlock = true;
        parseStmt(leaf.getM(), n);
        creatMidCode(MidType.Note, "#Out Block");
        if (!leaf.getType().equals(LeafType.IfStatement) || leaf.getR() == null) {
            closeTable();
            noBlock = false;
            creatMidCode(MidType.Label, endIf);
        } else {
            creatMidCode(MidType.jump, endElse);
            closeTable();
            noBlock = false;
            creatMidCode(MidType.Label, endIf);
            noBlock = true;
            openTable(BlockType.ELSE);
            parseStmt(leaf.getR(), n);
            creatMidCode(MidType.Note, "#Out Block");
            closeTable();
            creatMidCode(MidType.Label, endElse);
        }
    }

    public void parseCond(ASTLeaf leaf, String inLabel, String outLabel) {
        if (leaf == null) {
            creatMidCode(MidType.jump, inLabel);
            return;
        }
        String op = leaf.getOpName();
        if (Operate.isNumCmp(op)) {
            Var lExp = parseRelExp(leaf.getL());
            Var rExp = parseRelExp(leaf.getR());
            switch (op) {
                case ">=":
                    creatMidCode("blt", outLabel, lExp, rExp);
                    break;
                case "<=":
                    creatMidCode("bgt", outLabel, lExp, rExp);
                    break;
                case ">":
                    creatMidCode("ble", outLabel, lExp, rExp);
                    break;
                default:
                    creatMidCode("bge", outLabel, lExp, rExp);
                    break;
            }
        }
        else if (Operate.isEqCmp(op)) {
            Var lExp = parseEqExp(leaf.getL());
            Var rExp = parseEqExp(leaf.getR());
            if (op.equals("!=")) creatMidCode("beq", outLabel, lExp, rExp);
            else creatMidCode("bne", outLabel, lExp, rExp);
        }
        else if (leaf.getType().equals(LeafType.AND)) {
            parseCond(leaf.getL(), inLabel, outLabel);
            parseCond(leaf.getR(), inLabel, outLabel);
        }
        else if (leaf.getType().equals(LeafType.OR)) {
            String orLabel = "orLabel_" + orCount;
            orCount++;
            parseCond(leaf.getL(), inLabel, orLabel);
            creatMidCode(MidType.jump, inLabel);
            creatMidCode(MidType.Label, orLabel);
            parseCond(leaf.getR(), inLabel, outLabel);
        }
        else {
            if (op.equals("!")) creatMidCode("bne", outLabel, parseExp(leaf.getL()), new Var("num", 0));
            else creatMidCode("beq", outLabel, parseExp(leaf), new Var("num", 0));
        }
    }

    public Var parseEqExp(ASTLeaf leaf) {
        String op = leaf.getOpName();
        if (Operate.isEqCmp(op)) {
            Var left = parseEqExp(leaf.getL());
            Var right = parseEqExp(leaf.getR());
            Var tmp = getTmpVar();
            creatMidCode(MidType.setcmp, op.equals("==") ? "seq" : "sne", tmp, left, right);
            return tmp;
        }
        return parseRelExp(leaf);
    }

    public Var parseRelExp(ASTLeaf leaf) {
        String op = leaf.getOpName();
        if (Operate.isNumCmp(op)) {
            Var left = parseRelExp(leaf.getL());
            Var right = parseRelExp(leaf.getR());
            Var tmp = getTmpVar();
            if (op.equals(">=")) creatMidCode(MidType.setcmp, "sge", tmp, left, right);
            if (op.equals("<=")) creatMidCode(MidType.setcmp, "sle", tmp, left, right);
            if (op.equals(">")) creatMidCode(MidType.setcmp, "sgt", tmp, left, right);
            if (op.equals("<")) creatMidCode(MidType.setcmp, "slt", tmp, left, right);
            return tmp;
        }
        else return parseExp(leaf);
    }

    public void parsePrintf(ASTLeaf leaf) {
        String fs = leaf.getL().getName();
        fs = fs.substring(1, fs.length() - 1);
        creatMidCode(MidType.Note, "#Start Print");
        if (leaf.getR() != null) {
            String[] splits = fs.split("%d", -1);
            for (int i = 0; i < splits.length; i++) {
                if (!splits[i].equals("")) creatMidCode(MidType.Printf, new Var("str", splits[i]));
                if (leaf.getR().getLeafs() == null || i >= leaf.getR().getLeafs().size()) break;
                Var pExp = parseExp(leaf.getR().getLeafs().get(i));
                creatMidCode(MidType.Printf, pExp);
            }
        }
        else creatMidCode(MidType.Printf, new Var("str", fs));
        int start = -1;
        for (int i = midCodes.size() - 1; i > -1; i--)
            if (midCodes.get(i).getIRString() != null && midCodes.get(i).getIRString().equals("#Start Print")) {
                start = i;
                break;
            }
        if (start != -1) {
            int end = midCodes.size() - 1;
            ArrayList<MidCode> tmp = new ArrayList<>();
            for (int i = start; i <= end; i++) tmp.add(midCodes.get(i));
            if (end >= start) midCodes.subList(start, end + 1).clear();
            ArrayList<Integer> tmpId = new ArrayList<>();
            for (int i = 0; i < tmp.size(); i++) {
                MidCode midCode = tmp.get(i);
                if (midCode.getType().equals(MidType.Printf)) tmpId.add(i);
                else midCodes.add(midCode);
            }
            for (Integer integer : tmpId) midCodes.add(tmp.get(integer));
            creatMidCode(MidType.Note, "#End Print");
        }
    }

    public Var parseExp(ASTLeaf leaf) {
        String op = leaf.getOpName();
        LeafType type = leaf.getType();
        if (type.equals(LeafType.Ident)) {
            Var ident = parseIdent(leaf);
            if (!leaf.getKind().equals(LeafKind.FUNC)) {
                Symbol symbol = AllTable2Find(leaf.getName());
                ident.setSymbol(symbol);
            }
            return ident;
        }
        else if (type.equals(LeafType.Number)) return new Var("num", leaf.getNum());
        else if (Operate.hasOperator(op)) {
            if (op.equals("+")) return (leaf.getR() == null) ? parseExp(leaf.getL()) : generaOpeIr(leaf, "+");
            if (op.equals("*")) return generaOpeIr(leaf, "*");
            if (op.equals("/")) return generaOpeIr(leaf, "/");
            if (op.equals("%")) return generaOpeIr(leaf, "%");
            if (op.equals("-")) {
                if (leaf.getR() != null) return generaOpeIr(leaf, "-");
                Var left = new Var("num", 0);
                Var right = parseExp(leaf.getL());
                if (right.getType().equals("num")) return new Var("num", -right.getNum());
                Var tmp = getTmpVar();
                creatMidCode(MidType.assign, "-", tmp, left, right);
                return tmp;
            }
            if (op.equals("bitand")) return generaOpeIr(leaf, "bitand");
        }
        else if (op.equals("!")) {
            Var left = parseExp(leaf.getL());
            Var tmp = getTmpVar();
            creatMidCode(MidType.setcmp, "seq", tmp, left, new Var("num", 0));
            return tmp;
        }
        return null;
    }

    public Var parseLVal(ASTLeaf leaf) {
        LeafKind kind = leaf.getKind();
        if (kind.equals(LeafKind.INT) || kind.equals(LeafKind.CONSTINT)) {
            Var lVal = new Var("var", leaf.getName());
            Symbol symbol = AllTable2Find(leaf.getName());
            lVal.setSymbol(symbol);
            lVal.setKindTrue();
            return lVal;
        }
        else if (kind.equals(LeafKind.ARRAY) || kind.equals(LeafKind.CONSTARRAY)) return parseArrayVis(leaf);
        return null;
    }

    public Var parseIdent(ASTLeaf leaf) {
        LeafKind kind = leaf.getKind();
        if (kind.equals(LeafKind.FUNC)) {
            ASTLeaf paramsR = leaf.getL();
            Symbol func = fgTable.getSameSymbol(leaf.getName());
            if (paramsR != null) {
                for (int i = 0; i < paramsR.getLeafs().size(); i++) {
                    ASTLeaf pr = paramsR.getLeafs().get(i);
                    Symbol pf = func.getParams().get(i);
                    if (pf.getTableType().equals(TableType.ARRAY))
                        creatMidCode(MidType.Push, parseArrayParam(pr, pf.getDimension()));
                    else creatMidCode(MidType.Push, parseExp(pr));
                }
            }
            MidCode ir = new MidCode(MidType.call, leaf.getName());
            ir.setSymbol(func);
            iR4init(ir);
            if (func.getFuncKind() != null && !func.getFuncKind().equals(FuncKind.VOID)) {
                Var tmp = getTmpVar();
                creatMidCode(MidType.assign_ret, tmp);
                return tmp;
            }
            return null;
        }
        else if (kind.equals(LeafKind.INT) || kind.equals(LeafKind.CONSTINT)) {
            if (kind.equals(LeafKind.CONSTINT)) {  //此处为优化 常量直取
                Symbol symbol = AllTable2Find(leaf.getName());
                assert symbol != null;
                int num = symbol.getNum();
                return new Var("num", num);
            }
            Var intVar = new Var("var", leaf.getName());
            intVar.setKindTrue();//设置成是符号表里面的东西
            return intVar;
        }
        else if (kind.equals(LeafKind.ARRAY)) {
            Var arr = parseArrayVis(leaf), tmp = getTmpVar();
            creatMidCode(MidType.assign2, tmp, arr);
            return tmp;
        }
        else if (kind.equals(LeafKind.CONSTARRAY)) {
            Var arr = parseArrayVis(leaf), tmp = getTmpVar();
            creatMidCode(MidType.assign2, tmp, arr);
            return tmp;
        }
        return null;
    }

    public Var parseArrayVis(ASTLeaf leaf) {
        String identName = leaf.getName();
        Symbol arr = AllTable2Find(identName);
        assert arr != null;
        if (arr.getDimension() == 1) {
            Var index1 = parseExp(leaf.getL());
            if (arr.getConstType().equals(ConstType.CONST) && index1.getType().equals("num"))
                return new Var("num", arr.getArrayValue().get(index1.getNum()));
            else {
                Var var = new Var("array", leaf.getName(), index1);
                var.setSymbol(arr);
                return var;
            }
        }
        else {
            Var index1 = parseExp(leaf.getL()), index2 = parseExp(leaf.getR());
            if (index1.getType().equals("num")) {
                if (index2.getType().equals("num")) {
                    int off = index1.getNum() * arr.getDimen2() + index2.getNum();
                    if (arr.getConstType().equals(ConstType.VAR)) {
                        Var retArr = new Var("array", leaf.getName(), new Var("num", off));
                        retArr.setSymbol(arr);
                        return retArr;
                    }
                    else return new Var("num", arr.getArrayValue().get(off));
                }
                else {
                    Var base = new Var("num", index1.getNum() * arr.getDimen2());
                    Var offset = getTmpVar();
                    creatMidCode(MidType.assign, "+", offset, index2, base);
                    Var retArr = new Var("array", leaf.getName(), offset);
                    retArr.setSymbol(arr);
                    return retArr;
                }
            }
            else {
                Var tmp1 = getTmpVar();
                creatMidCode(MidType.assign, "*", tmp1, index1, new Var("num", arr.getDimen2()));
                Var tmp2 = getTmpVar();
                creatMidCode(MidType.assign, "+", tmp2, index2, tmp1);
                Var retArr = new Var("array", leaf.getName(), tmp2);
                retArr.setSymbol(arr);
                return retArr;
            }
        }
    }

    public Var parseArrayParam(ASTLeaf leaf, int dimen) {
        Var arr = new Var("array", leaf.getName());
        Symbol arrSym = AllTable2Find(leaf.getName());
        assert arrSym != null;
        if (arrSym.getDimension() == 2 && dimen == 1) arr.setVar(parseExp(leaf.getL()));
        arr.setSymbol(arrSym);
        arr.setKindTrue();
        return arr;
    }

    public void creatMidCode(MidType type, String IRString) {
        iR4init(new MidCode(type, IRString));
    }

    public void creatMidCode(MidType type, Var var) {
        iR4init(new MidCode(type, var));
    }

    public void creatMidCode(MidType type, LeafKind kind, String name) {
        if (type.equals(MidType.funcDecl)) iR4init(new MidCode(type, kind, name));
    }

    private void creatMidCode(MidType type, Var dest, Var op1) {
        if (type.equals(MidType.assign2)) {
            iR4init(new MidCode(type, dest, op1));
        }
    }

    private void creatMidCode(MidType type, String operator, Var dest, Var op1, Var op2) {
        if (type.equals(MidType.assign) || type.equals(MidType.setcmp))
            iR4init(new MidCode(type, operator, dest, op1, op2));
    }

    private void creatMidCode(String instr, String jumpto, Var op1, Var op2) {
        MidCode ir = new MidCode(MidType.branch, instr, jumpto, op1, op2);
        iR4init(ir);
    }

    public void iR4init(MidCode ir) {
        ir.setGlobal(global);
        ir.setScope(headTable);
        ir.initStr();
        midCodes.add(ir);
    }

    public static void openTable(BlockType blockType) {
        Table table = new Table(headTable, blockType);
        headTable.addSon(headTable);
        headTable = table;
    }

    public static void insertTable(Symbol symbol) {
        headTable.addSymbol(symbol);
//        symbol.setTable(headTable);
    }

    public static void closeTable() {
        headTable = headTable.getFatherTable();
    }

    public Symbol insert2SymTable(ASTLeaf ident, ASTLeaf init, Boolean isFunc, Boolean isPara) {
        LeafKind kind = ident.getKind();
        Symbol symbol = null;
        if (isFunc) {
            symbol = new Symbol(ident.getName(), TableType.FUNC);
            FuncKind funcKind = ident.getKind().equals(LeafKind.VOID) ? FuncKind.VOID : FuncKind.INT;
            symbol.setFuncKind(funcKind);
        }
        else if (kind.equals(LeafKind.INT) || kind.equals(LeafKind.CONSTINT)) {
            symbol = new Symbol(ident.getName(), TableType.INTEGER);
            if (ident.getKind().equals(LeafKind.CONSTINT)) symbol.setConstType(ConstType.CONST);
            else if (ident.getKind().equals(LeafKind.INT)) symbol.setConstType(ConstType.VAR);
            if (init != null && (ident.getKind().equals(LeafKind.CONSTINT) || global)) symbol.setNum(init.cal());
        }
        else if (kind.equals(LeafKind.ARRAY) || kind.equals(LeafKind.CONSTARRAY)) {
            symbol = new Symbol(ident.getName(), TableType.ARRAY);
            if (ident.getKind().equals(LeafKind.CONSTARRAY)) symbol.setConstType(ConstType.CONST);
            else symbol.setConstType(ConstType.VAR);
            symbol.setDimension(1);
            ASTLeaf dm1 = ident.getL();
            int num1 = 0;
            if (!isPara) {
                num1 = dm1.cal();
                symbol.setDimen1(num1);
            }
            int num2 = 0;
            if (ident.getR() != null) {
                symbol.setDimension(2);
                num2 = ident.getR().cal();
                symbol.setDimen2(num2);
            }
            if (init != null && (kind.equals(LeafKind.CONSTARRAY) || global)) {
                if (num2 == 0)
                    for (int i = 0; i < num1; i++) symbol.addArrayNum(init.getLeafs().get(i).cal());
                else {
                    for (int i = 0; i < num1; i++)
                        for (int j = 0; j < num2; j++)
                            symbol.addArrayNum(init.getLeafs().get(i).getLeafs().get(j).cal());
                }
            }
        }
        assert symbol != null;
        symbol.setGlobal(global);
        insertTable(symbol);
        return symbol;
    }

    public static Symbol GlobalTable2Find(String name) {
        return fgTable.getSameSymbol(name);
    }

    public static Symbol AllTable2Find(String name) {
        Table tmp = headTable;
        Symbol sameName = tmp.getSameSymbol(name);
        if (sameName == null) {
            while (tmp.getFatherTable() != null) {
                tmp = tmp.getFatherTable();
                sameName = tmp.getSameSymbol(name);
                if (sameName != null) {
                    return sameName;
                }
            }
            return null;
        }
        return sameName;
    }

    public Var getTmpVar() {
        varCount++;
        Var var = new Var("var", "#tmp" + varCount);
        var.table = headTable;
        return var;
    }

    public Var generaOpeIr(ASTLeaf leaf, String op) {
        Var left = parseExp(leaf.getL());
        Var right = parseExp(leaf.getR());
        if (left.getType().equals("num") && right.getType().equals("num") && !op.equals("bitand")) {
            int numl = left.getNum(), numr = right.getNum();
            switch (op) {
                case "+": return new Var("num", numl + numr);
                case "-": return new Var("num", numl - numr);
                case "*": return new Var("num", numl * numr);
                case "/": return new Var("num", numl / numr);
                case "%": return new Var("num", numl % numr);
            }
        }
        Var tmp = getTmpVar();
        creatMidCode(MidType.assign, op, tmp, left, right);
        return tmp;
    }
}
