package net.es.oscars.pathfinder.dragon;

import java.io.Serializable;

import net.es.oscars.database.HibernateBean;
import net.es.oscars.bss.topology.*;


/**
 * DragonLocalIdMap is adapted from a Middlegen class automatically generated 
 * from the schema for the topology.ipaddrs table.
 */
public class DragonLocalIdMap extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String ip;
    private String interdomainVlsr;
    private int number;
    private String type;

    /** persistent field */
    private Ipaddr vlsrIp;

    /** default constructor */
    public DragonLocalIdMap() { }

    /**
     * @return ip a string with the IP address
     */ 
    public String getIp() { return this.ip; }

    /**
     * @param ip a string with the IP address
     */ 
    public void setIp(String ip) { this.ip = ip; }
    
    /**
     * @return interdomainVlsr a string with the IP address of the interdomain VLSR
     */ 
    public String getInterdomainVlsr() { return this.interdomainVlsr; }

    /**
     * @param interdomainVlsr a string with the IP address of the interdomain VLSR
     */ 
    public void setInterdomainVlsr(String interdomainVlsr) { this.interdomainVlsr = interdomainVlsr; }

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
}
