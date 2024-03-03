package MipsGenerate;

import MidCodeGenerate.MidCode;
import MidCodeGenerate.MidType;
import MidCodeGenerate.*;
import MidCodeGenerate.NoteType;
import Table.Symbol;
import Table.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MipsGenerate {
    public static boolean inPush = false;
    private boolean inMain = false;
    private boolean inFuncDel = false;
    private int inFuncOff = 0;
    private int busyOff = 0;
    private int spOff = 0;
    private int pushOff = 0;
    public boolean optimize = true;
    private Symbol curFunc = null;
    private final ArrayList<MidCode> midCodes;
    private final ArrayList<MidCode> pushStore = new ArrayList<>();
    private final ArrayList<Mips> mips = new ArrayList<>();
    private final MipsData data = new MipsData();

    public MipsGenerate(ArrayList<MidCode> m) {
        this.midCodes = m;
    }

    public ArrayList<Mips> generate() {
        mips.add(data);
        makePrintfLabel();
        for (MidCode m : midCodes) {
            if (inMain) makeMipsStr(m);
            else if (inFuncDel) {
                if (m.getType().equals(MidType.Note) && m.getIRString().equals(Note.getVal(NoteType.StartMainFunc))) {
                    inMain = true; inFuncDel = false;
                    makeInstr("main:", false);
                }
                else if (m.getType().equals(MidType.funcDecl)) {
                    inFuncOff = 0;
                    curFunc = MidGenerate.GlobalTable2Find(m.getName());
                    makeInstr("Function_" + m.getName() + ":", false);
                }
                else if (m.getType().equals(MidType.Note) && m.getIRString().equals(Note.getVal(NoteType.endFunc))) {
                    RegisterManage.busyRegs.clear();
                    RegisterManage.tmpRegs.clear();
                    RegisterManage.resetRegs();
                    if (inFuncOff != 0) makeAddI(RegisterEnum.sp, RegisterEnum.sp, -inFuncOff);
                    makeInstr("jr $ra", true);
                }
                else makeMipsStr(m);
            }
            else {
                makeMipsStr(m);
                if (m.getType().equals(MidType.Note) && m.getIRString().equals(Note.getVal(NoteType.StartFuncDel))) {
                    inFuncDel = true;
                    makeInstr(".text", false);
                    makeInstr("j main", true);
                }
            }
        }
        return mips;
    }

    public void makePrintfLabel() {
        int cnt1 = 0, cnt2 = 0;
        for (MidCode m : midCodes) {
            if (m.getType().equals(MidType.Note) && m.getIRString().equals(Note.getVal(NoteType.StartPrint))) {
                cnt1++; cnt2++;
            }
            if (m.getType().equals(MidType.Printf) && m.getVar().getType().equals("str")) {
                data.addPSM(m, "print" + cnt1 + "_str" + cnt2);
                cnt2++;
            }
        }
    }

    public void makeMipsStr(MidCode midCode) {
        switch (midCode.getType()) {
            case Note:
                makeNote(midCode);
                break;
            case Label:
                makeInstr(midCode.getStr(), false);
                break;
            case Getint:
                makeGetint(midCode);
                break;
            case Printf:
                makePrintf(midCode);
                break;
            case Push:
                makeStorePush(midCode);
                break;
            case call:
                makeCall(midCode);
                break;
            case assign:
                makeAssign(midCode);
                break;
            case assign2:
                makeAssign2(midCode);
                break;
            case assign_ret:
                makeAssignRet(midCode);
                break;
            case intDecl:
                makeIntDecl(midCode);
                break;
            case Return:
                makeReturn(midCode);
                break;
            case jump:
                makeInstr(midCode.getStr(), true);
                break;
            case arrayDecl:
                makeArrayDecl(midCode);
                break;
            case branch:
                makeBranch(midCode);
                break;
            case setcmp:
                makeSetCmp(midCode);
                break;
            case funcPara:
            case funcDecl:
                break;
        }
    }

    public void makeNote(MidCode m) {
        if (m.getStr().equals(Note.getVal(NoteType.OutBlock))) {
            int off = m.getScope().getInBlockOff();
            if (off != 0) {
                makeAddI(RegisterEnum.sp, RegisterEnum.sp, -off);
                if (inFuncDel) inFuncOff -= off;
                else spOff -= off;
            }
        }
        else if (m.getStr().equals(Note.getVal(NoteType.OutBlockForCut))) {
             Table table = m.getScope();
             int offSum = 0;
             while (!table.getBlockType().equals(BlockType.For)) {
                 offSum += table.getInBlockOff();
                 table = table.getFatherTable();
             }
             offSum += table.getInBlockOff();
             if (offSum != 0) makeAddI(RegisterEnum.sp, RegisterEnum.sp, -offSum);
        }
        else makeInstr(m.getStr(), true);
    }

    public void makePrintf(MidCode m) {
        Var var = m.getVar();
        switch (var.getType()) {
            case "var":
                makeInstr("li $v0, 1", true);
                if (var.isKindFlag()) {
                    Symbol symbol = var.getSymbol();
                    if (symbol.getConstType().equals(ConstType.CONST)) makeInstr("li $a0, " + symbol.getNum(), true);
                    else if (symbol.isGlobal()) lwSymbolFromGlobalLabel(var, RegisterEnum.a0);
                    else lwSymbolFromStack(var, RegisterEnum.a0, false, false);
                } else {
                    RegisterEnum r = checkIfVariableHadReg_IFNotApply(var);
                    makeInstr("move $a0, $" + r, true);
                    RegisterManage.freeReg(r);
                }
                makeInstr("syscall", true);
                break;
            case "str":
                makeInstr("li $v0, 4", true);
                makeInstr("la $a0, " + data.getString(m), true);
                makeInstr("syscall", true);
                break;
            case "num":
                makeInstr("li $v0, 1", true);
                makeInstr("li $a0, " + var.getNum(), true);
                makeInstr("syscall", true);
                break;
            default:
                makeInstr("li $v0, 1", true);
                LwOrSW_between_Reg_Array("lw", RegisterEnum.a0, var);
                makeInstr("syscall", true);
                break;
        }
    }

    public void makeGetint(MidCode m) {
        Var var = m.getVar();
        makeInstr("li $v0, 5", true);
        makeInstr("syscall", true);
        if (var.getType().equals("var")) saveReg2Var(var, RegisterEnum.v0);
        else if (var.getType().equals("array")) LwOrSW_between_Reg_Array("sw", RegisterEnum.v0, var);
    }

    public RegisterEnum makePush(MidCode m) {
        pushOff -= 4;
        Var pushVar = m.getVar();
        if (pushVar.getType().equals("array")) {
            RegisterEnum pushArrReg = getReg_KickOutTmp(pushVar, false);
            writeArrayAddrToReg(pushVar, pushArrReg);
            makeMemory("sw", pushArrReg, pushOff + "($sp)");
            RegisterManage.freeReg(pushArrReg);
        }
        else {
            RegisterEnum pushArrReg = getVarReg(pushVar, false);
            makeMemory("sw", pushArrReg, pushOff + "($sp)");
            if (pushVar.getType().equals("num") ||
                    (pushVar.getType().equals("var") && pushVar.isKindFlag())) RegisterManage.freeReg(pushArrReg);
            else {
                if (pushVar.isHasBeenKicked()) RegisterManage.freeReg(pushArrReg);
                else return pushArrReg;
            }
        }
        return RegisterEnum.none;
    }

    public void writeArrayAddrToReg(Var array, RegisterEnum toReg) {
        String arrName = array.getName(); Symbol arrSymbol = array.getSymbol();
        if (arrSymbol.isGlobal()) {
            if (array.getVar() == null) makeMemory("la", toReg, "Global_" + arrName);
            else {
                Var indexVar = array.getVar();
                if (indexVar.getType().equals("num")) {
                    int arrOff = indexVar.getNum() * arrSymbol.getDimen2() * 4;
                    makeMemory("la", toReg, "Global_" + arrName);
                    makeAddI(toReg, toReg, arrOff);
                }
                else if (indexVar.getType().equals("var")) {
                    RegisterEnum varIndReg = getVarReg(indexVar, false);
                    makeSll(varIndReg, varIndReg, 2);
                    makeInstr("li $" + toReg + ", " + arrSymbol.getDimen2(), true);
                    makeInstr("mult", varIndReg, toReg);
                    makeInstr("mflo", toReg);
                    makeMemory("la", toReg, "Global_" + arrName + "($" + toReg + ")");
                    RegisterManage.freeReg(varIndReg);
                }
            }
        }
        else if (inFuncDel) {
            ArrayList<Integer> res = curFunc.haveThePara(arrName);
            String arrOff = getSymbolAddressAtStack(arrSymbol, 0);
            if (res.get(0) == 1) {
                if (array.getVar() == null) makeMemory("lw", toReg, arrOff);
                else {
                    Var indexVar = array.getVar();
                    if (indexVar.getType().equals("num")) {
                        makeMemory("lw", toReg, arrOff);
                        makeAddI(toReg, toReg, indexVar.getNum());
                    }
                    else if (indexVar.getType().equals("var")) {
                        RegisterEnum varIndReg = getVarReg(indexVar, false);
                        makeSll(varIndReg, varIndReg, 2);
                        makeInstr("li $" + toReg + ", " + arrSymbol.getDimen2(), true);
                        makeInstr("mult", varIndReg, toReg);
                        makeInstr("mflo", toReg);
                        makeMemory("lw", varIndReg, arrOff);
                        makeInstr("add", toReg, toReg, varIndReg);
                        RegisterManage.freeReg(varIndReg);
                    }
                }
            }
            else {
                String localArrayAddr = getSymbolAddressAtStack(arrSymbol, 0);
                if (array.getVar() == null) makeMemory("la", toReg, localArrayAddr);
                else {
                    Var indexVar = array.getVar();
                    if (indexVar.getType().equals("num")) {
                        int indNum = indexVar.getNum();
                        String localArrayAddr2 = getSymbolAddressAtStack(arrSymbol, indNum * arrSymbol.getDimen2() * 4);
                        makeMemory("la", toReg, localArrayAddr2);
                    }
                    else if (indexVar.getType().equals("var")) {
                        RegisterEnum varIndReg = getVarReg(indexVar, false);
                        makeSll(varIndReg, varIndReg, 2);
                        makeInstr("li $" + toReg + ", " + arrSymbol.getDimen2(), true);
                        makeInstr("mult", varIndReg, toReg);
                        makeInstr("mflo", toReg);
                        makeInstr("addu", toReg, toReg, RegisterEnum.sp);
                        makeMemory("la", toReg, localArrayAddr + "($" + toReg + ")");
                        RegisterManage.freeReg(varIndReg);
                    }
                }
            }
        }
        else {
            String addr = getSymbolAddressAtStack(arrSymbol, 0);
            if (array.getVar() == null) makeInstr("li $" + toReg + ", " + addr, true);
            else {
                Var indexVar = array.getVar();
                if (indexVar.getType().equals("num")) {
                    int indNum = indexVar.getNum();
                    String addr2 = getSymbolAddressAtStack(arrSymbol, indNum * arrSymbol.getDimen2() * 4);
                    makeInstr("li $" + toReg + ", " + addr2, true);
                }
                else if (indexVar.getType().equals("var")) {
                    RegisterEnum varIndReg = getVarReg(indexVar, false);
                    makeSll(varIndReg, varIndReg, 2);
                    makeInstr("li $" + toReg + ", " + arrSymbol.getDimen2(), true);
                    makeInstr("mult", varIndReg, toReg);
                    makeInstr("mflo", toReg);
                    makeAddIHex(toReg, toReg, addr);
                    RegisterManage.freeReg(varIndReg);
                }
            }
        }
    }

    public void makeStorePush(MidCode midCode) {
        pushStore.add(midCode);
    }

    public void makeCall(MidCode m) {
        Symbol funcSymbol = MidGenerate.GlobalTable2Find(m.getIRString());
        pushOff = 0; busyOff = 0;
        ArrayList<RegisterEnum> busyList = RegisterManage.busyRegs;
        int preOff = 0;
        for (int i = busyList.size() - 1; i >= 0; i--) {
            makeAddI(RegisterEnum.sp, RegisterEnum.sp, -4);
            makeMemory("sw", busyList.get(i), "($sp)");
            preOff += 4;
        }
        busyOff = preOff;
        ArrayList<Integer> removePush = new ArrayList<>();
        ArrayList<RegisterEnum> should_free_aft_push = new ArrayList<>();
        inPush = true;
        for (int i = pushStore.size() - funcSymbol.getParamsLen(); i < pushStore.size(); i++) {
            RegisterEnum free = makePush(pushStore.get(i));
            if (!free.equals(RegisterEnum.none)) should_free_aft_push.add(free);
            removePush.add(i);
        }
        inPush = false;
        for (int j = removePush.size() - 1; j >= 0; j--) {
            int i = removePush.get(j);
            pushStore.remove(i);
        }
        busyOff = 0;
        makeAddI(RegisterEnum.sp, RegisterEnum.sp, pushOff - 4);
        makeMemory("sw", RegisterEnum.ra, "($sp)");
        makeInstr("jal Function_" + m.getIRString(), true);
        makeMemory("lw", RegisterEnum.ra, "($sp)");
        makeAddI(RegisterEnum.sp, RegisterEnum.sp, -(pushOff - 4));
        for (RegisterEnum r : busyList) {
            makeMemory("lw", r, "($sp)");
            makeAddI(RegisterEnum.sp, RegisterEnum.sp, 4);
        }
        for (RegisterEnum r : should_free_aft_push) RegisterManage.freeReg(r);
    }

    public void makeAssign(MidCode m) {
        Var left = m.getOp1(), right = m.getOp2(), des = m.getDst();
        RegisterEnum lReg, rReg, dReg;
        if (left.getType().equals("array")) {
            lReg = getReg_KickOutTmp(left, false);
            LwOrSW_between_Reg_Array("lw", lReg, left);
        }
        else lReg = getVarReg(left, false);
        if (right.getType().equals("array")) {
            rReg = getReg_KickOutTmp(left, false);
            LwOrSW_between_Reg_Array("lw", rReg, right);
        }
        else rReg = getVarReg(right, false);
        dReg = checkIfVariableHadReg_IFNotApply(des);
        switch (m.getOperator()) {
            case "+":
                makeInstr("addu", dReg, lReg, rReg);
                break;
            case "-":
                makeInstr("subu", dReg, lReg, rReg);
                break;
            case "*":
                if (optimize) {
                    if (left.getType().equals("num")) multOptimize(dReg, rReg, left.getNum());
                    else if (right.getType().equals("num")) multOptimize(dReg, lReg, right.getNum());
                    else {
                        makeInstr("mult", lReg, rReg);
                        makeInstr("mflo", dReg);
                    }
                    break;
                }
                makeInstr("mult", lReg, rReg);
                makeInstr("mflo", dReg);
                break;
            case "/":
                if (optimize) {
                    if (right.getType().equals("num")) divOptimize(lReg, dReg, right.getNum());
                    else {
                        makeInstr("div", lReg, rReg);
                        makeInstr("mflo", dReg);
                    }
                    break;
                }
                makeInstr("div", lReg, rReg);
                makeInstr("mflo", dReg);
                break;
            case "%":
                if (optimize) {
                    if (right.getType().equals("num")) modOptimize(lReg, dReg, right.getNum());
                    else {
                        makeInstr("div", lReg, rReg);
                        makeInstr("mfhi", dReg);
                    }
                    break;
                }
                makeInstr("div", lReg, rReg);
                makeInstr("mfhi", dReg);
                break;
            case "bitand":
                makeInstr("and", dReg, lReg, rReg);
                break;
        }
        RegisterManage.freeReg(lReg);
        RegisterManage.freeReg(rReg);
        if (des.isKindFlag()) RegisterManage.freeReg(dReg);
    }

    public void makeAssign2(MidCode m) {
        Var dst = m.getDst();
        Var right = m.getOp1();
        RegisterEnum reg;
        if (right.getType().equals("array")) {
            reg = getReg_KickOutTmp(right, false);
            LwOrSW_between_Reg_Array("lw", reg, right);
        }
        else reg = getVarReg(right, false);
        if (dst.getType().equals("array")) LwOrSW_between_Reg_Array("sw", reg, dst);
        else saveReg2Var(dst, reg);
        RegisterManage.freeReg(reg);
    }

    public void outMipsCode() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("mips.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        for (Mips word : this.mips) {
            byte[] bytes = (word.toMipsString().toString()).getBytes();
            bufferedOutputStream.write(bytes);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }

    public void makeAssignRet(MidCode m) {
        saveReg2Var(m.getVar(), RegisterEnum.v0);
    }

    public void makeIntDecl(MidCode m) {
        if (m.isGlobal()) data.addGIA(m);
        else {
            Symbol symbol = m.getSymbol();
            makeInstr("addi $sp, $sp, -4", true);
            m.getScope().inBlockOff -= 4;
            if (inFuncDel) {
                inFuncOff -= 4;
                symbol.setSpOff(inFuncOff);
            }
            else {
                spOff -= 4;
                symbol.setSpOff(spOff);
            }
            if (m.isInit()) {
                makeInstr("li $v1, " + m.getNum(), true);
                makeInstr("sw $v1, 0($sp)", true);
            }
        }
    }

    public void makeReturn(MidCode m) {
        if (inFuncDel) {
            if (!m.isVoidReturn()) {
                Var ret = m.getVar();
                RegisterEnum r;
                if (ret.getType().equals("array")) {
                    r = getReg_KickOutTmp(ret, false);
                    LwOrSW_between_Reg_Array("lw", r, ret);
                } else r = getVarReg(ret, false);
                makeInstr("move", RegisterEnum.v0, r);
                RegisterManage.freeReg(r);
            }
            if (inFuncOff == 0) makeInstr("#addi $sp, $sp, 0", true);
            else makeInstr("addi $sp, $sp, " + -inFuncOff, true);
            makeInstr("jr $ra", true);
        }
        else {
            makeInstr("#End Program", true);
            makeInstr("li $v0, 10", true);
            makeInstr("syscall", true);
        }
    }

    public void makeArrayDecl(MidCode m) {
        if (m.isGlobal()) data.addGIA(m);
        else {
            int arrOff = m.getArrSize() * 4;
            Symbol symbol = m.getSymbol();
            m.getScope().inBlockOff -= arrOff;
            if (inFuncDel) {
                inFuncOff -= arrOff;
                symbol.setSpOff(inFuncOff);
            } else {
                spOff -= arrOff;
                symbol.setSpOff(spOff);
            }
            makeAddI(RegisterEnum.sp, RegisterEnum.sp, -arrOff);
            if (m.isInit()) {
                int off;
                for (int i = 0; i < m.getInitList().size(); i++) {
                    off = i * 4;
                    makeInstr("li $t0, " + m.getInitList().get(i), true);
                    makeMemory("sw", RegisterEnum.t0, off + "($sp)");
                }
            }
        }
    }

    public void makeBranch(MidCode m) {
        Var l = m.getOp1(), r = m.getOp2();
        String jLabel = m.getJmpPos();
        RegisterEnum lReg = getVarReg(l, true), rReg = getVarReg(r, true);
        if (l.getType().equals("var") && r.getType().equals("var")) makeBeq(m.getIns(), lReg, rReg, jLabel);
        else if (l.getType().equals("var") && r.getType().equals("num")) makeBeqI(m.getIns(), lReg, r.getNum(), jLabel);
        else if (l.getType().equals("num") && r.getType().equals("var")) makeBeqI(MipsValue.reverseCmp(m.getIns()), rReg, l.getNum(), jLabel);
        else {
            if (MipsValue.cmpBeq(m.getIns(), l.getNum(), r.getNum())) makeInstr("j " + jLabel, true);
        }
        if (!lReg.equals(RegisterEnum.wrong)) RegisterManage.freeReg(lReg);
        if (!rReg.equals(RegisterEnum.wrong)) RegisterManage.freeReg(rReg);
    }

    public void makeSetCmp(MidCode m) {
        Var l = m.getOp1(), r = m.getOp2(), d = m.getDst();
        RegisterEnum lReg = getVarReg(l, false), rReg = getVarReg(r, false);
        RegisterEnum dReg = getVarReg(d, false);
        if (l.getType().equals("num") && r.getType().equals("num"))
            if (MipsValue.cmpBeq(m.getOperator(), l.getNum(), r.getNum())) makeInstr("li $"+dReg+", 1", true);
            else makeInstr("li $"+dReg+", 0", true);
        else makeSlt(m.getOperator(), dReg, lReg, rReg);
        RegisterManage.freeReg(lReg);
        RegisterManage.freeReg(rReg);
    }

    public RegisterEnum lwSymbolFromGlobalLabel(Var var, RegisterEnum des) {
        if (des == null) {
            RegisterEnum r = getReg_KickOutTmp(var, false);
            makeInstr("lw $" + r + ", Global_" + var.getName(), true);
            return r;
        }
        else {
            makeInstr("lw $" + des + ", Global_" + var.getName(), true);
            return des;
        }
    }

    public RegisterEnum lwSymbolFromStack(Var var, RegisterEnum des, boolean an, boolean tn) {
        Symbol symbol = var. getSymbol();
        if ((var.isKindFlag() || an) && symbol != null) {
            String add = getSymbolAddressAtStack(symbol, 0);
            if (des == null) {
                RegisterEnum reg = getReg_KickOutTmp(var, false);
                makeInstr("lw $" + reg + ", " + add, true);
                return reg;
            } else {
                makeInstr("lw $" + des + ", " + add, true);
                return des;
            }
        }
        else if (tn) {
            makeInstr("lw $" + des + ", " + getTmpVarAddressAtStack(var), true);
            return des;
        }
        return null;
    }

    public RegisterEnum getReg_KickOutTmp(Var var, boolean isTmp) {
        RegisterEnum reg = RegisterManage.getReg(var, isTmp);
        if (!reg.equals(RegisterEnum.regisempty)) return reg;
        Var tmpVar = RegisterManage.kick();
        RegisterEnum preReg = tmpVar.getCurReg();
        tmpVar.setCurReg(RegisterEnum.wrong);
        if (!tmpVar.isHasBeenKicked()) {
            tmpVar.setHasBeenKicked(true);
            tmpVar.getTable().inBlockOff -= 4;
            makeInstr("addi $sp, $sp, -4", true);
            if (inFuncDel) {
                inFuncOff -= 4;
                tmpVar.setSpOffset(inFuncOff);
            } else {
                spOff -= 4;
                tmpVar.setSpOffset(spOff);
            }
            makeInstr("move", RegisterEnum.v1, preReg);
            makeInstr("sw $v1, 0($sp)", true);
        } else makeMemory("sw", preReg, getTmpVarAddressAtStack(var));
        return RegisterManage.getReg(var, isTmp);
    }

    public String getTmpVarAddressAtStack(Var v) {
        if (inFuncDel) return v.getSpOffset() - inFuncOff + busyOff + "($sp)";
        else return "0x" + Integer.toHexString(MipsValue.topStack + v.getSpOffset());
    }

    public String getSymbolAddressAtStack(Symbol symbol, int arr_off) {
        if (inFuncDel) return getSymbolOffInFunc(symbol, arr_off) + busyOff + "($sp)";
        else return "0x" + Integer.toHexString(MipsValue.topStack + symbol.getSpOff() + arr_off);
    }

    public int getSymbolOffInFunc(Symbol symbol, int array_off) {
        ArrayList<Integer> ans = curFunc.haveThePara(symbol.getName());
        if (ans.get(0) == 1) return array_off + 4 - inFuncOff + (curFunc.getParamsLen() - ans.get(1)) * 4;
        else return array_off - inFuncOff + symbol.getSpOff();
    }

    public RegisterEnum getVarReg(Var var, boolean needNum) {
        RegisterEnum reg = RegisterEnum.wrong;
        if (var.getType().equals("var")) {
            if (var.isKindFlag()) {
                if (var.getSymbol().getConstType().equals(ConstType.CONST)) {
                    reg = getReg_KickOutTmp(var, false);
                    makeInstr("li $" + reg + ", " + var.getSymbol().getNum(), true);
                }
                else if (var.getSymbol().isGlobal()) reg = lwSymbolFromGlobalLabel(var, null);
                else reg = lwSymbolFromStack(var, null, false, false);
            }
            else reg = checkIfVariableHadReg_IFNotApply(var);
        }
        else if (var.getType().equals("num")) {
            if (needNum) return RegisterEnum.wrong;
            reg = getReg_KickOutTmp(var, false);
            makeInstr("li $" + reg + ", " + var.getNum(), true);
        }
        return reg;
    }

    public RegisterEnum checkIfVariableHadReg_IFNotApply(Var var) {
        if (var.getCurReg() == RegisterEnum.wrong) {
            RegisterEnum reg = getReg_KickOutTmp(var, true);
            if (var.isHasBeenKicked()) lwSymbolFromStack(var, reg, false, true);
            var.setCurReg(reg);
            return reg;
        }
        else return var.getCurReg();
    }

    public void LwOrSW_between_Reg_Array(String instr, RegisterEnum reg, Var arrayNum) {
        Symbol arrSymbol = arrayNum.getSymbol();
        String arrName = arrayNum.getName();
        Var arrIndex = arrayNum.getVar();
        if (arrSymbol.isGlobal()) {
            if (arrIndex.getType().equals("num")) {
                if (instr.equals("lw")) {
                    makeMemory("la", reg, "Global_" + arrName);
                    makeMemory("lw", reg, arrIndex.getNum() * 4 + "($" + reg + ")");
                } else if (instr.equals("sw")) {
                    RegisterEnum tmp = getReg_KickOutTmp(arrayNum, false);
                    makeMemory("la", tmp, "Global_" + arrName);
                    makeMemory("sw", reg, arrIndex.getNum() * 4 + "($" + tmp + ")");
                    RegisterManage.freeReg(tmp);
                }
            }
            else if (arrIndex.getType().equals("var")) {
                RegisterEnum indexReg = getVarReg(arrIndex, false);
                makeSll(indexReg, indexReg, 2);
                makeMemory(instr, reg, "Global_" + arrName + "($" + indexReg + ")");
                RegisterManage.freeReg(indexReg);
            }
        }
        else {
            if (!inFuncDel) {
                if (arrIndex.getType().equals("num")) {
                    String addr = getSymbolAddressAtStack(arrSymbol, arrIndex.getNum() * 4);
                    makeMemory(instr, reg, addr);
                }
                else if (arrIndex.getType().equals("var")) {
                    String addr = getSymbolAddressAtStack(arrSymbol, 0);
                    RegisterEnum indexReg = getVarReg(arrIndex, false);
                    makeSll(indexReg, indexReg, 2);
                    makeMemory(instr, reg, addr + "($" + indexReg + ")");
                    RegisterManage.freeReg(indexReg);
                }
            }
            else {
                if (curFunc.haveThePara(arrName).get(0) == 1) {
                    RegisterEnum baseReg = lwSymbolFromStack(arrayNum, null, true, false);
                    if (arrIndex.getType().equals("num"))
                        makeMemory(instr, reg, arrIndex.getNum() * 4 + "($" + baseReg + ")");
                    else if (arrIndex.getType().equals("var")) {
                        RegisterEnum indexReg = getVarReg(arrIndex, false);
                        makeSll(indexReg, indexReg, 2);
                        makeInstr("add", baseReg, baseReg, indexReg);
                        makeMemory(instr, reg, "($" + baseReg + ")");
                        RegisterManage.freeReg(indexReg);
                    }
                    RegisterManage.freeReg(baseReg);
                }
                else {
                    if (arrIndex.getType().equals("num")) {
                        String addr = getSymbolAddressAtStack(arrSymbol, arrIndex.getNum() * 4);
                        makeMemory(instr, reg, addr);
                    }
                    else if (arrIndex.getType().equals("var")) {
                        RegisterEnum indexReg = getVarReg(arrIndex, false);
                        makeSll(indexReg, indexReg, 2);
                        makeInstr("add $" + indexReg + ", $" + indexReg + ", $sp", true);
                        int arrBaseOff = getSymbolOffInFunc(arrSymbol, 0);
                        makeMemory(instr, reg, arrBaseOff + "($" + indexReg + ")");
                        RegisterManage.freeReg(indexReg);
                    }
                }
            }
        }
    }

    public void saveReg2Var(Var dst, RegisterEnum rreg) {
        Symbol symbol = dst.getSymbol();
        if (dst.getType().equals("var")) {
            if (dst.isKindFlag()) {
                if (symbol.isGlobal()) makeMemory("sw", rreg, "Global_"+ symbol.getName());
                else makeMemory("sw", rreg, getSymbolAddressAtStack(symbol, 0));
            }
            else {
                RegisterEnum reg = getVarReg(dst, false);
                makeInstr("move", reg, rreg);
            }
        }
    }

    public void multOptimize(RegisterEnum dReg, RegisterEnum lReg, int r) {
        if (isPowerOf2(r)) {
            int mi = (int) (Math.log(r) / Math.log(2));
            makeSll(dReg, lReg, mi);
        } else if (r == 0) {
            makeLi(dReg, 0);
        } else {
            makeLi(RegisterEnum.fp, r);
            makeInstr("mult", lReg, RegisterEnum.fp);
            makeInstr("mflo", dReg);
        }
    }

    public boolean isPowerOf2(int n) {
        if (n <= 0) return false;
        return (n & (n - 1)) == 0;
    }

    public void divOptimize (RegisterEnum lReg, RegisterEnum dReg, int r) {
        Triple<Long, Integer, Integer> multiplier = chooseMultiplier(Math.abs(r));
        long m = multiplier.getFirst();
        int sh = multiplier.getSecond();
        if (Math.abs(r) == 1) makeInstr("move", dReg, lReg);
        else if (isPowerOf2(Math.abs(r))) {
            int mi = (int) (Math.log(Math.abs(r)) / Math.log(2));
            makeSraSrl("sra", dReg, lReg, (mi - 1));
            makeSraSrl("srl", dReg, dReg, (32 - mi));
            makeInstr("add", dReg, dReg, lReg);
            makeSraSrl("sra", dReg, dReg, mi);
        }
        else if (m < Math.pow(2, 31)) {
            makeLi(RegisterEnum.a3, m);
            makeInstr("mult", lReg, RegisterEnum.a3);
            makeInstr("mfhi", dReg);
            makeSraSrl("sra", dReg, dReg, sh);
            makeSraSrl("slti", RegisterEnum.a3, lReg, 0);
            makeInstr("add", dReg, dReg, RegisterEnum.a3);
        }
        else {
            makeLi(RegisterEnum.a3, (int) (m - Math.pow(2, 32)));
            makeInstr("mult", lReg, RegisterEnum.a3);
            makeInstr("mfhi", dReg);
            makeInstr("add", dReg, dReg, lReg);
            makeSraSrl("sra", dReg, dReg, sh);
            makeSraSrl("slti", RegisterEnum.a3, lReg, 0);
            makeInstr("add", dReg, dReg, RegisterEnum.a3);
        }
        if (r < 0) makeInstr("sub", dReg, RegisterEnum.zero, dReg);
    }

    public void modOptimize (RegisterEnum lReg, RegisterEnum dReg, int r) {
        if (r == 1 || r == -1) {
            makeLi(dReg, 0);
        } else {
            divOptimize(lReg, dReg, r);
            makeLi(RegisterEnum.fp, r);
            makeInstr("mult", dReg, RegisterEnum.fp);
            makeInstr("mflo", RegisterEnum.fp);
            makeInstr("sub", dReg, lReg, RegisterEnum.fp);
        }
    }

    private Triple<Long, Integer, Integer> chooseMultiplier(int d) {
        long nc = (1L << 31) - ((1L << 31) % d) - 1;
        long p = 32;
        while ((1L << p) <= nc * (d - (1L << p) % d)) {
            p++;
        }
        long m = (((1L << p) + (long) d - (1L << p) % d) / (long) d);
        long n = ((m << 32) >>> 32);
        return new Triple<>(n, (int) (p - 32), 0);
    }

    public void makeInstr(String s, boolean t) {
        mips.add(new MipsString(s , t));
    }

    public void makeInstr(String t, RegisterEnum dst, RegisterEnum op1, RegisterEnum op2) {
        mips.add(new MipsCal(t, dst, op1, op2));
    }

    public void makeInstr(String t, RegisterEnum op1, RegisterEnum op2) {
        mips.add(new MipsCal(t, op1, op2));
    }

    public void makeInstr(String t, RegisterEnum dst) {
        mips.add(new MipsCal(t, dst));
    }

    public void makeMemory(String type, RegisterEnum r, String address) {
        mips.add(new MipsString(type + " $" + r + ", " + address, true));
    }

    public void makeBeq(String type, RegisterEnum left, RegisterEnum right, String label) {
        mips.add(new MipsString(type + " $" + left + ", $" + right + ", " + label, true));
    }

    public void makeBeqI(String type, RegisterEnum left, int right, String label) {
        mips.add(new MipsString(type + " $" + left + ", " + right + ", " + label, true));
    }

    public void makeSlt(String t, RegisterEnum dst, RegisterEnum op1, RegisterEnum op2) {
        mips.add(new MipsString(t + " $" + dst + ", $" + op1 + ", $" + op2, true));
    }

    public void makeSll(RegisterEnum op1, RegisterEnum op2, int num) {
        mips.add(new MipsString("sll $" + op1 + ", $" + op2 + ", " + num, true));
    }

    public void makeAddI(RegisterEnum op1, RegisterEnum op2, int num) {
        mips.add(new MipsString("addi $" + op1 + ", $"+ op2 + ", " + num, true));
    }

    public void makeAddIHex(RegisterEnum op1, RegisterEnum op2, String num) {
        mips.add(new MipsString("addi $" + op1 + ", $"+ op2 + ", " + num, true));
    }

    public void makeSraSrl(String t, RegisterEnum dst, RegisterEnum left, int r) {
        mips.add(new MipsString(t + " $" + dst + ", $" + left + ", " + r, true));
    }

    public void makeLi(RegisterEnum r, long rv) {
        mips.add(new MipsString("li $" + r + ", " + rv, true));
    }
}
