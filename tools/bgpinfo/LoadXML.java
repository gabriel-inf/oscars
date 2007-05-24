import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.*;
import net.es.oscars.client.Client;
import net.es.oscars.bss.topology.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.*;
import org.hibernate.*;


import net.es.oscars.pathfinder.*;

import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 


public class LoadXML {

    public static PrintStream output;
    public static String peer = null;

    public static void init ()  {
        output = System.out;
    }

    private static String fName = null;
    private static boolean dryrun = false;
    private static boolean verbosity;

    public static void main(String[] argv) {
        int optind;
        String usage = "loadxml [-v] [-d] -f /path/to/bgpinfo.xml";

        init();

        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-d")) {
                dryrun = true;
            } else
                if (argv[optind].equals("-f")) {
                    fName = argv[++optind];
                } else
                    if (argv[optind].equals("-v")) {
                        verbosity = true;
                        } else
                            if (argv[optind].startsWith("-")) {
                                System.out.println("" + usage);
                                System.out.println("-- Invalid option " + argv[optind]);
                                System.exit(1);
                            } else {
                                break;
                            }
        }
        if (fName == null) {
            System.out.println(usage);
            System.exit(1);
        }
        processFile( fName);
    }


    public static void processFile( String fName ) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(fName));

             // normalize text representation
            doc.getDocumentElement ().normalize ();
            System.out.println ("Root element of the doc is " + 
                 doc.getDocumentElement().getNodeName());


            NodeList listOfPeers = doc.getElementsByTagName("bgp-peer");
            int totalPeers = listOfPeers.getLength();
            System.out.println("Total no of peers : " + totalPeers);

            for(int s=0; s<listOfPeers.getLength() ; s++){
                Node firstPeerNode = listOfPeers.item(s);
                if(firstPeerNode.getNodeType() == Node.ELEMENT_NODE){

                    Element firstPeerElement = (Element)firstPeerNode;

                    //-------
                    NodeList asNameList = firstPeerElement.getElementsByTagName("peer-as");
                    Element asNameElement = (Element)asNameList.item(0);

                    NodeList textasList = asNameElement.getChildNodes();
                    System.out.println("AS : " + 
                           ((Node)textasList.item(0)).getNodeValue().trim());

                    String as = ((Node)textasList.item(0)).getNodeValue().trim();
                    
                    //-------
                    NodeList localAddrList = firstPeerElement.getElementsByTagName("local-address");
                    Element localAddrElement = (Element)localAddrList.item(0);

                    NodeList textLAList = localAddrElement.getChildNodes();
                    System.out.println("Local Addr: " + 
                           ((Node)textLAList.item(0)).getNodeValue().trim());

                    String localAddr = ((Node)textLAList.item(0)).getNodeValue().trim();

                    //----
                    NodeList peerAddrList = firstPeerElement.getElementsByTagName("peer-address");
                    Element peerAddrElement = (Element)peerAddrList.item(0);

                    NodeList textPAList = peerAddrElement.getChildNodes();
                    System.out.println("Peer Addr: " + 
                           ((Node)textPAList.item(0)).getNodeValue().trim());

                    String peerAddr = ((Node)textPAList.item(0)).getNodeValue().trim();

                    updateDB(as,localAddr, peerAddr);

                    /*
                    //----
                    NodeList ageList = firstPersonElement.getElementsByTagName("age");
                    Element ageElement = (Element)ageList.item(0);

                    NodeList textAgeList = ageElement.getChildNodes();
                    System.out.println("Age : " + 
                           ((Node)textAgeList.item(0)).getNodeValue().trim());
                    */

                }//end of if clause
            }//end of for loop with s var

        }catch (SAXParseException err) {
            System.out.println ("** Parsing error" + ", line " 
                 + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.out.println(" " + err.getMessage ());

        }catch (SAXException e) {
            Exception x = e.getException ();
            ((x == null) ? e : x).printStackTrace ();

        }catch (Throwable t) {
            t.printStackTrace ();
        }    
    }

    public static void updateDB( String as, String localAddr, String peerAddr) {

        Initializer initializer = new Initializer();
        initializer.initDatabase();

        Session session;
        session = HibernateUtil.getSessionFactory("bss").getCurrentSession();

        System.out.println("setssion setup");
        System.out.println("setssion setup: " + session);

        PeerIpaddrDAO peerIpaddrDAO;
        List<PeerIpaddr> peerIpaddrList;

        peerIpaddrDAO = new PeerIpaddrDAO();

        peerIpaddrList = peerIpaddrDAO.list();
   
        for (PeerIpaddr p : peerIpaddrList) {
            System.out.println("id " + p.getId() + " ip: " + p.getIp() + " dom: " + p.getDomain() );
        }
    }
}
