package crowdlanes.config;

import static com.google.common.collect.Sets.cartesianProduct;
import crowdlanes.Simulation;
import crowdlanes.config.ConfigParam.Value;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.ini4j.Wini;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ParameterSweeper.class)
public class ParameterSweeperImpl implements ParameterSweeper {

    private final List<ConfigParam> params;
    private final Simulation sim;
    private Wini configFile;

    private PrintWriter paramWriter;

    public ParameterSweeperImpl() throws IOException, IllegalAccessException {
        params = new ArrayList<>();
        this.sim = new Simulation();
    }

    @Override
    public void readConfig(String fname) throws IOException {
        try {
            configFile = new Wini(new File(fname));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void run() {
        for (List<Value> l : cartesianProduct(params)) {
            sim.run(l);
            ResultsDir.updateResultPath();
        }
    }

    @Override
    public void registerParam(String section, Class clazz, String optionName) {
        ConfigParam pm = new ConfigParam(configFile, section, clazz, optionName);
        params.add(pm);
        pm.read();
    }
}
