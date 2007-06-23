package net.es.oscars.bss;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.bss.topology.*;

/**
 * @author David Robertson (dwrobertson@lbl.gov)
 *
 * This class contains utility methods for use by the scheduler, the
 * reservation manager, and the topology manager.
 */
public class Utils {
    private String dbname;
    private Logger log;

    public Utils(String dbname) {
        this.dbname = dbname;
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Checks to see if two paths contain the same information.
     *
     * @param firstPath first path information
     * @param secondPath second path information
     * @return boolean indicating whether paths are the same
     */
    public boolean isDuplicate(Path firstPath, Path secondPath) {

        this.log.info("isDuplicate.start");
        if (!firstPath.equals(secondPath)) {
            this.log.info("one path's fields are different");
            return false;
        }
        // first check that paths are the same length
        int firstCtr = 0;
        PathElem pathElem = firstPath.getPathElem();
        while (pathElem != null) {
            firstCtr++;
            pathElem = pathElem.getNextElem();
        }
        int secondCtr = 0;
        pathElem = secondPath.getPathElem();
        while (pathElem != null) {
            secondCtr++;
            pathElem = pathElem.getNextElem();
        }
        if (firstCtr != secondCtr) {
           this.log.info("two paths have different lengths");
           return false;
        } 
        // now that know paths are the same length,
        // check each element of the two paths for equality
        pathElem = firstPath.getPathElem();
        PathElem secondPathElem = secondPath.getPathElem();
        while (pathElem != null) {
            if (!pathElem.equals(secondPathElem)) {
                this.log.info("two paths are different");
                return false;
            }
            pathElem = pathElem.getNextElem();
            secondPathElem = secondPathElem.getNextElem();
        }
        this.log.info("isDuplicate.finish true");
        return true;
    }

    /**
     * Converts path to a series of host name/IP address pairs.
     *
     * @param path path to convert to string
     * @return pathStr converted path
     */
    public String pathToString(Path path) {

        String ip = null;
        String hostName = null;

        StringBuilder sb = new StringBuilder();
        PathElem pathElem  = path.getPathElem();
        while (pathElem != null) {
            ip = pathElem.getIpaddr().getIP();
            hostName = this.getHostName(ip);
            if (!hostName.equals(ip)) {
                sb.append(hostName + ": ");
            }
            sb.append(ip + ", ");
            pathElem = pathElem.getNextElem();
        }
        String pathStr = sb.toString();
        return pathStr.substring(0, pathStr.length() - 2);
    }

    /**
     * Given an IP address, gets the associated host name.
     *
     * @param hop string containing IP address
     * @return hostName string containing host name
     */
    public String getHostName(String hop) {
        InetAddress addr = null;
        String hostName = null;

        try {
            addr = InetAddress.getByName(hop);
            hostName = addr.getCanonicalHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host: " + hop);
        }
        // non-fatal error
        if (hostName == null) { hostName = hop; }
        return hostName;
    }
}
