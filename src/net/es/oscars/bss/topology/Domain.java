package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * Domain is adapted from a Middlegen class automatically generated 
 * from the schema for the bss.domains table.
 */
public class Domain extends BeanUtils implements Serializable {
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


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
