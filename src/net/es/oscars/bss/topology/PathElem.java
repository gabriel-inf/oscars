package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.BeanUtils;

/**
 * PathElem is the Hibernate bean for the bss.pathElems table.
 */
public class PathElem extends BeanUtils implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean loose;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Ipaddr ipaddr;

    /** nullable persistent field */
    private PathElem nextElem;

    /** default constructor */
    public PathElem() { }

    /**
     * @return loose a boolean indicating whether this entry is loose or strict
     */ 
    public boolean isLoose() { return this.loose; }

    /**
     * @param loose a boolean indicating whether this entry is loose or strict
     */ 
    public void setLoose(boolean loose) { this.loose = loose; }


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
     * @return nextElem the next path element (uses association)
     */ 
    public PathElem getNextElem() { return this.nextElem; }

    /**
     * @param nextElem the next path element (uses association)
     */ 
    public void setNextElem(PathElem nextElem) { this.nextElem = nextElem; }


    /**
     * @return ipaddr ipaddr instance associated with this path element
     */ 
    public Ipaddr getIpaddr() { return this.ipaddr; }

    /**
     * @param ipaddr ipaddr instance associated with this path element
     */ 
    public void setIpaddr(Ipaddr ipaddr) { this.ipaddr = ipaddr; }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
