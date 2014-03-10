package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.When;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.test.req.NSIRequestFactory;
import org.apache.log4j.Logger;

import javax.xml.ws.Holder;

public class LifecycleSteps {
    private static Logger log = Logger.getLogger(LifecycleSteps.class);

    @When("^I submit terminate$")
    public void I_submit_terminate() throws Throwable {
        ConnectionProvider cp = new ConnectionProvider();
        String connId = HelperSteps.getValue("connId");
        String corrId = HelperSteps.getValue("corrId");

        CommonHeaderType inHeader = NSIRequestFactory.makeHeader(corrId);
        Holder<CommonHeaderType> outHolder = new Holder<CommonHeaderType>();
        try {
            cp.terminate(connId, inHeader, outHolder);
        } catch (Exception ex) {
            log.error(ex);
            HelperSteps.setSubmitException(true);
            return;
        }
        HelperSteps.setSubmitException(false);

    }


}
