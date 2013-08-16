package net.es.oscars.nsibridge.state.life;


import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiLifeMdl;
import net.es.oscars.nsibridge.task.OscarsTermTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Stub_Life_Impl implements NsiLifeMdl {
    String connectionId = "";
    public NSI_Stub_Life_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Life_Impl() {}



    @Override
    public void localTerm() {
    }

    @Override
    public void sendTermCF() {
    }


    @Override
    public void notifyForcedEndErrorEvent() {

    }
}
