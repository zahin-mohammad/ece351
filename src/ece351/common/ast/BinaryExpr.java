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

package ece351.common.ast;

import ece351.util.Examinable;
import ece351.util.Examiner;

public abstract class BinaryExpr extends Expr {

	public final Expr left, right;
	
	public abstract BinaryExpr newBinaryExpr(final Expr left, final Expr right);
	
	public BinaryExpr(final Expr left, final Expr right){
		this.left = left;
		this.right = right;
	}
	
	public BinaryExpr varyLeft(final Expr newLeft) {
		return newBinaryExpr(newLeft, right);
	}
	
	public BinaryExpr varyRight(final Expr newRight) {
		return newBinaryExpr(left, newRight);
	}
	
	@Override
	public final boolean repOk() {
		assert left != null : "left should not be null";
		assert right != null : "right should not be null";
		assert left.repOk();
		assert right.repOk();
		return true;
	}

	@Override 
	public final String toString() {
		return String.format("(%s %s %s)", left.toString(), operator(), right.toString());
	}
	
	public final int hashCode() {
		int hash = 17;
		hash = hash * 13 + left.hashCode();
		hash = hash * 13 + right.hashCode();
		return hash;
	}
	
	/**
	 * left equals left and right equals right.
	 */
	@Override
	public final boolean equals(final Object obj) {
		return examine(Examiner.Equals, obj);
	}

	/**
	 * Subclasses may override this if order can be changed.
	 */
	@Override
	public boolean isomorphic(final Examinable obj) {
		return examine(Examiner.Isomorphic, obj);
	}

	private boolean examine(final Examiner e, final Object obj) {
		// basics
		if (obj == null) return false;
		if (!this.getClass().equals(obj.getClass())) return false;
		final BinaryExpr be = (BinaryExpr) obj;
		
		// compare field values
		if (!e.examine(left, be.left)) return false;
		if (!e.examine(right, be.right)) return false;
		
		// no differences
		return true;
	}
	
}
