/*
 * ProduceTraverseSegmentOp.java
 * 
 * Created on Oct 22, 2007, 3:52:39 PM
 * 
 */

package mlnr.draw.area;

import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.DrawingGraph;

/** This class is used to produce a TraverseSegment from the TraversePosition.
 *
 * @author Rob
 */
public class ProduceTraverseSegmentOp {
    
    /** This will create a TraverseSegment from the position tPosition to the next intersection.
     *  @param tStart is the position to create a TraverseSegment from. 
     *  @param ltGraphs is a list of graphs which could possibly have an intersection.
     *  @param tPrevious is the previous segement. Can be null. Used to check for the traversing to not allow it to go back to the same line
     *  at the same point. (The floating point value is imprecise there this will check to see if the parametric value is close enough to skip it.)
     */
    public static final TraverseSegment traverseSegment(TraversePosition tStart, LinkedList<DrawingGraph> ltGraphs, TraverseSegment tPrevious) {
        // This will get a list of all intersections on the line from all lines in the list of the drawing graphs.
        LinkedList<Intersections> ltIntersections = DrawingGraph.getIntersectionLines(ltGraphs, tStart.getLine(), tStart.getDirection());
        
        // This will remove all intersections that are completely before the tPosition's start position.
        cullTheIntersections(ltIntersections, tStart, tPrevious);
        
        // Going to a dead-end line. Time to start backward on it.
        if (ltIntersections.isEmpty()) {
            TraversePosition end = new TraversePosition(tStart.getLine(), tStart.getDirectionParametric());
            TraversePosition next = new TraversePosition(tStart.getLine(), tStart.getDirectionParametric(), tStart.getOppositeDirection());
            return new TraverseSegment(tStart, end, next);
        }
        
        // This is the next intersection position on the line from tStart (current line).
        float nextIntersectionParametric = traverseNextParameteric(ltIntersections, tStart);
        
        // This is the next TraversePosition on the line from tStart (current line).
        TraversePosition nextOnLine = new TraversePosition(tStart.getLine(), nextIntersectionParametric, tStart.getDirection());
        
        // Create a list of all TraversePosition at the intersection position.
        LinkedList<TraversePosition> ltIntersectPoints = createIntersectPoints(ltIntersections, nextIntersectionParametric);
        
        // This will get the next TraversePosition to traverse to.
        TraversePosition next = compareNextTraversePoints(ltIntersectPoints, nextOnLine);        
        
        // Create a traverse segment.
        return new TraverseSegment(tStart, nextOnLine, next);
    }

    /** This will compare the list of TraversePosition to the 'tStart' position and will get the one with the smallest positive angle from it.
     *  @param ltIntersectPoints is a list of TraversePosition that are possible places to travel to.
     *  @param tAt is the position on the current line which the returned position will be the same point (x,y) but will be on a different line (however, there is a
     *  possible situation where the TraversePosition could be the same line and in the same direction). The direction is reverse to accurately get the correct
     *  TraversePosition.
     *  @return the TraversePosition to travel to next. It will be the one which has the smallest positive angle from the tStart.
     */
    private static final TraversePosition compareNextTraversePoints(LinkedList<TraversePosition> ltIntersectPoints, TraversePosition tAt) {
        TraversePosition next = null;
        float angleDifference =(float)Math.PI * 2.1f;
        
        if (ltIntersectPoints.isEmpty())
            throw new IllegalArgumentException("Nothing in the list of possible places to traverse.");
        
        // The position at 'tAt' needs to point away, therefore it is inverted. This angle will now be degree zero.
        float atOffset = tAt.createInvertDirection().getAngleCCW();
        
        for (Iterator<TraversePosition> itr = ltIntersectPoints.iterator(); itr.hasNext(); ) {
            TraversePosition tPosition = itr.next();
            float posAngle = tPosition.getAngleCCW();
            
            // Get the angle difference. If the tPosition angle is before the tAt angle then add 360 degrees to it.
            float difference = posAngle - atOffset;
            if (difference < 0.0f)
                difference += (float)Math.PI * 2;
            
            // This is the next position to traverse to.
            if (difference < angleDifference) {
                angleDifference = difference;
                next = tPosition;
            }                            
        }
        
        return next;
    }
    
    /** This will remove all intersections that are completely before the tPosition's start position and it will also remove the intersection 
     *  that is the one where it previously traversed.
     *  @param ltIntersections is a list of Intersections from the tPosition's line.
     *  @param tPosition is the current traversed position at. It is used to traverse to the next position.
     *  @param tPrevious is the previous segement. Can be null. Used to check for the traversing to not allow it to go back to the same line
     *  at the same point. (The floating point value is imprecise there this will check to see if the parametric value is close enough to skip it.)
     */
    private static final void cullTheIntersections(LinkedList<Intersections> ltIntersections, TraversePosition tPosition, TraverseSegment tPrevious) {
        float start = tPosition.getPosition();
        float end = tPosition.getDirectionParametric();
        
        for (Iterator<Intersections> itr = ltIntersections.iterator(); itr.hasNext(); ) {
            Intersections intersections = itr.next();
            
            // Remove the intersection that it came from.
            if (tPrevious != null) {
                intersections.cullPreviousLineIntersection(tPrevious);            
                if (intersections.isEmpty()) {
                    itr.remove();
                    continue;
                }
            }
            
            // After start, but less than/equal to end.
            if (intersections.containAfterBetween(start, end) == false) {
                itr.remove();
                continue;
            }
        }
    }    
    
    /** This will create a list of all possible ways to go at the 'nextIntersectionParametric' position.
     *  @param ltIntersections is a list of Intersections as raw data to be converted to TraversePositions but only the intersections at the 'nextIntersectionPatamertic'.
     *  @param nextIntersectionParamteric is the position at to create the TraversePositions.
     *  @return a list of all possible ways to got at the 'nextIntersectionParametric' position.
     */
    private static final LinkedList<TraversePosition> createIntersectPoints(LinkedList<Intersections> ltIntersections, float nextIntersectionParametric) {
        LinkedList<TraversePosition> ltTraversePositions = new LinkedList<TraversePosition>();
            
        // Create the Intersected points.
        for (Iterator<Intersections> itr = ltIntersections.iterator(); itr.hasNext(); ) {
            Intersections intersections = itr.next();
            ltTraversePositions.addAll(intersections.createIntersectPoints(nextIntersectionParametric));
        }
        
        return ltTraversePositions;
    }

    /** This will get the next intersection to traverse to. It is the closest position after the 'tPosition'.
     *  @param ltIntersections is a list of Intersections from the tPosition's line.
     *  @param tPosition is the current traversed position at. It is used to traverse to the next position.
     *  @return the next intersection position to traverse to.
     *  @throws IllegalArgumentException if there are no intersections that meet the criteria.
     */
    private static final float traverseNextParameteric(LinkedList<Intersections> ltIntersections, TraversePosition tPosition) {
        float start = tPosition.getPosition();
        float end = tPosition.getDirectionParametric();
        float closest = end;
        boolean bFoundOne = false;
        
        if (start < end) {
            for (Iterator<Intersections> itr = ltIntersections.iterator(); itr.hasNext(); ) {
                Intersections intersections = itr.next();
                
                // Get the closest intersection position.
                float closestIntersection = intersections.getClosestToStart(start, end);                
                if (closestIntersection <= closest) {
                    bFoundOne = true;
                    closest = closestIntersection;
                }
            }
        } else {
            for (Iterator<Intersections> itr = ltIntersections.iterator(); itr.hasNext(); ) {
                Intersections intersections = itr.next();
                
                // Get the closest intersection position.
                float closestIntersection = intersections.getClosestToStart(start, end);                
                if (closestIntersection >= closest) {
                    bFoundOne = true;
                    closest = closestIntersection;
                }
            }
        }
        
        // Found an intersection close to start.
        if (bFoundOne)
            return closest;
        
        // Didn't find one...
        throw new IllegalArgumentException("Didn't find next position to traverse to. Position: " + tPosition + " ltIntersections: " + ltIntersections);
    }
    
}
