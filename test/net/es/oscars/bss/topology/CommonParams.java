package net.es.oscars.bss.topology;

public class CommonParams {

    public static String getIdentifier() {
        return "test suite";
    }

    public static String getReservationDescription() {
        return "Test reservation unique description 123aaa456zzz";
    }

    public static Long getMPLSBurstLimit() {
        return 1000000L;
    }

    public static String getSrcEndpoint() {
        return "urn:ogf:network:domainIdent:nodeIdent1:portIdent";
    }

    public static String getDestEndpoint() {
        return "urn:ogf:network:domainIdent:nodeIdent2:portIdent";
    }

    public static String getSrcHost() {
        return "test.src.domain";
    }

    public static String getDestHost() {
        return "test.dest.domain";
    }

    public static String getIpAddress() {
        return "127.0.0.1";
    }
}
