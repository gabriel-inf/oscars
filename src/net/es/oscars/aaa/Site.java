package net.es.oscars.aaa;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.Hibernate;

import net.es.oscars.database.HibernateBean;

/**
 * Site is the Hibernate bean for the aaa.sites table.
 */
public class Site extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String domainTopologyId;
    
    private Institution institution;

    /** default constructor */
    public Site() { }

    /**
     * @return a string with the domainTopologyIdentifier
     */
    public String getDomainTopologyId() { return this.domainTopologyId; }

    /**
     * @param domainTopologyId a string with the domainTopologyIdentifier
     */
    public void setDomainTopologyId(String domainTopologyId) {
        this.domainTopologyId = domainTopologyId;
    }
    /**
     * @return name a string with the domainTopologyIdentifier
     */
    public String getDomain() { return this.domainTopologyId; }

    /**
     * @param domain a string with the domainTopologyIdentifier
     */
    public void setDomain(String domain) {
        this.domainTopologyId = domain;
    }
    public Institution getInstitution(){
        return this.institution;
    }
    public void setInstitution(Institution inst){
        this.institution = inst;
    }
    // need to override superclass because dealing with transient
    // instances as well
    public boolean equals(Object o) {
        if (this == o) { return true; }
        Class thisClass = Hibernate.getClass(this);
        if (o == null || thisClass != Hibernate.getClass(o)) {
            return false;
        }
        Site castOther = (Site) o;
        // if both of these have been saved to the database
        if ((this.getId() != null) &&
            (castOther.getId() != null)) {
            return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
        } else {
            return new EqualsBuilder()
                .append(this.getDomainTopologyId(), castOther.getDomainTopologyId())
                .append(this.getInstitution(), castOther.getInstitution())
                .isEquals();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
