package net.es.oscars.bss.topology;


public class PathType {
    public static final String INTERDOMAIN = "interdomain";
    public static final String LOCAL = "local";
    public static final String REQUESTED = "requested";
    
    // never construct this object
    private PathType() {
    }

    public static boolean isValid(String pathType) {
        if (pathType.equals(INTERDOMAIN)) {
            return true;
        } else if (pathType.equals(LOCAL)) {
            return true;
        } else if (pathType.equals(REQUESTED)) {
            return true;
        }
        return false;
    }


}
