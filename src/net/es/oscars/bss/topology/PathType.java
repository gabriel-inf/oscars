package net.es.oscars.bss.topology;


public class PathType {
    public static final String INTERDOMAIN = "inter";
    public static final String INTRADOMAIN = "intra";
    public static final String REQUESTED = "requested";

    public static boolean isValid(String pathType) {
        if (pathType.equals(INTERDOMAIN)) {
            return true;
        } else if (pathType.equals(INTRADOMAIN)) {
            return true;
        } else if (pathType.equals(REQUESTED)) {
            return true;
        }
        return false;
    }


}
