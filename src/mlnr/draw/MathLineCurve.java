/*
 * MathLineCurve.java
 * 
 * Created on Oct 3, 2007, 4:11:05 PM
 * 
 */

package mlnr.draw;

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.type.SFPointType;

/** This class is used to perform mathematic calculations on lines and curve data.
 * @author Rob
 */
public class MathLineCurve {
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    /** To avoid a divide by zero error, whenever the divide is zero this is the value to use instead of zero. */
    private static final float ALMOST_ZERO = 0.00001f;
    /** This is the number of segments to break the curve into for the curve functions. */
    private static final int CURVE_SEGMENT = 20;
    /** Value used to determine if the parametric value is close enough to become a zero. */
    private static final float APPROX_ZERO = 0.0001f;
    /** Value used to determine if the parametric value is close enough to become an one. */
    private static final float APPROX_ONE = 0.9999f;
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    /** Class contains only public static functions. */
    private MathLineCurve() {
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" General Intersection methods ">
    /** This will see if the s1 intersects s2.
     * @param s1 is a Line2D.Float or a CubicCurve2D.Float. 
     * @param s2 is a Line2D.Float or a CubicCurve2D.Float
     *  @return true if they intersect else false they do not.
     */
    public final static boolean intersect(Shape s1, Shape s2) {
        if (s1 instanceof CubicCurve2D.Float && s2 instanceof CubicCurve2D.Float)
            return intersect((CubicCurve2D.Float) s1, (CubicCurve2D.Float) s2);
        else if (s1 instanceof CubicCurve2D.Float && s2 instanceof Line2D.Float)
            return intersect((CubicCurve2D.Float) s1, (Line2D.Float) s2);
        else if (s1 instanceof Line2D.Float && s2 instanceof CubicCurve2D.Float)
            return intersect((CubicCurve2D.Float) s2, (Line2D.Float) s1);

        return intersect((Line2D.Float) s1, (Line2D.Float) s2);
    }

    /** 
     *  @return true if they intersect else false they do not.
     */
    public final static boolean intersect(CubicCurve2D.Float fCurve1, CubicCurve2D.Float fCurve2) {
        // Perform a general bound intersection before an intensive calculation.
        if (fCurve1.getBounds2D().intersects(fCurve2.getBounds2D()) == false)
            return false;

        // Sample the curves.
        Line2D.Float[] lines1 = breakCurve(fCurve1, CURVE_SEGMENT);
        Line2D.Float[] lines2 = breakCurve(fCurve2, CURVE_SEGMENT);

        // Curve 1.
        for (int i = 0; i < lines1.length; i++) {
            // Curve 2.
            for (int j = 0; j < lines2.length; j++) {
                FPointType fpt = intersectPoint(lines1[i], lines2[j]);
                if (fpt == null)
                    continue;
                return true;
            }
        }

        return false;
    }

    /** 
     *  @return true if they intersect else false they do not.
     */
    public final static boolean intersect(CubicCurve2D.Float fCurve, Line2D.Float fLine) {
        // Perform a general bound intersection before an intensive calculation.
        if (fCurve.intersects(fLine.getBounds2D()) == false)
            return false;

        // Sample the curve.
        Line2D.Float[] lines = breakCurve(fCurve, CURVE_SEGMENT);

        // Curve.
        for (int i = 0; i < lines.length; i++) {
            // Get the intersected point.
            FPointType fpt = intersectPoint(lines[i], fLine);
            if (fpt == null)
                continue;
            return true;
        }

        // Return the list of points on the curve.
        return false;
    }

    /** 
     *  @return true if they intersect else false they do not.
     */
    public final static boolean intersect(Line2D.Float line1, Line2D.Float line2) {
        // Get the intersections.
        FPointType fpt = intersectPoint(line1, line2);

        // No intersections.
        if (fpt == null)
            return false;
        return true;
    }

    /** This will get all intersections between the two shapes and return the parameteric position of the intersection on the shape s1.
     * @param s1 is a Line2D.Float or a CubicCurve2D.Float. 
     *  @param s2 is a Line2D.Float or a CubicCurve2D.Float
     *  @return a list of parameteric positions of the intersection on the shape s1 in order from lowest to highest parameteric values.
     */
    public final static LinkedList<Float> intersections(Shape s1, Shape s2) {
        if (s1 instanceof CubicCurve2D.Float && s2 instanceof CubicCurve2D.Float)
            return intersectionsCurveCurve((CubicCurve2D.Float) s1, (CubicCurve2D.Float) s2);
        else if (s1 instanceof Line2D.Float && s2 instanceof Line2D.Float)
            return intersectionsLineLine((Line2D.Float) s1, (Line2D.Float) s2);
        else if (s1 instanceof CubicCurve2D.Float && s2 instanceof Line2D.Float)
            return intersectionsCurveLine((CubicCurve2D.Float) s1, (Line2D.Float) s2);
        else if (s1 instanceof Line2D.Float && s2 instanceof CubicCurve2D.Float)
            return intersectionsLineCurve((Line2D.Float) s1, (CubicCurve2D.Float) s2);

        throw new IllegalArgumentException("s1 is not a line or curve and/or s2 is not a line or curve." + s1 + " " + s2);
    }

    /** This will get all intersections between the two curves and return the parameteric position of the intersection on the curve fCurve1,
     *  @param fCurve1 is a CubicCurve2D.Float to be used as an intersection.
     *  @param fCurve2 is a CubicCurve2D.Float to be used as an intersection.
     *  @return a list of parameter positions of the intersection on the curve fCurve1 in order from lowest to highest parameter values.
     */
    private final static LinkedList<Float> intersectionsCurveCurve(CubicCurve2D.Float fCurve1, CubicCurve2D.Float fCurve2) {
        LinkedList<Float> ltFloats = new LinkedList<Float>();

        // Perform a general bound intersection before an intensive calculation.
        if (fCurve1.getBounds2D().intersects(fCurve2.getBounds2D()) == false)
            return ltFloats;

        // Sample the curves.
        Line2D.Float[] lines1 = breakCurve(fCurve1, CURVE_SEGMENT);
        Line2D.Float[] lines2 = breakCurve(fCurve2, CURVE_SEGMENT);

        // The size of the segments in paramtric terms.
        float divisions = 1.0f / CURVE_SEGMENT;

        // Curve 1.
        for (int i = 0; i < lines1.length; i++) {
            // Curve 2.
            for (int j = 0; j < lines2.length; j++) {
                FPointType fpt = intersectPoint(lines1[i], lines2[j]);
                if (fpt == null)
                    continue;

                // Get the parametric position on the curve from the point fpt from the curve 1.
                float parametric = divisions * closestParametric(lines1[i], fpt) + i * divisions;
                ltFloats.add(parametric);
            }
        }

        // Return the list of points on the curve.
        return ltFloats;
    }

    /** This will get all intersections between the curve and the line and return the paramteric position of the intersection on the curve fCurve.
     *  @param fCurve is a CubicCurve2D.Float to be used as an intersection.
     *  @param fLine is the Line2D.Float to be used as an intersection.
     *  @return a list of parameteric positions of the intersection on the curve fCurve in order from lowest to highest parameteric values.
     */
    private final static LinkedList<Float> intersectionsCurveLine(CubicCurve2D.Float fCurve, Line2D.Float fLine) {
        LinkedList<Float> ltFloats = new LinkedList<Float>();

        // Perform a general bound intersection before an intensive calculation.
        if (fCurve.intersects(fLine.getBounds2D()) == false)
            return ltFloats;

        // Sample the curve.
        Line2D.Float[] lines = breakCurve(fCurve, CURVE_SEGMENT);

        // The size of the segments in paramtric terms.
        float divisions = 1.0f / CURVE_SEGMENT;

        // Curve.
        for (int i = 0; i < lines.length; i++) {
            // Get the intersected point.
            FPointType fpt = intersectPoint(lines[i], fLine);
            if (fpt == null)
                continue;

            // Get the parametric position on the curve from the point fpt from the curve 1.
            float parametric = closestParametric(lines[i], fpt) * divisions + i * divisions;
            ltFloats.add(parametric);
        }

        // Return the list of points on the curve.
        return ltFloats;
    }

    /** This will get the intersection between the lines. The parametric position is from the source line.
     *  @param source is the line used to retrieve the parametric position.
     *  @param intersection is the line used to test if it intersects the source line.
     *  @return a list of parametric positions of the intersection on the source line.
     */
    private final static LinkedList<Float> intersectionsLineLine(Line2D.Float source, Line2D.Float intersection) {
        // Get the intersections.
        FPointType fpt = intersectPoint(source, intersection);

        // No intersections.
        if (fpt == null)
            return new LinkedList<Float>();

        LinkedList<Float> list = new LinkedList<Float>();
        list.add(pointToParametric(source, fpt));
        return list;
    }

    /** This will get all intersections between the curve and the line and return the paramteric position of the intersection on the curve fCurve.
     *  @param fCurve is a CubicCurve2D.Float to be used as an intersection.
     *  @param fLine is the Line2D.Float to be used as an intersection.
     *  @return a list of parameteric positions of the intersection on the curve fCurve in order from lowest to highest parameteric values.
     */
    private final static LinkedList<Float> intersectionsLineCurve(Line2D.Float fLine, CubicCurve2D.Float fCurve) {
        LinkedList<Float> ltFloats = new LinkedList<Float>();

        // Perform a general bound intersection before an intensive calculation.
        if (fCurve.intersects(fLine.getBounds2D()) == false)
            return ltFloats;

        // Sample the curve.
        Line2D.Float[] lines = breakCurve(fCurve, CURVE_SEGMENT);
        float divisions = 1.0f / CURVE_SEGMENT;

        // Curve.
        for (int i = 0; i < lines.length; i++) {
            // Get the intersected point.
            FPointType fpt = intersectPoint(lines[i], fLine);
            if (fpt == null)
                continue;

            // Get the parametric position on the line from the point fpt.
            float parametric = pointToParametric(fLine, fpt);
            ltFloats.add(parametric);
        }

        // Return the list of points on the curve.
        return ltFloats;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Line (Line2D.Float) methods ">
    
    /** This will sample the line and return a list of points on the line.
     * @param line is the line to be sampled. The start and end point will always be included in the list.
     * @param sampleSize is the number of points in the list.
     * @return list of samplied points.
     */
    public final static LinkedList<SFPointType> sampleLine(Line2D.Float line, int sampleSize) {
        LinkedList<SFPointType> list = new LinkedList<SFPointType>();
        float deltaX = line.x2 - line.x1;
        float deltaY = line.y2 - line.y1;        
        float step = 1.0f / (sampleSize - 1);
        
        for (float t=0; t <= 1.0f; t+=step) {
            if (t == 0)
                list.add(SFPointType.sampledLine(line.x1 + deltaX * t, line.y1 + deltaY * t, true));
            list.add(SFPointType.sampledLine(line.x1 + deltaX * t, line.y1 + deltaY * t, false));
        }
        
        if (list.size() == sampleSize)
            list.removeLast();
        list.add(SFPointType.sampledLine(line.x2, line.y2, true));
        
        return list;
    }
    
    /** This will compute the parametric value on the line where it is closest to the point.
     * @param line is the line used to get the parameteric position.
     * @param fpt is a point somewhere in space.
     * @return the closest parameter position on the line where the fpt distance is the closest.
     */
    public final static float closestParametric(Line2D.Float line, FPointType fpt) {
        float bottom = (line.x2 - line.x1);
        float top = (line.y2 - line.y1);
        float parametric = 0.0f;

        // Line is vertical.
        if (isAlmostZero(bottom)) {

            // Line is basically a point.
            if (isAlmostZero(top)) {
                if (fpt.distance(line.x1, line.y1) < fpt.distance(line.x2, line.y2))
                    return 0.0f;
                return 1.0f;
            }

            // Compute the parametric value.
            parametric = (fpt.y - line.y1) / top;

        } else if (isAlmostZero(top)) {  // Line is horizontal.            
            // Line is basically a point.
            if (isAlmostZero(bottom)) {
                if (fpt.distance(line.x1, line.y1) < fpt.distance(line.x2, line.y2))
                    return 0.0f;
                return 1.0f;
            }

            // Compute the parametric value.
            parametric = (fpt.x - line.x1) / bottom;

        } else { // Line has slope.

            // Slope values for the two lines. m2 is a 90 degree angle from the line to the point fpt.
            float m1 = top / bottom;
            float m2 = -1.0f * (bottom / top);

            // Get the b values.
            float b1 = line.y1 - m1 * line.x1;
            float b2 = fpt.y - m2 * fpt.x;

            // Compute the x position on the line.
            float tmp = m1 - m2;
            if (tmp == 0.0f)
                tmp = ALMOST_ZERO;
            float x = (b2 - b1) / tmp;

            // Finally calculate the t value.
            parametric = (x - line.x1) / bottom;
        }

        // If the value is negative (outside the segment) set it to 0.0f.
        if (parametric < APPROX_ZERO)
            parametric = 0.0f;
        else if (parametric > APPROX_ONE) // If the value is positive (outside the segment) set it to 1.0f.
            parametric = 1.0f;

        return parametric;
    }

    /** This will compute the position value on the line where it is closest to the point.
     * @param line is the line used to get the parameteric position.
     * @param fpt is a point somewhere in space.
     * @return the closest point on the line where the fpt distance is the closest.
     */
    public final static FPointType closestPoint(Line2D.Float line, FPointType fpt) {
        float bottom = (line.x2 - line.x1);
        float top = (line.y2 - line.y1);

        // Line is vertical.
        if (isAlmostZero(bottom)) {

            // Line is basically a point.
            if (isAlmostZero(top)) {
                if (fpt.distance(line.x1, line.y1) < fpt.distance(line.x2, line.y2))
                    return new FPointType(line.x1, line.y1);
                return new FPointType(line.x2, line.y2);
            }

            // If the y value is outside the line then return the end points.
            if (fpt.y < Math.min(line.y1, line.y2))
                return new FPointType(line.x1, Math.min(line.y1, line.y2));
            else if (fpt.y > Math.max(line.y1, line.y2))
                return new FPointType(line.x1, Math.max(line.y1, line.y2));

            return new FPointType(line.x1, fpt.y);

        } else if (isAlmostZero(top)) { // Line is horizontal.

            // Line is basically a point.
            if (isAlmostZero(bottom)) {
                if (fpt.distance(line.x1, line.y1) < fpt.distance(line.x2, line.y2))
                    return new FPointType(line.x1, line.y1);
                return new FPointType(line.x2, line.y2);
            }

            // If the x value is outside the line then return the end points.
            if (fpt.x < Math.min(line.x1, line.x2))
                return new FPointType(Math.min(line.x1, line.x2), line.y1);
            else if (fpt.x > Math.max(line.x1, line.x2))
                return new FPointType(Math.max(line.x1, line.x2), line.y1);

            return new FPointType(fpt.x, line.y1);

        }

        // Slope values for the two lines. m2 is a 90 degree angle from the line to the point fpt.
        float m1 = top / bottom;
        float m2 = -1.0f * (bottom / top);

        // Get the b values.
        float b1 = line.y1 - m1 * line.x1;
        float b2 = fpt.y - m2 * fpt.x;

        // Compute the x position on the line.
        float tmp = m1 - m2;
        if (tmp == 0.0f)
            tmp = ALMOST_ZERO;
        float x = (b2 - b1) / tmp;

        // Compute the y position on the line.
        float y = m1 * x + b1;

        // Finally calculate the x,y value.
        float t = (x - line.x1) / bottom;

        if (t < APPROX_ZERO)
            return new FPointType(line.x1, line.y1);
        else if (t > APPROX_ONE)
            return new FPointType(line.x2, line.y2);
        return new FPointType(x, y);
    }

    /** This will compute the intersection of the two line segments. This will only perform the calculation on the segment 
     * of the line and not the infinite line that they represent.
     * @param line1 is the first line.
     *  @param line2 is the second line.
     *  @return the point where the two lines intersect at, if they do not intersect at all then null is returned. 
     */
    public final static FPointType intersectPoint(Line2D.Float line1, Line2D.Float line2) {
        if (line1.intersectsLine(line2) == false)
            return null;

        // True if line is a vertical line.
        boolean line1yLine = false;
        boolean line2yLine = false;

        double temp = (line1.x2 - line1.x1);
        if (isAlmostZero(temp)) {
            line1yLine = true;
            temp = 0.0000001f;
        }
        double m1 = (line1.y2 - line1.y1) / temp;

        temp = (line2.x2 - line2.x1);
        if (isAlmostZero(temp)) {
            line2yLine = true;
            temp = 0.0000001f;
        }
        double m2 = (line2.y2 - line2.y1) / temp;

        double m_diff = m1 - m2;

        // Guard against the divide by zero error.
        if (m_diff == 0.0f)
            m_diff = ALMOST_ZERO;

        double b1 = line1.y1 - (m1 * line1.x1);
        double b2 = line2.y1 - (m2 * line2.x1);

        // If both are vertical lines, check to see if the end points are connected.
        if (line1yLine && line2yLine) {
            if (line1.x1 == line2.x1 && line1.y1 == line2.y1 ||
                    line1.x1 == line2.x2 && line1.y1 == line2.y2)
                return new FPointType(line1.x1, line1.y1);
            if (line1.x2 == line2.x1 && line1.y2 == line2.y1 ||
                    line1.x2 == line2.x2 && line1.y2 == line2.y2)
                return new FPointType(line1.x2, line1.y2);
            return null;
        } else if (line1yLine) {
            // line1 is a vertical line.
            temp = m2 * line1.x1 + b2;
            return new FPointType(line1.x1, (float) temp);

        } else if (line2yLine) {
            // line2 is a vertical line.
            temp = m1 * line2.x1 + b1;
            return new FPointType(line2.x1, (float) temp);
        }

        FPointType intersect = new FPointType();
        temp = (b2 - b1) / (m_diff);
        intersect.x = (float) temp;
        temp = (m1 * intersect.x) + b1;
        intersect.y = (float) temp;


        return intersect;
    }

    /** This will get the point on the line at the parametric position.
     *  @param fLine is the line used to get the parametric position.
     *  @param is the parametric value on the line.
     *  @return the point on the line where the parametric position is at.
     */
    public final static FPointType parametricToPoint(Line2D.Float fLine, float parametric) {
        FPointType point = new FPointType();

        point.x = fLine.x1 + (fLine.x2 - fLine.x1) * parametric;
        point.y = fLine.y1 + (fLine.y2 - fLine.y1) * parametric;

        return point;
    }

    /** This will get the parametric value on the fLine where the onLinePt is at. It is assumed the 'atPoint' is on the line. Undefined
     *  results if the point is not.
     *  @param fLine is the line used to get the parametric value.
     *  @param atPoint is assumed to be a point on the line (no checks are performed).
     *  @return the parametric position on the line. (value will always be between 0.0f and 1.0f including 0.0f and 1.0f)
     */
    public final static float pointToParametric(Line2D.Float fLine, FPointType atPoint) {
        // P = P1 + (P2 - P1) t
        // (P - P1) / (P2 - P1) = t

        // Get the division part.
        float div = fLine.x2 - fLine.x1;

        // Vertical line. Switch it over to using the y value.
        if (div < 1.0f && div > -1.0f) {
            div = fLine.y2 - fLine.y1;

            // Not a point.
            if (div != 0.0f) {
                // Get the top part.
                float top = atPoint.y - fLine.y1;

                float parametric = top / div;
                if (parametric < ALMOST_ZERO)
                    parametric = 0.0f;
                if (parametric > APPROX_ONE)
                    parametric = 1.0f;

                return parametric;
            } else
                // Both points are so close to each other that it is probably a point. Just use the x value.
                div = ALMOST_ZERO;
        }

        // Get the top part.
        float top = atPoint.x - fLine.x1;

        float parametric = top / div;
        if (parametric < ALMOST_ZERO)
            parametric = 0.0f;
        if (parametric > APPROX_ONE)
            parametric = 1.0f;

        return parametric;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Bezier (CubicCurve2D) curve methods ">
    /** This will get the bounds of a bezier curve. This is a much tighter bounds then what CubicCurve2D.Float provides, since that class simply
     * returns the bounds of the points and/or control points, thus it is not that accurate. However, this function is more resource hungry as it
     * will be far more accurate.
     *  @param bezierCurve is the bezier curve to get the bounds on.
     * @return exact bounds of the bezier curve.
     */
    public final static Rectangle2D.Float getBounds2D(CubicCurve2D.Float bezierCurve) {
        FPointType array[] = new FPointType[20];
        int i = 0;

        // Populate the array with points on the curve.
        for (float t = 0.0f; t <= 0.95f; t+=0.05f, i++)
            array[i] = parametricToPoint(bezierCurve, t);
        array[i] = parametricToPoint(bezierCurve, 1.0f);
        
        FPointType min = FPointType.min(array);
        FPointType max = FPointType.max(array);
        
        return new Rectangle2D.Float(min.x, min.y, max.x - min.x, max.y - min.y);
    }

    /** This will break the curve down into segments.
     *  @param curve is the bezier curve to break into multiple segments.
     *  @param segmentDivisions is the number of segments to break the curve into.
     *  @return Lines on the curve. The number will be equal to the number segmentDivisions. 
     */
    private final static Line2D.Float[] breakCurve(CubicCurve2D.Float curve, int segmentDivisions) {
        float t1, t2, t3, dpx, dpy;

        if (segmentDivisions < 1)
            throw new IllegalArgumentException("Must be more than 1 segment to divide. curve: " + curve + ", div: " + segmentDivisions);
        Line2D.Float[] lines = new Line2D.Float[segmentDivisions];

        // Set the points up.
        float x0 = (float) curve.getX1();
        float y0 = (float) curve.getY1();
        float x1 = (float) curve.getCtrlX1();
        float y1 = (float) curve.getCtrlY1();
        float x2 = (float) curve.getCtrlX2();
        float y2 = (float) curve.getCtrlY2();
        float x3 = (float) curve.getX2();
        float y3 = (float) curve.getY2();

        // Get the divisions of the curve and the count of points to sample (don't sample the end point).
        float divisons = 1.0f / segmentDivisions;
        float count = segmentDivisions - 1;

        // Get the starting point.
        float xPrev = curve.x1;
        float yPrev = curve.y1;

        // Begin sampling.
        int i = 0;
        for (float t = divisons; i < count; t += divisons, i++) {
            // Get the t's
            t1 = 1.0f - t;
            t2 = t1 * t1;
            t3 = t1 * t1 * t1;

            // Get the point on the bezier curve.
            dpx = t3 * x0;
            dpx += 3 * t * t2 * x1;
            dpx += 3 * t * t * t1 * x2;
            dpx += t * t * t * x3;
            dpy = t3 * y0;
            dpy += 3 * t * t2 * y1;
            dpy += 3 * t * t * t1 * y2;
            dpy += t * t * t * y3;

            lines[i] = new Line2D.Float(xPrev, yPrev, dpx, dpy);
            xPrev = dpx;
            yPrev = dpy;
        }

        // Add in the last line
        lines[segmentDivisions - 1] = new Line2D.Float(xPrev, yPrev, curve.x2, curve.y2);

        return lines;
    }

    /** This will compute the parametric value on the bezier where it is closest to the point.
     * @param curve is the curve used to get the parameteric position.
     * @param fpt is a point somewhere in space.
     * @return the closest parameter position on the bezier where the fpt distance is the closest.
     */
    public final static float closestParametric(CubicCurve2D.Float curve, FPointType fpt) {
        // Get the segment lines on the curve.
        Line2D.Float[] lines = breakCurve(curve, CURVE_SEGMENT);

        // The size of the segments in paramtric terms.
        float divisions = 1.0f / CURVE_SEGMENT;

        // Get the closest parametric point on the curve.
        float t = 0.0f; // Current paramtric value the segment starts out at.
        float closestT = 0.0f;
        float closestDistance = 1000000.0f;
        for (int i = 0; i < lines.length; i++) {
            // Get the closest point on the line.
            FPointType onLine = closestPoint(lines[i], fpt);

            // Get the distance from the point to the line and see if it closer than any other.
            float distance = onLine.distance(fpt);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestT = t + divisions * pointToParametric(lines[i], onLine); // Get the parametric value on the bezier curve (approximate from the line).
            }

            // Less error in the math doing it this way then incrementing.
            t = divisions * (i + 1);
        }

        if (closestT < APPROX_ZERO)
            return 0.0f;
        if (closestT > APPROX_ONE)
            return 1.0f;
        return closestT;
    }

    /** This will compute the closest point on the bezier where it is closest to the point 'fpt'.
     * @param curve is the curve used to get the closest point.
     * @param fpt is a point somewhere in space.
     * @return the closest point on the bezier where the fpt distance is the closest.
     */
    public final static FPointType closestPoint(CubicCurve2D.Float curve, FPointType fpt) {
        float parametric = closestParametric(curve, fpt);
        return parametricToPoint(curve, parametric);
    }

    /** This will get the control points for the divided bezier curve. It will calculate the two sets of control points needed for the bezier division.
     * @param curve is the bezier bezier curve.
     *  @param parametricPos is the parameteric position to divide the curve at.
     * @return 4 FPointTypes where the first two are the first half of the bezier curve that was divided and the last two are the last half of the
     * bezier curve that was divied. It goes like this: first control, last control, first control, and then last control.
     */
    public final static FPointType[] divideControlPoints(CubicCurve2D.Float curve, float parametricPos) {
        FPointType ptReturn[] = new FPointType[4];
        float xPt1, yPt1, xPt2, yPt2, xPt3, yPt3, xPt4, yPt4;
        float xTemp, yTemp;
        float t;

        float x1 = (float) curve.getX1();
        float y1 = (float) curve.getY1();
        float ctrlx1 = (float) curve.getCtrlX1();
        float ctrly1 = (float) curve.getCtrlY1();
        float ctrlx2 = (float) curve.getCtrlX2();
        float ctrly2 = (float) curve.getCtrlY2();
        float x2 = (float) curve.getX2();
        float y2 = (float) curve.getY2();

        t = parametricPos;

        // First control point calculated.
        xTemp = ((ctrlx1 - x1) * t);
        xPt1 = x1 + xTemp;
        yPt1 = y1 + ((ctrly1 - y1) * t);

        // Mid point between vControl1 - vControl2
        xTemp = ctrlx1 + ((ctrlx2 - ctrlx1) * t);
        yTemp = ctrly1 + ((ctrly2 - ctrly1) * t);

        // Second control point calculated.
        xPt2 = xPt1 + ((xTemp - xPt1) * t);
        yPt2 = yPt1 + ((yTemp - yPt1) * t);

        // Now get the other half of the bezier curve.
        t = 1 - parametricPos;

        // First control point calculated.
        xPt3 = x2 + ((ctrlx2 - x2) * t);
        yPt3 = y2 + ((ctrly2 - y2) * t);

        // Second control point calculated.
        xPt4 = xPt3 + ((xTemp - xPt3) * t);
        yPt4 = yPt3 + ((yTemp - yPt3) * t);

        // Now set the points up.
        ptReturn[0] = new FPointType(xPt1, yPt1);
        ptReturn[1] = new FPointType(xPt2, yPt2);
        ptReturn[2] = new FPointType(xPt4, yPt4);
        ptReturn[3] = new FPointType(xPt3, yPt3);

        return ptReturn;
    }

    /** This will get the point on the curve at the parameter position t.
     * @param bezierCurve is the bezier curve.
     * @param t is the parameter position t.
     * @return the point on the curve at the parameter position t.
     */
    public static FPointType parametricToPoint(CubicCurve2D.Float bezierCurve, float t) {
        // Set the points up.
        float x0 = (float) bezierCurve.getX1();
        float y0 = (float) bezierCurve.getY1();
        float x1 = (float) bezierCurve.getCtrlX1();
        float y1 = (float) bezierCurve.getCtrlY1();
        float x2 = (float) bezierCurve.getCtrlX2();
        float y2 = (float) bezierCurve.getCtrlY2();
        float x3 = (float) bezierCurve.getX2();
        float y3 = (float) bezierCurve.getY2();

        // Get the t's
        float t1 = 1.0f - t;
        float t2 = t1 * t1;
        float t3 = t1 * t1 * t1;

        // Get the point on the bezier curve.
        float dpx, dpy;
        dpx = t3 * x0;
        dpx += 3 * t * t2 * x1;
        dpx += 3 * t * t * t1 * x2;
        dpx += t * t * t * x3;
        dpy = t3 * y0;
        dpy += 3 * t * t2 * y1;
        dpy += 3 * t * t * t1 * y2;
        dpy += t * t * t * y3;

        return new FPointType(dpx, dpy);
    }

    /** This will convert a point on the curve to the paramteric value t.
     * @param bezierCurve is the bezier curve.
     *  @param atPoint is the point on the curve to convert to a parametric value t.
     *  @return the parametric value where the 'atPoint' is at on the curve. (value will always be between 0.0f and 1.0f including 0.0f and 1.0f)
     */
    public static float pointToParametric(CubicCurve2D.Float bezierCurve, FPointType atPoint) {
        // Break the curve into lines.
        Line2D.Float[] ltLines = breakCurve(bezierCurve, CURVE_SEGMENT);

        // The size of the segments in paramtric terms.
        float divisions = 1.0f / CURVE_SEGMENT;

        // Search all lines.
        float distance = closestPoint(ltLines[0], atPoint).distance(atPoint);
        float parametric = closestParametric(ltLines[0], atPoint);
        for (int i = 1; i < ltLines.length; i++) {
            // If distance is closer on this line segment then previous then use it.
            float currentDistance = closestPoint(ltLines[i], atPoint).distance(atPoint);
            if (currentDistance < distance) {
                distance = currentDistance;
                parametric = divisions * closestParametric(ltLines[i], atPoint) + i * divisions;
            }
        }

        if (parametric < ALMOST_ZERO)
            parametric = 0.0f;
        if (parametric > APPROX_ONE)
            parametric = 1.0f;

        return parametric;
    }

    public final static LinkedList<SFPointType> sampleCurve(CubicCurve2D.Float bezierCurve, int sampleSize) {
        return sampleCurve(bezierCurve, new FPointType(bezierCurve.x1, bezierCurve.y1), new FPointType(bezierCurve.x2, bezierCurve.y2),
                0.0f, 1.0f, sampleSize);
    }
    
    /** This will sample this curve from fpt1 to fpt2.
     * @param bezierCurve is the bezier curve used to get the sampled points from.
     * @param fpt1 is the start of the sampling.
     * @param fpt2 is the end of the sampling.
     * @param beginT is the start of samping in parameteric form. beginT can be greater than endT (reverse sampling).
     * @param endT is the end of samping in parameteric form. endT can be less than beginT (reverse sampling).
     * @param sampleSize is the number of points which this entire AbstractLine should have.
     * @return a list of sampled points from this curve should atleast sampleSize number of points. 
     * However, if fpt1 or fpt2 is part of the AbstractLine then it will contain less points. fpt1 and fpt2 are in
     * this list as the first and last points.
     */
    public final static LinkedList<SFPointType> sampleCurve(CubicCurve2D.Float bezierCurve, FPointType fpt1, FPointType fpt2,
            float beginT, float endT, int sampleSize) {
        LinkedList<SFPointType> ltPoints = new LinkedList<SFPointType>();

        // Set the points up.
        float x0 = (float) bezierCurve.getX1();
        float y0 = (float) bezierCurve.getY1();
        float x1 = (float) bezierCurve.getCtrlX1();
        float y1 = (float) bezierCurve.getCtrlY1();
        float x2 = (float) bezierCurve.getCtrlX2();
        float y2 = (float) bezierCurve.getCtrlY2();
        float x3 = (float) bezierCurve.getX2();
        float y3 = (float) bezierCurve.getY2();

        float tDelta = 1.0f / sampleSize;

        // Search for the closest distance on the bezier curve.
        float t1, t2, t3, dpx, dpy = 0.0f;
        if (beginT < endT)
            for (float t = beginT; t < endT; t += tDelta) {
                // Get the t's
                t1 = 1.0f - t;
                t2 = t1 * t1;
                t3 = t1 * t1 * t1;

                // Get the point on the bezier curve.
                dpx = t3 * x0;
                dpx += 3 * t * t2 * x1;
                dpx += 3 * t * t * t1 * x2;
                dpx += t * t * t * x3;
                dpy = t3 * y0;
                dpy += 3 * t * t2 * y1;
                dpy += 3 * t * t * t1 * y2;
                dpy += t * t * t * y3;

                // Add the point in.
                ltPoints.add(SFPointType.sampledCurve(dpx, dpy, false));
            }
        else
            for (float t = beginT; t > endT; t -= tDelta) {
                // Get the t's
                t1 = 1.0f - t;
                t2 = t1 * t1;
                t3 = t1 * t1 * t1;

                // Get the point on the bezier curve.
                dpx = t3 * x0;
                dpx += 3 * t * t2 * x1;
                dpx += 3 * t * t * t1 * x2;
                dpx += t * t * t * x3;
                dpy = t3 * y0;
                dpy += 3 * t * t2 * y1;
                dpy += 3 * t * t * t1 * y2;
                dpy += t * t * t * y3;

                // Add the point in.
                ltPoints.add(SFPointType.sampledCurve(dpx, dpy, false));
            }

        // pop the first and last points and add the first and last points.
        if (!ltPoints.isEmpty())
            ltPoints.removeFirst();
        if (!ltPoints.isEmpty())
            ltPoints.removeLast();
        ltPoints.addFirst(SFPointType.sampledCurve(fpt1, true));
        ltPoints.addLast(SFPointType.sampledCurve(fpt2, true));

        return ltPoints;
    }

    /** This will get the length from parameter beginT to endT for a given curve.
     * @param curve is the bezier curve used to get the sampled points from.
     * @param beginT is the start of samping in parameteric form. beginT can be greater than endT (reverse sampling).
     * @param endT is the end of samping in parameteric form. endT can be less than beginT (reverse sampling).
     * @return length from beginT to endT.
     */
    public final static float lengthCurve(CubicCurve2D.Float curve, float beginT, float endT) {
        FPointType start = parametricToPoint(curve, beginT);
        FPointType end = parametricToPoint(curve, endT);

        LinkedList<SFPointType> list = sampleCurve(curve, start, end, beginT, endT, 25);
        float length = 0.0f;
        
        // Calculate the length.
        Iterator<SFPointType> itr = list.iterator();
        SFPointType ptPrev = itr.next();
        for ( ;itr.hasNext(); ) {
            SFPointType ptNext = itr.next();
            length += ptNext.distance(ptPrev.x, ptPrev.y);
            ptPrev = ptNext;
        }
        
        return length;
    }
    
    /** This will sub divide the curve. It will get a partial part of the curve from start to end.
     *  @param curve is the curve used to create a sub-curve.
     *  @param start is the starting position on the curve. Can be more than end. parametric value.
     *  @param end is the ending position on the curve. Can be less than start. paramteric value.
     *  @return a sub-curve from the 'curve' beginning at 'start' and ending at 'to'.
     */
    public final static CubicCurve2D.Float subDivide(CubicCurve2D.Float curve, float start, float end) {
        // Flip the parametric values so that start is before to.
        if (start > end) {
            float temp = end;
            end = start;
            start = temp;
        }

        // There is no curve...
        if (start == 1.0f)
            return new CubicCurve2D.Float((float) curve.getX1(), (float) curve.getY1(), (float) curve.getX1(), (float) curve.getY1(),
                    (float) curve.getX2(), (float) curve.getY2(), (float) curve.getX2(), (float) curve.getY2());

        // Get the points on the curve for 'start' and 'to'.
        FPointType fptStart = parametricToPoint(curve, start);
        FPointType fptTo = parametricToPoint(curve, end);

        // Get the new divided curve's control points.
        FPointType[] arrPoints = divideControlPoints(curve, start);

        // Create the curve from start to the end of the from curve.
        CubicCurve2D.Float partialCurve = new CubicCurve2D.Float(fptStart.x, fptStart.y, arrPoints[2].x, arrPoints[2].y, arrPoints[3].x, arrPoints[3].y, curve.x2, curve.y2);

        // Get the final divided curve's control points. (to is minus the start because the partialCurve starts at the 'start' point.
        end = (end - start) / (1.0f - start);
        if (end == 1.0f)
            return partialCurve;

        arrPoints = divideControlPoints(partialCurve, end);

        return new CubicCurve2D.Float(fptStart.x, fptStart.y, arrPoints[0].x, arrPoints[0].y, arrPoints[1].x, arrPoints[1].y, fptTo.x, fptTo.y);
    }

    /** This will perform an exhaustive intersection between the bezier curve and the rectangle r.
     * @param bezierCurve is the bezier curve used to test against the rectangle to see if it intersects it.
     * @param r is the rectangle to test against to see if the curve intersects it.
     * @return true if the curve intersects the rectangle or false it does not.
     */
    public final static boolean intersectsBezier(CubicCurve2D.Float bezierCurve, Rectangle2D.Float r) {
        Line2D.Float[] lines = breakCurve(bezierCurve, CURVE_SEGMENT);
        for (int i = 0; i < lines.length; i++) {
            if (r.intersectsLine(lines[i]))
                return true;
        }

        return false;
    }

    /** This will invert the curve. Where the starting position becomes the end position. The start is flipped for the end.
     *  @param bezierCurve is the curve to flip it's start and end positions. (Does not modify the curve in any way.)
     *  @return the curve inverted.
     */
    public static CubicCurve2D.Float invert(CubicCurve2D.Float bezierCurve) {
        float x0 = (float) bezierCurve.getX1();
        float y0 = (float) bezierCurve.getY1();
        float x1 = (float) bezierCurve.getCtrlX1();
        float y1 = (float) bezierCurve.getCtrlY1();
        float x2 = (float) bezierCurve.getCtrlX2();
        float y2 = (float) bezierCurve.getCtrlY2();
        float x3 = (float) bezierCurve.getX2();
        float y3 = (float) bezierCurve.getY2();

        return new CubicCurve2D.Float(x3, y3, x2, y2, x1, y1, x0, y0);
    }
    // </editor-fold>
    /** @param value is used to see if it is so close to zero to be considered zero.
     *  @return true if the value is so close to zero, or false it is not.
     */
    private final static boolean isAlmostZero(float value) {
        if (value < 0.0000001f && value > -0.0000001f)
            return true;

        return false;
    }

    /** @param value is used to see if it is so close to zero to be considered zero.
     *  @return true if the value is so close to zero, or false it is not.
     */
    private final static boolean isAlmostZero(double value) {
        if (value < 0.0000001 && value > -0.0000001)
            return true;

        return false;
    }

    public static void main(String[] args) {
        Line2D.Float fLine1 = new Line2D.Float(40, 40, 80, 40);
        Line2D.Float fLine2 = new Line2D.Float(0, 0, 100, 100);
        CubicCurve2D.Float fBezier = new CubicCurve2D.Float(0, 0, 0, 50, 50, 50, 50, 0);

        FPointType fpt = parametricToPoint(fLine1, 0.25f);
        System.out.println("line: paramtericToPoint() = " + fpt);

        fpt = closestPoint(fLine1, new FPointType(45, 0));
        System.out.println("line: closestPoint() = " + fpt);

        float t = closestParametric(fLine1, new FPointType(45, 0));
        System.out.println("line: closestParametric() = " + t);

        fpt = intersectPoint(fLine1, fLine2);
        System.out.println("line: intersectPoint() = " + fpt);

        fpt = parametricToPoint(fBezier, 0.25f);
        System.out.println("bezier: paramtericToPoint() = " + fpt);

        t = pointToParametric(fLine1, parametricToPoint(fLine1, 0.25f));
        System.out.println("bezier: should be 0.25 = " + t);

        t = pointToParametric(fBezier, parametricToPoint(fBezier, 0.25f));
        System.out.println("bezier: should be 0.25 = " + t);

        // closestParametric
        t = closestParametric(fBezier, new FPointType(40, 5));
        System.out.println("bezier: closestParametric = " + t);

        fLine1 = new Line2D.Float(45.33654f, 46.25f, 45.33654f, 58.46154f);
        fLine2 = new Line2D.Float(38.942307f, 56.41026f, 49.39103f, 56.41026f);

        fpt = intersectPoint(fLine1, fLine2);
        t = pointToParametric(fLine1, fpt);
        float t2 = pointToParametric(fLine2, fpt);
        System.out.println("line: intersectPoint() = " + fpt + " parametric on line1: " + t + " parametric on line2: " + t2);

        fpt = intersectPoint(fLine2, fLine1);
        t = pointToParametric(fLine1, fpt);
        t2 = pointToParametric(fLine2, fpt);
        System.out.println("line: intersectPoint() = " + fpt + " parametric on line1: " + t + " parametric on line2: " + t2);

        fLine1.setLine(60.528847f, 58.46154f, 60.528847f, 46.25f);
        fpt = new FPointType(60.528847f, 46.25f);
        t = closestParametric(fLine1, fpt);
        System.out.println("paramteric: " + t + " fpt: " + fpt);

        CubicCurve2D.Float fBezier1 = new CubicCurve2D.Float(49.825176f, 40.00004f, 43.77476f, 40.00004f, 39.02098f, 44.74976f, 39.02098f, 49.89511f);
        CubicCurve2D.Float fBezier2 = new CubicCurve2D.Float(42.65734f, 40.000004f, 42.65734f, 42.690857f, 45.811146f, 45.17483f, 49.825176f, 45.17483f);

        System.out.println("INTERSECTIONS===");
        System.out.println("intersections: " + intersectionsCurveCurve(fBezier1, fBezier2).toString());
        System.out.println("intersections: " + intersectionsCurveCurve(fBezier2, fBezier1).toString());
    }

}
