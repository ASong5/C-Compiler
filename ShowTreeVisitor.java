/*
  Created by: Brendan Yawney and Andrew Song
  File Name: c-.cup
*/

import absyn.*;
public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent(int level) {
    for (int i = 0; i < level * SPACES; i++)
      System.out.print(" ");
  }

  public void visit(ExpList expList, int level, boolean isAddr) {
    while (expList != null) {
      if (expList.head != null)
        expList.head.accept(this, level, isAddr);
      expList = expList.tail;
    }
  }

  public void visit(AssignExp exp, int level, boolean isAddr) {
    indent(level);
    System.out.println("AssignExp:");
    level++;
    exp.lhs.accept(this, level, isAddr);
    exp.rhs.accept(this, level, isAddr);
  }

  public void visit(IfExp exp, int level, boolean isAddr) {
    indent(level);
    System.out.println("IfExp:");
    level++;
    exp.test.accept(this, level, isAddr);
    exp.thenpart.accept(this, level, isAddr);
    if (exp.elsepart != null)
      exp.elsepart.accept(this, level, isAddr);
  }

  public void visit(IntExp exp, int level, boolean isAddr) {
    indent(level);
    System.out.println("IntExp: " + exp.value);
  }

  public void visit(OpExp exp, int level, boolean isAddr) {
    indent(level);
    System.out.print("OpExp:");
    switch (exp.op) {
      case OpExp.PLUS:
        System.out.println(" + ");
        break;
      case OpExp.MINUS:
        System.out.println(" - ");
        break;
      case OpExp.MUL:
        System.out.println(" * ");
        break;
      case OpExp.DIV:
        System.out.println(" / ");
        break;
      case OpExp.EQ:
        System.out.println(" == ");
        break;
      case OpExp.NE:
        System.out.println(" != ");
        break;
      case OpExp.LT:
        System.out.println(" < ");
        break;
      case OpExp.LE:
        System.out.println(" <= ");
        break;
      case OpExp.GT:
        System.out.println(" > ");
        break;
      case OpExp.GE:
        System.out.println(" >= ");
        break;
      case OpExp.UMINUS:
        System.out.println(" - ");
        break;
      case OpExp.TILDE:
        System.out.println(" ~ ");
        break;
      case OpExp.OR:
        System.out.println(" || ");
        break;
      case OpExp.AND:
        System.out.println(" && ");
        break;
      default:
        System.out.println("Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
      exp.left.accept(this, level, isAddr);
    exp.right.accept(this, level, isAddr);
  }

  public void visit(VarExp exp, int level, boolean isAddr) {
    indent(level);
    System.out.println("VarExp:" );
    if (exp.variable != null)
      exp.variable.accept(this, ++level, isAddr);
  }

  public void visit(WhileExp exp, int level, boolean isAddr)  {
    indent(level);
    System.out.println("WhileExp: ");
    level++;
    if (exp.test != null)
      exp.test.accept(this, level, isAddr);
    if (exp.body != null)
      exp.body.accept(this, level, isAddr);
  }

  public void visit(ReturnExp exp, int level, boolean isAddr)  {
    indent(level);
    System.out.println("ReturnExp: ");
    level++;
    if (exp.exp != null)
      exp.exp.accept(this, level, isAddr);
  }

  public void visit(CallExp exp, int level, boolean isAddr)  {
    indent(level);
    System.out.println("CallExp: " + exp.func);
    level++;
    if (exp.args != null)
      exp.args.accept(this, level, isAddr);
  }

  public void visit(BoolExp exp, int level, boolean isAddr)  {
    indent(level);
    System.out.println("BoolExp: " + exp.value);
  }

  public void visit(SimpleVar exp, int level, boolean isAddr)  {
    indent(level);
    System.out.println("SimpleVar: " + exp.name);
  }

  public void visit(SimpleDec exp, int level, boolean isAddr)  {
    indent(level);
    switch (exp.typ.typ) {
      case NameTy.BOOL:
        System.out.println("SimpleDec: " + "bool " + exp.name);
        break;
      case NameTy.INT:
        System.out.println("SimpleDec: " + "int " + exp.name);
        break;
      case NameTy.VOID:
        System.out.println("SimpleDec: " + "void " + exp.name);
        break;
    }
  }

  public void visit(NameTy exp, int level, boolean isAddr)  {}

  public void visit(IndexVar exp, int level, boolean isAddr)  {
    indent( level );
    System.out.println("IndexVar: " + exp.name);
    exp.index.accept(this, ++level, isAddr);
  }

  public void visit(FunctionDec exp, int level, boolean isAddr)  {
    indent(level);
    switch (exp.result.typ) {
      case NameTy.BOOL:
        System.out.println("FunctionDec: " + "bool " + exp.func);
        break;
      case NameTy.INT:
        System.out.println("FunctionDec: " + "int " + exp.func);
        break;
      case NameTy.VOID:
        System.out.println("FunctionDec: " + "void " + exp.func);
        break;
    }
    level++;
    if (exp.params != null)
      exp.params.accept(this, level, isAddr);

    if (exp.body != null)
      exp.body.accept(this, level, isAddr);
  }

  public void visit(CompoundExp exp, int level, boolean isAddr)  {
    indent( level );
    System.out.println("CompoundExp: ");
    if (exp.decs != null && exp.exps != null)
      level++;
    if (exp.decs != null)
      exp.decs.accept(this, level, isAddr);
    if (exp.exps != null)
      exp.exps.accept(this, level, isAddr);
  }

  public void visit(VarDecList exp, int level, boolean isAddr)  {
    while(exp != null)
    {
      if (exp.head != null)
        exp.head.accept(this, level, isAddr);

      exp = exp.tail;
    }
  }

  public void visit(NilExp exp, int level, boolean isAddr)  {

  }

  public void visit(DecList exp, int level, boolean isAddr)  {
    while(exp != null)
    {
      if (exp.head != null)
        exp.head.accept(this, level, isAddr);

      exp = exp.tail;
    }
  }

  public void visit(ArrayDec exp, int level, boolean isAddr)  {
    indent(level);
    String type = "";
    switch (exp.typ.typ) {
      case NameTy.BOOL:
        type = "BOOL";
        break;
      case NameTy.INT:
        type = "INT";
        break;
      case NameTy.VOID:
        type = "VOID";
        break;
    }
    level++;
    if (exp.size != null)
    {
      if (exp.size.value <= 0) {
        System.out.println("ArrayDec: " + exp.name + "[]" + " - " + type);
      }
      else {
        System.out.println("ArrayDec: " + exp.name + "[" + exp.size.value + "]" + " - " + type);
      }
    }
  }
}
