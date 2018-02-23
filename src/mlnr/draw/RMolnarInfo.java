/*
 * RMolnarType.java
 *
 * Created on August 4, 2006, 1:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import mlnr.type.FPointType;

/** This represents an RMolnar curve.
 * @author Robert Molnar II
 */
public class RMolnarInfo extends AbstractLineInfo {
    protected FPointType fptControlPoint1;
    protected FPointType fptControlPoint2;
       
    /** Creates a new instance of RMolnarType */
    public RMolnarInfo() {
    }

    public RMolnarInfo(FPointType fptEndPoint1, FPointType fptEndPoint2, FPointType fptControlPoint1, FPointType fptControlPoint2) {
        super(fptEndPoint1, fptEndPoint2);
        this.fptControlPoint1 = fptControlPoint1;
        this.fptControlPoint2 = fptControlPoint2;
    }
    
    public FPointType getControlPoint1() {
        return fptControlPoint1;
    }

    public void setControlPoint1(FPointType fptControlPoint1) {
        this.fptControlPoint1 = fptControlPoint1;
    }

    public FPointType getControlPoint2() {
        return fptControlPoint2;
    }

    public void setControlPoint2(FPointType fptControlPoint2) {
        this.fptControlPoint2 = fptControlPoint2;
    }
    
    public String toString() {
        return "{RMolnarInfo fptEndPoint1[" + getEndPoint1() + "] fptEndPoint2[" + getEndPoint2() + "] fptControlPoint1[" + fptControlPoint1 + "] fptControlPoint2[" + fptControlPoint2 + "]}";
    }
}
