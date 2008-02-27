import net.es.oscars.bss.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.db.*;
import net.es.oscars.pathfinder.db.util.*;

import net.es.oscars.notify.*;

import org.hibernate.*;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.io.*;
import org.jdom.*;
import org.jdom.output.*;


public class Runner {
    private static DomainDAO domDAO;

	public static void main(String[] argv) {
    	/*
           NotifyInitializer ntfInit = new NotifyInitializer();
           NotifierSource src = ntfInit.getSource();
           String[] message = new String[2];
           message[0] = "hello";
           message[1] = "abc";
           src.eventOccured(message);
           */
    	
        Initializer init = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");
        init.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();
        domDAO = new DomainDAO("bss");
        Domain dom = domDAO.getLocalDomain();

        Domain newDom = new Domain(dom);

        HashSet<Node> newNodes = new HashSet<Node>();
        newDom.setNodes(newNodes);

        Link start = null;
        Link end = null;
        for (Link l : dom.getEdgeLinks()) {
        	String topoId = TopologyUtil.getFQTI(l);
        	System.out.println(topoId);
        	if (start == null) {
        		start = l;
        	} else if (end == null) {
        		end = l;
        	}
        	
        	Node oldNode = l.getPort().getNode();
        	boolean foundNode = false;
        	Node theNode = null;
        	Iterator nodeIt = newDom.getNodes().iterator();
        	while (nodeIt.hasNext()) {
        		Node tmpNode = nodeIt.next();
        		if (tmpNode.equalsTopoId(oldNode)) {
        			foundNode = true;
            		theNode = tmpNode;
        		}
        	}
        	
        	if (!foundNode) {
        		Node newNode = new Node(oldNode);
            	HashSet<Port> newPorts = new HashSet<Port>();
        		newNode.setDomain(newDom);
        		newNode.setPorts(newPorts);

        		HashSet nodes = newDom.getNodes();
        		nodes.add(newNode);
        		newDom.setNodes(nodes);
        		theNode = newNode;
        	}
        	
        	
        	
        	Port oldPort = l.getPort();
        	Port thePort = null;
        	String oldPortTopoId = TopologyUtil.getFQTI(oldPort);
        	Iterator portIt = theNode.getPorts().iterator();
        	boolean foundPort = false;
        	while (portIt.hasNext()) {
        		Port tmpPort = (Port) portIt.next();
        		if (tmpPort.equalsTopoId(oldPort)) {
        			foundPort = true;
        			thePort = tmpPort;
        		}
        	}
        	
        	if (!foundPort) {
        		Port newPort = new Port(oldPort);
        		newPort.setNode(theNode);
        		newPort.setLinks(null);

        		thePort = newPort;
        		HashSet<Port> ports = new HashSet<Port>(theNode.getPorts());
        		ports.add(thePort);
        		theNode.setPorts(ports);
        	}
        }
        /*
        DBPathfinder dbpf = new DBPathfinder("bss");
        
        Path path = dbpf.findPathBetween(start, end, 0L);
        if (path == null) {
        	System.out.println("No path!"); 
        	return;
        }
        
        PathElem pe = path.getPathElem();
        while (pe != null) {
        	Link l = pe.getLink();
        	Link remLink = l.getRemoteLink();
        	System.out.println(TopologyUtil.getFQTI(l)+" "+l.getMaximumReservableCapacity());
        	System.out.println("\t"+TopologyUtil.getFQTI(remLink)+" "+remLink.getMaximumReservableCapacity());
        	pe = pe.getNextElem();
        }
        */
        
        Topology topology = new Topology();
        topology.addDomain(newDom);
        HashSet nodeSet = new HashSet(newNodes);
        newDom.setNodes(nodeSet);
        
        
        
        
        
        
        TopologyXMLExporter exp = new TopologyXMLExporter("bss");
        Document doc = exp.getTopology(topology); 
        XMLOutputter outputter = new XMLOutputter();
        Format format = org.jdom.output.Format.getPrettyFormat();
        
        outputter.setFormat(format);
        try {
          outputter.output(doc, System.out);       
        }
        catch (IOException e) {
          System.err.println(e);
        }
        
        
        
        /*
        DBGraphAdapter dbga = new DBGraphAdapter("bss");
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(0L);
        DijkstraShortestPath sp;
        Iterator peIt;

        sp = new DijkstraShortestPath(graph, start, end);

        if ((sp == null) || (sp.getPathEdgeList() == null)) {
            System.out.println("No path!");
            return;
        }

        peIt = sp.getPathEdgeList().iterator();

        while (peIt.hasNext()) {
            DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();
            printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }

        System.out.println("\n\n");
*/
        
/*
        ReservationDAO resDao = new ReservationDAO("bss");
        Reservation res = resDao.findById(2367, false);

        String traceId = History.makeTraceId("http://www.es.net/");
        Long time = new Date().getTime()/1000;
        System.out.println(time.toString());
*/
        /*
        HistoryDAO hdao = new HistoryDAO("bss");
        History hentry = new History();
        hentry.setReservation(res);
        hentry.setDescription("foo");
        hentry.setTraceId(traceId);
        hentry.setResult("SUCCESS");
        hentry.setReceivedFrom("");
        hentry.setForwardedTo("");
        hentry.setOperationType("CREATE");
        hentry.setOperationTime(time);
        hdao.create(hentry); */
        
        
        /*
        JobDAO jdao = new JobDAO("bss");
        Job jentry = new Job();
        jentry.setReservation(res);
        jentry.setOperation("PATH_SETUP");
        jentry.setResult("");
        jentry.setDone(false);
        jentry.setScheduledTime(time);
        jdao.create(jentry);
        ArrayList<Job> jobs = new ArrayList<Job>(); 
        try {
        	jobs = (ArrayList<Job>) jdao.list(time - 2000L, time + 2000L);
        } catch (BSSException e) {
        	System.out.println("error: "+e.getMessage());
        }
        for (Job j : jobs) {
        	System.out.println(j.getOperation());
        }
        */

    }


}
