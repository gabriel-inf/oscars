package net.es.oscars.bss.policy;

import net.es.oscars.bss.BSSException;

/**
 * This class creates a PolicyFilyer instance used in oversubscription checks. 
 */
public class PolicyFilterFactory {

    /**
     * Factory method.
     *
     * @return an instance of a class implementing the PolicyFilter interface.
     */
    public static PolicyFilter create(String filter) throws BSSException{
    
        if (filter.equals("vlanMap")) {
            return new VlanMapFilter();
        }
        
        throw new BSSException("Unrecognized policy filter specified '" + 
                               filter + "'");
    }
}
