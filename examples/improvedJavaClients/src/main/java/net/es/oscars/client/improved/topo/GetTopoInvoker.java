package net.es.oscars.client.improved.topo;

import net.es.oscars.client.improved.topo.GetTopoClient;

public class GetTopoInvoker {
    public static void main(String[] args) throws Exception {
        String configFile = GetTopoClient.DEFAULT_CONFIG_FILE;
        String soapConfigFile = GetTopoClient.DEFAULT_SOAP_CONFIG_FILE;
        String soapConfigId = GetTopoClient.DEFAULT_SOAP_CONFIG_ID;

        GetTopoClient cl = new GetTopoClient();
        cl.setSoapConfigFile(soapConfigFile);
        cl.configureSoap(soapConfigId);
        cl.setConfigFile(configFile);
        cl.configure();
        cl.getTopology();
        cl.print();

    }

}
