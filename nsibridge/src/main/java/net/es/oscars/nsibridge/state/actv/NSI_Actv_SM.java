package net.es.oscars.nsibridge.state.actv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

public class NSI_Actv_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Actv_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Actv_SM(String id) {
        this.state = new NSI_Actv_State();
        this.state.setState(NSI_Actv_StateEnum.INACTIVE);
        this.id = id;
    }

    @Override
    public void process(SM_Event event) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Actv_State ps = (NSI_Actv_State) this.getState();
        NSI_Actv_State ns = new NSI_Actv_State();
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state+"] got event ["+event+"]";
        LOG.debug(pre);
        String error = pre;

        NSI_Actv_StateEnum prevState = (NSI_Actv_StateEnum) this.state.state();
        NSI_Actv_StateEnum nextState = null;



        switch (prevState) {
            case INACTIVE:
                if (event.equals(NSI_Actv_Event.PAST_START_TIME)) {
                    nextState = NSI_Actv_StateEnum.ACTIVATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Actv_Event.LOCAL_CLEANUP)) {

                } else if (event.equals(NSI_Actv_Event.PAST_END_TIME)) {

                } else if (event.equals(NSI_Actv_Event.RECEIVED_NSI_TERM_RQ)) {

                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case ACTIVATING:
                if (event.equals(NSI_Actv_Event.LOCAL_ACT_CONFIRMED)) {
                    nextState = NSI_Actv_StateEnum.ACTIVE;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Actv_Event.LOCAL_ACT_FAILED)) {
                    nextState = NSI_Actv_StateEnum.INACTIVE;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case ACTIVE:
                if (event.equals(NSI_Actv_Event.PAST_END_TIME)) {
                    nextState = NSI_Actv_StateEnum.DEACTIVATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Actv_Event.LOCAL_CLEANUP)) {
                    nextState = NSI_Actv_StateEnum.DEACTIVATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Actv_Event.RECEIVED_NSI_TERM_RQ)) {
                    nextState = NSI_Actv_StateEnum.DEACTIVATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case DEACTIVATING:
                if (event.equals(NSI_Actv_Event.LOCAL_DEACT_CONFIRMED)) {
                    nextState = NSI_Actv_StateEnum.INACTIVE;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Actv_Event.LOCAL_DEACT_FAILED)) {
                    nextState = NSI_Actv_StateEnum.ACTIVE;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
        }

        String post = "PST: PSM ["+this.getId()+"] now at state ["+this.getState()+"] after event ["+event+"]";
        LOG.debug(post);
        this.transitionHandler.process(ps, ns, event, this);
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
