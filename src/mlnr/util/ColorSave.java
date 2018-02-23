/*
 * ColorSave.java
 *
 * Created on January 10, 2006, 4:28 PM
 *
 */

package mlnr.util;
    
import java.awt.*;
import java.util.prefs.*;

/** Util class for saving/loading Color object.
 */
public class ColorSave {
    
    /** This will save the color.
     * @param prefs is the Preferences for the key.
     * @param key is the name of the key which it is to be stored under. This will add "r", "g", "b", "a" for each
     * component of the color.
     * @param c is the color which needs to be stored.
     */
    static public void saveColor(Preferences prefs, String key, Color c) {
        prefs.putInt(key + "r", c.getRed());
        prefs.putInt(key + "g", c.getGreen());
        prefs.putInt(key + "b", c.getBlue());
        prefs.putInt(key + "a", c.getAlpha());
    }
    
    /** This will load the color.
     * @param prefs is the Preferences for the key.
     * @param key is the key which contains the color.
     * @param def is the color returned if the key does not exist.
     * @return the color from the key location, else 'def'.
     */
    static public Color loadColor(Preferences prefs, String key, Color def) {
        int red = prefs.getInt(key + "r", -1);
        int green = prefs.getInt(key + "g", -1);
        int blue = prefs.getInt(key + "b", -1);
        int alpha = prefs.getInt(key + "a", -1);
        
        if (red != -1 && green != -1 && blue != -1 && alpha != -1)
            return new Color(red, green, blue, alpha);
        return def;
    }
}