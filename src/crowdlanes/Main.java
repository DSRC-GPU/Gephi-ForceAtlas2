package crowdlanes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.ini4j.Wini;

public class Main {

    private static void setup(String gexf_file) throws FileNotFoundException {
        GraphReader.getInstance().setup(gexf_file);
        EdgeWeight.getInstance().setup();
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, IllegalAccessException {
        if (args.length < 2) {
            System.out.println("Usage : gexf_file config_file");
            System.exit(-1);
        }

        setup(args[0]);

        Wini config = new Wini(new File(args[1]));
        ParameterSweeper ps = new ParameterSweeper(config, new Simulation());
        ps.run();
    }
}
