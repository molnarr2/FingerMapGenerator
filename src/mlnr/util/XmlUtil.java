/*
 * XmlUtil.java
 *
 * Created on August 3, 2005, 9:39 PM
 *
 */

package mlnr.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author Robert Molnar II
 */
public class XmlUtil {
    
    /** Creates a new instance of XmlUtil */
    private XmlUtil() {
    }
    
    /** This will get the root element of the xml file.
     * @param f is the xml file to load.
     */
    public static Element getRoot(File fXml) throws Exception {
        // Start the xml parsing.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(new FileInputStream(fXml));
        
        // This should be the <rxml> element.
        return doc.getDocumentElement();
    }
    
    /** This will make the string XML worthy.
     * @param s is the string in question.
     * @return a string which will change in special characters to XML escape characters.
     */
    public static String fixup(String s) {
        StringBuffer sb = new StringBuffer();
        int len = s.length();
        for(int i = 0; i < len; i++) {
            char c = s.charAt(i);
            switch(c) {
                default: sb.append(c); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
            }
        }
        return sb.toString();
    }
    
    /** This will get the attribute as a double from the element.
     * @param elem is the element to search for the attributes.
     * @param attributeName is the attribute to be searched for.
     * @return double value of the attributeName from the elem.
     * @exception Element [elem.getNodeName()] unable to convert [attributeName] to a double.
     * @exception Element [elem.getNodeName()] does not have attribute [attributeName].
     */
    public static double getAttributeDouble(Node elem, String attributeName) throws Exception {
        NamedNodeMap attributes = elem.getAttributes();
        int max = attributes.getLength();
        for (int i=0; i < max; i++) {
            if (attributes.item(i).getNodeName().equals(attributeName)) {
                try {
                    return Double.parseDouble(attributes.item(i).getNodeValue());
                } catch (NumberFormatException e) {
                    throw new Exception("Element [" + elem.getNodeName() + "] unable to convert attribute [" + attributeName + "] to a double.");
                }
            }
        }
        
        throw new Exception("Element [" + elem.getNodeName() + "] does not have attribute [" + attributeName + "].");
    }
    
    /** This will get the attribute as an integer from the element.
     * @param elem is the element to search for the attributes.
     * @param attributeName is the attribute to be searched for.
     * @return integer value of the attributeName from the elem.
     * @exception Element [elem.getNodeName()] unable to convert [attributeName] to an integer.
     * @exception Element [elem.getNodeName()] does not have attribute [attributeName].
     */
    public static int getAttributeInteger(Node elem, String attributeName) throws Exception {
        NamedNodeMap attributes = elem.getAttributes();
        int max = attributes.getLength();
        for (int i=0; i < max; i++) {
            if (attributes.item(i).getNodeName().equals(attributeName)) {
                try {
                    return Integer.parseInt(attributes.item(i).getNodeValue());
                } catch (NumberFormatException e) {
                    throw new Exception("Element [" + elem.getNodeName() + "] unable to convert attribute [" + attributeName + "] to an integer.");
                }
            }
        }
        
        throw new Exception("Element [" + elem.getNodeName() + "] does not have attribute [" + attributeName + "].");
    }
    
    /** This will get the attribute as an String from the element.
     * @param elem is the element to search for the attributes.
     * @param attributeName is the attribute to be searched for.
     * @return String value of the attribute.
     * @exception Element [elem.getNodeName()] unable to convert [attributeName] to an integer.
     * @exception Element [elem.getNodeName()] does not have attribute [attributeName].
     */
    public static String getAttributeString(Node elem, String attributeName) throws Exception {
        NamedNodeMap attributes = elem.getAttributes();
        int max = attributes.getLength();
        for (int i=0; i < max; i++) {
            if (attributes.item(i).getNodeName().equals(attributeName)) {
                return attributes.item(i).getNodeValue();
            }
        }
        
        throw new Exception("Element [" + elem.getNodeName() + "] does not have attribute [" + attributeName + "].");
    }
    
    /** This will verify the size of the attributes of the element.
     * @param elem is the element to verify the number of the attributes.
     * @param size is the number of attributes to verify.
     * @exception Element [elem.getNodeName()] must have [size] attributes, instead it has [max] attributes.
     */
    public static void verifySizeAttributes(Node elem, int size) throws Exception {
        NamedNodeMap attributes = elem.getAttributes();
        int max = attributes.getLength();
        if (max != size)
            throw new Exception("Element [" + elem.getNodeName() + "] must have [" + size + "] attributes, instead it has [" + max + "] attributes.");
    }
    
    /** This will get the single tag below the parent tag using the tagName.
     * @param parent is the parent tag element.
     * @param tagName is the name of the tag to get. There should be no duplicate tag's with that name under the parent.
     * @return the tag which has the tagName under the parent tag.
     * @exception Element contains more than one tag under the parent with the same tag name.
     * @exception Element does not have the tag.
     */
    public static org.w3c.dom.Element getElementByTagName(org.w3c.dom.Element parent, String tagName) throws Exception {
        NodeList nList = parent.getElementsByTagName(tagName);
        int length = nList.getLength();
        if (length == 0)
            throw new Exception("Element [" + parent.getNodeName() + "] does not have the tag [" + tagName + "].");
        else if (length > 1)
            throw new Exception("Element [" + parent.getNodeName() + "] contains more than one tag under the parent with the same tag name [" + tagName + "].");
        
        return (org.w3c.dom.Element)nList.item(0);
    }
    
    /**
     * @param parent is the parent tag element.
     * @param tagName is the name of the tag to see if it exists.
     * @return true if it exists else false..
     */
    public static boolean exists(org.w3c.dom.Element parent, String tagName) throws Exception {
        if (parent.getElementsByTagName(tagName).getLength() > 0)
            return true;
        return false;
    }
    
    /** This will get all children elements under the parent.
     * @param parent is the element which is used to search for children elements.
     * @return a LinkedList of children elements.
     */
    public static LinkedList<org.w3c.dom.Element> getChildrenElements(org.w3c.dom.Element parent) throws Exception {
        LinkedList<org.w3c.dom.Element> lt = new LinkedList();
        
        NodeList children = parent.getChildNodes();
        int childrenSize = children.getLength();
        for (int i=0; i < childrenSize; i++) {
            Node curNode = children.item(i);
            
            // No white space.
            if ("#text".equals(curNode.getNodeName()))
                continue;
            
            // Must be an element, else not in rxml file format.
            if (curNode instanceof Element)
                lt.add((org.w3c.dom.Element)curNode);
        }
        
        return lt;
    }
    
}
