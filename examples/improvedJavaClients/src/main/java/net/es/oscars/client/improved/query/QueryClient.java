package net.es.oscars.client.improved.query;

import java.rmi.RemoteException;

import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.wsdlTypes.GlobalReservationId;
import net.es.oscars.wsdlTypes.ResDetails;

public class QueryClient extends ImprovedClient {

    public ResDetails query(String gri) {

        ResDetails response = null;
        GlobalReservationId request = new GlobalReservationId();
        request.setGri(gri);
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.queryReservation(request);
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
        } finally {
            oscarsClient.cleanUp();
        }

        return response;
    }


}
