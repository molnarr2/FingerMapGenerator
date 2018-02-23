/*
 * Bezier.java 
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
import java.awt.geom.*;
import java.io.PrintWriter;
import org.w3c.dom.*;
import mlnr.type.*;
import mlnr.util.*;

/** Used to represent a Bezier curve in the DrawingLinePool.
 * @author Robert Molnar II
 */
public class Bezier extends AbstractLine {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Used to draw this curve. */
    CubicCurve2D.Float fBezierCurr = new CubicCurve2D.Float();
    
    /** Control points for the Bezier. Adjacent to vEnd1. */
    Vertex vControl1;
    /** Control points for the Bezier. Adjacent to vEnd2. */
    Vertex vControl2;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of Bezier */
    public Bezier(Vertex vEnd1, Vertex vEnd2, FPointType ptControl1, FPointType ptControl2) {
        super(vEnd1, vEnd2);
        
        vControl1 = new Vertex(ptControl1);
        vControl2 = new Vertex(ptControl2);
        
        validate();
    }
    
    // </editor-fold>
     
    // <editor-fold defaultstate="collapsed" desc=" Public Methods ">
    
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
            
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Implemented Methods ">
    
    public void draw(Graphics2D g2d) {
        // Don't draw line if invisible.
        if (visible == false)
            return;
        
        if (selected) {
            Color cOld = g2d.getColor();
            g2d.setColor(Color.RED);
            g2d.draw(fBezierCurr);
            g2d.setColor(cOld);
        } else
            g2d.draw(fBezierCurr);
    }
    
    public Rectangle2D.Float getBounds2D() {
        return MathLineCurve.getBounds2D(fBezierCurr);
    }
    
    public AbstractLineInfo getInfo() {
        BezierInfo bInfo = new BezierInfo();
        bInfo.setEndPoint1(vEnd1.getPoint());
        bInfo.setEndPoint2(vEnd2.getPoint());
        bInfo.setControlPoint1(vControl1.getPoint());
        bInfo.setControlPoint2(vControl2.getPoint());
        return bInfo;
    }
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fBezierCurr, fpt);
    }
    
    public Shape getShape(Vertex vFrom) {
        if (vFrom == vEnd1)            
            return fBezierCurr;
        else
            return new CubicCurve2D.Float(vEnd2.getPoint().x, vEnd2.getPoint().y, 
                    vControl2.getPoint().x, vControl2.getPoint().y, 
                    vControl1.getPoint().x, vControl1.getPoint().y,
                    vEnd1.getPoint().x, vEnd1.getPoint().y);
    }
    
    public Shape getShape(float from, float to) {
        CubicCurve2D.Float curve = MathLineCurve.subDivide(fBezierCurr, from, to);
        if (from > to) // Need to make sure curve comes through the correct way.
            return  MathLineCurve.invert(curve);
        return curve;
    }    
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fBezierCurr, 0.5f);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fBezierCurr, t);
    }
    
    public boolean isDupliate(AbstractLine abLine) {
        if (abLine instanceof Bezier == false)
            return false;
        Bezier b = (Bezier)abLine;
        if (b.vEnd1 == vEnd1 && b.vEnd2 == vEnd2 && b.vControl1.getPoint().equals(vControl1.getPoint())
        && b.vControl2.getPoint().equals(vControl2.getPoint()))
            return true;
        if (b.vEnd1 == vEnd2 && b.vEnd2 == vEnd1 && b.vControl1.getPoint().equals(vControl2.getPoint())
        && b.vControl2.getPoint().equals(vControl1.getPoint()))
            return true;
        
        return false;
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
    
    public boolean intersects(Rectangle2D.Float r) {
        // Perform a simple test.
        if (fBezierCurr.getBounds2D().intersects(r) == false)
            return false;
        
        // Perform an exhaustive test.
        return MathLineCurve.intersectsBezier(fBezierCurr, r);
    }
    
    public float ptSegDist(FPointType fpt) {
        return MathLineCurve.closestPoint(fBezierCurr, fpt).distance(fpt);
    }
    
    public String toString() {
        return "((Bezier id[" + id + "] vEnd1[" + vEnd1.getId() + "] vEnd2[" + vEnd2.getId() + "] vControl1[" + vControl1 + "] vControl2[" + vControl2 
            + "] visited: " + visited + " selected: " + selected + " visible: " + visible + ")";
    }
    
    public String toStringVerbose() {
        return "((Bezier id[" + id + "] vEnd1[" + vEnd1 + "] vEnd2[" + vEnd2 + "] vControl1[" + vControl1 + "] vControl2[" + vControl2 + "] )";
    }
    
    public void validate() {
        // Set new curve.
        fBezierCurr.setCurve(vEnd1.getPoint().x, vEnd1.getPoint().y, vControl1.getPoint().x, vControl1.getPoint().y,
                vControl2.getPoint().x, vControl2.getPoint().y, vEnd2.getPoint().x, vEnd2.getPoint().y);
    }
    
    public void write(PrintWriter out) {
        out.println("        <bezier id='" + id + "' v1='" + vEnd1.getId() + "' v2='" + vEnd2.getId() 
        + "' c1x='" + vControl1.getPoint().x + "' c1y='" + vControl1.getPoint().y + "' c2x='"
        + vControl2.getPoint().x + "' c2y='" + vControl2.getPoint().y + "' />");
    }
       
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will create a debug string encoded for the ToolDebug, uses | to
     * indicate a break.
     */
    public String debugToString() {
        return "Bezier|id [" + id + "]| vertex end point ids [" + vEnd1.getId() + " " + vEnd2.getId() + "]| vEnd1[" + vEnd1 + "]| vEnd2[" + vEnd2 + "]| vControl1[" + vControl1 + "]| vControl2[" + vControl2 + "]";
    }
    
    // </editor-fold>
    
}
