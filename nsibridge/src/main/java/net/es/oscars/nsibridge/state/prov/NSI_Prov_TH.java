package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NSI_Prov_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Prov_TH.class);


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
        switch (fromState) {
            case RELEASED:
                if (to.equals(ProvisionStateEnumType.PROVISIONING)) {
                    taskIds.add(mdl.localProv(correlationId));
                }
                break;

            case PROVISIONING:
                if (to.equals(ProvisionStateEnumType.PROVISIONED)) {
                    taskIds.add(mdl.sendProvCF(correlationId));
                }
                break;
            case PROVISIONED:
                if (to.equals(ProvisionStateEnumType.RELEASING)) {
                    taskIds.add(mdl.localRel(correlationId));
                }
                break;

            case RELEASING:
                if (to.equals(ProvisionStateEnumType.RELEASED)) {
                    taskIds.add(mdl.sendRelCF(correlationId));
                }
                break;
            default:
        }
        return taskIds;
    }


    public NsiProvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiProvMdl mdl) {
        this.mdl = mdl;
    }

}
