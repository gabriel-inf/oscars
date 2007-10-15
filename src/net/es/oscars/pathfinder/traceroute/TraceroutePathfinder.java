package net.es.oscars.pathfinder.traceroute;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.bss.topology.*;

/**
 * TraceroutePathfinder performs traceroutes to find path from
 * source to destination within the local domain.  It operates at
 * layer 3.  TODO: also handles explicit paths which may be layer 2.  Separate
 * out in that case.
 */
public class TraceroutePathfinder extends Pathfinder implements PCE {
    private Logger log;
    private Properties props;
    private Utils utils;

    public TraceroutePathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("traceroute", true);
        this.utils = new Utils(dbname);
        super.setDatabase(dbname);
    }

    /**
     * Finds path from source to destination, taking into account ingress
     * and egress nodes if specified by user.
     *
     * @throws PathfinderException
     */
    public boolean findPath(PathInfo pathInfo) throws PathfinderException {

        String ingressNodeId = null;
        String egressNodeId = null;

        this.log.debug("findPath.start");
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            DomainDAO domainDAO = new DomainDAO(this.dbname);
            CtrlPlaneHopContent[] ctrlPlaneHops = path.getHop();
            // if an ERO, not just ingress and/or egress
            if (ctrlPlaneHops.length > 2) {
               this.log.info("handling explicit route object");
               this.handleExplicitRouteObject(pathInfo);
               return true;
            // handle case where only ingress, egress, or both given
            } else if (ctrlPlaneHops.length == 2) {
                this.log.info("handling just ingress and egress");
                // convert to IP if necessary
                ingressNodeId = ctrlPlaneHops[0].getLinkIdRef();
                if (TopologyUtil.isTopologyIdentifier(ingressNodeId)) {
                    ingressNodeId = TopologyUtil.getLocalForm(ingressNodeId);
                    ingressNodeId =
                        domainDAO.convertTopologyIdentifier(ingressNodeId);
                // at this point, has to be a DNS name, an IPv4 address,
                // or an IPv6 address
                } else {
                    ingressNodeId = this.utils.getIP(ingressNodeId);
                }
                egressNodeId = ctrlPlaneHops[1].getLinkIdRef();
                if (TopologyUtil.isTopologyIdentifier(egressNodeId)) {
                    egressNodeId = TopologyUtil.getLocalForm(egressNodeId);
                    egressNodeId =
                        domainDAO.convertTopologyIdentifier(egressNodeId);
                } else {
                    egressNodeId = this.utils.getIP(egressNodeId);
                }
            }
        }
        this.traceroutePath(pathInfo, ingressNodeId, egressNodeId);
        this.log.debug("findPath.End");
        return false;
    }

    /**
     * Handles an explicit route object that may or may not contain
     * hops internal to this domain.  TODO:  put in a separate class.
     * Currently handles only layer 2 ERO's.
     *
     * @param ero CtrlPlanePathContent instance with complete path
     * @throws PathfinderException
     */
    private void handleExplicitRouteObject(PathInfo pathInfo)
            throws PathfinderException {

        boolean isTopologyId = false;
        int numHops = 0;

        this.log.debug("handleExplicitRouteObject.start");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = domainDAO.getLocalDomain();
        CtrlPlanePathContent ero = pathInfo.getPath();
        CtrlPlaneHopContent[] hops = ero.getHop();
        // only need IP addresses at this point in case only loose hops
        for (int i=0; i < hops.length; i++) {
            CtrlPlaneHopContent ctrlPlaneHop = hops[i];
            String hopId = ctrlPlaneHop.getLinkIdRef();
            this.log.debug("hop id (original):["+hopId+"]");
            hopId = TopologyUtil.getLocalForm(hopId);
            this.log.debug("hop id (local):["+hopId+"]");
            String[] componentList = hopId.split(":");
            if (domainDAO.isLocal(componentList[3])) {
                numHops++;
            }
            // reset id ref to local topology identifier
            ctrlPlaneHop.setLinkIdRef(hopId);
            // make schema validator happy
            ctrlPlaneHop.setId(hopId);
        }
        // throw error if no local path found
        if (numHops == 0) { 
            throw new PathfinderException("No local hops given in ERO");
        }
        this.log.debug("handleExplicitRouteObject.finish");
    }

    /**
     * Using traceroute, finds path from source to destination, taking into
     * account ingress and egress nodes if specified by user.
     *
     * @throws PathfinderException
     */
    private void traceroutePath(PathInfo pathInfo, String ingressNodeIP,
                                String egressNodeIP)
            throws PathfinderException {

        List<String> hops = null;
        List<String> reverseHops = null;
        List<String> previousHops = new ArrayList<String>();
        List<String> laterHops = null;
        String ingressIP = null;
        String loopbackIP = null;
        CtrlPlanePathContent ctrlPlanePath = null;

        this.log.debug("traceroutePath.start");
        if (ingressNodeIP != null) {
            // check ingress, if given, for validity
            this.checkIngress(ingressNodeIP);
        }
        if (egressNodeIP != null) {
            // check egress, if given, for validity
            this.checkEgress(egressNodeIP);
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        // layer 3 path will have complete path from source to destination;
        // stopgap for layer 2 testing only, where only have loose hops
        if ((layer3Info == null) &&
                (ingressNodeIP != null) && (egressNodeIP != null)) {
            // get interior path
            loopbackIP = this.utils.getLoopback(ingressNodeIP, null);
            hops = this.traceroute(loopbackIP, egressNodeIP);
            // TODO:  fix traceroute
            hops.set(0, ingressNodeIP);
            ctrlPlanePath = this.pathFromHops(hops);
            pathInfo.setPath(ctrlPlanePath);
            return;
        }
        if (layer3Info == null) {
            throw new PathfinderException("No layer 3 information provided");
        }
        String srcHost = layer3Info.getSrcHost();
        if (srcHost == null) {
            throw new PathfinderException( "no source for path given");
        }
        String destHost = layer3Info.getDestHost();
        if (destHost == null) {
            throw new PathfinderException( "no destination for path given");
        }
        if (ingressNodeIP != null) {
            // since storing complete path, need hops before ingress
            reverseHops = this.reversePath(ingressNodeIP, srcHost);
            // go in forward direction for later addition to the path
            for (int i=reverseHops.size()-1; i >= 0; i--) {
                previousHops.add(reverseHops.get(i));
            }
            ingressIP = ingressNodeIP;
        // else if the ingress is not given, find it by doing a reverse path
        } else {
            reverseHops = this.reversePath(egressNodeIP, srcHost);
            // go in forward direction to get Juniper ingress
            for (int i=reverseHops.size()-1; i >= 0; i--) {
                previousHops.add(reverseHops.get(i));
            }
            // make sure this path contains a Juniper router
            for (String hop: previousHops) {
                this.log.info(hop);
                loopbackIP = this.utils.getLoopback(hop, "Juniper");
                if (loopbackIP != null) {
                    break;
                }
            }
            if (loopbackIP == null) {
                throw new PathfinderException("path between src and host " +
                    "does not contain a Juniper router");
            }

        }
        // if no explicit egress, just find path from ingress to destination
        if (egressNodeIP == null) {
            hops = this.traceroute(loopbackIP, destHost);
            // don't store loopback in path
            hops.set(0, ingressIP);
        } else {
            // get hops from egress to destHost (to get next hop)
            String egressLoopback = this.utils.getLoopback(egressNodeIP, null);
            laterHops = this.traceroute(egressLoopback, destHost);
            laterHops.set(0, egressNodeIP);
            if (laterHops.size() == 1) {
                throw new PathfinderException("no hops past egress node: " +
                                              egressNodeIP);
            }
            // get interior path
            hops = this.traceroute(loopbackIP, egressNodeIP);
            hops.set(0, ingressIP);
        }
        List<String> completeHops = this.stitch(previousHops, hops, laterHops);
        ctrlPlanePath = this.pathFromHops(completeHops);
        pathInfo.setPath(ctrlPlanePath);
        this.log.debug("findPath.End");
    }

    /**
     * Checks ingress, if given, for validity.  It must be associated with
     * a Juniper router for now.
     *
     * @param ingressNodeIP string with IP of ingress to check
     */
    private void checkIngress(String ingressNodeIP) throws PathfinderException {

        String loopbackIP = null;
        // if not placeholder allowing only one of ingress or egress to
        // be set
        if (!ingressNodeIP.equals("*")) {
             // make sure the given ingress node is a Juniper router
             loopbackIP =
                     this.utils.getLoopback(ingressNodeIP, "Juniper");
             if (loopbackIP == null) {
                 throw new PathfinderException("given ingress node " +
                         ingressNodeIP + " is not a Juniper router");
             }
        }
    }

    /**
     * Checks egress, if given, for validity.  It must be associated with
     * a node that has a wan-loopback.
     *
     * @param egressNodeIP string with IP of egress to check
     */
    private void checkEgress(String egressNodeIP) throws PathfinderException {

        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        // if not placeholder
        if (!egressNodeIP.equals("*")) {
            // make sure the given egress node is in the database
            if (ipaddrDAO.queryByParam("IP", egressNodeIP) == null) {
                 throw new PathfinderException("given egress node: " +
                     egressNodeIP + " is not in the database");
            }
        }
    }


    /**
     * Converts list of hops to Axis2 data structure, and
     * marks hops that are local.
     *
     * @param hops list of strings representing hops
     * @return CtrlPlanePath sequence of fully qualified links
     * @throws PathfinderException
     */
    private CtrlPlanePathContent pathFromHops(List<String> hops)
            throws PathfinderException {

        CtrlPlanePathContent ctrlPlanePath = new CtrlPlanePathContent();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        String fqn = null;
        boolean hopFound = false;

        Domain domain = domainDAO.getLocalDomain();
        String localTopologyIdent = domain.getTopologyIdent();

        this.log.debug("pathFromHops.start");
        // assign topology identifiers and identify local hops
        for (String hop: hops)  {
            CtrlPlaneHopContent ctrlPlaneHop = new CtrlPlaneHopContent();
            if (ipaddrDAO.queryByParam("IP", hop) != null) {
                fqn = domainDAO.setFullyQualifiedLink(localTopologyIdent, hop);
                hopFound = true;
            } else {
                fqn = domainDAO.setFullyQualifiedLink("other", hop);
            }
            // this statement is to make the XSD schema happy
            ctrlPlaneHop.setId(fqn);
            ctrlPlaneHop.setLinkIdRef(fqn);
            ctrlPlanePath.addHop(ctrlPlaneHop);
        }
        // throw error if no local path found
        if (!hopFound) { 
            throw new PathfinderException("No local hops found in path");
        }
        this.log.debug("pathFromHops.finish");
        return ctrlPlanePath;
    }

    
    /**
     * Given sections of a traceroute, eliminate duplicates and build complete
     * list of hops.  Some of the sections may be null.  Note that there are
     * some redundant checks between this and pathFromHops for locality to
     * the domain.  The check requires a database query.  Caching the IP addresses
     * from the database in a data structure would improve performance.
     *
     * @param previousHops list of addresses that are prior to the ingress
     * @param hops list of addresses from the ingress onwards
     * @param laterHops list of addresses further on than the egress
     * @return completeHops list of addresses from source to destination
     */
    private List<String> stitch(List<String> previousHops, List<String> hops,
                                List<String> laterHops) {

        this.log.debug("stitch.start");
        List<String> completeHops = new ArrayList<String>();
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        for (String hop: previousHops) {
            if (ipaddrDAO.queryByParam("IP", hop) == null) {
                this.log.debug("previous: " + hop);
                completeHops.add(hop);
            }
        }
        for (String hop: hops) {
            if (ipaddrDAO.queryByParam("IP", hop) == null) {
                this.log.debug("local hop: " + hop);
                completeHops.add(hop);
            } else if (laterHops == null) {
                completeHops.add(hop);
                this.log.debug("non-local hop: " + hop);
            }
        }
        if (laterHops != null) {
            for (String hop: laterHops) {
                if (ipaddrDAO.queryByParam("IP", hop) == null) {
                    this.log.debug("non-local later hop: " + hop);
                    completeHops.add(hop);
                }
            }
        }
        this.log.debug("stitch.end");
        return completeHops;
    }
    
    /**
     * Performs reverse traceroute to source.
     *
     * @param egressNodeIP string with egress node, if any
     * @param srcHost string with IP address of source host
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> reversePath(String egressNodeIP, String srcHost)
            throws PathfinderException {

        String src = null;
        List<String> hops = null;

        // use egress node, if given, as the source of the traceroute
        if (egressNodeIP != null) {
            src = egressNodeIP;

        } else { // otherwise use default node as source
            src = this.props.getProperty("jnxSource");
        }
        // run the reverse traceroute to the reservation source
        hops = traceroute(src, srcHost);
        return hops;
    }

    /**
     * Performs traceroute.
     *
     * @param src source string
     * @param dest destination string
     * @return hops list of strings with addresses in path
     * @throws PathfinderException
     */
    private List<String> traceroute(String src, String dest)
            throws PathfinderException {

        JnxTraceroute jnxTraceroute = null;
        String source = null;
        String pathSrc = null;
        List<String> hops = null;

        jnxTraceroute = new JnxTraceroute();
        try {
            pathSrc = jnxTraceroute.traceroute(src, dest);
        } catch (IOException ex) {
            throw new PathfinderException(ex.getMessage());
        }
        hops = jnxTraceroute.getHops();
        // prepend source to path
        hops.add(0, pathSrc);
        return hops;
    }
}
