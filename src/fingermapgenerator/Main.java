/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fingermapgenerator;

import javax.swing.JOptionPane;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import mlnr.fngr.MapGenerator;

/**
 *
 * @author rmolnar
 */
public class Main {

    public static final String PREF_STRING1 = "39821FingerMAPGeneratorPREF_STRING1";
    public static final String PREF_STRING2 = "39821FingerMAPGeneratorPREF_STRING2";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        try {
            // Load the location where the install directory is.
            Preferences prefs = Preferences.userNodeForPackage(Main.class);
            String installedPath = prefs.get(PREF_STRING1, System.getProperty("user.home"));

            // Get the directory where the install files are at.
            JOptionPane.showMessageDialog(null, "Choose directory that contains the map files:");
            JFileChooser fc = new JFileChooser(installedPath);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int retVal = fc.showOpenDialog(null);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;
            File fInstall = fc.getSelectedFile();   // This is where the maps are located at.
            prefs.put(PREF_STRING1, fInstall.getAbsolutePath());
            
            // Now run the map generator and output the maps to the file.
            MapGenerator generator = new MapGenerator(fInstall);
            generator.createMapFile();
        
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "ERROR... see console output.");
        }
        
    }

}
