package net.es.oscars.nsibridge.state.life;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NSI_Life_TH implements TransitionHandler {

    private static final Logger log = Logger.getLogger(NSI_Life_TH.class);


    private NsiLifeMdl mdl;

    @Override
    public Set<UUID> process(String correlationId, SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Life_State from = (NSI_Life_State) gfrom;
        NSI_Life_State to = (NSI_Life_State) gto;
        NSI_Life_Event ev = (NSI_Life_Event) gev;
        LifecycleStateEnumType fromState = (LifecycleStateEnumType) from.state();
        LifecycleStateEnumType toState = (LifecycleStateEnumType) to.state();
        HashSet<UUID> taskIds = new HashSet<UUID>();
        String transitionStr = fromState+" -> "+toState;
        String pre = "corrId: "+correlationId+" "+transitionStr;
        log.debug(pre);




        switch (fromState) {
            case CREATED:
                if (toState.equals(LifecycleStateEnumType.TERMINATING)) {
                    taskIds.add(mdl.localTerm(correlationId));
                    taskIds.add(mdl.localCancel(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;
            case TERMINATING:
                if (toState.equals(LifecycleStateEnumType.TERMINATED)) {
                    taskIds.add(mdl.sendTermCF(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;
            case FAILED:
                throw new StateException("invalid state transition ["+transitionStr+"]");
            case TERMINATED:
                throw new StateException("invalid state transition ["+transitionStr+"]");
            default:
                throw new StateException("invalid state transition ["+transitionStr+"]");
        }
        log.debug(pre+" taskIds: "+ StringUtils.join(taskIds.toArray(), ","));
        return taskIds;
    }


    public NsiLifeMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiLifeMdl mdl) {
        this.mdl = mdl;
    }

}
