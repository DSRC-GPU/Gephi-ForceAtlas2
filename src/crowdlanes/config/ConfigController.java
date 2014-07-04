package crowdlanes.config;

import java.util.List;


public interface ConfigController extends Iterable<List<ConfigParam.Value>> {
    
    void registerParam(ConfigParam pm);
    
}
