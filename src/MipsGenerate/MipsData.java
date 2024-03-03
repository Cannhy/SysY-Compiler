package MipsGenerate;

import MidCodeGenerate.MidCode;
import MidCodeGenerate.MidType;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsData extends Mips {
    private final HashMap<MidCode, String> printfStringMap = new HashMap<>();
    private final ArrayList<MidCode> globalInitArray = new ArrayList<>();

    public void addPSM(MidCode midCode, String string) {
        printfStringMap.put(midCode, string);
    }

    public void addGIA(MidCode midCode) {
        globalInitArray.add(midCode);
    }

    public String getString(MidCode midCode) {
        return printfStringMap.getOrDefault(midCode, null);
    }

    @Override
    public StringBuilder toMipsString() {
        StringBuilder sb = new StringBuilder(".data\n");
        for (MidCode m : printfStringMap.keySet())
            sb.append(tab).append(printfStringMap.get(m)).append(": ").append(".asciiz").append(tab)
                    .append("\"").append(m.getVar().getName()).append("\"").append("\n");
        for (MidCode m : globalInitArray) {
            sb.append(tab).append("Global_")
                    .append(m.getName()).append(": ").append(".word ");
            if (m.getType().equals(MidType.intDecl)) sb.append(m.isInit() ? m.getNum() : "0");
            else {
                if (m.isInit()) {
                    for (Integer i : m.getInitList()) sb.append(i).append(",");
                    sb.deleteCharAt(sb.length() - 1);
                } else sb.append("0:").append(m.getArrSize());
            }
            sb.append("\n");
        }
        return sb;
    }
}
