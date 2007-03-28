package net.es.oscars.pathfinder.dragon;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import net.es.oscars.bss.topology.*;


/**
 * DragonLocalIdMap is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.ipaddrs table.
 */
public class DragonLocalIdMap implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String ip;
    private int number;
    private String type;

    /** persistent field */
    private Ipaddr vlsrIp;

    /** default constructor */
    public DragonLocalIdMap() { }

    /**
     * Auto generated getter method.
     * @return id primary key in the ipaddrs table
     */ 
    public Integer getId() { return this.id; }

    /**
     * Auto generated setter method.
     * @param id primary key in the ipaddrs table
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * Auto generated getter method.
     * @return ip a string with the IP address
     */ 
    public String getIp() { return this.ip; }

    /**
     * Auto generated setter method.
     * @param ip a string with the IP address
     */ 
    public void setIp(String ip) { this.ip = ip; }

    /**
     * @return local id number
     */ 
    public int getNumber() { return this.number; }

    /**
     * @param number to set
     */ 
    public void setNumber(int number) { this.number = number; }

    /**
     * @return the type of local id (tagged or untagged port or group)
     */ 
    public String getType() { return this.type; }

    /**
     * @param type a string with this local id's type
     */ 
    public void setType(String type) {
        this.type = type;
    }


    /**
     * @return ipaddr of vlsr that controls the switch to which host is connected
     */ 
    public Ipaddr getVlsrIp() { return this.vlsrIp; }

    /**
     * @param vlsrIp an Ipaddr instance of vlsr that controls the switch to which host is connected
     */ 
    public void setVlsrIp(Ipaddr vlsrIp) {
        this.vlsrIp = vlsrIp;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof DragonLocalIdMap) ) return false;
        DragonLocalIdMap castOther = (DragonLocalIdMap) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
