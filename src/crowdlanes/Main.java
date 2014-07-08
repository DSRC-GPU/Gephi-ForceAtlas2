package crowdlanes;

import crowdlanes.config.ConfigParam;
import static crowdlanes.config.ParamNames.*;
import crowdlanes.config.ParameterSweeper;
import crowdlanes.graphReader.GraphReader;
import crowdlanes.stages.EmbeddingStage;
import crowdlanes.stages.SmootheningStage;
import crowdlanes.stages.VelocityProcessorStage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.ini4j.Wini;
import org.openide.util.Lookup;

public class Main {


    public static void main(String[] args) throws FileNotFoundException, IOException, IllegalAccessException {
        if (args.length < 2) {
            System.out.println("Usage : gexf_file config_file");
            System.exit(-1);
        }

        Lookup.getDefault().lookup(GraphReader.class).importFile(args[0]);


        Wini config = new Wini(new File(args[1]));
        ParameterSweeper ps = new ParameterSweeper();

        ps.registerParam(new ConfigParam(config, EmbeddingStage.SECTION, Integer.class, CONFIG_PARAM_INITIAL_EMBEDDING_SEED));
        ps.registerParam(new ConfigParam(config, EmbeddingStage.SECTION, String.class, CONFIG_PARAM_EMBEDDING_TYPE));
        ps.registerParam(new ConfigParam(config, EmbeddingStage.SECTION, Integer.class, CONFIG_PARAM_FORCE_ATLAS_NO_ITER));
        ps.registerParam(new ConfigParam(config, EmbeddingStage.SECTION, Boolean.class, CONFIG_PARAM_FORCE_ATLAS_USE_EDGE_WEIGHTS));
        
        ps.registerParam(new ConfigParam(config, VelocityProcessorStage.SECTION, Integer.class, CONFIG_PARAM_VELOCITY_VEC_WINDOW_SIZE));
        
        ps.registerParam(new ConfigParam(config, DynamicGraphIterator.SECTION, Double.class, CONFIG_PARAM_GRAPH_ITERATOR_STEP));
        ps.registerParam(new ConfigParam(config, DynamicGraphIterator.SECTION, Double.class, CONFIG_PARAM_GRAPH_ITERATOR_WINDOW_SIZE));
        
        ps.registerParam(new ConfigParam(config, SmootheningStage.SECTION, Float.class, CONFIG_PARAM_SMOOTHENING_PHI_FINE));
        ps.registerParam(new ConfigParam(config, SmootheningStage.SECTION, Float.class, CONFIG_PARAM_SMOOTHENING_PHI_COARSE));
        ps.registerParam(new ConfigParam(config, SmootheningStage.SECTION, Integer.class, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE));
        ps.registerParam(new ConfigParam(config, SmootheningStage.SECTION, Integer.class, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE));
        ps.registerParam(new ConfigParam(config, SmootheningStage.SECTION, String.class, CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS));
        ps.run();
    }
}
