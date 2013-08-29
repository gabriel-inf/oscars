package net.es.oscars.nsibridge.ifces;

import java.util.Set;
import java.util.UUID;

public interface StateMachine {

    public Set<UUID> process(SM_Event ev, String correlationId) throws StateException;

    public void setTransitionHandler(TransitionHandler th);
    public TransitionHandler getTransitionHandler();
    public SM_State getState();


}

