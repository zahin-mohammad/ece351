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

package ece351.v;

import java.util.LinkedHashSet;
import java.util.Set;

import org.parboiled.common.FileUtils;
import org.parboiled.common.ImmutableList;

import ece351.common.ast.AndExpr;
import ece351.common.ast.AssignmentStatement;
import ece351.common.ast.ConstantExpr;
import ece351.common.ast.EqualExpr;
import ece351.common.ast.Expr;
import ece351.common.ast.NAndExpr;
import ece351.common.ast.NOrExpr;
import ece351.common.ast.NaryAndExpr;
import ece351.common.ast.NaryOrExpr;
import ece351.common.ast.NotExpr;
import ece351.common.ast.OrExpr;
import ece351.common.ast.Statement;
import ece351.common.ast.VarExpr;
import ece351.common.ast.XNOrExpr;
import ece351.common.ast.XOrExpr;
import ece351.common.visitor.PostOrderExprVisitor;
import ece351.util.CommandLine;
import ece351.v.ast.Component;
import ece351.v.ast.DesignUnit;
import ece351.v.ast.IfElseStatement;
import ece351.v.ast.Process;
import ece351.v.ast.VProgram;

/**
 * Checks that variables are defined before they are used in a VProgram.
 */
public final class DefBeforeUseChecker extends PostOrderExprVisitor {
	
	private final Set<String> outputPins = new LinkedHashSet<String>();
	private final Set<String> inputPins = new LinkedHashSet<String>();
	private final Set<String> signals = new LinkedHashSet<String>();
	private final Set<String> varsInExpr = new LinkedHashSet<String>();
	
	private DefBeforeUseChecker() {
		super();
	}
	
	public static void main(final String arg) {
		main(new String[]{arg});
	}

	/**
	 * Throws a RuntimeException if there's a problem.
	 * Otherwise returns silently.
	 * @param args
	 */
	public static void main(final String[] args) {
		checkUse(VParser.parse(FileUtils.readAllText(args[0])));
	}
	
	public static void checkUse(final String[] args) {
		checkUse(new CommandLine(args));
	}
	
	public static void checkUse(final CommandLine c) {
		checkUse(VParser.parse(c.readInputSpec()));
	}
	
	public static void checkUse(final VProgram program) {
		final DefBeforeUseChecker useChecker = new DefBeforeUseChecker();
		useChecker.check(program);
	}
	
	private void check(final VProgram program) {
		// TODO: 
		// For each design unit, update the sets that will be used to determine if variables are used before they are defined
		// Upon the first occurrence of the use of an undefined variable, throw an exception
			// for each statement in the architecture, perform checks on instances of AssignmentStatement
		// TODO: Check all signals in sensitivity list in process, p
		// TODO: Check all assignment statements in the process, p
		// TODO: check all variables used in expression
		// Obtain all variables used in expression
		// Check to see if all variables that are used in the expression are defined
		// TODO: check all variables used in the assignment statement
		// Ensure that the output variable is not an input pin
		// Check the right hand side of the statement
// TODO: longer code snippet
throw new ece351.util.Todo351Exception();
	}
	
	private void updateDefinedVars(DesignUnit d) {
		this.inputPins.clear();
		this.outputPins.clear();
		this.signals.clear();
		for(String pin : d.entity.input) {
			this.inputPins.add(pin);
		}
		for(String pin : d.entity.output) {
			this.outputPins.add(pin);
		}
		for(String signal : d.arch.signals) {
			this.signals.add(signal);
		}
	}

	@Override
	public Expr visitEqual(EqualExpr equalVExpr) {
		return equalVExpr;
	}

	@Override
	public Expr visitVar(VarExpr e) {
// TODO: short code snippet
//throw new ece351.util.Todo351Exception();
		return e;
	}

	@Override
	public Expr visitNot(NotExpr e) {
		return e;
	}

	@Override
	public Expr visitAnd(AndExpr e) {
		return e;
	}

	@Override
	public Expr visitOr(OrExpr e) {
		return e;
	}

	@Override
	public Expr visitXOr(XOrExpr e) {
		return e;
	}

	@Override
	public Expr visitNAnd(NAndExpr e) {
		return e;
	}

	@Override
	public Expr visitNOr(NOrExpr e) {
		return e;
	}

	@Override
	public Expr visitXNOr(XNOrExpr e) {
		return e;
	}

	@Override
	public Expr visitConstant(ConstantExpr constantVExpr) {
		return constantVExpr;
	}
	
	@Override public Expr visitNaryAnd(NaryAndExpr e) { return e; }
	@Override public Expr visitNaryOr(NaryOrExpr e) { return e; }
}
