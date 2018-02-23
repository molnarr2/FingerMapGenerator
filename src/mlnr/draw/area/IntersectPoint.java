/*
 * IntersectPoint.java
 * 
 * Created on Oct 22, 2007, 3:30:39 PM
 * 
 */

package mlnr.draw.area;

import mlnr.draw.AbstractLine;

/** This class is used to represent a single intersection between two AbstractLines.
 * @author Rob
 */
public class IntersectPoint {
    /** This is the line which the intersection is on. */
    private AbstractLine sourceLine;
    /** Parametric position on the sourceLine where the intersection happened. */
    private float sourceParametric;
    /** This is the line which intersected the source line. */
    private AbstractLine intersectLine;
    /** Parameteric position on the intersectLine where the intersection happened. */
    private float intersectParametric;

    IntersectPoint() {
        
    }
}
