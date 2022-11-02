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
import ece351.f.ast.FProgram;
import ece351.util.CommandLine;
import ece351.v.ast.*;
import ece351.v.ast.Process;
import org.parboiled.common.ImmutableList;

/**
 * Translates VHDL to F.
 */
public final class Synthesizer extends PostOrderExprVisitor {
	
	private String varPrefix;
	private int condCount;
	private static String conditionPrefix = "condition";
	
	public static void main(String[] args) { 
		System.out.println(synthesize(args));
	}
	
	public static FProgram synthesize(final String[] args) {
		return synthesize(new CommandLine(args));
	}
	
	public static FProgram synthesize(final CommandLine c) {
        final VProgram program = DeSugarer.desugar(c);
        return synthesize(program);
	}
	
	public static FProgram synthesize(final VProgram program) {
		VProgram p = Splitter.split(program);
		final Synthesizer synth = new Synthesizer();
		return synth.synthesizeit(p);
	}
	
	public Synthesizer(){
		condCount = 0;
	}
		
	private FProgram synthesizeit(final VProgram root) {
//		System.out.println(root);
		FProgram result = new FProgram();
		for( DesignUnit designUnit : root.designUnits){
			this.varPrefix = designUnit.entity.identifier;
			Architecture arch = designUnit.arch;
			for (Statement statement: arch.statements){
				if (statement instanceof Process){
					for (Statement sequentialStatements : ((Process) statement).sequentialStatements){
						result = getfProgram(result, sequentialStatements);
					}
				} else result = getfProgram(result, statement);
			}
		}
			// set varPrefix for this design unit
		return result;
	}

	private FProgram getfProgram(FProgram result, Statement statement) {
		if (statement instanceof IfElseStatement){
			result = result.appendAll(implication((IfElseStatement) statement));
		} else if (statement instanceof AssignmentStatement){
			Expr expr = traverseExpr(((AssignmentStatement) statement).expr);
			VarExpr outputVar = (VarExpr) traverseExpr(((AssignmentStatement) statement).outputVar);
			result = result.append(new AssignmentStatement(outputVar, expr));
		}
		return result;
	}

	private FProgram implication(final IfElseStatement statement) {
		// error checking
		if( statement.ifBody.size() != 1) {
			throw new IllegalArgumentException("if/else statement: " + statement + "\n can only have one assignment statement in the if-body and else-body where the output variable is the same!");
		}
		if (statement.elseBody.size() != 1) {
			throw new IllegalArgumentException("if/else statement: " + statement + "\n can only have one assignment statement in the if-body and else-body where the output variable is the same!");
		}
		final AssignmentStatement ifb = statement.ifBody.get(0);
		final AssignmentStatement elb = statement.elseBody.get(0);
		if (!ifb.outputVar.equals(elb.outputVar)) {
			throw new IllegalArgumentException("if/else statement: " + statement + "\n can only have one assignment statement in the if-body and else-body where the output variable is the same!");
		}

		// build result
		FProgram result = new FProgram();
		VarExpr condName = new VarExpr(conditionPrefix + ++condCount);
		AssignmentStatement condition = new AssignmentStatement(condName, traverseExpr(statement.condition));

		Expr expr  = new OrExpr( new AndExpr(condition.outputVar, traverseExpr(ifb.expr)), new AndExpr(new NotExpr(condition.outputVar), traverseExpr(elb.expr)));
		AssignmentStatement assignmentStatement = new AssignmentStatement((traverseExpr(ifb.outputVar)).toString(), expr);
		return new FProgram(ImmutableList.of(condition, assignmentStatement));
	}

	/** Rewrite var names with prefix to mitigate name collision. */
	@Override
	public Expr visitVar(final VarExpr e) {
		return new VarExpr(varPrefix + e.identifier);
	}
	
	@Override public Expr visitConstant(ConstantExpr e) { return e; }
	@Override public Expr visitNot(NotExpr e) { return e; }
	@Override public Expr visitAnd(AndExpr e) { return e; }
	@Override public Expr visitOr(OrExpr e) { return e; }
	@Override public Expr visitNaryAnd(NaryAndExpr e) { return e; }
	@Override public Expr visitNaryOr(NaryOrExpr e) { return e; }
	
	// We shouldn't see these in the AST, since F doesn't support them
	// They should have been desugared away previously
	@Override public Expr visitXOr(XOrExpr e) { throw new IllegalStateException("xor not desugared"); } 
	@Override public Expr visitEqual(EqualExpr e) { throw new IllegalStateException("EqualExpr not desugared"); }
	@Override public Expr visitNAnd(NAndExpr e) { throw new IllegalStateException("nand not desugared"); }
	@Override public Expr visitNOr(NOrExpr e) { throw new IllegalStateException("nor not desugared"); }
	@Override public Expr visitXNOr(XNOrExpr e) { throw new IllegalStateException("xnor not desugared"); }
	
}



