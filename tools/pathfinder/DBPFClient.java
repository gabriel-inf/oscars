import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pathfinder.db.*;
import net.es.oscars.pathfinder.db.util.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

import org.hibernate.*;
import org.hibernate.cfg.*;

import org.jgrapht.alg.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.*;
import java.util.*;


public class DBPFClient {
    private static DomainDAO domDAO;

    public static void main(String[] argv) {
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        SessionFactory sf = HibernateUtil.getSessionFactory("bss");
        sf.getCurrentSession().beginTransaction();
        Date today = new Date();
        Long now = today.getTime();
        now = now / 1000;
        Long startTime = now;
        Long endTime = now + 600;

        String start;
        String end;
        String usage = "Usage:\ndbPfClient.sh\n";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        DBPathfinder dbpf = new DBPathfinder("bss");

        Reservation reservation = new Reservation();
        reservation.setBandwidth(0L);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        String srcEndpoint  = "urn:ogf:network:domain=es.net:node=bnl-mr1:port=TenGigabitEthernet7/3:link=*";
        String destEndpoint = "urn:ogf:network:domain=es.net:node=pnwg-sdn1:port=xe-1/3/0:link=*";
        Path reqPath = new Path();


        Layer2Data l2data = new Layer2Data();
        l2data.setSrcEndpoint(srcEndpoint);
        l2data.setDestEndpoint(destEndpoint);
//        reqPath.setLayer2Data(l2data);

        Layer3Data l3data = new Layer3Data();
        l3data.setSrcHost("198.128.2.62");
        l3data.setDestHost("192.84.86.28");
        reqPath.setLayer3Data(l3data);

        try {
            reqPath.setPathType(PathType.REQUESTED);
            reservation.addPath(reqPath);
        } catch (Exception ex) {
            ex.printStackTrace(pw);
        }


        List<Path> paths = null;
        try {
            paths = dbpf.findLocalPath(reservation);
        } catch (Exception ex) {
            ex.printStackTrace(pw);
            System.out.println("Error: "+ex.getMessage());
            System.out.println(sw.toString());
        }

        if (paths == null) {
            System.out.println("No path");
        } else {

            System.out.println("\nFound path:");
            for (Path path : paths) {

                List<PathElem> pes = path.getPathElems();
                for (PathElem pe : pes) {
                    System.out.println(pe.getLink().getFQTI());
                }
            }
        }

    }


}
