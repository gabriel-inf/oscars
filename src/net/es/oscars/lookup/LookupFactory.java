package net.es.oscars.lookup;
import java.util.Properties;

import net.es.oscars.PropHandler;

public class LookupFactory {
    private Properties props;


    /** Constructor */
    public LookupFactory() {
    }

    public PSLookupClient getPSLookupClient() {
        PSLookupClient result = new PSLookupClient();
        return result;
    }

}