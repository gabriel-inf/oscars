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

    /** nullable persistent field */
    private String description;
    
    /** nullable persistent field */
    private String linkDescr;
    
    /** persistent field */
    private Link link;

    /** nullable persistent field */
    private PathElem nextElem;

    private Path path;

    /** default constructor */
    public PathElem() { }

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
     * @return nextElem the next path element (uses association)
     */ 
    public PathElem getNextElem() { return this.nextElem; }

    /**
     * @param nextElem the next path element (uses association)
     */ 
    public void setNextElem(PathElem nextElem) { this.nextElem = nextElem; }
    
    /**
     * @return link link instance associated with this path element
     */ 
    public Link getLink() { return this.link; }

    /**
     * @param link link instance associated with this path element
     */ 
    public void setLink(Link link) { this.link = link; }


    /**
     * @return path return path associated with this path element;
     *              null except for first element in path
     */ 
    public Path getPath() { return this.path; }

    /**
     * @param path path instance associated with this path element
     */ 
    public void setPath(Path path) { this.path = path; }


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
    
    public PathElem copy(){
        PathElem pathElemCopy = new PathElem();
        PathElem nextElemCopy = null;
        Link linkCopy = null;
        
        pathElemCopy.setDescription(this.description);
        pathElemCopy.setLinkDescr(this.linkDescr);
        
        if(this.link != null){
            linkCopy = this.link.topoCopy();
        }
        pathElemCopy.setLink(linkCopy);
        
        if(this.nextElem != null){
            nextElemCopy = this.nextElem.copy();
        }
        pathElemCopy.setNextElem(nextElemCopy);
        
        return pathElemCopy;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
