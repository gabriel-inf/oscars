package net.es.oscars.notify;

/**
 * NotifyException is the top-level exception thrown by Notify methods.
 */
public class NotifyException extends Exception {
    private static final long serialVersionUID = 1;  // make -Xlint happy

    public NotifyException(String msg) {
        super(msg);
    }
}
