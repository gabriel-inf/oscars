package net.es.oscars.pss.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.pss.PSSException;

public class PathUtils {
    public enum EdgeType {
        INGRESS,
        EGRESS
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

    public static Map<EdgeType, String> getEdgeNodeAddresses(Path localPath) {
        HashMap<EdgeType, String> edgeAddresses = new HashMap<EdgeType, String>();

        List<PathElem> pathElems = localPath.getPathElems();
        Node ingressNode    = pathElems.get(0).getLink().getPort().getNode();
        Node egressNode     = pathElems.get(pathElems.size()-1).getLink().getPort().getNode();
        String ingressAddress = ingressNode.getNodeAddress().getAddress();
        String egressAddress = egressNode.getNodeAddress().getAddress();
        edgeAddresses.put(EdgeType.INGRESS, ingressAddress);
        edgeAddresses.put(EdgeType.EGRESS, egressAddress);


        return edgeAddresses;
    }

    public static boolean sameNode(Path localPath) {
        List<PathElem> pathElems = localPath.getPathElems();
        Node ingressNode    = pathElems.get(0).getLink().getPort().getNode();
        Node egressNode     = pathElems.get(pathElems.size()-1).getLink().getPort().getNode();
        return (ingressNode.equals(egressNode));
    }


}
