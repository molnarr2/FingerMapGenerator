/*
 * TransformRMolnar.java
 *
 * Created on April 27, 2007, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.gui.geom.RMolnarCubicCurve2D;
import mlnr.type.FPointType;
import mlnr.type.SFPointType;

/**
 *
 * @author Robert Molnar 2
 */
public class TransformRMolnar extends TransformAbstractLine {
     
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    // Control points for the RMolnar.
    TransformVertex vControl1;
    TransformVertex vControl2;
    
    RMolnarCubicCurve2D fRMolnarCurr = new RMolnarCubicCurve2D();
    RMolnarCubicCurve2D fRMolnarPrev = new RMolnarCubicCurve2D();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of RMolnar */
    public TransformRMolnar(TransformVertex vEnd1, TransformVertex vEnd2, TransformVertex vControl1, TransformVertex vControl2) {
        super(vEnd1, vEnd2);
        this.vControl1 = vControl1;
        this.vControl2 = vControl2;
        
        validate();
    }
    
    // </editor-fold>
     
    // <editor-fold defaultstate="collapsed" desc=" Implemented Abstract Methods ">
    
    public void draw(Graphics2D g2d, boolean erase) {
        if (erase)
            g2d.draw(fRMolnarPrev);
        g2d.draw(fRMolnarCurr);
    }

    Rectangle2D.Float getBounds2D() {
        return MathLineCurve.getBounds2D(fRMolnarCurr);
    }
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fRMolnarCurr, fpt);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fRMolnarCurr, t);
    }
      
    public AbstractLineInfo getInfo() {
        return new RMolnarInfo(vEnd1.getPoint(), vEnd2.getPoint(), vControl1.getPoint(), vControl2.getPoint());
    }
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fRMolnarCurr, 0.5f);
    }
    
    LinkedList<SFPointType> getSampledPoints(TransformVertex vFrom, TransformVertex vTo, int sampleSize) {
        FPointType fpt1 = vFrom.getPoint();
        FPointType fpt2 = vTo.getPoint();
        float beginT = getParametericT(vFrom);
        float endT = getParametericT(vTo);
        return MathLineCurve.sampleCurve(fRMolnarCurr, fpt1, fpt2, beginT, endT, sampleSize);
    }
    
    public boolean intersects(Rectangle2D.Float r) {
        // Perform a simple test.
        if (fRMolnarCurr.getBounds2D().intersects(r) == false)
            return false;
        
        // Perform an exhaustive test.
        return MathLineCurve.intersectsBezier(fRMolnarCurr, r);
    }
    
    public boolean isDupliate(AbstractLineInfo abLineInfo) {
        if (abLineInfo instanceof RMolnarInfo == false)
            return false;
        
        RMolnarInfo iRmolnar = (RMolnarInfo)abLineInfo;
        if (vEnd1.isVertexSpace(iRmolnar.getEndPoint1()) && vEnd2.isVertexSpace(iRmolnar.getEndPoint2()) && vControl1.isVertexSpace(iRmolnar.getControlPoint1()) && vControl2.isVertexSpace(iRmolnar.getControlPoint2()))
            return true;
        if (vEnd2.isVertexSpace(iRmolnar.getEndPoint1()) && vEnd1.isVertexSpace(iRmolnar.getEndPoint2()) && vControl2.isVertexSpace(iRmolnar.getControlPoint1()) && vControl1.isVertexSpace(iRmolnar.getControlPoint2()))
            return true;
        return false;
    }
   
    public float ptSegDist(FPointType fpt) {
        return MathLineCurve.closestPoint(fRMolnarCurr, fpt).distance(fpt);
    }
    
    public void validate() {                        
        // Old becomes new.
        fRMolnarPrev.setCurve(fRMolnarCurr);
        // Set new curve.        
        fRMolnarCurr.setCurve(vEnd1.getPoint(), vEnd2.getPoint(), vControl1.getPoint(), vControl2.getPoint());
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Public Methods Only To This Class ">
        
    /** This will get the corresponding control vertex to the end vertex 'tEnd'.
     * @param tEnd is an end vertex to this TransformRMolnar.
     * @return the corresponding control vertex to the end vertex 'tEnd'.
     * @throw IllegalArgumentException TransformRMolnar []: does not contain the vertex:
     */
    public TransformVertex getControlVertex(TransformVertex tEnd) {
        if (tEnd == vEnd1)
            return vControl1;
        else if (tEnd == vEnd2)
            return vControl2;
        throw new IllegalArgumentException("TransformRMolnar [" + this + "]: does not contain the vertex: " + tEnd);
    }
    
    /** This will get the end vertex for the tControl vertex. The tControl must be a control vertex of this RMolnar.
     * @param tControl must be a control vertex of this RMolnar. It is used to get the corresponding end vertex.
     * @return the corresponding end vertex to the control vertex 'tControl'.
     * @throw IllegalArgumentException TransformRMolnar []: does not contain the vertex as control vertex.
     */
    public TransformVertex getEndVertex(TransformVertex tControl) {
        if (tControl == vControl1)
            return vEnd1;
        else if (tControl == vControl2)
            return vEnd2;
        throw new IllegalArgumentException("TransformRMolnar [" + this + "]: does not contain the vertex: " + tControl + " as a control vertex.");
    }
    
    /** @return the first control vertex.
     */
    public TransformVertex getFirstControlVertex() {
        return vControl1;
    }
    
    /** @return the last control vertex.
     */
    public TransformVertex getLastControlVertex() {
        return vControl2;
    }
    
    /** This will see if 'vControl' is a control vertex. Will match based on memory location, therefore it
     * MUST be the exact control vertex.
     * @param vControl is the vertex to see if it is a control vertex.
     * @return true if it is a control vertex in this RMolnar, else false is not a control vertex in this RMolnar.
     */
    boolean isControlVertex(TransformVertex vControl) {
        if (vControl == vControl1)
            return true;
        else if (vControl == vControl2)
            return true;
        return false;
    }
    
    /** @param vControl is the vertex to see if it is the first control vertex.
     * @return true if it is the first control vertex.
     */
    boolean isFirstControlVertex(TransformVertex vControl) {
        if (vControl == vControl1)
            return true;
        return false;
    }
    
    /** This will make the curve at the control point as an end curve.
     * @param vControl is the control point to set it to it's curve end point.
     */       
    void makeIntoEndCurve(TransformVertex vControl) {
        if (vControl == vControl1)
            vControl1 = vEnd1;
        else if (vControl == vControl2)
            vControl2 = vEnd2;
        else
            throw new IllegalArgumentException("This TransformRMolnar[" + this + "] does not have the control point: " + vControl + ".");
    }    
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Filter Methods For Lists ">
    
    /** This will produce a list of TransformRMolnar from the list of TransformAbstractLine. It will only pick the ones that
     * are TransformRMolnar curves. 
     * @param ltLines is the list of TransformAbstractLine that will be filtered.
     * @return a list of TransformRMolnar from the list of TransformAbstractLine. Can be empty if none exist.
     */
    public static final LinkedList<TransformRMolnar> filterRMolnar(LinkedList<TransformAbstractLine> ltLines) {
        LinkedList<TransformRMolnar> ltRMolnars = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = ltLines.iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            if (tLine instanceof TransformRMolnar)
                ltRMolnars.add((TransformRMolnar)tLine);
        }
        
        return ltRMolnars;
    }
    
    /** This will produce a list of TransformRMolnar from the list of TransformAbstractLine. For a TransformRMolnar to
     * be placed in the list, it must be an end curve at the tVertex (end vertex == end control). 
     * @param ltLines is the list of TransformAbstractLine that will be filtered.
     * @param tVertex is the vertex used to test to see if the TransformRMolnar curves are end curves at that point.
     * @return a list of TransformRMolnar from the list of TransformAbstractLine. For a TransformRMolnar to
     * be placed in the list, it must be an end curve at the tVertex (end vertex == end control). 
     */
    public static final LinkedList<TransformRMolnar> filterEndAt(LinkedList<TransformAbstractLine> ltLines, TransformVertex tVertex) {
        LinkedList<TransformRMolnar> ltRMolnars = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = ltLines.iterator(); itr.hasNext(); ) {
            // Get the TransformRMolnar.
            TransformAbstractLine tLine = itr.next();
            if (tLine instanceof TransformRMolnar == false)
                continue;
            TransformRMolnar rmolnar = (TransformRMolnar)tLine;
            
            // Is this an end curve at the point tVertex?
            if (rmolnar.vEnd1 == tVertex && rmolnar.vControl1 == rmolnar.vEnd1)
                ltRMolnars.add(rmolnar);
            else if (rmolnar.vEnd2 == tVertex && rmolnar.vControl2 == rmolnar.vEnd2)
                ltRMolnars.add(rmolnar);
        }
        
        return ltRMolnars;
    }
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public boolean equals(Object obj) {
        if (obj instanceof TransformRMolnar) {
            if (((TransformRMolnar)obj).id == id)
                return true;
        } else if (obj instanceof RMolnarInfo) {
            RMolnarInfo rmolnar = ((RMolnarInfo)obj);
            if (vEnd1.getPoint().equals(rmolnar.getEndPoint1()) &&
                vEnd2.getPoint().equals(rmolnar.getEndPoint2()) &&
                vControl1.getPoint().equals(rmolnar.getControlPoint1()) &&
                vControl2.getPoint().equals(rmolnar.getControlPoint2()))
                return true;
        }
        
        return false;
    }
    
    public String toString() {
        return "{TransformRMolnar id[" + id + "] vControl1[" + vControl1.getId() + "] vEnd1[" + vEnd1.getId() + "] vEnd2["
                + vEnd2.getId() + "]  vControl2[" + vControl2.getId() + "] visited: " + visited + " selected: " + selected + "}";
    }

    // </editor-fold>
    
}
