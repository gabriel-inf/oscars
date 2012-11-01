package net.es.oscars.nsibridge.state;

import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.ifces.StateMachine;
import net.es.oscars.nsibridge.ifces.SM_Event;
import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsibridge.ifces.TransitionHandler;
import org.apache.log4j.Logger;

public class ProviderSM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(ProviderSM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public ProviderSM(String id) {
        this.state = PSM_State.INITIAL;
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        PSM_State prevState = (PSM_State) this.getState();
        PSM_State nextState = null;
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;

        if (event.equals(PSM_Event.TERM_RQ) || event.equals(PSM_Event.END_TIME) || event.equals(PSM_Event.FATAL_FAILURE)) {
            nextState = PSM_State.TERMINATED;
            this.setState(nextState);
        } else {
            switch (prevState) {
                case INITIAL:
                    if (!event.equals(PSM_Event.RSV_RQ)) {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    } else {
                        nextState = PSM_State.RESERVING;
                        this.setState(nextState);
                    }
                    break;
                case RESERVING:
                    if (event.equals(PSM_Event.RSV_FL)) {
                        nextState = PSM_State.TERMINATED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.RSV_OK)) {
                        nextState = PSM_State.RESERVED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;
                case RESERVED:
                    if (event.equals(PSM_Event.PROV_RQ)) {
                        nextState = PSM_State.PROVISIONING;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.START_TIME)) {
                        nextState = PSM_State.SCHEDULED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;

                case PROVISIONING:
                    if (event.equals(PSM_Event.PROV_OK)) {
                        nextState = PSM_State.PROVISIONED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.PROV_FL)) {
                        nextState = PSM_State.RESERVED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;

                case PROVISIONED:
                    if (event.equals(PSM_Event.PROV_OK)) {
                        nextState = PSM_State.PROVISIONED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.REL_RQ)) {
                        nextState = PSM_State.RESERVED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.START_TIME)) {
                        nextState = PSM_State.ACTIVATING;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;

                case SCHEDULED:
                    if (event.equals(PSM_Event.PROV_RQ)) {
                        nextState = PSM_State.ACTIVATING;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.REL_RQ)) {
                        nextState = PSM_State.SCHEDULED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;

                case ACTIVATING:
                    if (event.equals(PSM_Event.ACT_OK)) {
                        nextState = PSM_State.ACTIVATED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.ACT_FL)) {
                        nextState = PSM_State.SCHEDULED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;

                case ACTIVATED:
                    if (event.equals(PSM_Event.PROV_OK) || event.equals(PSM_Event.ACT_OK)) {
                        nextState = PSM_State.ACTIVATED;
                        this.setState(nextState);
                    } else if (event.equals(PSM_Event.REL_RQ)) {
                        nextState = PSM_State.RELEASING;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;
                case RELEASING:
                    if (event.equals(PSM_Event.REL_OK) || event.equals(PSM_Event.REL_FL) ) {
                        nextState = PSM_State.RESERVED;
                        this.setState(nextState);
                    } else {
                        error = pre + " : error : event ["+event+"] not allowed";
                        LOG.error(error);
                        throw new StateException(error);
                    }
                    break;
            }
        } // end else

        String post = "PST: PSM ["+this.getId()+"] now at state ["+this.getState()+"] after event ["+event+"]";
        LOG.debug(post);
        this.transitionHandler.process(prevState, nextState, event, this);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public SM_State getState() {
        return state;
    }

    public void setState(SM_State state) {
        this.state = state;
    }

    @Override
    public TransitionHandler getTransitionHandler() {
        return transitionHandler;
    }

    @Override
    public void setTransitionHandler(TransitionHandler transitionHandler) {
        this.transitionHandler = transitionHandler;
    }


}
