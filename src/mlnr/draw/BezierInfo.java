/*
 * BezierType.java
 *
 * Created on August 4, 2006, 1:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.geom.CubicCurve2D;
import mlnr.type.FPointType;

/**
 *
 * @author Robert Molnar II
 */
public class BezierInfo extends AbstractLineInfo {
    private FPointType fptControlPoint1;
    private FPointType fptControlPoint2;
    private int layerId = -1;
    
    /** Creates a new instance of BezierType */
    public BezierInfo() {
    }

    public BezierInfo(FPointType fptEndPoint1, FPointType fptEndPoint2, FPointType fptControlPoint1, FPointType fptControlPoint2) {
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
    
    /** This will set the curve fBezier to reflect this curve's points.
     * @param fBezier is the curve to set to reflect this curve's points. If null then return.
     */
    public void bezierCurve(CubicCurve2D.Float fBezier) {
        if (fBezier == null)
            return;
        
        FPointType fptEnd1 = getEndPoint1();
        FPointType fptEnd2 = getEndPoint2();
        
        fBezier.setCurve(fptEnd1.x, fptEnd1.y, fptControlPoint1.x, fptControlPoint1.y, 
                fptControlPoint2.x, fptControlPoint2.y, fptEnd2.x, fptEnd2.y);
    }
    
    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }
    
    public int getLayerId() {
        return layerId;
    }
}
