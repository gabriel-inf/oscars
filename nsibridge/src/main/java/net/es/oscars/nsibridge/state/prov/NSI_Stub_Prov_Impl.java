package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.task.LocalProvTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;


public class NSI_Stub_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_Stub_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Prov_Impl() {}



    @Override
    public void localProv() {

    }

    @Override
    public void sendProvCF() {
    }

    @Override
    public void notifyProvFL() {
    }

    @Override
    public void localRel() {
    }

    @Override
    public void sendRelCF() {
    }

    @Override
    public void notifyRelFL() {
    }
}
