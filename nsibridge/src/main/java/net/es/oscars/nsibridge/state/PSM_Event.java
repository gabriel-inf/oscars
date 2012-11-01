package net.es.oscars.nsibridge.state;

import net.es.oscars.nsibridge.ifces.SM_Event;
/**
 * @haniotak Date: 2012-08-07
 */
public enum PSM_Event implements SM_Event {
    RSV_RQ,
    RSV_OK,
    RSV_FL,

    PROV_RQ,
    PROV_OK,
    PROV_FL,

    REL_RQ,
    REL_OK,
    REL_FL,

    ACT_OK,
    ACT_FL,

    TERM_RQ,


    FATAL_FAILURE,
    START_TIME,
    END_TIME,


    MOD_CHK_RQ,
    MOD_CHK_OK,
    MOD_CHK_FL,

    MOD_RQ,
    MOD_OK,
    MOD_FL,

    MOD_CNC_RQ,
    MOD_CNC_OK,
    MOD_CNC_FL,


}
