package net.es.oscars.rmi.bss;

/**
 * Make sure the graph is populated for an object.  Need to look more at
 * Hibernate to see if there is a way to force load of complete graph with
 * query.
 *
 * @author Evangelos Chaniotakis, David Robertson
 */

import java.util.*;

import org.hibernate.Hibernate;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.MPLSData;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.PathElemParam;

/**
 * BssRmiUtils - Hibernate utils for BSS RMI interface
 */
public class BssRmiUtils {

    public static void initialize(Reservation reservation) {
        Hibernate.initialize(reservation);
        Hibernate.initialize(reservation.getToken());
        Set<Path> paths = reservation.getPaths();
        Iterator<Path> pathIt = paths.iterator();
        while (pathIt.hasNext()) {
            Path path = pathIt.next();
            // Hibernate.initialize(path);
            Hibernate.initialize(path.getLayer2DataSet());
            Hibernate.initialize(path.getLayer3DataSet());
            Hibernate.initialize(path.getMplsDataSet());
            Hibernate.initialize(path.getNextDomain());
            for (PathElem pe : path.getPathElems()) {
                Hibernate.initialize(pe.getLink());
                Hibernate.initialize(pe.getLink().getIpaddrs());
                Hibernate.initialize(pe.getLink().getRemoteLink());
                Hibernate.initialize(pe.getLink().getL2SwitchingCapabilityData());
                Hibernate.initialize(pe.getLink().getPort());
                Hibernate.initialize(pe.getLink().getPort().getNode());
                Hibernate.initialize(pe.getLink().getPort().getNode().getNodeAddress());
                Hibernate.initialize(pe.getLink().getPort().getNode().getDomain());
                Hibernate.initialize(pe.getLink().getPort().getNode().getDomain().getSite());
                for (PathElemParam pep: pe.getPathElemParams()) {
                    Hibernate.initialize(pep);
                }
            }
        }
    }
}
