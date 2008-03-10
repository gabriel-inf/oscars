package net.es.oscars;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.*;
import java.util.*;


/**
 * EnvHandler handles expanding environment variables in properties files.
 * based on their prefix.
 */
public class EnvHandler {
    private String propertiesFile;

    // place holder in case want to do initialization
    public EnvHandler() {
        ;
    }

    /**
     * Expands environment variables in property value.  Adapted from p.215,
     * Java in a Nutshell.  Environment variables should be in Ant-like
     * format, i.e. ${catalina.home}.
     *
     * @param origTxt string with original text to check for being env var
     * @return finalTxt string with expanded version if env var
     */
    public String expandEnv(String origTxt) {
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
