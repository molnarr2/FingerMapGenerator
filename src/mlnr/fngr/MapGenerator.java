/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlnr.fngr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import mlnr.draw.DrawingDesign;

/**
 *
 * @author rmolnar
 */
public class MapGenerator {
    
    File fDir;
    
    public MapGenerator(File fDir) {
        this.fDir = fDir;
    }
    
    /** This will create the Obj-C file that contains the map information.
     */
    public void createMapFile() throws Exception {
        // fDir is where the Single and 2Player directories are at.
        int set = 1;
        
        // Single Player maps
        File fSingle = new File(fDir + File.separator + "Single");
        File s[] = fSingle.listFiles();        
        for (int i=0; i < s.length; i++) {
            File f = new File(fSingle.getAbsolutePath() + File.separator + s[i].getName());
            if (f.isDirectory()) {
                PrintWriter out = new PrintWriter(fDir.getAbsolutePath() + File.separator + "set_" + set + ".txt");                
                outputSet(out, f.getName(), f, false);
                out.close();
                set++;
            }
        }
        
        // 2 Player maps.
        File f2Player = new File(fDir + File.separator + "2Player");
        File s2[] = f2Player.listFiles();        
        for (int i=0; i < s.length; i++) {
            File f = new File(f2Player.getAbsolutePath() + "/" + s2[i].getName());
            if (f.isDirectory()) {
                PrintWriter out = new PrintWriter(fDir.getAbsolutePath() + File.separator + "set_" + set + ".txt");                
                outputSet(out, f.getName(), f, true);
                out.close();
                set++;
            }
        }
        
        JOptionPane.showMessageDialog(null, "Success.");       
    }
    
    /** This will output the set. It will load each file in the directory and create
     * a level for it.
     * @param setName is the name of the set.
     * @param dirPath is the path where the files are at.
     * @param b2PlayerMap is ture if 2 player map.
     */
    private void outputSet(PrintWriter out, String setName, File dirPath, boolean b2PlayerMap) throws Exception {
        File s[] = dirPath.listFiles();
        
        out.println("SETNAME=" + setName);
        out.println("UNIQUEID=" + setName.hashCode());
        
        if (b2PlayerMap)
            out.println("2PLAYERMAP=TRUE");
        else
            out.println("2PLAYERMAP=FALSE");
        
        // Process the info.txt file if exists.
        File infoTxt = new File(dirPath.getAbsolutePath() + File.separator + "info.txt");
        if (infoTxt.exists() && infoTxt.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(infoTxt));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("LINE=")) {
                        if (line.length() < 46)
                            out.println(line);                    
                        else {
                            JOptionPane.showMessageDialog(null, "Line too long: " + infoTxt.getAbsolutePath());
                        }
                    }
                            
                }             
                reader.close();
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Unable to process infoTxt: '" + infoTxt.getAbsolutePath() + "'");
            }
        }
        
        for (int i=0; i < s.length; i++) {
            try {
                // Skip all hidden files and any ones that do not end with .rxml.
                if (s[i].isHidden() || s[i].getName().endsWith(".rxml") == false)
                    continue;
                
                // Load the rxml file.
                MapLoader loader = new MapLoader();
                loader.open(s[i]);
                
                // Now process it.
                MapLevel level = new MapLevel(loader);
                level.generate();
                level.write(out, i);
                
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Unable to process set: '" + setName + "', file: '" + s[i].getName() + "'");
            }
        }        
    }
    
}
