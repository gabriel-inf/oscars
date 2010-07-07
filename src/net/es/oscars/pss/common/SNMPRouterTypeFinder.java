package net.es.oscars.pss.common;

import java.util.HashMap;

import org.apache.log4j.Logger;

import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.SNMP;


public class SNMPRouterTypeFinder {
    private Logger log;

    private static SNMPRouterTypeFinder instance = null;

    private HashMap<String, Integer> cacheLastUpdated = new HashMap<String, Integer>();
    private HashMap<String, RouterType> routerTypeCache = new HashMap<String, RouterType>();
    private int secondsToCache = 3600;



    private boolean stubMode = true;
    private RouterType stubType = RouterType.JUNIPER;



    public RouterType getType(String hostname) throws PSSException {
        if (stubMode) {
            return stubType;
        }

        RouterType result = null;
        Long seconds = System.currentTimeMillis() / 1000;
        Integer now = seconds.intValue();
        boolean mustUpdate = true;
        if (routerTypeCache.containsKey(hostname)) {
            if (cacheLastUpdated.containsKey(hostname)) {
                Integer lastUpdated = cacheLastUpdated.get(hostname);
                if (lastUpdated > now - secondsToCache) {
                    mustUpdate = false;
                }
            }
        }

        if (mustUpdate) {
            result = this.getRouterType(hostname);
            routerTypeCache.put(hostname, result);
            cacheLastUpdated.put(hostname, now);
        } else {
            result = routerTypeCache.get(hostname);
        }


        return result;
    }


    /**
     * Determines whether the initial router is a Juniper or Cisco.
     *
     * @param Link link associated with router
     * @param sysDescr string with router type, if successful
     * @throws PSSException
     */
    private RouterType getRouterType(String hostname) throws PSSException {
        String sysDescr = null;

        String errorMsg = "";
        int numTries = 5;
        for (int i = 0; i < numTries; i++) {
            this.log.info("Querying router type using SNMP for node address: ["+hostname+"]");
            try {
                SNMP snmp = new SNMP();
                snmp.initializeSession(hostname);
                sysDescr = snmp.queryRouterType();
                snmp.closeSession();
                i = numTries;
            } catch (Exception ex) {
                errorMsg = ex.getMessage();
                if (i < numTries-1) {
                    this.log.error("Error querying router type using SNMP; ["+errorMsg+"], retrying in 5 sec.");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        this.log.error("Thread interrupted, failing");
                        throw new PSSException("Unable to determine router type");
                    }
                } else {
                    this.log.error("Error querying router type using SNMP; ["+errorMsg+"], failing after "+i+" attempts.");
                }
            }
        }

        if (sysDescr == null) {
            throw new PSSException("Unable to determine router type; error was: "+errorMsg);
        }
        this.log.info("Got sysdescr: ["+sysDescr+"]");
        sysDescr = sysDescr.toLowerCase();
        if (sysDescr.contains("juniper")) {
            return RouterType.JUNIPER;
        } else if (sysDescr.contains("cisco")) {
            return RouterType.CISCO;
        } else {
            throw new PSSException("Could not determine router type - sysdescr was: ["+sysDescr+"]");
        }
    }



    /**
     * private constructor
     */
    private SNMPRouterTypeFinder() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * singleton
     * @return the instance
     */
    public static SNMPRouterTypeFinder getInstance() {
        if (instance == null) {
            instance = new SNMPRouterTypeFinder();
        }
        return instance;
    }


    public void setStubType(RouterType stubType) {
        this.stubType = stubType;
    }


    public RouterType getStubType() {
        return stubType;
    }


    public void setStubMode(boolean stubMode) {
        this.stubMode = stubMode;
    }


    public boolean isStubMode() {
        return stubMode;
    }

    public int getSecondsToCache() {
        return secondsToCache;
    }


    public void setSecondsToCache(int secondsToCache) {
        this.secondsToCache = secondsToCache;
    }
}

