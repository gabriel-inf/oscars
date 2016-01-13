package net.es.oscars.pss.beans.review;

import net.es.oscars.pss.enums.ReviewStatus;

import java.util.ArrayList;
import java.util.List;

public class ReviewResult {
    private String gri;
    private ReviewStatus status;
    private String deviceId;
    private List<String> notes = new ArrayList<String>();

    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getNotes() {
        return notes;
    }

}
