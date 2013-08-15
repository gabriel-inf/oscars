package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Resv_Event implements SM_Event {
    RECEIVED_NSI_RESV_RQ,
    RECEIVED_NSI_RESV_AB,
    RECEIVED_NSI_RESV_CM,
    LOCAL_RESV_CONFIRMED,
    LOCAL_RESV_FAILED,
    LOCAL_RESV_ABORTED,


}
