/*
 * FileNameFilter.java
 *
 * Created on May 8, 2006, 2:34 PM
 *
 * This is a generic file name filter.
 */

package mlnr.util.gui;

import java.util.*;
import java.io.*;

/**
 *
 * @author Robert Molnar II
 */
public class FileNameFilter extends javax.swing.filechooser.FileFilter  implements java.io.FileFilter {
    LinkedList ltExtensions = new LinkedList();    
    boolean allowDirs = true;
    
    /** This will create a new filter. Dirs will default pass through.
     * @param filterExtension is the extension that will be accepted. Must be without the dot, such as "bmp".
     * Case doesn't matter for the extension when adding.
     * @param desciption of the filter.
     */
    public FileNameFilter(String filterExtension, String description) {
        addFilterExtension(filterExtension, description);
    }
    
    /** This will add a filter extension to the list of files accepted.
     * @param filterExtension is the extension that will be accepted. Can contain the dot at the start of
     * the extension or not. Case doesn't matter for the extension when adding.
     * @param desciption of the filter.
     */
    public void addFilterExtension(String filterExtension, String description) {        
        if (filterExtension == null || filterExtension.length() == 0)
            throw new IllegalArgumentException ("filterExtension[" + filterExtension + "] is null or is empty.");
        if (filterExtension.charAt(0) == '.')
            filterExtension = filterExtension.substring(1, filterExtension.length());
        
        ltExtensions.add(new FilterNameDescription(filterExtension.toLowerCase(), description));
    }
    
    /** This will make sure that the file has the extension to it. Can only be used if
     * there is one extension in this filter.
     * @param fileName will make sure that the extension is on the file name.
     * @return The fileName with the extension appended on it if it is not already there.
     */
    public String addExtension(String fileName) {
        if (ltExtensions.size() != 1)
            throw new IllegalArgumentException("This filter has more than one extension and cannot add extension.");
        
        // Get the extension.
        String extension = ((FilterNameDescription)ltExtensions.getFirst()).filterExtension;
        
        // See if the fileName ends with the filter extension.
        String name = fileName.toLowerCase();
        if (name.endsWith(extension))
            return fileName;
        
        return fileName + "." + extension;
    }
    
    public boolean accept(File f) {
        if (f.isDirectory())
            return allowDirs;
        
        // Get the name of the file in lowercase since the extensions are in lowercase.
        String fileName = f.getName().toLowerCase();
        
        for (Iterator itr = ltExtensions.iterator(); itr.hasNext(); ) {
            FilterNameDescription filterName = (FilterNameDescription)itr.next();
            
            // Since file ends with that extension.
            if (fileName.endsWith(filterName.filterExtension))
                return true;
        }
        
        return false;
    }
    
    public String getDescription() {
        StringBuffer sbuf = new StringBuffer(80);
        
        // Get the first one.
        Iterator itr = ltExtensions.iterator();         
        FilterNameDescription filterName = (FilterNameDescription)itr.next();
        sbuf.append(filterName.description);
        
        // Get the next ones.
        for (; itr.hasNext(); ) {
            filterName = (FilterNameDescription)itr.next();
            sbuf.append(" & " + filterName.description);
        }
        
        return sbuf.toString();
    }
    
    /** @param allowDirs is true if it should allow dirs in the filter else false no dirs are allowed.
     */
    public void setAllowDirs(boolean allowDirs) {
        this.allowDirs = allowDirs;
    }
    
    
    class FilterNameDescription {
        String filterExtension;
        String description;
        
        FilterNameDescription(String filterExtension, String description) {
            this.filterExtension = filterExtension;
            this.description = description;
        }
    }    
}