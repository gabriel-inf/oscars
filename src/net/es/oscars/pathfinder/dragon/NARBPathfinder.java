package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;

import org.apache.log4j.*;

import edu.internet2.hopi.dragon.narb.NARBWSClient;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBStub.FindPathResponseContent;
import edu.internet2.hopi.dragon.narb.ws.client.NARBFaultMessageException;


/**
 * NARBPathfinder that uses NARB to calculate path
 *
 */
public class NARBPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private List<CommonPathElem> completePath;
    private Logger log;
    
    /**
     * Constructor that initializes NARB properties from oscars.properties file
     *
     */
    public NARBPathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("narb", true);
        this.completePath = null;
        super.setDatabase(dbname);
    }


    /**
     * Finds path from source to destination using the NARB web service
     * interface, taking into account ingress and egress routers if
     * specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressRouterIP string with address of ingress router, if any
     * @param egressRouterIP string with address of egress router, if any
     * @return hops A list of elements containing IP addresses
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(String srcHost, String destHost,
                         String ingressRouterIP, String egressRouterIP)
            throws PathfinderException {
            
        List<String> localHops = null;

        // ask NARB to calculate path
        List<String> hops =
            this.findNARBPath(srcHost, destHost, ingressRouterIP,
                              egressRouterIP);
        localHops = this.getLocalHops(hops);    // get local path
        this.completePathFromHops(hops);             // store complete path
        // ingress and egress are loopbacks corresponding to first and last
        // hops
        String ingressLoopback = this.getOSCARSLoopback(localHops.get(0));
        if (ingressLoopback == null) {
            throw new PathfinderException("No ingress loopback found in path");
        }
        String egressLoopback =
            this.getOSCARSLoopback(localHops.get(localHops.size()-1));
        List<CommonPathElem> path =
            this.pathFromHops(localHops, ingressLoopback, egressLoopback);
        return path;
    }
    
    /**
     * Gets local hops given an explicit path from source to destination..
     *
     * @param reqPath list of CommonPathElem with explicit path
     * @return hops A list of elements containing IP addresses
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(List<CommonPathElem> reqPath)
            throws PathfinderException {
            
        List<CommonPathElem> localPath =
                this.getLocalPath(reqPath); // get path in local domain
        this.setCompletePath(reqPath);      // store complete path
        return localPath;
    }
    
    /**
     * Returns list of hops that are in the local domain, given an
     * list. Also sets nextHop to the first hop past the local domain.
     *
     * @param reqPath list of CommonPathElem containing hops of entire path
     * @return returns list of strings representing only hops within the domain
     */
    public List<String> findLocalHops(List<CommonPathElem> reqPath)
            throws PathfinderException{

        List<String> localHops = new ArrayList<String>();
        List<String> hops = new ArrayList<String>();
        boolean hopFound = false;

        // parse hops
        CommonPathElem prevHop = null;
        String ip = null;
        String prevIp = null;
        for (int i = 0; i < reqPath.size(); i++) {
            CommonPathElem hop = reqPath.get(i);
            ip = hop.getIP();
            if (!this.isLocalHop(ip)) {
                if (hopFound) {
                    this.log.info("nextHop: " + ip);
                    this.nextHop = ip;
                    break;
                }else{
                    continue;   /* continue until local hop found */
                }
            }
            this.log.info("localHop: " + ip);
            hopFound = true;

            // expand hops
            if (prevHop != null) {
                prevIp = prevHop.getIP();
                if (hop.isLoose() || prevHop.isLoose()) {
                    String src = this.getLoopback(prevIp);
                    String dst = this.getLoopback(ip);
                    List<String> expandedHops = null;
                        
                    // expand path
                    if (src.equals(dst)) { //if interfaces on same router
                        expandedHops = new ArrayList<String>();
                        expandedHops.add(prevIp);
                        expandedHops.add(ip);
                    } else {
                        expandedHops = this.findNARBPath(src, dst, null, null);
                        // make sure outgoing interface is added to list
                        if (!expandedHops.get(expandedHops.size() - 1).equals(ip)) {
                            expandedHops.add(ip);
                        }
                    }
                    // check edges to ensure correct incoming interface is used
                    if (localHops.isEmpty() && (!expandedHops.isEmpty()) &&
                            (!expandedHops.get(0).equals(prevIp))) {
                        // add hop if incoming interface not included in list
                        expandedHops.add(0, prevIp);

                    } else if ((!localHops.isEmpty()) && 
                            localHops.get(localHops.size() - 1).equals(expandedHops.get(0))) {
                        // remove first hop if already in the list
                        expandedHops.remove(0);
                    }
                    // append expanded path to list
                    for (String expandedHop : expandedHops) {
                        localHops.add(expandedHop);
                    }
                } else {
                    // check if in database
                    //TODO: Assumes that if ip exists locally it has link to previous hop
                    localHops.add(ip);
                }
            }
            prevHop = hop;
        }
        if (localHops.isEmpty()) {
            throw new PathfinderException(
                "No local hops found in path");
        }
        return localHops;
    }


    /**
     * Retrieves path calculation from DRAGON NARB
     *
     * @param src string with IP address of source host
     * @param dst string with IP address of destination host
     * @param ingress string with IP address of desired ingress
     * @param egress string with IP address of desired egress
     * @return list of hops in path
     * @throws PathfinderException
     */
    public List<String>
        findNARBPath(String src, String dst, String ingress, String egress)
            throws PathfinderException {

        List<String> hops = new ArrayList<String>();
        String narbURL = this.props.getProperty("url");
        String hostDst = null;
        
        /* Determine ingress if specified */
        if(ingress != null){
            src = ingress;
        }
        
        /* determine egress */
        if(egress != null){
            hostDst = dst;
            dst = egress;
        }
        
        // calculate path
        try {
            this.log.info("findNARBPath.start");
            NARBWSClient client = new NARBWSClient(narbURL);
            FindPathContent request = new FindPathContent();
            request.setSrcHost(src);
            request.setDstHost(dst);
            request.setBandwidth(100);
            request.setPreferred(true);
            request.setStrict(true);
            FindPathResponseContent response = client.sendRequest(request);
            NARBStub.Hop[] path = response.getPath().getHops().getHop();
            for (int i = 0; i < path.length; i++) {
                this.log.info("narbPath.hop: " + path[i].getString());
                hops.add(path[i].getString());
            }
            // if egress specified, add hops after egress so next domain can
            // be found
            if (egress != null) {
                List<String> egressHops =
                    this.findNARBPath(egress, hostDst, null, null);
                hops.addAll(egressHops);
            }
            this.log.info("narbPath.end");
        } catch (UnknownHostException e) {
            throw new PathfinderException(e.getMessage());
        } catch (IOException e) {
            throw new PathfinderException(e.getMessage());
        } catch (NARBFaultMessageException e) {
            throw new PathfinderException(e.getFaultMessage().getMsg());
        }
        return hops;
    }
    
    /**
     * Set complete path to passed value
     *
     * @param path list of CommonPathElem to set
     */
    public void setCompletePath(List<CommonPathElem> path){
        this.completePath = path;
    }
    
    /**
     * Given a list of hops as strings, converts it to a path,
     * and store in completePath
     * @param hops list of hop IP addresses as strings
     */
    public void completePathFromHops(List<String> hops) {

        List<CommonPathElem> paths = new ArrayList<CommonPathElem>();
        CommonPathElem h = null;

        for (String hop : hops) {
            h = new CommonPathElem();
            h.setLoose(true);  //TODO: allow this to be configed
            h.setIP(hop);
            paths.add(h);
        }
        this.setCompletePath(paths);
    }
    
     /**
     * Returns path that includes both local and interdomain hops. 
     * Hops used are from previous findPath call.
     *
     * @return path that includes both local and interdomain hops
     */
    public List<CommonPathElem> getCompletePath() {
        return this.completePath;
    }
}
