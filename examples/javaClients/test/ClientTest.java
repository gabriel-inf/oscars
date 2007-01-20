import junit.framework.*;

import java.util.*;
import java.text.DateFormat;
import java.io.IOException;
import java.rmi.RemoteException;

import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.wsdlTypes.*;

public class ClientTest extends TestCase {

    public ClientTest(String name) {
        super(name);
    }

    public void testCreate() {
        CreateReservationClient cl = null;
        CreateReply response = null;

        try {
            cl = new CreateReservationClient();
            response = cl.create(null, false);
        } catch (AAAFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            fail(e1.getMessage());
        } catch (Exception e1) {
            fail(e1.getMessage());
        }
        cl.outputResponse(response);
        Assert.assertTrue(true);
    }

    public void testQuery() {
        QueryReservationClient cl = null;
        ResDetails response = null;

        try {
            cl = new QueryReservationClient();
            response = cl.query(null, false);
        } catch (AAAFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            fail(e1.getMessage());
        } catch (Exception e1) {
            fail(e1.getMessage());
        }
        cl.outputResponse(response);
        Assert.assertNotNull(response.getStatus());
    }

    public void testList() {
        ListReservationsClient cl = null;
        ListReply response = null;

        try {
            cl = new ListReservationsClient();
            response = cl.list(null, false);
        } catch (AAAFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            fail(e1.getMessage());
        } catch (Exception e1) {
            fail(e1.getMessage());
        }
        cl.outputResponse(response);
        Assert.assertTrue(true);
    }

    public void testCancel() {
        CancelReservationClient cl = null;
        String response = null;

        try {
            cl = new CancelReservationClient();
            response = cl.cancel(null, false);
        } catch (AAAFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (BSSFaultMessageException e1) {
            fail(e1.getFaultMessage().getMsg());
        } catch (java.rmi.RemoteException e1) {
            fail(e1.getMessage());
        } catch (Exception e1) {
            fail(e1.getMessage());
        }    
        cl.outputResponse(response);
        Assert.assertNotNull(response);
    }

    public void testForward() {

        ForwardClient cl = new ForwardClient();
        Assert.assertTrue(true);
    }

}
