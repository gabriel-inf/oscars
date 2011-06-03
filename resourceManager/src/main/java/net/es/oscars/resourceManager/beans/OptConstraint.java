package net.es.oscars.resourceManager.beans;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

import net.es.oscars.database.hibernate.HibernateBean;

/**
 * IdSequence is the Hibernate bean for for the rm.idsequence table.
 */
public class OptConstraint extends HibernateBean implements Serializable {
    // TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4099;

    /** persistent field */
    private Integer id;

    /** persistent field */
    private String category;
    
    /** persistent field */
    private String value;
    
    /** default constructor */
    public OptConstraint() { }

    /**
     * @return the current id of this entry
     */ 
    public Integer getId() { return this.id; }

    /**
     * @param id the is to set
     */ 
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * @param catatory String that identifies the category of an optional constraint
     */
    public void setCategory(String category){
        this.category = category;
    }
    
    /**
     * 
     * @return the category
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * 
     * @param value contains an xml string that defines the contents of the constraint
     */
    public void setValue(String value){
        this.value = value;
    }
    /**
     * 
     * @return contents of the constraint as an XML string
     */
    public String getValue() {
        return this.value;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nOptional constraint of type " + getCategory() + "\n");
        if (this.getValue() != null){
            sb.append("value is \n" + getValue() + "\n");
        }

        return sb.toString();
    }
}
