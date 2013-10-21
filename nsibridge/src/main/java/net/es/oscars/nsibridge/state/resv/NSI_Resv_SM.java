package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.UUID;

public class NSI_Resv_SM implements StateMachine {

    private static final Logger LOG = Logger.getLogger(NSI_Resv_SM.class);

    private TransitionHandler transitionHandler;
    private SM_State state;
    private String id;


    public NSI_Resv_SM(String id) {
        this.state = new NSI_Resv_State();
        this.state.setState(ReservationStateEnumType.RESERVE_START);
        this.id = id;
    }

    @Override
    public Set<UUID> process(SM_Event event, String correlationId) throws StateException {
        if (this.transitionHandler == null) {
            LOG.error("PSM: ["+this.id+"]: Null transition handler");
            throw new NullPointerException("PSM: ["+this.id+"]: Null transition handler.");
        }

        ReservationStateEnumType prevState = (ReservationStateEnumType) this.state.state();

        NSI_Resv_State ns = new NSI_Resv_State();
        NSI_Resv_State ps = (NSI_Resv_State) this.state;


        String pre = "PRE: PSM ["+this.getId()+"] at state ["+prevState.value()+"] got event ["+event+"]";
        //LOG.debug(pre);
        String error = pre;


        switch (prevState) {
            case RESERVE_START:
                if (!event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_RQ)) {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                } else {
                    ns.setState(ReservationStateEnumType.RESERVE_CHECKING);
                    this.setState(ns);
                }
                break;
            case RESERVE_CHECKING:
                if (event.equals(NSI_Resv_Event.LOCAL_RESV_CHECK_CF)) {
                    ns.setState(ReservationStateEnumType.RESERVE_HELD);
                    this.setState(ns);
                } else if (event.equals(NSI_Resv_Event.LOCAL_RESV_CHECK_FL)) {
                    ns.setState(ReservationStateEnumType.RESERVE_FAILED);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;
            case RESERVE_HELD:
                if (event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_CM)) {
                    ns.setState(ReservationStateEnumType.RESERVE_COMMITTING);
                    this.setState(ns);
                } else if (event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_AB)) {
                    ns.setState(ReservationStateEnumType.RESERVE_ABORTING);
                    this.setState(ns);
                } else if (event.equals(NSI_Resv_Event.RESV_TIMEOUT)) {
                    ns.setState(ReservationStateEnumType.RESERVE_TIMEOUT);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case RESERVE_COMMITTING:
                if (event.equals(NSI_Resv_Event.LOCAL_RESV_COMMIT_CF)) {
                    ns.setState(ReservationStateEnumType.RESERVE_START);
                    this.setState(ns);
                } else if (event.equals(NSI_Resv_Event.LOCAL_RESV_COMMIT_FL)) {
                    ns.setState(ReservationStateEnumType.RESERVE_START);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case RESERVE_ABORTING:
                 if (event.equals(NSI_Resv_Event.LOCAL_RESV_ABORT_CF)) {
                     ns.setState(ReservationStateEnumType.RESERVE_START);
                     this.setState(ns);
                 } else if (event.equals(NSI_Resv_Event.LOCAL_RESV_ABORT_FL)) {
                     ns.setState(ReservationStateEnumType.RESERVE_START);
                     this.setState(ns);
                 } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case RESERVE_TIMEOUT:
                if (event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_CM)) {
                    ns.setState(ReservationStateEnumType.RESERVE_START);
                    this.setState(ns);
                } else if (event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_AB)) {
                    ns.setState(ReservationStateEnumType.RESERVE_ABORTING);
                    this.setState(ns);

                } else {
                    error = pre + " : error : event ["+event+"] not allowed";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

            case RESERVE_FAILED:
                if (event.equals(NSI_Resv_Event.RECEIVED_NSI_RESV_AB)) {
                    ns.setState(ReservationStateEnumType.RESERVE_ABORTING);
                    this.setState(ns);
                } else {
                    error = pre + " : error : event ["+event+"] not allowed; at RESERVE_FAILED";
                    LOG.error(error);
                    throw new StateException(error);
                }
                break;

        }

        String post = "PST: PSM ["+this.getId()+"] now at state ["+this.getState().value()+"] after event ["+event+"]";
        LOG.debug(post);
        Set<UUID> taskIds = this.transitionHandler.process(correlationId, ps, ns, event, this);
        for (UUID taskId : taskIds) {
            LOG.debug("   task id:  " +taskId);
        }

        return taskIds;
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


    public static void handleEvent(String connectionId, String correlationId, NSI_Resv_Event event) throws StateException {
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM sm = smh.findNsiResvSM(connectionId);
        sm.process(event, correlationId);
    }

}
