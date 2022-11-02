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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
import ece351.v.ast.Component;
import ece351.v.ast.DesignUnit;
import ece351.v.ast.IfElseStatement;
import ece351.v.ast.Process;
import ece351.v.ast.VProgram;


/**
 * Inlines logic in components to architecture body.
 */
public final class Elaborator extends PostOrderExprVisitor {

	private final Map<String, String> current_map = new LinkedHashMap<String, String>();
	
	public static void main(String[] args) {
		System.out.println(elaborate(args));
	}
	
	public static VProgram elaborate(final String[] args) {
		return elaborate(new CommandLine(args));
	}
	
	public static VProgram elaborate(final CommandLine c) {
        final VProgram program = DeSugarer.desugar(c);
        return elaborate(program);
	}
	
	public static VProgram elaborate(final VProgram program) {
		final Elaborator e = new Elaborator();
		return e.elaborateit(program);
	}

	private VProgram elaborateit(final VProgram root) {

		// our ASTs are immutable. so we cannot mutate root.
		// we need to construct a new AST that will be the return value.
		// it will be like the input (root), but different.
		VProgram result = new VProgram();
		int compCount = 0;

		// iterate over all of the designUnits in root.
		for (DesignUnit du : root.designUnits){
			// for each one, construct a new architecture.
			// Architecture a = du.arch.varyComponents(ImmutableList.<Component>of());
			// this gives us a copy of the architecture with an empty list of components.
			// now we can build up this Architecture with new components.
			Architecture a = du.arch.varyComponents(ImmutableList.of());
			for (Component c : du.arch.components){
				compCount++;
				// In the elaborator, an architectures list of signals, and set of statements may change (grow)
				for (DesignUnit result_du: result.designUnits){
					//populate dictionary/map
					if (result_du.entity.identifier.equals(c.entityName)){
						current_map.clear();
						//add input signals, map to ports
						ImmutableList<String> entity_inputs =  result_du.entity.input;
						for (int i = 0; i < entity_inputs.size(); i++){
							current_map.put(entity_inputs.get(i), c.signalList.get(i));
						}
						//add output signals, map to ports
						ImmutableList<String> entity_outputs =  result_du.entity.output;
						int output_index = entity_inputs.size();
						for (int i = 0; i < entity_outputs.size(); i++){
							current_map.put(entity_outputs.get(i), c.signalList.get(output_index++));
						}
						for( String s : result_du.arch.signals){
							String comp = String.format("comp%d_%s",compCount,s);
							current_map.put(s,comp);

							// add local signals, add to signal list of current designUnit
							a = a.appendSignal(comp);
						}
						//loop through the statements in the architecture body
							// make the appropriate variable substitutions for signal assignment statements
							// i.e., call changeStatementVars
							// make the appropriate variable substitutions for processes (sensitivity list, if/else body statements)
							// i.e., call expandProcessComponent
						for (Statement statement: result_du.arch.statements){
							Statement s;
							if (statement instanceof IfElseStatement)
								s = changeIfVars((IfElseStatement)statement);
							else if (statement instanceof Process)
								s = expandProcessComponent((Process)statement);
							else if (statement instanceof AssignmentStatement)
								s  = changeStatementVars((AssignmentStatement)statement);
							else
								throw new ece351.util.Todo351Exception();
							a = a.appendStatement(s);
						}

					}
				}
			}
			// append this new architecture to result
			result = result.append(new DesignUnit(a,du.entity));
		}
		assert result.repOk();
		return result;
	}
	
	// you do not have to use these helper methods; we found them useful though
	private Process expandProcessComponent(final Process process) {
		ImmutableList<String> sensitivityList = ImmutableList.of();
		ImmutableList<Statement> sequentialStatements = ImmutableList.of();
		for (String s : process.sensitivityList){
			sensitivityList = sensitivityList.append(current_map.getOrDefault(s,s));
		}
		for (Statement s : process.sequentialStatements){
			if (s instanceof  IfElseStatement)
				sequentialStatements = sequentialStatements.append(changeIfVars((IfElseStatement)s));
			else if (s instanceof AssignmentStatement)
				sequentialStatements = sequentialStatements.append(changeStatementVars((AssignmentStatement)s));
			else
				throw new ece351.util.Todo351Exception();
		}
		return new Process(sequentialStatements,sensitivityList);

	}
	
	// you do not have to use these helper methods; we found them useful though
	private  IfElseStatement changeIfVars(final IfElseStatement s) {
		IfElseStatement return_statement = new IfElseStatement(traverseExpr(s.condition));
		for(Statement assignmentStatement :s.ifBody){
			return_statement = return_statement.appendToTrueBlock(
					changeStatementVars((AssignmentStatement)assignmentStatement)
			);
		}
		for(Statement assignmentStatement :s.elseBody){
			return_statement = return_statement.appendToElseBlock(
					changeStatementVars((AssignmentStatement)assignmentStatement)
			);
		}
		return return_statement;
	}

	// you do not have to use these helper methods; we found them useful though
	private AssignmentStatement changeStatementVars(final AssignmentStatement s){
		return new AssignmentStatement(current_map.getOrDefault(s.outputVar.toString(), s.outputVar.toString()), traverseExpr(s.expr));
	}
	
	
	@Override
	public Expr visitVar(VarExpr e) {
		return new VarExpr(current_map.getOrDefault(e.toString(), e.toString()));
	}
	
	// do not rewrite these parts of the AST
	@Override public Expr visitConstant(ConstantExpr e) { return e; }
	@Override public Expr visitNot(NotExpr e) { return e; }
	@Override public Expr visitAnd(AndExpr e) { return e; }
	@Override public Expr visitOr(OrExpr e) { return e; }
	@Override public Expr visitXOr(XOrExpr e) { return e; }
	@Override public Expr visitEqual(EqualExpr e) { return e; }
	@Override public Expr visitNAnd(NAndExpr e) { return e; }
	@Override public Expr visitNOr(NOrExpr e) { return e; }
	@Override public Expr visitXNOr(XNOrExpr e) { return e; }
	@Override public Expr visitNaryAnd(NaryAndExpr e) { return e; }
	@Override public Expr visitNaryOr(NaryOrExpr e) { return e; }
}
