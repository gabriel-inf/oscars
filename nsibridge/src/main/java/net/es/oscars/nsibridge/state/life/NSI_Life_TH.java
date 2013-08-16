package net.es.oscars.nsibridge.state.life;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import org.apache.log4j.Logger;

public class NSI_Life_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Life_TH.class);


    private NsiLifeMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Life_State from = (NSI_Life_State) gfrom;
        NSI_Life_State to = (NSI_Life_State) gto;
        NSI_Life_Event ev = (NSI_Life_Event) gev;
        LifecycleStateEnumType fromState = (LifecycleStateEnumType) from.state();
        LifecycleStateEnumType toState = (LifecycleStateEnumType) to.state();

        switch (fromState) {
            case CREATED:
                if (to.equals(LifecycleStateEnumType.TERMINATING)) {
                    mdl.localTerm();
                }
                break;
            case TERMINATING:
                if (to.equals(LifecycleStateEnumType.TERMINATED)) {
                    mdl.sendTermCF();
                }
                break;
            case FAILED:
                break;
            case TERMINATED:
                break;
            default:
        }
    }


    public NsiLifeMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiLifeMdl mdl) {
        this.mdl = mdl;
    }

}
