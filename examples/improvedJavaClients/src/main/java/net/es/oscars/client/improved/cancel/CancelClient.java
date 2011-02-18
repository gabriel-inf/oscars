package net.es.oscars.client.improved.cancel;


import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ClientException;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.wsdlTypes.GlobalReservationId;

public class CancelClient extends ImprovedClient {

    public String cancel(String gri) throws ClientException {

        String response = null;
        GlobalReservationId request = new GlobalReservationId();
        request.setGri(gri);
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.cancelReservation(request);
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            oscarsClient.cleanUp();
        }
        oscarsClient.cleanUp();

        return response;
    }


}
