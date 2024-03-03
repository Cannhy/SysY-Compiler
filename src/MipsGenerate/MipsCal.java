package MipsGenerate;

public class MipsCal extends Mips{
    private String type = "";
    private RegisterEnum op1 = RegisterEnum.wrong;
    private RegisterEnum op2 = RegisterEnum.wrong;
    private RegisterEnum dst = RegisterEnum.wrong;

    public MipsCal(String type, RegisterEnum dest, RegisterEnum op1, RegisterEnum op2) {
        this.type = type;
        this.dst = dest;
        this.op1 = op1;
        this.op2 = op2;
    }

    public MipsCal(String type, RegisterEnum dest) {
        this.type = type;
        this.dst = dest;
    }

    public MipsCal(String type, RegisterEnum op1, RegisterEnum op2) {
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public StringBuilder toMipsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tab).append(type).append(" $");
        if (dst.equals(RegisterEnum.wrong))  return sb.append(op1).append(", $").append(op2).append("\n");
        if (op1.equals(RegisterEnum.wrong) && op2.equals(RegisterEnum.wrong)) return sb.append(dst).append("\n");
        return sb.append(dst).append(", $").append(op1).append(", $").append(op2).append("\n");
    }
}
