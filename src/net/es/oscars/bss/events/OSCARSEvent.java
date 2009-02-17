package net.es.oscars.bss.events;

import net.es.oscars.bss.Reservation;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Stores information about an event that occurs in OSCARS. It implements
 * the Serializable interface so that it can be passed to methods over
 * Remote Method Invocation (RMI).
 */
public class OSCARSEvent implements Serializable{
    private String type;
    private long timestamp;
    private String userLogin;
    private String errorCode;
    private String errorMessage;
    private HashMap<String, String[]> reservationParams;
    private Reservation reservation;
    private String producerUrl;
    private String subscriptionId;
    private String source;
    
    public static String RESV_CREATE_RECEIVED = "RESERVATION_CREATE_RECEIVED";
    public static String RESV_CREATE_ACCEPTED = "RESERVATION_CREATE_ACCEPTED";
    public static String RESV_CREATE_STARTED = "RESERVATION_CREATE_STARTED";
    public static String RESV_CREATE_FWD_STARTED = "RESERVATION_CREATE_FORWARD_STARTED";
    public static String RESV_CREATE_FWD_ACCEPTED = "RESERVATION_CREATE_FORWARD_ACCEPTED";
    public static String RESV_CREATE_CONFIRMED = "RESERVATION_CREATE_CONFIRMED";
    public static String RESV_CREATE_COMPLETED = "RESERVATION_CREATE_COMPLETED";
    public static String RESV_CREATE_FAILED = "RESERVATION_CREATE_FAILED";
    
    public static String RESV_MODIFY_RECEIVED= "RESERVATION_MODIFY_RECEIVED";
    public static String RESV_MODIFY_ACCEPTED = "RESERVATION_MODIFY_ACCEPTED";
    public static String RESV_MODIFY_STARTED = "RESERVATION_MODIFY_STARTED";
    public static String RESV_MODIFY_FWD_STARTED = "RESERVATION_MODIFY_FORWARD_STARTED";
    public static String RESV_MODIFY_FWD_ACCEPTED = "RESERVATION_MODIFY_FORWARD_ACCEPTED";
    public static String RESV_MODIFY_CONFIRMED = "RESERVATION_MODIFY_CONFIRMED";
    public static String RESV_MODIFY_COMPLETED = "RESERVATION_MODIFY_COMPLETED";
    public static String RESV_MODIFY_FAILED = "RESERVATION_MODIFY_FAILED";
    
    public static String RESV_CANCEL_RECEIVED = "RESERVATION_CANCEL_RECEIVED";
    public static String RESV_CANCEL_ACCEPTED = "RESERVATION_CANCEL_ACCEPTED";
    public static String RESV_CANCEL_STARTED = "RESERVATION_CANCEL_STARTED";
    public static String RESV_CANCEL_FWD_STARTED = "RESERVATION_CANCEL_FORWARD_STARTED";
    public static String RESV_CANCEL_FWD_ACCEPTED = "RESERVATION_CANCEL_FORWARD_ACCEPTED";
    public static String RESV_CANCEL_CONFIRMED = "RESERVATION_CANCEL_CONFIRMED";
    public static String RESV_CANCEL_COMPLETED = "RESERVATION_CANCEL_COMPLETED";
    public static String RESV_CANCEL_FAILED = "RESERVATION_CANCEL_FAILED";
    
    public static String PATH_SETUP_RECEIVED = "PATH_SETUP_RECEIVED";
    public static String PATH_SETUP_ACCEPTED = "PATH_SETUP_ACCEPTED";
    public static String PATH_SETUP_STARTED = "PATH_SETUP_STARTED";
    public static String PATH_SETUP_FWD_STARTED = "PATH_SETUP_FORWARD_STARTED";
    public static String PATH_SETUP_FWD_ACCEPTED = "PATH_SETUP_FORWARD_ACCEPTED";
    public static String PATH_SETUP_CONFIRMED = "PATH_SETUP_CONFIRMED";
    public static String PATH_SETUP_COMPLETED = "PATH_SETUP_COMPLETED";
    public static String PATH_SETUP_FAILED = "PATH_SETUP_FAILED";
    
    public static String PATH_REFRESH_RECEIVED = "PATH_REFRESH_RECEIVED";
    public static String PATH_REFRESH_ACCEPTED = "PATH_REFRESH_ACCEPTED";
    public static String PATH_REFRESH_STARTED = "PATH_REFRESH_STARTED";
    public static String PATH_REFRESH_FWD_STARTED = "PATH_REFRESH_FORWARD_STARTED";
    public static String PATH_REFRESH_FWD_ACCEPTED = "PATH_REFRESH_FORWARD_ACCEPTED";
    public static String PATH_REFRESH_CONFIRMED = "PATH_REFRESH_CONFIRMED";
    public static String PATH_REFRESH_COMPLETED = "PATH_REFRESH_COMPLETED";
    public static String PATH_REFRESH_FAILED = "PATH_REFRESH_FAILED";
    
    public static String PATH_TEARDOWN_RECEIVED = "PATH_TEARDOWN_RECEIVED";
    public static String PATH_TEARDOWN_ACCEPTED = "PATH_TEARDOWN_ACCEPTED";
    public static String PATH_TEARDOWN_STARTED = "PATH_TEARDOWN_STARTED";
    public static String PATH_TEARDOWN_FWD_STARTED = "PATH_TEARDOWN_FORWARD_STARTED";
    public static String PATH_TEARDOWN_FWD_ACCEPTED = "PATH_TEARDOWN_FORWARD_ACCEPTED";
    public static String PATH_TEARDOWN_CONFIRMED = "PATH_TEARDOWN_CONFIRMED";
    public static String PATH_TEARDOWN_COMPLETED = "PATH_TEARDOWN_COMPLETED";
    public static String PATH_TEARDOWN_FAILED = "PATH_TEARDOWN_FAILED";
   
    public static String UP_PATH_SETUP_CONFIRMED = "UPSTREAM_PATH_SETUP_CONFIRMED";
    public static String DOWN_PATH_SETUP_CONFIRMED = "DOWNSTREAM_PATH_SETUP_CONFIRMED";
    public static String UP_PATH_REFRESH_CONFIRMED = "UPSTREAM_PATH_REFRESH_CONFIRMED";
    public static String DOWN_PATH_REFRESH_CONFIRMED = "DOWNSTREAM_PATH_REFRESH_CONFIRMED";
    public static String UP_PATH_TEARDOWN_CONFIRMED = "UPSTREAM_PATH_TEARDOWN_CONFIRMED";
    public static String DOWN_PATH_TEARDOWN_CONFIRMED = "DOWNSTREAM_PATH_TEARDOWN_CONFIRMED";
    
    public static String RESV_LIST_RECEIVED = "RESERVATION_LIST_RECEIVED";
    public static String RESV_LIST_STARTED = "RESERVATION_LIST_STARTED";
    public static String RESV_LIST_FWD_STARTED = "RESERVATION_LIST_FORWARD_STARTED";
    public static String RESV_LIST_FWD_COMPLETED = "RESERVATION_LIST_FORWARD_ACCEPTED";
    public static String RESV_LIST_COMPLETED = "RESERVATION_LIST_COMPLETED";
    public static String RESV_LIST_FAILED = "RESERVATION_LIST_FAILED";
    
    public static String RESV_QUERY_RECEIVED = "RESERVATION_QUERY_RECEIVED";
    public static String RESV_QUERY_STARTED = "RESERVATION_QUERY_STARTED";
    public static String RESV_QUERY_FWD_STARTED = "RESERVATION_QUERY_FORWARD_STARTED";
    public static String RESV_QUERY_FWD_COMPLETED = "RESERVATION_QUERY_FORWARD_ACCEPTED";
    public static String RESV_QUERY_COMPLETED = "RESERVATION_QUERY_COMPLETED";
    public static String RESV_QUERY_FAILED = "RESERVATION_QUERY_FAILED";
    
    public static String RESV_INVALIDATED = "RESERVATION_INVALIDATED";
    public static String RESV_PERIOD_STARTED = "RESERVATION_PERIOD_STARTED";
    public static String RESV_PERIOD_FINISHED = "RESERVATION_PERIOD_FINISHED";
    public static String RESV_EXPIRES_IN_1DAY = "RESERVATION_EXPIRES_IN_1DAY";
    public static String RESV_EXPIRES_IN_7DAYS = "RESERVATION_EXPIRES_IN_7DAYS";
    public static String RESV_EXPIRES_IN_30DAYS = "RESERVATION_EXPIRES_IN_30DAYS";
    public static String IDC_STARTED = "IDC_STARTED";
    public static String IDC_FAILED = "IDC_FAILED";
    
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
     * Set reservationParams if you want a snapshot of a reservation 
     * at the time an event happened.
     *
     * @param reservationParams the reservation parameters to set
     */
    public void setReservationParams(HashMap<String, String[]> reservationParams){
        this.reservationParams = reservationParams;
    }

    /**
     * Returns the reservation parameters of this event
     *
     * @return the reservation parameters of this event
     */
    public HashMap<String, String[]> getReservationParams(){
        return this.reservationParams;
    }

    /**
     * Set reservation if you received an event and want an easy
     * way to read the fields.
     * 
     * @return the reservation
     */
    public Reservation getReservation() {
        return this.reservation;
    }

    /**
     * @param reservation the reservation to set
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * @return the producerUrl
     */
    public String getProducerUrl() {
        return this.producerUrl;
    }

    /**
     * @param producerUrl the producerUrl to set
     */
    public void setProducerUrl(String producerUrl) {
        this.producerUrl = producerUrl;
    }

    /**
     * @return the producerId
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @param producerId the producerId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}