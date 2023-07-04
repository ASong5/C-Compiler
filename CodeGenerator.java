import absyn.*;

public class CodeGenerator implements AbsynVisitor {
    private static final int pc = 7;
    private static final int gp = 6;
    private static final int fp = 5;
    private static final int ac = 0;
    private static final int ac1 = 1;
    public static final int ofpFO = 0;
    public static final int retFO = -1;
    public static final int initFO = -2;

    private static int emitLoc = 0;
    private static int highEmitLoc = 0;
    private static int globalOffset = 0;
    private static int frameOffset = 0;
    private static int mainEntry = 0;

    public CodeGenerator() {}

    private void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d, %d, %d", emitLoc, op, r, s, t);
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }

    private void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d, %d(%d)", emitLoc, op, r, d, s);
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }

    private void emitRM_ABS(String op, int r, int a, String c) {
        System.out.printf("%3d: %5s %d, %d(%d)", emitLoc, op, r, a - (emitLoc + 1), pc);
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
    }

    private int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;

        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;

        return i;
    }

    private void emitBackup(int loc) {
        if (loc > highEmitLoc)
            emitComment("BUG in emitBackup");
        emitLoc = loc;
    }

    public void emitRestore() {
        emitLoc = highEmitLoc;
    }

    private void emitComment(String c) {
        System.out.printf("* %s\n", c);
    }

    public void visit(Absyn trees)
    {
        emitComment("C-Minus Compilation to TM Code");
        emitComment("Standard prelude");
        emitRM("LD", gp, 0, ac, "load gp with maxaddr");
        emitRM("LDA", fp, 0, gp, "copy gp to fp");
        emitRM("ST", ac, 0, ac, "clear value at location " + ac);

        int savedLoc = emitSkip(1);

        emitComment("Jump around i/o routines here");
        emitComment("Code for input routine");
        emitRM("ST", 0, retFO, fp, "store return");
        emitRO("IN", 0, 0, 0, "input");
        emitRM("LD", pc, retFO, fp, "return to caller");

        emitComment("Code for output routine");
        emitRM("ST", 0, retFO, fp, "store return");
        emitRM("ST", 0, initFO, fp, "load output value");
        emitRO("OUT", 0, 0, 0, "output");
        emitRM("LD", pc, retFO, fp, "return to caller");

        int savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitRM_ABS("LDA", pc, savedLoc2, "jump around i/o code");
        emitRestore();
        emitComment("End of standard prelude.");

        visit((DecList)trees, 0, false);

        emitComment("Standard finale");
        emitRM("ST", fp, globalOffset+ofpFO, fp, "push ofp");
        emitRM("LDA", fp, globalOffset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRM_ABS("LDA", pc, mainEntry, "jump to main loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");

        emitComment("End of execution");
        emitRO("HALT", 0, 0, 0, "");
    }

    public void visit(ExpList expList, int level, boolean isAddr) {
        while (expList != null) {
            if (expList.head != null)
                expList.head.accept(this, level, false);
            expList = expList.tail;
        }
    }

    public void visit(AssignExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        //lhs is an address
        if (exp.lhs != null)
            exp.lhs.accept(this, level - 1, true);

        //rhs is a value
        exp.rhs.accept(this, level - 2, false);

        //save result of assignment
        emitRM("LD", ac, level - 1, fp, "op: load left");
        emitRM("LD", ac1, level - 2, fp, "op: load right");
        emitRM("ST", ac1, ac, ac, "");
        emitRM("ST", ac1, level, fp, "assign: store value");

        emitComment("<- op");
    }

    public void visit(IfExp exp, int level, boolean isAddr) {
        emitComment("-> if");

        //process condition
        exp.test.accept( this, level, false);

        //save location for backpatching
        int savedLoc = emitSkip(1);

        exp.thenpart.accept( this, level - 1 , false);

        int savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitComment("if: jump to end belongs here");
        emitRM_ABS("JEQ", pc, savedLoc2, "if: jmp to else");
        emitRestore();

        //handle else
        if (exp.elsepart != null ) {
            emitComment("if: jump to else belongs here");
            exp.elsepart.accept( this, level - 1, false);
        }

        emitComment("<- if");
    }

    public void visit(IntExp exp, int level, boolean isAddr) {
        emitComment("-> constant");

        emitRM("LDC", ac, exp.value, ac, "load const");
        emitRM("ST", ac, level, fp, "");

        emitComment("<- constant");
    }

    public void visit(OpExp exp, int level, boolean isAddr) {
        emitComment("-> op");

        exp.left.accept(this, level - 1, false);
        exp.right.accept(this, level - 2, false);

        emitRM("LD", ac, level - 1, fp, "load lhs");
        emitRM("LD", ac1, level - 2, fp, "load rhs");

        if (exp.op == OpExp.PLUS)
            emitRO("ADD", ac, ac, ac1, "op +");
        else if (exp.op == OpExp.MINUS)
            emitRO("SUB", ac, ac, ac1, "op -");
        else if (exp.op == OpExp.MUL)
            emitRO("MUL", ac, ac, ac1, "op *");
        else if (exp.op == OpExp.DIV)
            emitRO("DIV", ac, ac, ac1, "op /");
        else
        {
            if (exp.op == OpExp.LT)
            {
                emitRO("SUB", ac, ac, ac1, "op <");
                emitRM("JLT", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.LE)
            {
                emitRO("SUB", ac, ac, ac1, "op <=");
                emitRM("JLE", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.GT)
            {
                emitRO("SUB", ac, ac, ac1, "op >");
                emitRM("JGT", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.GE)
            {
                emitRO("SUB", ac, ac, ac1, "op >=");
                emitRM("JGE", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.EQ)
            {
                emitRO("SUB", ac, ac, ac1, "op ==");
                emitRM("JEQ", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.NE)
            {
                emitRO("SUB", ac, ac, ac1, "op !=");
                emitRM("JNE", ac, 2, pc, "br if true");
            }
            emitRM("LDC", ac, ac, ac, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, ac, "true case");
        }

        emitComment("<- op");
    }

    public void visit(VarExp exp, int level, boolean isAddr) {
        if (exp.variable != null)
            exp.variable.accept(this, level, isAddr);
    }

    public void visit(WhileExp exp, int level, boolean isAddr) {
        emitComment("-> while");
        emitComment("while: jump after body comes back here");

        //save initial location
        int savedLoc = emitSkip(0);

        if (exp.test != null)
            exp.test.accept(this, level, false);

        emitComment("while: jump to end belongs here");

        //save location after test
        int savedLoc2 = emitSkip(1);

        if (exp.body != null)
            exp.body.accept(this, level - 1, false);

        emitRM_ABS("LDA", pc, savedLoc, "while: absolute jmp to test");

        //save location after body
        int savedLoc3 = emitSkip(0);

        emitBackup(savedLoc2);
        emitRM_ABS("JEQ", 0, savedLoc3, "while: jmp to end");
        emitRestore();

        emitComment("<- while");
    }

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        emitComment("-> return");

        //return expression not an address
        if (exp.exp != null)
            exp.exp.accept(this, level, false);

        emitRM("LD", pc, retFO, fp, "return to caller");

        emitComment("<- return");
    }

    public void visit(CallExp exp, int level, boolean isAddr) {
        emitComment("-> call of function: " + exp.func);

        //check call arguments
        if (exp.args != null)
            exp.args.accept(this, level + initFO, true);

        //store fp
        emitRM("ST", fp, level+ofpFO, fp, "push ofp");
        emitRM("LDA", fp, level, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRM_ABS("LDA", pc, ((FunctionDec)exp.dtype).funaddr, "jump to fun loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");

        emitComment("<- call");
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
        emitComment("-> boolean");

        //convert bool to int
        int boolInt = exp.value ? 1 : 0;

        emitRM("LDC", ac, boolInt, ac, "load boolean");
        emitRM("ST", ac, level, fp, "");

        emitComment("<- boolean");
    }

    public void visit(SimpleVar exp, int level, boolean isAddr) {
        emitComment("-> id");
        emitComment("looking up id: " + exp.name);

        if (isAddr) {
            emitRM("LDA", ac, exp.dec.offset, fp, "load id address");
            emitRM("ST", ac, level, fp, "store address");
        }
        else {
            emitRM("LD", ac, exp.dec.offset, fp, "load id value");
            emitRM("ST", ac, level, fp, "store value");
        }

        emitComment("<- id");
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {
        exp.offset = level;

        if (exp.nestLevel == 0)
            emitComment("allocating global var: " + exp.name);
        else
            emitComment("processing local var: " + exp.name);
    }

    public void visit(NameTy exp, int level, boolean isAddr) {}

    public void visit(IndexVar exp, int level, boolean isAddr) {
        emitComment("-> subs");

        exp.index.accept( this, level, isAddr);

        emitComment("<- subs");
    }

    public void visit(FunctionDec exp, int level, boolean isAddr) {
        emitComment("Processing function: " + exp.func);
        emitComment("jump around function body here");
        emitRM("ST", ac, retFO, fp, "store return");

        int savedLoc = emitSkip(1);

        //check if entering main functon
        if (exp.func.equalsIgnoreCase("main"))
            mainEntry = emitLoc;

        exp.funaddr = emitLoc;

        level += initFO;

        if (exp.params != null)
            exp.params.accept(this, level, false);

        int args = 0;

        VarDecList params = exp.params;

        //count number of function parameters
        while (params != null)
        {
            if (params.head != null)
                args++;

            params = params.tail;
        }

        if (exp.body != null)
            exp.body.accept(this, level - args, false);

        emitRM("LD", pc, retFO, fp, "return to caller");

        int savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitRM_ABS("LDA", pc, savedLoc2, "jump around fn body");
        emitRestore();
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        emitComment("-> compound statement");

        if (exp.decs != null)
        {
            exp.decs.accept(this, level, false);
            level = frameOffset;
        }

        if (exp.exps != null)
            exp.exps.accept(this, level, false);

        emitComment("<- compound statement");
    }

    public void visit(VarDecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null) {
                exp.head.accept(this, level, false);

                //size we are going to offset by, default is 1 for SimpleDec
                int size = 1;

                //if it is an array we need to reduce offset by its size
                if (exp.head instanceof ArrayDec)
                    size += (((ArrayDec)exp.head).size.value);

                //offset by size
                level -= size;

                //if we are at global scope, reduce global offset
                if (exp.head.nestLevel == 0)
                    globalOffset -= size;
            }

            exp = exp.tail;
        }

        frameOffset = level;
    }

    public void visit(NilExp exp, int level, boolean isAddr) {
    }

    public void visit(DecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null)
                exp.head.accept(this, level, false);
            exp = exp.tail;
        }
    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {
        exp.offset = level;

        if (exp.nestLevel == 0)
            emitComment("allocating global array: " + exp.name);
        else
            emitComment("processing local array: " + exp.name);
    }
}