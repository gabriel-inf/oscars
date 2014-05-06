package net.es.nsi.client.types;


public interface NsiAuth {
    public AuthType getAuthType();

    public void setAuthType(AuthType authType);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

    public String getOauth();

    public void setOauth(String oauth);
}

