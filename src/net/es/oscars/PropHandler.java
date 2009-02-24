package net.es.oscars;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;


/**
 * PropHandler handles getting groups of properties from a properties file,
 * based on their prefix.
 */
public class PropHandler {
    private String propertiesFile;

    public PropHandler(String fname) {
        //has own ConfigFinder since used so many places
        ConfigFinder configFinder = ConfigFinder.getInstance();
        
        try {
            this.propertiesFile = 
                configFinder.find(ConfigFinder.PROPERTIES_DIR, fname);
        } catch (RemoteException e) {
            System.out.println("fatal error: unable to find file " + fname);
            System.exit(0);
        }
    }

    /**
     * Retrieves group of properties from a file, given a prefix.
     *
     * @param groupName A string with the name of the prefix
     * @return properties Properties from oscars.properties
     */
    public Properties getPropertyGroup(String groupName,
                                       boolean stripPrefix) {
        Properties groupProperties = new Properties();
        EnvHandler envHandler = new EnvHandler();
        String propertyName = null;

        // load properties
        Properties allProperties = new Properties();
        try {
            FileInputStream in = new FileInputStream(this.propertiesFile);
            allProperties.load(in);
            in.close();
        }
        catch (IOException e) {
            System.out.println("fatal error:  no properties file " +
                               this.propertiesFile);
            System.exit(0);
        }
        Enumeration e = allProperties.propertyNames();
        while (e.hasMoreElements()) {
            String elem = ( String )e.nextElement();
            if (elem.startsWith(groupName)) {
                if (stripPrefix) {
                    // get rid of period as well
                    propertyName = elem.substring(groupName.length()+1);
                } else {
                    propertyName = elem;
                }
                String propertyValue = allProperties.getProperty(elem);
                String expandedValue = envHandler.expandEnv(propertyValue);
                groupProperties.setProperty(propertyName,
                                            expandedValue);
            }
        }
        return groupProperties;
    }
}
