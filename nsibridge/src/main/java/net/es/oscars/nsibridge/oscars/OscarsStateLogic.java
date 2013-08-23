package net.es.oscars.nsibridge.oscars;

public class OscarsStateLogic {
    public static boolean isStateSteady(OscarsStates state) {
        switch (state) {
            case CREATED:
            case INPATHCALCULATION:
            case INCOMMIT:
            case INSETUP:
            case INTEARDOWN:
                return false;
            case RESERVED:
            case ACTIVE:
            case FAILED:
            case UNKNOWN:
            case CANCELLED:
                return true;
        }
        return false;
    }
}
