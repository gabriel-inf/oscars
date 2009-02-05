package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

/**
 * Bean containing information for a RMI request to handle forcibly modifying
 * the status of a reservation with a given global reservation id.
 */
public class RmiModifyStatusRequest implements Serializable {
    private static final long serialVersionUID = 50;

    private String gri;     // reservation's global reservation id

    private String status;   // token identifying reservation

    public RmiModifyStatusRequest() {
    }

    /**
     * @return global reservation id
     */
    public String getGlobalReservationId() {
        return this.gri;
    }

    /**
     * @param gri string with reservation's global reservation id
     */
    public void setGlobalReservationId(String gri) {
        this.gri = gri;
    }

    /**
     * @return string with status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * @param status string with desired status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
