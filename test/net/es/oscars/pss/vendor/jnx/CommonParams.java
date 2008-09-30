package net.es.oscars.pss.vendor.jnx;

public class CommonParams {

    public static String getBandwidth() {
        return "10000000";
    }

    public static String getSrc() {
        return "127.0.0.1";
    }

    public static String getDest() {
        return "127.0.0.2";
    }

    public static String getLoopback() {
        return "127.0.0.200";
    }

    public static String getVlanId() {
        return "1";
    }

    public static String getCommunity() { 
        return "65535.1";
    }

    public static String getInterface() {
        return "srcInterface";
    }

    public static String getPort() {
        return "destPort";
    }
}
