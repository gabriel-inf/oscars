package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationStateEnumType;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_Event;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ResvTimeoutMonitor extends Thread {

    private static final Logger log = Logger.getLogger(ResvTimeoutMonitor.class);

    public void run(){
        while(true){
            try{
                this.execute();
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                break;
            }catch(Exception e){
                log.error("Error in ResvTimeoutMonitor: " + e.getMessage());
            }
        }
    }
    
    public void execute() {
        Date now = new Date();

        List<ConnectionRecord> recordList;
        try {
            recordList = DB_Util.getConnectionRecords();

        } catch (ServiceException ex) {
            log.error(ex);
            return;
        }
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

        for (ConnectionRecord cr : recordList) {

            String connId = cr.getConnectionId();

            List<ResvRecord> rrs = ConnectionRecord.getUncommittedResvRecords(cr);
            if (rrs == null || rrs.size() == 0) {
                // log.debug("no uncommitted resvRecords for connId:"+connId);
                continue;
            }


            if (cr.getReserveState() == null) {
                log.debug("no reserve state for connId: " + connId);
                continue;
            }
            NSI_Resv_SM rsm;
            try {
                DB_Util.restoreStateMachines(connId);
                rsm = smh.findNsiResvSM(connId);
                if (rsm == null) {
                    log.error("no RSM found for "+connId);
                    continue;
                }
            } catch (ServiceException ex) {
                log.error(ex.toString(), ex);
                continue;
            }


            if (!cr.getReserveState().equals(ReservationStateEnumType.RESERVE_HELD)) {
                continue;
            }




            ResvRecord rr = rrs.get(0);
            Date submittedAt = rr.getSubmittedAt();
            ApplicationContext ax = SpringContext.getInstance().getContext();
            TimingConfig tx = ax.getBean("timingConfig", TimingConfig.class);
            Long resvTimeout = new Double(tx.getResvTimeout()).longValue();


            Long timeoutMs = submittedAt.getTime() + resvTimeout * 1000;
            Date timeout = new Date(timeoutMs);


            if (timeout.before(now)) {
                log.debug("timed out connId: "+connId+" RR v: "+rr.getVersion()+" timeout:" +resvTimeout);
                try {
                    rsm.process(NSI_Resv_Event.RESV_TIMEOUT, UUID.randomUUID().toString());
                    DB_Util.persistStateMachines(connId);
                } catch (ServiceException ex) {
                    log.error(ex);

                } catch (StateException ex) {
                    log.error(ex);
                }
            }
        }


    }



}
