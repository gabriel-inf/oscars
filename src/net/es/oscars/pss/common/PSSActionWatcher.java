package net.es.oscars.pss.common;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import net.es.oscars.pss.PSSException;

public class PSSActionWatcher {
    private boolean watching = false;
    private Scheduler scheduler;
    private PSSQueuer queuer;

    private ConcurrentHashMap<String, PSSActionDirections> watchList = new ConcurrentHashMap<String, PSSActionDirections>();
    
    public synchronized void watch(String gri, PSSAction action, List<PSSDirection> directions) throws PSSException {
        if (this.watchList.containsKey(gri)) {
            System.out.println("was already watching "+gri);
        }
        PSSActionDirections ad = new PSSActionDirections();
        ad.setAction(action);
        ad.setDirections(directions);
        watchList.put(gri, ad);
        if (!watching) {
            watching = true;
            this.startWatchJob();
        }
    }
    public synchronized void unwatch(String gri) {
        this.watchList.remove(gri);
    }
    
    /** 
     * 
     */
    private void startWatchJob() throws PSSException{
        System.out.println("Starting up watch");
        // start a thread that will watch the watchList from here on every second
        String jobName = "PSSActionWatcher";
        JobDetail jobDetail = new JobDetail(jobName, "PSSActionWatcher", PSSActionWatchJob.class);

        System.out.println("Adding job "+jobName);
        jobDetail.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        jobDetail.setJobDataMap(jobDataMap);
        
        CronTrigger trigger = null;
        try {
            trigger = new CronTrigger("PSSActionWatcher", "PSS", "0/1 * * * * ?");
        } catch (ParseException ex) {
            throw new PSSException(ex.getMessage());
        }
        // SimpleTrigger trigger = new SimpleTrigger("PSSActionWatcherTrigger", "PSS", new Date());

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new PSSException(e.getMessage());
        }
    }
    
    
    
    private static PSSActionWatcher instance;
    private PSSActionWatcher() {
        
    }
    public static PSSActionWatcher getInstance() {
        if (instance == null) {
            instance = new PSSActionWatcher();
        }
        return instance;
    }
    
    
    public ConcurrentHashMap<String, PSSActionDirections> getWatchList() {
        return watchList;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setQueuer(PSSQueuer queuer) {
        this.queuer = queuer;
    }
    public PSSQueuer getQueuer() {
        return queuer;
    }


}
