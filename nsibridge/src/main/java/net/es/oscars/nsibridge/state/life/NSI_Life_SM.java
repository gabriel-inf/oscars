package net.es.oscars.nsibridge.state.life;

import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.UUID;

public class NSI_Life_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Life_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Life_SM(String id) {
        this.state = new NSI_Life_State();
        this.state.setState(LifecycleStateEnumType.CREATED);
        this.id = id;
    }

    @Override
    public Set<UUID> process(SM_Event event, String correlationId) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Life_State ps = (NSI_Life_State) this.getState();
        NSI_Life_State ns = new NSI_Life_State();
        String pre = "PRE: LSM ["+this.getId()+"] at state ["+state.value()+"] got event ["+event+"]";
        //LOG.debug(pre);
        String error = pre;

        LifecycleStateEnumType prevState = (LifecycleStateEnumType) this.state.state();
        LifecycleStateEnumType nextState = null;


        switch (prevState) {
            // no events allowed if already terminated
            case TERMINATED:
                error = pre + " : error : event ["+event+"] not allowed";
                LOG.error(error);
                throw new StateException(error);

            case CREATED:
                if (event.equals(NSI_Life_Event.LOCAL_FORCED_END)) {
                    nextState = LifecycleStateEnumType.FAILED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Life_Event.END_TIME)) {
                    nextState = LifecycleStateEnumType.PASSED_END_TIME;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Life_Event.RECEIVED_NSI_TERM_RQ)) {
                    nextState = LifecycleStateEnumType.TERMINATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case TERMINATING:
                if (event.equals(NSI_Life_Event.LOCAL_TERM_CONFIRMED)) {
                    nextState = LifecycleStateEnumType.TERMINATED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case PASSED_END_TIME:
            case FAILED:
                if (event.equals(NSI_Life_Event.RECEIVED_NSI_TERM_RQ)) {
                    nextState = LifecycleStateEnumType.TERMINATING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);

                }
        }


        String post = "PST: LSM ["+this.getId()+"] now at state ["+this.getState().value()+"] after event ["+event+"]";
        LOG.debug(post);
        return (this.transitionHandler.process(correlationId, ps, ns, event, this));
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
