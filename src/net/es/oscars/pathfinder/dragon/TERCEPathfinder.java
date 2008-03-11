package net.es.oscars.pathfinder.dragon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.pathfinder.generic.*;
import net.es.oscars.bss.Reservation;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.*;

import edu.internet2.hopi.dragon.terce.ws.types.rce.*;
import edu.internet2.hopi.dragon.terce.ws.service.*;

/**
 * TERCEPathfinder that uses TERCE to calculate path
 *
 * @author Andrew Lake (alake@internet2.edu), David Robertson (dwrobertson@lbl.gov)
 */
public class TERCEPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;

    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public TERCEPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("terce", true);
    }

    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing interdomain hops
     * @return intradomain path used for resource scheduling
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo, Reservation reservation) throws PathfinderException{

        InterdomainPathfinder interPathfinder = new InterdomainPathfinder(this.dbname);
        PathInfo intraPathInfo = interPathfinder.findPath(pathInfo, reservation);
        CtrlPlaneHopContent[] intraHops = intraPathInfo.getPath().getHop();
        String src = intraHops[0].getLinkIdRef();
        String dest = intraHops[1].getLinkIdRef();
        CtrlPlanePathContent intraPath = this.terce(src, dest);

        intraPathInfo.setPath(intraPath);

        return intraPathInfo;
    }

    /**
     * Retrieves path calculation from TERCE
     *
     * @param src string with IP address of source host
     * @param dest string with IP address of destination host
     * @return responseContent list of hops in path
     * @throws PathfinderException
     */
    public CtrlPlanePathContent terce(String src, String dest)
            throws PathfinderException {

        String terceURL = this.props.getProperty("url");
        FindPath fp = new FindPath();
        FindPathContent request = new FindPathContent();
        FindPathResponse response = null;
        FindPathResponseContent responseContent= null;
        CtrlPlanePathContent path = null;
        CtrlPlaneHopContent[] hops = null;
        TERCEStub terce= null;

        /* Calculate path */
        try {
            this.log.info("terce.start");
            this.log.info("src=" + src);
            this.log.info("dest=" + dest);
            String repo = System.getenv("CATALINA_HOME");
            repo += (repo.endsWith("/") ? "" :"/");
            repo += "shared/classes/terce.conf/repo/";
            System.setProperty("axis2.xml", repo + "axis2.xml");
            ConfigurationContext configContext =
                ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, null);
            terce = new TERCEStub(configContext, terceURL);

            /* Format Request */
            request.setSrcEndpoint(src);
            request.setDestEndpoint(dest);
            request.setVtag("any");
            request.setPreferred(true);
            request.setStrict(true);
            request.setAllvtags(true);

            /* Send request and get response*/
            fp.setFindPath(request);
            response = terce.findPath(fp);
            responseContent = response.getFindPathResponse();
            path = responseContent.getPath();
            hops = path.getHop();

            log.info("terce.path.start");
            for(int i = 0; i < hops.length; i++){
                log.info("terce.path.hop=" + hops[i].getLinkIdRef());
            }
            log.info("terce.path.end");

            this.log.info("terce.end");
        } catch (RemoteException e) {
            throw new PathfinderException(e.getMessage());
        }catch (RCEFaultMessage e) {
            throw new PathfinderException(e.getFaultMessage().getMsg());
        }

        return responseContent.getPath();
    }
}
