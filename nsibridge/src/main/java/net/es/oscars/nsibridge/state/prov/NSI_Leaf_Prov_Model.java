package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.NsiProvModel;
import net.es.oscars.nsibridge.task.ProcNSIResvRequest;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Prov_Model implements NsiProvModel {
    String connectionId = "";
    public NSI_Leaf_Prov_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Prov_Model() {}



    @Override
    public void doLocalProv() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiProvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiProvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doLocalRel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiRelCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNsiRelFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
