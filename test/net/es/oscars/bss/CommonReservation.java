package net.es.oscars.bss;

import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.CommonParams;

/**
 * This class sets the fields for a layer 2 reservation that are
 * available before scheduling
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class CommonReservation {
    private Properties props;
    private final Long BANDWIDTH = 25000000L;  // 25 Mbps
    private final int DURATION = 240;

    public CommonReservation () {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

    public void setParameters(Reservation resv, String description) {

        Long seconds = System.currentTimeMillis()/1000;
        resv.setStartTime(seconds);
        resv.setCreatedTime(seconds);
        seconds += DURATION;
        resv.setEndTime(seconds);

        resv.setBandwidth(BANDWIDTH);
        resv.setDescription(description);
        resv.setStatus("TEST");
        resv.setLogin(this.props.getProperty("login"));
    }
}
