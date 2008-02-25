package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * InterdomainRoute is the Hibernate bean for the bss.interdomainRoutes table.
 */
public class InterdomainRoute extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;
    
    /** nullable persistent field */
    private Node srcNode;

    /** nullable persistent field */
    private Port srcPort;
    
    /** nullable persistent field */
    private Link srcLink;
    
    /** nullable persistent field */
    private Domain destDomain;
    
    /** nullable persistent field */
    private Node destNode;

    /** nullable persistent field */
    private Port destPort;
    
    /** nullable persistent field */
    private Link destLink;
    
    /** nullable persistent field */
    private RouteElem routeElem;
    
    /** persistent field */
    private int preference;
    
    /** persistent field */
    private boolean defaultRoute;
    
    /** default constructor */
    public InterdomainRoute() { }
    
    /**
     * @return the node that the source must match for this path to be used
     */ 
    public Node getSrcNode() { return this.srcNode; }

    /**
     * @param srcNode the node that the src must match for this path to be used
     */ 
    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;
    }
    
    /**
     * @return the port that the source must match for this path to be used
     */ 
    public Port getSrcPort() { return this.srcPort; }

    /**
     * @param srcPort the port that the src must match for this path to be used
     */ 
    public void setSrcPort(Port srcPort) {
        this.srcPort = srcPort;
    }
    
    /**
     * @return the link that the source must match for this path to be used
     */ 
    public Link getSrcLink() { return this.srcLink; }

    /**
     * @param srcLink the link that the src must match for this path to be used
     */ 
    public void setSrcLink(Link srcLink) {
        this.srcLink = srcLink;
    }
    
    /**
     * @return the domain that can be reached using this entry's route
     */ 
    public Domain getDestDomain() { return this.destDomain; }

    /**
     * @param destDomain the domain that can be reached with this entry's route
     */ 
    public void setDestDomain(Domain destDomain) {
        this.destDomain = destDomain;
    }
    
    /**
     * @return the node that can be reached using this entry's route
     */ 
    public Node getDestNode() { return this.destNode; }

    /**
     * @param destNode the node that can be reached using this entry's route
     */ 
    public void setDestNode(Node destNode) {
        this.destNode = destNode;
    }
    
    /**
     * @return the port that can be reached using this entry's route
     */ 
    public Port getDestPort() { return this.destPort; }

    /**
     * @param destPort the port that can be reached using this entry's route
     */ 
    public void setDestPort(Port destPort) {
        this.destPort = destPort;
    }
    
    /**
     * @return the link that can be reached using this entry's route
     */ 
    public Link getDestLink() { return this.destLink; }

    /**
     * @param destLink the link that can be reached using this entry's route
     */ 
    public void setDestLink(Link destLink) {
        this.destLink = destLink;
    }
    
    /**
     * @return the first element of this entry's route
     */ 
    public RouteElem getRouteElem() { return this.routeElem; }

    /**
     * @param routeElem the first element of this entry's route
     */ 
    public void setRouteElem(RouteElem routeElem) {
        this.routeElem = routeElem;
    }
    
    /**
     * @return the path's preference when compared to similar paths
     */ 
    public int getPreference() { return this.preference; }

    /**
     * @param preference the path's preference when compared to similar paths
     */ 
    public void setPreference(int preference) {
        this.preference = preference;
    } 
    
    /**
     * @return A boolean indicating whether this is the default route
     */ 
    public boolean isDefaultRoute() { return this.defaultRoute; }

    /**
     * @param defaultRoute A boolean indicating whether this is the default route
     */ 
    public void setDefaultRoute(boolean defaultRoute) { this.defaultRoute = defaultRoute; }
    
    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        InterdomainRoute castOther = (InterdomainRoute) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getSrcNode(), castOther.getSrcNode())
                .append(this.getSrcPort(), castOther.getSrcPort())
                .append(this.getSrcLink(), castOther.getSrcLink())
                .append(this.getDestDomain(), castOther.getDestDomain())
                .append(this.getDestNode(), castOther.getDestNode())
                .append(this.getDestPort(), castOther.getDestPort())
                .append(this.getDestLink(), castOther.getDestLink())
                .append(this.getRouteElem(), castOther.getRouteElem())
                .append(this.getPreference(), castOther.getPreference())
                .append(this.isDefaultRoute(), castOther.isDefaultRoute())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
