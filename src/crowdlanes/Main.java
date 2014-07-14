package crowdlanes;

import static crowdlanes.config.ConfigSections.*;
import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.config.ParameterSweeper;
import crowdlanes.graphReader.GraphReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.openide.util.Lookup;

public class Main {


    public static void main(String[] args) throws FileNotFoundException, IOException, IllegalAccessException {
        if (args.length < 2) {
            System.out.println("Usage : gexf_file config_file");
            System.exit(-1);
        }

        Lookup.getDefault().lookup(GraphReader.class).importFile(args[0]);


        ParameterSweeper ps =  Lookup.getDefault().lookup(ParameterSweeper.class);
        ps.readConfig(args[1]);

        ps.registerParam(GENERAL_SECTION, Boolean.class, CONFIG_PARAM_USE_GROUNDTRUTH);
        ps.registerParam(DOP_SECTION, Boolean.class, CONFIG_PARAM_USE_PCA_BEFORE_SMOOTHENING);
        ps.registerParam(EMBEDDING_SECTION, Integer.class, CONFIG_PARAM_INITIAL_EMBEDDING_SEED);
        ps.registerParam(EMBEDDING_SECTION, String.class, CONFIG_PARAM_EMBEDDING_TYPE);
        ps.registerParam(EMBEDDING_SECTION, Integer.class, CONFIG_PARAM_FORCE_ATLAS_NO_ITER);
        ps.registerParam(EMBEDDING_SECTION, Boolean.class, CONFIG_PARAM_FORCE_ATLAS_USE_EDGE_WEIGHTS);
        
        ps.registerParam(VELOCITY_SECTION, Integer.class, CONFIG_PARAM_VELOCITY_VEC_WINDOW_SIZE);
        
        ps.registerParam(GRAPH_ITER_SECTION, Double.class, CONFIG_PARAM_GRAPH_ITERATOR_STEP);
        ps.registerParam(GRAPH_ITER_SECTION, Double.class, CONFIG_PARAM_GRAPH_ITERATOR_WINDOW_SIZE);
        
        ps.registerParam(SMOOTHENING_SECTION, Float.class, CONFIG_PARAM_SMOOTHENING_PHI_FINE);
        ps.registerParam(SMOOTHENING_SECTION, Float.class, CONFIG_PARAM_SMOOTHENING_PHI_COARSE);
        ps.registerParam(SMOOTHENING_SECTION, Integer.class, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE);
        ps.registerParam(SMOOTHENING_SECTION, Integer.class, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE);
        ps.registerParam(SMOOTHENING_SECTION, String.class, CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS);
           
        ps.run();
    }
}
