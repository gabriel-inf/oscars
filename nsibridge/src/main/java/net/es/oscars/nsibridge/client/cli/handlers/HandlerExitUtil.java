package net.es.oscars.nsibridge.client.cli.handlers;

/**
 * Class to exit handler after message received. Creates thread that 
 * exits after a few milliseconds. This allows handler methods to return so 
 * server side doesn't get extraneous exception
 *
 */
public class HandlerExitUtil {
    /**
     * Creates thread that exits system after 500 milliseconds
     * @param status the exit status
     */
    static public void exit(int status){
        ExitThread thread = new ExitThread(status);
        thread.start();
    }
}

class ExitThread extends Thread {
    private int status;
    
    public ExitThread(int status){
        this.status = status;
    }
    
    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}
        System.exit(status);
    }
    
}
