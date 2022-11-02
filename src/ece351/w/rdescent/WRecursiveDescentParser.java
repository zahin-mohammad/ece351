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

package ece351.w.rdescent;

import ece351.util.BitsException;
import org.parboiled.common.ImmutableList;

import ece351.util.Lexer;
import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;

public final class WRecursiveDescentParser {
    private final Lexer lexer;

    public WRecursiveDescentParser(final Lexer lexer) {
        this.lexer = lexer;
    }

    public static WProgram parse(final String input) {
    	final WRecursiveDescentParser p = new WRecursiveDescentParser(new Lexer(input));
        return p.parse();
    }

    public WProgram parse() {
        return program();
    }
    public WProgram program(){
        ImmutableList<Waveform> waveforms = ImmutableList.of(waveform());
        while (!lexer.inspectEOF()) {
            waveforms = waveforms.append(waveform());
        }
        lexer.consumeEOF();
        return new WProgram(waveforms);
    }
    public Waveform waveform(){
        String name = idCheck();
        ImmutableList<String> bits = bitsCheck();
        return new Waveform(bits, name);
    }
    public String idCheck(){
        String name = lexer.consumeID();
        lexer.consume(":");
        return name;
    }
    public ImmutableList bitsCheck(){
        ImmutableList<String> bits = ImmutableList.of();
        while(true){
            if (lexer.inspect("0")){
                bits = bits.append(lexer.consume("0"));
            }else if (lexer.inspect("1")){
                bits = bits.append(lexer.consume("1"));
            } else if (lexer.inspect(";")){
                lexer.consume(";");
                return bits;
            }
            else{
                throw new BitsException("Lexer does not contain 0 or 1 or ;");
            }
        }
    }
}
