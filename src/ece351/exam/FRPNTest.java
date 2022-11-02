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

import static ece351.exam.ConstantExpr.TrueExpr;
import static ece351.exam.ConstantExpr.FalseExpr;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FRPNTest {

	private static VarExpr X = new VarExpr("x");
	private static VarExpr Y = new VarExpr("y");
	
	private void test(final String rpn, final Expr full, final Expr expectedSimplified) {
		final Expr rpnSimplified = FRPN.parse(rpn.split(" "));
		final Expr visitorSimplified = SimplifyVisitor.doit(full);
		assertEquals(rpnSimplified, expectedSimplified);		
		assertEquals(visitorSimplified, expectedSimplified);		
		assertEquals(rpnSimplified, visitorSimplified);
	}


	@Test
	public void testBasicConstruction() {
		final BinaryExpr orXY = new OrExpr(X, Y);
		final BinaryExpr andXY = new AndExpr(X, Y);
		final NotExpr notX = new NotExpr(X);
		test("x y or", orXY, orXY);
		test("x y and", andXY, andXY);
		test("x not", notX, notX);
	}

	@Test
	public void testConstants() {
		test("1 0 or", new OrExpr(TrueExpr, FalseExpr), TrueExpr);
		test("1 0 and", new AndExpr(TrueExpr, FalseExpr), FalseExpr);
		test("1 not", new NotExpr(TrueExpr), FalseExpr);
		test("0 not", new NotExpr(FalseExpr), TrueExpr);
	}

	@Test
	public void testDoubleNegation() {
		test("x not not", new NotExpr(new NotExpr(X)), X);
	}

	@Test
	public void testDeduplication() {
		test("x x and", new AndExpr(X, X), X);
		test("x x or", new OrExpr(X, X), X);
	}

	@Test
	public void testIdentityElement() {
		test("x 0 or", new OrExpr(X, FalseExpr), X);
		test("x 1 and", new AndExpr(X, TrueExpr), X);
	}

	@Test
	public void testAbsorbingElement() {
		test("x 1 or", new OrExpr(X, TrueExpr), TrueExpr);
		test("x 0 and", new AndExpr(X, FalseExpr), FalseExpr);
	}

	@Test
	public void testBigger() {
		test("x 0 or x 1 and or not not", 
				new NotExpr(new NotExpr(new OrExpr(new AndExpr(X, TrueExpr), new OrExpr(X, FalseExpr)))), 
				X);
	}
	
}
