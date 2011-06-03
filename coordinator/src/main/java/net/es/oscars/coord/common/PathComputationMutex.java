package net.es.oscars.coord.common;

import net.es.oscars.coord.req.CoordRequest;
import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.OSCARSNetLogger;

import org.apache.log4j.Logger;


/**
 * This class implements a mutual exclusion on the path computation part of the IDC: one one path can be computed at any given time.
 * The current implementation is rather simple, barely only encapsulating an object used as a lock. That allows to track who has the lock and
 * add log messages. If it is needed to add more semantics, this class will hold the implementation.
 **/


public class PathComputationMutex {
    
    private static final long       serialVersionUID  = 178923479L;
    private static final Logger LOG = Logger.getLogger(PathComputationMutex.class.getName());
    
    private boolean locked = false;
    private String  holdingGRI = null;    
    
    public PathComputationMutex() {
        this.locked = false;
        this.holdingGRI = null;
    }
    

    @SuppressWarnings("unchecked")
    public synchronized void get (CoordRequest request) throws InterruptedException {
        assert (request != null);
        String gri = request.getGRI();
        assert (gri != null);

        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "getMutex";
        LOG.debug(netLogger.start(event, request.getName() + " requestGRI= " + gri +
                                  " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
        if ((this.locked) && ( ! this.holdingGRI.equals(gri))) {
            // Lock is already taken. Block until getting signaled
            LOG.debug(netLogger.getMsg(event,"wait for lock " + request.getName()  + 
                                       " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
            this.wait();
        }
        LOG.debug(netLogger.end(event, gri + " mutex is granted"));
        this.locked = true;
        this.holdingGRI = gri;
    }
    
    public synchronized void release(String gri) {

        if (this.holdingGRI == null) { return; }
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        String event = "releaseMutex";
        LOG.debug(netLogger.start(event, " holding gri= " + (this.holdingGRI != null ? this.holdingGRI : "None")));
        if ( ! gri.equals(this.holdingGRI)) {
            // do nothing.
            return;
        }
        if (this.locked) {
            this.locked = false;
            this.holdingGRI = null;
            this.notify();
        }
        LOG.debug(netLogger.end(event));
    }
    
}
