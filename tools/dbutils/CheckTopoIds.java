import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.Transaction;


/**
 * This class will read a file of newline-separated topology identifiers
 * and try and look them up in the local topology database according
 * to their type. Currently handles domains, nodes, ports and link entries.
 * Will output results to stdout.
 * 
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class CheckTopoIds {
    public static void main(String[] argv) {
        String usage = "Usage:\ncheckTopoIds.sh /path/to/file\n" + 
        			   "file must contain topology identifiers, each in its own line\n";

        String filename = "";

        if (argv.length != 1) {
            System.out.println(""+usage);
            System.exit(1);
        }
        
        filename = argv[0];

        try {
            Initializer initializer = new Initializer();
            List<String> dbnames = new ArrayList<String>();
            dbnames.add("bss");
            initializer.initDatabase(dbnames);
            Session ses = HibernateUtil.getSessionFactory("bss").getCurrentSession();
            Transaction tx = ses.beginTransaction();

            BufferedReader in = null;
            in = new BufferedReader(new FileReader(filename));
            String[] cols = null;
            String line;
    		DomainDAO domDAO = new DomainDAO("bss");
    		NodeDAO nodeDAO = new NodeDAO("bss");
    		PortDAO portDAO = new PortDAO("bss");
    		LinkDAO linkDAO = new LinkDAO("bss");

            while ((line = in.readLine()) != null) {
            	line = line.trim();
            	
            	Hashtable<String, String> result = TopologyUtil.parseTopoIdent(line);
            	String type = result.get("type");
            	System.out.print("identifier: ["+line+"] ");
            	boolean found = false;
            	if (type != null && (type.equals("link") || type.equals("port") || type.equals("node") || type.equals("domain")) ) {

        			String domainId = result.get("domainId");
            		Domain dom = domDAO.fromTopologyIdent(domainId);
            		if (dom == null) {
            			System.out.println("... (type:["+type+"]) domain not found in DB");
            		} else if ( type.equals("link") || type.equals("port") || type.equals("node") ) {
            		
	            		String nodeId = result.get("nodeId");
	            		Node node = nodeDAO.fromTopologyIdent(nodeId, dom);
	            		if (node == null) {
	            			System.out.println("... (type:["+type+"]) node not found in DB");
	            		} else if ( type.equals("link") || type.equals("port")) {
	            			
	            		
		            		String portId = result.get("portId");
		            		Port port = portDAO.fromTopologyIdent(portId, node);
		            		if (port == null) {
		            			System.out.println("...  (type:["+type+"]) port not found in DB");
		            		} else if ( type.equals("link") ) {
			            		String linkId = result.get("linkId");
			            		Link link = linkDAO.fromTopologyIdent(linkId, port);
			            		if (link == null) {
			            			System.out.println("... (type:["+type+"]) link not found in DB");
			            		} else {
			            			found = true;
			            		}
		            		} else {
		            			found = true;
		            		}
	            		} else {
	            			found = true;
	            		}
            		} else {
            			found = true;
            		}
	        		if (found) {
	        			System.out.println("... (type:["+type+"]) all entries found in DB"); 
	        		}
	        	} else {
	        		System.out.println("... wrong type: ["+type+"]");
	        	}
            }
        	
        } catch (IOException ex) {
            System.out.println("Error reading file ["+filename+"].");
            System.exit(1);
        }
    }
}
