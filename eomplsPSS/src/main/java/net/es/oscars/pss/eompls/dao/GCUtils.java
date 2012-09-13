package net.es.oscars.pss.eompls.dao;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.enums.ActionType;
import net.es.oscars.pss.eompls.beans.GeneratedConfig;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.List;


public class GCUtils {

    private static Logger log = Logger.getLogger(GCUtils.class);

    public static String retrieveDeviceConfig(String gri, String deviceId, ActionType phase) throws PSSException {
        String config = null;
        String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();
        GeneratedConfigDAO gcDAO =  new GeneratedConfigDAO(dbname);

        List<GeneratedConfig> gcs= gcDAO.getGC(phase.toString(), gri, deviceId);
        if (gcs.isEmpty()) {
            log.error("could not find saved generated config for gri "+gri+" device "+deviceId+" phase: "+phase);
        } else if (gcs.size() > 1) {

            log.error("found multiple generated configs for gri "+gri+" device "+deviceId+" for phase: "+phase);
        } else {
            log.debug("found exactly one saved config for gri "+gri+" device "+deviceId+" for phase: "+phase);
            config = gcs.get(0).getConfig();
        }
        sf.getCurrentSession().flush();

        return config;
    }

    public static void storeDeviceConfig(String gri, String deviceId, ActionType phase, String config) throws PSSException {
        String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();
        GeneratedConfigDAO gcDAO =  new GeneratedConfigDAO(dbname);
        GeneratedConfig gc = new GeneratedConfig();
        gc.setConfig(config);
        gc.setDeviceId(deviceId);
        gc.setGri(gri);
        gc.setPhase(phase.toString());
        gcDAO.update(gc);
        sf.getCurrentSession().flush();


    }


}
