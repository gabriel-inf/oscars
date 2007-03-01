package net.es.oscars;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


/**
 * PropHandler handles getting groups of properties from a properties file,
 * based on their prefix.
 */
public class PropHandler {
    private String propertiesFile;

    public PropHandler(String fname) {
        this.propertiesFile = System.getenv("CATALINA_HOME") +
            "/shared/oscars.conf/server/" + fname;
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
                groupProperties.setProperty(propertyName,
                                            allProperties.getProperty(elem));
            }
        }
        return groupProperties;
    }
}
