/*
 * FileMenuList.java
 *
 * Created on December 11, 2006, 7:09 PM
 *
 */

package mlnr.util.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Robert Molnar II
 */
public class FileMenuList implements ActionListener {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">  
    
    /** Accelerators for the file menu lists. */
    private static final char KEY_NUMBERS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8','9'};
    
    /** Preference key for this class to store the file list. */
    private static final String LIST_OPEN_FILE = "list_open_file";
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">  
    
    /** This is the class which is used to store the preferences. */
    private Class cPreferences;
    /** this is the number of files in the list. */
    private int numberOfOpenFiles;
    /** This is the array of menu items. */
    private JMenuItem openFilesMenu[];
    /** This is the interface used to call when the user clicks on one of the file menu items. */
    private InterfaceFileMenuList iFileMenuList;
    /** This is the menu which contains the menu items. */
    JMenu menuFile = null;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">  
    
    /** Creates a new instance of FileMenuList
     * @param cPreferences is the class to use to store the preferences.
     * @param numberOfOpenFiles is the number of open files. Cannot be more than 10 or less than 1.
     */
    public FileMenuList(Class cPreferences, InterfaceFileMenuList iFileMenuList, int numberOfOpenFiles) {
        if (numberOfOpenFiles > 10 || numberOfOpenFiles < 1)
            throw new IllegalArgumentException("number of open files must be greater than 0 and less than 11.");
        this.cPreferences = cPreferences;
        this.numberOfOpenFiles = numberOfOpenFiles;
        this.iFileMenuList = iFileMenuList;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Public Methods ">  
    
    /** @return the first file on the file menu list, can be null if no files have been saved before.
     */
    public File getFirstFile() {
        Preferences prefs = Preferences.userNodeForPackage(cPreferences);
        String firstFile = prefs.get(LIST_OPEN_FILE + "0", null);
        if (firstFile == null)
            return null;
        return new File(firstFile);
    }
    
    /** This will setup the menu file. It should be the first one and only called once.
     * @param doActionListeners true if it should add the menu action listeners, else false do
     * not add the action listeners.
     */
    public void setupFileList(JMenu menuFile) {
        this.menuFile = menuFile;
        
        // Setup the list of files to add.
        openFilesMenu = new JMenuItem[numberOfOpenFiles];
        for (int i=0; i < numberOfOpenFiles; i++) {
            openFilesMenu[i] = new JMenuItem("T", KEY_NUMBERS[i+1]);
            menuFile.add(openFilesMenu[i]);
            openFilesMenu[i].addActionListener(this);
        }
        
        // Update the menu items.
        updateFileMenuFileList();        
    }
    
    /** This will place the absolutePath at the top of the file open list. If it is
     * already there then it will bump it up to the first place.
     * @param absolutePath is the path to an opened or saved design file.
     */
    public void updateFileOpenList(String absolutePath) {
        Preferences prefs = Preferences.userNodeForPackage(cPreferences);
        
        // Get the file open list.
        String []fileOpenList = new String[numberOfOpenFiles];
        String []transfer = new String[numberOfOpenFiles];
        for (int i=0; i < numberOfOpenFiles; i++)
            fileOpenList[i] = prefs.get(LIST_OPEN_FILE + Integer.toString(i), null);
        
        // Create the new list of file opens.
        transfer[0] = absolutePath;
        int ti=1;
        for (int i=0; i < numberOfOpenFiles; i++) {
            if (fileOpenList[i] == null)
                continue;
            
            if (!fileOpenList[i].equals(absolutePath)) {
                transfer[ti] = fileOpenList[i];
                ti++;
                
                // No more.
                if (ti == numberOfOpenFiles)
                    break;
            }
        }
        
        // Now transfer the list.
        for (int i=0; i < numberOfOpenFiles; i++) {
            if (transfer[i] == null)
                prefs.remove(LIST_OPEN_FILE + Integer.toString(i));
            else
                prefs.put(LIST_OPEN_FILE + Integer.toString(i), transfer[i]);
        }
        
        // Update the menu.
        if (menuFile != null) {
            updateFileMenuFileList();
            menuFile.invalidate();
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Private Methods ">  
    
    /** This will update the file menu's file list of open files.
     */
    private void updateFileMenuFileList() {
        Preferences prefs = Preferences.userNodeForPackage(cPreferences);
        for (int i=0; i < numberOfOpenFiles; i++) {
            String absolutePath = prefs.get(LIST_OPEN_FILE + i, null);
            
            // Set the visiblity.
            if (absolutePath == null) {
                openFilesMenu[i].setVisible(false);
                continue;
            }            
            openFilesMenu[i].setVisible(true);
            
            // Get the file.
            File f = new File(absolutePath);
            openFilesMenu[i].setText(Integer.toString(i + 1) + " " + f.getName());
        }
    }

    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Interface ActionListener ">  

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        for (int i=0; i < openFilesMenu.length; i++) {
            if (obj == openFilesMenu[i]) {
                Preferences prefs = Preferences.userNodeForPackage(cPreferences);
                String absolutePath = prefs.get(LIST_OPEN_FILE + i, null);
                if (absolutePath == null) {
                    JOptionPane message = new JOptionPane();
                    message.showMessageDialog(null, "Unknown java error. Unable to open the Preferences.");
                    break;
                }

                // This will update the file menu list to place it to the top.
                updateFileOpenList(absolutePath);
                
                // Call the hookback function.
                iFileMenuList.fileMenuListOpen(new File(absolutePath));
            }
        }
    }
    
    // </editor-fold>
}
