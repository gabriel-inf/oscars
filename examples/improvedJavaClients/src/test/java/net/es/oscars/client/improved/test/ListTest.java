package net.es.oscars.client.improved.test;

import java.util.List;

import net.es.oscars.client.improved.list.ListClient;
import net.es.oscars.client.improved.list.ListOutputterFactory;
import net.es.oscars.client.improved.list.ListOutputterInterface;
import net.es.oscars.wsdlTypes.ListReply;
import net.es.oscars.wsdlTypes.ResDetails;

import org.testng.annotations.Test;


@Test
public class ListTest {

    public void testClient() {
        System.out.println("make sure you have a good repo directory before enabling tests");

        List<ListOutputterInterface> outputters =
                ListOutputterFactory.getConfiguredOutputters();

        ListClient cl = new ListClient();
        cl.configureSoap();
        cl.configure();

        ListReply listResp = cl.performRequest(cl.formRequest());
        ResDetails[] resvs = cl.filterResvs(listResp.getResDetails());
        for (ListOutputterInterface outputter : outputters) {
            outputter.output(resvs);
        }
    }
}
