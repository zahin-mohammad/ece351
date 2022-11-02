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

package ece351.w.svg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.parboiled.common.ImmutableList;

import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;


public final class TransformSVG2W {
	
	/**
	 * Transforms an instance of WSVG to an instance of WProgram.
	 */
	public static final WProgram transform(final PinsLines pinslines) {
		final List<Line> lines = new ArrayList<Line>(pinslines.segments);
		final List<Pin> pins = new ArrayList<Pin>(pinslines.pins);
		return new WProgram(waveforms(lines, pins));
	}

	/**
	 * Transform a list of Line to an instance of Waveform.
	 */
	private static ImmutableList<Waveform> waveforms(final List<Line> lines, final List<Pin> pins) {
		if(lines.isEmpty() || pins.isEmpty()) return null;
		// assume pins and lines are given in order
		ImmutableList<Waveform> waveforms = ImmutableList.of();
		int i = 0;
		int j = lines.size();

		for(Pin p: pins){
			ImmutableList<String> bits = ImmutableList.of();
			int mid = p.y;
			while(i != j){
				Line l = lines.get(i);

				if (l.y1 == l.y2 ){
					if(l.y1 > mid){ bits = bits.append("0"); }
					else if (l.y1 < mid){ bits = bits.append("1"); }
					else{ throw new ece351.util.Todo351Exception(); }
				}

				i++;
				if(i !=j && lines.get(i).x1 < l.x1 ){
					break;
				}
			}
			Waveform waveform = new Waveform(bits, p.id);
			waveforms = waveforms. append(waveform);
		}
		return waveforms;
	}
}
