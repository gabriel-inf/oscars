import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.*;

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
				url= args[1];
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
		this.client = new Client(true);
		try {
			this.client.setUp(url, repo);
		} catch (AxisFault e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

    protected Properties getProperties() { return this.props; }
    protected Client getClient() { return this.client; }
}
