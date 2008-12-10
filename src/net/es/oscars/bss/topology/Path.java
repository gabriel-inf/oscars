package net.es.oscars.bss.topology;

import java.util.*;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.BSSException;
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
    private String pathType;

    /** nullable persistent field */
    private int priority;

    /** nullable persistent field */
    private Domain nextDomain;

    /** nullable persistent field */
    private String direction;

    /** nullable persistent field */
    private String grouping;

    /** transient field */
    private String pathHopType;

    private List<PathElem> pathElems = new ArrayList<PathElem>();
    private Set layer2DataSet = new HashSet<Layer2Data>();
    private Set layer3DataSet = new HashSet<Layer3Data>();
    private Set mplsDataSet = new HashSet<MPLSData>();

    /** default constructor */
    public Path() { }

    /**
     * @return explicit boolean indicating whether this path was explicitly set
     */
    public boolean isExplicit() { return this.explicit; }

    /**
     * @param explicit boolean indicating whether this path was explicitly set
     */
    public void setExplicit(boolean explicit) { this.explicit = explicit; }


    /**
     * @return path starting path instance associated with reservation
     */
    public Domain getNextDomain() { return this.nextDomain; }

    /**
     * @param domain Domain instance to associate with this reservation
     */
    public void setNextDomain(Domain domain) { this.nextDomain = domain; }

    /**
     * @return the way this reservation will be set up.
     */
    public String getPathSetupMode() { return this.pathSetupMode; }

    /**
     * @param pathSetupMode the way this reservation will be set up.
     */
    public void setPathSetupMode(String pathSetupMode) {
        this.pathSetupMode = pathSetupMode;
    }

    /**
     * @return int with priority of this path
     */
    public int getPriority() { return this.priority; }

    /**
     * @param priority int with priority of this path
     */
    public void setPriority(int priority) { this.priority = priority; }


    /**
     * @return path type (currently intra or inter)
     */
    public String getPathType() { return this.pathType; }

    /**
     * @param pathType path type (currently intra or inter)
     */
    public void setPathType(String pathType) throws BSSException {
        if (!PathType.isValid(pathType)) {
            throw new BSSException("Invalid pathType: " + pathType);
        }
        this.pathType = pathType;
    }

    /**
     * @return the direction of the path.
     */
    public String getDirection() { return this.direction; }

    /**
     * @param direction the direction of the path.
     */
    public void setDirection(String direction) throws BSSException {
        // Hibernate does a setter for all fields, even if null, upon creation
        // don't need to do this on pathType because it cannot be null
        if (direction == null) {
            return;
        }
        if (!PathDirection.isValid(direction)) {
            throw new BSSException("Invalid direction: "+direction);
        }
        this.direction = direction;
    }

    /**
     * @return future use.
     */
    public String getGrouping() { return this.grouping; }

    /**
     * @param grouping future use.
     */
    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    /**
     * @return list of elements in this path.
     */
    public List<PathElem> getPathElems() {
        return this.pathElems;
    }

    /**
     * @param pathElems list of new path elements.  NOTE:  Don't use after
     *                  path has been made persistent.
     */
    public void setPathElems(List<PathElem> pathElems) {
        this.pathElems = pathElems;
    }

    /**
     * @param pathElem new path element in list, can only be added sequentially.
     */
    public void addPathElem(PathElem pathElem) {
        this.pathElems.add(pathElem);
    }

    public Set getLayer2DataSet() {
        return this.layer2DataSet;
    }

    public void setLayer2DataSet(Set layer2DataSet) {
        this.layer2DataSet = layer2DataSet;
    }

    public Set getLayer3DataSet() {
        return this.layer3DataSet;
    }

    public void setLayer3DataSet(Set layer3DataSet) {
        this.layer3DataSet = layer3DataSet;
    }

    public Set getMplsDataSet() {
        return this.mplsDataSet;
    }

    public void setMplsDataSet(Set mplsDataSet) {
        this.mplsDataSet = mplsDataSet;
    }

    // NOTE:  The following are a set of kludges to have a one-to-one
    //    mapping along with the cascade option "delete-orphan".
    //    Do not make the mapping unique in the configuration file on the *Data
    //    side.  Hibernate does saves before deletes, so you'll get a duplicate
    //    key error.
    public Layer2Data getLayer2Data() {
        if (this.layer2DataSet.isEmpty()) {
            return null;
        } else {
            return (Layer2Data) this.layer2DataSet.iterator().next();
        }
    }

    public void setLayer2Data(Layer2Data layer2Data) {
        if (layer2Data == null) {
            return;
        }
        if (!this.layer2DataSet.isEmpty()) {
            this.layer2DataSet.clear();
        }
        this.layer2DataSet.add(layer2Data);
    }

    public Layer3Data getLayer3Data() {
        if (this.layer3DataSet.isEmpty()) {
            return null;
        } else {
            return (Layer3Data) this.layer3DataSet.iterator().next();
        }
    }

    public void setLayer3Data(Layer3Data layer3Data) {
        if (layer3Data == null) {
            return;
        }
        if (!this.layer3DataSet.isEmpty()) {
            this.layer3DataSet.clear();
        }
        this.layer3DataSet.add(layer3Data);
    }

    public MPLSData getMplsData() {
        if (this.mplsDataSet.isEmpty()) {
            return null;
        } else {
            return (MPLSData) this.mplsDataSet.iterator().next();
        }
    }

    public void setMplsData(MPLSData mplsData) {
        if (mplsData == null) {
            return;
        }
        if (!this.mplsDataSet.isEmpty()) {
            this.mplsDataSet.clear();
        }
        this.mplsDataSet.add(mplsData);
    }

    // end kludged section
    

    /**
     * @return pathHopType String with WSDL path type (transient) 
     */
    public String getPathHopType() { return this.pathHopType; }

    /**
     * @param pathHopType String with WSDL path type
     */
    public void setPathHopType(String pathHopType) {
        this.pathHopType = pathHopType;
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
                .append(this.getPathElems().get(0), castOther.getPathElems().get(0))
                .append(this.getNextDomain(), castOther.getNextDomain())
                .isEquals();
        }
    }

    public boolean containsAnyOf(List<Link> links) {
        for (Link link : links) {
            for (PathElem pe: this.pathElems) {
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
