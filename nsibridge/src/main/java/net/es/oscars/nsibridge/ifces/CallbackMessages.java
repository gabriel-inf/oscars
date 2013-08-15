package net.es.oscars.nsibridge.ifces;

public enum CallbackMessages {
    ERROR,

    RESV_CF,
    RESV_FL,
    RESV_CM_CF,
    RESV_CM_FL,
    RESV_AB_CF,

    PROV_CF,
    REL_CF,
    TERM_CF,

    // query
    QUERY_NOT_CF,
    QUERY_NOT_FL,
    QUERY_REC_CF,
    QUERY_REC_FL,
    QUERY_SUM_CF,
    QUERY_SUM_FL,


    // notifications

    DATAPLANE_CHANGE,
    RESV_TIMEOUT,
    ERROR_EVENT,
    MSG_TIMEOUT

}
