package net.es.oscars.pss.beans.review;

import net.es.oscars.pss.enums.ReviewChkStatus;


public class ReviewSpecification {

    private String gri;
    private ReviewChkStatus status;
    private String deviceId;

    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public ReviewChkStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewChkStatus status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
