package net.es.oscars.nsi.soap.util.output;

/**
 * Interface for outputting simple operation results
 *
 */
public interface SimpleOpOutputter {

    public void outputCommitConfirmed(String connectionId);

    public void outputCommitFailed(String connectionId);

    public void outputAbortConfirmed(String connectionId);

    public void outputProvisionConfirmed(String connectionId);

    public void outputTerminateConfirmed(String connectionId);

    public void outputReleaseConfirmed(String connectionId);

}
