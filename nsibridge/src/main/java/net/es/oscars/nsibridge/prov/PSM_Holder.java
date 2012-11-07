package net.es.oscars.nsibridge.prov;


import net.es.oscars.nsibridge.state.ProviderSM;

import java.util.ArrayList;
import java.util.List;

public class PSM_Holder {
    private ArrayList<ProviderSM> stateMachines = new ArrayList<ProviderSM>();

    private static PSM_Holder instance;
    private PSM_Holder() {}
    public static PSM_Holder getInstance() {
        if (instance == null) instance = new PSM_Holder();
        return instance;
    }


    public List<ProviderSM> getStateMachines() {
        return this.stateMachines;
    }

    public ProviderSM findStateMachine(String psmId) {
        for (ProviderSM psm : stateMachines) {
            if (psm.getId().equals(psmId)) {
                return psm;
            }
        }
        return null;
    }



}
