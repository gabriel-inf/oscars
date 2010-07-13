package net.es.oscars.pss.eompls;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.pss.common.PSSDirection;

public class EoMPLSUtils {

    public static ArrayList<String> makeHops(List<PathElem> pathElems, PSSDirection direction) {
        ArrayList<String> pathHops = new ArrayList<String>();
        Node curNode = pathElems.get(0).getLink().getPort().getNode();
        for (PathElem pe : pathElems) {
            Node tmpNode = pe.getLink().getPort().getNode();
            if (!tmpNode.equalsTopoId(curNode)) {
                curNode = tmpNode;
                pathHops.add(pe.getLink().getValidIpaddr().getIP());
            }
        }
        return pathHops;
    }
    
}
