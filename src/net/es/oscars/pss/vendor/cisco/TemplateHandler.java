package net.es.oscars.pss.vendor.cisco;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.log4j.*;
import net.es.oscars.pss.PSSException;

/**
 * TemplateHandler builds up a string from a file template and user supplied
 * information.
 *
 * @author David Robertson
 */
public class TemplateHandler {

    private Logger log;

    public TemplateHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Finds and substitutes the correct values into the template.
     *
     * @param hm a hash map containing info retrieved from the reservation,
     *           and from OSCAR's configuration
     * @param hops a list of hops only used if explicit path was given
     * @param fname full path of template file
     * @return string with information for configuring router
     * @throws IOException
     * @throws PSSException
     */
    public String buildString(Map<String,String> hm, List<String> hops,
                              String fname)
            throws IOException, PSSException {

        BufferedReader in = null;
        Matcher matcher = null;
        String line = null;

        this.log.info("buildString.start");
        StringBuilder sb = new StringBuilder();

        in = new BufferedReader(new FileReader(fname));
        while ((line = in.readLine()) != null) {
            sb.append(line + "\n");
        }
        in.close();
        StringBuilder filledTemplate =
            this.replaceVars(sb.toString(), hm, hops);
        this.log.info("buildString.finish");
        return filledTemplate.toString();
    }

    /**
     * Replaces the contents of substrings delimited by "**" if there is a
     * corresponding entry in the hash map.  If there is no corresponding
     * entry, throw an exception.
     *
     * @param buf string with contents of template file
     * @param hm a hash map of name value pairs
     * @param hops list of strings containing hops to add, if any
     * @return filledTemplate StringBuilder instance with substrings replaced
     * @throws PSSException
     */
    private StringBuilder replaceVars(String buf, Map<String,String> hm,
                                      List<String> hops)
            throws PSSException {

        String userVar = null;

        StringBuilder filledTemplate = new StringBuilder();
        Pattern pattern = Pattern.compile("[*][*]([\\w\\-]*)[*][*]");
        List<MatchResult> results = this.findAll(pattern, buf);
        int prevPosition = 0;
        for (MatchResult r : results) {
            filledTemplate.append(buf.substring(prevPosition, r.start(0)));
            userVar = r.group(1);
            // this user var isn't present in the teardown template
            if (userVar.equals("explicit-hop")) {
                this.addPath(filledTemplate, hops);
            } else if (hm.containsKey(userVar)) {
                filledTemplate.append(hm.get(userVar));
            } else {
                throw new PSSException(
                    "input does not have required user variable: " + userVar);
            }
            // end doesn't behave like substring, is one past
            prevPosition = r.end(0);
        }
        // TODO: last bit is chomped off..
        filledTemplate.append("\n!\nend\n");
        return filledTemplate;
    }

    /**
     * Handles adding explicit path hops to setup string, if any.
     *
     * @param sb StringBuilder instance with filled template
     * @param hops list of strings containing hops to add, if any
     * @throws PSSException
     */
    public void addPath(StringBuilder sb, List<String> hops) {

        // first instance of "next-address" already part of template
        for (int i=0; i < hops.size(); i++) {
            sb.append(hops.get(i) + "\n");
            if (i != hops.size()-1) {
                sb.append(" next-address ");
            }
        }
    }


    /*
     * Builds up list of results of pattern matches (from Java book [ref])
     *
     * @param pattern Pattern to match
     * @param buf string with patterns to match
     * @return list of MatchResults with matches, if any
     */
    public static List<MatchResult> findAll(Pattern pattern, String text)
    {
        List<MatchResult> results = new ArrayList<MatchResult>();
        Matcher m = pattern.matcher(text);
        while(m.find()) results.add(m.toMatchResult());
        return results;
    }
}
