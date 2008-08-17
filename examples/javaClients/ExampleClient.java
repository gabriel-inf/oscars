import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

import org.apache.axis2.AxisFault;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;

/**
 * Superclass for example clients.
 */
public class ExampleClient {
    private Client client;

    private Properties props;

    /**
     * Init called by all the derived classes to get the parameters either by
     * prompting or from the test.properties (now unused). Creates a client and
     * calls setup on it.
     * 
     * @param args
     *            [0] pathname of the repository directory that contains the
     *            axis2.xml configuration file args [1] URL to the requested
     *            service. -can be overidden by prompted for arguments.
     * @param isInteractive -
     *            if true, prompt for addtional parameters, otherwise get values
     *            from a properties file.
     */

    public void init(String[] args, boolean isInteractive) {
        String url = null;
        String repo = null;

        try {
            if (isInteractive) {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));
                // Get the repository (axis2.xml ) location from the args
                repo = args[0];
                url = args[1];
                url = Args.getArg(br, "Requested service URL", url);
            } else {
                PropHandler propHandler = new PropHandler("test.properties");
                Properties clientProps = propHandler.getPropertyGroup(
                        "test.client", true);
                this.props = propHandler.getPropertyGroup("test.bss", true);
                repo = "../" + clientProps.getProperty("repo");
                url = clientProps.getProperty("url");
            }
            System.out.println("Service URL is: " + url);
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        this.client = new Client();
        try {
            this.client.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    protected Properties getProperties() {
        return this.props;
    }

    protected Client getClient() {
        return this.client;
    }

    public void outputHops(CtrlPlanePathContent path) {
        System.out.println("Path is:");
        CtrlPlaneHopContent[] hops = path.getHop();
        for (int i = 0; i < hops.length; i++) {
            // What is passed back depends on what layer information is
            // associated with a reservation.  This will be a topology
            // identifier for layer 2, and an IPv4 or IPv6 address for
            // layer 3.
            String hopId = hops[i].getLinkIdRef();
            System.out.println("\t" + hopId);
        }
    }
}
