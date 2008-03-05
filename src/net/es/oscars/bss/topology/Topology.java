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
            if ( TopologyUtil.getFQTI(d).equals(TopologyUtil.getFQTI(domain)) ) {
                found = true;
            }
        }
        if (!found) {
            this.domains.add(domain);
            return true;
        } else {
            return false;
        }
    }
}