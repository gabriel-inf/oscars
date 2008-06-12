package net.es.oscars.notify;

import net.es.oscars.bss.Reservation;
import java.io.Serializable;

/**
 * Stores information about an event that occurs in OSCARS. It implements
 * the Serializable interface so that it can be passed to methods over
 * Remote Method Invocation (RMI).
 */
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
    
    /**
     * Sets the type of event. See the constants defined in this class for 
     * common event types.
     *
     * @param type the type to set
     */
    public void setType(String type){
        this.type = type;
    }
    
    /**
     * Returns the type of event. See the constants defined in this class for 
     * common event types.
     *
     * @return the type of event
     */
    public String getType(){
        return this.type;
    }
    
    /**
     * Sets the timestamp at which the event ocurred. 
     *
     * @param timestamp the time (in milliseconds) of when the event occurred
     */
    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }
    
    /**
     * Returns the timestamp at which the event ocurred. 
     *
     * @return the timestamp (in milliseconds) of when the event occurred
     */
    public long getTimestamp(){
        return this.timestamp;
    }
    
    /**
     * Sets the login of the user that triggered the event. For events 
     * affecting reservations this may or may not be the same person that
     * created the reservation.     
     *
     * @param userLogin the user login to set
     */
    public void setUserLogin(String userLogin){
        this.userLogin = userLogin;
    }
    
    /**
     * Returns the login of the user that triggered the event. For events 
     * affecting reservations this may or may not be the same person that
     * created the reservation.     
     *
     * @return the login of the event triggering user
     */
    public String getUserLogin(){
        return this.userLogin;
    }
    
    /**
     * Sets the component that reported the event. Currently may be API, WBUI,
     * or SCHEDULER.
     *
     * @param source name of the reporting compenent (WBUI, API, or SCHEDULER)
     */
    public void setSource(String source){
        this.source = source;
    }
    
    /**
     * Returns the component that reported the event. Currently may be API, WBUI,
     * or SCHEDULER.
     *
     * @return name of the reporting compenent (WBUI, API, or SCHEDULER)
     */
    public String getSource(){
        return this.source;
    }
    
    /**
     * Sets the error code if the event is reporting an error.
     *
     * @param errorCode the error code to set
     */
    public void setErrorCode(String errorCode){
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the error code if the event is reporting an error.
     *
     * @return the error code of the event
     */
    public String getErrorCode(){
        return this.errorCode;
    }
    
    /**
     * Sets the error message if the event is reporting an error.
     *
     * @param errorMessage the error message to set
     */
    public void setErrorMessage(String errorMessage){
        this.errorMessage = errorMessage;
    }
    
    /**
     * Returns the error message if the event is reporting an error.
     *
     * @return the error message of the event
     */
    public String getErrorMessage(){
        return this.errorMessage;
    }
    
    /**
     * Sets the reservation affected by this event
     *
     * @param reservation the reservation to set
     */
    public void setReservation(Reservation reservation){
        this.reservation = reservation;
    }
    
    /**
     * Returns the reservation affected by this event
     *
     * @return the reservation affected by this event
     */
    public Reservation getReservation(){
        return this.reservation;
    }
}