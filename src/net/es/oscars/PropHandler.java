package net.es.oscars;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.*;
import java.util.*;


/**
 * PropHandler handles getting groups of properties from a properties file,
 * based on their prefix.
 */
public class PropHandler {
    private String propertiesFile;

    public PropHandler(String fname) {
        this.propertiesFile = System.getenv("CATALINA_HOME") +
            "/shared/classes/server/" + fname;
    }

    /**
     * Retreives group of properties from a file, given a prefix.
     *
     * @param groupName A string with the name of the prefix
     * @return properties Properties from
     *     $CATALINA_HOME/shared/oscars.conf/server/oscars.properties
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
