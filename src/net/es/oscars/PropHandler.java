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
                String expandedValue = this.expandEnv(propertyValue);
                groupProperties.setProperty(propertyName,
                                            expandedValue);
            }
        }
        return groupProperties;
    }

    /**
     * Expands environment variables in property value.  Adapted from p.215,
     * Java in a Nutshell.  Environment variables should be in Ant-like
     * format, i.e. ${catalina.home}.
     *
     * @param origTxt string with original text to check for being env var
     * @return finalTxt string with expanded version if env var
     */
    private String expandEnv(String origTxt) {
        Pattern pattern = Pattern.compile("\\$\\{.+?\\}");
        StringBuilder sb = new StringBuilder();
        int currentPos = 0;
        boolean foundEnvVar = false;
        String finalTxt = null;

        List<MatchResult> results = findAll(pattern, origTxt);
        for(MatchResult r : results) {
            foundEnvVar = true;
            for (int i = currentPos; i < r.start(); i++) {
                sb.append(origTxt.charAt(i));
            }
            // remove enclosing ${}
            String enclosedEnv = r.group().substring(2,r.end()-r.start()-1);
            String upperEnv = enclosedEnv.toUpperCase();
            String canonicalEnv = upperEnv.replace(".", "_");
            String envVar = System.getenv(canonicalEnv);
            if (envVar != null) {
                sb.append(System.getenv(canonicalEnv));
            } else {
                // TODO:  better error handling
                sb.append(r.group());
            }
            currentPos = r.end();
        }
        if (!foundEnvVar) {
            finalTxt = origTxt;
        } else {
            for (int i = currentPos; i < origTxt.length(); i++) {
                sb.append(origTxt.charAt(i));
            }
            finalTxt = sb.toString();
        }
        return finalTxt;
    }

    /**
     * Builds a pattern match set.  From p.215, Java in a Nutshell.
     *
     * @param pattern Pattern to match
     * @param text CharSequence to check for match
     * @return results list of MatchResults, if any
     */
    private List<MatchResult> findAll(Pattern pattern, CharSequence text)
    {
        List<MatchResult> results = new ArrayList<MatchResult>();
        Matcher m = pattern.matcher(text);
        while(m.find()) results.add(m.toMatchResult());
        return results;
    }
}
