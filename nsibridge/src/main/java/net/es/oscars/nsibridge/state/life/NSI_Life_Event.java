package net.es.oscars.nsibridge.state.life;

import net.es.oscars.nsibridge.ifces.SM_Event;

public enum NSI_Life_Event implements SM_Event {
    END_TIME,
    RECEIVED_NSI_TERM_RQ,

    LOCAL_FORCED_END,
    LOCAL_TERM_CONFIRMED,
}
