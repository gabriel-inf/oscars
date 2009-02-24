package net.es.oscars;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Finds configuration files in file system
 * 
 * @author Andrew Lake (alake@internet2.edu)
 *
 */
public class ConfigFinder {
    private HashMap<String,String> catalinaDirectoryMap;
    final public static String CATALINA_SERVER_DIR = "server";
    final public static String CATALINA_REPO_DIR = "repo";
    final public static String AXIS_TOMCAT_DIR = "axis-tomcat";
    final public static String MAIL_TEMPLATES_DIR = "mail_templates";
    final public static String NOTIFY_DIR = "notify";
    final public static String PROPERTIES_DIR = "properties";
    final public static String PSS_DIR = "pss";
    
    private static ConfigFinder instance = null;
    
    public ConfigFinder(){
        this.catalinaDirectoryMap = new HashMap<String,String>();
        this.catalinaDirectoryMap.put(AXIS_TOMCAT_DIR, CATALINA_REPO_DIR);
        this.catalinaDirectoryMap.put(MAIL_TEMPLATES_DIR, CATALINA_SERVER_DIR+"/mail_templates");
        this.catalinaDirectoryMap.put(NOTIFY_DIR, CATALINA_SERVER_DIR);
        this.catalinaDirectoryMap.put(PROPERTIES_DIR, CATALINA_SERVER_DIR);
        this.catalinaDirectoryMap.put(PSS_DIR, CATALINA_SERVER_DIR);
    }
    
    /**
     * @return the instance
     */
    synchronized public static ConfigFinder getInstance() {
        if(ConfigFinder.instance == null){
            ConfigFinder.instance = new ConfigFinder();
        }
        return instance;
    }

    /**
     * Finds a given configuration file expected to be under <i>dir</i> with 
     * filename <i>fname</i>. It looks in the following locations for a config file:
     *     1. $OSCARS_HOME/conf/<i>dir</i>/<i>fname</i>
     *     2. $CATALINA_HOME/shared/classes/<i>map(dir)</i>/<i>fname</i>
     *     3. $CLASSPATH/conf/<i>dir</i>/<i>fname</i>
     * As of version 0.5 CATALINA_HOME is no longer the default so a mapping kept of 
     * dir that translates dir values to "server" or "repo".
     * @param dir the directory under conf to check
     * @param fname the name of the file to find
     * @return the full path to the config file
     * @throws RemoteException 
     */
     public String find(String dir, String fname) throws RemoteException{
        List<String> propFileCanidates = new ArrayList<String>();
        //1. Check OSCARS_HOME/conf
        propFileCanidates.add(System.getenv("OSCARS_HOME") + "/conf/" + 
                dir + "/" + fname);
        
        //2. Check CATALINA_HOME (requires mapping to server/repo)
        if(this.catalinaDirectoryMap.containsKey(dir)){
            propFileCanidates.add(System.getenv("CATALINA_HOME") + 
                "/shared/classes/" + this.catalinaDirectoryMap.get(dir) + 
                "/" + fname);
        }
        
        //3. Check conf under classpath
        propFileCanidates.add("conf/" + dir + "/" + fname);
        
        //Iterate through locations until find file
        for(String propFileCanidate : propFileCanidates){
            File tmpFile = new File(propFileCanidate);
            if(tmpFile.exists()){
                return propFileCanidate;
            }
        }
        
        throw new RemoteException("Unable to find config file " + dir + 
                "/" + fname);
    }
}
