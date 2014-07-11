package net.es.oscars.nsibridge.task;


import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.ifces.StateException;
import net.es.oscars.nsibridge.prov.DB_Util;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.state.life.NSI_Life_Event;
import net.es.oscars.nsibridge.state.life.NSI_Life_SM;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_Event;
import net.es.oscars.nsibridge.state.prov.NSI_Prov_SM;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpirationMonitor extends Thread {

    private static final Logger log = Logger.getLogger(ExpirationMonitor.class);
    private List<String> expiredConnIds = new ArrayList<String>();
    
    public void run(){
        while(true){
            try{
                this.execute();
                Thread.sleep(1000);
            }catch (InterruptedException e) {
                break;
            }catch(Exception e){
                log.error("Error in ExpirationMonitor: " + e.getMessage());
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
            if (expiredConnIds.contains(cr.getConnectionId())) {
                continue;
            }

            if (cr.getLifecycleState() == null) {
                continue;
            } else if (!cr.getLifecycleState().equals(LifecycleStateEnumType.CREATED)) {
                // nothing to do for FAILED, TERMINATING, PASSED_ENDTIME or TERMINATED records
                continue;
            }
            if (cr.getProvisionState() == null) {
                continue;
            } else if (cr.getProvisionState().equals(ProvisionStateEnumType.RELEASING)) {
                // wait til done releasing before trying again
                continue;
            }

            ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
            // if there's no committed resvRecord, we're before the very first ReserveHeld
            if (rr == null) {
                continue;
            // it's not time to terminate yet
            } else if (rr.getEndTime().getTime() > now.getTime()) {
                continue;
            }
            log.debug("past end time for connId "+cr.getConnectionId()+" endTime: ["+rr.getEndTime().getTime()+"] now: ["+now.getTime()+"]");

            // actually do things:
            String connId = cr.getConnectionId();
            NSI_Life_SM lsm;
            NSI_Prov_SM psm;
            try {
                DB_Util.restoreStateMachines(connId);
                lsm = smh.findNsiLifeSM(connId);
                psm = smh.findNsiProvSM(connId);
                if (lsm == null) {
                    log.error("no LSM found for "+connId);
                    continue;
                }
            } catch (ServiceException ex) {
                log.error(ex.toString(), ex);
                continue;
            }

            expiredConnIds.add(cr.getConnectionId());

            try {
                lsm.process(NSI_Life_Event.END_TIME, UUID.randomUUID().toString());
                psm.process(NSI_Prov_Event.END_TIME, UUID.randomUUID().toString());
            } catch (StateException ex) {
                log.error(ex);
            }

            try {
                DB_Util.persistStateMachines(cr.getConnectionId());
            } catch (ServiceException e) {
                log.error(e);
                e.printStackTrace();
            }


        }
    }



}
