/*
 * TransformBezier.java
 *
 * Created on April 27, 2007, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.type.SFPointType;

/**
 *
 * @author Robert Molnar 2
 */
public class TransformBezier extends TransformAbstractLine {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    CubicCurve2D.Float fBezierCurr = new CubicCurve2D.Float();
    CubicCurve2D.Float fBezierPrev = new CubicCurve2D.Float();
    
    TransformVertex vControl1;
    TransformVertex vControl2;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of TransformBezier */
    
    /** Creates a new instance of Bezier */
    public TransformBezier(TransformVertex vEnd1, TransformVertex vEnd2, TransformVertex vControl1, TransformVertex vControl2) {
        super(vEnd1, vEnd2);
        
        this.vControl1 = vControl1;
        this.vControl2 = vControl2;
        
        validate();
    }   
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Implemented Abstract Methods ">
    
    public void draw(Graphics2D g2d, boolean erase) {
        if (erase)
            g2d.draw(fBezierPrev);
        g2d.draw(fBezierCurr);
    }

    Rectangle2D.Float getBounds2D() {
        return MathLineCurve.getBounds2D(fBezierCurr);
    }
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fBezierCurr, fpt);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fBezierCurr, t);
    }
      
    public AbstractLineInfo getInfo() {
        return new BezierInfo(vEnd1.getPoint(), vEnd2.getPoint(), vControl1.getPoint(), vControl2.getPoint());
    }
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fBezierCurr, 0.5f);
    }
    
    LinkedList<SFPointType> getSampledPoints(TransformVertex vFrom, TransformVertex vTo, int sampleSize) {        
        FPointType fpt1 = vFrom.getPoint();
        FPointType fpt2 = vTo.getPoint();
        float beginT = getParametericT(vFrom);
        float endT = getParametericT(vTo);
        return MathLineCurve.sampleCurve(fBezierCurr, fpt1, fpt2, beginT, endT, sampleSize);
    }
    
    public boolean intersects(Rectangle2D.Float r) {
        // Perform a simple test.
        if (fBezierCurr.getBounds2D().intersects(r) == false)
            return false;
        
        // Perform an exhaustive test.
        return MathLineCurve.intersectsBezier(fBezierCurr, r);
    }
    
    public boolean isDupliate(AbstractLineInfo abLineInfo) {
        if (abLineInfo instanceof BezierInfo == false)
            return false;
        
        BezierInfo b = (BezierInfo)abLineInfo;
        if (vEnd1.isVertexSpace(b.getEndPoint1()) && vEnd2.isVertexSpace(b.getEndPoint2()) && vControl1.getPoint().equals(b.getControlPoint1())
        && vControl2.getPoint().equals(b.getControlPoint2()))
            return true;
        if (vEnd1.isVertexSpace(b.getEndPoint2()) && vEnd2.isVertexSpace(b.getEndPoint1()) && vControl1.getPoint().equals(b.getControlPoint2())
        && vControl2.getPoint().equals(b.getControlPoint1()))
            return true;
        
        return false;
    }
    
    public float ptSegDist(FPointType fpt) {
        return MathLineCurve.closestPoint(fBezierCurr, fpt).distance(fpt);
    }
    
    public void validate() {
        // Old becomes new.
        fBezierPrev.setCurve(fBezierCurr);
        // Set new curve.
        fBezierCurr.setCurve(vEnd1.getPoint().x, vEnd1.getPoint().y, vControl1.getPoint().x, vControl1.getPoint().y,
                vControl2.getPoint().x, vControl2.getPoint().y, vEnd2.getPoint().x, vEnd2.getPoint().y);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Overridden Methods ">
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Public Methods Only To This Class ">
        
    /** This will get the corresponding control vertex to the end vertex 'tEnd'.
     * @param tEnd is an end vertex to this TransformBezier.
     * @return the corresponding control vertex to the end vertex 'tEnd'.
     * @throw IllegalArgumentException TransformBezier []: does not contain the vertex:
     */
    public TransformVertex getControlVertex(TransformVertex tEnd) {
        if (tEnd == vEnd1)
            return vControl1;
        else if (tEnd == vEnd2)
            return vControl2;
        throw new IllegalArgumentException("TransformBezier [" + this + "]: does not contain the vertex: " + tEnd);
    }
    
    /** @return the first control vertex of this bezier curve.
     */
    public TransformVertex getFirstControlVertex() {
        return vControl1;
    }
    
    /** This will get the control points for the divided bezier curve. It will calculate the two sets of control points needed for the bezier division.
     * @param fptCurvePoint is a point calculated by the getTangentPoint() function.
     * @return 4 FPointTypes where the first two are the first half of the
     * bezier curve that was divided and the last two are the last half of the
     * bezier curve that was divied. It goes like this: first control, last control,
     * first control, and then last control.
     */
    public FPointType[] getDividedControlPoints(FPointType fptCurvePoint) {
        return MathLineCurve.divideControlPoints(fBezierCurr, MathLineCurve.closestParametric(fBezierCurr, fptCurvePoint));
    }
    
    /** @return the last control vertex of this bezier curve.
     */
    public TransformVertex getLastControlVertex() {
        return vControl2;
    }

    /** This will update the control vertices with the bezierInfo information.
     * @param bezierInfo contains the updated control vertices to update with.
     */
    void update(BezierInfo bezierInfo) {
        vControl1.translateTo(bezierInfo.getControlPoint1());
        vControl2.translateTo(bezierInfo.getControlPoint2());
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public boolean equals(Object obj) {
        if (obj instanceof TransformBezier) {
            if (((TransformLine)obj).id == id)
                return true;
        } else if (obj instanceof BezierInfo) {
            BezierInfo bezier = (BezierInfo)obj;
            if (vEnd1.getPoint().equals(bezier.getEndPoint1()) &&
                vEnd2.getPoint().equals(bezier.getEndPoint2()) &&
                vControl1.getPoint().equals(bezier.getControlPoint1()) &&
                vControl2.getPoint().equals(bezier.getControlPoint2()))
                return true;
        }
        
        return false;
    }
    
    public String toString() {
        return "{TransformBezier id[" + id + "] vEnd1[" + vEnd1.getId() + "] vEnd2[" + vEnd2.getId() + "] vControl1[" + vControl1 + "] vControl2[" + vControl2 + "] }";
    }    
    
    // </editor-fold>
    
}
