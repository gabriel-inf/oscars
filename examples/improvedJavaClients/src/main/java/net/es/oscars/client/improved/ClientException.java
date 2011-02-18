package net.es.oscars.client.improved;

public class ClientException extends Exception {
    
    private static final long serialVersionUID = 1L;
    private String message;
    @Override public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public ClientException() {
        super();
    }
    public ClientException(String message) {
        super();
        this.message = message;
    }
    public ClientException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }
    public ClientException(Throwable cause) {
        super(cause);
    }
}
