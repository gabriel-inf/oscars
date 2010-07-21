package net.es.oscars.lookup;

public class LookupFactory {

    /** Constructor */
    public LookupFactory() {
    }

    public PSLookupClient getPSLookupClient() {
        PSLookupClient result;

        try {
            result = new PSLookupClient();
        } catch (Exception e) {
            return null;
        }

        return result;
    }

}
