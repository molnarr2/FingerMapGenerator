/*
 * InterfaceFileMenuList.java
 *
 * Created on December 11, 2006, 7:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.util.gui;

import java.io.File;

/**
 *
 * @author Robert Molnar II
 */
public interface InterfaceFileMenuList {
    /** This is called by the FileMenuList class when the user clicks on a menu item to open.
     * @param f is the file to open up.
     */
    public void fileMenuListOpen(File f);
}
