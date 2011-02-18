package net.es.oscars.client.improved.query;


import net.es.oscars.client.Client;
import net.es.oscars.client.improved.ClientException;
import net.es.oscars.client.improved.ImprovedClient;
import net.es.oscars.wsdlTypes.GlobalReservationId;
import net.es.oscars.wsdlTypes.ResDetails;

public class QueryClient extends ImprovedClient {

    public ResDetails query(String gri) throws ClientException {

        ResDetails response = null;
        GlobalReservationId request = new GlobalReservationId();
        request.setGri(gri);
        Client oscarsClient = new Client();
        try {
            oscarsClient.setUp(true, wsdlUrl, repoDir);
            response = oscarsClient.queryReservation(request);
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            oscarsClient.cleanUp();
        }

        return response;
    }


}
