package net.es.oscars.bss;

/**
 * BSSException is the top-level exception thrown by BSS methods.
 */
public class BSSException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public BSSException(String msg) {
        super(msg);
    }
    public BSSException(Exception ex) {
    	super(ex);
    }
}
