package net.es.oscars.pss.vendor.jnx;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.*;
import org.jdom.xpath.*;
import org.jdom.input.*;
import org.jdom.output.*;

import org.apache.log4j.*;
import net.es.oscars.pss.PSSException;

/**
 * TemplateHandler fills in an XML template with user supplied information.
 *
 * @author David Robertson
 */
public class TemplateHandler {

    private Logger log;

    public TemplateHandler() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Builds a document from a template where no user variables are
     * involved.
     *
     * @param fname full path of template file
     * @return doc XML Document suitable for configuring router
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public Document buildTemplate(String fname) 
            throws IOException, JDOMException, PSSException {

        this.log.info("buildTemplate.start");
        // request document building without validation
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(new File(fname));
        return doc;
    }

    /**
     * Finds and substitutes the correct values into the document.
     *
     * @param hm a hash map containing info retrieved from the reservation,
     *           and from OSCAR's configuration
     * @param hops list of hops
     * @param fname full path of template file
     * @return doc XML Document suitable for configuring router
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public Document fillTemplate(Map<String,String> hm, List<String> hops,
                                 String fname)
            throws IOException, JDOMException, PSSException {

        this.log.info("fillTemplate.start");
        // request document building without validation
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(new File(fname));
        Element root = doc.getRootElement();
        // find all user variables to replace
        XPath xpath  = XPath.newInstance("//user-var");
        List userVarList = xpath.selectNodes(doc);
        this.replaceElements(userVarList, hm);
        // special case for layer 3
        xpath = XPath.newInstance("//term[@name=\"firewall_filter_marker\"]");
        List termList = xpath.selectNodes(doc);
        this.replaceFilterNames(termList, hm);
        // fills in the primary element and the path-list element
        this.handlePathElements(doc, hm, hops);
        // remove comments before sending to router
        this.removeComments(doc);
        this.log.info("fillTemplate.finish");
        return doc;
    }

    /**
     * Replaces the contents of the listed elements if there is a
     * corresponding entry in the hash map; otherwise, delete that
     * element.
     *
     * @param list a List of elements
     * @param hm a hash map of name value pairs
     * @throws PSSException
     */
    private void replaceElements(List list, Map<String,String> hm)
            throws PSSException {

        Parent parent = null;
        Map<Element,String> parentMap = new HashMap<Element,String>();

        for (Iterator i = list.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            Element parentElement = e.getParentElement();
            parentMap.put(parentElement, null);
        }
        // Note that order of parent elements is not important.
        // TODO:  more error-checking.
        for (Element p: parentMap.keySet()) {
            StringBuilder sb = new StringBuilder();
            List children = p.getChildren();
            Attribute a = p.getAttribute("optional");
            // replace all text of user-var's, add text of all txt's
            for (Iterator c = children.iterator(); c.hasNext();) {
                Element child = (Element) c.next();
                String txt = child.getText();
                if (child.getName().equals("txt")) {
                    sb.append(txt);
                } else if (hm.containsKey(txt)) {
                    sb.append(hm.get(txt));
                } else {
                    // TODO not a fatal error if user-var not present; sometimes
                    // it should be.  Needs some work.
                    if ((a != null) && (!a.getValue().equals("yes"))) {
                        throw new PSSException("required user var: " + txt +
                                               " is not present");
                    }
                }
            }
            if (sb.length() == 0) {
                // Remove the parent element.
                p.detach();
            } else {
                // NOTE WELL:  this overwrites all current children of parent.
                // Detaching a child explicitly, results in an exception.
                p.setText(sb.toString());
                p.removeAttribute("optional");
            }
        }
    }

    /**
     * Replaces the name attribute of the listed elements with the
     * corresponding entry in the hash map.  If the hash map doesn't
     * contain the corresponding entry, throw an exception.
     *
     * @param list a List of elements
     * @param hm a hash map of name value pairs
     * @throws PSSException
     */
    private void replaceFilterNames(List list, Map<String,String> hm)
            throws PSSException {

        String replacementVal = null;

        for (Iterator i = list.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            Attribute a = e.getAttribute("name");
            if (a != null) {
                replacementVal = a.getValue();
                if (!hm.containsKey(replacementVal)) {
                    throw new PSSException(
                        "No value supplied for firewall_filter_marker");
                } else {
                    a.setValue(hm.get(replacementVal));
                }
            }
        }
    }

    /**
     * Handles path elements in template.  A noop with teardown
     * template (no elements present).
     *
     * @throws JDOMException
     * @throws PSSException
     */
    public void handlePathElements(Document doc, Map<String,String> hm,
                                   List<String> hops)
           throws JDOMException, PSSException {

        XPath xpath = XPath.newInstance("//primary");
        List elements = xpath.selectNodes(doc);
        // if no elements with this name, using teardown template
        if (elements.size() == 0) {
            return;
        }
        Element primaryElement = (Element) elements.get(0);
        xpath = XPath.newInstance("//path");
        elements = xpath.selectNodes(doc);
        if (elements.size() != 1) {
            throw new PSSException(
                    "Must be exactly one path element in template");
        }
        Element pathElement = (Element) elements.get(0);
        if (hops == null) {
            primaryElement.detach();
            pathElement.detach();
        } else {
            primaryElement.removeAttribute("optional");
            pathElement.removeAttribute("optional");
            // for each layer 2 hop, add
            // <path-list><name>hop</name><strict /></path-list>
            //     under the path element
            // for each layer 3 hop, add
            // <path-list><name>hop</name></path-list> under the path element
            for (String hop: hops) {
                Element pathList = new Element("path-list");
                Element name = new Element("name");
                name.setText(hop);
                pathList.addContent(name);
                // if layer 2
                if (hm.containsKey("vlan_id")) {
                    Element strictElem = new Element("strict");
                    pathList.addContent(strictElem);
                }
                pathElement.addContent(pathList);
            }
        }
    }

    public void removeComments(Document doc) throws JDOMException {
        XPath xpath = XPath.newInstance("//comment()");
        List comments = xpath.selectNodes(doc);
        for (Iterator i = comments.iterator(); i.hasNext();) {
            Comment c = (Comment) i.next();
            c.detach();
        }
    }

    // convenience functions from
    // http://javaboutique.internet.com/tutorials/jdom&/index-2.html
    private void listElements(List es, String indent) {
        for (Iterator i = es.iterator(); i.hasNext();) {
            Element e = (Element) i.next();
            listElement(e, indent);
        }
    }

    private void listElement(Element e, String indent) {
        this.log.info(indent + "*Element, name:" +
                           e.getName() + ", text:" +
                           e.getText().trim());

        // List all attributes
        List as = e.getAttributes();
        listAttributes(as, indent + " ");
        // List all children
        List c = e.getChildren();
        listElements(c, indent + " ");
    }

    private void listAttributes(List as, String indent) {
        for (Iterator i = as.iterator(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            this.log.info(indent + "*Attribute, name:" +
                                   a.getName() + ", value:" +
                                   a.getValue());
        }
    }
}
