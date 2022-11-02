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

package ece351.w.parboiled;
import ece351.common.ast.NaryExpr;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.common.FileUtils;

import ece351.util.BaseParser351;
import ece351.w.ast.WProgram;


@BuildParseTree
//Parboiled requires that this class not be final
public /*final*/ class WParboiledRecognizer extends BaseParser351 {

	/**
	 * Run this recognizer, exit with error code 1 to reject.
	 * This method is called by wave/Makefile.
	 * @param args args[0] is the name of the input file to read
	 */
	public static void main(final String[] args) {
    	process(WParboiledRecognizer.class, FileUtils.readAllText(args[0]));
    }
	
	public static void recognize(final String inputText) {
		process(WParboiledRecognizer.class, inputText);
	}

	
	/** 
	 * Use this method to print the parse tree for debugging.
	 * @param w the text of the W program to recognize
	 */
	public static void printParseTree(final String w) {
		printParseTree(WParboiledRecognizer.class, w);
	}

	/**
	 * By convention we name the top production in the grammar "Program".
	 */
	@Override
	public Rule Program() {
		// TODO: short code snippet
//		throw new ece351.util.Todo351Exception();
		return Sequence(OneOrMore(Waveform()), EOI) ;

	}
    
	/**
	 * Each line of the input W file represents a "pin" in the circuit.
	 */
    public Rule Waveform() {
	//    regexs.add("(Name:BitString;\\s*)+");
		return Sequence(Name(),':', BitString(),';', W0());
    }

    /**
     * The first token in each statement is the name of the waveform 
     * that statement represents.
     */
    public Rule Name() {
		// TODO: short code snippet
		//throw new ece351.util.Todo351Exception();
		//(Letter*+\\s*)
		// TODO: Account for digit
		return Sequence(OneOrMore(Letter()), W0());
    }

    /**
     * A Name is composed of a sequence of Letters. 
     * Recall that PEGs incorporate lexing into the parser.
     */
    public Rule Letter() {
		//[a-zA-Z]
    	return FirstOf(CharRange('a','z'),CharRange('A','Z')); // Can prob simplify this with IgnoreCase
    }

    /**
     * A BitString is the sequence of values for a pin.
     */
    public Rule BitString() {
		// (0|1|\\s)+
		return OneOrMore(FirstOf(Bit(), W1()) );
    }
    
    /**
     * A BitString is composed of a sequence of Bits. 
     * Recall that PEGs incorporate lexing into the parser.
     */
    public Rule Bit() {       
		//0|1
		return FirstOf('1','0');
    }

}

