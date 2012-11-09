package net.es.oscars.nsibridge.state.act;


import net.es.oscars.nsibridge.ifces.NsiActModel;
import net.es.oscars.nsibridge.task.ProcNSIResvRequest;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Leaf_Act_Model implements NsiActModel {
    String connectionId = "";
    public NSI_Leaf_Act_Model(String connId) {
        connectionId = connId;
    }
    private NSI_Leaf_Act_Model() {}



    public void rqLocalResv() {
        long now = new Date().getTime();

        Workflow wf = Workflow.getInstance();
        Task procNSI = new ProcNSIResvRequest(connectionId);

        try {
            wf.schedule(procNSI, now + 1000);
        } catch (TaskException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void doLocalAct() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doLocalDeact() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
