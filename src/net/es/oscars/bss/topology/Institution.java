package net.es.oscars.bss.topology;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * Institution is the Hibernate bean for the bss.institutions table.
 */
public class Institution extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String name;

    /** persistent field */
    private Domain domain;

    /** default constructor */
    public Institution() { }


    /**
     * @return name a string with the institution name
     */
    public String getName() { return this.name; }

    /**
     * @param name a string with the institution name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return domain a Domain instance (uses association)
     */
    public Domain getDomain() { return this.domain; }

    /**
     * @param domain a Domain instance (uses association)
     */
    public void setDomain(Domain domain) { this.domain = domain; }

    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Institution castOther = (Institution) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getName(), castOther.getName())
                .append(this.getDomain(), castOther.getDomain())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
