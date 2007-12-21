package net.es.oscars.pathfinder.db.util;

import java.util.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;

import org.hibernate.SessionFactory;
import org.jgrapht.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;


public class DBGraphAdapter {
    private SessionFactory sf;
    private String dbname;
    private Properties props;
    private String localDomain;

 
    public DBGraphAdapter(String dbname) {
    	this.dbname = dbname;
        List<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);
        this.sf = HibernateUtil.getSessionFactory(this.dbname);

        
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo", true);
        
        this.localDomain = this.props.getProperty("localdomain").trim();
  
    }
    
    public DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> dbToGraph(Long bandwidth) {
    	DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> g =
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        this.sf.getCurrentSession().beginTransaction();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        /*
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        PortDAO portDAO = new PortDAO(this.dbname);
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        */

        DefaultWeightedEdge edge;



        Domain dom = domainDAO.fromTopologyIdent(this.localDomain);
        Iterator nodeIt = dom.getNodes().iterator();
        while (nodeIt.hasNext()) {
        	Node node = (Node) nodeIt.next();
        	while (!node.isValid()) {
        		node = (Node) nodeIt.next();
        	}
        	String nodeFQTI = TopologyUtil.getFQTI(node);
//        	System.out.println(nodeFQTI);
        	g.addVertex(nodeFQTI);
        	
            Iterator portIt = node.getPorts().iterator();
            while (portIt.hasNext()) {
            	Port port = (Port) portIt.next();
            	if (!port.isValid()) {
            		continue;
            	} 
            	if (bandwidth > 0 && port.getCapacity() < bandwidth) {
            		continue;
                }
            	
            	String portFQTI = TopologyUtil.getFQTI(port);
//            	System.out.println(portFQTI);
                

            	g.addVertex(portFQTI);
            	edge = g.addEdge(nodeFQTI, portFQTI);
                g.setEdgeWeight(edge, 0d);

                edge = g.addEdge(portFQTI, nodeFQTI);
                g.setEdgeWeight(edge, 0d);

            	Iterator linkIt = port.getLinks().iterator();
                while (linkIt.hasNext()) {
                	Link link = (Link) linkIt.next();
                	while (!link.isValid()) {
                		link = (Link) linkIt.next();
                	}
                   	String linkFQTI = TopologyUtil.getFQTI(link);
//                	System.out.println(linkFQTI);
                   	
                	g.addVertex(linkFQTI);
                	edge = g.addEdge(linkFQTI, portFQTI);
                    g.setEdgeWeight(edge, 0d);

                    edge = g.addEdge(portFQTI, linkFQTI);
                    g.setEdgeWeight(edge, 0d);
                	
                	if (link.getRemoteLink() != null) {
                		Link remLink = link.getRemoteLink(); 
                		String remLinkFQTI = TopologyUtil.getFQTI(remLink);
                		if (remLink.isValid() && remLinkFQTI != null) {
                			Double edgeWeight = 10d;
                			if (link.getTrafficEngineeringMetric() != null) {
                				edgeWeight = this.parseTEM(link.getTrafficEngineeringMetric());
                			}
	                		
	                		g.addVertex(remLinkFQTI);
	                		
	                    	edge = g.addEdge(linkFQTI, remLinkFQTI);
	                    	if (edge != null) {
                    	        g.setEdgeWeight(edge, edgeWeight);
                			}
                		}
                	}

                }
            }
        	
        }
        
        
        this.sf.getCurrentSession().getTransaction().commit();
    	return g;
    	
    }
    
    
    double parseTEM(String trafficEngineeringMetric) {
    	double weight = 0d;
    	String[] elems = trafficEngineeringMetric.split(":");
    	if (elems[0].trim().equals("weight")) {
    		weight = new Double(elems[1].trim()).doubleValue();
    	}
    	return weight;
    }
  
    
}
        
