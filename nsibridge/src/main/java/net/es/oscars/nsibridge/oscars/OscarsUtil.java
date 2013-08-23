package net.es.oscars.nsibridge.oscars;

import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.OscarsStatusRecord;
import net.es.oscars.nsibridge.common.PersistenceHolder;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import net.es.oscars.nsibridge.prov.NSI_SM_Holder;
import net.es.oscars.nsibridge.prov.NSI_Util;
import net.es.oscars.nsibridge.prov.TranslationException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.state.resv.NSI_Resv_SM;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.task.TaskException;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import java.util.Date;

public class OscarsUtil {
    private static final Logger log = Logger.getLogger(OscarsUtil.class);

    public static void submitResv(ResvRequest resvRequest) throws TranslationException, ServiceException  {
        log.debug("submitResv start");
        String connId = resvRequest.getReserveType().getConnectionId();
        ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);
        NSI_SM_Holder smh = NSI_SM_Holder.getInstance();
        NSI_Resv_SM rsm = smh.getResvStateMachines().get(connId);


        ResCreateContent rc = null;
        try {
            rc = NSI_OSCARS_Translation.makeOscarsResv(resvRequest);
            log.debug("translated NSI to OSCARS");
        } catch (TranslationException ex) {
            log.debug(ex);
            log.debug("could not translate NSI request");
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw ex;
        }

        if (rc == null) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            throw new TranslationException("null result in translation");
        }

        try {
            CreateReply reply = OscarsProxy.getInstance().sendCreate(rc);
            log.debug("connId: "+connId+" gri: "+reply.getGlobalReservationId());
            addOscarsRecord(cr, reply.getGlobalReservationId(), new Date(), reply.getStatus());
        } catch (OSCARSServiceException e) {
            addOscarsRecord(cr, null, new Date(), "FAILED");
            log.debug(e);
            throw new ServiceException("Failed to submit reservation");
        }
    }

    public static void submitQuery(ConnectionRecord cr) throws TranslationException {
        String oscarsGri = cr.getOscarsGri();
        if (oscarsGri == null || oscarsGri.equals("")) {
            throw new TranslationException("could not find OSCARS GRI for connId: "+cr.getConnectionId());
        }
        QueryResContent qc = NSI_OSCARS_Translation.makeOscarsQuery(oscarsGri);

        if (qc != null) {
            try {
                QueryResReply reply = OscarsProxy.getInstance().sendQuery(qc);
                String oscStatus =  reply.getReservationDetails().getStatus();

                log.debug("query result for connId: "+cr.getConnectionId()+" gri: "+oscarsGri+" status: "+oscStatus);
                addOscarsRecord(cr, oscarsGri, new Date(), oscStatus);


            } catch (OSCARSServiceException e) {
                e.printStackTrace();
            }
        } else {
            throw new TranslationException("could not translate to OSCARS query");
        }
    }

    public static void addOscarsRecord(ConnectionRecord cr, String gri, Date date, String status) {
        String connId = cr.getConnectionId();
        log.debug("addOscarsRecord connId: "+connId+" gri: "+gri+" status: "+status);
        EntityManager em = PersistenceHolder.getEntityManager();

        em.getTransaction().begin();
        OscarsStatusRecord or = new OscarsStatusRecord();
        or.setDate(date);
        cr.getOscarsStatusRecords().add(or);
        cr.setOscarsGri(gri);
        or.setStatus(status);
        em.persist(cr);
        em.getTransaction().commit();

    }
}
