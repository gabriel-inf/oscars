package net.es.oscars.pss.common;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;

public class PSSActionWatcher {
    private boolean watching = false;
    private Scheduler scheduler;
    private PSSQueuer queuer;
    private static Logger log = Logger.getLogger(PSSActionWatcher.class);
    private String jobName = "PSSActionWatcher";

    private ConcurrentHashMap<Reservation, PSSActionDirections> watchList = new ConcurrentHashMap<Reservation, PSSActionDirections>();
    
    public synchronized void watch(Reservation resv, PSSAction action, List<PSSDirection> directions) throws PSSException {
        String dirStr= "";
        for (PSSDirection dir : directions) {
            dirStr = dirStr+dir+" ";
        }
        String gri = resv.getGlobalReservationId();
        String subject = gri+" "+action+" "+dirStr;
        log.debug("starting to watch "+subject);

        if (watchList.containsKey(resv)) {
            PSSActionDirections prv = watchList.get(resv);
            PSSAction prvAction = prv.getAction();
            String prvDirStr= "";
            List<PSSDirection> prvDirections = prv.getDirections();
            for (PSSDirection dir : prvDirections) {
                prvDirStr = prvDirStr+dir+" ";
            }
            String prvSubject = gri+" "+prvAction+" "+prvDirStr;
            
            if (!prvAction.equals(action) || 
                !(directions.containsAll(prvDirections) && prvDirections.containsAll(directions))) {
                throw new PSSException("can't watch "+subject+" - was already watching "+prvSubject);
            }
                
        }
        PSSActionDirections ad = new PSSActionDirections();
        ad.setAction(action);
        ad.setDirections(directions);
        watchList.put(resv, ad);
        if (!watching) {
            log.debug("was not previously watching");
            watching = true;
            this.startWatchJob();
        }
    }
    
    public synchronized void unwatch(Reservation resv) {
        String gri = resv.getGlobalReservationId();
        log.debug("unwatching "+gri);
        this.watchList.remove(resv);
    }
    
    public synchronized void stopIfUnneeded() {
        if (this.watchList.isEmpty()) {
            log.debug("stopping because watchlist is empty");
            try {
                watching = false;
                scheduler.pauseJob(jobName, "PSS");
            } catch (SchedulerException e) {
                log.error(e);
            }
        }
        
    }
    
    private void startWatchJob() throws PSSException{
        System.out.println("Starting up a watch job");
        try {
            scheduler.resumeJob(jobName, "PSS");
        } catch (SchedulerException e) {
            log.error(e);
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
    
    
    public ConcurrentHashMap<Reservation, PSSActionDirections> getWatchList() {
        return watchList;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        // start a thread that will watch the watchList from here on every second
        JobDetail watcherJob = new JobDetail(jobName, "PSS", PSSActionWatchJob.class);
        watcherJob.setDurability(true);
        JobDataMap jobDataMap = new JobDataMap();
        watcherJob.setJobDataMap(jobDataMap);
        
        CronTrigger watcherTrigger = null;
        try {
            watcherTrigger = new CronTrigger("PSSActionWatcherTrigger", "PSS", "0/5 * * * * ?");
        } catch (ParseException ex) {
            log.error(ex);
        }
        try {
            scheduler.scheduleJob(watcherJob, watcherTrigger);
            scheduler.pauseJob(jobName, "PSS");
        } catch (SchedulerException e) {
            log.error(e);
        }
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
