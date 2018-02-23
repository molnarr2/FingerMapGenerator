/*
 * Version.java
 *
 * Created on March 17, 2006, 2:50 PM
 *
 * The package mlnr.embd should only be used for this Embroidery Draw program. This
 * package should never exist in another program since it is used to store program
 * and version specific information.
 */

package mlnr.embd;

import mlnr.embd.finger.Version_Generator;
import mlnr.embd.v1.*;
import mlnr.embd.v2.Version_v2;

/**
 *
 * @author Robert Molnar II
 */
public class Version {
    
    /** This must always be updated when a new release is brought forth to the public.
     */
    private static final String CURRENT_VERSION = "1.0.0";
    
    /** Creates a new instance of Version */
    public Version() {
    }
    
    /** This is the major version of this release.
     * @return the current version of this software. This must be updated everything a release is published for the
     * public.
     */
    static public String getCurrentVersion() {
        return CURRENT_VERSION;
    }
    
    /** This will get the current version used in this build. This is done so that when
     * version 2,3,4,etc.. comes then there will not be any conflicts with data stored in
     * the Windows Registry. Version 1,2,3, etc.. will use different places to store there
     * data.
     * @return the class that represents the current version in this build. 
     */
    static public Class getVersion() {
        return Version_Generator.class;
        
    }    
}
