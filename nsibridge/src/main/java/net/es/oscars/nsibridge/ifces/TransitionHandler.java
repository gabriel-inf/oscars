package net.es.oscars.nsibridge.ifces;

import java.util.Set;
import java.util.UUID;

/**
 * @haniotak Date: 2012-08-08
 */
public interface TransitionHandler {
    public Set<UUID> process(SM_State from, SM_State to, SM_Event ev, StateMachine sm) throws StateException;
}
