/*
 * IntersectSegment.java
 * 
 * Created on Oct 22, 2007, 3:30:23 PM
 * 
 */

package mlnr.draw.area;

import java.awt.Shape;
import mlnr.draw.AbstractLine;

/** This class represents a segment on a line to traverse and also contains the starting position for the
 *  next traversing.
 * @author Rob
 */
public class TraverseSegment {
    TraversePosition start;
    TraversePosition end;
    TraversePosition next;

    public TraverseSegment(TraversePosition start, TraversePosition end, TraversePosition next) {
        this.start = start;
        this.end = end;
        this.next = next;
    }

    /** @return the next starting position to traverse.
     */
    public TraversePosition getNext() {
        return next;
    }
    
    /** @return the line this segment represents.
     */
    public AbstractLine getCurrentLine() {
        return start.getLine();
    }
    
    /** This will see if it needs to update this segment to the starting position.
     *  @param startPosition is the position to see if this segment goes to.
     *  @return true if it updated the segment to go to the starting position, else false it was not updated.
     */
    public boolean performToStart(TraversePosition startPosition) {
        // Not even on the same line or going in the wrong direction.
        if (start.getLine() != startPosition.getLine() || startPosition.getDirection() != start.getDirection())
            return false;
        
        float startPos = startPosition.getPosition();
        float beginPos = start.getPosition();
        float endPos = end.getPosition();
        
        // Starting position is between the segment.
        if ((beginPos < endPos && beginPos <= startPos && endPos >= startPos) ||
                (beginPos > endPos && beginPos >= startPos && endPos <= startPos)) {
            end = startPosition;
            next = null;
            return true;
        }
        
        return false;
    }
    
    /** @return the shape of this TraverseSegment.
     */
    public Shape getShape() {
        return start.getLine().getShape(start.getPosition(), end.getPosition());
    }
    
    public String toStringCompact() {
        if (next != null)
            return "{Seg: [" + start.getLine().getId() + ": " + start.getPosition() + " to " + end.getPosition() +"] Next: [" + next.toStringCompact() + "]}";
        else
            return "{Seg: [" + start.getLine().getId() + ": " + start.getPosition() + " to " + end.getPosition() +"] Next: [" + null + "]}";
    }
    
    public String toString() {
        return "{TraverseSegment: start:[" + start + "] end:[" + end + "] next:[" + next + "]}";        
    }
}
