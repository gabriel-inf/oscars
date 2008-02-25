package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * RouteElem is the Hibernate bean for the bss.routeElems table.
 */
public class RouteElem extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** nullable persistent field */
    private String description;
    
    /** nullable persistent field */
    private boolean strict;
    
    /** persistent field */
    private Domain domain;
    
    /** persistent field */
    private Node node;
    
    /** persistent field */
    private Port port;
    
    /** persistent field */
    private Link link;

    /** nullable persistent field */
    private RouteElem nextHop;

    /** default constructor */
    public RouteElem() { }

    /**
     * @return description a string with this path element's description
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description a string with this path element's description
     */ 
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return description a string with this path element's description
     */ 
    public boolean isStrict() { return this.strict; }

    /**
     * @param description a string with this path element's description
     */ 
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * @return nextHop the next path element (uses association)
     */ 
    public RouteElem getNextHop() { return this.nextHop; }

    /**
     * @param nextHop the next path element (uses association)
     */ 
    public void setNextHop(RouteElem nextHop) { this.nextHop = nextHop; }
    
    /**
     * @return domain domain instance associated with this path element
     */ 
    public Domain getDomain() { return this.domain; }

    /**
     * @param domain domain instance associated with this path element
     */ 
    public void setDomain(Domain domain) { this.domain = domain; }
    
    /**
     * @return node node instance associated with this path element
     */ 
    public Node getNode() { return this.node; }

    /**
     * @param node node instance associated with this path element
     */ 
    public void setNode(Node node) { this.node = node; }
    
    /**
     * @return port port instance associated with this path element
     */ 
    public Port getPort() { return this.port; }

    /**
     * @param port port instance associated with this path element
     */ 
    public void setPort(Port port) { this.port = port; }
    
    /**
     * @return link link instance associated with this path element
     */ 
    public Link getLink() { return this.link; }

    /**
     * @param link link instance associated with this path element
     */ 
    public void setLink(Link link) { this.link = link; }


    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        RouteElem castOther = (RouteElem) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            // decided not to check nextHop; would lead to checking
            // entire remaining path for each element in path
            return new EqualsBuilder()
                .append(this.getDomain(), castOther.getDomain())
                .append(this.getNode(), castOther.getNode())
                .append(this.getPort(), castOther.getPort())
                .append(this.getLink(), castOther.getLink())
                .append(this.getDescription(), castOther.getDescription())
                .append(this.getNextHop(), castOther.getNextHop())
                .append(this.isStrict(), castOther.isStrict())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
