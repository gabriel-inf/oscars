package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
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
    private PathElem pathElem;

    /** nullable persistent field */
    private Vlan vlan;

    /** nullable persistent field */
    private Domain nextDomain;

    private Set reservations;

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


    /**
     * @return vlan the reservation's associated vlan with tag, if any
     */ 
    public Vlan getVlan() { return this.vlan; }

    /**
     * @param vlan the reservation's associated vlan with tag, if any
     */ 
    public void setVlan(Vlan vlan) {
        this.vlan = vlan;
    }


    public void setReservations(Set reservations) {
        this.reservations = reservations;
    }

    public Set getReservations() {
        return this.reservations;
    }

    public void addReservation(Reservation resv) {
        resv.setPath(this);
        this.reservations.add(resv);
    }


    /**
     * @return path starting path instance associated with reservation
     */ 
    public Domain getNextDomain() { return this.nextDomain; }

    /**
     * @param path path instance to associate with this reservation
     */ 
    public void setNextDomain(Domain domain) { this.nextDomain = domain; }


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
                .append(this.getVlan(), castOther.getVlan())
                .isEquals();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        PathElem pathElem  = this.getPathElem();
        while (pathElem != null) {
            sb.append(pathElem.getIpaddr().getIP() + ", ");
            pathElem = pathElem.getNextElem();
        }
        String pathStr = sb.toString();
        return pathStr.substring(0, pathStr.length() - 2);
    }
}
