package net.es.oscars.scheduler;
import java.util.*;
import net.es.oscars.bss.*;



public class VendorStatusSemaphore {
    private static HashMap<String, String> statusMap = null;
    private static HashMap<String, String> setupMap = null;

    private VendorStatusSemaphore() {
    }

    public static synchronized String syncStatusCheck(String gri, String operation, String direction) throws BSSException {
        String reverse;
        if (!operation.equals("SETUP") && !operation.equals("TEARDOWN")) {
            throw new BSSException("Invalid operation:"+operation);
        }

        if (direction.equals("FORWARD")) {
            reverse = "REVERSE";
        } else if (direction.equals("REVERSE")) {
            reverse = "FORWARD";
        } else {
            throw new BSSException("Invalid direction:"+direction);
        }

        if (statusMap == null) {
            statusMap = new HashMap<String, String>();
        }

        String newStatus;
        if (statusMap.get(gri) == null) {
            newStatus = operation+"_"+direction;
            statusMap.put(gri, newStatus);
            return newStatus;
        } else if (statusMap.get(gri).equals(operation+"_"+direction)) {
            newStatus = operation+"_"+direction;
            return newStatus;
        } else if (statusMap.get(gri).equals(operation+"_"+reverse)) {
            newStatus = operation+"_BOTH";
            statusMap.put(gri, newStatus);
            return newStatus;
        } else if (statusMap.get(gri).equals(operation+"_BOTH")) {
            newStatus = operation+"_BOTH";
            statusMap.put(gri, newStatus);
            return newStatus;
        } else {
            throw new BSSException("Corrupt statusMap for gri: "+gri+" st: "+statusMap.get(gri)+" dir: "+direction+" op: "+operation);
        }
    }



    public static synchronized String syncSetupCheck(String gri, String operation, String direction) throws BSSException {
        String reverse;
        if (!operation.equals("SETUP") && !operation.equals("TEARDOWN")) {
            throw new BSSException("Invalid operation:"+operation);
        }

        if (direction.equals("forward")) {
            reverse = "reverse";
        } else if (direction.equals("reverse")) {
            reverse = "forward";
        } else {
            throw new BSSException("Invalid direction:"+direction);
        }

        if (setupMap == null) {
            setupMap = new HashMap<String, String>();
        }

        String newStatus;
        if (setupMap.get(gri) == null) {
            newStatus = operation+"_"+direction;
            setupMap.put(gri, newStatus);
            return newStatus;
        } else if (setupMap.get(gri).equals(operation+"_"+direction)) {
            newStatus = operation+"_"+direction;
            return newStatus;
        } else if (setupMap.get(gri).equals(operation+"_"+reverse)) {
            newStatus = operation+"_BOTH";
            setupMap.put(gri, newStatus);
            return newStatus;
        } else if (setupMap.get(gri).equals(operation+"_BOTH")) {
            newStatus = operation+"_BOTH";
            setupMap.put(gri, newStatus);
            return newStatus;
        } else {
            throw new BSSException("Corrupt setupMap for gri: "+gri+" st: "+setupMap.get(gri)+" dir: "+direction+" op: "+operation);
        }

    }





}