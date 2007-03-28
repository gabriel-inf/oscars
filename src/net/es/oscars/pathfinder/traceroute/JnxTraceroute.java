package net.es.oscars.pathfinder.traceroute;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.es.oscars.*;
import net.es.oscars.pathfinder.PathfinderException;


/**
 * JnxTraceroute contains methods handling traceroutes.
 */
public class JnxTraceroute {
    private ArrayList<String> rawHopData;
    private ArrayList<String> hops;
    private Properties props;
    private LogWrapper log;

    /**
     * Contructor for JnxTraceroute.
     */
    public JnxTraceroute() {
        this.rawHopData = new ArrayList<String>();
        this.hops = new ArrayList<String>();
        this.log = new LogWrapper(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("traceroute", true);
    }

    /**
     * Runs traceroute from source to destination.
     *
     * @param src string containing source IP
     * @param dst string containing destination IP
     * @throws IOException
     */
    public String traceroute(String src, String dst)
            throws PathfinderException, IOException {

        String cmd = "";
        String hopInfo = "";
        Pattern ipPattern = 
            Pattern.compile(".*?\\((\\d+\\.\\d+\\.\\d+\\.\\d+)\\).*?");
        
        Pattern errPattern = Pattern.compile(".*Operation timed out.*");

        if ((src == null) || (dst == null)) {
            throw new PathfinderException("Traceroute source or destination not defined");
        } else if(src.equals("default")) {
            src = this.props.getProperty("jnxSource");
        }

        // remove subnet mask if necessary,  e.g. 10.0.0.0/8 => 10.0.0.0
        dst = dst.replaceAll("/\\d*", "");
        String jnxKey = System.getenv("CATALINA_HOME") +
                        "/shared/oscars.conf/server/oscars.key";

        // prepare traceroute command
        cmd = "ssh -x -a -i " + jnxKey + " -l " + 
                   this.props.getProperty("jnxUser") + " " + src + " traceroute " + dst + 
                   " wait " + this.props.getProperty("timeout") +
                   " ttl " + this.props.getProperty("ttl");
        //cmd = "traceroute " + dst;

        this.log.info("traceroute", cmd);
        // run traceroute command
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader tracerouteOuput = 
            new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader tracerouteError =
        	new BufferedReader(new InputStreamReader(p.getErrorStream()));
        
        String errInfo = tracerouteError.readLine();
        if (errInfo != null )
        {
        	this.log.warn("error stream is: ", errInfo );
        	if ( errPattern.matcher(errInfo).matches())
        	{
                tracerouteOuput.close();
                tracerouteError.close();
        		throw new PathfinderException("Traceroute error: " + errInfo);
        	}
        }
    	
        // parse the results
        while ((hopInfo = tracerouteOuput.readLine()) != null) {
        	this.log.debug("hopinfo is ",hopInfo);
       
            this.rawHopData.add(hopInfo);

            Matcher m = ipPattern.matcher(hopInfo);
            if (m.matches()) {
                this.hops.add(m.group(1));
            }
        }
        tracerouteOuput.close();
        return src;
    }


    /**
     * Returns raw hop data for traceroute.
     *
     * @return ArrayList<String> of rawHopData
     */
    public ArrayList<String> getRawHopData() {
        return this.rawHopData;
    }

    /**
     * Returns traceroute hops.
     *
     * @return ArrayList<String> of hops
     */
    public ArrayList<String> getHops() {
        return this.hops;
    }
}
