package net.es.oscars.rmi.bss.xface;

import java.io.Serializable;
import java.util.List;

/**
 * Bean containing information for a RMI request to handle path requests with
 * a given global reservation id.  Token is unused by some path requests.
 */
public class RmiPathRequest implements Serializable {
    private static final long serialVersionUID = 50;

    private String gri;     // reservation's global reservation id

    private String token;   // token identifying reservation

    public RmiPathRequest() {
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
     * @return string with token
     */
    public String getToken() {
        return this.token;
    }

    /**
     * @param token string with reservation's token
     */
    public void setToken(String token) {
        this.token = token;
    }
}
