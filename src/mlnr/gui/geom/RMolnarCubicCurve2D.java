/*
 * RMolnarCubicCurve2D.java
 *
 * Created on August 8, 2006, 2:52 PM
 *
 */

package mlnr.gui.geom;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import mlnr.type.FPointType;

/**
 * @author Robert Molnar II
 */
public class RMolnarCubicCurve2D extends CubicCurve2D.Float {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** This is the tension of the RMolnar curve. This is the blessed number that will create a smooth curve through points. */
    private static float TENSION=5.0f;
    
    // </editor-fold>
        
    /**
     * Constructs and initializes a RMolnarCubicCurve2D with coordinates
     * (0, 0, 0, 0, 0, 0).
     */
    public RMolnarCubicCurve2D() {
    }
    
    /**
     * Constructs and initializes a <code>RMolnarCubicCurve2D</code> from
     * the specified coordinates.
     * @param fptEnd1 the first end point.
     * @param fptEnd2 the last end point.
     * @param fptControl1 the first control point.
     * @param fptControl2 the last control point.
     */
    public RMolnarCubicCurve2D(FPointType fptEnd1, FPointType fptEnd2, FPointType fptControl1, FPointType fptControl2) {
        setCurve(fptEnd1, fptEnd2, fptControl1, fptControl2);
    }
    
    /** This will set the curve to an RMolnar curve. It will convert the coordinates
     * into a bezier which this class is built on top of, since an RMolnar curve is
     * a bezier curve tweaked to produce a curve through the points. <br>
     * Do not use any of the other setCurve() functions since they will set the curve
     * as a Bezier curve.
     * @param fptEnd1 the first end point.
     * @param fptEnd2 the last end point.
     * @param fptControl1 the first control point.
     * @param fptControl2 the last control point.
     */
    public void setCurve(FPointType fptEnd1, FPointType fptEnd2, FPointType fptControl1, FPointType fptControl2) {
        // Set the end points.
        x1 = fptEnd1.x;
        y1 = fptEnd1.y;
        x2 = fptEnd2.x;
        y2 = fptEnd2.y;
        
        // Calculate the vector of the left part.
        FPointType temp = new FPointType();
        temp.x = fptEnd2.x - fptControl1.x;
        temp.y = fptEnd2.y - fptControl1.y;
        
        // This is the sweat spot of div.
        if (temp.x != 0.0)
            temp.x /= TENSION;
        if (temp.y != 0.0)
            temp.y /= TENSION;
        
        // Calculate the first control point.
        ctrlx1 = temp.x + fptEnd1.x;
        ctrly1 = temp.y + fptEnd1.y;
        
        // Calculate the vector of the right part.
        temp.x = fptEnd1.x - fptControl2.x;
        temp.y = fptEnd1.y - fptControl2.y;
        
        // This is the sweat spot of div.
        if (temp.x != 0.0)
            temp.x /= TENSION;
        if (temp.y != 0.0)
            temp.y /= TENSION;
        
        // Calculate the last control point.
        ctrlx2 = temp.x + fptEnd2.x;
        ctrly2 = temp.y + fptEnd2.y;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Implemented Methods ">    
    
    /**
     * Returns the X coordinate of the start point
     * in double precision.
     * @return the X coordinate of the start point of the
     * 		<code>CubicCurve2D</code>.
     */
    public double getX1() {
        return (double) x1;
    }
    
    /**
     * Returns the Y coordinate of the start point
     * in double precision.
     * @return the Y coordinate of the start point of the
     * 		<code>CubicCurve2D</code>.
     */
    public double getY1() {
        return (double) y1;
    }
    
    /**
     * Returns the start point.
     * @return a {@link Point2D} that is the start point of the
     *		<code>CubicCurve2D</code>.
     */
    public Point2D getP1() {
        return new Point2D.Float(x1, y1);
    }
    
    /**
     * Returns the X coordinate of the first control point
     * in double precision.
     * @return the X coordinate of the first control point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getCtrlX1() {
        return (double) ctrlx1;
    }
    
    /**
     * Returns the Y coordinate of the first control point
     * in double precision.
     * @return the Y coordinate of the first control point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getCtrlY1() {
        return (double) ctrly1;
    }
    
    /**
     * Returns the first control point.
     * @return a <code>Point2D</code> that is the first control point
     *		of the <code>CubicCurve2D</code>.
     */
    public Point2D getCtrlP1() {
        return new Point2D.Float(ctrlx1, ctrly1);
    }
    
    /**
     * Returns the X coordinate of the second control point
     * in double precision.
     * @return the X coordinate of the second control point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getCtrlX2() {
        return (double) ctrlx2;
    }
    
    /**
     * Returns the Y coordinate of the second control point
     * in double precision.
     * @return the Y coordinate of the second control point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getCtrlY2() {
        return (double) ctrly2;
    }
    
    /**
     * Returns the second control point.
     * @return a <code>Point2D</code> that is the second control point
     *		of the <code>CubicCurve2D</code>.
     */
    public Point2D getCtrlP2() {
        return new Point2D.Float(ctrlx2, ctrly2);
    }
    
    /**
     * Returns the X coordinate of the end point
     * in double precision.
     * @return the X coordinate of the end point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getX2() {
        return (double) x2;
    }
    
    /**
     * Returns the Y coordinate of the end point
     * in double precision.
     * @return the Y coordinate of the end point of the
     *		<code>CubicCurve2D</code>.
     */
    public double getY2() {
        return (double) y2;
    }
    
    /**
     * Returns the end point.
     * @return a <code>Point2D</code> that is the end point
     *		of the <code>CubicCurve2D</code>.
     */
    public Point2D getP2() {
        return new Point2D.Float(x2, y2);
    }
    
    /** This will NOT set the RMolanrCubicCurve to produce an RMolnar curve. It will set the curve as a bezier curve.<br>
     * Sets the location of the endpoints and controlpoints
     * of this curve to the specified float coordinates.
     * @param x1,&nbsp;y1 the first specified coordinates used to set the start
     *		point of this <code>CubicCurve2D</code>
     * @param ctrlx1,&nbsp;ctrly1 the second specified coordinates used to set the
     *		first control point of this <code>CubicCurve2D</code>
     * @param ctrlx2,&nbsp;ctrly2 the third specified coordinates used to set the
     *		second control point of this <code>CubicCurve2D</code>
     * @param x2,&nbsp;y2 the fourth specified coordinates used to set the end
     *		point of this <code>CubicCurve2D</code>
     */
    public void setCurve(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2) {
        this.x1     = (float) x1;
        this.y1     = (float) y1;
        this.ctrlx1 = (float) ctrlx1;
        this.ctrly1 = (float) ctrly1;
        this.ctrlx2 = (float) ctrlx2;
        this.ctrly2 = (float) ctrly2;
        this.x2     = (float) x2;
        this.y2     = (float) y2;
    }
    
    /** This will NOT set the RMolanrCubicCurve to produce an RMolnar curve. It will set the curve as a bezier curve.<br>
     * Sets the location of the endpoints and controlpoints
     * of this curve to the specified float coordinates.
     * @param x1,&nbsp;y1 the first specified coordinates used to set the start
     *		point of this <code>CubicCurve2D</code>
     * @param ctrlx1,&nbsp;ctrly1 the second specified coordinates used to set the
     *		first control point of this <code>CubicCurve2D</code>
     * @param ctrlx2,&nbsp;ctrly2 the third specified coordinates used to set the
     *		second control point of this <code>CubicCurve2D</code>
     * @param x2,&nbsp;y2 the fourth specified coordinates used to set the end
     *		point of this <code>CubicCurve2D</code>
     */
    public void setCurve(float x1, float y1,
            float ctrlx1, float ctrly1,
            float ctrlx2, float ctrly2,
            float x2, float y2) {
        this.x1     = x1;
        this.y1     = y1;
        this.ctrlx1 = ctrlx1;
        this.ctrly1 = ctrly1;
        this.ctrlx2 = ctrlx2;
        this.ctrly2 = ctrly2;
        this.x2     = x2;
        this.y2     = y2;
    }
    
    /**
     * Returns the bounding box of the shape.
     * @return a {@link Rectangle2D} that is the bounding box of the
     * 		shape.
     */
    public Rectangle2D getBounds2D() {
        float left   = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
        float top    = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
        float right  = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
        float bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
        
        // If curve is a line then make sure there is a little room.
        
        // Vertical line.
        if (left == right) { 
            left -= 1.0f;
            right += 1.0f;
        } else if (top == bottom) {
            top -= 1.0f;
            bottom += 1.0f;
        }
        
        return new Rectangle2D.Float(left, top, right - left, bottom - top);
    }
    
    // </editor-fold>
}
