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
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class NARBPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;
    
    /**
     * Constructor that initializes NARB properties from oscars.properties file
     *
     */
    public NARBPathfinder(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("narb", true);
        super.setDatabase(dbname);
    }


    /**
     * Finds path from source to destination using the NARB web service
     * interface, taking into account ingress and egress nodes if
     * specified by user.
     *
     * @param srcHost string with address of source host
     * @param destHost string with address of destination host
     * @param ingressNodeIP string with address of ingress node, if any
     * @param egressNodeIP string with address of egress node, if any
     * @return pathElems a list of elements containing IP addresses
     * @throws PathfinderException
     */
    public List<CommonPathElem> findPath(String srcHost, String destHost,
                         String ingressNodeIP, String egressNodeIP)
            throws PathfinderException {
            
        if (srcHost == null) {
            throw new PathfinderException( "no source for path given");
        }
        if (destHost == null) {
            throw new PathfinderException( "no destination for path given");
        }
        // ask NARB to calculate path
        List<String> hops =
            this.findNARBPath(srcHost, destHost, ingressNodeIP,
                              egressNodeIP);
        List<CommonPathElem> pathElems = this.pathFromHops(hops);
        return pathElems;
    }
    
    /**
     * Expands loose hops and finds hops that are in the local domain, given a
     * path containing a list of addresses.
     *
     * @param path CommonPath instance containing hops of entire path
     * @throws PathfinderException
     */
    public void findPath(CommonPath path) throws PathfinderException{

        List<String> localHops = new ArrayList<String>();
        List<String> hops = new ArrayList<String>();
        boolean hopFound = false;

        List<CommonPathElem> pathElems = path.getElems();
        
        /* Expand local hops */
        CommonPathElem prevPathElem = null;
        String ip = null;
        String prevIp = null;
        for (int i = 0; i < pathElems.size(); i++) {
            CommonPathElem pathElem = pathElems.get(i);
            ip = pathElem.getIP();

            if (this.isLocalHop(ip)) {
                this.log.info("localHop: " + ip);
                hopFound = true;
    
                // expand hops
                if (prevPathElem != null) {
                    prevIp = prevPathElem.getIP();
                    if (pathElem.isLoose() || prevPathElem.isLoose()) {
                        String src = this.getLoopback(prevIp);
                        String dst = this.getLoopback(ip);
                        List<String> expandedHops = null;
                            
                        // expand path
                        if (src.equals(dst)) { //if interfaces on same node
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
                        pathElem.setDescription("local");
                    }
                }
                prevPathElem = pathElem;
            }else if (hopFound) {
                break;
            }
        }
        
        /* Return error if no local hops found */
        if (localHops.isEmpty()) {
            throw new PathfinderException(
                "No local hops found in path");
        }
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
}
