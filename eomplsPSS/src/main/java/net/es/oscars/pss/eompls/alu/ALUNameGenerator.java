package net.es.oscars.pss.eompls.alu;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.pss.eompls.beans.ScopedResourceLock;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.dao.ScopedResourceLockDAO;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public class ALUNameGenerator {
    private static Logger log = Logger.getLogger(ALUNameGenerator.class);
    private static ALUNameGenerator instance;
    private ALUNameGenerator() {
    }
    public static ALUNameGenerator getInstance() {
        if (instance == null) {
            instance = new ALUNameGenerator();
        }
        return instance;
    }

    public String getLSPName(String gri) {
        return gri+"_lsp";
    }

    public String getPathName(String gri) {
        return gri+"_path";
    }

    public String numbers(String gri) {

        String circuitStr = gri;
        circuitStr = circuitStr.replaceAll("[^0-9]+", "");
        
        return circuitStr;
    }


    public String getVplsId(String deviceId, Integer preferred, String gri) {
        Integer vplsId = getIdentifier(deviceId+":vpls", gri, preferred, "6000-6999");
        return vplsId.toString();
    }

    public String getQosId(String deviceId, Integer preferred, String gri) {
        Integer qosId = getIdentifier(deviceId+":qos", gri, preferred, "6000-6999");
        return qosId.toString();
    }

    public String getSdpId(String deviceId, Integer preferred, String gri) {
        Integer sdpId = getIdentifier(deviceId+":sdp", gri, preferred, "6000-6999");
        return sdpId.toString();
    }

    public List<Integer> releaseVplsIds(String deviceId, String gri) {
        List<Integer> ids = releaseIdentifiers(deviceId + ":vpls", gri);
        return ids;
    }

    public List<Integer> releaseQosIds(String deviceId, String gri) {
        List<Integer> ids = releaseIdentifiers(deviceId + ":qos", gri);
        return ids;
    }
    public List<Integer> releaseSdpIds(String deviceId, String gri) {
        List<Integer> ids = releaseIdentifiers(deviceId + ":sdp", gri);
        return ids;
    }


    public static List<Integer> releaseIdentifiers(String scope, String gri) {
        log.debug("releasing ids for scope: "+scope+" gri: "+gri);
        String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();
        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        List<ScopedResourceLock> srls = srlDAO.getByScopeAndGri(scope, gri);
        List<Integer> ids = new ArrayList<Integer>();
        for (ScopedResourceLock srl : srls) {
            ids.add(srl.getResource());
            log.debug("releasing "+srl.toString());
            srlDAO.remove(srl);
        }
        return ids;

    }


    public static Integer getIdentifier(String scope, String gri, Integer preferred, String rangeExp) {
        String dbname = EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname();
        SessionFactory sf = HibernateUtil.getSessionFactory(dbname);
        sf.getCurrentSession().beginTransaction();
        ScopedResourceLockDAO srlDAO = new ScopedResourceLockDAO(dbname);
        ScopedResourceLock srl = srlDAO.createSRL(scope, rangeExp, preferred, gri);
        if (srl != null) {
            srlDAO.update(srl);
        }
        sf.getCurrentSession().flush();
        return srl.getResource();
    }


    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getEpipeId(String gri) {
        return numbers(gri);
    }

    
}
