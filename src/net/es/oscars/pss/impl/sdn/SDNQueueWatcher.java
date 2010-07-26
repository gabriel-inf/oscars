package net.es.oscars.pss.impl.sdn;

public class SDNQueueWatcher {
    
    private static SDNQueueWatcher instance;
    public static SDNQueueWatcher getInstance() {
        if (instance == null) {
            instance = new SDNQueueWatcher();
        }
        return instance;
    }
    private SDNQueueWatcher() {
        
    }
    
    private boolean isStarted = false;
    
    /**
     * starts a queue watcher if one is not started yet
     * the watcher monitors the various ContactNode jobs 
     * and is responsible for updating the overall reservation status
     * once all statuses are returned
     */
    public synchronized void start() {
        if (!this.isStarted) {
            this.isStarted = true;
        }
        // FIXME
    }
    
    public synchronized void stop() {
        if (this.isStarted) {
            this.isStarted = false;
        }
        // FIXME
    }

}
