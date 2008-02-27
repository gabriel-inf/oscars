package net.es.oscars.bss.topology;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateBean;

/**
 * Domain is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.domains table.
 */
public class Domain extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String topologyIdent;

    /** persistent field */
    private String name;

    /** persistent field */
    private String url;

    /** persistent field */
    private String abbrev;

    /** persistent field */
    private boolean local;
    
    private Set nodes;

    private Set paths;
    
    /** default constructor */
    public Domain() { }
    
    /** copy constructor */
    public Domain(Domain domain) {
    	this.name = domain.getName();
    	this.abbrev = domain.getAbbrev();
    	this.local = domain.isLocal();
    	this.nodes = domain.getNodes();
    	this.topologyIdent = domain.getTopologyIdent();
    	this.url = domain.getUrl();
    	this.paths = domain.getPaths();
    }

    /**
     * @return topologyIdent a string with the topology domain id (currently
     * autonomous system number)
     */ 
    public String getTopologyIdent() { return this.topologyIdent; }

    /**
     * @param topologyIdent a string with the topology domain id
     */ 
    public void setTopologyIdent(String topologyIdent) {
        this.topologyIdent = topologyIdent;
    }


    /**
     * @return name A String with the domain name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the domain name
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * @return name A String with the URL of the reservation server
     */ 
    public String getUrl() { return this.url; }

    /**
     * @param url A String with the URL of the reservation server
     */ 
    public void setUrl(String url) { this.url = url; }


    /**
     * @return abbrev A String with a locally defined abbreviation for domain
     */ 
    public String getAbbrev() { return this.abbrev; }

    /**
     * @param abbrev A String with a locally defined abbreviation for domain
     */ 
    public void setAbbrev(String abbrev) { this.abbrev = abbrev; }


    /**
     * @return local A boolean indicating whether this domain is the local one
     */ 
    public boolean isLocal() { return this.local; }

    /**
     * @param local A boolean indicating whether this domain is the local one
     */ 
    public void setLocal(boolean local) { this.local = local; }

    /**
     * @return list of nodes
     */ 
    public Set getNodes() { return this.nodes; }

    /**
     * @param nodes nodes to set
     */ 
    public void setNodes(Set nodes) { this.nodes = nodes; }
    
    /**
     * @return list of paths that have this as next domain
     */ 
    public Set getPaths() { return this.paths; }

    /**
     * @param paths probably never used
     */ 
    public void setPaths(Set paths) { this.paths = paths; }

    /**
     * @return list of edge links to other domains
     */ 
    public List<Link> getEdgeLinks() {
    	ArrayList<Link> edgeLinks = new ArrayList<Link>();
    	Iterator nodeIt;
    	Iterator portIt;
    	Iterator linkIt;
    	nodeIt = this.getNodes().iterator();
    	while (nodeIt.hasNext()) {
    		Node n = (Node) nodeIt.next();
    		portIt = n.getPorts().iterator();
    		while (portIt.hasNext()) {
    			Port p = (Port) portIt.next();
    			linkIt = p.getLinks().iterator();
    			while (linkIt.hasNext()) {
    				Link l = (Link) linkIt.next();
    				Link remLink = l.getRemoteLink();
    				if (remLink != null) {
    					Domain remoteDomain = remLink.getPort().getNode().getDomain();
    					if (!this.equals(remoteDomain)) {
    						edgeLinks.add(l);
    					}
    				}
    			}
    		}
    	}
		return edgeLinks;
    	
    }
    
    public boolean equalsTopoId(Domain domain) {
    	String thisFQTI = TopologyUtil.getFQTI(this);
    	String thatFQTI = TopologyUtil.getFQTI(domain);
    	return thisFQTI.equals(thatFQTI);
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Domain castOther = (Domain) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getTopologyIdent(), castOther.getTopologyIdent())
                .append(this.getName(), castOther.getName())
                .append(this.getUrl(), castOther.getUrl())
                .append(this.getAbbrev(), castOther.getAbbrev())
                .append(this.isLocal(), castOther.isLocal())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
