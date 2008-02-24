package net.es.oscars.pathfinder.db;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.rmi.RemoteException;

import net.es.oscars.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.topology.DomainDAO;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import org.apache.log4j.*;
 
/**
 * DBPathfinder that uses the local database to calculate path
 *
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class DBPathfinder extends Pathfinder implements PCE {
    private Properties props;
    private Logger log;
    
    /**
     * Constructor that initializes TERCE properties from oscars.properties file
     *
     */
    public DBPathfinder(String dbname) {
        super(dbname);
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("dbpath", true);
    }
    
    /**
     * Finds a path given just source and destination or by expanding
     * a path the user explicitly sets
     *
     * @param pathInfo PathInfo instance containing hops of entire path
     * @throws PathfinderException
     */
    public PathInfo findPath(PathInfo pathInfo) throws PathfinderException{
        CtrlPlanePathContent ctrlPlanePath = pathInfo.getPath();
        CtrlPlanePathContent localPathForOSCARSDatabase;
        CtrlPlanePathContent pathToForwardToNextDomain;
        
        if(ctrlPlanePath == null){
            /* Calculate path that contains strict local hops and 
            loose interdomain hops */
            CtrlPlanePathContent path = null;
            
            pathInfo.setPath(path);
        } else {

        }
        
        return pathInfo;  // just for compatibility with interface
    }

}
