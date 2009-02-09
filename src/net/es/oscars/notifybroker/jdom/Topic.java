package net.es.oscars.notifybroker.jdom;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Represents a Topic element as defined in WS-Notification
 * 
 * @author Andrew Lake (alake@internet2.edu)
 *
 */
public class Topic {
    private String dialect;
    private String expression;

    /**
     * @return the dialect
     */
    public String getDialect() {
        return this.dialect;
    }

    /**
     * @param dialect the dialect to set
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return this.expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return a JDOM representation of the Topic element
     */
    public Element getJdom() {
        Element jdom = new Element("Topic", WSNotifyConstants.WSN_NS);
        
        //Set Dialect
        if(dialect != null){
            Attribute attr = new Attribute("Dialect", this.dialect);
            jdom.setAttribute(attr);
        }
        
        //Add Topic expression
        jdom.setText(this.expression);
        return jdom;
    }
}