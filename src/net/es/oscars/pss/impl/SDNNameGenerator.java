package net.es.oscars.pss.impl;

public class SDNNameGenerator {

    public static String getFilterName(String gri, String description, String type) {

        String base = oscarsName(gri, description);
        if (type.equals("stats")) {
            return base+"_stats";
        } else if (type.equals("policing")) {
            return base+"_policing";
        } else {
            return base;
        }

    }

    public static String getLSPName(String gri, String description) {
        return oscarsName(gri, description);
    }

    public static String getPathName(String gri, String description) {
        return oscarsName(gri, description);
    }

    public static String getPolicerName(String gri, String description) {
        return oscarsName(gri, description);
    }

    public static String getPolicyName(String gri, String description) {
        return oscarsName(gri, description);
    }

    public static String getCommunityName(String gri, String description) {
        return oscarsName(gri, description);
    }


    private static String oscarsName(String gri, String description) {
        String header = "oscars_";

        String circuitStr = gri;

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

        // "." is illegal character in resv-id parameter
        circuitStr = circuitStr.replaceAll("\\.", "_");
        // capitalize circuit names for production circuits
        if (description.contains("PRODUCTION")) {
            circuitStr = circuitStr.toUpperCase();
        }
        return circuitStr;
    }
}
