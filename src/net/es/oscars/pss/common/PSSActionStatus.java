package net.es.oscars.pss.common;


public class PSSActionStatus {
    private PSSStatus status;
    private String message;
    private Integer timestamp;
    
    public PSSStatus getStatus() {
        return status;
    }
    public void setStatus(PSSStatus status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }
    public Integer getTimestamp() {
        return timestamp;
    }
}
