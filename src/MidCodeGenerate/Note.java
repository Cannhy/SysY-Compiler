package MidCodeGenerate;

import java.util.HashMap;

public class Note {
    public static HashMap<NoteType, String> notes;

    public static String getVal(NoteType n) {
        return notes.get(n);
    }

    static {
        notes = new HashMap<>();
        notes.put(NoteType.StartPrint,"#Start Print");
        notes.put(NoteType.StartFuncDel,"#Start FuncDecl");
        notes.put(NoteType.StartDecl,"#Start Decl");
        notes.put(NoteType.StartMainFunc,"#Start MainFunc");
        notes.put(NoteType.OutBlock,"#Out Block");
        notes.put(NoteType.OutBlockForCut,"#Out Block ForCut");
        notes.put(NoteType.endFunc,"#end func");
    }
}
