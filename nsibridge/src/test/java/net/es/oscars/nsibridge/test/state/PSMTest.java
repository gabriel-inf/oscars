package net.es.oscars.nsibridge.test.state;


import net.es.oscars.nsibridge.state.PSM_TransitionHandler;
import net.es.oscars.nsibridge.state.ProviderSM;
import net.es.oscars.nsibridge.state.PSM_Event;
import net.es.oscars.nsibridge.ifces.StateException;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

/**
 * @haniotak Date: 2012-08-07
 */
public class PSMTest {
    private static final Logger LOG = Logger.getLogger(PSMTest.class);

    @Test (expectedExceptions = NullPointerException.class)
    public void noTH() throws Exception {
        ProviderSM sm = new ProviderSM("noTH");
        sm.process(PSM_Event.RSV_RQ);
    }

    @Test (expectedExceptions = StateException.class)
    public void badAfterInit() throws Exception {
        ProviderSM sm = new ProviderSM("badAfterInit");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.ACT_FL);
    }

    @Test (expectedExceptions = StateException.class)
    public void badAfterRsvRQ() throws Exception {
        ProviderSM sm = new ProviderSM("badAfterRsvRQ");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.ACT_OK);
    }

    @Test
    public void rsvFail() throws Exception {
        ProviderSM sm = new ProviderSM("rsvFail");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_FL);
    }


    @Test
    public void simpleWorkflow() throws Exception {
        ProviderSM sm = new ProviderSM("simple");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.PROV_OK);
        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.ACT_OK);

        sm.process(PSM_Event.END_TIME);
    }

    @Test
    public void provisionedWorkflow() throws Exception {
        ProviderSM sm = new ProviderSM("provision");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.ACT_OK);


        sm.process(PSM_Event.END_TIME);
    }

    @Test
    public void actThenReleaseWorkflow() throws Exception {
        ProviderSM sm = new ProviderSM("actRelease");
        sm.setTransitionHandler(new PSM_TransitionHandler());
        sm.process(PSM_Event.RSV_RQ);
        sm.process(PSM_Event.RSV_OK);

        sm.process(PSM_Event.PROV_RQ);
        sm.process(PSM_Event.PROV_OK);
        sm.process(PSM_Event.START_TIME);
        sm.process(PSM_Event.ACT_OK);

        sm.process(PSM_Event.REL_RQ);
        sm.process(PSM_Event.REL_OK);

        sm.process(PSM_Event.END_TIME);
    }

}
