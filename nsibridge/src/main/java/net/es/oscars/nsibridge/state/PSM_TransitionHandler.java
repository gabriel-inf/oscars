package net.es.oscars.nsibridge.state;

import net.es.oscars.nsibridge.ifces.*;
import org.apache.log4j.Logger;

/**
 * @haniotak Date: 2012-08-08
 */
public class PSM_TransitionHandler implements TransitionHandler {

    private static final Logger LOG = Logger.getLogger(PSM_TransitionHandler.class);


    private ProviderMDL mdl;

    @Override
    public void process(SM_State gfrom, SM_State gto, SM_Event gev, StateMachine gsm) throws StateException {
        PSM_State from = (PSM_State) gfrom;
        PSM_State to = (PSM_State) gto;
        PSM_Event ev = (PSM_Event) gev;
        if (to.equals(PSM_State.TERMINATED)) {
            mdl.cleanup();
        }
        switch (from) {
            case INITIAL:
                if (to.equals(PSM_State.RESERVING)) {
                    mdl.sendResvRQ();
                } else if (to.equals(PSM_State.TERMINATED)) {
                    mdl.sendResvFL();
                }

                break;
            case RESERVING:
                if (to.equals(PSM_State.RESERVED)) {
                    mdl.sendResvCF();
                }
                break;
            case RESERVED:
                if (to.equals(PSM_State.PROVISIONING)) {
                    mdl.sendProvRQ();
                } else if (to.equals(PSM_State.SCHEDULED)) {
                }
                break;
            case SCHEDULED:
                if (to.equals(PSM_State.ACTIVATING)) {
                    mdl.sendProvCF();
                } else if (to.equals(PSM_State.SCHEDULED)) {
                    mdl.sendRelCF();
                }
                break;
            case PROVISIONING:
                if (to.equals(PSM_State.PROVISIONED)) {
                    mdl.sendProvCF();
                } else if (to.equals(PSM_State.PROVISIONED)) {
                    mdl.sendProvFL();
                }
                break;
            case PROVISIONED:
                if (to.equals(PSM_State.PROVISIONED)) {
                    mdl.sendProvCF();
                } else if (to.equals(PSM_State.RESERVED)) {
                    mdl.sendRelCF();
                } else if (to.equals(PSM_State.ACTIVATING)) {
                    mdl.activate();
                }
                break;
            case ACTIVATING:
                if (to.equals(PSM_State.ACTIVATED)) {
                    mdl.sendActCF();
                } else if (to.equals(PSM_State.SCHEDULED)) {
                    mdl.sendActFL();
                }
            case ACTIVATED:
                if (to.equals(PSM_State.ACTIVATED)) {
                    mdl.sendActCF();
                    mdl.sendProvCF();
                } else if (to.equals(PSM_State.RELEASING)) {
                    mdl.sendRelRQ();
                }
                break;
            case RELEASING:
                if (to.equals(PSM_State.SCHEDULED)) {
                    if (ev.equals(PSM_Event.REL_OK)) {
                        mdl.sendRelCF();
                    } else if (ev.equals(PSM_Event.REL_FL)) {
                        mdl.sendRelFL();
                    }
                }
                break;
            default:
        }
    }


    public ProviderMDL getMdl() {
        return mdl;
    }

    public void setMdl(ProviderMDL mdl) {
        this.mdl = mdl;
    }

}
