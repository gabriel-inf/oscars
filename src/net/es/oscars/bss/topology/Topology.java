package net.es.oscars.bss.topology;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import java.util.*;


public class Topology {
    private ArrayList<Domain> domains;

    public Topology() {
        this.domains = new ArrayList<Domain>();
    }
    public List<Domain> getDomains() {
        return this.domains;
    }

    public void setDomains(List<Domain> newDomains) {
        this.domains.clear();
        this.domains.addAll(newDomains);
    }

    public boolean addDomain(Domain domain) {
        boolean found = false;
        for (Domain d : this.domains) {
            if ( d.equalsTopoId(domain)) {
                found = true;
                break;
            }
        }
        if (!found) {
            this.domains.add(domain);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the given identifier is available in this topology
     * @return The element corresponding to the passed id or null if not found.
     */
    public Object lookupElement(String id) {
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(id);
	String domFQID = parseResults.get("domFQID");
	if (domFQID == null) {
            return null;
	}

        for (Domain d : this.domains) {
            if (domFQID.equals(d.getFQTI()))
                return d.lookupElement(id);
        }

        return null;
    }
}
