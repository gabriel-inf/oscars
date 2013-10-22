package net.es.oscars.nsi.soap.util.output;

/**
 * Outputs results using a human-readable text format
 *
 */
public class ReservePrettyOutputter implements ReserveOutputter{

    @Override
    public void outputConfirmed(String connectionId) {
        System.out.println("Reserved with connection ID " + connectionId);
    }

    @Override
    public void outputTimeout(String connectionId) {
        System.err.println("Reservation timed-out" + (connectionId == null ? "" : " (Connection ID: " + connectionId + ")"));
    }

    @Override
    public void outputFailed(String connectionId) {
        System.err.println("Reservation failed" + (connectionId == null ? "" : " (Connection ID: " + connectionId + ")"));
    }

    @Override
    public void outputResponse(String connectionId) {
        System.out.println("Connection created with ID " + connectionId);
    }
}
