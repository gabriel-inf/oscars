package net.es.oscars.bss.topology;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

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
    private String name;

    /** persistent field */
    private String abbrev;

    /** persistent field */
    private String url;

    /** persistent field */
    private int asNum;

    /** persistent field */
    private boolean local;

    /** default constructor */
    public Domain() { }

    /**
     * @return asNum An int with the autonomous system number (TODO: check)
     */ 
    public int getAsNum() { return this.asNum; }

    /**
     * @param asNum An int with the autonomous system number (TODO: check)
     */ 
    public void setAsNum(int asNum) { this.asNum = asNum; }


    /**
     * @return name A String with the domain name
     */ 
    public String getName() { return this.name; }

    /**
     * @param name A String with the domain name
     */ 
    public void setName(String name) { this.name = name; }


    /**
     * @return abbrev A String with a locally defined abbreviation for domain
     */ 
    public String getAbbrev() { return this.abbrev; }

    /**
     * @param abbrev A String with a locally defined abbreviation for domain
     */ 
    public void setAbbrev(String abbrev) { this.abbrev = abbrev; }


    /**
     * @return name A String with the URL of the reservation server
     */ 
    public String getUrl() { return this.url; }

    /**
     * @param url A String with the URL of the reservation server
     */ 
    public void setUrl(String url) { this.url = url; }


    /**
     * @return local A boolean indicating whether this domain is the local one
     */ 
    public boolean isLocal() { return this.local; }

    /**
     * @param local A boolean indicating whether this domain is the local one
     */ 
    public void setLocal(boolean local) { this.local = local; }


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
                .append(this.getName(), castOther.getName())
                .append(this.getAbbrev(), castOther.getAbbrev())
                .append(this.getUrl(), castOther.getUrl())
                .append(this.getAsNum(), castOther.getAsNum())
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
