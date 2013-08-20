package net.es.oscars.nsibridge.oscars;

public enum OscarsStates {
    RESERVED,
    CANCELLED,
    ACTIVE,
    FAILED,
    UNKNOWN,

    CREATED,
    INPATHCALCULATION,
    INCOMMIT,
    INTEARDOWN,
    INSETUP,
}
