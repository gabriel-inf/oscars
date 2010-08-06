package net.es.oscars.bss.topology.random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.NodeAddress;
import net.es.oscars.bss.topology.random.RandomDomainGeneratorParams.Bandwidths;
import net.es.oscars.bss.topology.random.RandomDomainGeneratorParams.LinkRole;
import net.es.oscars.bss.topology.random.RandomDomainGeneratorParams.TopologyType;

public class RandomDomainGenerator {

    private Random rand;
    private static RandomDomainGenerator instance;
    
    
    public static RandomDomainGenerator getInstance() {
        if (instance == null) {
            instance = new RandomDomainGenerator();
        }
        return instance;
        
    }
    
    private RandomDomainGenerator() {
    }
    
    
    protected List<String> getIfceNames(Long bw, Integer num) {
        ArrayList<String> names = new ArrayList<String>();
        
        String media = "ge-";
        if (bw == Bandwidths.TENGBPS) media = "xe-";
        
        for (int i = 0; i < num; ) {
            Integer fpc = rand.nextInt(10);
            Integer pic = rand.nextInt(4);
            Integer port;
            if (bw >= Bandwidths.TENGBPS) {
                port = rand.nextInt(8);
            } else {
                port = rand.nextInt(16);
            }
            String name = media+fpc+"/"+pic+"/"+port;
            if (!names.contains(name)) {
                i++;
                names.add(name);
            }
        }
        return names;
    }
    
    protected List<String> getNodeNames(Integer num) {
        String[] names = {
                "alfa", "bravo", "charlie", "delta", "echo", "foxtrot",
                "golf", "hotel", "india", "juliet", "kilo", "lima",
                "mike", "november", "oscar", "papa", "quebec", "romeo",
                "sierra", "tango", "uniform", "victor", "whiskey", "xray", 
                "yankee", "zulu"
        };
        
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < num; i++) {
            Integer index = i % names.length;
            result.add(names[index]);
        }
        return result;
    }
    
    protected List<String> getNodeAddresses(Integer num) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < num; ) {
            Integer ip = rand.nextInt(254);
            String address = "10.0.1."+(ip);
            if (!result.contains(address)) {
                result.add(address);
                i++;
            }
        }
        return result;
    }
    
    protected List<String> getLinkAddresses(Integer num) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < num; ) {
            
            Integer ip = rand.nextInt(254);
            String address = "10.0.20."+(ip);
            if (!result.contains(address)) {
                result.add(address);
                i++;
            }
        }
        return result;
    }
    
    
    public class DomainResult {
        public Domain domain;
        public ArrayList<Link> edgeLinks;
        DirectedMultigraph<Node, DefaultEdge> graph;
    }
    

    
    
    
    public DomainResult makeDomain(RandomDomainGeneratorParams params) {
        rand = new Random(params.seed);
        DomainResult res = new DomainResult();
        Domain dom = new Domain();
        dom.setLocal(true);
        dom.setTopologyIdent("random.net");
        
        Integer numNodes = params.minNodes + rand.nextInt(params.maxNodes - params.minNodes + 1);
        System.out.println("\n\nnodes:"+numNodes);
        List<String> nodeNames = this.getNodeNames(numNodes);
        List<String> nodeAddresses = this.getNodeAddresses(numNodes);
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < numNodes; i++) {
            Node node = new Node();
            node.setDomain(dom);
            node.setTopologyIdent(nodeNames.get(i));
            node.setValid(true);
            node.setId(i);
            NodeAddress addr = new NodeAddress();
            addr.setAddress(nodeAddresses.get(i));
            node.setNodeAddress(null);
            nodes.add(node);
        }
        
        
        DirectedMultigraph<Node, DefaultEdge> graph = this.makeAdjacencies(params, nodes);
        
        res.graph = graph;
        return res;
        
    }

    private DirectedMultigraph<Node, DefaultEdge> makeConnectedGraph(List<Node> nodes, Integer maxDegree) {
        Integer numNodes = nodes.size();
        if (numNodes == 1) return null;
        
        
        DirectedMultigraph<Node, DefaultEdge> graph = new DirectedMultigraph<Node, DefaultEdge>(DefaultEdge.class);

        System.out.println("making a connected graph, nodes: "+numNodes+" maxDegree: "+maxDegree);
        
        // try begins. initialize data structures
        Integer[][] adjMatrix = new Integer[numNodes][numNodes];
        ArrayList<Integer> degrees = new ArrayList<Integer>();
        for (int i = 0; i < numNodes; i++) {
            degrees.add(0);
            for (int j = 0; j < numNodes; j++) {
                adjMatrix[i][j] = 0;
            }
        }
        
        // first make sure the graph is connected
        // go over existing nodes, connect each of them to one 
        // over the previous ones. disallow going over maxDegree
        for (int i = 1; i < numNodes; i++) {
            
            int nei = rand.nextInt(i);
            int connTries = 0;
            int maxConnTries = 100;
            // System.out.println(i+" candidate: "+nei+" deg: "+degrees.get(nei));
            while (degrees.get(nei) >= maxDegree || connTries < connTries) {
                nei =  rand.nextInt(i);
                connTries++;
            }
            if (connTries >= maxConnTries) {
                System.out.println("could not generate a connected graph");
                return null;
            }
            degrees.set(i,   degrees.get(i)+1);
            degrees.set(nei, degrees.get(nei)+1);
//            System.out.println(i+ " " +nei + " "+ degrees.get(i) + " " + degrees.get(nei));
            adjMatrix[i][nei] = adjMatrix[i][nei] + 1;
            adjMatrix[nei][i] = adjMatrix[nei][i] + 1;
        }
        for (int i = 0; i < numNodes; i++) {
            graph.addVertex(nodes.get(i));
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                graph.addEdge(nodes.get(i), nodes.get(j));
            }
        }

        
        
        
        return graph;
        
    }

    @SuppressWarnings("unchecked")
    private DirectedMultigraph<Node, DefaultEdge> makeAdjacencies(RandomDomainGeneratorParams params, List<Node> nodes) {
        Integer numNodes = nodes.size();
        if (numNodes == 1) return null;
        boolean retry = false;
        Integer tries = 0;
        Integer maxTries = 1000;
        DirectedMultigraph<Node, DefaultEdge> graph;
        do {
            graph = this.makeConnectedGraph(nodes, params.maxBBLinks);
            tries++;
            if (graph == null) {
                retry = true;
                if (tries >= maxTries) {
                    break;
                }
                continue;
            }
            return graph;
            
            /*
            
            
            ArrayList<Integer> degrees = new ArrayList<Integer>();
            // copy over data structures in prep for minBBLinks
            Integer[][] tmpAdjMatrix = adjMatrix.clone();
            // print inital connected graph
            for (int i = 0; i < numNodes; i++) {
                Integer deg = 0;
                for (int j = 0; j < numNodes; j++) {
                    System.out.print(" "+tmpAdjMatrix[i][j]+" ");
                    deg += tmpAdjMatrix[i][j];
                }
                degrees.add(deg);
                System.out.println(" --- "+deg);
            }
            
            
            ArrayList<Integer> tmpDegrees = (ArrayList<Integer>) degrees.clone();
            ArrayList<Integer> needNeighbors = new ArrayList<Integer>();
            for (int j = 0; j < numNodes; j++) {
                if (tmpDegrees.get(j) < params.minBBLinks) needNeighbors.add(j);
            }
            
            
            for (int i : needNeighbors) {
                while (tmpDegrees.get(i) < params.minBBLinks) {
//                    System.out.println("finding new neighbors for "+i+" curDeg = "+tmpDegrees.get(i));
                    ArrayList<Integer> candidates = new ArrayList<Integer>();
                    for (int j = 0; j < numNodes; j++) {
                        if (tmpDegrees.get(j) < params.maxBBLinks && j != i) {
//                            System.out.println("Candidate: "+j+" "+tmpDegrees.get(j));
                            candidates.add(j);
                        }
                    }
                    if (candidates.isEmpty()) {
                        retry = true;
                        break; 
                    }
                    Integer nei = candidates.get(rand.nextInt(candidates.size()));
                    tmpDegrees.set(i,   tmpDegrees.get(i)+1);
                    tmpDegrees.set(nei, tmpDegrees.get(nei)+1);
                    tmpAdjMatrix[i][nei] = tmpAdjMatrix[i][nei] + 1;
                    tmpAdjMatrix[nei][i] = tmpAdjMatrix[nei][i] + 1;
                    System.out.println(i+ " new neighbor: " +nei + " "+ tmpDegrees.get(i) + " " + tmpDegrees.get(nei));
                }
            }
            
            // check if we need to retry
            if (retry) {
                if (tries >= maxTries) {
                    break;
                }
                continue;
            }
            
            for (int i = 0; i < numNodes; i++) {
                Integer deg = 0;
                for (int j = 0; j < numNodes; j++) {
                    System.out.print(" "+tmpAdjMatrix[i][j]+" ");
                    deg += tmpAdjMatrix[i][j];
                }
                System.out.println(" --- "+deg);
            }
            
            */

            
        } while (retry);

            
        
        
        return graph;
    }
    
    
    
    
}
