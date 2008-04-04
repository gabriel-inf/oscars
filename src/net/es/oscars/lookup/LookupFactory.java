package net.es.oscars.lookup;
import java.util.Properties;

import net.es.oscars.PropHandler;

public class LookupFactory {
    private Properties props;


    /** Constructor */
    public LookupFactory() {
    }

    public PSLookupClient getPSLookupClient() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("lookup", true);
        String url = this.props.getProperty("url");
        String fname =  System.getenv("CATALINA_HOME") + "/shared/classes/server/perfSONAR-LSQuery.xml";
        PSLookupClient result = new PSLookupClient();
        result.setUrl(url);
        result.setFname(fname);
        return result;
    }

}