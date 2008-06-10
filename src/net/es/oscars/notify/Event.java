package net.es.oscars.notify;

import net.es.oscars.bss.Reservation;
import java.io.Serializable;

public class Event implements Serializable{
    private String type;
    private long timestamp;
    private String userLogin;
    private String errorCode;
    private String errorMessage;
    private Reservation reservation;
    private String source;
    
    public static String RESV_CREATE_STARTED = "RESERVATION_CREATE_STARTED";
    public static String RESV_CREATE_COMPLETED = "RESERVATION_CREATE_COMPLETED";
    public static String RESV_CREATE_FAILED = "RESERVATION_CREATE_FAILED";
    public static String RESV_MODIFY_STARTED = "RESERVATION_MODIFTY_STARTED";
    public static String RESV_MODIFY_COMPLETED = "RESERVATION_MODIFY_COMPLETED";
    public static String RESV_MODIFY_FAILED = "RESERVATION_MODIFY_FAILED";
    public static String RESV_CANCEL_STARTED = "RESERVATION_CANCEL_STARTED";
    public static String RESV_CANCEL_COMPLETED = "RESERVATION_CANCEL_COMPLETED";
    public static String RESV_CANCELLED = "RESERVATION_CANCELLED";
    public static String RESV_CANCEL_FAILED = "RESERVATION_CANCEL_FAILED";
    public static String RESV_INVALIDATED = "RESERVATION_INVALIDATED";
    public static String PATH_SETUP_STARTED = "PATH_SETUP_STARTED";
    public static String PATH_SETUP_COMPLETED = "PATH_SETUP_COMPLETED";
    public static String PATH_SETUP_FAILED = "PATH_SETUP_FAILED";
    public static String PATH_TEARDOWN_STARTED = "PATH_STEARDOWN_STARTED";
    public static String PATH_TEARDOWN_COMPLETED = "PATH_TEARDOWN_COMPLETED";
    public static String PATH_TEARDOWN_FAILED = "PATH_TEARDOWN_FAILED";
    public static String RESV_EXPIRES_IN_1DAY = "RESERVATION_EXPIRES_IN_1DAY";
    public static String RESV_EXPIRES_IN_7DAYS = "RESERVATION_EXPIRES_IN_7DAYS";
    public static String RESV_EXPIRES_IN_30DAYS = "RESERVATION_EXPIRES_IN_30DAYS";
    
    public void setType(String type){
        this.type = type;
    }
    
    public String getType(){
        return this.type;
    }
    
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    
    public long getTimestamp(){
        return this.timestamp;
    }
    
    public void setUserLogin(String userLogin){
        this.userLogin = userLogin;
    }
    
    public String getSource(){
        return this.source;
    }
    
    public void setSource(String source){
        this.source = source;
    }
    
    public String getUserLogin(){
        return this.userLogin;
    }
    
    public void setErrorCode(String errorCode){
        this.errorCode = errorCode;
    }
    
    public String getErrorCode(){
        return this.errorCode;
    }
    
    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }
    
    public String getErrorMessage(){
        return this.errorMessage;
    }
    
    public void setReservation(Reservation reservation){
        this.reservation = reservation;
    }
    
    public Reservation getReservation(){
        return this.reservation;
    }
}