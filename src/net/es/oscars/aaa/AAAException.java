package net.es.oscars.aaa;

public class AAAException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public AAAException(String msg) {
        super(msg);
    }
}
