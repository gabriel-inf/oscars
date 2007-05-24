package net.es.oscars.pathfinder;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * CommonPathElem stores a hop that is not Axis2 or Hibernate dependent.
 * It is used during all steps of reservation creation up to persistence
 * to the database, at which point it is converted to a PathElem.
 */
public class CommonPathElem {

    private boolean loose;
    private String description;
    private String ip;

    /** default constructor */
    public CommonPathElem() { }

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
     * @return ip a string with this path element's IP address
     */ 
    public String getIP() { return this.ip; }

    /**
     * @param ip a string with this path element's IP address
     */ 
    public void setIP(String ip) {
        this.ip = ip;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("loose", isLoose())
            .append("description", getDescription())
            .append("ip", getIP())
            .toString();
    }
}
