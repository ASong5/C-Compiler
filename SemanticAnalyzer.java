import absyn.*;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer implements AbsynVisitor {
    final static int SPACES = 4;
    public static boolean valid = true;
    HashMap<String, ArrayList<NodeType>> table = new HashMap<>();

    public SemanticAnalyzer() {
        FunctionDec inputDec = new FunctionDec(-1, -1, new NameTy(-1, -1, NameTy.INT), "input", null, null);
        FunctionDec outputDec = new FunctionDec(-1, -1, new NameTy(-1, -1, NameTy.VOID), "output",
                new VarDecList(new SimpleDec(-1, -1, new NameTy(-1, -1, NameTy.INT), null), null), null);

        inputDec.funaddr = 4;
        outputDec.funaddr = 7;

        ArrayList<NodeType> a_list_input = new ArrayList<NodeType>();
        ArrayList<NodeType> a_list_output = new ArrayList<NodeType>();

        a_list_input.add(new NodeType(inputDec.func, inputDec, -1));
        a_list_output.add(new NodeType(outputDec.func, outputDec, -1));

        this.table.put(inputDec.func, a_list_input);
        this.table.put(outputDec.func, a_list_output);
    }

    private void indent(int level) {
        for (int i = 0; i < level * SPACES; i++)
            System.out.print(" ");
    }

    private void insert(Dec dec, String name, int level) {
        ArrayList<NodeType> new_defn = new ArrayList<>();
        new_defn.add(new NodeType(name, dec, level));
        table.put(name, new_defn);
    }

    private NodeType lookup(String name) {
        NodeType node = null;

        if (table.containsKey(name)) {
            ArrayList<NodeType> list = table.get(name);
            if (list.isEmpty())
                return node;
            node = list.get(list.size() - 1);
        }

        return node;
    }

    private String typeString(int type) {
        String type_string = "NULL";

        if (type == NameTy.BOOL)
            type_string = "BOOL";

        else if (type == NameTy.INT)
            type_string = "INT";

        else if (type == NameTy.VOID)
            type_string = "VOID";

        return type_string;
    }

    private void printError(int r, int c, String message) {
        valid = false;

        int row = r + 1;
        int col = c + 1;

        System.err.println("Error in line " + row + ", " + "column " + col + " : " + message);
    }

    public void printScope(int level) {
        for (String key : table.keySet()) {
            ArrayList<NodeType> defn = table.get(key);
            if (defn != null && !defn.isEmpty()) {
                // System.out.println("key: " + key + " level: " + defn.get(0).level);
                if (defn.get(0).level == level) {
                    indent(level + 1);
                    Dec declaration = defn.get(0).def;
                    if (declaration instanceof SimpleDec) {
                        if (((SimpleDec) (declaration)).typ.typ == NameTy.INT)
                            System.out.printf("%s: INT%n", ((SimpleDec) (declaration)).name);
                        else if (((SimpleDec) (declaration)).typ.typ == NameTy.BOOL)
                            System.out.printf("%s: BOOL%n", ((SimpleDec) (declaration)).name);
                        else
                            System.out.printf("%s: VOID%n", ((SimpleDec) (declaration)).name);
                    } else if (declaration instanceof ArrayDec) {
                        if (((ArrayDec) (declaration)).typ.typ == NameTy.INT)
                            System.out.printf("%s: INT[]%n", ((ArrayDec) (declaration)).name);
                        else if (((ArrayDec) (declaration)).typ.typ == NameTy.BOOL)
                            System.out.printf("%s: BOOL%n", ((ArrayDec) (declaration)).name);
                        else
                            System.out.printf("%s: VOID[]%n", ((ArrayDec) (declaration)).name);
                    } else if (declaration instanceof FunctionDec) {
                        System.out.printf("%s: (", ((FunctionDec) (declaration)).func);
                        while ((((FunctionDec) (declaration)).params != null)
                                && (((FunctionDec) (declaration)).params.tail != null)) {
                            if (((FunctionDec) (declaration)).params.head instanceof ArrayDec) {
                                ArrayDec arrayDec = (ArrayDec) (((FunctionDec) (declaration)).params.head);
                                System.out.printf("%s", arrayDec.typ.typ == NameTy.INT ? "INT[]"
                                        : arrayDec.typ.typ == NameTy.VOID ? "VOID[]" : "BOOL[]");

                                if (((FunctionDec) (declaration)).params.tail != null)
                                    System.out.print(", ");

                            } else {
                                System.out.printf("%s",
                                        ((SimpleDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.INT
                                                ? "INT"
                                                : ((SimpleDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.VOID
                                                ? "VOID"
                                                : "BOOL");
                                if (((FunctionDec) (declaration)).params.tail != null)
                                    System.out.print(", ");
                            }
                            ((FunctionDec) (declaration)).params = ((FunctionDec) (declaration)).params.tail;
                        }
                        if ((((FunctionDec) (declaration)).params != null)
                                && (((FunctionDec) (declaration)).params.head != null)) {
                            if (((FunctionDec) (declaration)).params.head instanceof SimpleDec) {
                                System.out.printf("%s",
                                        ((SimpleDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.INT
                                                ? "INT"
                                                : ((SimpleDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.VOID
                                                ? "VOID"
                                                : "BOOL");
                            } else {
                                System.out.printf("%s",
                                        ((ArrayDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.INT
                                                ? "INT[]"
                                                : ((ArrayDec) ((FunctionDec) (declaration)).params.head).typ.typ == NameTy.INT
                                                ? "VOID[]"
                                                : "BOOL[]");
                            }
                        }
                        System.out.printf(") -> %s%n",
                                ((FunctionDec) (declaration)).result.typ == NameTy.INT ? "INT"
                                        : ((FunctionDec) (declaration)).result.typ == NameTy.VOID ? "VOID" : "BOOL");
                    }
                }
            }
        }
    }

    private int deleteScope(int level) {
        int num_deleted = 0;

        for (Map.Entry<String, ArrayList<NodeType>> hash_element : table.entrySet()) {
            ArrayList<NodeType> defn = table.get(hash_element.getKey());

            ListIterator<NodeType> list_iter = defn.listIterator();

            while (list_iter.hasNext()) {
                if (list_iter.next().level == level) {
                    list_iter.remove();
                    num_deleted++;
                }
            }
        }

        return num_deleted;
    }

    private boolean isBool(Dec dtype) {
        if (dtype instanceof SimpleDec) {
            SimpleDec sDec = (SimpleDec) dtype;
            return sDec.typ.typ == NameTy.BOOL;
        } else if (dtype instanceof ArrayDec) {
            ArrayDec aDec = (ArrayDec) dtype;
            return aDec.typ.typ == NameTy.BOOL;
        } else if (dtype instanceof FunctionDec) {
            FunctionDec fDec = (FunctionDec) dtype;
            return fDec.result.typ == NameTy.BOOL;
        }
        return false;
    }

    private boolean isInteger(Dec dtype) {
        if (dtype instanceof SimpleDec) {
            SimpleDec sDec = (SimpleDec) dtype;
            return sDec.typ.typ == NameTy.INT;
        } else if (dtype instanceof ArrayDec) {
            ArrayDec aDec = (ArrayDec) dtype;
            return aDec.typ.typ == NameTy.INT;
        } else if (dtype instanceof FunctionDec) {
            FunctionDec fDec = (FunctionDec) dtype;
            return fDec.result.typ == NameTy.INT;
        }

        return false;
    }

    private boolean isVoid(Dec dtype) {
        if (dtype instanceof SimpleDec) {
            SimpleDec sDec = (SimpleDec) dtype;
            return sDec.typ.typ == NameTy.VOID;
        } else if (dtype instanceof ArrayDec) {
            ArrayDec aDec = (ArrayDec) dtype;
            return aDec.typ.typ == NameTy.VOID;
        } else if (dtype instanceof FunctionDec) {
            FunctionDec fDec = (FunctionDec) dtype;
            return fDec.result.typ == NameTy.VOID;
        }
        return false;
    }

    private int getExpType(Exp exp) {
        int type = -1;

        Dec dtype = exp.dtype;

        if (exp instanceof IntExp)
            type = NameTy.INT;
        else if (exp instanceof ReturnExp) {
            ReturnExp r_exp = (ReturnExp) exp;
            type = getExpType(r_exp.exp);
        } else if (exp instanceof BoolExp)
            type = NameTy.BOOL;
        else if (isBool(dtype))
            type = NameTy.BOOL;
        else if (isInteger(dtype))
            type = NameTy.INT;
        else if (isVoid(dtype))
            type = NameTy.VOID;
        else if (exp instanceof OpExp) {
            if (((OpExp) (exp)).op > 3 && ((OpExp) (exp)).op < 13)
                type = NameTy.BOOL;
        } else if (exp instanceof CallExp) {
            if (table.get(((CallExp) (exp)).func).isEmpty() || table.get(((CallExp) (exp)).func) == null)
                return type;
            Dec dec = table.get(((CallExp) (exp)).func).get(0).def;
            if (dec instanceof FunctionDec) {
                FunctionDec f_dec = (FunctionDec) dec;
                type = f_dec.result.typ;
            }
        }

        return type;
    }

    private int getOpExpType(OpExp o_exp) {
        int o_type = -1;
        int l_type = -1;
        int r_type = -1;

        if (o_exp.op >= 4 && o_exp.op <= 12) {
            o_type = NameTy.BOOL;
        } else {
            if (o_exp.left instanceof OpExp)
                l_type = getOpExpType((OpExp) (o_exp.left));
            else
                l_type = getExpType(o_exp.left);

            if (o_exp.right instanceof OpExp)
                r_type = getOpExpType((OpExp) (o_exp.right));
            else
                r_type = getExpType(o_exp.right);

            if (l_type == r_type)
                o_type = l_type;
        }
        return o_type;
    }

    private int getFuncReturnType(CompoundExp body) {
        int type = 2;

        ExpList tempExps = body.exps;
        while (tempExps != null) {
            if (tempExps.head instanceof IfExp) {
                if (((IfExp) tempExps.head).thenpart instanceof ReturnExp) {
                    if (((ReturnExp) ((IfExp) tempExps.head).thenpart).exp instanceof OpExp) {
                        type = getOpExpType((OpExp) ((ReturnExp) ((IfExp) tempExps.head).thenpart).exp);
                    } else {
                        type = getExpType(((IfExp) tempExps.head).thenpart);
                    }
                }
            } else if (tempExps.head instanceof CompoundExp) {
                type = getFuncReturnType((CompoundExp) tempExps.head);
            } else if (tempExps.head instanceof ReturnExp) {
                if (((ReturnExp) (tempExps.head)).exp instanceof OpExp) {
                    type = getOpExpType(((OpExp) ((ReturnExp) (tempExps.head)).exp));
                } else {
                    type = getExpType(((ReturnExp) tempExps.head).exp);

                }
            }
            tempExps = tempExps.tail;
        }

        return type;
    }

    public void visit(ExpList expList, int level, boolean isAddr) {
        while (expList != null) {
            if (expList.head != null)
                expList.head.accept(this, level, isAddr);
            expList = expList.tail;
        }
    }

    public void visit(AssignExp exp, int level, boolean isAddr) {
        exp.lhs.accept(this, level, isAddr);
        exp.rhs.accept(this, level, isAddr);

        int l_type = -1;
        int r_type = -1;

        if (exp.lhs instanceof SimpleVar) {
            SimpleVar s_var = (SimpleVar) exp.lhs;

            ArrayList<NodeType> defn = table.get(s_var.name);

            if (defn != null && !defn.isEmpty()) {
                Dec local_dec = defn.size() > 0 ? defn.get(defn.size() - 1).def : defn.get(0).def;
                if (local_dec instanceof SimpleDec) {
                    SimpleDec s_dec = (SimpleDec) local_dec;
                    l_type = s_dec.typ.typ;
                }
            }
        } else {
            IndexVar i_var = (IndexVar) exp.lhs;

            ArrayList<NodeType> defn = table.get(i_var.name);

            if (defn != null && !defn.isEmpty()) {
                if (defn.get(0).def instanceof ArrayDec) {
                    ArrayDec a_dec = (ArrayDec) defn.get(0).def;
                    l_type = a_dec.typ.typ;
                } else if (defn.get(0).def instanceof SimpleDec) {
                    SimpleDec a_dec = (SimpleDec) defn.get(0).def;
                    l_type = a_dec.typ.typ;
                }
            }
        }

        if (exp.rhs instanceof OpExp)
            r_type = getOpExpType((OpExp) exp.rhs);
        else if (exp.rhs instanceof CallExp) {
            CallExp c_exp = (CallExp) exp.rhs;
            if (exp.rhs.dtype == null)
                return;
            NodeType node = lookup(c_exp.func);
            if (node != null) {
                FunctionDec f_dec = (FunctionDec) lookup(c_exp.func).def;
                r_type = f_dec.result.typ;
            }
        } else
            r_type = getExpType(exp.rhs);

        if (l_type != r_type) {
            String m = "cannot assign " + typeString(r_type) + " to " + typeString(l_type);
            printError(exp.row, exp.col, m);
        }
    }

    public void visit(IfExp exp, int level, boolean isAddr) {
        indent(level);
        System.out.println("Entering a new block:");
        exp.test.accept(this, level, isAddr);
        exp.thenpart.accept(this, level, isAddr);

        if (exp.elsepart != null)
            exp.elsepart.accept(this, level, isAddr);

        if (getExpType(exp.test) != NameTy.BOOL) {
            String m = " test condition must evaluate to type BOOL";
            printError(exp.row, exp.col, m);
        }

        printScope(level);
        deleteScope(level);

        indent(--level);
        System.out.println("Leaving the block");
    }

    public void visit(IntExp exp, int level, boolean isAddr) {
    }

    public void visit(OpExp exp, int level, boolean isAddr) {
        if (exp.left != null)
            exp.left.accept(this, level, isAddr);
        exp.right.accept(this, level, isAddr);

        int l_type = -1;
        int r_type = -1;

        if (exp.right instanceof BoolExp)
            if (((BoolExp) exp.right).value)
                r_type = 1;
            else
                r_type = 0;

        if (l_type != r_type) {
            String m = "cannot assign " + typeString(r_type) + " to " + typeString(l_type);
            printError(exp.row, exp.col, m);
        }
    }

    public void visit(VarExp exp, int level, boolean isAddr) {
        if (exp.variable != null)
            exp.variable.accept(this, level, isAddr);

        NodeType node = null;

        if (exp.variable instanceof SimpleVar)
            node = lookup(((SimpleVar) exp.variable).name);
        if (exp.variable instanceof IndexVar)
            node = lookup(((IndexVar) exp.variable).name);

        if (node != null)
            exp.dtype = node.def;
    }

    public void visit(WhileExp exp, int level, boolean isAddr) {
        indent(level++);
        System.out.println("Entering a new block");
        if (exp.test != null)
            exp.test.accept(this, level, isAddr);
        if (exp.body != null)
            exp.body.accept(this, level, isAddr);
        if (exp.test == null) {
            String m = "test condition within if statement can not be empty";
            printError(exp.row, exp.col, m);
        } else if (getExpType(exp.test) != NameTy.BOOL) {
            String m = "test condition must evaluate to type BOOL";
            printError(exp.row, exp.col, m);
        }

        printScope(level);
        deleteScope(level);

        indent(--level);
        System.out.println("Leaving the block");
    }

    public void visit(ReturnExp exp, int level, boolean isAddr) {
        if (exp.exp != null)
            exp.exp.accept(this, level, isAddr);
    }

    public void visit(CallExp exp, int level, boolean isAddr) {
        if (exp.args != null)
            exp.args.accept(this, level, isAddr);

        ArrayList<NodeType> a_list = table.get(exp.func);

        if (a_list == null) {
            String m = "function " + exp.func + " could not be resolved";
            printError(exp.row, exp.col, m);
        } else {
            int param_type = -1;
            int arg_type = -1;
            if (a_list.isEmpty()) {
                String m = "function " + exp.func + " is undefined";
                printError(exp.row, exp.col, m);
            } else if (a_list.get(0).def instanceof FunctionDec) {
                FunctionDec f_dec = (FunctionDec) a_list.get(0).def;
                exp.dtype = f_dec;

                VarDecList tempParams = f_dec.params;
                ExpList tempArgs = exp.args;
                while (tempParams != null) {
                    if (tempParams.head == null)
                        break;
                    if (tempArgs == null) {
                        String m = "argument list should not be empty for function " + exp.func;
                        printError(exp.row, exp.col, m);
                        break;
                    }

                    if (tempParams.head instanceof SimpleDec)
                        param_type = ((SimpleDec) tempParams.head).typ.typ;

                    else if (tempParams.head instanceof ArrayDec)
                        param_type = ((ArrayDec) tempParams.head).typ.typ;

                    if (tempArgs.head instanceof OpExp)
                        arg_type = getOpExpType((OpExp) tempArgs.head);

                    else
                        arg_type = getExpType(tempArgs.head);

                    if (param_type != arg_type) {
                        String m = "type " + typeString(arg_type) + " does not match type: " + typeString(param_type);
                        printError(exp.row, exp.col, m);
                        break;
                    }

                    tempArgs = tempArgs.tail;
                    tempParams = tempParams.tail;
                }

                if (exp.args != null && f_dec.params == null) {
                    String m = "argument list exceeds number of expected arguments";
                    printError(exp.row, exp.col, m);
                }
            }
        }
    }

    public void visit(BoolExp exp, int level, boolean isAddr) {
    }

    public void visit(SimpleVar exp, int level, boolean isAddr) {
        ArrayList<NodeType> defn = table.get(exp.name);
        exp.dec = (SimpleDec)defn.get(0).def;
    }

    public void visit(SimpleDec exp, int level, boolean isAddr) {
        ArrayList<NodeType> defn = table.get(exp.name);
        if (defn != null && !defn.isEmpty()) {
            if (defn.get(0).level == level) {
                String m = "variable " + exp.name + " has already been declared in this scope";
                printError(exp.row, exp.col, m);
            } else {
                defn.add(new NodeType(exp.name, exp, level));
                table.put(exp.name, defn);
            }
        }
        else
            insert(exp, exp.name, level);

        if (level > 0)
            exp.nestLevel = 1;
        else
            exp.nestLevel = 0;
    }

    public void visit(NameTy exp, int level, boolean isAddr) {
    }

    public void visit(IndexVar exp, int level, boolean isAddr) {
        exp.index.accept(this, level, isAddr);

        ArrayList<NodeType> defn = table.get(exp.name);
        if (defn != null) {
            if (!defn.isEmpty()) {
                if (!(defn.get(0).def instanceof ArrayDec)) {
                    SimpleDec a_dec = (SimpleDec) defn.get(0).def;
                    if (a_dec.typ.typ == NameTy.INT) {
                        String m = "variable " + exp.name + " of type INT cannot be indexed like an array";
                        printError(exp.row, exp.col, m);
                    } else {
                        String m = "variable " + exp.name + " of type BOOL cannot be indexed like an array";
                        printError(exp.row, exp.col, m);
                    }
                }
                if (getExpType(exp.index) != NameTy.INT) {
                    String m = "variable " + exp.name + " must have an integer size as its index";
                    printError(exp.row, exp.col, m);
                }
            }
        }
    }

    public void visit(FunctionDec exp, int level, boolean isAddr) {
        ArrayList<NodeType> defn = table.get(exp.func);

        if (defn != null && !defn.isEmpty()) {
            if (defn.get(0).level == level) {
                String m = "redeclaration error for function " + exp.func;
                printError(exp.row, exp.col, m);
            } else {
                defn.add(new NodeType(exp.func, exp, level));
                table.put(exp.func, defn);
            }
        } else
            insert(exp, exp.func, level);

        indent(++level);
        System.out.println("Entering the scope for function " + exp.func + ":");

        if (exp.params != null)
            exp.params.accept(this, level, isAddr);

        int type = 2;

        if (exp.body != null) {
            exp.body.accept(this, level, isAddr);

            type = getFuncReturnType(exp.body);
        }

        if (type != exp.result.typ) {
            if (!(type == -1 && exp.result.typ == 2)) {
                String m = "function " + exp.func + " should return a value of type " +
                        typeString(exp.result.typ) + ", instead returns " + typeString(type);
                printError(exp.row, exp.col, m);
            }
        }

        printScope(level);
        deleteScope(level);

        indent(level--);
        System.out.println("Leaving the function scope");
    }

    public void visit(CompoundExp exp, int level, boolean isAddr) {
        if (exp.decs != null)
            exp.decs.accept(this, level, isAddr);
        level++;
        if (exp.exps != null)
            exp.exps.accept(this, level, isAddr);
    }

    public void visit(VarDecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null)
                exp.head.accept(this, level, isAddr);
            exp = exp.tail;
        }
    }

    public void visit(NilExp exp, int level, boolean isAddr) {
    }

    public void visit(DecList exp, int level, boolean isAddr) {
        while (exp != null) {
            if (exp.head != null)
                exp.head.accept(this, level, isAddr);
            exp = exp.tail;
        }
    }

    public void visit(ArrayDec exp, int level, boolean isAddr) {
        ArrayList<NodeType> defn = table.get(exp.name);
        if (exp.typ.typ == NameTy.VOID) {
            String m = "array declaration of variable " + exp.name
                    + " is not semantically meaningful because it is of type VOID";
            printError(exp.row, exp.col, m);
        }
        if (defn != null && !defn.isEmpty()) {
            if (defn.get(0).level == level) {
                String m = "redeclaration error for " + exp.name;
                printError(exp.row, exp.col, m);
            } else {
                defn.add(new NodeType(exp.name, exp, level));
                table.put(exp.name, defn);
            }

        }
        else
            insert(exp, exp.name, level);

        if (level > 0)
            exp.nestLevel = 1;
        else
            exp.nestLevel = 0;
    }
}