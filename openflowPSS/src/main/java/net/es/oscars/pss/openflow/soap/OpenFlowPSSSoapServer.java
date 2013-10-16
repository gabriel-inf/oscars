package net.es.oscars.pss.openflow.soap;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.workflow.WorkflowInspectorJob;

@OSCARSService (
        implementor = "net.es.oscars.pss.soap.PSSSoapHandler",
        serviceName = ServiceNames.SVC_PSS,
        config = ConfigDefaults.CONFIG
)

public class OpenFlowPSSSoapServer extends OSCARSSoapService<PSSService, PSSPortType> {
    private static OpenFlowPSSSoapServer instance;

    public static OpenFlowPSSSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OpenFlowPSSSoapServer();
        }
        return instance;
    }

    private OpenFlowPSSSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PSS);
        WorkflowInspectorJob wfJob = new WorkflowInspectorJob();
        wfJob.start();

    }
}
