package net.es.oscars.aaa;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class UserAttributePK implements Serializable {

    /** identifier field */
    private Integer UserId;

    /** identifier field */
    private Integer AttributeId;

    /** full constructor */
    public UserAttributePK(Integer UserId, Integer AttributeId) {
        this.UserId = UserId;
        this.AttributeId = AttributeId;
    }

    /** default constructor */
    public UserAttributePK() {
    }


    public Integer getUserId() {
        return this.UserId;
    }

    public void setUserId(Integer UserId) {
        this.UserId = UserId;
    }


    public Integer getAttributeId() {
        return this.AttributeId;
    }

    public void setAttributeId(Integer AttributeId) {
        this.AttributeId = AttributeId;
    }


    public String toString() {
        return new ToStringBuilder(this)
            .append("UserId", getUserId())
            .append("AttributeId", getAttributeId())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof UserAttributePK) ) return false;
        UserAttributePK castOther = (UserAttributePK) other;
        return new EqualsBuilder()
            .append(this.getUserId(), castOther.getUserId())
            .append(this.getAttributeId(), castOther.getAttributeId())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getUserId())
            .append(getAttributeId())
            .toHashCode();
    }

}
