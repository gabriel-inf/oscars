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
    private Logger log = Logger.getLogger(PSSActionWatcher.class);

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
                scheduler.unscheduleJob("PSSActionWatcher", "PSS");
            } catch (SchedulerException e) {
                log.error(e);
            }
        }
        
    }
    
    private void startWatchJob() throws PSSException{
        System.out.println("Starting up a watch job");
        // start a thread that will watch the watchList from here on every second
        String jobName = "PSSActionWatcher";
        JobDetail jobDetail = new JobDetail(jobName, "PSSActionWatcher", PSSActionWatchJob.class);
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
    
    
    public ConcurrentHashMap<Reservation, PSSActionDirections> getWatchList() {
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