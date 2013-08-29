package net.es.oscars.nsibridge.task;


import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.oscars.OscarsProxy;
import net.es.oscars.nsibridge.oscars.OscarsStates;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

public class OscarsQueryTask extends Task  {
    private static final Logger log = Logger.getLogger(OscarsQueryTask.class);

    private String corrId = "";

    public OscarsQueryTask(String corrId) {
        this.scope = "oscars";
        this.corrId = corrId;
    }

    public void onRun() throws TaskException {
        log.debug(this.id + " starting");
        try {
            super.onRun();
            RequestHolder rh = RequestHolder.getInstance();
            QueryRequest req = rh.getQueryRequests().get(corrId);
            List<String> connIds = req.getQuery().getConnectionId();


            // FIXME
            String connId = connIds.get(0);



            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);

            NSI_SM_Holder smh = NSI_SM_Holder.getInstance();

            String oscarsGri = cr.getOscarsGri();
            if (oscarsGri == null || oscarsGri.equals("")) {
                throw new TaskException("could not find OSCARS GRI for connId: "+connId);
            }

            QueryResContent qc = null;
            try {
                qc = NSI_OSCARS_Translation.makeOscarsQuery(oscarsGri);
            } catch (TranslationException ex) {
                log.debug(ex);
                log.debug("could not translate NSI request");

            }
            if (qc != null) {
                try {
                    QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc);

                    log.debug("connId: "+connId+"gri: "+reply.getReservationDetails().getGlobalReservationId());

                    EntityManager em = PersistenceHolder.getEntityManager();
                    em.getTransaction().begin();
                    OscarsStatusRecord or = new OscarsStatusRecord();
                    or.setDate(new Date());
                    or.setStatus(reply.getReservationDetails().getStatus());
                    cr.getOscarsStatusRecords().add(or);
                    em.persist(cr);
                    em.getTransaction().commit();

                } catch (OSCARSServiceException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

}
