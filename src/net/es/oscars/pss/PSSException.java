package net.es.oscars.pss;

/**
 * PSSException is a PSS-specific exception thrown by PSS methods.
 */
public class PSSException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public PSSException(String msg) {
        super(msg);
    }
}
