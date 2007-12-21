package net.es.oscars.bss.topology;

import java.util.Set;
import java.util.List;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.Reservation;

/**
 * Path is the Hibernate bean for the bss.Paths table.
 */
public class Path extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private boolean explicit;
    
    /** persistent field */
    private String pathSetupMode;
    
    /** persistent field */
    private PathElem pathElem;
    
    /** nullable persistent field */
    private Domain nextDomain;

    /** nullable persistent field */
    private Layer2Data layer2Data;
    
    /** nullable persistent field */
    private Layer3Data layer3Data;
    
    /** nullable persistent field */
    private MPLSData mplsData;

    private Reservation reservation;

    /** default constructor */
    public Path() { }

    /**
     * @return pathElem the first path element (uses association)
     */ 
    public PathElem getPathElem() { return this.pathElem; }

    /**
     * @param pathElem the first path element (uses association)
     */ 
    public void setPathElem(PathElem pathElem) { this.pathElem = pathElem; }
    
    /**
     * @return explicit boolean indicating whether this path was explicitly set
     */ 
    public boolean isExplicit() { return this.explicit; }

    /**
     * @param explicit boolean indicating whether this path was explicitly set
     */ 
    public void setExplicit(boolean explicit) { this.explicit = explicit; }


    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return this.reservation;
    }

    public void setLayer2Data(Layer2Data layer2Data) {
        this.layer2Data = layer2Data;
    }

    public Layer2Data getLayer2Data() {
        return this.layer2Data;
    }

    public void setLayer3Data(Layer3Data layer3Data) {
        this.layer3Data = layer3Data;
    }

    public Layer3Data getLayer3Data() {
        return this.layer3Data;
    }

    public void setMplsData(MPLSData mplsData) {
        this.mplsData = mplsData;
    }

    public MPLSData getMplsData() {
        return this.mplsData;
    }

    /**
     * @return path starting path instance associated with reservation
     */ 
    public Domain getNextDomain() { return this.nextDomain; }

    /**
     * @param domain Domain instance to associate with this reservation
     */ 
    public void setNextDomain(Domain domain) { this.nextDomain = domain; }
    
    /**
     * @return the way this reservation will be setup.
     */ 
    public String getPathSetupMode() { return this.pathSetupMode; }

    /**
     * @param pathSetupMode the way this reservation will be setup.
     */ 
    public void setPathSetupMode(String pathSetupMode) { 
        this.pathSetupMode = pathSetupMode; 
    }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Path castOther = (Path) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.isExplicit(), castOther.isExplicit())
                .append(this.getPathElem(), castOther.getPathElem())
                .append(this.getNextDomain(), castOther.getNextDomain())
                .isEquals();
        }
    }
    
    public boolean containsAnyOf(List<Link> links) {
    	PathElem pe = this.getPathElem();
    	for (Link link : links) {
	    	if (pe.getLink().equals(link)) {
	    		return true;
	    	} 
			while (pe.getNextElem() != null) {
				pe = pe.getNextElem();
		    	if (pe.getLink().equals(link)) {
		    		return true;
		    	} 
	    	}
    	}
    	return false;
    }

    // string representation is layer and client dependent; done
    // in bss/Utils.java
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
