package crowdlanes.config;

import static com.google.common.collect.Sets.cartesianProduct;
import crowdlanes.Simulation;
import crowdlanes.config.ConfigParam.Value;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ParameterSweeper {

    private final List<ConfigParam> params;
    private final Simulation sim;

    private PrintWriter paramWriter;

    public ParameterSweeper() throws IOException, IllegalAccessException {
        params = new ArrayList<>();
        this.sim = new Simulation();
    }

    public void run() {
        for (List<Value> l : cartesianProduct(params)) {
            sim.run(l);
            ResultsDir.updateResultPath();
        }
    }

    public void registerParam(ConfigParam pm) {
        params.add(pm);
        pm.read();
    }
}
