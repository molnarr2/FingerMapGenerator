/*
 * Intersections.java
 * 
 * Created on Oct 22, 2007, 4:04:50 PM
 * 
 */

package mlnr.draw.area;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.AbstractLine;
import mlnr.draw.MathLineCurve;
import mlnr.draw.Vertex;
import mlnr.type.FPointType;

/** This is 
 *
 * @author Rob
 */
public class Intersections {
    /** This is the line which the intersectedLine intersected. */
    AbstractLine source;
    /** This is the line which intersected the source. */
    AbstractLine intersectedLine;
    /** Paramteric values on the source line where the intersectedLine intersected it. */
    LinkedList<Float> intersections;

    /** @param source is the line which the intersections are from.
     *  @param intersectedLine is the line which had intersected the 'source' line.
     *  @param intersections is a list of intersections on the 'source' line.
     */
    private Intersections(AbstractLine source, AbstractLine intersectedLine, LinkedList<Float> intersections) {
        this.source = source;
        this.intersectedLine = intersectedLine;
        this.intersections = intersections;
    }

    /** @param source is the line which the intersections are from.
     *  @param intersectedLine is the line which had intersected the 'source' line.
     *  @param vConnect is the vertex where the source and intersectedLine are connected at.
     */
    public Intersections(AbstractLine source, AbstractLine intersectedLine, Vertex vConnect) {
        this.source = source;
        this.intersectedLine = intersectedLine;
        this.intersections = new LinkedList<Float>();
        
        if (source.isFirstEndVertex(vConnect))
            this.intersections.add(new Float(0.0f));
        else if (source.isLastEndVertex(vConnect))
            this.intersections.add(new Float(1.0f));
        else
            throw new IllegalArgumentException("The Vertex " + vConnect + " does not connect to source: " + source +". intersectedLine: " + intersectedLine);
    }
    
    /** This will get all intersections on the 'source' that are intersected by 'intersected'. Note that what is returned is all paramteric positions on the 'source'.
     *  @param source is the first AbstractLine of the intersection.
     *  @param intersect is the second AbstractLine of the intersection.
     *  @return a new Intersections of all intersections between 'source' and 'intersect' or null if none.
     */
    public static Intersections intersect(AbstractLine source, AbstractLine intersect) {
        LinkedList<Float> ltParameteric = MathLineCurve.intersections(source.getShape(source.getFirstEndVertex()), intersect.getShape(intersect.getFirstEndVertex()));
        if (ltParameteric.isEmpty()) {
            return null;
        }
        return new Intersections(source, intersect, ltParameteric);
    }
    
    /**  This will see if any of the intersections are after start and equal to/before end.
     *  @param start is the starting position (can be greater than start). Intersections must be after this position.
     *  @param end is the ending position (can be less than start). Intersections must be before or equal to this position.
     * @return true if after start and equal to/before end.
     */
    public boolean containAfterBetween(float start, float end) {
        if (start < end) {
            for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
                float intersection = itr.next();                
                if (intersection > start && intersection <= end)
                    return true;
            }
        } else {
            for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
                float intersection = itr.next();                
                if (intersection < start && intersection >= end)
                    return true;
            }
        }
        
        return false;
    }
    
    /** This will get the closest intersection that is after start and equal to/before end.
     *  @param start is the starting position (can be greater than start). Intersections must be after this position.
     *  @param end is the ending position (can be less than start). Intersections must be before or equal to this position.
     *  @return the closest intersection that is after start and equal to/before end.
     *  @throws IllegalArgumentException if there are no intersections that meet the criteria.
     */
    public float getClosestToStart(float start, float end) {
        float closest = end;
        boolean bFoundOne = false;
        
        if (start < end) {
            for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
                float intersection = itr.next();                
                if (intersection > start && intersection <= end && intersection <= closest) {
                    bFoundOne = true;
                    closest = intersection;
                }
            }
        } else {
            for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
                float intersection = itr.next();                
                if (intersection < start && intersection >= end && intersection >= closest) {
                    bFoundOne = true;
                    closest = intersection;
                }
            }
        }
        
        // Found an intersection close to start.
        if (bFoundOne)
            return closest;
        
        // Didn't find one...
        throw new IllegalArgumentException("Nothing between start: " + start + ", end: " + end + ", for this " + this.toString());
    }
    
    /** This will creatae a list of TraversePosition at the intersected position.
     *  @param nextIntersectionParametric is the paramteric position to create the TraversePositions.
     *  @return a list of TraversePosition at the intersected position from the intersected line (not from the source). Will create 0,1, or 2 TraversePoints.
     */
    public LinkedList<TraversePosition> createIntersectPoints(float nextIntersectionParametric) {
        LinkedList<TraversePosition> ltPositions = new LinkedList<TraversePosition>();
        
        // Make sure the position exists.
        if (exists(nextIntersectionParametric) == false)
            return ltPositions;
        
        // Get the parametric position on the intersected line.
        FPointType fptSource = source.getParameterValue(nextIntersectionParametric);
        float parametricIntersected = intersectedLine.getClosestParameterT(fptSource);
        
        // Only create one even though it is very to the end.
        if (parametricIntersected > 0.998)
            ltPositions.add(new TraversePosition(intersectedLine, 1.0f, intersectedLine.getFirstEndVertex()));
        else if (parametricIntersected < 0.002)
            ltPositions.add(new TraversePosition(intersectedLine, 0.0f, intersectedLine.getLastEndVertex()));
        else {
            ltPositions.add(new TraversePosition(intersectedLine, parametricIntersected, intersectedLine.getFirstEndVertex()));
            ltPositions.add(new TraversePosition(intersectedLine, parametricIntersected, intersectedLine.getLastEndVertex()));
        }
        
        return ltPositions;
    }    
    
    /** @return true if this parametric position exists, or false it does not exist.
     */
    private boolean exists(float parametric) {
        for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
            float p = itr.next().floatValue();
            if (p == parametric)
                return true;
        }
        return false;
    }
            
    /** This will remove the intersection that it came from if this intersection is on the line in the tPrevious segment. The problem is that intersection
     *  parametric value is imprecise when performing the calculations from parametric to closest point back to paramteric position.
     *  @param tPrevious is the previous segement. Can be null. Used to check for the traversing to not allow it to go back to the same line
     *  at the same point. (The floating point value is imprecise there this will check to see if the parametric value is close enough to skip it.)
     */
    void cullPreviousLineIntersection(TraverseSegment tPrevious) {
        // Not on the previous line.
        if (tPrevious.getCurrentLine() != intersectedLine)
            return;
        
        // Now remove the intersected point that it came from.
        for (Iterator<Float> itr = intersections.iterator(); itr.hasNext(); ) {
            float p = itr.next().floatValue();
            float p2 = tPrevious.getNext().getPosition();
            if (Math.abs(p - p2) < 0.001)
                itr.remove();
        }
    }
    
    /** @return true if there are no intersections in this class.
     */
    public boolean isEmpty() {
        return intersections.isEmpty();
    }
    
    public String toString() {
        return "{Intersections: source: " + source.getId() + " intersections on source: " + intersections.toString() + " intersectedLine: " + intersectedLine.getId() + "}";
    }
}
