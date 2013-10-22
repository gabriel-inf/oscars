package net.es.oscars.nsi.soap.util.output;

/**
 * Interface for outputting reserve results
 *
 */
public interface ReserveOutputter {
    
    void outputResponse(String connectionId);
    
    void outputConfirmed(String connectionId);

    void outputTimeout(String connectionId);

    void outputFailed(String connectionId);

}
