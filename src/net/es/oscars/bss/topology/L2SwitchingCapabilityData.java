package net.es.oscars.bss.topology;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.HibernateBean;

/**
 * This is a Hibernate bean mapping the bss.l2SwitchingCapabilityData table
 */
public class L2SwitchingCapabilityData
        extends HibernateBean implements Serializable {

    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private Link link;

    /** persistent field */
    private String vlanRangeAvailability;

    /** persistent field */
    private int interfaceMTU;
    
    /** persistent field */
    private boolean vlanTranslation;
    
    /** default constructor */
    public L2SwitchingCapabilityData() { }


    /**
     * @return link edge link instance 
     */ 
    public Link getLink() { return this.link; }

    /**
     * @param link edge link instance
     */ 
    public void setLink(Link link) { this.link = link; }



    /**
     * @return vlanRangeAvailability string with range of available VLAN tags
     */ 
    public String getVlanRangeAvailability() {
        return this.vlanRangeAvailability;
    }

    /**
     * @param vlanRangeAvailability string with range of available VLAN tags
     */ 
    public void setVlanRangeAvailability(String vlanRangeAvailability) {
        this.vlanRangeAvailability = vlanRangeAvailability;
    }


    /**
     * @return interfaceMTU  interface MTU value
     */ 
    public int getInterfaceMTU() {
        return this.interfaceMTU;
    }

    /**
     * @param interfaceMTU string with range of available VLAN tags
     */ 
    public void setInterfaceMTU(int interfaceMTU) {
        this.interfaceMTU = interfaceMTU;
    }
    
    /**
     * @return the vlanTranslation value
     */ 
    public boolean getVlanTranslation() {
        return this.vlanTranslation;
    }

    /**
     * @param vlanTranslation boolean indicating the ability to translate VLANs
     */ 
    public void setVlanTranslation(boolean vlanTranslation) {
        this.vlanTranslation = vlanTranslation;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }
}
