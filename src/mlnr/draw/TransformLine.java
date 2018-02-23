/*
 * TransformLine.java
 *
 * Created on April 27, 2007, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.type.SFPointType;
import mlnr.util.XmlUtil;
import org.w3c.dom.Element;

/**
 *
 * @author Robert Molnar 2
 */
public class TransformLine extends TransformAbstractLine {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    Line2D.Float fLineCurr = new Line2D.Float();
    Line2D.Float fLinePrev = new Line2D.Float();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of TransformLine */
    public TransformLine(TransformVertex vEnd1, TransformVertex vEnd2) {
        super(vEnd1, vEnd2);
        // Set the lines. 
        validate();
        validate();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Implemented Abstract Methods ">
    
    public void draw(Graphics2D g2d, boolean erase) {
        if (erase)
            g2d.draw(fLinePrev);
        g2d.draw(fLineCurr);
    }

    Rectangle2D.Float getBounds2D() {
        return (Rectangle2D.Float)fLineCurr.getBounds2D();
    }
    
    public float getClosestParameterT(FPointType fpt) {
        return MathLineCurve.closestParametric(fLineCurr, fpt);
    }
    
    public FPointType getParameterValue(float t) {
        return MathLineCurve.parametricToPoint(fLineCurr, t);
    }
    
    public AbstractLineInfo getInfo() {
        return new LineInfo(vEnd1.getPoint(), vEnd2.getPoint());
    }
    
    public FPointType getMiddlePt() {
        return MathLineCurve.parametricToPoint(fLineCurr, 0.5f);
    }
    
    LinkedList<SFPointType> getSampledPoints(TransformVertex vFrom, TransformVertex vTo, int sampleSize) {
        LinkedList<SFPointType> ltPoints = new LinkedList();
        ltPoints.add(SFPointType.sampledLine(vFrom.getPoint(), true));
        ltPoints.add(SFPointType.sampledLine(vTo.getPoint(), true));
        return ltPoints;
    }
    
    public boolean intersects(Rectangle2D.Float r) {
        return r.intersectsLine(fLineCurr);
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
    
    public float ptSegDist(FPointType fpt) {
        return (float)fLineCurr.ptSegDist(fpt.x, fpt.y);
    }
    
    public void validate() {
        // Old becomes new.
        fLinePrev.setLine(fLineCurr);
        // Set new curve.        
        fLineCurr.setLine(vEnd1.getPoint().x, vEnd1.getPoint().y, vEnd2.getPoint().x, vEnd2.getPoint().y);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public boolean equals(Object obj) {
        if (obj instanceof TransformLine) {
            if (((TransformLine)obj).id == id)
                return true;
        } else if (obj instanceof LineInfo) {
            LineInfo line = (LineInfo)obj;
            if (vEnd1.getPoint().equals(line.getEndPoint1()) &&
                vEnd2.getPoint().equals(line.getEndPoint2()))
                return true;
        }
        
        return false;
    }
    
    public String toString() {
        return "{TransformLine id[" + id + "] vEnd1[" + vEnd1.getId() + "] vEnd2[" + vEnd2.getId() + "]}";
    }        
      
    // </editor-fold>   
    
}
