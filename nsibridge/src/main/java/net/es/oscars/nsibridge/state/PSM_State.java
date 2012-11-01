package net.es.oscars.nsibridge.state;

import net.es.oscars.nsibridge.ifces.SM_State;

/**
 * @haniotak Date: 2012-08-07
 */
public enum PSM_State implements SM_State {
    INITIAL,
    RESERVING,
    RESERVED,
    PROVISIONING,
    PROVISIONED,
    ACTIVATING,
    SCHEDULED,
    ACTIVATED,
    RELEASING,
    TERMINATED
}
