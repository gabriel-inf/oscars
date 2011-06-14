package net.es.oscars.pss.eompls.test;



import net.es.oscars.api.soap.gen.v06.ResDetails;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.sched.quartz.PSSScheduler;
import net.es.oscars.pss.sched.quartz.WorkflowInspectorJob;
import net.es.oscars.pss.soap.PSSSoapHandler;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import org.apache.log4j.Logger;

import org.testng.SkipException;
import org.testng.annotations.Test;

@Test
public class SetupLifecycleTest {
    private Logger log = Logger.getLogger(SetupLifecycleTest.class);
    @Test(groups = { "lifecycle" })
    public void testSetup() throws ConfigException, PSSException {
        
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(ServiceNames.SVC_PSS,  ConfigDefaults.MANIFEST); // manifest.yaml
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);

        try {
        String configFn = cc.getFilePath("config.yaml");
        ConfigHolder.loadConfig(configFn);
        ClassFactory.getInstance().configure();
        
        
        
        String eoMPLSConfigFilePath = cc.getFilePath("config-eompls.yaml");
        EoMPLSConfigHolder.loadConfig(eoMPLSConfigFilePath);
        EoMPLSClassFactory.getInstance().configure();
        } catch (ConfigException ex ) {
            log.debug ("skipping Tests, eompls is  not configured");
            throw new SkipException("skipping Tests, eompls is  not configured");
        }
        
        log.debug("starting PSS main scheduler");
        PSSScheduler sched = PSSScheduler.getInstance();
        try {
            sched.setWorkflowInspector(WorkflowInspectorJob.class);
            sched.start();
        } catch (PSSException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
        PSSSoapHandler soap = new PSSSoapHandler();
        
        
        SetupReqContent setupReq            = new SetupReqContent();

        ResDetails resDet = RequestFactory.getSameDevice();
        setupReq.setReservation(resDet);

        soap.setup(setupReq);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resDet = RequestFactory.getTwoHop();
        setupReq.setReservation(resDet);
        soap.setup(setupReq);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        TeardownReqContent tdReq = new TeardownReqContent();
        tdReq.setReservation(resDet);
        soap.teardown(tdReq);
        

        
        boolean done = false;
        while (!done) {
            try {
                Thread.sleep(1000);
                if (!ClassFactory.getInstance().getWorkflow().hasOutstanding()) {
                    done = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.debug("simulation.run.end");
        PSSScheduler.getInstance().stop();
    }
    /*
    public class PSSkipException extends SkipException {
        PSSkipException ( ) {
            new Exception()  ()
        }

    } */

}
