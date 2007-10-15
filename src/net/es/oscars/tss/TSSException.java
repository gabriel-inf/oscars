package net.es.oscars.tss;

/**
 * Exception thrown by errors caused during Topology Exchange 
 *  
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TSSException extends Exception{
    private static final long serialVersionUID = 1;

    public TSSException(String msg) {
        super(msg);
    }
}