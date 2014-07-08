package crowdlanes.config;

import java.util.HashMap;
import java.util.List;

public class CurrentConfig {

    HashMap<String, Object> vals;

    public CurrentConfig(List<ConfigParam.Value> paramVals) {
        vals = new HashMap<>();
        for (ConfigParam.Value v : paramVals) {
            vals.put(v.getName(), v.getValue());
        }
    }

    public Object getValue(String name) {
        return vals.get(name);
    }

    public Short getShortValue(String name) {
        return (Short) getValue(name);
    }

    public Integer getIntegerValue(String name) {
        return (Integer) getValue(name);
    }

    public String getStringValue(String name) {
        return (String) getValue(name);
    }

    public Double getDoubleValue(String name) {
        return (Double) getValue(name);
    }

    public Float getFloatValue(String name) {
        return (Float) getValue(name);
    }

    public Boolean getBooleanValue(String name) {
        return (Boolean) getValue(name);
    }
}
