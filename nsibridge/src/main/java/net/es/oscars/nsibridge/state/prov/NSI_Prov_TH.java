package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NSI_Prov_TH implements TransitionHandler {

    private static final Logger log = Logger.getLogger(NSI_Prov_TH.class);


    private NsiProvMdl mdl;

    @Override
    public Set<UUID> process(String correlationId, SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Prov_State from = (NSI_Prov_State) gfrom;
        NSI_Prov_State to = (NSI_Prov_State) gto;
        NSI_Prov_Event ev = (NSI_Prov_Event) gev;

        ProvisionStateEnumType fromState = (ProvisionStateEnumType) from.state();
        ProvisionStateEnumType toState = (ProvisionStateEnumType) to.state();
        HashSet<UUID> taskIds = new HashSet<UUID>();
        String transitionStr = fromState+" -> "+toState;
        String pre = "corrId: "+correlationId+" "+transitionStr;
        log.debug(pre);

        switch (fromState) {
            case RELEASED:
                if (toState.equals(ProvisionStateEnumType.PROVISIONING)) {
                    taskIds.add(mdl.localProv(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");

                }
                break;

            case PROVISIONING:
                if (toState.equals(ProvisionStateEnumType.PROVISIONED)) {
                    taskIds.add(mdl.sendProvCF(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;
            case PROVISIONED:
                if (toState.equals(ProvisionStateEnumType.RELEASING)) {
                    taskIds.add(mdl.localRel(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;

            case RELEASING:
                if (toState.equals(ProvisionStateEnumType.RELEASED)) {
                    taskIds.add(mdl.sendRelCF(correlationId));
                } else {
                    throw new StateException("invalid state transition ["+transitionStr+"]");
                }
                break;
            default:
                throw new StateException("default case!");
        }
        log.debug(pre+" taskIds: "+ StringUtils.join(taskIds.toArray(), ","));

        return taskIds;
    }


    public NsiProvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiProvMdl mdl) {
        this.mdl = mdl;
    }

}
