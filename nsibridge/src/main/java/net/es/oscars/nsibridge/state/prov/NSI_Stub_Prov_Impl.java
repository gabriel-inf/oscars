package net.es.oscars.nsibridge.state.prov;


import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.ifces.NsiProvMdl;
import net.es.oscars.nsibridge.task.LocalProvTask;
import net.es.oscars.nsibridge.task.SendNSIMessageTask;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;

import java.util.Date;
import java.util.UUID;


public class NSI_Stub_Prov_Impl implements NsiProvMdl {
    String connectionId = "";
    public NSI_Stub_Prov_Impl(String connId) {
        connectionId = connId;
    }
    private NSI_Stub_Prov_Impl() {}



    @Override
    public UUID localProv() {
        return null;
    }

    @Override
    public UUID sendProvCF() {
        return null;
    }

    @Override
    public UUID notifyProvFL() {
        return null;
    }

    @Override
    public UUID localRel() {
        return null;
    }

    @Override
    public UUID sendRelCF() {
        return null;
    }

    @Override
    public UUID notifyRelFL() {
        return null;
    }
}
