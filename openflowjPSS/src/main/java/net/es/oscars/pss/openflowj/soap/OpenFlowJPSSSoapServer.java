package net.es.oscars.pss.openflowj.soap;

import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.soap.OSCARSService;
import net.es.oscars.utils.soap.OSCARSSoapService;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;

import net.es.oscars.pss.workflow.WorkflowInspectorJob;
import net.es.oscars.pss.soap.gen.PSSService;
import net.es.oscars.pss.soap.gen.PSSPortType;

@OSCARSService (
        implementor = "net.es.oscars.pss.soap.PSSSoapHandler",
        serviceName = ServiceNames.SVC_PSS,
        config = ConfigDefaults.CONFIG
)

public class OpenFlowJPSSSoapServer extends OSCARSSoapService<PSSService, PSSPortType> {
    private static OpenFlowJPSSSoapServer instance;

    public static OpenFlowJPSSSoapServer getInstance() throws OSCARSServiceException {
        if (instance == null) {
            instance = new OpenFlowJPSSSoapServer();
        }
        return instance;
    }

    private OpenFlowJPSSSoapServer() throws OSCARSServiceException {
        super(ServiceNames.SVC_PSS);
        WorkflowInspectorJob wfJob = new WorkflowInspectorJob();
        wfJob.start();
    }
}
