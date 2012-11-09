package net.es.oscars.nsibridge.state.resv;


import net.es.oscars.nsibridge.ifces.NsiProvModel;
import net.es.oscars.nsibridge.ifces.NsiResvModel;
import net.es.oscars.nsibridge.task.ProcNSIResvRequest;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Resv_Model implements NsiResvModel {
    String connectionId = "";
    public NSI_Leaf_Resv_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Resv_Model() {}



    public void rqLocalResv() {


    }


    @Override
    public void doLocalResv() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task procNSI = new ProcNSIResvRequest(connectionId);

        try {
            wf.schedule(procNSI, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }    }

    @Override
    public void sendNsiResvCF() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendNSIResvFL() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
