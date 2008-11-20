package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.hibernate.Hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * PathElem is the Hibernate bean for the bss.pathElems table.
 */
public class PathElem extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private int seqNumber;
    
    /** nullable persistent field */
    private String urn;
    
    /** nullable persistent field */
    private String userName;
    
    /** nullable persistent field */
    private String description;
    
    /** nullable persistent field */
    private String linkDescr;
    
    /** persistent field */
    private Link link;

    /** default constructor */
    public PathElem() { }


    /**
     * @return seqNumber int with this path element's position in list
     */ 
    public int getSeqNumber() {
        return this.seqNumber;
    }

    /**
     * @param num not actually settable
     */ 
    public void setSeqNumber(int num) {
    }


    /**
     * @return urn a string with this path element's associated urn
     */ 
    public String getUrn() { return this.urn; }

    /**
     * @param urn string with path element's associated urn
     */ 
    public void setUrn(String urn) {
        this.urn = urn;
    }


    /**
     * @return userName a string with element's associated user name
     */ 
    public String getUserName() { return this.userName; }

    /**
     * @param userName string with path element's associated user name
     */ 
    public void setUserName(String userName) {
        this.userName = userName;
    }


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
     * @return linkDescr a string with the associated link's description
     */ 
    public String getLinkDescr() { return this.linkDescr; }

    /**
     * @param linkDescr a string with the associated link's description
     */ 
    public void setLinkDescr(String linkDescr) {
        this.linkDescr = linkDescr;
    }


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
        PathElem castOther = (PathElem) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            // decided not to check nextElem; would lead to checking
            // entire remaining path for each element in path
            return new EqualsBuilder()
                .append(this.getLink(), castOther.getLink())
                .append(this.getDescription(), castOther.getDescription())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
