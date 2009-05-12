package net.es.oscars.client.improved.cancel;

import java.rmi.RemoteException;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GlobalReservationId;

public class CancelClient extends ImprovedClient {

    public String cancel(String gri) {

        String response = null;
        GlobalReservationId request = new GlobalReservationId();
        request.setGri(gri);
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.cancelReservation(request);
        } catch (RemoteException e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AAAFaultMessage e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        return response;
    }


}
