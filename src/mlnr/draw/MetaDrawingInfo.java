/*
 * MetaDrawingInfo.java
 *
 * Created on November 11, 2006, 2:10 PM
 *
 */

package mlnr.draw;

import java.io.PrintWriter;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import mlnr.util.InterfaceSettings;
import mlnr.util.XmlUtil;
import org.w3c.dom.*;

/** This contains user entered information about the drawing.
 * @author Robert Molnar II
 */
public class MetaDrawingInfo {
    
    // <editor-fold defaultstate="collapsed" desc=" User-Setting Fields ">
    
    /** This is the author's name. */
    static String us_authorName = "";
    
    /** This is the web site which the file possibly came from or is the author's web site. */
    static String us_website = "";
    
    /** This is the author's email address. */
    static String us_emailAddress = "";
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Field ">
    
    /** This is the author's name. */
    private String authorName = us_authorName;
    /** This is the web site which the file possibly came from or is the author's web site. */
    private String website = us_website;
    /** This is the author's email address. */
    private String emailAddress = us_emailAddress;
    /** This is the description of the file. */
    private String description = "";
    /** This is the name of the set which this drawing belongs to. */
    private String setName = "";
    /** This is the category which the drawing belongs in. */
    private String category = "";
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Static ">
    
    static {
        new MetaDrawingInfoSettings().load();
    }

    // </editor-fold>
    
    /** Creates a new instance of MetaDrawingInfo 
     * This is used for the author's creation of a new drawing.
     */
    public MetaDrawingInfo() {
        
    }

    /** This will set all strings to "".
     * @param MetaDrawingInfo with all strings to "".
     */
    public static MetaDrawingInfo createEmpty() {
        MetaDrawingInfo meta = new MetaDrawingInfo();
        meta.authorName = "";
        meta.website = "";
        meta.emailAddress = "";        
        return meta;
    }

    /** This will bring up a dialog box to edit the meta drawing information.
     */
    public void edit(JFrame frame, String fileName) {
//        DialogEditMetaInfo dialog = new DialogEditMetaInfo(frame, true, this, fileName);
//        dialog.setVisible(true);
    }
    
    /** This will save the default values.
     */
    public void saveDefault() {
        us_authorName = authorName;
        us_emailAddress = emailAddress;
        us_website = website;
        new MetaDrawingInfoSettings().save();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Support ">
    
    /** This will load the version 2.0 of RXML file.
     * @param eDrawing is the element for the metaDrawingInfo in the RXML file.
     */
    public void loadVersion20(Element eDrawing) throws Exception {
        // Get the author information.
        Element eAuthor = XmlUtil.getElementByTagName(eDrawing, "author");
        authorName = XmlUtil.getAttributeString(eAuthor, "name");
        website = XmlUtil.getAttributeString(eAuthor, "website");
        emailAddress = XmlUtil.getAttributeString(eAuthor, "email");
        
        // Get the description.
        Element eDescription = XmlUtil.getElementByTagName(eDrawing, "description");
        description = XmlUtil.getAttributeString(eDescription, "value");
        
        // Get the set name.
        Element eSet = XmlUtil.getElementByTagName(eDrawing, "set");
        setName = XmlUtil.getAttributeString(eSet, "value");
        
        // Get the categories.
        Element eCategory = XmlUtil.getElementByTagName(eDrawing, "category");
        setCategory(XmlUtil.getAttributeString(eCategory, "value"));
    }
    
    /** This will write out the Design in xml file format.
     */
    public void write(PrintWriter out) {
        out.println("  <metaDrawingInfo>");
        out.println("   <author name=\"" + XmlUtil.fixup(authorName) + "\" website=\"" + XmlUtil.fixup(website) + "\" email=\"" + XmlUtil.fixup(emailAddress) + "\" />");
        out.println("   <description value=\"" + description + "\" />");        
        out.println("   <set value=\"" + setName + "\" />");
        out.println("   <category value=\"" + getCategory() + "\" />");        
        out.println("  </metaDrawingInfo>");
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Set/Get Methods ">
        
    public String getAuthorName() {
        return authorName;
    }
    
    /** @param unknown is true then it should return "Unknown" if no author.
     * @return author.
     */
    public String getAuthorName(boolean unknown) {
        if ("".equals(authorName))
            return "Unknown";
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getWebsite() {
        return website;
    }
    
    /** @param unknown is true then it should return "Unknown" if no web site.
     * @return web site.
     */
    public String getWebsite(boolean unknown) {
        if ("".equals(website))
            return "Unknown";
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
    
    /** @param unknown is true then it should return "Unknown" if no email address.
     * @return email address.
     */
    public String getEmailAddress(boolean unknown) {
        if ("".equals(emailAddress))
            return "Unknown";
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDescription() {
        return description;
    }
    
    /** @param unknown is true then it should return "Unknown" if no description.
     * @return description.
     */
    public String getDescription(boolean unknown) {
        if ("".equals(description))
            return "Unknown";
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSetName() {
        return setName;
    }
    
    /** @param unknown is true then it should return "Unknown" if no set Name.
     * @return set Name.
     */
    public String getSetName(boolean unknown) {
        if ("".equals(setName))
            return "Unknown";
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
    }

    public String getCategory() {
        return category;
    }
    
    /** @param unknown is true then it should return "Unknown" if no category.
     * @return category.
     */
    public String getCategory(boolean unknown) {
        if ("".equals(category))
            return "Unknown";
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    // </editor-fold>
}

// <editor-fold defaultstate="collapsed" desc=" class MetaDrawingInfoSettings ">

/** This will save the drawing information settings
 */
class MetaDrawingInfoSettings implements InterfaceSettings {
    static private String AUTHOR_NAME = "metadrawing_author";  // String
    static private String WEBSITE_NAME = "metadrawing_website"; // String
    static private String EMAIL_ADDRESS_NAME = "metadrawing_emailAddress"; // String

    public MetaDrawingInfoSettings() {        
    }
    
    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        prefs.put(AUTHOR_NAME, MetaDrawingInfo.us_authorName);
        prefs.put(EMAIL_ADDRESS_NAME, MetaDrawingInfo.us_emailAddress);
        prefs.put(WEBSITE_NAME, MetaDrawingInfo.us_website);
    }
    
    public void load() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        MetaDrawingInfo.us_authorName = prefs.get(AUTHOR_NAME, MetaDrawingInfo.us_authorName);
        MetaDrawingInfo.us_emailAddress = prefs.get(EMAIL_ADDRESS_NAME, MetaDrawingInfo.us_emailAddress);
        MetaDrawingInfo.us_website= prefs.get(WEBSITE_NAME, MetaDrawingInfo.us_website);
    }
}

// </editor-fold>
