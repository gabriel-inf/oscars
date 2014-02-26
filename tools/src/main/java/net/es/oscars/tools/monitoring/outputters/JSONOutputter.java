package net.es.oscars.tools.monitoring.outputters;

import java.util.Map;

import net.sf.json.JSONObject;

public class JSONOutputter implements Outputter{

    public void init(Map<String, Object> config) {
        
    }
    
    public void output(Map<String, Object> data) {
        JSONObject json = JSONObject.fromObject(data);
        System.out.println(json.toString());
    }

}
