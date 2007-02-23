package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * User is adapted from an Middlegen class automatically generated 
 * from the schema for the aaa.users table.
 */
public class User implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4149;

    /** identifier field */
    private Integer id;

    /** persistent field */
    private String login;

    /** nullable persistent field */
    private String certificate;

    /** nullable persistent field */
    private String certSubject;

    /** persistent field */
    private String lastName;

    /** persistent field */
    private String firstName;

    /** persistent field */
    private String emailPrimary;

    /** persistent field */
    private String phonePrimary;

    /** nullable persistent field */
    private String password;

    /** nullable persistent field */
    private String description;

    /** nullable persistent field */
    private String emailSecondary;

    /** nullable persistent field */
    private String phoneSecondary;

    /** nullable persistent field */
    private String status;

    /** nullable persistent field */
    private String activationKey;

    /** nullable persistent field */
    private Long lastActiveTime;

    /** nullable persistent field */
    private Long registerTime;

    /** persistent field */
    private Institution institution;

    /** default constructor */
    public User() { }

    /**
     * @return id An Integer with a user table primary key
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id An Integer with a user table primary key
     */ 
    public void setId(Integer id) { this.id = id; }


    /**
     * @return login A String with the user login name
     */ 
    public String getLogin() { return this.login; }

    /**
     * @param login A String with the user login name
     */ 
    public void setLogin(String login) { this.login = login; }


    /**
     * @return certificate A String with the certificate name
     */ 
    public String getCertificate() { return this.certificate; }

    /**
     * @param certificate A String with the certificate name
     */ 
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }


    /**
     * @return certSubject A String with the certificate subject
     */ 
    public String getCertSubject() { return this.certSubject; }

    /**
     * @param certSubject A String with the certificate subject
     */ 
    public void setCertSubject(String certSubject) {
        this.certSubject = certSubject;
    }


    /**
     * @return lastName A String with the user's last name
     */ 
    public String getLastName() { return this.lastName; }

    /**
     * @param lastName A String with the user's last name
     */ 
    public void setLastName(String lastName) { this.lastName = lastName; }


    /**
     * @return firstName A String with the user's first name
     */ 
    public String getFirstName() { return this.firstName; }

    /**
     * @param firstName A String with the user's first name
     */ 
    public void setFirstName(String firstName) { this.firstName = firstName; }


    /**
     * @return emailPrimary A String with the user's primary email address
     */ 
    public String getEmailPrimary() { return this.emailPrimary; }

    /**
     * @param emailPrimary A String with the user's primary email address
     */ 
    public void setEmailPrimary(String emailPrimary) {
        this.emailPrimary = emailPrimary;
    }


    /**
     * @return phonePrimary A String with the user's primary phone number
     */ 
    public String getPhonePrimary() { return this.phonePrimary; }

    /**
     * @param phonePrimary A String with the user's primary phone number
     */ 
    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }


    /**
     * @return password A String with the user's password
     */ 
    public String getPassword() { return this.password; }

    /**
     * @param password A String with the user's password
     */ 
    public void setPassword(String password) { this.password = password; }


    /**
     * @return description A String with a description of the user
     */ 
    public String getDescription() { return this.description; }

    /**
     * @param description A String with a description of the user
     */ 
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return emailSecondary A String with the user's secondary email address
     */ 
    public String getEmailSecondary() { return this.emailSecondary; }

    /**
     * @param emailSecondary A String with the user's secondary email address
     */ 
    public void setEmailSecondary(String emailSecondary) {
        this.emailSecondary = emailSecondary;
    }


    /**
     * @return phoneSecondary A String with the user's secondary phone #
     */ 
    public String getPhoneSecondary() { return this.phoneSecondary; }

    /**
     * @param phoneSecondary A String with the user's secondary phone #
     */ 
    public void setPhoneSecondary(String phoneSecondary) {
        this.phoneSecondary = phoneSecondary;
    }


    /**
     * @return status A String with the user's current system status
     */ 
    public String getStatus() { return this.status; }

    /**
     * @param status A String with the user's current system status
     */ 
    public void setStatus(String status) { this.status = status; }


    /**
     * @return activationKey A String, currently unused
     */ 
    public String getActivationKey() { return this.activationKey; }

    /**
     * @param activationKey A String, currently unused
     */ 
    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }


    /**
     * @return lastActiveTime A Long instance with user's active time
     */ 
    public Long getLastActiveTime() { return this.lastActiveTime; }

    /**
     * @param lastActiveTime A Long instance with user's active time
     */ 
    public void setLastActiveTime(Long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }


    /**
     * @return registerTime A Long with the user's registration time
     */ 
    public Long getRegisterTime() {
        return this.registerTime;
    }

    /**
     * @param registerTime A Long with the user's registration time
     */ 
    public void setRegisterTime(Long registerTime) {
        this.registerTime = registerTime;
    }


    /**
     * @return institution The Institution of this user
     */ 
    public Institution getInstitution() { return this.institution; }

    /**
     * @param institution Set the Institution for this user
     */ 
    public void setInstitution(Institution institution) {
        this.institution = institution;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof User) ) return false;
        User castOther = (User) other;
        return new EqualsBuilder()
            .append(this.getId(), castOther.getId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
