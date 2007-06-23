package net.es.oscars.pathfinder;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Stores information about layer 2 port id's.
 */
public class EdgeInfo {

    private String portType;
    private String portValue;

    /** default constructor */
    public EdgeInfo() { }

    /**
     * @return portType a string with the port id's type
     */ 
    public String getPortType() { return this.portType; }

    /**
     * @param portType a string with the port id's type
     */ 
    public void setPortType(String portType) {
        this.portType = portType;
    }


    /**
     * @return portValue a string with the port id's value
     */ 
    public String getPortValue() { return this.portValue; }

    /**
     * @param portValue a string with the port id's value
     */ 
    public void setPortValue(String portValue) {
        this.portValue = portValue;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("portType", getPortType())
            .append("portValue", getPortValue())
            .toString();
    }
}
