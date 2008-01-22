import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.pathfinder.db.MultiPathFinder;

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
    	String start, end;
        String usage = "Usage:\ndbPfClient.sh\n"; 
        
        DBGraphAdapter dbga = new DBGraphAdapter("bss");

        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);
        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();

        domDAO = new DomainDAO("bss");

//        Long bandwidth = 10 000 000 000L;
        Long bandwidth = 1000000000L;
        //Long bandwidth = 0L;
        
        System.out.println("Requested bandwidth is: "+bandwidth.toString());
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph(bandwidth);
  
        start = "urn:ogf:network:es.net:chi-sl-mr1:TenGigabitEthernet7/1:*";
        end = "urn:ogf:network:es.net:chic-sdn1:TenGigabitEthernet4/1:*";

        doPf(start, end, graph);
        
        

    }
    
    private static void doPf(String start, String end, DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph) {
        start = TopologyUtil.parseTopoIdent(start).get("fqti");
        end   = TopologyUtil.parseTopoIdent(end).get("fqti");

        DijkstraShortestPath sp;
        Iterator peIt;

        sp = new DijkstraShortestPath(graph, start, end);
        peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next(); 
        	printEdge(edge.toString(), graph.getEdgeWeight(edge));
        }
        
        System.out.println("\n\n");
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
