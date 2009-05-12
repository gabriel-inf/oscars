package net.es.oscars.client.improved.query;


import net.es.oscars.client.improved.ResvToString;
import net.es.oscars.wsdlTypes.ResDetails;

public class QueryConsoleOutputter {

    public void outputResponse(ResDetails response) {
        System.out.println(ResvToString.convert(response));

    }
}
