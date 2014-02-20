package net.es.oscars.pss.eompls.util;

import net.es.oscars.api.soap.gen.v06.OptionalConstraintType;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import org.apache.log4j.Logger;

public class VPLS_ServiceParams {
    private static Logger log = Logger.getLogger(VPLS_ServiceParams.class);
    protected boolean softPolice = false;
    protected boolean applyQos = true;
    protected boolean protection = false;

    public boolean isSoftPolice() {
        return softPolice;
    }

    public void setSoftPolice(boolean softPolice) {
        this.softPolice = softPolice;
    }

    public boolean isApplyQos() {
        return applyQos;
    }

    public void setApplyQos(boolean applyQos) {
        this.applyQos = applyQos;
    }

    public boolean isProtection() {
        return protection;
    }

    public void setProtection(boolean protection) {
        this.protection = protection;
    }



    public static VPLS_ServiceParams fromResDetails(ResDetails res) {
        VPLS_ServiceParams params = new VPLS_ServiceParams();

        for (OptionalConstraintType oct : res.getOptionalConstraint()) {
            if (oct.getCategory().equals("policing")) {
                if (oct.getValue().getStringValue().equals("hard")) {
                    params.setSoftPolice(false);
                } else if (oct.getValue().getStringValue().equals("soft")) {
                    params.setSoftPolice(true);
                } else {
                    log.error("invalid softpolice value:"+oct.getValue().getStringValue());
                }
            }
            if (oct.getCategory().equals("apply-qos")) {
                if (oct.getValue().getStringValue().equals("true")) {
                    params.setApplyQos(true);
                } else if (oct.getValue().getStringValue().equals("false")) {
                    params.setApplyQos(false);
                } else {
                    log.error("invalid apply-qos value:"+oct.getValue().getStringValue());
                }
            }
            if (oct.getCategory().equals("protection")) {
                if (oct.getValue().getStringValue().equals("loose-secondary-path")) {
                    params.setProtection(true);
                } else if (oct.getValue().getStringValue().equals("none")) {
                    params.setProtection(false);
                } else {
                    log.error("invalid protection value:"+oct.getValue().getStringValue());
                }
            }
        }
        log.debug("soft: "+params.isSoftPolice()+" qos: "+params.isApplyQos()+" prot:"+params.isProtection());
        return params;
    }

}
