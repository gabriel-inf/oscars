package net.es.oscars.pss.eompls.util;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.eompls.dao.SRLUtils;
import net.es.oscars.utils.topology.PathTools;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

public class VPLS_Identifier {
    public final static Integer NONE = -1;
    private static Logger log = Logger.getLogger(VPLS_Identifier.class);
    protected Integer vplsId;

    protected Integer secondaryVplsId;

    public Integer getVplsId() {
        return vplsId;
    }

    public void setVplsId(Integer vplsId) {
        this.vplsId = vplsId;
    }

    public Integer getSecondaryVplsId() {
        return secondaryVplsId;
    }

    public void setSecondaryVplsId(Integer secondaryVplsId) {
        this.secondaryVplsId = secondaryVplsId;
    }

    public static VPLS_Identifier reserve(String gri) throws PSSException {
        return VPLS_Identifier.reserve(gri, false);
    }


    public static VPLS_Identifier reserve(String gri, boolean secondary) throws PSSException {
        log.debug("gri: "+gri+" secondary? "+secondary);
        VPLS_Identifier srids = new VPLS_Identifier();

        String vplsScope = PathTools.getLocalDomainId() + ":vpls";
        String rangeExpr = "6000-6999";
        String idString = "";
        List<Integer> ids;

        ids = SRLUtils.getExistingIdentifiers(vplsScope, gri);

        if (!secondary) {
            if (ids == null || ids.size() == 0) {
                log.debug("did not find VPLS ids");
                Integer vplsId = SRLUtils.getIdentifier(vplsScope, gri, null, rangeExpr);
                srids.setVplsId(vplsId);

            } else if (ids.size() > 1) {
                idString = StringUtils.join(ids, ", ");
                log.error("multiple vpls ids found: [" + idString + "] , getting first one");
                srids.setVplsId(ids.get(0));
            } else {
                log.debug("found VPLS id: " + ids.get(0));
                srids.setVplsId(ids.get(0));
            }
            srids.setSecondaryVplsId(NONE);

            // add one record
            String thisGriScope = gri+":vpls";
            Integer tmpId = SRLUtils.getIdentifier(thisGriScope, gri, 1, "1-100");
            log.debug("saved a trick SRL: "+thisGriScope+" "+tmpId);
            log.debug("saved primary VPLS id: "+srids.getVplsId()+ ", no secondary");

        } else {
            if (ids == null || ids.size() == 0) {
                log.debug("did not find any VPLS ids");

                Integer vplsId = SRLUtils.getIdentifier(vplsScope, gri, null, rangeExpr);
                srids.setVplsId(vplsId);

                vplsId = SRLUtils.getIdentifier(vplsScope, gri, null, rangeExpr);
                srids.setSecondaryVplsId(vplsId);

                log.debug("saved primary VPLS id: "+srids.getVplsId()+ ", secondary: "+srids.getSecondaryVplsId());

            } else if (ids.size() > 2) {
                idString = StringUtils.join(ids, ", ");
                log.error("more than 2 vpls ids found: [" + idString + "] , getting first two (this should not happen!)");
                srids.setVplsId(ids.get(0));
                srids.setSecondaryVplsId(ids.get(1));
            } else {
                log.debug("found VPLS ids: " + ids.get(0) + ", " + ids.get(1));
                srids.setVplsId(ids.get(0));
                srids.setSecondaryVplsId(ids.get(1));
            }
            // add two trick records
            String thisGriScope = gri+":vpls";
            Integer tmpId = SRLUtils.getIdentifier(thisGriScope, gri, 1, "1-100");
            log.debug("saved a trick SRL: "+thisGriScope+" "+tmpId);
            String protGriScope = gri+":vpls-protect";
            tmpId = SRLUtils.getIdentifier(protGriScope, gri, 1, "1-100");
            log.debug("saved a trick SRL: "+protGriScope+" "+tmpId);


        }


        return srids;

    }



    public static VPLS_Identifier release(String gri) throws PSSException {
        VPLS_Identifier srids = new VPLS_Identifier();
        boolean protect = false;

        String vplsScope = PathTools.getLocalDomainId() + ":vpls";
        String idString;
        List<Integer> srlIds;
        List<Integer> protSrlIds;
        List<Integer> vplsIds;


        // find if there's any SRLs
        String thisGriScope = gri+":vpls";
        srlIds = SRLUtils.getExistingIdentifiers(thisGriScope, gri);
        if (srlIds == null || srlIds.size() == 0) {
            log.debug("no existing identifiers for gri scope: "+thisGriScope);
        } else {
            // release one
            log.debug("releasing a trick SRL: "+thisGriScope+" "+srlIds.get(0));
            SRLUtils.releaseIdentifier(thisGriScope, srlIds.get(0));
        }

        String protGriScope = gri+":vpls-protect";
        protSrlIds = SRLUtils.getExistingIdentifiers(protGriScope, gri);
        if (protSrlIds == null || protSrlIds.size() == 0) {
            // do nothing
        } else {
            // release one
            log.debug("releasing a trick SRL: "+thisGriScope+" "+protSrlIds.get(0));
            SRLUtils.releaseIdentifier(protGriScope, protSrlIds.get(0));
            protect = true;
        }



        // check if those were the last ones - if so release the global VPLS id
        if (protect) {
            if (protSrlIds.size() == 1 && srlIds.size() == 1) {
                vplsIds = SRLUtils.releaseIdentifiers(vplsScope, gri);
                srids.setVplsId(vplsIds.get(0));
                srids.setSecondaryVplsId(vplsIds.get(1));
                idString = StringUtils.join(vplsIds, ", ");
                log.debug("released vpls id(s) :"+idString+" for gri: "+gri);
            } else if (protSrlIds.size() != srlIds.size()) {
                throw new PSSException("SRL id size mismatch");
            } else {
                vplsIds = SRLUtils.getExistingIdentifiers(vplsScope, gri);
                srids.setVplsId(vplsIds.get(0));
                srids.setSecondaryVplsId(vplsIds.get(1));
                log.debug("found vpls ids: " + vplsIds.get(0) +" "+ vplsIds.get(1)+" but not releasing yet");
            }
        } else {
            if (srlIds == null) {
                log.debug("already released for scope: "+vplsScope+" gri: "+gri);
            } else if (srlIds.size() == 1) {
                vplsIds = SRLUtils.releaseIdentifiers(vplsScope, gri);
                srids.setVplsId(vplsIds.get(0));
                idString = StringUtils.join(vplsIds, ", ");
                log.debug("released vpls id(s) :" + idString + " for gri: " + gri);
            } else {
                vplsIds = SRLUtils.getExistingIdentifiers(vplsScope, gri);
                srids.setVplsId(vplsIds.get(0));
                log.debug("found vpls id: " + vplsIds.get(0) + " but not releasing yet");
            }
        }


        return srids;
    }

}
