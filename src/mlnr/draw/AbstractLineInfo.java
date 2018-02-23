/*
 * AbstractLineType.java
 *
 * Created on August 4, 2006, 2:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import mlnr.type.FPointType;

/**
 * @author Robert Molnar II
 */
public class AbstractLineInfo {
    /** First end point, must correspond to AbstractLine vEnd1.  */
    private FPointType fptEndPoint1;
    /** First end point, must correspond to AbstractLine vEnd2. */
    private FPointType fptEndPoint2;
    
    /** Creates a new instance of AbstractLineType */
    public AbstractLineInfo() {
    }
    
    public AbstractLineInfo(FPointType fptEndPoint1, FPointType fptEndPoint2) {
        this.fptEndPoint1 = fptEndPoint1;
        this.fptEndPoint2 = fptEndPoint2;
    }

    public FPointType getEndPoint1() {
        return fptEndPoint1;
    }

    public void setEndPoint1(FPointType fptEndPoint1) {
        this.fptEndPoint1 = fptEndPoint1;
    }

    public FPointType getEndPoint2() {
        return fptEndPoint2;
    }

    public void setEndPoint2(FPointType fptEndPoint2) {
        this.fptEndPoint2 = fptEndPoint2;
    }        

    /** This will get the opposite point in this AbstractLineInfo based on which point the atPoint is.
     * It performs the operation by comparing the positions of the atPoint to the positions of the end points
     * in this class.
     * @param atPoint is the point used to perform the operation.
     * @return the opposite point of the end point based on which one the 'atPoint' matches.
     * @throws IllegalArgumentException if point does not match either end point.
     */
    public FPointType getOppositePoint(FPointType atPoint) {
        if (fptEndPoint1.equals(atPoint))
            return fptEndPoint2;
        else if (fptEndPoint2.equals(atPoint))
            return fptEndPoint1;
        throw new IllegalArgumentException("Point: " + atPoint + " does not exist in this data structure.");
    }
}
