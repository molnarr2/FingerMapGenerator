/*
 * InterfaceSettings.java
 *
 * Created on January 10, 2006, 4:32 PM
 *
 */

package mlnr.util;

/** Interface used to save settings.
 * @author Robert Molnar
 */
public interface InterfaceSettings {
    /** This will save the settings from the class.
     */
    public void save();
    
    /** Assumed that this will be called only once per program created.
     * This will load the settings. If the settings don't exist then use the classes default settings.
     */
    public void load();
}