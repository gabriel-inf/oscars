package net.es.oscars.tools.monitoring.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.tools.monitoring.datasources.DataSource;
import net.es.oscars.tools.monitoring.outputters.Outputter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class CicuitListCLI {
    public static void main(String[] args){
        //Read command line options
        OptionParser parser = new OptionParser(){
            {
                acceptsAll(Arrays.asList("h", "help"), "prints this help screen");
                acceptsAll(Arrays.asList("c", "config"), "configuration file").withRequiredArg().ofType(String.class);
                acceptsAll(Arrays.asList("l", "log4j"), "log4j configuration file").withRequiredArg().ofType(String.class);
            }
        };
        
        OptionSet opts = parser.parse(args);
        if(opts.has("h")){
            try{
                parser.printHelpOn(System.out);
            }catch(Exception e){}
            System.exit(0);
        }
        
        String configFile = "./config/circuitlist.yaml";
        if(opts.has("c")){
            configFile = (String) opts.valueOf("c");
        }
        
        String logConfigFile = "./config/log4j.properties";
        if(opts.has("l")){
            logConfigFile = (String) opts.valueOf("l");
        }
        System.setProperty("log4j.configuration", "file:" + logConfigFile);
        
        //load settings
        CircuitListGlobals.init(configFile);
        CircuitListGlobals globals = CircuitListGlobals.getInstance();
        
        //Build Data
        Map<String,Object> data = new HashMap<String, Object>();
        for(DataSource ds : globals.getDataSources()){
            ds.retrieve(data);
        }
        
        //Output Data
        for(Outputter outputter : globals.getOutputters()){
            outputter.output(data);
        }
        
    }
}
