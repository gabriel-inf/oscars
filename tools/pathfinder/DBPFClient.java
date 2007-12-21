import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.pathfinder.db.util.*;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.hibernate.Transaction;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.*;


public class DBPFClient {
	
	private static DomainDAO domDAO;
	
    public static void main(String[] argv) {
        String usage = "Usage:\ndbPfClient.sh\n"; 
        
        DBGraphAdapter dbga = new DBGraphAdapter("bss");

        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);
        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();

        domDAO = new DomainDAO("bss");

        Long bandwidth = 10000000000L;
        
        //Long bandwidth = 0L;
        
        System.out.println("Requested bandwidth is: "+bandwidth.toString());
        
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth);

        String start;
        String end;
        DijkstraShortestPath sp;
        Iterator peIt;

        start = "urn:ogf:network:domain=es.net:node=aofa-cr1:port=xe-7/0/0:link=*";
        end = "urn:ogf:network:domain=es.net:node=sunn-sdn1:port=TenGigabitEthernet2/1:link=*";
        sp = new DijkstraShortestPath(graph, start, end);
        peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next(); 
        	printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }
        
        System.out.println("\n\n");
        
        start = "urn:ogf:network:domain=es.net:node=chic-sdn1:port=TenGigabitEthernet7/3:link=*";
        end = "urn:ogf:network:domain=es.net:node=chi-sl-mr1:port=TenGigabitEthernet4/3:link=*";
        sp = new DijkstraShortestPath(graph, start, end);
        peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();  
        	printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }
        
        System.out.println("\n\n");
        start = "urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet1/3:link=*";
        end = "urn:ogf:network:domain=es.net:node=aofa-mr1:port=TenGigabitEthernet1/3:link=*";
        sp = new DijkstraShortestPath(graph, start, end);
        peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();  
        	printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }
        
        System.out.println("\n\n");
        start = "urn:ogf:network:domain=es.net:node=snll-mr1:port=TenGigabitEthernet2/1:link=TenGigabitEthernet2/1.1103";
        end = "urn:ogf:network:domain=es.net:node=aofa-mr1:port=TenGigabitEthernet1/3:link=*";
        sp = new DijkstraShortestPath(graph, start, end);
        peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();  
        	printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }

    }
    
    
    private static void printEdge(String edge, double edgeWeight) {        	
    	String[] cols = edge.toString().split("\\s\\:\\s");
    	String topoId = cols[0].substring(1);
        Hashtable<String, String> parseResults = TopologyUtil.parseTopoIdent(topoId);
        String type = parseResults.get("type");
        String bandwidth = "";
        String compact = parseResults.get("realcompact");
        Long cap;
    	if (type.equals("link")) {
    		Link link = domDAO.getFullyQualifiedLink(topoId);
    	
    		cap = link.getCapacity() / 1000000;
    		if (cap >= 1000) {
    			cap = cap/1000;
    			bandwidth = cap.toString()+"Gbps";
    		} else {
    			bandwidth = cap.toString()+"Mbps";
    		}
    	} else if (type.equals("port")) {
    		Port port = domDAO.getFullyQualifiedPort(topoId);
    		cap = port.getCapacity() / 1000000;
    		if (cap >= 1000) {
    			cap = cap/1000;
    			bandwidth = cap.toString()+"Gbps";
    		} else {
    			bandwidth = cap.toString()+"Mbps";
    		}
    	}
    	if (type.equals("link") || type.equals("port")) {
    		System.out.println(compact +"     " + edgeWeight + "    " + bandwidth);
    	}
    }
    
}
