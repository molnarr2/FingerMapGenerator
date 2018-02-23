/*
 * SFPointType.java
 *
 * Created on November 25, 2006, 1:16 PM
 *
 */

package mlnr.type;

import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;

/** Sampled floating point type. This is a floating point used in sampling lines, beziers, and rmolnars.
 * @author Robert Molnar II
 */
public class SFPointType extends FPointType {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** This indicates that it is from a line sample. */
    public static final int SAMPLED_LINE = 2;
    /** This indicates that it is from a curve sample. */
    public static final int SAMPLED_CURVE = 4;
    /** This indicates that it is an end point sample (Vertex point). */
    public static final int END_POINT = 1;
    /** This indicates this is not a sample of anything. */
    public static final int NOT_SAMPLED = -1;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Used to store the end point sampled? and if it is a line or a curve. This is a bit vector. */
    private int data = NOT_SAMPLED;

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    /** @param x is the x position.
     * @param y is the y position.
     * @param data is the bit vector used to save information about this sampling.
     */
    private SFPointType(float x, float y, int data) {
        super(x, y);
        this.data = data;
    }
    
    /** @param x is the x position.
     * @param y is the y position.
     * @param endPoint is true if this is a sampled end point (vertex position).
     * @return a sampled point with information as being a sample from a line.
     */
    public static SFPointType sampledLine(float x, float y, boolean endPoint) {
        int data;
        if (endPoint)
            data = SAMPLED_LINE | END_POINT;
        else
            data = SAMPLED_LINE;        
        return new SFPointType(x, y, data);
    }
    
    /** @param fpt is the position.
     * @param endPoint is true if this is a sampled end point (vertex position).
     * @return a sampled point with information as being a sample from a line.
     */
    public static SFPointType sampledLine(FPointType fpt, boolean endPoint) {
        int data;
        if (endPoint)
            data = SAMPLED_LINE | END_POINT;
        else
            data = SAMPLED_LINE;        
        return new SFPointType(fpt.x, fpt.y, data);
    }

    /** @param x is the x position.
     * @param y is the y position.
     * @param endPoint is true if this is a sampled end point (vertex position).
     * @return a sampled point with information as being a sample from a curve.
     */
    public static SFPointType sampledCurve(float x, float y, boolean endPoint) {
        int data;
        if (endPoint)
            data = SAMPLED_CURVE | END_POINT;
        else
            data = SAMPLED_CURVE;        
        return new SFPointType(x, y, data);
    }

    /** @param fpt is the position.
     * @param endPoint is true if this is a sampled end point (vertex position).
     * @return a sampled point with information as being a sample from a curve.
     */
    public static SFPointType sampledCurve(FPointType fpt, boolean endPoint) {
        int data;
        if (endPoint)
            data = SAMPLED_CURVE | END_POINT;
        else
            data = SAMPLED_CURVE;        
        return new SFPointType(fpt.x, fpt.y, data);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Operation Methods ">
    
    /** @return true if this is a sampling from a line, else false it is not.
     */
    public boolean isLineSample() {
        if ((data & SAMPLED_LINE) == SAMPLED_LINE)
            return true;
        return false;
    }
    
    /** @return true if this is a sampling from a curve, else false it is not.
     */
    public boolean isCurveSample() {
        if ((data & SAMPLED_CURVE) == SAMPLED_CURVE)
            return true;
        return false;
    }
    
    /** @return true if this is a sampled end point, else false it is not a sampled end point.
     */
    public boolean isEndPoint() {
        if ((data & END_POINT) == END_POINT)
            return true;
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Public Static Methods ">
    
    /** This will convert a list of points to a list of lines.
     *  @param ltPoints is a list of points to convert.
     *  @return a list of lines starting from the first point and producing a line between each point until the last point.
     */
    public final static LinkedList<Line2D.Float> toLines(LinkedList<SFPointType> ltPoints) {
        LinkedList<Line2D.Float> ltLines = new LinkedList<Line2D.Float>();
        
        // Nothing to do to produce the lines.
        if (ltLines.size() <= 1)
            return ltLines;
        
        // Get the previous point.
        Iterator<SFPointType> itr = ltPoints.iterator();
        SFPointType prev = itr.next();
        
        // Now create lines.
        for (;itr.hasNext(); ) {
            SFPointType curr = itr.next();
            
            ltLines.add(new Line2D.Float(prev.x, prev.y, curr.x, curr.y));
            prev = curr;
        }
        
        return ltLines;
    }
    
    // </editor-fold>
}
