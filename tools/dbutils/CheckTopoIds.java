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
 * or a topology identifier in an argument,  and try and look them up
 * in the local topology database according to their type. Currently
 * handles domains, nodes, ports and link entries.
 *
 * Will output results to stdout.
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class CheckTopoIds {

    public static DomainDAO domDAO;
    public static NodeDAO nodeDAO;
    public static PortDAO portDAO;
    public static LinkDAO linkDAO;
    public static IpaddrDAO ipaddrDAO;

    public static void main(String[] args) {
        String usage = "Usage:\ncheckTopoIds.sh [-v] [-d] [-f /path/to/file] [-id topo_id] \n" +
                       "use -id to check a single topology identifier\n" +
                       "use -f to check a file containing topology identifiers, each in its own line\n" +
                       "use -d to additionally display database ids for objects in hierarchy\n" +
                       "use -v (implies -d) for verbose information on every object found \n";



        boolean verbose = false;
        boolean showdbids = false;
        String filename = "";
        String identifier = "";

        if (args.length < 1) {
            System.out.println(""+usage);
            System.exit(0);
        }

        for(int i = 0; i < args.length; i++){
            if (args[i].equals("-help") ||
                args[i].equals("-h")) {
                System.out.println(usage);
                System.exit(0);
            }
            if (args[i].equals("-f")) {
                if (args.length >= i+1) {
                    filename = args[i+1];
                } else {
                    System.out.println(usage);
                    System.exit(0);
                }
            } else if (args[i].equals("-id")) {
                if (args.length >= i+1) {
                    identifier = args[i+1];
                } else {
                    System.out.println(usage);
                    System.exit(0);
                }
            } else if (args[i].equals("-v")) {
                verbose = true;
            } else if (args[i].equals("-d")) {
                showdbids = true;
            }
        }




        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
        Session ses = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        Transaction tx = ses.beginTransaction();

        CheckTopoIds.domDAO = new DomainDAO("bss");
        CheckTopoIds.nodeDAO = new NodeDAO("bss");
        CheckTopoIds.portDAO = new PortDAO("bss");
        CheckTopoIds.linkDAO = new LinkDAO("bss");
        CheckTopoIds.ipaddrDAO = new IpaddrDAO("bss");


        if (!identifier.equals("")) {
            CheckTopoIds.checkIdentifier(identifier, verbose, showdbids);
        }

        if (!filename.equals("")) {
            try {
                BufferedReader in = null;
                in = new BufferedReader(new FileReader(filename));
                String line;

                while ((line = in.readLine()) != null) {
                    CheckTopoIds.checkIdentifier(line, verbose, showdbids);
                }
            } catch (IOException ex) {
                System.out.println("Error reading file ["+filename+"].");
                System.exit(1);
            }
        }


    }

    public static void checkIdentifier(String line, boolean verbose, boolean showdbids) {
        line = line.trim();

        Hashtable<String, String> result = URNParser.parseTopoIdent(line);
        String type = result.get("type");
        String so = "\n";
        String vo = "\n";
        String dbo = "\n";

        so = "input id: ["+line+"] ";

        boolean found = false;
        if (type == null) {
            so += "type: null ";
        } else if (type.equals("link") || type.equals("port") || type.equals("node") || type.equals("domain") ) {
            dbo += "db ids: ";
            so += "type: ["+type+"] ";

            String domainId = result.get("domainId");
            Domain dom = domDAO.fromTopologyIdent(domainId);

            if (dom == null) {
                so += "domain not found in DB ";
            } else {
                dbo += "d:[" + dom.getId().toString()+ "] ";
                vo += "domain details:\n";
                vo += "  URL: "+dom.getUrl()+"\n";


                if (type.equals("domain")) {
                    found = true;
                } else if ( type.equals("link") || type.equals("port") || type.equals("node") ) {
                    String nodeId = result.get("nodeId");
                    Node node = nodeDAO.fromTopologyIdent(nodeId, dom);

                    if (node == null) {
                        so += "node not found in DB ";
                    } else {
                        dbo += "n:[" + node.getId().toString()+ "] ";
                        NodeAddress na = node.getNodeAddress();
                        vo += "node details:\n";
                        if (na != null) {
                            vo += "  address: "+na.getAddress()+"\n";
                            dbo += "na: ["+na.getId()+"] ";
                        } else {
                            vo += "  no address\n";
                        }
                        vo += "  valid: "+node.isValid()+"\n";

                        if (type.equals("node")) {
                            found = true;
                        } else if ( type.equals("link") || type.equals("port")) {

                            String portId = result.get("portId");
                            Port port = portDAO.fromTopologyIdent(portId, node);

                            if (port == null) {
                                so += "port not found in DB ";
                            } else {
                                dbo += "p:[" + port.getId().toString()+ "] ";
                                vo += "port details:\n";
                                vo += "  cap: "+ port.getCapacity().toString()+"\n";
                                vo += "  minResCap: "+ port.getMinimumReservableCapacity().toString()+"\n";
                                vo += "  maxResCap: "+ port.getMaximumReservableCapacity().toString()+"\n";
                                vo += "  unResCap: "+ port.getUnreservedCapacity().toString()+"\n";
                                vo += "  valid: "+port.isValid()+"\n";

                                if ( type.equals("port")) {
                                    found = true;
                                } else if ( type.equals("link") ) {
                                    String linkId = result.get("linkId");
                                    Link link = linkDAO.fromTopologyIdent(linkId, port);
                                    if (link == null) {
                                        so += "link not found in DB ";
                                    } else {
                                        dbo += "l:[" + link.getId().toString()+ "] ";
                                        vo += "link details:\n";
                                        vo += "  cap: "+ link.getCapacity().toString()+"\n";
                                        vo += "  minResCap: "+ link.getMinimumReservableCapacity().toString()+"\n";
                                        vo += "  maxResCap: "+ link.getMaximumReservableCapacity().toString()+"\n";
                                        vo += "  unResCap: "+ link.getUnreservedCapacity().toString()+"\n";
                                        vo += "  TEmetric: "+link.getTrafficEngineeringMetric()+"\n";
                                        vo += "  valid: "+link.isValid()+"\n";


                                        L2SwitchingCapabilityData l2data = link.getL2SwitchingCapabilityData();

                                        if (l2data == null) {
                                            vo += "  no l2swcap data\n";
                                        } else {
                                            vo += "    l2swcap: vlans: ["+l2data.getVlanRangeAvailability()+"]\n";
                                            vo += "    l2swcap: mtu: "+l2data.getInterfaceMTU()+"\n";
                                            dbo += "l2: ["+l2data.getId()+"] ";
                                        }
                                        Ipaddr ipaddr = ipaddrDAO.fromLink(link);
                                        if (ipaddr == null) {
                                            vo += "  no ipaddr data \n";
                                        } else {
                                            vo += "  ipaddr ip: "+ipaddr.getIP()+"\n";
                                            dbo += "ip: ["+ipaddr.getId()+"] ";
                                        }

                                        Link remLink = link.getRemoteLink();
                                        if (remLink == null) {
                                            vo += "  no remote link\n";
                                        } else {
                                            vo += "  remote link id: "+remLink.getFQTI()+"\n";
                                            dbo += "rem: ["+remLink.getId()+"] ";
                                        }

                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (type.equals("empty")) {
            // do nothing
        } else {
            so += " wrong type: ["+type+"]";
        }

        if (found) {
            so += " OK, all entries found in DB";
        }
        System.out.println(so);
        if (showdbids || verbose) {
            System.out.println(dbo);
        }
        if (verbose) {
            System.out.println(vo);
        }
    }
}
