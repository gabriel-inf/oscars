package net.es.oscars.nsibridge.client.cli.output;

/**
 * Outputs results using a human-readable text format
 *
 */
public class SimpleOpPrettyOutputter implements SimpleOpOutputter{

    @Override
    public void outputCommitConfirmed(String connectionId) {
        System.out.println("Commit confirmed for " + connectionId);
    }

    @Override
    public void outputCommitFailed(String connectionId) {
        System.out.println("Commit failed for " + connectionId);
    }

    @Override
    public void outputAbortConfirmed(String connectionId) {
        System.out.println("Abort confirmed for " + connectionId);
    }

    @Override
    public void outputProvisionConfirmed(String connectionId) {
        System.out.println("Provision confirmed for " + connectionId);
        
    }

    @Override
    public void outputTerminateConfirmed(String connectionId) {
        System.out.println("Terminate confirmed for " + connectionId);
    }

    @Override
    public void outputReleaseConfirmed(String connectionId) {
        System.out.println("Release confirmed for " + connectionId);
    }
}
