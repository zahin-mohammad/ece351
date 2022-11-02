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

package ece351.f.parboiled;

import ece351.common.ast.*;
import org.parboiled.Rule;

import ece351.f.ast.FProgram;
import ece351.util.CommandLine;
import org.parboiled.common.ImmutableList;

// Parboiled requires that this class not be final
public /*final*/ class FParboiledParser extends FBase implements Constants {

	
	public static void main(final String[] args) {
    	final CommandLine c = new CommandLine(args);
    	final String input = c.readInputSpec();
    	final FProgram fprogram = parse(input);
    	assert fprogram.repOk();
    	final String output = fprogram.toString();
    	
    	// if we strip spaces and parens input and output should be the same
    	if (strip(input).equals(strip(output))) {
    		// success: return quietly
    		return;
    	} else {
    		// failure: make a noise
    		System.err.println("parsed value not equal to input:");
    		System.err.println("    " + strip(input));
    		System.err.println("    " + strip(output));
    		System.exit(1);
    	}
    }
	
	private static String strip(final String s) {
		return s.replaceAll("\\s", "").replaceAll("\\(", "").replaceAll("\\)", "");
	}
	
	public static FProgram parse(final String inputText) {
		final FProgram result = (FProgram) process(FParboiledParser.class, inputText).resultValue;
		assert result.repOk();
		return result;
	}

	@Override
	public Rule Program() {
		// STUB: return NOTHING; // TODO: replace this stub
		// For the grammar production Id, ensure that the Id does not match any of the keywords specified
		// in the rule, 'Keyword'
		return Sequence(
				push(ImmutableList.of()), // []
				W0(),
				OneOrMore(
						Sequence(

								Formula(),
								W0()
						)
				),
				push(new FProgram((ImmutableList)pop())),
				debugmsg(peek()),
				EOI
		);

	}

	public Rule Formula(){
		return Sequence(
				Var(), // ... [] Var
				W0(),
				"<=",
				W0(),
				Expr(), // ... [] Var Expr
				swap(), // ... [] Expr Var
				push(new AssignmentStatement((VarExpr)pop(), (Expr)pop())), // ... [] Expr
				swap(), // ... Expr []
				push(((ImmutableList)pop()).append(pop())), // ... [Expr]
				W0(),
				";"
		);
	}

	public Rule Expr(){
		return Sequence(
				Term(), // Expr A
				W0(),
				ZeroOrMore(
					OR(),
					W0(),
					Term(), // Expr A Expr B
					swap(), // Expr B Expr A
					push ( new OrExpr((Expr)pop(), (Expr)pop()))
				)
		);
	}

	public Rule Term(){
		return Sequence(
				Factor(), // .. ExprA
				W0(),
				ZeroOrMore(
					AND(),
					W0(),
					Factor(), // ... Expr A Expr B
					W0(),
					swap(), // ... Expr B Expr A
					push( new AndExpr((Expr)pop(), (Expr)pop()))
				)
		);
	}

	public Rule Factor(){
		return FirstOf(
				Sequence(
						NOT(),
						W0(),
						Factor(), // ... Expr
						W0(),
						push(new NotExpr((Expr)pop())) // ... NotExpr
				),
				Sequence(
						"(",
						W0(),
						Expr(), // ... Expr
						W0(),
						")"
				),
				Var(), // ... Var
				Constant() // ... Var Const
		);
	}
	public Rule Constant(){
		return
			Sequence(
					"'",
					FirstOf("1", "0"),
					push(ConstantExpr.make(match())),
					"'"
			);
	}

	public Rule Var(){
		return Sequence(
				TestNot(Keyword()),
				Sequence(Char(), ZeroOrMore(FirstOf(Char(), Digit(), '_'))),
				push(new VarExpr(match())),
				W0()
		);
	}
	public Rule Letter() {
		//[a-zA-Z]
		return FirstOf(CharRange('a','z'),CharRange('A','Z')); // Can prob simplify this with IgnoreCase
	}
}
