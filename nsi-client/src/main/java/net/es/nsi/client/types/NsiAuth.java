package net.es.nsi.client.types;


public class NsiAuth {
    protected Long id;
    protected AuthType authType;
    protected String username;
    protected String password;
    protected String oauth;

    public String toString() {

        String out = "";
        out += "\n          type:        "+authType;
        out += "\n          username:    "+username;
        out += "\n          password:    <SECRET>";
        out += "\n          oauth:       <SECRET>";
        return out;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOauth() {
        return oauth;
    }

    public void setOauth(String oauth) {
        this.oauth = oauth;
    }
}
