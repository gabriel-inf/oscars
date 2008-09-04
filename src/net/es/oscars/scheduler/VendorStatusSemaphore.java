package net.es.oscars.scheduler;
import java.util.*;
import net.es.oscars.bss.*;



public class VendorStatusSemaphore {
    private static HashMap<String, String> statusMap = null;

    private VendorStatusSemaphore() {
    }

    public static synchronized String syncStatusCheck(String gri, String operation, String direction) throws BSSException {
        String reverse;
        if (!operation.equals("PATH_SETUP") && !operation.equals("PATH_TEARDOWN")) {
            throw new BSSException("Invalid operation:"+operation);
        }

        if (direction.equals("DOWN")) {
            reverse = "UP";
        } else if (direction.equals("UP")) {
            reverse = "DOWN";
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





}