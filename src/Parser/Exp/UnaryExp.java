package Parser.Exp;

import AST.ASTLeaf;
import AST.LeafKind;
import AST.LeafType;
import Lexer.*;
import Error.*;
import Parser.Func.FuncRParams;
import Parser.Parser;
import Parser.TokenParent;
import Table.FuncKind;
import Table.Symbol;
import Table.TableType;

import java.util.ArrayList;

public class UnaryExp extends TokenParent {

    @Override
    public ArrayList<String> parse(){
        if (Lexer.symValIs("+") || Lexer.symValIs("-") || Lexer.symValIs("!")) {
            String operator = Lexer.sym.getVal();
            addAll(new UnaryOp().parse());
            UnaryExp unaryExp = new UnaryExp();
            addAll(unaryExp.parse());
            this.astLeaf = new ASTLeaf(LeafType.OPE, unaryExp.getAstLeaf());
            this.astLeaf.setOpName(operator);
        } else if (Lexer.wordTypeIs(0, Type.IDENFR) && Lexer.wordTypeIs(1, Type.LPARENT)) {
            Word ident = Lexer.sym;
            ASTLeaf identLeaf = generateLeaf(ident);
            identLeaf.setKind(LeafKind.FUNC);
            Symbol symbol = ErrorMaker.makeC(ident, TableType.FUNC);
            if (symbol != null) Parser.dimension = (symbol.getFuncKind().equals(FuncKind.INT)) ? 0 : 114514;

            add(Lexer.getNextSym());
            add(Lexer.getNextSym());

            int num = 0;
            ArrayList<Integer> dimensions = null;
            boolean start2DECheck = true;
            ASTLeaf paramsLeaf = null;
            if (Lexer.symValIs(")")) add(Lexer.getNextSym());
            else {
                int tmpAddressDimension = Parser.dimension;
                FuncRParams f = new FuncRParams();
                addAll(f.parse());
                num = f.getParaNum();
                paramsLeaf = f.getAstLeaf();
                dimensions = f.getAddressDimensions();
                start2DECheck = match(")");
                Parser.dimension = tmpAddressDimension;
            }
            identLeaf.setL(paramsLeaf);
            this.astLeaf = identLeaf;
            if (start2DECheck && symbol != null)
                if (!ErrorMaker.makeD(ident, symbol, num) && num != 0) ErrorMaker.makeE(ident, symbol, dimensions);
        } else {
            PrimaryExp primaryExp = new PrimaryExp();
            addAll(primaryExp.parse());
            this.astLeaf = primaryExp.getAstLeaf();
        }
        add("<UnaryExp>");
        return sublist;
    }
}
