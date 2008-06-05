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
    
    public static String RESV_CREATE_STARTED = "RESERVATION_CREATE_STARTED";
    public static String RESV_CREATE_COMPLETED = "RESERVATION_CREATE_COMPLETED";
    
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