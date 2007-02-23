package net.es.oscars.bss;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Domain is adapted from a Middlegen class automatically generated 
 * from the schema for the oscars.domains table.
 */
public class Domain implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

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
     * @return id An Integer with the primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id An Integer with the primary key
     */ 
    public void setId(Integer id) { this.id = id; }


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


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof Domain) ) return false;
        Domain castOther = (Domain) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
