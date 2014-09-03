package crowdlanes.config;

import java.io.IOException;


public interface ParameterSweeper {
    
    public void run();
    public void readConfig(String fname) throws IOException;
    public void registerParam(String section, Class clazz, String optionName);

    public void setGexfFile(String string);
    
}
