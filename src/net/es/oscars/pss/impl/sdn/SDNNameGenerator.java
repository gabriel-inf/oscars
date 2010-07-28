package net.es.oscars.pss.impl.sdn;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.common.ConfigNameGenerator;

/**
 * creates ESNet SDN-style names
 * @author haniotak
 *
 */
public class SDNNameGenerator implements ConfigNameGenerator {
    
    public String getFilterName(Reservation resv, String type) {

        String base = oscarsName(resv);
        if (type.equals("stats")) {
            return base+"_stats";
        } else if (type.equals("policing")) {
            return base+"_policing";
        } else {
            return base;
        }
    }
    public String getInetFilterMarker(Reservation resv) {
        return "oscars-filters-start";
    }
    public String getRoutingInstanceName(Reservation resv) {
        String base = oscarsName(resv);
        return base;
    }
    
    public String getRoutingInstanceRibName(Reservation resv) {
        String base = oscarsName(resv);
        return base+".inet.0";
    }
    
    public String[] getLayer3Filters() {
        String[] filters = { 
                "internal-interface-inbound-inet.0-filter", 
                "external-interface-inbound-inet.0-filter", 
                "test-interface-inbound-inet.0-filter" };

        return filters;
    }
    
    public String getFilterTerm(Reservation resv, String type) {
        String base = oscarsName(resv);
        return base;
    }
    
    public String getPrefixListName(Reservation resv, boolean src) {
        String base = oscarsName(resv);
        if (src) {
             return base+"_src";
        } else {
            return base+"_dst";
        }
    }
    
    public String getInterfaceDescription(Reservation resv) {
        String base = oscarsName(resv);
        Long bandwidth = resv.getBandwidth();
        return base + ":"+bandwidth+":oscars-l2circuit:show:circuit-intercloud";
    }

    public String getL2CircuitDescription(Reservation resv) {
        return oscarsName(resv);
    }

    public String getLSPName(Reservation resv) {
        return oscarsName(resv);
    }

    public String getPathName(Reservation resv) {
        return oscarsName(resv);
    }

    public String getPolicerName(Reservation resv) {
        return oscarsName(resv);
    }

    public String getPolicyName(Reservation resv) {
        return oscarsName(resv);
    }

    public String getCommunityName(Reservation resv) {
        return oscarsName(resv);
    }


    private String oscarsName(Reservation resv) {
        String header = "oscars_";
        String gri = resv.getGlobalReservationId();
        String description = resv.getDescription();

        String circuitStr = gri;
        
        // gri should look like domain.name.com-1234

        // the maximum length is 32 characters so we need to make sure that the "oscars_" portion fits on
        if ((header + circuitStr).length() > 32) {
            int split_offset = circuitStr.lastIndexOf('-');

            if (split_offset == -1) {
        // it's not of the form domain-####, so remove from the
        // beginning of the string until we have a proper length string
        // so we can prepend the header.
                int offset = header.length() + circuitStr.length() - 32;
                circuitStr = circuitStr.substring(offset, circuitStr.length());
            } else {
                // here we likely have something of the form "domain-#"
                String domainSegment = circuitStr.substring(0,split_offset-1);
                String tailSegment   = circuitStr.substring(split_offset, circuitStr.length());

        // hack off the end of the domain section so that we have a
        // proper length string once we prepend the header.
                domainSegment = domainSegment.substring(0, 32 - header.length() - tailSegment.length());

                circuitStr = domainSegment+tailSegment;
            }
        }

        circuitStr = header + circuitStr;

        // replace dots with _ 
        circuitStr = circuitStr.replaceAll("\\.", "_");
        // don't allow junk characters - safety 
        circuitStr = circuitStr.replaceAll("[^a-zA-Z0-9\\-\\_]+", "");
        
        // capitalize circuit names for production circuits
        if (description.contains("PRODUCTION")) {
            circuitStr = circuitStr.toUpperCase();
        }
        return circuitStr;
    }
    
    private static SDNNameGenerator instance;
    private SDNNameGenerator() {
    }
    public static SDNNameGenerator getInstance() {
        if (instance == null) {
            instance = new SDNNameGenerator();
        }
        return instance;
    }

    public Integer getOscarsCommunity(Reservation resv) {
        // return OSCARS
        return 672277;
    }
    
}
