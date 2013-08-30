package net.es.oscars.utils.task.sched;


import net.es.oscars.utils.task.Outcome;
import net.es.oscars.utils.task.RunState;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Workflow {
    Logger log = Logger.getLogger(Workflow.class);
    private static Workflow instance;
    private Workflow() {}
    public static Workflow getInstance() {
        if (instance == null) {
            instance = new Workflow();
        }
        return instance;
    }

    private class ScheduledTask {
        Task task;
        long when;
    }

    private ConcurrentLinkedQueue<ScheduledTask> scheduledTasks = new ConcurrentLinkedQueue<ScheduledTask>();
    private ConcurrentLinkedQueue<Task> runningTasks = new ConcurrentLinkedQueue<Task>();
    private ConcurrentHashMap<UUID, RunState> runStates = new ConcurrentHashMap<UUID, RunState>();

    public synchronized UUID schedule(Task task, long when) throws TaskException {
        for (Task prereq : task.getPrereqs()) {
            this.schedule(prereq, when);
        }
        ScheduledTask st = new ScheduledTask();
        st.task = task;
        st.when = when;
        scheduledTasks.add(st);
        runStates.put(task.getId(), RunState.SCHEDULED);

        task.onScheduled(new Date().getTime());
        return task.getId();
    }



    public String printTasks() {
        String result = "Scheduled:\n";
        for (ScheduledTask st : scheduledTasks) {
            result += "when: "+st.when+" id: "+st.task.getId()+" scope: "+st.task.getScope()+"\n";
        }
        result += "Running:\n";
        for (Task t : runningTasks) {
            result += "id: "+t.getId()+" scope: "+t.getScope()+"\n";
        }
        return result;
    }

    public synchronized boolean runScheduledTask(UUID id) throws TaskException {
        if (id == null) return true;

        boolean found = false;
        ScheduledTask entry = null;
        for (ScheduledTask st : scheduledTasks) {
            if (st.task.getId().equals(id)) {
                found = true;
                entry = st;
                break;
            }
        }
        Task task;
        if (found) {
            task = entry.task;
            scheduledTasks.remove(entry);
            runningTasks.add(task);
        } else {
            return false;
        }

        log.debug("preparing to run task id "+id);
        runStates.put(task.getId(), RunState.RUNNING);
        log.debug("running task id "+id);

        task.onRun();
        this.finishRunning(task);
        return true;
    }


    public synchronized RunState getRunState(UUID id) {
        if (id == null) return RunState.FINISHED;

        return runStates.get(id);
    }

    public synchronized Task nextRunnable(long time) throws TaskException {
        Task task = null;
        // log.debug("nextRunnable starting");
        for (Task waiting : this.waitingToRun(time)) {
            if (waiting.getScope() == null || waiting.getScope().equals("")) {
                task = waiting;
                break;
            }

            boolean blocked = false;
            for (Task running : runningTasks) {
                if (running.getScope() == null || running.getScope().equals("")) {

                } else if (running.getScope().equals(waiting.getScope())) {
                    blocked = true;
                    log.debug(running.getId()+" blocks "+waiting.getId()+", scope "+waiting.getScope());
                }
            }
            if (!blocked) {
                task = waiting;
                break;
            }
        }

        if (task == null) {
            return task;
        }

        boolean found = false;
        ScheduledTask entry = null;
        for (ScheduledTask st : scheduledTasks) {
            if (st.task.equals(task)) {
                found = true;
                entry = st;
                break;
            }
        }
        if (found) {
            scheduledTasks.remove(entry);
            runningTasks.add(task);
            runStates.put(task.getId(), RunState.RUNNING);
        }
        if (task != null) {
            log.debug("nextRunnable: "+task.getId());
        } else {
            log.debug("nextRunnable: none");
        }

        return task;
    }


    public boolean hasItems() {
        return (!scheduledTasks.isEmpty() || !runningTasks.isEmpty());
    }

    private List<Task> waitingToRun(long time) {
        ArrayList<Task> result = new ArrayList<Task>();
        for (ScheduledTask st : scheduledTasks) {
            if (st.when < time) {
                if (st.task.getPrereqs().isEmpty()) {
                    result.add(st.task);
                    // System.out.println("task no prereqs: uuid " + st.task.getId() + " scope " + st.task.getScope());
                } else {
                    boolean prereqsComplete = true;
                    for (Task prereq : st.task.getPrereqs()) {
                        if (prereq.getOutcome().equals(Outcome.UNDEFINED)) {
                            prereqsComplete = false;
                        }
                    }
                    if (prereqsComplete) {
                        result.add(st.task);
                        // System.out.println("task prereqs complete: uuid " + st.task.getId() + " scope " + st.task.getScope());
                    } else {
                        // System.out.println("waiting on prereqs: uuid " + st.task.getId() + " scope " + st.task.getScope());
                    }
                }

            }
        }
        return result;
    }

    public synchronized void finishRunning(Task task) {
        log.debug("finishRunning "+task.getId());
        runningTasks.remove(task);
        runStates.put(task.getId(), RunState.FINISHED);

    }



}
