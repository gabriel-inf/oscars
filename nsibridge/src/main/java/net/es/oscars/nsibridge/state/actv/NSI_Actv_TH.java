package net.es.oscars.nsibridge.state.actv;

import net.es.oscars.nsibridge.ifces.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.LifecycleStateEnumType;
import org.apache.log4j.Logger;


public class NSI_Actv_TH implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(NSI_Actv_TH.class);


    private NsiActvMdl mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        NSI_Actv_State from = (NSI_Actv_State) gfrom;
        NSI_Actv_State to = (NSI_Actv_State) gto;
        NSI_Actv_Event ev = (NSI_Actv_Event) gev;

        NSI_Actv_StateEnum fromState = (NSI_Actv_StateEnum) from.state();
        NSI_Actv_StateEnum toState = (NSI_Actv_StateEnum) to.state();

        switch (fromState) {
            case INACTIVE:

                break;
            case ACTIVATING:
                if (to.equals(NSI_Actv_StateEnum.ACTIVATING)) {
                    mdl.localAct();
                }
            case ACTIVE:

                break;
            case DEACTIVATING:
                if (to.equals(NSI_Actv_StateEnum.DEACTIVATING)) {
                    mdl.localDeact();
                }
                break;
            default:
        }
    }


    public NsiActvMdl getMdl() {
        return mdl;
    }

    public void setMdl(NsiActvMdl mdl) {
        this.mdl = mdl;
    }

}
