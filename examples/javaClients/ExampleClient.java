import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.axis2.AxisFault;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
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

    public void outputHops(ExplicitPath path) {
        System.out.println("Path is:");
        HopList hList = path.getHops();
        Hop hop[] = hList.getHop();
        for (int i = 0; i < hop.length; i++) {
            String ip = hop[i].getValue();
            System.out.println("\t" + this.getHostName(ip) + "\t" + ip);
        }
    }

    public String getHostName(String hop) {
        InetAddress addr = null;
        String hostName = null;

        try {
            addr = InetAddress.getByName(hop);
            hostName = addr.getCanonicalHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host: " + hop);
        }
        // non-fatal error
        if (hostName == null) {
            hostName = hop;
        }
        return hostName;
    }
}
