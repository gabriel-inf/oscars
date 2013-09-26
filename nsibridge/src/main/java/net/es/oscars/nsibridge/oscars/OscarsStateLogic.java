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

    // for each operation & state combination
    // if YES, this operation succeeded
    // if NO, this operation failed
    // if ASK_LATER, I need to wait and ask again
    private static HashMap<OscarsOps, HashMap<OscarsStates, OscarsLogicAction>> success;


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

        allow.put(OscarsStates.INCANCEL, waitOps);
        allow.put(OscarsStates.MODCOMMITTED, waitOps);
        allow.put(OscarsStates.COMMITTED, waitOps);

        allow.put(OscarsStates.INSETUP, waitOps);
        allow.put(OscarsStates.INTEARDOWN, waitOps);
        allow.put(OscarsStates.INCOMMIT, waitOps);
        allow.put(OscarsStates.INMODIFY, waitOps);
        allow.put(OscarsStates.INPATHCALCULATION, waitOps);
        allow.put(OscarsStates.CREATED, waitOps);
        allow.put(OscarsStates.ACCEPTED, waitOps);

        // not allowed
        HashMap<OscarsOps, OscarsLogicAction> noOps = new HashMap<OscarsOps, OscarsLogicAction>();
        noOps.put(OscarsOps.RESERVE, OscarsLogicAction.NO);
        noOps.put(OscarsOps.MODIFY, OscarsLogicAction.NO);
        noOps.put(OscarsOps.SETUP, OscarsLogicAction.NO);
        noOps.put(OscarsOps.TEARDOWN, OscarsLogicAction.NO);
        noOps.put(OscarsOps.CANCEL, OscarsLogicAction.NO);
        allow.put(OscarsStates.FINISHED, noOps);
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
        need.put(OscarsStates.INMODIFY, waitOps);
        need.put(OscarsStates.ACCEPTED, waitOps);
        need.put(OscarsStates.INCANCEL, waitOps);
        need.put(OscarsStates.COMMITTED, waitOps);
        need.put(OscarsStates.MODCOMMITTED, waitOps);

        need.put(OscarsStates.FAILED, noOps);
        need.put(OscarsStates.UNKNOWN, noOps);
        need.put(OscarsStates.FINISHED, noOps);




        success = new HashMap<OscarsOps, HashMap<OscarsStates, OscarsLogicAction>>();
        HashMap<OscarsStates, OscarsLogicAction> resvSuccess = new HashMap<OscarsStates, OscarsLogicAction>();
        resvSuccess.put(OscarsStates.ACCEPTED, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.INCOMMIT, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.INPATHCALCULATION, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.INMODIFY, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.CREATED, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.COMMITTED, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.MODCOMMITTED, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.RESERVED, OscarsLogicAction.YES);
        resvSuccess.put(OscarsStates.INSETUP, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.ACTIVE, OscarsLogicAction.YES);
        resvSuccess.put(OscarsStates.INTEARDOWN, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.INCANCEL, OscarsLogicAction.ASK_LATER);
        resvSuccess.put(OscarsStates.UNSUBMITTED, OscarsLogicAction.NO);
        resvSuccess.put(OscarsStates.UNKNOWN, OscarsLogicAction.NO);
        resvSuccess.put(OscarsStates.CANCELLED, OscarsLogicAction.NO);
        resvSuccess.put(OscarsStates.FAILED, OscarsLogicAction.NO);
        resvSuccess.put(OscarsStates.FINISHED, OscarsLogicAction.NO);
        success.put(OscarsOps.RESERVE, resvSuccess);


        HashMap<OscarsStates, OscarsLogicAction> cancelSuccess = new HashMap<OscarsStates, OscarsLogicAction>();
        cancelSuccess.put(OscarsStates.ACCEPTED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.INCOMMIT, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.INPATHCALCULATION, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.INMODIFY, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.CREATED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.COMMITTED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.MODCOMMITTED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.RESERVED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.INSETUP, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.ACTIVE, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.INTEARDOWN, OscarsLogicAction.ASK_LATER);
        cancelSuccess.put(OscarsStates.INCANCEL, OscarsLogicAction.ASK_LATER);
        cancelSuccess.put(OscarsStates.UNSUBMITTED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.UNKNOWN, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.CANCELLED, OscarsLogicAction.YES);
        cancelSuccess.put(OscarsStates.FAILED, OscarsLogicAction.NO);
        cancelSuccess.put(OscarsStates.FINISHED, OscarsLogicAction.NO);
        success.put(OscarsOps.CANCEL, cancelSuccess);

        HashMap<OscarsStates, OscarsLogicAction> modSuccess = new HashMap<OscarsStates, OscarsLogicAction>();
        modSuccess.put(OscarsStates.ACCEPTED, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.INCOMMIT, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.INPATHCALCULATION, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.CREATED, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.INMODIFY, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.COMMITTED, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.MODCOMMITTED, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.RESERVED, OscarsLogicAction.YES);
        modSuccess.put(OscarsStates.INSETUP, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.ACTIVE, OscarsLogicAction.YES);
        modSuccess.put(OscarsStates.INTEARDOWN, OscarsLogicAction.ASK_LATER);
        modSuccess.put(OscarsStates.UNSUBMITTED, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.UNKNOWN, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.CANCELLED, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.FAILED, OscarsLogicAction.NO);
        modSuccess.put(OscarsStates.FINISHED, OscarsLogicAction.NO);
        success.put(OscarsOps.MODIFY, modSuccess);

        HashMap<OscarsStates, OscarsLogicAction> setupSuccess = new HashMap<OscarsStates, OscarsLogicAction>();
        setupSuccess.put(OscarsStates.RESERVED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.ACTIVE, OscarsLogicAction.YES);
        setupSuccess.put(OscarsStates.INCOMMIT, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.INPATHCALCULATION, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.INMODIFY, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.ACCEPTED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.CREATED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.COMMITTED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.MODCOMMITTED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.INSETUP, OscarsLogicAction.ASK_LATER);
        setupSuccess.put(OscarsStates.INTEARDOWN, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.INCANCEL, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.UNSUBMITTED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.UNKNOWN, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.CANCELLED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.FAILED, OscarsLogicAction.NO);
        setupSuccess.put(OscarsStates.FINISHED, OscarsLogicAction.NO);
        success.put(OscarsOps.SETUP, setupSuccess);


        HashMap<OscarsStates, OscarsLogicAction> teardownSuccess = new HashMap<OscarsStates, OscarsLogicAction>();
        teardownSuccess.put(OscarsStates.RESERVED, OscarsLogicAction.YES);
        teardownSuccess.put(OscarsStates.ACTIVE, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.INCOMMIT, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.INPATHCALCULATION, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.INMODIFY, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.ACCEPTED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.CREATED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.COMMITTED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.MODCOMMITTED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.INCANCEL, OscarsLogicAction.ASK_LATER);
        teardownSuccess.put(OscarsStates.INSETUP, OscarsLogicAction.YES);
        teardownSuccess.put(OscarsStates.INTEARDOWN, OscarsLogicAction.ASK_LATER);
        teardownSuccess.put(OscarsStates.UNSUBMITTED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.UNKNOWN, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.CANCELLED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.FAILED, OscarsLogicAction.NO);
        teardownSuccess.put(OscarsStates.FINISHED, OscarsLogicAction.NO);
        success.put(OscarsOps.TEARDOWN, teardownSuccess);



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


    public static OscarsLogicAction didOperationSucceed(OscarsOps op, OscarsStates state) {

        OscarsLogicAction result = success.get(op).get(state);
        return result;
    }

    public static OscarsLogicAction isOperationAllowed(OscarsOps op, OscarsStates state) {
        OscarsLogicAction result = allow.get(state).get(op);
        return result;

    }

    public static OscarsLogicAction isOperationNeeded(OscarsOps op, OscarsStates state) {
        OscarsLogicAction result = need.get(state).get(op);
        return result;

    }

}
