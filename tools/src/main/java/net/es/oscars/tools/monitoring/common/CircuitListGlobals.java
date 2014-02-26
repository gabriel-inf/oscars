package net.es.oscars.tools.monitoring.common;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.es.oscars.tools.monitoring.datasources.DataSource;
import net.es.oscars.tools.monitoring.datasources.OSCARSDataSource;
import net.es.oscars.tools.monitoring.outputters.JSONOutputter;
import net.es.oscars.tools.monitoring.outputters.Outputter;

import org.apache.log4j.Logger;
import org.ho.yaml.Yaml;

public class CircuitListGlobals {
    static private CircuitListGlobals instance = null;
    static private String configFile = null;
    
    private List<DataSource> dataSources;
    private List<Outputter> outputters;
    
    
    final public static String PROP_DATA_SOURCES = "dataSources";
    final public static String PROP_OUTPUTTERS = "outputters";
    final public static String PROP_DATA_STORE_TYPE = "type";
    final public static String PROP_OUTPUTTER_TYPE = "type";
    final public static String DS_TYPE_NMWG = "nmwg";
    final public static String DS_TYPE_OSCARS = "oscars-circuits";
    final public static String OUPUTTER_TYPE_JSON = "json";
    
    /**
     * Sets the configuration file to use
     * 
     * @param newConfigFile the configuration file to use on initialization
     */
    public static void init(String newConfigFile){
        configFile = newConfigFile;
    }
    
    private CircuitListGlobals(){
      //check config file
        if(configFile == null){
            throw new RuntimeException("No config file set.");
        }
        Map config = null;
        try {
            config = (Map) Yaml.load(new File(configFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        //Verify config
        this.verifyListProp(config, PROP_DATA_SOURCES);
        this.verifyListProp(config, PROP_OUTPUTTERS);
        
        //Load data source
        this.dataSources = new ArrayList<DataSource>();
        for(Map<String, Object> ds : (List<Map<String, Object>>) config.get(PROP_DATA_SOURCES)){
            DataSource tmpDS = null;
            if(DS_TYPE_OSCARS.equals(ds.get(PROP_DATA_STORE_TYPE))){
                tmpDS = new OSCARSDataSource();
            }else{
                throw new RuntimeException("Unrecognized data store type " + ds.get(PROP_DATA_STORE_TYPE));
            }
            tmpDS.init(ds);
            this.dataSources.add(tmpDS);
        }
        
        //Load outputters
        this.outputters = new ArrayList<Outputter>();
        for(Map<String, Object> outputter : (List<Map<String, Object>>) config.get(PROP_OUTPUTTERS)){
            Outputter tmpOutputter = null;
            if(OUPUTTER_TYPE_JSON.equals(outputter.get(PROP_OUTPUTTER_TYPE))){
                tmpOutputter = new JSONOutputter();
            }else{
                throw new RuntimeException("Unrecognized outputter type " + outputter.get(PROP_OUTPUTTER_TYPE));
            }
            tmpOutputter.init(outputter);
            this.outputters.add(tmpOutputter);
        }
    }
    
    private void verifyListProp(Map config, String propName){
        if(!config.containsKey(propName) || config.get(propName) == null){
            throw new RuntimeException("Mising required config option " + propName);
        }
        List<Map<String, Object>> tmp = null;
        try{
           tmp = ((List<Map<String, Object>>) config.get(propName));
        }catch(Exception e){
            throw new RuntimeException(propName + " config option must be a list");
        }
        
        if(tmp.isEmpty()){
            throw new RuntimeException(propName + " cannot be an empty list");
        }
    }
    
    synchronized static public CircuitListGlobals getInstance() {
        if(instance == null){
            instance = new CircuitListGlobals();
        }

        return instance;
    }

    public List<DataSource> getDataSources() {
        return dataSources;
    }
    
    public List<Outputter> getOutputters() {
        return outputters;
    }
    
}
