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
        if (firstPath.isExplicit() != secondPath.isExplicit()) {
            this.log.info("one path is explicit, one is not");
            return false;
        }
        if (firstPath.getVlanId() != secondPath.getVlanId()) {
            this.log.info("two VLAN ids are different");
            return false;
        }
        // tests database identity or both being null
        if (firstPath.getNextDomain() != secondPath.getNextDomain()) {
            this.log.info("two domains are different");
            return false;
        }
        if ((firstPath.getNextDomain() != null) &&
                (secondPath.getNextDomain() != null)) {
            if (firstPath.getNextDomain().getUrl() !=
                secondPath.getNextDomain().getUrl()) {
                this.log.info("two url's are different");
                return false;
            }
        }
        // build string representations of the two paths to check
        // for equality
        StringBuilder secondSb = new StringBuilder();
        PathElem pathElem = firstPath.getPathElem();
        while (pathElem != null) {
            secondSb.append(String.valueOf(pathElem.isLoose()));
            secondSb.append(pathElem.getDescription());
            secondSb.append(pathElem.getIpaddr().getIP());
            secondSb.append(pathElem.getIpaddr().isValid());
            pathElem = pathElem.getNextElem();
        }
        StringBuilder firstSb = new StringBuilder();
        pathElem = secondPath.getPathElem();
        while (pathElem != null) {
            firstSb.append(String.valueOf(pathElem.isLoose()));
            firstSb.append(pathElem.getDescription());
            firstSb.append(pathElem.getIpaddr().getIP());
            firstSb.append(pathElem.getIpaddr().isValid());
            pathElem = pathElem.getNextElem();
        }
        //this.log.info(firstSb.toString());
        //this.log.info(secondSb.toString());
        if (!firstSb.toString().equals(secondSb.toString())) {
            this.log.info("two paths are different");
            return false;
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
