import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.db.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import org.hibernate.*;
import org.hibernate.cfg.*;

import org.jgrapht.alg.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.*;


public class ReservationVisualizer {
    private static DomainDAO domDAO;

    public static void main(String[] argv) {
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        Session ses = sf.getCurrentSession();
        ses.beginTransaction();

        Topology topo = new Topology();
        topo.setDomains(ses.createQuery("from Domain").list());

        ReservationManager rm = new ReservationManager("bss");

        ArrayList<String> statuses = new ArrayList<String>();
        statuses.add("ACTIVE");



        GraphVizExporter ge = new GraphVizExporter();
        String neatoFile="neato.png";
        String dotFile="dot.png";
        String graphFile="graph.dot";

        try {

            ge.exportTopology(topo, rm.list(null, null, statuses, "FNAL", null, null, null, null));

            String dot = ge.getDotSource();
            ge.writeDotSourceToFile(dot, graphFile);

            ge.setDOT("/usr/bin/neato");
            ge.writeGraphToFile(neatoFile);

//            ge.setDOT("/usr/bin/dot");
//            ge.writeGraphToFile(dotFile);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }


    }
}
