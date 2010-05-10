package net.es.oscars.pss;

import java.util.*;

import org.apache.log4j.Logger;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.PCEUtils;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * Class handling set up of path-related variables for router configuration for
 * all vendors, for layer 2 and layer 3.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class LSPData {
    private String dbname;
    // common layer 2 and layer 3 parameters
    private PathElem ingressPathElem;
    private PathElem egressPathElem;
    private PathElem lastXfacePathElem;
    private Link ingressLink;
    private Link egressLink;
    private String ingressVlanTag;
    private String egressVlanTag;
    private String ingressRtrLoopback;
    private String egressRtrLoopback;
    private Logger log;

    /** Constructor. */
    public LSPData(String dbname) {
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    public PathElem getIngressPathElem() { return this.ingressPathElem; }
    public PathElem getEgressPathElem() { return this.egressPathElem; }
    public PathElem getLastXfaceElem() { return this.lastXfacePathElem; }

    public Link getIngressLink() { return this.ingressLink; }
    public Link getEgressLink() { return this.egressLink; }


    public String getIngressRtrLoopback() { return this.ingressRtrLoopback; }
    public String getEgressRtrLoopback() { return this.egressRtrLoopback; }

    /**
     * Given path, sets elements in path used in setting a number of
     * configuration variables.
     *
     * @param pathElems reservation's intradomain path
     * @throws PSSException
     */
    public void setPathVars(List<PathElem> pathElems)
            throws PSSException {

        this.ingressPathElem = pathElems.get(0);
        // check probably unnecessary
        if (pathElems.size() > 2) {
            this.lastXfacePathElem = pathElems.get(pathElems.size()-2);
        }
        this.egressPathElem = pathElems.get(pathElems.size()-1);
        this.ingressLink = this.ingressPathElem.getLink();
        this.egressLink = this.egressPathElem.getLink();
    }

    /**
     * Gets necessary info from layer 2 path to configure router.
     * @param getLoopbacks boolean indicating whether should get loopback info
     *
     * @throws PSSException
     */
    public void setLayer2PathInfo(boolean getLoopbacks) throws PSSException {

        // assume just one VLAN for now
        try {
            PathElemParam pep;
            pep = this.ingressPathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                                        PathElemParamType.L2SC_VLAN_RANGE);
            this.ingressVlanTag = pep.getValue();
            pep = this.egressPathElem.getPathElemParam(PathElemParamSwcap.L2SC,
                    PathElemParamType.L2SC_VLAN_RANGE);
            this.egressVlanTag = pep.getValue();
        } catch (BSSException ex) {
            throw new PSSException(ex.getMessage());
        }
        if (this.ingressVlanTag == null) {
            throw new PSSException("Ingress VLAN tag is null!");
        }
        if (this.egressVlanTag == null) {
            throw new PSSException("Egress VLAN tag is null!");
        }
        if (!getLoopbacks) {
            return;
        }

        // find ingress loopback
        NodeAddress ingressNodeAddress =
        this.ingressLink.getPort().getNode().getNodeAddress();
        String ingressAddr = ingressNodeAddress.getAddress();
        PCEUtils utils = new PCEUtils(this.dbname);
        this.ingressRtrLoopback = utils.getIP(ingressAddr);
        if (this.ingressRtrLoopback == null) {
            throw new PSSException("no ingress loopback in path");
        }

        NodeAddress egressNodeAddress =
            this.egressLink.getPort().getNode().getNodeAddress();
        String egressAddr = egressNodeAddress.getAddress();
        this.egressRtrLoopback = utils.getIP(egressAddr);
        if (this.egressRtrLoopback == null) {
            throw new PSSException("no egress loopback in path");
        }
    }

    /**
     * Gets loopbacks from layer 3 path to configure router.
     *
     * @param sysDescr router type
     * @throws PSSException
     */
    public void setLayer3PathInfo(String sysDescr)
            throws PSSException {

        log.debug("setLayer3PathInfo.start");
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = null;
        ipaddr = ipaddrDAO.queryByParam("linkId", this.ingressLink.getId());
        log.debug("ingress link IP address: "+ipaddr.getIP());
        PCEUtils utils = new PCEUtils(this.dbname);
        try {
            this.ingressRtrLoopback = utils.getLoopback(ipaddr.getIP(), sysDescr);
        } catch (PathfinderException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        if (this.ingressRtrLoopback == null) {
            throw new PSSException("no ingress loopback in path");
        }

        this.egressRtrLoopback =
            this.egressLink.getPort().getNode().getNodeAddress().getAddress();
        if (this.egressRtrLoopback == null) {
            throw new PSSException("no egress loopback in path");
        }
        log.debug("setLayer3PathInfo.end");
    }

    /**
     * Gets IP addresses of all hops in path except ingress and egress.
     *
     * @param pathElems reservation's path list
     * @param direction string indicating whether forward or reverse direction
     * @param useLocalHops boolean indicating whether to use all hops in path
     * @throws PSSException
     */
    public List<String> getHops(List<PathElem> pathElems, String direction,
                                boolean useLocalHops)
            throws PSSException {

        List<String> hops = new ArrayList<String>();
        List<String> restrictedHops = new ArrayList<String>();
        // this gets everything except the ingress and egress, which we
        // don't want
        for (int i=1; i < pathElems.size()-1; i++) {
            PathElem pathElem = pathElems.get(i);
            Link link = pathElem.getLink();
            Ipaddr ipaddr = link.getValidIpaddr();
            if (ipaddr == null) {
                throw new PSSException("No IP for link: ["+link.getFQTI()+"]");
            }
            hops.add(ipaddr.getIP());
        }
        if (direction.equals("reverse")) {
            ArrayList<String> reverseHops = new ArrayList<String>();
            for (int i=hops.size()-1; i >= 0; i--) {
                reverseHops.add(hops.get(i));
            }
            hops = reverseHops;
        }
        // if not using all hops in path
        if (!useLocalHops) {
            // only have every other hop in path
            for (int i=0; i < hops.size(); i++) {
                if ((i % 2) == 1) {
                    restrictedHops.add(hops.get(i));
                }
            }
            return restrictedHops;
        }
        return hops;
    }

    public void setIngressVlanTag(String ingressVlanTag) {
        this.ingressVlanTag = ingressVlanTag;
    }

    public String getIngressVlanTag() {
        return ingressVlanTag;
    }

    public void setEgressVlanTag(String egressVlanTag) {
        this.egressVlanTag = egressVlanTag;
    }

    public String getEgressVlanTag() {
        return egressVlanTag;
    }
}
