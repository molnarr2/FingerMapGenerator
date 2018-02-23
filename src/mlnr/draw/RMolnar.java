/*
 * RMolnar.java
 *
 * Created on July 14, 2006, 1:18 PM 
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import org.w3c.dom.*;
import mlnr.type.*;
import mlnr.util.*;
import mlnr.gui.geom.RMolnarCubicCurve2D;

/** Used to represent a Curve in the DrawingLinePool.
 * @author Robert Molnar II
 */
public class RMolnar extends AbstractLine {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Control point for the RMolnar. Adjacent to vEnd1. */
    Vertex vControl1;
    
    /** Control point for the RMolnar. Adjacent to vEnd2. */
    Vertex vControl2;

    /** Used to draw this curve. */
    RMolnarCubicCurve2D fRMolnarCurr = new RMolnarCubicCurve2D();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of RMolnar */
    public RMolnar(Vertex vEnd1, Vertex vEnd2, Vertex vControl1, Vertex vControl2) {
        super(vEnd1, vEnd2);
        this.vControl1 = vControl1;
        this.vControl2 = vControl2;
        
        validate();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Public Methods ">
    
    /** This will get the control point that corresponds to the end vertex.
     * @param vEnd is the end vertex.
     * @return the control vertex that corresponds to the end vertex.
     */
    public Vertex getControlVertex(Vertex vEnd) {
        if (vEnd == vEnd1)
            return vControl1;
        else if (vEnd == vEnd2)
            return vControl2;
        throw new IllegalArgumentException("RMolnar::getControlVertex(" + vEnd + ") does not exist in this[" + this + "].");
    }
    
    /** @return the first control vertex.
     */
    public Vertex getFirstControlVertex() {
        return vControl1;
    }
    
    /** @return the last control vertex.
     */
    public Vertex getLastControlVertex() {
        return vControl2;
    }

    /** This will see if 'vControl' is a control vertex. Will match based on memory location, therefore it
     * MUST be the exact control vertex.
     * @param vControl is the vertex to see if it is a control vertex.
     * @return true if it is a control vertex in this RMolnar, else false is not a control vertex in this RMolnar.
     */
    boolean isControlVertex(Vertex vControl) {
        if (vControl == vControl1)
            return true;
        else if (vControl == vControl2)
            return true;
        return false;
    }
    
    /** @param v is an end point vertex of this RMolnar. 
     * @return true if at the vertex v that this RMolnar is an ending RMolnar, meaning that its control and end points
     * are the same vertex.
     */
    boolean isEnd(Vertex v) {
        if (v == vEnd1) {
            if (vEnd1 == vControl1)
                return true;
        } else if (v == vEnd2) {
            if (vEnd2 == vControl2)
                return true;
        } 
        return false;
    }
    
    /** This will set the first control vertex.
     * @param vNew is the new vertex for the first control vertex.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem setFirstControlVertex(Vertex vNew) {
        RelinkControlPoints relink = new RelinkControlPoints(RelinkControlPoints.CONTROLPT_FIRST, vControl1, vNew);
        vControl1 = vNew;
        return relink;
    }
    
    /** This will set the last control vertex.
     * @param vNew is the new vertex for the last control vertex.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem setLastControlVertex(Vertex vNew) {
        RelinkControlPoints relink = new RelinkControlPoints(RelinkControlPoints.CONTROLPT_LAST, vControl2, vNew);
        vControl2 = vNew;
        return relink;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Implemented Methods ">
    
    public void draw(Graphics2D g2d) {
        // Don't draw line if invisible.
        if (visible == false)
            return;
        
        if (selected) {
            Color cOld = g2d.getColor();
            g2d.setColor(Color.RED);
            g2d.draw(fRMolnarCurr);
            g2d.setColor(cOld);
        } else
            g2d.draw(fRMolnarCurr);
    }
    
    public Rectangle2D.Float getBounds2D() {
        return MathLineCurve.getBounds2D(fRMolnarCurr);
    }
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fRMolnarCurr, fpt);
    }
    
    public AbstractLineInfo getInfo() {
        RMolnarInfo iRmolnar = new RMolnarInfo();
        iRmolnar.setEndPoint1(vEnd1.getPoint());
        iRmolnar.setEndPoint2(vEnd2.getPoint());
        iRmolnar.setControlPoint1(vControl1.getPoint());
        iRmolnar.setControlPoint2(vControl2.getPoint());
        return iRmolnar;
    }
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fRMolnarCurr, 0.5f);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fRMolnarCurr, t);
    }
    
    public Shape getShape(Vertex vFrom) {
        if (vFrom == vEnd1)
            return fRMolnarCurr;
        
        return new RMolnarCubicCurve2D(vEnd2.getPoint(), vEnd1.getPoint(), vControl2.getPoint(), vControl1.getPoint());
    }    
    
    public Shape getShape(float from, float to) {
        CubicCurve2D.Float curve = MathLineCurve.subDivide(fRMolnarCurr, from, to);
        if (from > to) // Need to make sure curve comes through the correct way.
            return MathLineCurve.invert(curve);
        return curve;
    }    
    
    public boolean isDupliate(AbstractLine abLine) {
        if (abLine instanceof RMolnar == false)
            return false;
        
        RMolnar r = (RMolnar)abLine;
        if (r.vEnd1 == vEnd1 && r.vEnd2 == vEnd2 && r.vControl1 == vControl1 && r.vControl2 == vControl2)
            return true;
        if (r.vEnd1 == vEnd2 && r.vEnd2 == vEnd1 && r.vControl1 == vControl2 && r.vControl2 == vControl1)
            return true;
        return false;
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
    
    public boolean intersects(Rectangle2D.Float r) {
        // Perform a simple test.
        if (fRMolnarCurr.getBounds2D().intersects(r) == false)
            return false;
        
        // Perform an exhaustive test.
        return MathLineCurve.intersectsBezier(fRMolnarCurr, r);
    }
    
    public float ptSegDist(FPointType fpt) {
        return MathLineCurve.closestPoint(fRMolnarCurr, fpt).distance(fpt);
    }
    
    public String toString() {
        return "(RMolnar id[" + id + "] vControl1[" + vControl1.getId() + "] vEnd1[" + vEnd1.getId() + "] vEnd2["
                + vEnd2.getId() + "]  vControl2[" + vControl2.getId() + "] visited: " + visited + " selected: " + selected + " visible: " + visible + ")";
    }
    
    public String toStringVerbose() {
        return "(RMolnar id[" + id + "] vControl1[" + vControl1 + "] vEnd1[" + vEnd1 + "] vEnd2["
                + vEnd2 + "]  vControl2[" + vControl2 + "])";
    }
    
    public void validate() {                        
        // Set new curve.        
        fRMolnarCurr.setCurve(vEnd1.getPoint(), vEnd2.getPoint(), vControl1.getPoint(), vControl2.getPoint());
    }    
    
    public void write(PrintWriter out) {
        out.println("        <rmolnar id='" + id + "' v1='" + vEnd1.getId() + "' v2='" 
                + vEnd2.getId() + "' c1='" + vControl1.getId() + "' c2='" + vControl2.getId() + "' />");
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will create a debug string encoded for the ToolDebug, uses | to
     * indicate a break.
     */
    public String debugToString() {
        return "RMolnar|id [" + id + "]| vertex point ids [" + vControl1.getId() + " " + vEnd1.getId() + " " + vEnd2.getId() + " " 
                + vControl2.getId() + "]| vEnd1[" + vEnd1 + "]| vEnd2[" + vEnd2 + "]| vControl1[" + vControl1 + "]| vControl2[" 
                + vControl2 + "]|";
    }
   
    /** This will print out the line information onto the graph.
     */
    public void debugDrawNumbers(Graphics2D g2d) {
        FPointType fpt1 = vEnd1.getPoint();
        FPointType fpt2 = vEnd2.getPoint();
        
        // Get the middle point.
        float xMid = (fpt1.x + fpt2.x) / 2;
        float yMid = (fpt1.y + fpt2.y) / 2;
        
        // Print out information. 
        String str = "" + id + ":[" + vControl1.getId() + " " + vEnd1.getId() + " " + vEnd2.getId() + " " + vControl2.getId() + "]";
        g2d.drawString(str, xMid, yMid);
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" RelinkControlPoints Class ">
    
    class RelinkControlPoints implements InterfaceUndoItem {
        /** This is the first control point. */
        public final static int CONTROLPT_FIRST=1;
        /** This is the last control point. */
        public final static int CONTROLPT_LAST=2;
        
        /** This is the vertex that was there before it was changed. */
        Vertex oldVertex;
        /** This is the new vertex that will be there. */
        Vertex newVertex;
        /** This is the position where the vertex was changed. */
        int controlType;
        
        /** Creates a new instance of RelinkControlPoints
         * @param abLine is the RMolnar that had a change in the a vertex position.
         * @param controlType is either CONTROLPT_FIRST, CONTROLPT_LAST.
         * @param oldVertex was the vertex of the controlType before the changing.
         * @param newVertex is the vertex which will replace the oldVertex.
         */
        public RelinkControlPoints(int controlType, Vertex oldVertex, Vertex newVertex) {
            this.controlType = controlType;
            this.oldVertex = oldVertex;
            this.newVertex = newVertex;
        }
        
        public void undoItem() {
            if (controlType == CONTROLPT_FIRST)
                vControl1 = oldVertex;
            else
                vControl2 = oldVertex;
        }
        
        public void redoItem() {
            if (controlType == CONTROLPT_FIRST)
                vControl1 = newVertex;
            else
                vControl2 = newVertex;
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        /** get the textual name of the control type.
         */
        protected String getTextual() {
            if (controlType == CONTROLPT_FIRST)
                return "CONTROLPT_FIRST";
            else if (controlType == CONTROLPT_LAST)
                return "CONTROLPT_LAST";
            return "";
        }
        
        public String toString() {
            return "{RMolnar::RelinkControlPoints id[" + id + "] controlType[" + getTextual() + "] oldVertexId[" + oldVertex.getId() + "] newVertexId[" + newVertex.getId() + "]}";
        }
    }
    
    // </editor-fold>
}
