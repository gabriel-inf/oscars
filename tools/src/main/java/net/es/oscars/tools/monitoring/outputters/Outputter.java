package net.es.oscars.tools.monitoring.outputters;

import java.util.Map;

public interface Outputter {
    
    public void init(Map<String, Object> config);
    
    public void output(Map<String,Object> data);
}
