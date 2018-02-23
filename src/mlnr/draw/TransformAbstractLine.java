/*
 * TransformAbstractLine.java
 *
 * Created on April 27, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.type.SFPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;
import org.w3c.dom.Element;

/** This class is used to represent a line, bezier, or rmolnar as transformable.
 * @author Robert Molnar 2
 */
abstract public class TransformAbstractLine implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Id of this object. */
    int id=0;
    
    /** This is the first end TransformVertex. */
    protected TransformVertex vEnd1;
    
    /** This is the other end TransformVertex. */
    protected TransformVertex vEnd2;
    
    /** This is the line pointed to in the DrawingLinePool. Only used when selecting lines and then adding
      * them back into the DrawingLinePool. See DrawingLinePool.getSelectedItems(). */
    private int lineToId = 0;
    
    /** Flag used to indicate if it has been visited yet. */
    protected boolean visited = false;
    
    /** Flag used to indicate if it is selected or not yet. */
    protected boolean selected = false;
    
    /** This is the list of auto linked vertices that were auto linked to this line. Placed in linkedList
     * in no order. */
    protected LinkedList<TransformVertex> ltAutoLinkedVertex = null;
    
    /** This is the list of visited segments. */
    protected LinkedList<VisitSegment> ltVisitSegment = null;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    public TransformAbstractLine(TransformVertex vEnd1, TransformVertex vEnd2) {
        this.vEnd1 = vEnd1;
        this.vEnd2 = vEnd2;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" State/Status Methods ">
    
    /** This will see if this TransformAbstractLine contains the statuses of the matching flag. Note
     * that not all of the TransformLinePool.SEARCH_* flags can be used. Check those flag comments to
     * see which ones will work.
     * @param flag is used to see if this TransformAbstractLine is of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return true if this TransformAbstractLine matches the flag values or false it does not match the flag values.
     */
    public final boolean is(int flag) {
        // Does the flags match?
        if (flag == DrawingLinePool.SEARCH_OFF)
            return true;
        
        // Check all flags.
        if ((flag & TransformLinePool.SEARCH_SELECT_ON) == TransformLinePool.SEARCH_SELECT_ON && !selected)
            return false;
        if ((flag & TransformLinePool.SEARCH_SELECT_OFF) == TransformLinePool.SEARCH_SELECT_OFF && selected)
            return false;
        if ((flag & TransformLinePool.SEARCH_VISIT_ON) == TransformLinePool.SEARCH_VISIT_ON && !visited)
            return false;
        if ((flag & TransformLinePool.SEARCH_VISIT_OFF) == TransformLinePool.SEARCH_VISIT_OFF && visited)
            return false;
        
        // All flags set have passed.
        return true;
    }

    /** This will set the TransformAbstractLine based on the flag values. Note that not all of the 
     * TransformLinePool.SEARCH_* flags can be used. Check those flag comments to see which ones will work.
     * @param flag is used to set this TransformAbstractLine flags based on the inputted flag values. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific set. Unknown effects 
     * if using opposite SEARCH_* flags in one set.
     */
    void set(int flag) {
        // Does the flags match?
        if (flag == DrawingLinePool.SEARCH_OFF)
            return;
        if ((flag & TransformLinePool.SEARCH_SELECT_ON) == TransformLinePool.SEARCH_SELECT_ON)
            selected = true;
        if ((flag & TransformLinePool.SEARCH_SELECT_OFF) == TransformLinePool.SEARCH_SELECT_OFF)
            selected = false;
        if ((flag & TransformLinePool.SEARCH_VISIT_ON) == TransformLinePool.SEARCH_VISIT_ON)
            visited = true;
        if ((flag & TransformLinePool.SEARCH_VISIT_OFF) == TransformLinePool.SEARCH_VISIT_OFF)
            visited = false;
    }

    /** This will set the line pointed to. It is used only when selecing lines from the DrawingLinePool.
     * See DrawingLinePool.getSelectedItems().
     * @param id is the line which this line will point to. (Represents)
     */
    void setLineTo(int id) {
        this.lineToId = id;
    }

    /** This will set the end point vertices to flag values. 
     * @param flag is used to set the end point vertices to the inputted flag values. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific set. Unknown effects 
     * if using opposite SEARCH_* flags in one set.
     */
    public void setVertices(int flag) {
        vEnd1.set(flag);
        vEnd2.set(flag);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Segment Methods ">    
    
    /** This will add a visit segment. If the visit segment added allows vEnd1 to reach vEnd2 by
     * the visit segments then it is complete visited.
     * @param visitSegment is the visit segment which needs to be added to this AbstractLine.
     */
    public void addVisitSegment(VisitSegment visitSegment) {
        if (ltVisitSegment == null)
            ltVisitSegment = new LinkedList();
        ltVisitSegment.add(visitSegment);
        
        // See if a complete visit can be compeleted, and if so then line is visited.
        if (isSegmentsCompleted()) {
            visited = true;
            ltVisitSegment = null;
        }
        
    }
    
    /** This will get the next vertex to be visited. And travel to it will be by a segment which has not been visited.
     * @param vFrom is the vertex to start the search from. This can be an end vertex or an AutoLinkVertex. vFrom must always be the AutoLinkVertex
     * if it can.
     * @param visit is true if it should add this segment as being visited, else false do not add this segment as being visited.
     * If the segment is visited and it is the last segment to be visited then this AbstractLine will be visited (isVisited() will
     * return true).
     * @return the next vertex can be an AutoLinkVertex, can be null if the segments to get to the next Vertex has already been
     * visited.
     */
    public TransformVertex getNextNonVisitedSegment(TransformVertex vFrom, boolean visit) {
        // AbstractLine is already visited.
        if (visited)
            return null;
        
        // No auto linked vertices, therefore go to the other end vertex. And set visited if need be.
        if (ltAutoLinkedVertex == null) {
            visited = visit;
            return getOppositeEndVertex(vFrom);
        }
        
        // Since the vertex 'from' is an end vertex of this line therefore get the closest auto link vertex.
        if (vFrom == vEnd1) {
            TransformVertex vNext = ltAutoLinkedVertex.getFirst();
            if (isVisited(vFrom, vNext))
                return null;
            if (visit)
                addVisitSegment(new VisitSegment(vFrom, vNext));
            return vNext;
        } else if (vFrom == vEnd2) {
            TransformVertex vNext = ltAutoLinkedVertex.getLast();
            if (isVisited(vNext, vFrom))
                return null;
            if (visit)
                addVisitSegment(new VisitSegment(vNext, vFrom));
            return vNext;
        }
        
        // Since vFrom is an auto link vertex there is two possible paths from this Vertex. See if one is available.
        TransformVertex vPrev = vEnd1;
        for (Iterator<TransformVertex> itr = ltAutoLinkedVertex.iterator(); itr.hasNext(); ) {
            TransformVertex vCurr = itr.next();
            
            // If the current vertex the from vertex then see if the previous path is open to traverse.
            if (vCurr == vFrom && isVisited(vPrev, vFrom) == false) {
                addVisitSegment(new VisitSegment(vPrev, vFrom));
                return vPrev;
            } else if (vPrev == vFrom && isVisited(vFrom, vCurr) == false) { // See if the current vertex is an open path to traverse.
                addVisitSegment(new VisitSegment(vFrom, vCurr));
                return vCurr;
            }
            
            vPrev = vCurr;            
        }
        
        // The vFrom vertex could be the last AutoLinkVertex in the list therefore check and see if you can go to the end vertex.
        TransformVertex vLastEnd = vEnd2;
        if (vPrev == vFrom && !isVisited(vFrom, vLastEnd)) {           
            addVisitSegment(new VisitSegment(vFrom, vLastEnd));
            return vLastEnd;
        }
        
        // Both segments are visited.
        return null;
    }

    /** @return true if the visited segments can make a complete trip from vEnd1 to vEnd2 via the visited segments,
     * else false.
     */
    private boolean isSegmentsCompleted() {
        if (ltVisitSegment == null)
            return false;

        // Sort them.
        Collections.sort(ltVisitSegment);

        // Make sure to start out with vEnd1.
        Iterator<VisitSegment> itr = ltVisitSegment.iterator();
        VisitSegment prev = itr.next();
        if (prev.getStart() != vEnd1)
            return false;

        // Now make sure each segment connects.
        for ( ;itr.hasNext(); ) {
            VisitSegment segment = itr.next();
            if (prev.getEnd() != segment.getStart())
                return false;                
            prev = segment;
        }

        // The last segment must end with vEnd2.
        if (prev.getEnd() != vEnd2)
            return false;

        return true;
    }
    
    /** This will see if this segment has already been visited.
     * @param v1 is the first vertex to be used to search for a segment.
     * @param v2 is the second vertex to be used to search for a segment.
     * @return true if this segment has already been visited, else false.
     */
    private boolean isVisited(TransformVertex v1, TransformVertex v2) {
        // None in there, therefore return false.
        if (ltVisitSegment == null)
            return false;
        
        // Search through each segment and see if it is in there.
        for (Iterator<VisitSegment> itr = ltVisitSegment.iterator(); itr.hasNext(); ) {
            VisitSegment segment = itr.next();
            if (segment.contains(v1, v2))
                return true;
        }
        
        // Didn't find the segment.
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Vertex Methods ">
    
    /** This will add the vertex to this line as an auto link vertex. 
     * @param v is the vertex that will be used as a connection point to this line which is on the line and not the end
     * points.
     */
    void addAutoLinkVertex(TransformVertex v) {
        // Create a new auto link vertex link.
        if (ltAutoLinkedVertex == null)
            ltAutoLinkedVertex = new LinkedList();
        ltAutoLinkedVertex.add(v);
        
        // Sort the collection of auto linked vertices.
        Collections.sort(ltAutoLinkedVertex);
    }
    
    /** This will check to see if the fpt point is equal to one of the end points.
     * @param fpt is the point to check to see if it matches one the of the end points.
     * @return true if fpt does in fact match one of the end points, else false it does not.
     */
    public boolean containsEnd(FPointType fpt) {
        if (vEnd1.getPoint().equals(fpt) || vEnd2.getPoint().equals(fpt))
            return true;
        return false;
    }
    
    /** This will return true if the 'vertex' is one of the end point vertices. It will NOT check
     * the control points. Performs a memory location match and NOT vertex id compare.
     * @param vertex is the vertex to see if it exists in this line as an end point vertex.
     * @return true if the 'vertex' is one of the end point vertices. It will NOT check
     * the control points, else false does not exist as an end point vertex.
     */
    public boolean containsEnd(TransformVertex vertex) {
        if (vertex == vEnd1 || vertex == vEnd2)
            return true;
        return false;
    }
    
    /** This will get the first vertex end point in the line.
     * @return the first end point TransformVertex in the line.
     */
    public TransformVertex getFirstEndVertex() {
        return vEnd1;
    }

    /** This will get the last vertex end point in the line.
     * @return the last end point TransformVertex in the line.
     */
    public TransformVertex getLastEndVertex() {
        return vEnd2;
    }
    
    /** This will get the opposite end point vertex of this line.
     * @param v is the vertex that is contained in this line.
     * @return the opposite vertex end point of the v vertex.
     */
    public TransformVertex getOppositeEndVertex(TransformVertex v) {
        if (v == vEnd1)
            return vEnd2;
        if (v == vEnd2)
            return vEnd1;
        
        throw new IllegalArgumentException("AbstractLine:: Vertex[" + v + "] is not in this line[" + this + "].");
    }
    
    /** @param v is the vertex to see where the parameteric t position is. Can be an AutoLinkVertex.
     * @return parameter t position of vertex.
     */
    protected float getParametericT(TransformVertex v) {            
        if (v == vEnd1)
            return 0.0f;
        else if (v == vEnd2)
            return 1.0f;
        else if (v.isAutoLinkVertex())
            return v.getParametericT();
        
        throw new IllegalArgumentException("TransformVertex[" + v + "] does not exist in this TransformAbstractLine. ");
    }
    
    /** @param v is to be checked against the first end point or if v is an auto link vertex then against this auto link vertex.
     * @return true if the vertex is the first end point of this line.
     */
    public boolean isFirstEndVertex(TransformVertex v) {
        return vEnd1 == v;
    }
    
    /** @param v is to be checked against the last end point. 
     * @return true if the vertex is the last end point of this line.
     */
    public boolean isLastEndVertex(TransformVertex v) {
        return vEnd2 == v;
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Geometry Methods ">
    
    /** This will get a point on the line. It will be the closest point from the fpt to the line.
     * @param fpt is a point that is used to determine the closest point from it to the line.
     * @return a point closest on the line to the point fpt.
     */
    public FPointType getPointOn(FPointType fpt) {
        return getParameterValue(getClosestParameterT(fpt));
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Abstract Methods ">
    
    /** This will draw the line.
     * @param g2d is the graphics device.
     * @param erase is true if it should erase before drawing.
     */
    abstract public void draw(Graphics2D g2d, boolean erase);
    
    /** Will check against another TransformAbstractLine by checking to see if id's match. Or
     * will check against a AbstractLineInfo of proper sub class matching... i.e. TransformLine
     * with a LineInfo. In the later case, end points must match and control points must match.
     * @returns true if the above information is true, or false if it does not match. */
    abstract public boolean equals(Object obj);

    /** This will get the bounds of this AbstractLine.
     * @return a bound of this AbstractLine.
     */
    abstract Rectangle2D.Float getBounds2D();
    
    /** This will get the closest parameter position on the TransformAbstractLine where the fpt  distance is the closest.
     * @param fpt is a point can or can not be on the TransformAbstractLine.
     * @return the closest parameter position on the TransformAbstractLine where the fpt distance is the closest.
     */
    abstract public float getClosestParameterT(FPointType fpt);
    
    /** This will get the position on the TransformAbstractLine by using the parametric t value.
     * @param t is the parameteric value.
     * @return the position on the AbstractLine according to the value t.
     */
    abstract public FPointType getParameterValue(float t);

    /** @return an AbstractLineInfo describing this AbstractLine.
     */
    abstract public AbstractLineInfo getInfo();
    
    /** @return the middle point on the line or curve.
     */
    abstract public FPointType getMiddlePt();
    
    /** This will sample this line from vFrom to vTo.
     * @param vFrom is the starting position to sample from.
     * @param vTo is the ending position to sample to.
     * @param sampleSize is the number of points which this entire AbstractLine should have.
     * @return a list of sampled points from this AbstractLine should atleast sampleSize number of points. 
     * However, if vFrom or vTo is part of the AbstractLine then it will contain less points. vFrom and vTo are in
     * this list.
     */
    abstract LinkedList<SFPointType> getSampledPoints(TransformVertex vFrom, TransformVertex vTo, int sampleSize);
    
    /** Checks to see if this AbstractLine intersects with the rectangle.
     * @param r is the rectangle to see if this AbstractLine intersects with.
     * @return true if it intersects with the rectangle.
     */
    abstract public boolean intersects(Rectangle2D.Float r);
    
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
    
    /** This will validate the TransformAbstractLine by updating its drawing line structure.
     */
    abstract public void validate();
    
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
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Class VisitSegment ">
    
    /** This class is used to segment the line when a visit is only part of the line.
     */
    class VisitSegment implements Comparable {
        /** The start of this visit. */
        private TransformVertex vStart;
        /** The end of this visit. */
        private TransformVertex vEnd;
        
        /** Can mix Vertex AutoLinkVertex when creating a segment, matter a fact you have to unless segment
         * goes from one end vertex to another end vertex. For this to work if a end vertex is represented by an
         * AutoLinkVertex then you must use the AutoLinkVertex instead of the Vertex.
         * @param vStart is the vertex which this segment visit will begin at.
         * @param vEnd is the vertex which this segment visit will end at.
         */
        VisitSegment(TransformVertex vStart, TransformVertex vEnd) {
            this.vStart = vStart;
            this.vEnd = vEnd;
        }
        
        TransformVertex getStart() {
            return vStart;            
        }
        
        TransformVertex getEnd() {
            return vEnd;
        }
        
        /** @param v1 is a vertex start-end.
         * @param v2 is a vertex start-end.
         * @return true if v1 and v2 match this segment.
         */
        boolean contains(TransformVertex v1, TransformVertex v2) {
            if ((v1 == vStart && v2 == vEnd) || (v1 == vEnd && v2 == vStart))
                return true;
            return false;
        }

        public int compareTo(Object o) {
            VisitSegment seg = (VisitSegment)o;
            
            // Take care of end cases.
            TransformVertex vTemp = vEnd1;
            if (vStart == vTemp || vEnd == vTemp)
                return -1;
            vTemp = vEnd2;
            if (vStart == vTemp || vEnd == vTemp)
                return 1;
            if (seg.vStart == vTemp || seg.vEnd == vTemp)
                return -1;
            vTemp = vEnd1;
            if (seg.vStart == vTemp || seg.vEnd == vTemp)
                return 1;
            
            // These are complete auto link vertices segments.
            if (((TransformVertex)vStart).getParametericT() < ((TransformVertex)seg.vStart).getParametericT())
                return -1;
            else
                return 1;            
        }   
    }
    
    // </editor-fold>

}
