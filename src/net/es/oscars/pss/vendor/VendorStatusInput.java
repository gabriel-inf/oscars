package net.es.oscars.pss.vendor;

import java.util.Set;
import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * This class is a bean passed from scheduler.VendorMaintainStatusJob to
 * VendorCheckStatusJob, and from there to the vendor-specific statusLSP
 * methods.
 */
public class VendorStatusInput implements Serializable {
    // arbitrary at this point
    private static final long serialVersionUID = 1;

    private String gri;
    private String description;
    private String layer;
    private String operation;
    private String direction;
    private String desiredStatus;

    /** default constructor */
    public VendorStatusInput() { }

    /**
     * @return gri a string with the associated reservation's GRI
     */
    public String getGri() { return this.gri; }

    /**
     * @param gri a string with a reservation's GRI
     */
    public void setGri(String gri) {
        this.gri = gri;
    }

    /**
     * @return description a string with the reservation's description 
     */
    public String getDescription() { return this.description; }

    /**
     * @param description a string with the reservation's description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return layer a string with the circuit layer (2 or e)
     */
    public String getLayer() { return this.layer; }

    /**
     * @param op a string with the circuit layer
     */
    public void setLayer(String layer) {
        this.layer = layer;
    }

    /**
     * @return operation a string with the operation (setup or teardown)
     */
    public String getOperation() { return this.operation; }

    /**
     * @param op a string with the operation
     */
    public void setOperation(String op) {
        this.operation = op;
    }

    /**
     * @return direction a string with the direction (forward or reverse)
     */
    public String getDirection() { return this.direction; }

    /**
     * @param direction a string with the circuit direction
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * @return desiredStatus a string with the reservation's desired status
     */
    public String getDesiredStatus() { return this.desiredStatus; }

    /**
     * @param status a string with the desired statuus
     */
    public void setDesiredStatus(String status) {
        this.desiredStatus = status;
    }
}
