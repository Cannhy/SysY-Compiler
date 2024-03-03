package MipsGenerate;

import MidCodeGenerate.Var;

import java.util.ArrayList;
import java.util.LinkedList;

public class RegisterManage {
    public static final LinkedList<Var> tmpRegs = new LinkedList<>();
    public static final ArrayList<RegisterEnum> freeRegs = new ArrayList<>();
    public static final ArrayList<RegisterEnum> busyRegs = new ArrayList<>();
    public static final ArrayList<RegisterEnum> freeRegsPush = new ArrayList<>();

    static {
        resetRegs();
    }
    static {
        resetPushRegs();
    }

    public static RegisterEnum getReg(Var var, boolean isTmp) {
        if (MipsGenerate.inPush) {
            if (!freeRegsPush.isEmpty()) {
                RegisterEnum t = freeRegsPush.get(0);
                freeRegsPush.remove(0);
                return t;
            }
            return RegisterEnum.wrong;
        }
        if (!freeRegs.isEmpty()) {
            RegisterEnum r = freeRegs.get(0);
            if (isTmp) tmpRegs.add(var);
            if (!busyRegs.contains(r)) busyRegs.add(r);
            freeRegs.remove(0);
            return r;
        }
        return RegisterEnum.regisempty;
    }

    public static void freeReg(RegisterEnum r) {
        if (MipsGenerate.inPush) {
            freeRegsPush.add(r);
            return;
        }
        int RegPos = r.ordinal();
        if (RegPos == 28 || RegPos==29 || RegPos == 30 || RegPos == 31 || RegPos < 8 || RegPos > 31) {
            freeRegs.add(RegisterEnum.none);
            freeRegs.add(RegisterEnum.wrong);
            freeRegs.add(RegisterEnum.wrong);
            freeRegs.add(RegisterEnum.wrong);
            busyRegs.remove(RegisterEnum.none);
        } else {
            freeRegs.add(r);
            ArrayList<Var> delRegs = new ArrayList<>();
            for (Var var : tmpRegs) if (var.getCurReg().equals(r)) delRegs.add(var);
            for (Var var : delRegs) tmpRegs.remove(var);
            busyRegs.remove(r);
        }
    }

    public static Var kick() {
        Var tmp = tmpRegs.removeFirst();
        freeReg(tmp.getCurReg());
        return tmp;
    }

    public static void resetRegs() {
        freeRegs.clear();
        for (RegisterEnum r : RegisterEnum.values()) {
            int i = r.ordinal();
            if (i >= 8 && i<=31 && !(i == 28 || i==29 || i == 30 || i == 31)) freeRegs.add(r);
        }
    }

    public static void resetPushRegs() {
        freeRegsPush.clear();
        freeRegsPush.add(RegisterEnum.a1);
        freeRegsPush.add(RegisterEnum.a2);
        freeRegsPush.add(RegisterEnum.a3);
        freeRegsPush.add(RegisterEnum.gp);
        freeRegsPush.add(RegisterEnum.fp);
    }
}
