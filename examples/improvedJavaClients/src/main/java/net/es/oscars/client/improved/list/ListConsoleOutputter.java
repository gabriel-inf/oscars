package net.es.oscars.client.improved.list;


import net.es.oscars.client.improved.ResvToString;
import net.es.oscars.wsdlTypes.ResDetails;

public class ListConsoleOutputter implements ListOutputterInterface {
    public void output(ResDetails[] resvs) {
        for (ResDetails resv : resvs) {
            System.out.println("\n\n-------------------------------------------------------");
            System.out.println(ResvToString.convert(resv));
        }
    }


}
