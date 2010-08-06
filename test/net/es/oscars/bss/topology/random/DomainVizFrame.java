package net.es.oscars.bss.topology.random;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.es.oscars.bss.topology.Node;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.ListenableDirectedGraph;

public class DomainVizFrame extends JFrame {
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 800, 600 );
    private JGraphModelAdapter<Node, DefaultEdge> adapter;
    
    
    public DomainVizFrame() {

        setSize(DEFAULT_SIZE);
        setTitle("DomainVizFrame");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/2 - getWidth()/2, 
        size.height/2 - getHeight()/2);
        
   }
    
    
    public void showGraph(DirectedMultigraph<Node, DefaultEdge> graph) {
        if (graph == null) return;
        ListenableDirectedMultigraph<Node, DefaultEdge> listenableGraph 
            = new ListenableDirectedMultigraph<Node, DefaultEdge> (DefaultEdge.class);
        
        adapter = new JGraphModelAdapter<Node, DefaultEdge>( listenableGraph );
        JGraph jgraph = new JGraph(adapter );
        
        //adjustDisplaySettings( jgraph );
        getContentPane().add(new JScrollPane(jgraph));
    }
    



    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );
        jg.setBackground(DEFAULT_BG_COLOR);
    }


    private void positionVertexAt( Object vertex, int x, int y ) {
        DefaultGraphCell cell = adapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );
        Rectangle2D        b    = GraphConstants.getBounds( attr );
        Double dw = b.getWidth();
        Double dh = b.getHeight();
        
        
        GraphConstants.setBounds( attr, new Rectangle( x, y, dw.intValue(), dh.intValue() ) );

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );

    }
    /**
     * a listenable directed multigraph that allows loops and parallel edges.
     */
    private static class ListenableDirectedMultigraph<V, E>
        extends DefaultListenableGraph<V, E>
        implements DirectedGraph<V, E>
    {
        private static final long serialVersionUID = 1L;

        ListenableDirectedMultigraph(Class<E> edgeClass)
        {
            super(new DirectedMultigraph<V, E>(edgeClass));
        }
    }
    
}
