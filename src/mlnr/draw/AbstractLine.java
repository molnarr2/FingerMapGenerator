/*
 * AbstractLine.java
 *
 * Created on June 2, 2005, 10:59 PM
 */

package mlnr.draw;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import mlnr.type.*;

/** All drawing objects inherit from this class.
 * @author Robert Molnar II
 */
abstract public class AbstractLine implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Id of this object. */
    int id=0;
    
    /** This is the first end vertex. */
    protected Vertex vEnd1;
    
    /** This is the other end vertex. */
    protected Vertex vEnd2;
    
    /** True if the line has been visited. */
    protected boolean visited = false;
    
    /** True if the line is selected. */
    protected boolean selected = false;
    
    /** True if the line is visible. */
    protected boolean visible = true;
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">

    /** This is the distance that is ok to divide this line into. */
    private static final float DIVIDE_DISTANCE = 1.0f;
    
    // </editor-fold>    
        
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    public AbstractLine(Vertex vEnd1, Vertex vEnd2) {
        this.vEnd1 = vEnd1;
        this.vEnd2 = vEnd2;
    }

    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Flag Methods ">
    
    /** 
     * @param flag is used to see if this AbstractLine is of a certain type. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific search.
     * @return true if this AbstractLine matches the flag values or false it does not match the flag values.
     */
    public final boolean is(int flag) {
        // Does the flags match?
        if (flag == DrawingLinePool.SEARCH_OFF)
            return true;
        
        // Check all flags.
        if ((flag & DrawingLinePool.SEARCH_SELECT_ON) == DrawingLinePool.SEARCH_SELECT_ON && !selected)
            return false;
        if ((flag & DrawingLinePool.SEARCH_SELECT_OFF) == DrawingLinePool.SEARCH_SELECT_OFF && selected)
            return false;
        if ((flag & DrawingLinePool.SEARCH_VISIT_ON) == DrawingLinePool.SEARCH_VISIT_ON && !visited)
            return false;
        if ((flag & DrawingLinePool.SEARCH_VISIT_OFF) == DrawingLinePool.SEARCH_VISIT_OFF && visited)
            return false;
        if ((flag & DrawingLinePool.SEARCH_VISIBLE_ON) == DrawingLinePool.SEARCH_VISIBLE_ON && !visible)
            return false;
        if ((flag & DrawingLinePool.SEARCH_VISIBLE_OFF) == DrawingLinePool.SEARCH_VISIBLE_OFF && visible)
            return false;
        
        // All flags set have passed.
        return true;
    }

    /** This will set the AbstractLine based on the flag values.
     * @param flag is used to set this AbstractLine flags based on the inputted flag values. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific set. Unknown effects if using opposite SEARCH_* flags in one set.
     */
    void set(int flag) {
        // Does the flags match?
        if (flag == DrawingLinePool.SEARCH_OFF)
            return;
        if ((flag & DrawingLinePool.SEARCH_SELECT_ON) == DrawingLinePool.SEARCH_SELECT_ON)
            selected = true;
        if ((flag & DrawingLinePool.SEARCH_SELECT_OFF) == DrawingLinePool.SEARCH_SELECT_OFF)
            selected = false;
        if ((flag & DrawingLinePool.SEARCH_VISIT_ON) == DrawingLinePool.SEARCH_VISIT_ON)
            visited = true;
        if ((flag & DrawingLinePool.SEARCH_VISIT_OFF) == DrawingLinePool.SEARCH_VISIT_OFF)
            visited = false;
        if ((flag & DrawingLinePool.SEARCH_VISIBLE_ON) == DrawingLinePool.SEARCH_VISIBLE_ON)
            visible = true;
        if ((flag & DrawingLinePool.SEARCH_VISIBLE_OFF) == DrawingLinePool.SEARCH_VISIBLE_OFF)
            visible = false;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" End Point Methods ">
    
    /** This will see if it contains the 'vEnd' as an end vertex and if the line's flags match
     * the requirements of the 'searchFlag'.
     * @param vEnd is the vertex to see if it is an end vertex of this line.
     * @param searchFlag is a constant value from DrawingLinePool.SEARCH_* which can be
     * OR'd together with other flag requirements. Unknown resuls if OR'd together like flag requirements
     * that are opposite.
     * @return true if the line contains the 'vEnd' as an end vertex and if the line's flags match the
     * requirements of the 'searchFlag', else false did not.
     */
    boolean contain(Vertex vEnd, int searchFlag) {
        // Does the end vertex match?
        if (vEnd != vEnd1 && vEnd != vEnd2)
            return false;
        
        // Does the flags match?
        return is(searchFlag);
    }
    
    /** This will get the first vertex end point in the line.
     * @return the first end point Vertex in the line.
     */
    public Vertex getFirstEndVertex() {
        return vEnd1;
    }

    /** This will get the last vertex end point in the line.
     * @return the last end point Vertex in the line.
     */
    public Vertex getLastEndVertex() {
        return vEnd2;
    }
    
    /** This will get the opposite end point vertex of this line.
     * @param v is the vertex that is contained in this line.
     * @return the opposite vertex end point of the v vertex.
     */
    public Vertex getOppositeEndVertex(Vertex v) {
        if (v == vEnd1)
            return vEnd2;
        if (v == vEnd2)
            return vEnd1;
        
        throw new IllegalArgumentException("AbstractLine:: Vertex[" + v + "] is not in this line[" + this + "].");
    }

    /** This will see if the vEnd is a vertex end point of this line. It must match memory locations.
     * @param vEnd is the vertex to check against this line.
     * @return true if the vEnd is an end vertex for this line, else false it is not.
     */
    boolean isEndVertex(Vertex vEnd) {
        if (vEnd1 == vEnd || vEnd2 == vEnd)
            return true;
        return false;
    }
    
    /** @param v is to be checked against the first end point or if v is an auto link vertex then against this auto link vertex.
     * @return true if the vertex is the first end point of this line.
     */
    public boolean isFirstEndVertex(Vertex v) {
        return (vEnd1 == v);
    }
    
    /** @param v is to be checked against the last end point. 
     * @return true if the vertex is the last end point of this line.
     */
    public boolean isLastEndVertex(Vertex v) {
        return (vEnd2 == v);
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Angle Methods Methods ">
    
    /** This will get the difference angle of this AbstractLine and the abOther at the 'thisParametric'. This AbstractLine
     *  is the starting angle and the difference is abLine angle minus this AbstractLine angle. It will always be a positive
     * angle returned in radians.
     *  @param tThis is the position on this AbstractLine where the angle will be sampled at.
     *  @param vThis is the direction going to for this AbstractLine where the angle will be sampled at.
     *  @param abOther is the AbstractLine used to get the angle difference.
     *  @param tOTher is the position on the other AbstractLine to get the angle sampled at.
     *  @param vOther is the direction going to for the other AbstractLine where the angle will be sampled at.
     *  @return the angle difference between going counter clockwise from this AbstractLine to abOther and it will always
     *  be a positive angle returned in radians.
     */
    public float getDifferenceAngleCCW(float tThis, Vertex vThis, AbstractLine abOther, float tOther, Vertex vOther) {
        float rad1 = getAngleCCW(tThis, vThis);
        float rad2 = abOther.getAngleCCW(tOther, vOther);
        
        // Get the angle difference.
        float radDelta = rad2 - rad1;
        if (radDelta < 0.0f)
            radDelta = 2.0f * (float)Math.PI + radDelta;
        
        return radDelta;
    }
    
    /** This will get the difference angle of this AbstractLine and abLine at the point vEnd. This AbstractLine
     * is the starting angle and the difference is abLine angle minus this AbstractLine angle. It will always be
     * a positive angle returned in radians.
     * @param vEnd is the vertex which both lines have in common.
     * @param abLine is the AbstractLine to get the difference angle from.
     * @return the difference angle between going Counter Clockwise from this AbstractLine to abLine and it will always
     * be a positive angle returned in radians.
     */
    public float getDifferenceAngleCCW(Vertex vEnd, AbstractLine abLine) {
        throw new UnsupportedOperationException("NOT SUPPORTED ANYMORE");
//        float rad1 = getAngleCCW(vEnd);
//        float rad2 = abLine.getAngleCCW(vEnd);
//        
//        // Get the angle difference.
//        float radDelta = rad2 - rad1;
//        if (radDelta < 0.0f)
//            radDelta = 2.0f * (float)Math.PI + radDelta;
//        
//        return radDelta;
    }
    
    /** This will get the angle of this AbstractLine at the parametric position parameteric and to vEnd. Uses a small distance from parameteric to
     *  vEnd to compute the angle.
     *  @param tFrom is the starting position to get the angle at.
     * @param direction is the vertex to use as the direction.
     * @return the angle in radians for this AbstractLine going CCW.
     * @throws IllegalArgumentException Vertex does not belong to the line.
     */
    public float getAngleCCW(float tFrom, Vertex direction) {        
        FPointType fptFrom = getParameterValue(tFrom);
        
        // Get the point that is a 10th of the way from 'vEnd'.
        FPointType fpt10th = null;
        if (vEnd1 == direction)
            fpt10th = getParameterValue(tFrom - 0.05f);
        else if (vEnd2 == direction)
            fpt10th = getParameterValue(tFrom + 0.05f);
        else
            throw new IllegalArgumentException("The vertex: " + direction + " does not exist in this AbstractLine: " + this);
        
        float xDelta = fpt10th.x - fptFrom.x;
        float yDelta = fpt10th.y - fptFrom.y;
                
        // Get the radian angle.
        float rad;
        if (xDelta > -0.00001f && xDelta < 0.00001f) {
            if (yDelta < 0.0f)
                rad = (float)Math.PI + ((float)Math.PI / 2);
            else
                rad =  ((float)Math.PI / 2);
        }else {
            rad = (float)Math.atan(yDelta/xDelta);

            // Convert it to the II,III quadarants if the radian would be in those areas.
            if (xDelta < 0)
                rad += Math.PI;
        }
        
        if (rad < 0.0f) {
            rad += (float)Math.PI * 2;
        }
        
        return rad;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Selected/Visible/Visit Methods ">
    
    /** @return true if the Line is selected.
     */
    public boolean isSelected() {        
        return selected;
    }
    
    /** @return true if this line is visible, else false it is not.
     */
    public boolean isVisible() {
        return visible;
    }
    
    /** @return true if the Line is visited.
     */
    public boolean isVisited() {
        return visited;
    }

    /** This will reset the statuses of this AbstractLine to their original statuses.
     * visited = false, selected = false, visible = true.
     */
    public void resetStatuses() {
        visited = false;
        selected = false;
        visible = true;
    }
    
    /** Set the line selected or not.
     * @param selected is true if a line is selected, else false it is not selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /** This will set the visible flag.
     * @param visible is true if the line is to be visible, else false it is to be invisible.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /** Set the line visited or not.
     * @param visited is true if a line is visited, else false it is not visited.
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    /** @return true if the line id's are equal.
     */
    public boolean equals(Object obj) {
        if (((AbstractLine)obj).id == id)
            return true;
        return false;
    }    
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Abstract Methods ">

    /** This will draw the line.
     * @param g2d is the graphics device.
     */
    abstract public void draw(Graphics2D g2d);
    
    /** @return The bounds of this LinePool by using the measurements of the lines (not the vertices) or null.
     */
    abstract public Rectangle2D.Float getBounds2D();
    
    /** This will get the closest parameter position on the AbstractLine where the fpt distance is the closest.
     * @param fpt is a point can or can not be on the AbstractLine.
     * @return the closest parameter position on the AbstractLine where the fpt distance is the closest.
     */
    abstract public float getClosestParameterT(FPointType fpt);

    /** @return an AbstractLineInfo describing this AbstractLine.
     */
    abstract public AbstractLineInfo getInfo();
    
    /** @return the middle point on the line or curve.
     */
    abstract public FPointType getMiddlePt();
    
    /** This will get the position on the AbstractLine by using the parametric t value.
     * @param t is the parameteric value.
     * @return the position on the AbstractLine according to the value t.
     */
    abstract public FPointType getParameterValue(float t);
    
    /** This will get the shape from the parametric value of 'from' to 'to'.
     *  @param from is the parametric value where the shape will start at.
     *  @param to is the parametric value where the shape will end at.
     *  @return a shape representing this Line, Bezier, and/or RMolnar from 'from' to 'to'.
     */
    abstract public Shape getShape(float from, float to);
    
    /** @param vFrom is the vertex which this shape should start from. 
     * @return the shape of this AbstractLine.
     */
    abstract public Shape getShape(Vertex vFrom);
    
    /** Checks to see if this AbstractLine intersects with the rectangle.
     * @param r is the rectangle to see if this AbstractLine intersects with.
     * @return true if it intersects with the rectangle.
     */
    abstract public boolean intersects(Rectangle2D.Float r);
    
    /** This will return true if the line abLine is a duplicate of this line. The vertex
     *  used in the line must match. However, the end point vertices could be inverted that
     *  is still a duplicate.
     * @param abLine is the AbstractLine to see if it is a duplicate of this AbstractLine.
     * @return true if abLine is a duplicate of this line.
     */
    abstract public boolean isDupliate(AbstractLine abLine);
    
    /** Checks to see if abLineInfo is a duplicate.
     * @param abLineInfo is used to see if the points in that line match this lines points.
     * @return true if abLineInfo is a duplicate of this line.
     */
    abstract public boolean isDupliate(AbstractLineInfo abLineInfo);
    
    /** Returns the distance from a <code>FPointType</code> to this line segment.
     * The distance measured is the distance between the specified point and the closest point between 
     * the current line's endpoints. If the specified point intersects the line segment in between the
     * endpoints, this method returns 0.0.
     * @param fpt the specified <code>FPointType</code> being measured	against this line segment
     * @return a float value that is the distance from the specified <code>FPointType</code> to the current line
     * segment.
     */
    abstract public float ptSegDist(FPointType fpt);
    
    /** This is the verbose version of printing out the AbstractLine. */
    abstract String toStringVerbose();

    /** This will validate the line by update its drawing line structure.
     */
    abstract public void validate();
    
    /** This will write out the AbstractLine in XML format.
     */
    abstract public void write(PrintWriter out);
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Interface InterfacePoolObject ">
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public int getZDepth() {
        return 0;
    }
    
    public void setZDepth(int zDepth) {        
    }
    
    public int compareTo(Object o) {
        return 0;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will print out the line information onto the graph.
     */
    public void debugDrawNumbers(Graphics2D g2d) {
        FPointType fpt1 = vEnd1.getPoint();
        FPointType fpt2 = vEnd2.getPoint();
        
        // Get the middle point.
        float xMid = (fpt1.x + fpt2.x) / 2;
        float yMid = (fpt1.y + fpt2.y) / 2;
        
        // Print out the information.
        String str = "" + id + ":[" + vEnd1.getId() + " " + vEnd2.getId() + "]";
        g2d.drawString(str, xMid, yMid);
    }
    
    /** This will create a debug string encoded for the ToolDebug, uses | to
     * indicate a break.
     */
    abstract public String debugToString();
    
    // </editor-fold>        
    
}