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
import java.io.File;

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.common.FileUtils;
import org.parboiled.common.ImmutableList;

import ece351.util.BaseParser351;
import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;

@BuildParseTree
//Parboiled requires that this class not be final
public /*final*/ class WParboiledParser extends BaseParser351 {

	/**
	 * Run this parser, exit with error code 1 for malformed input.
	 * Called by wave/Makefile.
	 * @param args
	 */
	public static void main(final String[] args) {
    	process(WParboiledParser.class, FileUtils.readAllText(args[0]));
    }

	/**
	 * Construct an AST for a W program. Use this for debugging.
	 */
	public static WProgram parse(final String inputText) {
		return (WProgram) process(WParboiledParser.class, inputText).resultValue;
	}

	/**
	 * By convention we name the top production in the grammar "Program".
	 */
	@Override
    public Rule Program() {
		return Sequence(
				push(ImmutableList.of()), //[] for waveforms
				OneOrMore(Waveform()), // [] [waveforms]
				push (new WProgram((ImmutableList<Waveform>)pop())),
				EOI
		);
    }

	/**
	 * Each line of the input W file represents a "pin" in the circuit.
	 */
    public Rule Waveform() {
		return Sequence(
				Name(),
				push(match().replace(" ", "")), // ... [...] name
				swap(), // ... name [...]
				':',
				BitString(), // ... name [...] bits,
				swap(), // ... name bits [...]
				';',
				W0(),

				push(
						((ImmutableList)pop()).append(new Waveform((ImmutableList)pop(), (String)pop()))
				) // ...[... waveform]
		);
    }

    /**
     * The first token on each line is the name of the pin that line represents.
     */
    public Rule Name() {
		return Sequence(
				OneOrMore(Letter()), W0()
		);
    }
    
    /**
     * A Name is composed of a sequence of Letters. 
     * Recall that PEGs incorporate lexing into the parser.
     */
    public Rule Letter() {
		return
				FirstOf(CharRange('a','z'),CharRange('A','Z'));
    }

    /**
     * A BitString is the sequence of values for a pin.
     */
    public Rule BitString() {
		return Sequence(
				push(ImmutableList.of()),
				OneOrMore(FirstOf(Bit(), W1()))
		);
    }
    
    /**
     * A BitString is composed of a sequence of Bits. 
     * Recall that PEGs incorporate lexing into the parser.
     */
    public Rule Bit() {   
		return Sequence(

				FirstOf('1','0'),
				push(match()), // ...[...] 1
				swap(), // ...1 [...]
				push(((ImmutableList)pop()).append(pop())) // ...[...1]

		);
    }

}
