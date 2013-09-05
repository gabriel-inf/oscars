package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_State;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.UUID;

public class NSI_Prov_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Prov_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Prov_SM(String id) {
        this.state = new NSI_Prov_State();
        this.state.setState(ProvisionStateEnumType.RELEASED);
        this.id = id;
    }

    @Override
    public Set<UUID> process(SM_Event event, String correlationId) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        NSI_Prov_State ps = (NSI_Prov_State) this.getState();
        NSI_Prov_State ns = new NSI_Prov_State();
        String pre = "PRE: PSM ["+this.getId()+"] at state ["+state.value()+"] got event ["+event+"]";
        // LOG.debug(pre);
        String error = pre;

        ProvisionStateEnumType prevState = (ProvisionStateEnumType) this.state.state();
        ProvisionStateEnumType nextState = null;



        switch (prevState) {
            case RELEASED:
                if (event.equals(NSI_Prov_Event.LOCAL_TEARDOWN_CONFIRMED)) {
                    nextState = ProvisionStateEnumType.RELEASED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Prov_Event.LOCAL_TEARDOWN_FAILED)) {
                    nextState = ProvisionStateEnumType.RELEASED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Prov_Event.RECEIVED_NSI_PROV_RQ)) {
                    nextState = ProvisionStateEnumType.PROVISIONING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;



            case PROVISIONING:
                if (event.equals(NSI_Prov_Event.LOCAL_PROV_CONFIRMED)) {
                    nextState = ProvisionStateEnumType.PROVISIONED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Prov_Event.LOCAL_PROV_FAILED)) {
                    nextState = ProvisionStateEnumType.RELEASED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case PROVISIONED:
                if (event.equals(NSI_Prov_Event.RECEIVED_NSI_REL_RQ)) {
                    nextState = ProvisionStateEnumType.RELEASING;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Prov_Event.LOCAL_SETUP_CONFIRMED)) {
                    nextState = ProvisionStateEnumType.PROVISIONED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else if (event.equals(NSI_Prov_Event.LOCAL_SETUP_FAILED)) {
                    nextState = ProvisionStateEnumType.PROVISIONED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;


            case RELEASING:
                if (event.equals(NSI_Prov_Event.LOCAL_REL_CONFIRMED)) {
                    nextState = ProvisionStateEnumType.RELEASED;
                    ns.setState(nextState);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
        }

        Set<UUID> taskIds = this.transitionHandler.process(correlationId, ps, ns, event, this);

        String post = "PST: PSM ["+this.getId()+"] now at state ["+this.getState()+"] after event ["+event+"]";
        LOG.debug(post);

        return(taskIds);
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
