package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * StaticLIDP is the Hibernate bean for the bss.staticLIDP table.
 */
public class StaticLIDP extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;
    
    /** persistent field */
    private Link localLink;
    
    /** nullable persistent field */
    private Domain destDomain;
    
    /** nullable persistent field */
    private Node destNode;

    /** nullable persistent field */
    private Port destPort;
    
    /** nullable persistent field */
    private Link destLink;
    
    /** persistent field */
    private boolean defaultRoute;
    
    /** default constructor */
    public StaticLIDP() { }

    /**
     * @return the local link used when forwarding to the matched value
     */ 
    public Link getLocalLink() { return this.localLink; }

    /**
     * @param localLink a link used when forwarding to the matched d,n,p, or l
     */ 
    public void setLocalLink(Link localLink) {
        this.localLink = localLink;
    }
    
    /**
     * @return the domain that can be reached using this entry's localLink
     */ 
    public Domain getDestDomain() { return this.destDomain; }

    /**
     * @param destDomain the domain that can be reached using this entry's localLink
     */ 
    public void setDestDomain(Domain destDomain) {
        this.destDomain = destDomain;
    }
    
    /**
     * @return the node that can be reached using this entry's localLink
     */ 
    public Node getDestNode() { return this.destNode; }

    /**
     * @param destNode the node that can be reached using this entry's localLink
     */ 
    public void setDestNode(Node destNode) {
        this.destNode = destNode;
    }
    
    /**
     * @return the port that can be reached using this entry's localLink
     */ 
    public Port getDestPort() { return this.destPort; }

    /**
     * @param destPort the port that can be reached using this entry's localLink
     */ 
    public void setDestPort(Port destPort) {
        this.destPort = destPort;
    }
    
    /**
     * @return the link that can be reached using this entry's localLink
     */ 
    public Link getDestLink() { return this.destLink; }

    /**
     * @param destLink the link that can be reached using this entry's localLink
     */ 
    public void setDestLink(Link destLink) {
        this.destLink = destLink;
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
        StaticLIDP castOther = (StaticLIDP) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getLocalLink(), castOther.getLocalLink())
                .append(this.getDestDomain(), castOther.getDestDomain())
                .append(this.getDestNode(), castOther.getDestNode())
                .append(this.getDestPort(), castOther.getDestPort())
                .append(this.getDestLink(), castOther.getDestLink())
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
