package Table;

import java.util.ArrayList;

public class Table {
    private ArrayList<Symbol> symbols;
    private Table fatherTable;
    private ArrayList<Table> sonTables;
    private BlockType blockType;
    public int inBlockOff = 0;

    public Table(Table father,BlockType blockType){
        this.symbols = new ArrayList<>();
        this.sonTables = new ArrayList<>();
        this.fatherTable = father;
        this.blockType = blockType;
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    public void addSymbol(Symbol symbol){
        this.symbols.add(symbol);
    }

    public Table getFatherTable(){
        return this.fatherTable;
    }

    public Symbol getSameSymbol(String name) {
        for (Symbol symbol : symbols)
            if (symbol.getName().equals(name)) return symbol;
        return null;
    }

    public void addSon(Table son){
        sonTables.add(son);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public int getInBlockOff() {
        return inBlockOff;
    }

    public void setInBlockOff(int inBlockOff) {
        this.inBlockOff = inBlockOff;
    }
}
