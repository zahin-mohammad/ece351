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

import java.io.PrintWriter;
import java.io.StringWriter;

import ece351.util.Debug;
import ece351.w.ast.WProgram;
import ece351.w.ast.Waveform;

public final class TransformW2SVG {

	public static String transform(final WProgram wp) {
		final StringWriter sw = new StringWriter();
		final PrintWriter out = new PrintWriter(sw);
		transform(wp, out);
		out.close();
		return sw.toString();
	}
	
	public static void transform(final WProgram wp, final PrintWriter out) {
		
		// header
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
		out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%\" height=\"100%\" version=\"1.1\">");
		out.println("<style type=\"text/css\"><![CDATA[line{stroke:#006600;fill:#00 cc00;} text{font-size:\"large\";font-family:\"sans-serif\"}]]></style>");
		out.println();

		final int WIDTH = 100;
		final int HEIGHT = 100;
		int y_mid = 150;
		int next = 150;
		int last_bit;
		int curr_bit;
		final int y_off = 50;

		// loop on waveforms
		// this line implicitly uses an iterator
		for (final Waveform w : wp.waveforms) {
			// reset the initial x position
			int x = 50;

			// write out the waveform name
			out.println(Pin.toSVG(w.name, x, y_mid));

			// advance the x position to start drawing
			x=100;

			last_bit = -1;
			curr_bit = -1;
			// loop on bits
			for (final String bit : w.bits) {
				// draw the horizontal line
				if (bit == "1"){
					out.println(new Line(x,y_mid - y_off,(x+ WIDTH), y_mid - y_off).toSVG());
					curr_bit = 1;
				}else if (bit ==  "0"){
					out.println(new Line(x,y_mid + y_off,(x+ WIDTH),y_mid + y_off).toSVG());
					curr_bit = 0;
				}
				// draw the vertical line
				if(last_bit != -1 && last_bit != curr_bit){
					out.println(new Line(x ,y_mid - y_off , x , y_mid + y_off).toSVG());
				}
				last_bit = curr_bit;
				// advance drawing
				x = x + WIDTH;
			}
			// advance waveform
			y_mid  = y_mid + next;
		}

		// footer
		out.println("</svg>");
		
	}

	/**
	 * Extra exploration activity (optional).
	 * Try changing the value of this flag and see how it changes the performance
	 * of the test harnesses. Why does that happen? What is the difference
	 * between a DOM-style XML parser and a SAX-style XML parser?
	 */
	public static final boolean USE_DOM_XML_PARSER = true; // TODO: replace this stub

}
