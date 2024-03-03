package MipsGenerate;

public class MipsValue {
    public static final int topStack = 0x7fffeffc;

    public static String reverseCmp(String cmp) {
        if ("bge".equals(cmp)) return "blt";
        else if ("ble".equals(cmp)) return "bgt";
        else if ("bgt".equals(cmp)) return "ble";
        else if ("blt".equals(cmp)) return "bge";
        else if ("sge".equals(cmp)) return "slt";
        else if ("sle".equals(cmp)) return "sgt";
        else if ("sgt".equals(cmp)) return "sle";
        else if ("slt".equals(cmp)) return "sge";
        return cmp;
    }

    public static boolean cmpBeq(String cmp, int num1, int num2) {
        if ("beq".equals(cmp) || "seq".equals(cmp)) return num1 == num2;
        else if ("bne".equals(cmp) || "sne".equals(cmp)) return num1 != num2;
        else if ("bge".equals(cmp) || "sge".equals(cmp)) return num1 >= num2;
        else if ("ble".equals(cmp) || "sle".equals(cmp)) return num1 <= num2;
        else if ("bgt".equals(cmp) || "sgt".equals(cmp)) return num1 > num2;
        else if ("blt".equals(cmp) || "slt".equals(cmp)) return num1 < num2;
        return false;
    }
}
