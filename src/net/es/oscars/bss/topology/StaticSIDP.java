package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * StaticSIDP is the Hibernate bean for the bss.staticSIDP table.
 */
public class StaticSIDP extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** nullable persistent field */
    private String description;
    
    /** persistent field */
    private Link link;

    /** nullable persistent field */
    private StaticSIDP nextHop;

    /** default constructor */
    public StaticSIDP() { }

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
     * @return nextHop the next path element (uses association)
     */ 
    public StaticSIDP getNextHop() { return this.nextHop; }

    /**
     * @param nextHop the next path element (uses association)
     */ 
    public void setNextHop(StaticSIDP nextHop) { this.nextHop = nextHop; }
    
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
        StaticSIDP castOther = (StaticSIDP) o;
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
                .append(this.getLink(), castOther.getLink())
                .append(this.getDescription(), castOther.getDescription())
                .append(this.getNextHop(), castOther.getNextHop())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
