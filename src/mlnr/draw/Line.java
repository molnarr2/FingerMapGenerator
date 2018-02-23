/*
 * Line.java
 *
 * Created on July 14, 2006, 1:18 PM
 *
 */

package mlnr.draw;

import java.awt.*;
import java.awt.geom.*;
import java.io.PrintWriter;
import org.w3c.dom.*;
import mlnr.type.*;

/** Used to represent a Line in the LinePool.
 * @author Robert Molnar II
 */
public class Line extends AbstractLine {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    Line2D.Float fLineCurr = new Line2D.Float();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    public Line(Vertex vEnd1, Vertex vEnd2) {
        super(vEnd1, vEnd2);
        validate();
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
            g2d.draw(fLineCurr);
            g2d.setColor(cOld);
        } else
            g2d.draw(fLineCurr);
    }
        
    public Rectangle2D.Float getBounds2D() {
        return (Rectangle2D.Float)fLineCurr.getBounds2D();
    }            
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fLineCurr, fpt);
    }
    
    public AbstractLineInfo getInfo() {
        return new LineInfo(vEnd1.getPoint(), vEnd2.getPoint());
    }
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fLineCurr, 0.5f);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fLineCurr, t);
    }

    public Shape getShape(Vertex vFrom) {
        if (vFrom == vEnd1)
            return fLineCurr;
        else
            return new Line2D.Float(vEnd2.getPoint().x, vEnd2.getPoint().y, vEnd1.getPoint().x, vEnd1.getPoint().y);
    }
            
    public Shape getShape(float from, float to) {
        FPointType fptFrom = getParameterValue(from);
        FPointType fptTo = getParameterValue(to);
        return new Line2D.Float(fptFrom.x, fptFrom.y, fptTo.x, fptTo.y);
    }    
    
    public boolean isDupliate(AbstractLine abLine) {
        if (abLine instanceof Line == false)
            return false;
        Line l = (Line)abLine;
        if (l.vEnd1 == vEnd1 && l.vEnd2 == vEnd2)
            return true;
        if (l.vEnd1 == vEnd2 && l.vEnd2 == vEnd1)
            return true;
        
        return false;
    }
    
    public boolean isDupliate(AbstractLineInfo abLineInfo) {
        if (abLineInfo instanceof LineInfo == false)
            return false;
        
        LineInfo l = (LineInfo)abLineInfo;
        if (vEnd1.isVertexSpace(l.getEndPoint1()) && vEnd2.isVertexSpace(l.getEndPoint2()))
            return true;
        if (vEnd1.isVertexSpace(l.getEndPoint2()) && vEnd2.isVertexSpace(l.getEndPoint1()))
            return true;
        
        return false;
    }
    
    public boolean intersects(Rectangle2D.Float r) {
        return r.intersectsLine(fLineCurr);
    }
    
    public float ptSegDist(FPointType fpt) {
        return (float)fLineCurr.ptSegDist(fpt.x, fpt.y);
    }
    
    public String toString() {
        return "((Line id[" + id + "] vEnd1[" + vEnd1.getId() + "] vEnd2[" + vEnd2.getId() 
            + "] visited: " + visited + " selected: " + selected + " visible: " + visible + ")";
    }    
    
    public String toStringVerbose() {
        return "((Line id[" + id + "] vEnd1[" + vEnd1 + "] vEnd2[" + vEnd2 + "])";
    }
    
    public void validate() {
        // Set new curve.        
        fLineCurr.setLine(vEnd1.getPoint().x, vEnd1.getPoint().y, vEnd2.getPoint().x, vEnd2.getPoint().y);
    }
    
    public void write(PrintWriter out) {
        out.println("        <line id='" + id + "' v1='" + vEnd1.getId() + "' v2='" + vEnd2.getId() + "' />");
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will create a debug string encoded for the ToolDebug, uses | to
     * indicate a break.
     */
    public String debugToString() {
        return "Line|id [" + id + "]| vertex end point ids [" + vEnd1.getId() + " " + vEnd2.getId() + "]| vEnd1[" + vEnd1 + "]| vEnd2[" + vEnd2 + "]";
    }
    
    // </editor-fold>
    
}