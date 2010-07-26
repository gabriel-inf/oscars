package net.es.oscars.pss.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.pss.PSSException;

public class PathUtils {
    
    public static Path getLocalPath(Reservation resv) throws PSSException {
        Path localPath;
        try {
            localPath = resv.getPath(PathType.LOCAL);
        } catch (BSSException e) {
            throw new PSSException(e.getMessage());
        }
        if (localPath == null) {
            throw new PSSException("No local path set");
        }
        PathUtils.checkPath(localPath);
        return localPath;

    }

    
    public static void checkPath(Path localPath) throws PSSException {
        List<PathElem> pathElems = localPath.getPathElems();
        if (pathElems.size() < 2) {
            throw new PSSException("Local path too short");
        }
        PathElem ingressPathElem = pathElems.get(0);
        PathElem egressPathElem = pathElems.get(pathElems.size()-1);
        Link ingressLink = ingressPathElem.getLink();
        if (ingressLink == null) {
            throw new PSSException("Link not set for ingress");
        }
        Link egressLink = egressPathElem.getLink();
        if (egressLink == null) {
            throw new PSSException("Link not set for ingress");
        }

    }

    public static Map<PSSEdgeType, String> getEdgeNodeAddresses(Path localPath) {
        HashMap<PSSEdgeType, String> edgeAddresses = new HashMap<PSSEdgeType, String>();

        List<PathElem> pathElems = localPath.getPathElems();
        Node ingressNode    = pathElems.get(0).getLink().getPort().getNode();
        Node egressNode     = pathElems.get(pathElems.size()-1).getLink().getPort().getNode();
        String ingressAddress = ingressNode.getNodeAddress().getAddress();
        String egressAddress = egressNode.getNodeAddress().getAddress();
        edgeAddresses.put(PSSEdgeType.A, ingressAddress);
        edgeAddresses.put(PSSEdgeType.Z, egressAddress);


        return edgeAddresses;
    }

    public static boolean sameNode(Path localPath) {
        List<PathElem> pathElems = localPath.getPathElems();
        Node ingressNode    = pathElems.get(0).getLink().getPort().getNode();
        Node egressNode     = pathElems.get(pathElems.size()-1).getLink().getPort().getNode();
        return (ingressNode.equals(egressNode));
    }

    public static ArrayList<PathElem> reversePath(List<PathElem> pathElems) {
        ArrayList<PathElem> revPathElems = new ArrayList<PathElem>();
        revPathElems.addAll(pathElems);
        
        for (int i = 0; i < pathElems.size(); i++) {
            PathElem pe = pathElems.get(i);
            revPathElems.set(pathElems.size() - 1 -i, pe);
        }
        return revPathElems;
    }
    
    public static Node getNodeToConfigure(Reservation resv, PSSDirection direction) throws PSSException {
        Path localPath;
        try {
            localPath = resv.getPath(PathType.LOCAL);
        } catch (BSSException e) {
            throw new PSSException(e.getMessage());
        }
        PathElem pe;
        if (direction.equals(PSSDirection.A_TO_Z) || direction.equals(PSSDirection.BIDIRECTIONAL)) {
             pe = localPath.getPathElems().get(0);
        } else {
            pe = localPath.getPathElems().get(localPath.getPathElems().size() - 1);
        }
        Node node = pe.getLink().getPort().getNode();
        return node;
    }

}
