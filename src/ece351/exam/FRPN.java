/* *********************************************************************
 * ECE351 
 * Department of Electrical and Computer Engineering 
 * University of Waterloo 
 * Term: Spring 2019 (1195)
 *
 * The base version of this file is the intellectual property of the
 * University of Waterloo. Redistribution is prohibited.
 *
 * By pushing changes to this file I affirm that I am the author of
 * all changes. I affirm that I have complied with the course
 * collaboration policy and have not plagiarized my work. 
 *
 * I understand that redistributing this file might expose me to
 * disciplinary action under UW Policy 71. I understand that Policy 71
 * allows for retroactive modification of my final grade in a course.
 * For example, if I post my solutions to these labs on GitHub after I
 * finish ECE351, and a future student plagiarizes them, then I too
 * could be found guilty of plagiarism. Consequently, my final grade
 * in ECE351 could be retroactively lowered. This might require that I
 * repeat ECE351, which in turn might delay my graduation.
 *
 * https://uwaterloo.ca/secretariat-general-counsel/policies-procedures-guidelines/policy-71
 * 
 * ********************************************************************/

package ece351.exam;

import java.util.Arrays;
import java.util.Stack;


/** A parser with integrated simplifier for an F-like language using RPN. */
public final class FRPN {

    public final static void main(final String[] args) {
        final Expr result = parse(args);

        // print outputs
        System.out.println("rpn:              " + Arrays.toString(args));
        System.out.println("simplified infix: " + result.toString());
        System.out.println("---");
    }
    
    public final static Expr parse(final String[] args) {
        // our stack for evaluating RPN
        final Stack<Expr> stack = new Stack<Expr>();
        // parse Boolean expressions in RPN off the command line
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];
            if (a.equals("1")) {
                stack.push(ConstantExpr.TrueExpr);
            } else if (a.equals("0")) {
                stack.push(ConstantExpr.FalseExpr);
            } else if (a.equals("and")) {
                final Expr right = stack.pop();
                final Expr left = stack.pop();
                final AndExpr e = new AndExpr(left, right);
                stack.push( e.simplify() );
            } else if (a.equals("or")) {
                final Expr right = stack.pop();
                final Expr left = stack.pop();
                final OrExpr e = new OrExpr(left, right);
                stack.push( e.simplify() );
            } else if (a.equals("not")) {
                final NotExpr e = new NotExpr(stack.pop());
                stack.push( e.simplify() );
            } else {
                stack.push(new VarExpr(a));
            }
        }

        // the result of the parse is the expr on the top of the stack
        // just like Parboiled
        return stack.pop();
    }
    
    
}

abstract class Expr {
    abstract Expr simplify();
    abstract Expr accept(PostOrderVisitor v);
}
abstract class BinaryExpr extends Expr {
    final Expr left;
    final Expr right;
    BinaryExpr(Expr l, Expr r) {left = l; right = r;}
    public String toString() { return "(" + left + operator() + right + ")";}
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (getClass().equals(obj.getClass())) {
            final BinaryExpr b = (BinaryExpr) obj;
            return left.equals(b.left) && right.equals(b.right);
        } else return false;
    }
    abstract String operator();
    abstract ConstantExpr getAbsorbingElement();
    abstract ConstantExpr getIdentityElement();
    abstract BinaryExpr newBinaryExpr(Expr l2, Expr r2);
    Expr simplify() {
        // are both children the same?
        if (left.equals(right)) return left.simplify();
        // is one of the children either absorbing or identity?
        if (left.equals(getAbsorbingElement())) return getAbsorbingElement();
        if (left.equals(getIdentityElement())) return right.simplify();
        if (right.equals(getAbsorbingElement())) return getAbsorbingElement();
        if (right.equals(getIdentityElement())) return left.simplify();
        return this;
    }
}
final class AndExpr extends BinaryExpr {
    AndExpr(Expr l, Expr r) {super(l,r);}
    String operator() { return " and "; }
    ConstantExpr getAbsorbingElement() {return ConstantExpr.FalseExpr;}
    ConstantExpr getIdentityElement() {return ConstantExpr.TrueExpr;}
    BinaryExpr newBinaryExpr(Expr l2, Expr r2) {return new AndExpr(l2, r2);}
    Expr accept(PostOrderVisitor v) {return v.visitAnd(this); }
}
final class OrExpr extends BinaryExpr {
    OrExpr(Expr l, Expr r) {super(l,r);}
    String operator() { return " or "; }
    ConstantExpr getAbsorbingElement() {return ConstantExpr.TrueExpr;}
    ConstantExpr getIdentityElement() {return ConstantExpr.FalseExpr;}
    BinaryExpr newBinaryExpr(Expr l2, Expr r2) {return new OrExpr(l2, r2);}
    Expr accept(PostOrderVisitor v) {return v.visitOr(this); }
}
final class NotExpr extends Expr {
    final Expr e;
    NotExpr(Expr e) {this.e = e;}
    public String toString() { return "(not " + e.toString() + ")";}
    Expr accept(PostOrderVisitor v) {return v.visitNot(this); }
    Expr simplify() {
        if (e instanceof NotExpr) {
            // eliminate double negative
            final NotExpr oppositionalChild = (NotExpr)e;
            return oppositionalChild.e.simplify();
        } else if (e instanceof ConstantExpr) {
            // constant folding
            final ConstantExpr c = (ConstantExpr)e;
            return c.opposite();
        } else return this;
    }
    public boolean equals(final Object obj) {
        if (obj instanceof NotExpr) {
            final NotExpr n = (NotExpr) obj;
            return e.equals(n.e);
        } else return false;
    }
}
final class ConstantExpr extends Expr {
    static ConstantExpr FalseExpr = new ConstantExpr(false);
    static ConstantExpr TrueExpr = new ConstantExpr(true);
    final Boolean val;
    ConstantExpr(final Boolean val) {this.val = val;}
    public String toString() {return val.toString();}
    public boolean equals(final Object obj) { 
        if (obj instanceof ConstantExpr) {
            final ConstantExpr c = (ConstantExpr) obj;
            return val.equals(c.val);
        } else return false;
    }
    ConstantExpr opposite() { return val ? FalseExpr : TrueExpr; }
    Expr simplify() {return this;}
    Expr accept(PostOrderVisitor v) {return v.visitConstant(this); }
}
final class VarExpr extends Expr {
    final String var;
    VarExpr(final String var) {this.var = var;}
    public String toString() {return var;}
    Expr simplify() {return this;}
    public boolean equals(final Object obj) { 
        if (obj instanceof VarExpr) {
            final VarExpr v = (VarExpr) obj;
            return var.equals(v.var);
        } else return false;
    }
    Expr accept(PostOrderVisitor v) {return v.visitVar(this); }
}
abstract class PostOrderVisitor {
    abstract Expr visitConstant(ConstantExpr e);
    abstract Expr visitVar(VarExpr e);
    abstract Expr visitAnd(AndExpr e);
    abstract Expr visitOr(OrExpr e);
    abstract Expr visitNot(NotExpr e);
    
    Expr traverseExpr(final Expr e) {
        if (e instanceof BinaryExpr) {
            final BinaryExpr b = (BinaryExpr)e;
            final Expr left2 = traverseExpr(b.left);
            final Expr right2 = traverseExpr(b.right);
            final BinaryExpr result = b.newBinaryExpr(left2, right2);
            return result.accept(this);
        } else if (e instanceof NotExpr) {
            final NotExpr n = (NotExpr)e;
            final Expr child2 = traverseExpr(n.e);
            final Expr result = child2.equals(n.e) ? n.e : new NotExpr(child2);
            return result.accept(this);
        } else {
            return e.accept(this);
        }
    }
}
class SimplifyVisitor extends PostOrderVisitor {
    static Expr doit(final Expr e) {
        final SimplifyVisitor s = new SimplifyVisitor();
        return s.traverseExpr(e);
    }
    Expr visitConstant(final ConstantExpr e) { return e; }
    Expr visitVar(final VarExpr e) { return e; }
    Expr visitAnd(final AndExpr e) { return visitBinaryExpr(e); }
    Expr visitOr(OrExpr e) { return visitBinaryExpr(e); }
    private Expr visitBinaryExpr(final BinaryExpr e) {
        if (e.left.equals(e.right)) { return e.right; }
        else if (e.left.equals(e.getAbsorbingElement())) { return e.getAbsorbingElement(); }
        else if (e.left.equals(e.getIdentityElement())) { return e.right; }
        else if (e.right.equals(e.getAbsorbingElement())) { return e.getAbsorbingElement(); }
        else if (e.right.equals(e.getIdentityElement())) { return e.left; }
        else { return e; }
    }
    Expr visitNot(final NotExpr n) {
        if (n.e instanceof NotExpr) {
            // eliminate double negative
            final NotExpr oppositionalChild = (NotExpr)n.e;
            return oppositionalChild.e.simplify();
        } else if (n.e instanceof ConstantExpr) {
            // constant folding
            final ConstantExpr c = (ConstantExpr)n.e;
            return c.opposite();
        } else return n;
    }
}
