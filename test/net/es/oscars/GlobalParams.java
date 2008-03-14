package net.es.oscars;

/**
 *  This class contains static methods returning properties that are required
 *  by all tests, but unlikely the user will want to change.  Using this also
 *  requires less set up than for properties.
 */
public class GlobalParams {

    public static String getAAATestDBName() {
        return "testaaa";
    }

    public static String getReservationTestDBName() {
        return "testbss";
    }

    public static String getExportedTopologyFname() {
        return "/tmp/exportedTopology.xml";
    }

    public static String getExportedIpaddrFname() {
        return "/tmp/exportedIpaddrs.txt";
    }

}
