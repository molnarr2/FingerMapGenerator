/*
 * Vertex.java
 *
 * Created on July 14, 2006, 2:44 PM
 */

package mlnr.draw;

import java.util.*;
import java.io.*; 
import java.awt.*;
import java.util.prefs.*;
import java.awt.geom.*;
        
import org.xml.sax.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;

import mlnr.type.FPointType;
import mlnr.util.*;

/** This class represents a Vertex used by the VertexPool.
 *  The classes properties:<br>
 *  -  Once a vertex is created the position does not change. <br>
 * @author Robert Molnar II
 */
public class Vertex implements InterfacePoolObject  {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Unique id of the vertex. */
    int id;

    /** Current Point of the Vertex. */
    FPointType fCurrPt;
    
    /** True if the line is selected. */
    boolean selected = false;

    /** True if this vertex has been visited, else false it has not been. */
    boolean visited = false;
    
    /** True if the line is visible. */
    protected boolean visible = true;
    
    /** Used for visible box around the vertex. */
    Rectangle2D.Float fRectView = new Rectangle2D.Float();
    /** This is the rectangle used as the vertex proximity (2x that of the view). */
    Rectangle2D.Float fRectProximity = new Rectangle2D.Float();
    
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** This is the initial id of the vertex before the id is set. */
    static final int UNINITIALIZED = -1;
    
    /** This is true if the control points should be shown, false if not. TranslateVertex uses this too. */
    static boolean showControlPoints = true;
    
    /** This is the size of the rectangle box around the vertex if glue is turned on. It
     * is measured in mm. TranslateVertex uses this too. */
    static float glueRadius = 0.2f;
    
    /** This is the diameter of the glue box. TranslateVertex uses this too. */
    static float glueDiameter = 0.40f;
    
    /** Color of the box around each vertex. TranslateVertex uses this too. */
    static Color colorGlue = new Color(12,12,12);
    
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor Methods">
    
    /** Creates a new instance of Vertex
     * @param fPt is the point of the vertex.
     */
    public Vertex(FPointType fPt) {
        this.id = UNINITIALIZED;
        
        fCurrPt = new FPointType(fPt);
                
        // Set the rectangle used in this vertex.
        fRectView.setRect(fCurrPt.x - glueRadius,  fCurrPt.y - glueRadius, glueDiameter, glueDiameter);        
        fRectProximity.setRect(fCurrPt.x - (glueRadius * 2.0f),  fCurrPt.y - (Vertex.glueRadius * 2.0f), (glueDiameter * 2.0f), (glueDiameter * 2.0f));
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">

    /** This will write out the LinePool information.
     */
    void write(PrintWriter out) {
        out.println("         <vertex id='" + id + "' x='" + fCurrPt.x + "' y='" + fCurrPt.y + "' />");
    }
    
    // </editor-fold>    
        
    // <editor-fold defaultstate="collapsed" desc=" Is/Set/Has/Contain Methods ">

    /** This will see if the Vertex flags match the flag value.
     * @param flag is used to match against this Vertex flags. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific set. Unknown effects if using opposite SEARCH_* flags in one set.
     * @return true if it matches the passed in flag values, or false it does not.
     */
    final boolean is(int flag) {
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
    
    /** @return true if the x,y cordinates are within this vertex space, else false.
     */
    public final boolean isVertexSpace(float x, float y) {
        return fRectProximity.contains(x, y);
    }
    
    /** @return true if the x,y cordinates are within this vertex space, else false.
     */
    public final boolean isVertexSpace(FPointType fpt) {
        return fRectProximity.contains(fpt.x, fpt.y);
    }
    
    /** @return true if the x,y cordinates are within this vertex space, else false.
     */
    public final boolean isVertexSpace(Vertex v) {
        return fRectProximity.contains(v.fCurrPt.x, v.fCurrPt.y);
    }

    /** This will reset the statuses of this Vertex to their original statuses.
     * visited = false, selected = false, visible = true.
     */
    public void resetStatuses() {
        visited = false;
        selected = false;
        visible = true;
    }

    /** This will set the Vertex flags based on the flag values.
     * @param flag is used to set this Vertex flags based on the inputted flag values. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific set. Unknown effects if using opposite SEARCH_* flags in one set.
     */
    final void set(int flag) {
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

    // <editor-fold defaultstate="collapsed" desc=" Get/Draw Methods ">
    
    /** This will get the current position as a Rectangle2D.Float of the box around it.
     * @return Rectangle2D.Float of the current position (this is the box drawn for the vertex). Note, this function will create
     * a new Rectangle2D.Float every time it is called.
     */
    public Rectangle2D.Float getGluePoint() {
        return new Rectangle2D.Float(fCurrPt.x - glueRadius,  fCurrPt.y - glueRadius, glueDiameter, glueDiameter);
    }
    
    /** @return the current point this Vertex represents.
     */
    public FPointType getPoint() {
        return fCurrPt;
    }
    
    /** This will draw the glue point if enabled.
     */
    public final void drawGluePoint(Graphics2D g2d) {
        if (visible == false)
            return;
        
        if (selected) {
            Color cOld = g2d.getColor();
            g2d.setColor(Color.RED);
            g2d.draw(fRectView);
            g2d.setColor(cOld);
        } else
            g2d.draw(fRectView);
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Static Methods ">
    
    /** This will create a rectangle with double the size of a normal Vertex proximity size.
     *  @param fpt used to create the rectangle.
     *  @return a rectangle with double the size of a normal Vertex proximity size.
     */
    public static final Rectangle2D.Float createDoubleProximity(FPointType fpt) {
        return new Rectangle2D.Float(fpt.x - glueDiameter, fpt.y - glueDiameter, glueDiameter * 2.0f, glueDiameter * 2.0f);
    }
    
    // </editor-fold>
 
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    /** @param obj can be another vertex or even a FPointType. If it is a vertex then
     * it must have the same id. If it is a FPointType then the vertex must have the
     * same point cordinates.
     */
    public boolean equals(Object obj) throws IllegalArgumentException {
        if (obj instanceof FPointType) {
            return fCurrPt.equals((FPointType)obj);
        } else if (obj instanceof Vertex) {
            if (((Vertex)obj).id == id)
                return true;
            else
                return false;
        }
        
        throw new IllegalArgumentException("Unknown Object type: [" + obj + "].");
    }
    
    public String toString() {
        return "{Vertex " + id + ": s: [" + selected + "] v: [" + visited + "] (x,y): (" + fCurrPt.x + " " + fCurrPt.y + ")}";
    }
    
    // </editor-fold>
         
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
   
    /** This will print out the numbers for the lines.
     */
    public void debugDrawNumber(Graphics2D g2d) {
        g2d.drawString("{" + id + "}", fCurrPt.x, fCurrPt.y - 0.001f);
    }
    
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
    
    // <editor-fold defaultstate="collapsed" desc=" User Settings Methods ">
    
    /** This will set the radius of the glue boxes for the vertices. 
     * @param fGlueRadius is the radius of the glue boxes in mm.
     */
    static public void setGlueRadius(float fGlueRadius) {
        glueRadius = fGlueRadius;
        glueDiameter = glueRadius * 2;
    }
    
    /** @return the radius of the glue boxes in mm.
     */
    static public float getGlueRadius() {
        return glueRadius;
    }
    
    /** This will turn on/off the visible control points around all vertices.
     * @param visible is true if the should be shown.
     */
    static public void setControlPointsVisible(boolean visible) {
        showControlPoints = visible;
    }
    
    /** @return true if the control points are visible, else false.
     */
    static public boolean isControlPointsVisible() {
        return showControlPoints;
    }
    
    /** @param c is the color of the control points.
     */
    static public void setControlPointColor(Color c) {
        colorGlue = c;
    }
    
    /** @return the color of the control point.
     */
    static public Color getControlPointColor() {
        return colorGlue;
    }
    
    // </editor-fold>
    
}

// <editor-fold defaultstate="collapsed" desc=" class VertexSettings ">

/** This will save the vertex settings
 */
class VertexSettings implements InterfaceSettings {
    static private String GLUE_PROX_RADIUS = "VertexGlueRadius";  // Float
    static private String CONTROL_COLOR = "VertexControlPointColor"; // Color

    public VertexSettings() {        
    }
    
    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        prefs.putFloat(GLUE_PROX_RADIUS, Vertex.getGlueRadius());
        ColorSave.saveColor(prefs, CONTROL_COLOR, Vertex.getControlPointColor());
    }
    
    public void load() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        Vertex.setGlueRadius(prefs.getFloat(GLUE_PROX_RADIUS, Vertex.getGlueRadius()));        
        Vertex.setControlPointColor(ColorSave.loadColor(prefs, CONTROL_COLOR, Vertex.getControlPointColor()));
    }
}

// </editor-fold>
