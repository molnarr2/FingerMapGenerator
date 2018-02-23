/*
 * TraversePosition.java
 * 
 * Created on Oct 22, 2007, 3:47:02 PM
 * 
 */

package mlnr.draw.area;

import mlnr.draw.AbstractLine;
import mlnr.draw.Vertex;

/** This contains a traverse position.
 * @author Rob
 */
public class TraversePosition {
    private AbstractLine line;
    private float position;
    private Vertex direction;

    /** This will creatae a traverse position but with no direction. This means that the point is at the end of the traversing on the line. The line will not be traveled at the position.
     * @param line is the line which the position is on.
     *  @param position is the parametric value on the 'line'.
     */
    public TraversePosition(AbstractLine line, float position) {
        this.line = line;
        this.position = position;
    }
    
    /** This will create a traverse position with direction.
     * @param line is the line which the position is on.
     *  @param position is the parametric value on the 'line'.
     *  @param direction is the vertex from the 'line' used as an indicator of which to traverse to next.
     */
    public TraversePosition(AbstractLine line, float position, Vertex direction) {        
        if (line.isFirstEndVertex(direction) == false && line.isLastEndVertex(direction) == false)
            throw new IllegalArgumentException("Line: " + line + " does not have the vertex: " + direction);
        
        this.line = line;
        this.position = position;
        this.direction = direction;
    }

    /** @return the line which the position is on.
     */
    public AbstractLine getLine() {
        return line;
    }

    /** @return the position which is the parametric value on the 'line',.
     */
    public float getPosition() {
        return position;
    }

    /** @return the direction which is from the ''line'  and is used as an indicator of which way to traverse to next,
     */
    public Vertex getDirection() {
        return direction;
    }
    
    /** @return the direction to as a parametric value. 0.0f or 1.0f.
     */
    public float getDirectionParametric() {
        if (line.isFirstEndVertex(direction))
            return 0.0f;
        else
            return 1.0f;
    }
    
    /** @return the vertex that is in the opposite direction.
     */
    public Vertex getOppositeDirection() {
        if (direction == null)
            throw new IllegalStateException("line: " + line + " direction is null.");
        if (line.isFirstEndVertex(direction))
            return line.getLastEndVertex();
        return line.getFirstEndVertex();
    }
    
    /** This will get the angle as counter clockwise angle. 
     *  @return the angle in radians as a counter clockwise angle.
     */
    public float getAngleCCW() {
        return line.getAngleCCW(position, direction);
    }
    
    /** This will create an inverted TraversePosition by changing the direction to the opposite vertex of the line.
     *  @return a new inverted TraversePosition by changing the direction to the opposite vertex of the line.
     */
    public TraversePosition createInvertDirection() {
        if (direction == null)
            throw new IllegalStateException("Unable to invert direction. Direction is null.");
        return new TraversePosition(line, position, line.getOppositeEndVertex(direction));
    }
    
    /** @param tPosition is used to see if it equals this TraversePosition.
     *  @return true if the line, position, and direction are the same.
     */
    public boolean equals(TraversePosition tPosition) {
        return (line == tPosition.line && position == tPosition.position && direction == tPosition.direction);
    }
    
    public String toString() {
        if (line.isFirstEndVertex(direction))
            return ("{TraversePosition: line id: " + line.getId() + ", position: " + position + ", direction: " + direction.getId() + "(0.0f)}");
        else
            return ("{TraversePosition: line id: " + line.getId() + ", position: " + position + ", direction: " + direction.getId() + "(1.0f)}");
    }
    
    public String toStringCompact() {
        if (line.isFirstEndVertex(direction))
            return ("{" +line.getId() + ": (" + position + " - 0.0f)}");
        else
            return ("{" +line.getId() + ": (" + position + " - 1.0f)}");
    }
    
}