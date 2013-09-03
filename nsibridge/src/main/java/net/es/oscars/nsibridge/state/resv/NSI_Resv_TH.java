package net.es.oscars.nsibridge.state.resv;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class NSI_Resv_TH implements TransitionHandler {

    private static final Logger log = Logger.getLogger(NSI_Resv_TH.class);


    private NsiResvMdl mdl;

    @Override
    public Set<UUID> process(String correlationId, SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Resv_State from = (NSI_Resv_State) gfrom;
        NSI_Resv_State to = (NSI_Resv_State) gto;
        NSI_Resv_Event ev = (NSI_Resv_Event) gev;

        ReservationStateEnumType fromState = (ReservationStateEnumType) from.state();
        ReservationStateEnumType toState = (ReservationStateEnumType) to.state();
        String transitionStr = fromState+" -> "+toState;
        HashSet<UUID> taskIds = new HashSet<UUID>();
        String pre = "corrId: "+correlationId+" "+transitionStr;
        log.debug(pre);

        switch (fromState) {
            case RESERVE_START:
                if (toState == ReservationStateEnumType.RESERVE_CHECKING) {
                    taskIds.add(mdl.localCheck(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_CHECKING:
                if (toState == ReservationStateEnumType.RESERVE_HELD) {
                    taskIds.add(mdl.localHold(correlationId));
                    taskIds.add(mdl.sendRsvCF(correlationId));
                } else if (toState == ReservationStateEnumType.RESERVE_FAILED) {
                    taskIds.add(mdl.localRollback(correlationId));
                    taskIds.add(mdl.sendRsvFL(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_HELD:
                if (toState == ReservationStateEnumType.RESERVE_COMMITTING) {
                    taskIds.add(mdl.localCommit(correlationId));
                } else if (toState == ReservationStateEnumType.RESERVE_ABORTING) {
                    taskIds.add(mdl.localAbort(correlationId));
                } else if (toState == ReservationStateEnumType.RESERVE_TIMEOUT) {
                    taskIds.add(mdl.localAbort(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_COMMITTING:
                if (toState == ReservationStateEnumType.RESERVE_START) {
                    if (ev == NSI_Resv_Event.LOCAL_RESV_COMMIT_CF) {
                        taskIds.add(mdl.sendRsvCmtCF(correlationId));
                    } else if (ev == NSI_Resv_Event.LOCAL_RESV_COMMIT_FL) {
                        taskIds.add(mdl.sendRsvCmtFL(correlationId));
                    } else {
                        throw new StateException("invalid event received ["+ev+"]");
                    }

                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_ABORTING:
                if (toState == ReservationStateEnumType.RESERVE_START) {
                    taskIds.add(mdl.sendRsvAbtCF(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_TIMEOUT:
                if (toState == ReservationStateEnumType.RESERVE_ABORTING) {
                    taskIds.add(mdl.localAbort(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RESERVE_FAILED:
                if (toState == ReservationStateEnumType.RESERVE_START) {
                    // taskIds.add(mdl.localAbort());
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;


            default:

        }
        log.debug(pre+" taskIds: "+ StringUtils.join(taskIds.toArray(), ","));

        return taskIds;
    }


    public NsiResvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiResvMdl mdl) {
        this.mdl = mdl;
    }

}
