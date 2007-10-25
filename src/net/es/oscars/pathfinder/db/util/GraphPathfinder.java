package net.es.oscars.pathfinder.db.util;

import java.util.*;
import org.jgrapht.*;


public class GraphPathfinder<V, E> {

 
    public GraphPathfinder() {
  
    }
    
    public ArrayList<ArrayList<E>> findAllPaths(
    		DirectedGraph g, 
    		V in, 
    		V out, 
    		ArrayList<E> edgePath) {
    	
    	// int depth = 0;

//        System.out.println(depth+" Looking at in: ["+in+"] out: ["+out+"]");

    	if (!g.containsVertex(in) || !g.containsVertex(out)) {
        	return new ArrayList<ArrayList<E>>();
        }


    	ArrayList<V> nodePath = new ArrayList<V>();
    	ArrayList<E> tmpEdgePath = new ArrayList<E>(); 

    	if (edgePath!= null) {
    		Iterator it = edgePath.iterator();
    		while (it.hasNext()) {
    			E e = (E) it.next();
    			tmpEdgePath.add(e);
    			V src = (V) g.getEdgeSource(e);
//    	    	System.out.println(depth+" edge path src: " +src);
    			if (!nodePath.contains(src)) {
    				nodePath.add(src);
    			}
    		}
        } else {
    		edgePath = new ArrayList<E>();
        }

/*
    	System.out.print(depth+" Node path so far: === ");
    	Iterator node_pathIt = node_path.iterator();
		while (node_pathIt.hasNext()) {
	        System.out.print(" : "+node_pathIt.next());
		}
        System.out.print("\n");

    	System.out.print(depth+" Edge path so far: === ");
    	Iterator edge_pathIt = edge_path.iterator();
		while (edge_pathIt.hasNext()) {
	        System.out.print(" : "+edge_pathIt.next());
		}
        System.out.print("\n");

    
        System.out.println(depth+" Adding ingress node "+in);
*/
        nodePath.add(in);
        
        ArrayList<ArrayList<E>> resultEdgePaths = new ArrayList<ArrayList<E>>();

        if (in.equals(out)) {
//	        System.out.println(depth+" Found end node "+out);
        	resultEdgePaths.add(tmpEdgePath); 
        	return resultEdgePaths;
        }

        Iterator it = g.vertexSet().iterator();

        while (it.hasNext()) {
        	V v = (V) it.next();
        	if (g.containsEdge(in, v)) {
        		E nextEdge = (E) g.getEdge(in, v);
        		
        		ArrayList<E> nextEdgePath = new ArrayList<E>();
	    		Iterator ep_it = tmpEdgePath.iterator();
	    		while (ep_it.hasNext()) {
	    			E e = (E) ep_it.next();
	    			nextEdgePath.add(e);
	    		}

	    		nextEdgePath.add(nextEdge);

//        		System.out.println(depth+" Node "+v+" is neighbor to "+in);
		        ArrayList<ArrayList<E>> childEdgePaths;
		        
        		if (!nodePath.contains(v)) {
        			
        			childEdgePaths = findAllPaths(g, v, out, nextEdgePath);
        			Iterator ne_pathIt = childEdgePaths.iterator();
        			while (ne_pathIt.hasNext()) {
        				ArrayList<E> next_path = (ArrayList<E>) ne_pathIt.next(); 
        				resultEdgePaths.add(next_path);
        			}
        		}
        	}
    	}
        
        return resultEdgePaths;
    }
    
    
    
    public double getLength(DirectedGraph g, List<E> edgePath) {
    	double length = 0d;
		Iterator it = edgePath.iterator();
		while (it.hasNext()) {
			length += g.getEdgeWeight((E) it.next());
		}
    	
    	return length;
    }

    
    
    public double comparePaths(DirectedGraph g, List<E> onePath, List<E> otherPath) {
    	Iterator oneIt = onePath.iterator();
    	
		int commonEdges = 1; 
    	
    	while (oneIt.hasNext()) {
    		E oneEdge = (E) oneIt.next();
    		V oneS = (V) g.getEdgeSource(oneEdge);
    		V oneT = (V) g.getEdgeTarget(oneEdge);
        	Iterator otherIt = otherPath.iterator();
    		while (otherIt.hasNext()) {
        		E otherEdge = (E) otherIt.next();
        		V otherS = (V) g.getEdgeSource(otherEdge);
        		V otherT = (V) g.getEdgeTarget(otherEdge);
        		if (oneS.equals(otherS) && oneT.equals(otherT)) {
        			commonEdges++;
        		}
    		}
    	}
    	return 1d/commonEdges;
    }

    
}
        
