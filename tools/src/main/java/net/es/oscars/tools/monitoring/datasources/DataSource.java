package net.es.oscars.tools.monitoring.datasources;

import java.util.Map;

public interface DataSource {
    
    public void init(Map<String, Object> config);
    
    public void retrieve(Map<String, Object> data);
}
