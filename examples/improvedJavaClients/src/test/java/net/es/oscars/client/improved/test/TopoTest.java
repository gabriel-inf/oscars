package net.es.oscars.client.improved.test;

import net.es.oscars.client.improved.topo.GetTopoClient;
import net.es.oscars.wsdlTypes.GetTopologyResponseContent;

import org.testng.annotations.Test;


@Test
public class TopoTest {

    public void testClient() {
        System.out.println("make sure you have a good repo directory before enabling tests");

        String configFile = GetTopoClient.DEFAULT_CONFIG_FILE;
        String soapConfigFile = GetTopoClient.DEFAULT_SOAP_CONFIG_FILE;
        String soapConfigId = GetTopoClient.DEFAULT_SOAP_CONFIG_ID;


        GetTopoClient cl = new GetTopoClient();
        cl.setSoapConfigFile(soapConfigFile);
        cl.configureSoap(soapConfigId);
        cl.setConfigFile(configFile);
        cl.configure();

        GetTopologyResponseContent response = cl.performRequest(cl.formRequest());

        GetTopoClient.print(response);
    }
}
