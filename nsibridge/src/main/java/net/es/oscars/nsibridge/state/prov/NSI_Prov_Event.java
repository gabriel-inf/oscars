package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Prov_Event implements SM_Event {

    RECEIVED_NSI_PROV_RQ,
    LOCAL_PROV_CONFIRMED,
    LOCAL_PROV_FAILED,
    END_TIME,



    RECEIVED_NSI_REL_RQ,
    LOCAL_REL_CONFIRMED,
    LOCAL_REL_FAILED,


    LOCAL_SETUP_CONFIRMED,
    LOCAL_SETUP_FAILED,
    LOCAL_TEARDOWN_CONFIRMED,
    LOCAL_TEARDOWN_FAILED,

    RECEIVED_NSI_TERM_RQ,


}
