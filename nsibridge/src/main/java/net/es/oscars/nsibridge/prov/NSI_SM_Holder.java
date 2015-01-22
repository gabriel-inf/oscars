package net.es.oscars.nsibridge.prov;


import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.life.NSI_Life_TH;
import net.es.oscars.nsibridge.state.life.NSI_UP_Life_Impl;
import net.es.oscars.nsibridge.state.prov.NSI_UP_Prov_Impl;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_State;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_TH;
import net.es.oscars.nsibridge.state.resv.NSI_UP_Resv_Impl;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_TH;

import java.util.HashMap;

public class NSI_SM_Holder {
    private HashMap<String, NSI_Prov_SM> provStateMachines= new HashMap<String, NSI_Prov_SM>();
    private HashMap<String, NSI_Resv_SM> resvStateMachines= new HashMap<String, NSI_Resv_SM>();
    private HashMap<String, NSI_Life_SM> lifeStateMachines = new HashMap<String, NSI_Life_SM>();

    private static NSI_SM_Holder instance;
    private NSI_SM_Holder() {}
    public static NSI_SM_Holder getInstance() {
        if (instance == null) instance = new NSI_SM_Holder();
        return instance;
    }

    public void makeStateMachines(String connId) throws ServiceException {
        NSI_Prov_SM psm = this.findNsiProvSM(connId);
        NSI_Resv_SM rsm = this.findNsiResvSM(connId);
        NSI_Life_SM tsm = this.findNsiLifeSM(connId);
        boolean error = false;
        String errMsg = "";
        if (psm != null) {
            error = true;
            errMsg += "found existing provSM";
        }
        if (rsm != null) {
            error = true;
            errMsg += "found existing resvSM";
        }

        if (tsm != null) {
            error = true;
            errMsg += "found existing termSM";
        }
        if (error) {
            throw new ServiceException(errMsg);
        }

        psm = new NSI_Prov_SM(connId);
        NSI_Prov_State ps = new NSI_Prov_State();
        ps.setState(ProvisionStateEnumType.RELEASED);
        psm.setState(ps);
        NSI_Prov_TH pth = new NSI_Prov_TH();
        psm.setTransitionHandler(pth);
        NSI_UP_Prov_Impl pml = new NSI_UP_Prov_Impl(connId);
        pth.setMdl(pml);


        rsm = new NSI_Resv_SM(connId);
        NSI_Resv_TH rth = new NSI_Resv_TH();
        rsm.setTransitionHandler(rth);
        NSI_UP_Resv_Impl rml = new NSI_UP_Resv_Impl(connId);
        rth.setMdl(rml);

        tsm = new NSI_Life_SM(connId);
        NSI_Life_TH tth = new NSI_Life_TH();
        tsm.setTransitionHandler(tth);
        NSI_UP_Life_Impl tml = new NSI_UP_Life_Impl(connId);
        tth.setMdl(tml);



        this.provStateMachines.put(connId, psm);
        this.resvStateMachines.put(connId, rsm);
        this.lifeStateMachines.put(connId, tsm);

    }

    public boolean hasStateMachines(String connId) {

        if ( (provStateMachines.get(connId) != null) &&
             (lifeStateMachines.get(connId) != null) &&
             (resvStateMachines.get(connId) != null) ) {
            return true;

        }
        return false;
    }


    public NSI_Prov_SM findNsiProvSM(String connId) {
        return provStateMachines.get(connId);
    }

    public NSI_Resv_SM findNsiResvSM(String connId) {
        return resvStateMachines.get(connId);
    }

    public NSI_Life_SM findNsiLifeSM(String connId) {
        return lifeStateMachines.get(connId);
    }


    public HashMap<String, NSI_Prov_SM> getProvStateMachines() {
        return provStateMachines;
    }

    public HashMap<String, NSI_Resv_SM> getResvStateMachines() {
        return resvStateMachines;
    }

    public HashMap<String, NSI_Life_SM> getLifeStateMachines() {
        return lifeStateMachines;
    }
}
