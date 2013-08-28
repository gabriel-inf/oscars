package net.es.oscars.nsibridge.oscars;

import org.apache.log4j.Logger;

import java.util.HashMap;

public class OscarsStateLogic {
    private static final Logger log = Logger.getLogger(OscarsStateLogic.class);
    // for each state & each operation combination
    // if YES, I can perform this operation now
    // if NO, I can not perform this operation now
    // if ASK_LATER, I need to wait and ask again
    private static HashMap<OscarsStates, HashMap<OscarsOps, OscarsLogicAction>> allow;

    // for each state & each operation combination
    // if YES, this operation is necessary
    // if NO, this operation is not necessary
    // if ASK_LATER, I need to wait and ask again
    private static HashMap<OscarsStates, HashMap<OscarsOps, OscarsLogicAction>> need;

    static {
        allow = new HashMap<OscarsStates, HashMap<OscarsOps, OscarsLogicAction>>();
        // can't reserve on reserved
        // can't tear down if not set up
        HashMap<OscarsOps, OscarsLogicAction> resvAllowedOps = new HashMap<OscarsOps, OscarsLogicAction>();
        resvAllowedOps.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        resvAllowedOps.put(OscarsOps.MODIFY, OscarsLogicAction.YES);
        resvAllowedOps.put(OscarsOps.SETUP, OscarsLogicAction.YES);
        resvAllowedOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.NO);
        resvAllowedOps.put(OscarsOps.CANCEL, OscarsLogicAction.YES);
        allow.put(OscarsStates.RESERVED, resvAllowedOps);

        HashMap<OscarsOps, OscarsLogicAction> unsubOps = new HashMap<OscarsOps, OscarsLogicAction>();
        unsubOps.put(OscarsOps.RESERVE, OscarsLogicAction.YES);
        unsubOps.put(OscarsOps.MODIFY, OscarsLogicAction.NO);
        unsubOps.put(OscarsOps.SETUP, OscarsLogicAction.NO);
        unsubOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.NO);
        unsubOps.put(OscarsOps.CANCEL, OscarsLogicAction.NO);
        allow.put(OscarsStates.UNSUBMITTED, unsubOps);

        // can't reserve on reserved, must modify
        // can't set up if active
        HashMap<OscarsOps, OscarsLogicAction> actAllowedOps = new HashMap<OscarsOps, OscarsLogicAction>();
        actAllowedOps.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        actAllowedOps.put(OscarsOps.MODIFY, OscarsLogicAction.YES);
        actAllowedOps.put(OscarsOps.SETUP, OscarsLogicAction.NO);
        actAllowedOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.YES);
        actAllowedOps.put(OscarsOps.CANCEL, OscarsLogicAction.YES);
        allow.put(OscarsStates.ACTIVE, actAllowedOps);

        // wait until status stabilizes
        HashMap<OscarsOps, OscarsLogicAction> waitOps = new HashMap<OscarsOps, OscarsLogicAction>();
        waitOps.put(OscarsOps.RESERVE, OscarsLogicAction.ASK_LATER);
        waitOps.put(OscarsOps.MODIFY, OscarsLogicAction.ASK_LATER);
        waitOps.put(OscarsOps.SETUP, OscarsLogicAction.ASK_LATER);
        waitOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.ASK_LATER);
        waitOps.put(OscarsOps.CANCEL, OscarsLogicAction.ASK_LATER);
        allow.put(OscarsStates.INSETUP, waitOps);
        allow.put(OscarsStates.INTEARDOWN, waitOps);
        allow.put(OscarsStates.INCOMMIT, waitOps);
        allow.put(OscarsStates.INPATHCALCULATION, waitOps);
        allow.put(OscarsStates.CREATED, waitOps);

        // not allowed
        HashMap<OscarsOps, OscarsLogicAction> noOps = new HashMap<OscarsOps, OscarsLogicAction>();
        noOps.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        noOps.put(OscarsOps.MODIFY, OscarsLogicAction.NO);
        noOps.put(OscarsOps.SETUP, OscarsLogicAction.NO);
        noOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.NO);
        noOps.put(OscarsOps.CANCEL, OscarsLogicAction.NO);
        allow.put(OscarsStates.FAILED, noOps);
        allow.put(OscarsStates.UNKNOWN, noOps);


        need = new HashMap<OscarsStates, HashMap<OscarsOps, OscarsLogicAction>>();

        // if OSCARS reserved I must perform the modify, setup, and cancel, no need to reserve or tear down
        HashMap<OscarsOps, OscarsLogicAction> resvNeededOps = new HashMap<OscarsOps, OscarsLogicAction>();
        resvNeededOps.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        resvNeededOps.put(OscarsOps.MODIFY, OscarsLogicAction.YES);
        resvNeededOps.put(OscarsOps.SETUP, OscarsLogicAction.YES);
        resvNeededOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.NO);
        resvNeededOps.put(OscarsOps.CANCEL, OscarsLogicAction.YES);
        need.put(OscarsStates.RESERVED, resvNeededOps);

        need.put(OscarsStates.UNSUBMITTED, unsubOps);

        // if OSCARS active I must perform the modify, teardown, and cancel, no need to set up
        HashMap<OscarsOps, OscarsLogicAction> actNeeded = new HashMap<OscarsOps, OscarsLogicAction>();
        actNeeded.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        actNeeded.put(OscarsOps.MODIFY, OscarsLogicAction.YES);
        actNeeded.put(OscarsOps.SETUP, OscarsLogicAction.NO);
        actNeeded.put(OscarsOps.TEARDOWN, OscarsLogicAction.YES);
        actNeeded.put(OscarsOps.CANCEL, OscarsLogicAction.YES);
        need.put(OscarsStates.ACTIVE, actNeeded);

        // don't know what the eventual state will be so wait until status stabilizes
        need.put(OscarsStates.INSETUP, waitOps);
        need.put(OscarsStates.INTEARDOWN, waitOps);
        need.put(OscarsStates.INCOMMIT, waitOps);
        need.put(OscarsStates.INPATHCALCULATION, waitOps);
        need.put(OscarsStates.CREATED, waitOps);

        need.put(OscarsStates.FAILED, noOps);
        need.put(OscarsStates.UNKNOWN, noOps);

    }


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


    public static OscarsLogicAction isOperationAllowed(OscarsOps op, OscarsStates state) {
        OscarsLogicAction result = allow.get(state).get(op);
        log.debug("op: "+op+" state:"+state+" result:"+result);
        return result;

    }

    public static OscarsLogicAction isOperationNeeded(OscarsOps op, OscarsStates state) {
        OscarsLogicAction result = need.get(state).get(op);
        return result;

    }

}
