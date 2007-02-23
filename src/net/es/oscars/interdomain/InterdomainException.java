package net.es.oscars.interdomain;

/**
 * InterdomainException is the top-level exception thrown by interdomain
 * methods.
 */
public class InterdomainException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public InterdomainException(String msg) {
        super(msg);
    }
}
