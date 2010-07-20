package net.es.oscars.pss.eompls.junos;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.testng.log4testng.Logger;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSHandler;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.common.PathUtils;

public class EoMPLS_Junos implements PSSHandler {
    private PSSHandlerConfigBean config;
    private Logger log = Logger.getLogger(EoMPLS_Junos.class);
    private Scheduler scheduler = null;
    
    
    public void setup(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        OSCARSCore core = OSCARSCore.getInstance();

        if (direction.equals(PSSDirection.BIDIRECTIONAL)) {
            throw new PSSException("invalid direction");
        } else {
            String gri = resv.getGlobalReservationId();
            if (scheduler == null) {
                scheduler = core.getScheduleManager().getScheduler();
            }
            
            Node node = PathUtils.getNodeToConfigure(resv, direction);
            String nodeId = node.getTopologyIdent();

            String jobName = "setup-"+nodeId+"-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, "SERIALIZE_NODECONFIG_"+nodeId, PathSetupJob.class);
            log.debug("Adding job "+jobName);
            // FIXME
            System.out.println("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("resv", resv);
            jobDataMap.put("direction", direction);
            jobDetail.setJobDataMap(jobDataMap);
            try {
                scheduler.addJob(jobDetail, false);
            } catch (SchedulerException e) {
                log.error(e);
                throw new PSSException(e.getMessage());
            }
            
        
        }
    }

    public void status(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        // TODO Auto-generated method stub
    }

    public void teardown(Reservation resv, Path localPath, PSSDirection direction) throws PSSException {
        // TODO Auto-generated method stub
    }

    public void setConfig(PSSHandlerConfigBean config) {
        this.config = config;
    }

    public PSSHandlerConfigBean getConfig() {
        return config;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

}
