package MipsGenerate;

public class MipsString extends Mips {
    private final String note;

    public MipsString(String note, boolean T) {
        this.note = T ? tab + note : note;
    }

    @Override
    public StringBuilder toMipsString() {
        return new StringBuilder().append(note).append("\n");
    }
}
