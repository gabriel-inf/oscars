package net.es.oscars.nsibridge.task;


import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;

public class QueryOscars extends Task  {

    public QueryOscars() {
        this.scope = "oscars";
    }
    public void onRun() throws TaskException {
        super.onRun();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {

        }
        System.out.println(this.id+" ran!");
        this.onSuccess();
    }

}
