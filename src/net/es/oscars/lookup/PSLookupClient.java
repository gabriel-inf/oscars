package net.es.oscars.lookup;

import java.util.*;
import org.apache.log4j.*;

import edu.internet2.perfsonar.dcn.DCNLookupClient;
import net.es.oscars.PropHandler;


/**
 * Class used to retrieve the URNs assocaited with a given hostname
 * via the perfSONAR Lookup Service
 *
 * @author Andrew Lake
 */
public class PSLookupClient {
    private Logger log;
    private Properties props;
    private DCNLookupClient client;
    
    /** Contructor */
    public PSLookupClient() throws LookupException {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("lookup", true);
        String hints = this.props.getProperty("hints");
        String[] gLSs = null;
        String[] hLSs = null;
        
        int i = 0;
        ArrayList<String> gLSList = new ArrayList<String>();
        while(this.props.getProperty("global." + i) != null){
            gLSList.add(this.props.getProperty("global." + i));
            i++;
        }
        if(!gLSList.isEmpty()){
            String [] temp = new String[1];
            gLSList.toArray(temp);
            gLSs = temp;
        }

        i = 0;
        ArrayList<String> hLSList = new ArrayList<String>();
        while(this.props.getProperty("home." + i) != null){
            hLSList.add(this.props.getProperty("home." + i));
            i++;
        }
        if(!hLSList.isEmpty()){
            String [] temp = new String[1];
            hLSList.toArray(temp);
            hLSs = temp;
        }

        try {
            if(gLSs != null || hLSs != null){
                this.client = new DCNLookupClient(gLSs, hLSs);
            }else if(hints != null){
                this.client = new DCNLookupClient(hints);
            }else{
                throw new LookupException("Cannot initialize perfSONAR lookup client " +
                    "because missing required properties. Please set " +
                    "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            }
        } catch (Exception e) {
            this.client = null;
        }

        if (this.client == null) {
            throw new LookupException("Cannot initialize perfSONAR lookup client");
        }

        String useGlobals = this.props.getProperty("useGlobal");
        if(useGlobals != null){
            this.client.setUseGlobalLS(("1".equals(useGlobals) || "true".equals(useGlobals)));
        }
    }

    /**
     * Retieves the URN for a given hostname from the perfSONAR
     * Lookup Service. The URL of the service is defined in oscars.properties.
     *
     * @param hostname a String containing the hostname of the URN to lookup
     * @return String of URN found. null if URN is not found by Lookup Service.
     * throws BSSException
     */
    public String lookup(String hostname) throws LookupException {
        if(this.client == null){
            this.log.error("Cannot use perfSONAR lookup client " +
                "because missing required properties. Please set " +
                "lookup.hints, lookup.global.1 or lookup.home.1 in oscars.properties");
            throw new LookupException("Cannot lookup " + hostname + 
                    " because lookup client not intialized");
        }
        String urn = null;
        try{
            urn =this.client.lookupHost(hostname);
        }catch(Exception e){
            throw new LookupException(e.getMessage());
        }
        return urn;
    }
}

