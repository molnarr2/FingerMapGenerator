/*
 * TransformVertex.java
 *
 * Created on April 27, 2007, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import mlnr.type.FPointType;
import mlnr.util.XmlUtil;
import org.w3c.dom.Element;

/** This class is used for transforming vertices. Since the vertices will be moving, they can
 * overlap each other. There is no restrictions on where a vertex can be at. This class uses the
 * settings from Vertex class so as to keep them insync with each other.
 * @author Robert Molnar II
 */
public class TransformVertex implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Unique id of the TransformVertex. */
    private int id;
    
    /** This is the TransformAbstractLine which was divided by this AutoLinkVertex. */
    private TransformAbstractLine parametericLine = null;
    
    /** This is the parameteric t position which this AutoLinkVertex position came from. */
    private float parametericT = 0.0f;
    
    /** Used for the control rectangle around the vertex. */
    private Rectangle2D.Float fRectangle = new Rectangle2D.Float();
    
    /** This is the radius from the calculated point to the original point. Used in resizing and rotating. */
    private float radius;
    
    /** This is the radian from the calculated point to the original point. Used in resizing and rotating. */
    private float radian;
    
    /** Current Point of the TransformVertex. */
    private FPointType fCurrPt;
    
    /** Previous Point of the TransformVertex. */
    private FPointType fPrevPt;
    
    /** Save Point of the TransformVertex. */
    private FPointType fSavePt;
    
    /** True if this vertex is a bezierControl. */
    private boolean bezierControl = false;
    
    /** True if the TransformVertex can be moved, else false it cannot be translated anywhere. */
    private boolean transformable = false;
    
    /** True if this vertex has been visited, else false has not been visited. */
    private boolean visited = false;
    
    /** True if this vertex is visible, else false it is invisible. */
    private boolean visible = true;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors and Clone Methods">
    
    /** Creates a new instance of TransformVertex. Uses the id of Vertex.UNINITIALIZED, must be set.
     * @param fPt is the point of the vertex.
     */
    public TransformVertex(FPointType fPt) {
        this.id = Vertex.UNINITIALIZED;
        
        fCurrPt = new FPointType(fPt);
        fPrevPt = new FPointType(fPt);
        fSavePt = new FPointType(fPt);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eVertex is the element for the vertex in the RXML file.
     */
    static TransformVertex loadVersion10(Element eVertex) throws Exception {
        // get the values.
        int id = XmlUtil.getAttributeInteger(eVertex, "id");
        float x = (float)XmlUtil.getAttributeInteger(eVertex, "x") / 20.0f;
        float y = (float)XmlUtil.getAttributeInteger(eVertex, "y") / 20.0f;
        
        // Create the new vertex.
        TransformVertex v = new TransformVertex(new FPointType(x, y));
        v.setId(id);
        return v;
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eVertex is the element for the vertex in the RXML file.
     */
    static TransformVertex loadVersion20(Element eVertex) throws Exception {
        // get the values.
        int id = XmlUtil.getAttributeInteger(eVertex, "id");
        float x = (float)XmlUtil.getAttributeDouble(eVertex, "x");
        float y = (float)XmlUtil.getAttributeDouble(eVertex, "y");
        
        // Create the new vertex.
        TransformVertex v = new TransformVertex(new FPointType(x, y));
        v.setId(id);
        return v;
    }

    /** This will write out the LinePool information.
     */
    void write(PrintWriter out) {
        out.println("         <vertex id='" + id + "' x='" + fCurrPt.x + "' y='" + fCurrPt.y + "' />");
    }
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" State/Status Methods">

    /** Since this is needed so much the control vertex has its own function.
     *  @return true if it is a control vertex, else false it is not a control vertex.
     */
    final boolean isControlVertex() {
        return bezierControl;
    }
    
    /** This will see if the TransformVertex flags match the flag value. Note that not all of the 
     * TransformLinePool.SEARCH_* flags can be used. Check those flag comments to see which ones will work.
     * @param flag is used to match against this TransformVertex flags. See the TransformLinePool.SEARCH_* flag. 
     * They can be OR'd together for a more specific set. Unknown effects if using opposite SEARCH_* flags in one set.
     * @return true if it matches the passed in flag values, or false it does not.
     */
    final boolean is(int flag) {
        // Does the flags match?
        if (flag == TransformLinePool.SEARCH_OFF)
            return true;
        
        // Check all flags.
        if ((flag & TransformLinePool.SEARCH_VISIT_ON) == TransformLinePool.SEARCH_VISIT_ON && !visited)
            return false;
        if ((flag & TransformLinePool.SEARCH_VISIT_OFF) == TransformLinePool.SEARCH_VISIT_OFF && visited)
            return false;
        if ((flag & TransformLinePool.SEARCH_VISIBLE_ON) == TransformLinePool.SEARCH_VISIBLE_ON && !visible)
            return false;
        if ((flag & TransformLinePool.SEARCH_VISIBLE_OFF) == TransformLinePool.SEARCH_VISIBLE_OFF && visible)
            return false;
        if ((flag & TransformLinePool.SEARCH_TRANSFORMABLE_ON) == TransformLinePool.SEARCH_TRANSFORMABLE_ON && !transformable)
            return false;
        if ((flag & TransformLinePool.SEARCH_TRANSFORMABLE_OFF) == TransformLinePool.SEARCH_TRANSFORMABLE_OFF && transformable)
            return false;
        if ((flag & TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON) == TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON && !bezierControl)
            return false;
        if ((flag & TransformLinePool.SEARCH_BEZIER_CONTROL_PT_OFF) == TransformLinePool.SEARCH_BEZIER_CONTROL_PT_OFF && bezierControl)
            return false;
        
        // All flags set have passed.
        return true;        
    }

    /** This will set the TransformVertex flags based on the flag values. Note that not all of the 
     * TransformLinePool.SEARCH_* flags can be used. Check those flag comments to see which ones will work.
     * @param flag is used to set this TransformVertex flags based on the inputted flag values. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific set. Unknown 
     * effects if using opposite SEARCH_* flags in one set.
     */
    final void set(int flag) {
        // Does the flags match?
        if (flag == TransformLinePool.SEARCH_OFF)
            return;
        if ((flag & TransformLinePool.SEARCH_VISIT_ON) == TransformLinePool.SEARCH_VISIT_ON)
            visited = true;
        if ((flag & TransformLinePool.SEARCH_VISIT_OFF) == TransformLinePool.SEARCH_VISIT_OFF)
            visited = false;
        if ((flag & TransformLinePool.SEARCH_VISIBLE_ON) == TransformLinePool.SEARCH_VISIBLE_ON)
            visible = true;
        if ((flag & TransformLinePool.SEARCH_VISIBLE_OFF) == TransformLinePool.SEARCH_VISIBLE_OFF)
            visible = false;
        if ((flag & TransformLinePool.SEARCH_TRANSFORMABLE_ON) == TransformLinePool.SEARCH_TRANSFORMABLE_ON)
            transformable = true;
        if ((flag & TransformLinePool.SEARCH_TRANSFORMABLE_OFF) == TransformLinePool.SEARCH_TRANSFORMABLE_OFF)
            transformable = false;
        if ((flag & TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON) == TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON)
            bezierControl = true;
        if ((flag & TransformLinePool.SEARCH_BEZIER_CONTROL_PT_OFF) == TransformLinePool.SEARCH_BEZIER_CONTROL_PT_OFF)
            bezierControl = false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Vertex Space Methods">
    
    /** This will get the current position as a Rectangle2D.Float of the box around it.
     * @return Rectangle2D.Float of the current position (this is the box drawn for the vertex). Note, this function will create
     * a new Rectangle2D.Float every time it is called.
     */
    public Rectangle2D.Float getGluePoint() {
        return new Rectangle2D.Float(fCurrPt.x - Vertex.glueRadius,  fCurrPt.y - Vertex.glueRadius, Vertex.glueDiameter, Vertex.glueDiameter);
    }
    
    /** @return true if the x,y cordinates are within this vertex space, else false.
     */
    public boolean isVertexSpace(float x, float y) {
        fRectangle.setRect(fCurrPt.x - (Vertex.glueRadius * 2.0f),  fCurrPt.y - (Vertex.glueRadius * 2.0f), (Vertex.glueDiameter * 2.0f), (Vertex.glueDiameter * 2.0f));
        return fRectangle.contains(x, y);
    }
    
    /** @return true if the x,y cordinates are within this vertex space, else false.
     */
    public boolean isVertexSpace(TransformVertex v) {
        fRectangle.setRect(fCurrPt.x - (Vertex.glueRadius * 2.0f),  fCurrPt.y - (Vertex.glueRadius * 2.0f), (Vertex.glueDiameter * 2.0f), (Vertex.glueDiameter * 2.0f));
        return fRectangle.contains(v.fCurrPt.x, v.fCurrPt.y);
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Auto Link Vertex Methods ">
    
    /** @return true if this vertex connects to a line which does not have this vertex as an end point.
     */
    public boolean isAutoLinkVertex() {
        if (parametericLine == null)
            return false;
        return true;
    }    
    
    /** @return the line which this AutoLinkVertex is on.
     */
    public TransformAbstractLine getParametericLine() {
        return parametericLine;
    }
    
    /** @return the position on the parametericLine where this AutoLinkVertex resides.
     */
    public float getParametericT() {
        return parametericT;
    }
    
    /** This will set the auto link information.
     * @param parametericLine is the parameteric line which this vertex will connect to automatically.
     * @param parametericT is the parameteric position on the line which this vertex rests on.
     */
    public void setAutoLinkInfo(TransformAbstractLine parametericLine, float parametericT) {
        this.parametericT = parametericT;
        this.parametericLine = parametericLine;
    }
    
    // </editor-fold>            
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
    
    /** This will draw the glue point if enabled.
     */
    public final void drawGluePoint(Graphics2D g2d, boolean erase) {
        if (visible == false)
            return;
        
        if (transformable) {
            // Special color for transformable vertices.
            Color oldColor = g2d.getColor();
//            g2d.setColor(Vertex.colorGlue);
            g2d.setColor(Color.RED);
            
            // Erase first.
            if (erase) {
                fRectangle.setRect(fPrevPt.x - Vertex.glueRadius,  fPrevPt.y - Vertex.glueRadius, Vertex.glueDiameter, Vertex.glueDiameter);
                g2d.draw(fRectangle);
            }
            
            // Draw.
            fRectangle.setRect(fCurrPt.x - Vertex.glueRadius,  fCurrPt.y - Vertex.glueRadius, Vertex.glueDiameter, Vertex.glueDiameter);
            g2d.draw(fRectangle);
            
            // Restore color.
            g2d.setColor(oldColor);
        } else {
            // Erase first.
            if (erase) {
                fRectangle.setRect(fPrevPt.x - Vertex.glueRadius,  fPrevPt.y - Vertex.glueRadius, Vertex.glueDiameter, Vertex.glueDiameter);
                g2d.draw(fRectangle);
            }
            
            // Draw.
            fRectangle.setRect(fCurrPt.x - Vertex.glueRadius,  fCurrPt.y - Vertex.glueRadius, Vertex.glueDiameter, Vertex.glueDiameter);
            g2d.draw(fRectangle);
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public String toString() {
        return "{TransformVertex " + id + ": trans " + transformable + " pt " + fCurrPt.x + ", " + fCurrPt.y + "}";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Calculation Methods ">
    
    /** This will save the current point.
     */
    public void saveCurrent() {
        fSavePt.x = fCurrPt.x;
        fSavePt.y = fCurrPt.y;
    }
    
    /** This will calculate the radius and radian of this vertex from the ptCalculated
     * point. This is used to precalculate the radius and radian that will be
     * constant through out a rotation or a resizing of this vertex.
     * @param fCalculated is the point which is used to determine this vertex's
     * radius and radian.
     */
    public void calculate(FPointType fCalculated) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        float x = fCurrPt.x - fCalculated.x;
        float y = fCurrPt.y - fCalculated.y;
        
        if (x > -0.00001f && x < 0.00001f) {
            if (y < 0.0f)
                radian = (float)Math.PI + ((float)Math.PI / 2);
            else
                radian =  ((float)Math.PI / 2);
        }else {
            radian = (float)Math.atan(y/x);
            
            // Convert it to the II,III quadarants if the radian would be in those areas.
            if (x < 0)
                radian += Math.PI;
        }
        
        // Get the radius.
        radius = (float)Math.sqrt(x * x + y * y);
    }
    
    /** This will check v's vertex space against this vertex space.
     * @return true if the v vertex cordinates are within this vertex space, else false.
     */
    public boolean isVertexSpace(FPointType fPointType) {
        fRectangle.setRect(fCurrPt.x - (Vertex.glueRadius * 2.0f),  fCurrPt.y - (Vertex.glueRadius * 2.0f),
            (Vertex.glueDiameter * 2.0f), (Vertex.glueDiameter * 2.0f));
        return fRectangle.contains(fPointType.x, fPointType.y);
    }
    
    /** This will mirror the vertex horizontally on the x-axis of the ptCalculated.
     * @param fCalculated is the point which was calculated for the selected object.
     */
    public void mirrorHorizontal(FPointType fCalculated) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.y = fCurrPt.y + 2 * (fCalculated.y - fCurrPt.y);
    }
    
    /** This will mirror the vertex horizontally on the x-axis of the ptCalculated.
     * @param fCalculated is the point which was calculated for the selected object.
     */
    public void mirrorVertical(FPointType fCalculated) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.x = fCurrPt.x + 2 * (fCalculated.x - fCurrPt.x);
    }
    
    /** This will mirror the vertex from the line. Not to be used for degrees of 0, 90, 180, 270, 360.
     * @param line2D is the line which the vertex will be mirrored from.
     * @param radian is the angle which the user wants to mirror around.
     */
    public void mirrorDegree(Line2D.Float line2D, float radian) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Distance from current point to the line.
        float distance = (float)line2D.ptLineDist(fCurrPt.x, fCurrPt.y);
        
        // Do nothing if the vertex is on the line.
        distance *= 2;
        
        // Get the radian that the vector needs to go from the current point.
        int relativeCCW = line2D.relativeCCW(fCurrPt.x, fCurrPt.y);
        if (relativeCCW == 1)
            radian += ((float)Math.PI / 2);
        else
            radian -= ((float)Math.PI / 2);
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // This is the vector that the current position of the vertex needs to be translated to.
        float x = (float)Math.cos(radian) * distance;
        float y = (float)Math.sin(radian) * distance;
        
        // Update Current position.
        fCurrPt.x += x;
        fCurrPt.y += y;
    }
    
    /** This will resize the vertex by using the precomputed radian and radius from the calculated
     * point. The radian and radius are precomputed by using the calculateRadiusRadian().
     * @param fCalculated is the point which the radian and radius are used to compute the new point of this vertex.
     * @param fMultipler is the multipler of the radius for the x,y value of the new point.
     */
    public void resize(FPointType fCalculated, float xMultipler, float yMultipler) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.x = fCalculated.x + radius * xMultipler * (float)Math.cos(radian);
        fCurrPt.y = fCalculated.y + radius * yMultipler * (float)Math.sin(radian);
    }
    
    /** This will rotate the vertex by using the precomputed radian and radius from the calculated point.
     * The radian and radius are precomputed by using the calculateRadiusRadian().
     * @param ptCalculated is the point which the radian and radius are used to compute the new point of this vertex.
     * @param offsetRadian is the offset radian which this vertex should be rotated to.
     */
    public void rotate(FPointType fCalculated, float offsetRadian) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.x = fCalculated.x + radius * (float)Math.cos(radian + offsetRadian);
        fCurrPt.y = fCalculated.y + radius * (float)Math.sin(radian + offsetRadian);
    }
    
    /** This move the vertex to the position of caluclated position plus the offset. Uses the
     * saved point to calculate the new point. Make sure to save the current position before translating.
     * @param xOffset is the offset position from the saved position.
     * @param yOffset is the offset position from the saved position.
     */
    public void translate(float xOffset, float yOffset) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.x = fSavePt.x + xOffset;
        fCurrPt.y = fSavePt.y + yOffset;
    }
    
    /** This will move the vertex to the position moveTo.
     * @param fMoveTo is the position to move the vertex to.
     */
    public void translateTo(FPointType fMoveTo) {
        // Make sure vertex can be transformed.
        if (transformable == false)
            return;
        
        // Set the previous to the current.
        fPrevPt.x = fCurrPt.x;
        fPrevPt.y = fCurrPt.y;
        
        // Update Current position.
        fCurrPt.x = fMoveTo.x;
        fCurrPt.y = fMoveTo.y;
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
        float t = parametericT - ((TransformVertex)o).parametericT;
        if (t < 0)
            return -1;
        if (t > 0)
            return 1;
        return 0;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Interface InterfaceVertex ">
    
    // public int getId() - InterfacePoolObject declares it too, check that interface for use.
    
    public FPointType getPoint() {
        return fCurrPt;
    }
    
    // </editor-fold>

}
