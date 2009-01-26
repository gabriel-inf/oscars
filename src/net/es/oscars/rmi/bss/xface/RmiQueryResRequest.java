package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

/**
 * Bean containing information for a RMI request to query reservation with
 * a given global reservation id.
 */
public class RmiQueryResRequest implements Serializable {
    private static final long serialVersionUID = 50;

    private String gri;     // find reservation with this global reservation id

    public RmiQueryResRequest() {
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
}
