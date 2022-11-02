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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

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
import ece351.v.ast.Architecture;
import ece351.v.ast.DesignUnit;
import ece351.v.ast.IfElseStatement;
import ece351.v.ast.Process;
import ece351.v.ast.VProgram;


/**
 * Process splitter.
 */
public final class Splitter extends PostOrderExprVisitor {
	private final Set<String> usedVarsInExpr = new LinkedHashSet<String>();

	public static void main(String[] args) {
		System.out.println(split(args));
	}
	
	public static VProgram split(final String[] args) {
		return split(new CommandLine(args));
	}
	
	public static VProgram split(final CommandLine c) {
		final VProgram program = DeSugarer.desugar(c);
        return split(program);
	}
	
	public static VProgram split(final VProgram program) {
		VProgram p = Elaborator.elaborate(program);
		final Splitter s = new Splitter();
		return s.splitit(p);
	}

	private VProgram splitit(final VProgram program) {
		// Determine if the process needs to be split into multiple processes
			// Split the process if there are if/else statements so that the if/else statements only assign values to one pin
		VProgram result = new VProgram();
//		ImmutableList<DesignUnit> newDesignUnitList = ImmutableList.of();

		for (DesignUnit designUnit: program.designUnits){
			ImmutableList newStatementList = ImmutableList.of();
			for (Statement statement: designUnit.arch.statements){

				if (statement instanceof Process){
					ImmutableList<Statement> nonIfElseStatements = ImmutableList.of();
					for (Statement sequentialStatement: ((Process)statement).sequentialStatements){
						if (sequentialStatement instanceof IfElseStatement){
							ImmutableList<Statement> splitStatements = splitIfElseStatement((IfElseStatement) sequentialStatement);
							for (int i =0; i< splitStatements.size(); i++){
								newStatementList = newStatementList.append(splitStatements.get(i));
							}
						}else{
							nonIfElseStatements = nonIfElseStatements.append(sequentialStatement);
						}
					}
					if (nonIfElseStatements.size() >0){
						this.usedVarsInExpr.clear();
						for (int i = 0; i < nonIfElseStatements.size(); i++){
							traverseAssignmentStatement((AssignmentStatement)nonIfElseStatements.get(i));
						}
						ImmutableList<String> sensitivityList = ImmutableList.copyOf((new ArrayList<String>(this.usedVarsInExpr)));
						Process processToAdd = new Process(nonIfElseStatements, sensitivityList);
							newStatementList = newStatementList.append(processToAdd);
					}
				}else{
					newStatementList = newStatementList.append(statement);
				}
			}
			DesignUnit newDesignUnit = designUnit.setArchitecture(designUnit.arch.varyStatements(newStatementList));
			result = result.append(newDesignUnit);

		}
		return result;
	}
	
	// You do not have to use this helper method, but we found it useful
	
	private ImmutableList<Statement> splitIfElseStatement(final IfElseStatement ifStmt) {
		ImmutableList<Statement> splitStatements = ImmutableList.of();
		// loop over each statement in the ifBody
		for (AssignmentStatement statementIf: ifStmt.ifBody){
			Statement ifElseStatement = statementIf;
			// loop over each statement in the elseBody
			for (AssignmentStatement statementElse: ifStmt.elseBody){
				// check if outputVars are the same
				if (statementIf.outputVar.equals(statementElse.outputVar)){
					// initialize/clear this.usedVarsInExpr
					this.usedVarsInExpr.clear();
					// call traverse a few times to build up this.usedVarsInExpr
					// build sensitivity list from this.usedVarsInExpr
					traverseExpr(ifStmt.condition);
					traverseAssignmentStatement(statementIf);
					traverseAssignmentStatement(statementElse);
					 ifElseStatement = ifStmt.setTrueBlock(ImmutableList.of(statementIf)).setElseBlock(ImmutableList.of(statementElse));
					// build the resulting list of split statements
					ImmutableList<String> sensitivtyList = ImmutableList.copyOf(new ArrayList<String>(this.usedVarsInExpr));
					ifElseStatement = new Process(ImmutableList.of(ifElseStatement), sensitivtyList);
				}
			}
			splitStatements = splitStatements.append(ifElseStatement);
		}
		// return result
		return splitStatements;
	}

	@Override
	public Expr visitVar(final VarExpr e) {
		this.usedVarsInExpr.add(e.identifier);
		return e;
	}

	// no-ops
	@Override public Expr visitConstant(ConstantExpr e) { return e; }
	@Override public Expr visitNot(NotExpr e) { return e; }
	@Override public Expr visitAnd(AndExpr e) { return e; }
	@Override public Expr visitOr(OrExpr e) { return e; }
	@Override public Expr visitXOr(XOrExpr e) { return e; }
	@Override public Expr visitNAnd(NAndExpr e) { return e; }
	@Override public Expr visitNOr(NOrExpr e) { return e; }
	@Override public Expr visitXNOr(XNOrExpr e) { return e; }
	@Override public Expr visitEqual(EqualExpr e) { return e; }
	@Override public Expr visitNaryAnd(NaryAndExpr e) { return e; }
	@Override public Expr visitNaryOr(NaryOrExpr e) { return e; }

}
