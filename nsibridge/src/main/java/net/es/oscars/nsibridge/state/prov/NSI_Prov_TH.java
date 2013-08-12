package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;
import org.apache.log4j.Logger;

public class NSI_Prov_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Prov_TH.class);


    private NsiProvMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Prov_State from = (NSI_Prov_State) gfrom;
        NSI_Prov_State to = (NSI_Prov_State) gto;
        NSI_Prov_Event ev = (NSI_Prov_Event) gev;

        ProvisionStateEnumType fromState = (ProvisionStateEnumType) from.state();
        ProvisionStateEnumType toState = (ProvisionStateEnumType) to.state();

        String transitionStr = fromState+" -> "+toState;
        switch (fromState) {
            case RELEASED:
                if (to.equals(ProvisionStateEnumType.PROVISIONING)) {
                    mdl.localProv();
                }
                break;

            case PROVISIONING:
                if (to.equals(ProvisionStateEnumType.PROVISIONED)) {
                    mdl.sendProvCF();
                }
                break;
            case PROVISIONED:
                if (to.equals(ProvisionStateEnumType.RELEASING)) {
                    mdl.localRel();
                }
                break;

            case RELEASING:
                if (to.equals(ProvisionStateEnumType.RELEASED)) {
                    mdl.sendRelCF();
                }
                break;
            default:
        }
    }


    public NsiProvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiProvMdl mdl) {
        this.mdl = mdl;
    }

}
