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
    public static void main(String[] argv) {
        String usage = "Usage:\ndbPfClient.sh\n"; 
        
        DBGraphAdapter dbga = new DBGraphAdapter("bss");
        
        DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> graph = dbga.dbToGraph();
//        System.out.println(graph.toString());;

        String start = "urn:ogf:network:domain=es.net:node=aofa-cr1:port=xe-7/0/0:link=*";
        String end = "urn:ogf:network:domain=es.net:node=sunn-sdn1:port=TenGigabitEthernet2/1:link=*";
        DijkstraShortestPath sp = new DijkstraShortestPath(graph, start, end);
        Iterator peIt = sp.getPathEdgeList().iterator();
        while (peIt.hasNext()) {
        	DefaultWeightedEdge edge = (DefaultWeightedEdge) peIt.next();  

        	System.out.println(edge.toString() + " w: ["+graph.getEdgeWeight(edge)+"]");
        }
        

    }
}
