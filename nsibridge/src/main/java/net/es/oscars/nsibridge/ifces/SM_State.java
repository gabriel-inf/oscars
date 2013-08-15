package net.es.oscars.nsibridge.ifces;

public interface SM_State {
    public Object state();
    public String value();
    public void setValue(String value);
    public void setState(Object state);

}
