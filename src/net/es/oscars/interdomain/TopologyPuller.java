package net.es.oscars.interdomain;

import net.es.oscars.client.Client;
import net.es.oscars.wsdlTypes.*;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;

/**
 * Client for pulling topology from a domain using a getNetworkTopology request
 */
public class TopologyPuller extends Client{
    /**
     * Constructor that sets the URL to an initial value. It also loads 
     * configuration files from CATALINA_HOME/shared/oscars.conf/axis2.repo
     * The URL may be changed using setUrl so that a client does not have to
     * be reinitialized when making requests to multiple locations
     *
     * @param url a string indicating the url of the server to contact
     * @throws InterdomainException
     */
    public TopologyPuller(String url) throws InterdomainException{
        String catalinaHome = System.getProperty("catalina.home");
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        String repo = catalinaHome + "shared/oscars.conf/axis2.repo/";
        
        System.setProperty("axis2.xml", repo + "axis2.xml");
        
        try {
            this.setUp(true, url, repo, repo + "axis2.xml");
        } catch (AxisFault af) {
            this.log.error("setup.axisFault: " + af.getMessage());
            throw new InterdomainException("Unable to create topology " + 
                "pull client");
        }
    }
    
    /**
     * Sets the URL of the server to contact to the given string
     *
     * @param url a string indicating the url of the server to contact
     */
    public void setUrl(String url){
        this.stub._getServiceClient().getOptions().setTo(
            new EndpointReference(url));
    }
}