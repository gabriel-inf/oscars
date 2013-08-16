package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Resv_Event implements SM_Event {
    RECEIVED_NSI_RESV_RQ,
    RECEIVED_NSI_RESV_AB,
    RECEIVED_NSI_RESV_CM,

    RESV_TIMEOUT,

    LOCAL_TIMEOUT,

    LOCAL_RESV_CHECK_CF,
    LOCAL_RESV_CHECK_FL,
    LOCAL_RESV_COMMIT_CF,
    LOCAL_RESV_COMMIT_FL,
    LOCAL_RESV_ABORT_CF,
    LOCAL_RESV_ABORT_FL,


}
