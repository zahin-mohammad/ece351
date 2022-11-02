import java.util.*;
import ece351.w.ast.*;
import ece351.w.parboiled.*;
import static ece351.util.Boolean351.*;
import ece351.util.CommandLine;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import ece351.util.Debug;

public final class Simulator_z03 {
    public static void main(final String[] args) {
        // read the input F program
        // write the output
        // read input WProgram
        final CommandLine cmd = new CommandLine(args);
        final String input = cmd.readInputSpec();
        final WProgram wprogram = WParboiledParser.parse(input);
        // construct storage for output
        final Map<String,StringBuilder> output = new LinkedHashMap<String,StringBuilder>();
        output.put("m", new StringBuilder());
        int timeCount;
        // loop over each time step
        timeCount = wprogram.timeCount();
        for (int time = 0; time < timeCount; time++){
            // values of input variables at this time step
            final boolean c = wprogram.valueAtTime("c", time);
            // values of input variables at this time step
            final boolean b = wprogram.valueAtTime("b", time);
            // values of output variables at this time step
final String out_m = (or(c, b) ) ? "1 " : "0 ";
            // store outputs
            output.get("m").append(out_m);
        }
        try{
            final File f = cmd.getOutputFile();
            f.getParentFile().mkdirs();
            final PrintWriter pw = new PrintWriter(new FileWriter(f));
            // write the input
            System.out.println(wprogram.toString());
            pw.println(wprogram.toString());
            // write the output
            System.out.println(f.getAbsolutePath());
            for (final Map.Entry<String,StringBuilder> e : output.entrySet()) {
                System.out.println(e.getKey() + ":" + e.getValue().toString()+ ";");
                pw.write(e.getKey() + ":" + e.getValue().toString()+ ";\n");

            }
            pw.close();
        } catch (final IOException e) {

            Debug.barf(e.getMessage());

        }
    }
    // methods to compute values for output pins
}
