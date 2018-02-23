/*
 * GeneralTreeSegment.java
 *
 * Created on November 17, 2006, 3:05 PM
 *
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.*;

/**
 *
 * @author Robert Molnar II
 */
public class GeneralTreeSegment {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the line representing this segment. */
    TransformAbstractLine segment;
    
    /** This is the Vertex (or AutoLinkVertex) on the segment line which this segment starts from. */
    TransformVertex vFrom;
    
    /** This is the Vertex (or AutoLinkVertex) on the segment line which this segment ends at. */
    TransformVertex vTo;
        
    /**  This is the node which this segment starts from. */
    GeneralTreeNode nodeFrom;
        
    /**  This is the node which this segment ends at. */
    GeneralTreeNode nodeTo;
        
    /** START HERE.... PLAN:: Create a segment which loads this list when the segments including this. */
    LinkedList<GeneralTreeSegment> ltSegments = null;
            
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /**
     * Creates a new instance of GeneralTreeSegment
     * @param segment is the line representing this segment.
     * @param vFrom this is the TransformVertex (or AutoLinkVertex) on the segment line which this segment starts from.
     * @param vTo this is the TransformVertex (or AutoLinkVertex) on the segment line which this segment ends at.
     */
    public GeneralTreeSegment(TransformAbstractLine segment, TransformVertex vFrom, TransformVertex vTo) {
        this.segment = segment;
        this.vFrom = vFrom;
        this.vTo = vTo;
    }

    public TransformAbstractLine getSegment() {
        return segment;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Set Method ">
    
    /** @param nodeFrom is the node which this segment starts from.
     * @param nodeTo is the node which this segment ends at.
     */
    public void setNodes(GeneralTreeNode nodeFrom, GeneralTreeNode nodeTo) {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Get Methods ">
    
    /** @return the to node.
     */
    public GeneralTreeNode getToNode() {
        return nodeTo;
    }
    
    /** @return the from node.
     */
    public GeneralTreeNode getFromNode() {
        return nodeFrom;
    }

    /** This will sample the segment. If this is a mulitple one then it will sample each one, thus it could return
     * sampleSize times the number of segments.
     * @param sampleSize is the number of points from the segment, if this is a multiple segment then it will return the
     * points from those segments. Note that if a line or part of a curve is sampled then it will not return as many points. This
     * is only the number sampling points used for an entire curve.
     * @return a list of each segment's sampled points. If this is a single segment then only one list of SFPointTypes will be present.
     * However if this is a multiple segment then starting from the 'from node' to the 'to node' it will sample each segment, therefore
     * the first segment in the list will be the segment from the 'from node'.
     */
    public LinkedList<LinkedList<SFPointType>> getSampledPoints(int sampleSize) {
        LinkedList<LinkedList<SFPointType>> ltPoints = new LinkedList();
        if (isDummySegment())
            return ltPoints;
        if (isMultipleSegments()) {
            for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) 
                ltPoints.addAll(itr.next().getSampledPoints(sampleSize));
            return ltPoints;
        }
        
        ltPoints.add(segment.getSampledPoints(vFrom, vTo, sampleSize));
        return ltPoints;
    }
    
    // </editor-fold>
            
    // <editor-fold defaultstate="collapsed" desc=" Is Methods ">
    
    /** @return true if this segment is a dummy, else false it is not a dummy.
     */
    public boolean isDummySegment() {
        if (segment == null)
            return true;
        return false;
    }
    
    /** @return true if this segment contains multiple segments, else false is a single segment.
     */
    boolean isMultipleSegments() {
        if (ltSegments == null)
            return false;
        return true;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Convert Methods ">
    
    /** This will compress the tree so that each segment leads to a node that contains more than two segments or is the leaf.
     * So this could create segments which represent multiple segments.
     */
    void convertToCompressedTree() {
        if (!isDummySegment() && nodeTo.childrenCount() == 1) {            
            ltSegments = new LinkedList();
            
            // Clone it so that 'this' segment can be modified into a segment which represents multiple segments.
            GeneralTreeSegment clone = new GeneralTreeSegment(this.segment, this.vFrom, this.vTo);
            clone.setNodes(this.nodeFrom, this.nodeTo);
            
            // Traverse the path until a node is reach that contains more than 1 segment.
            GeneralTreeSegment segment = clone;
            while (segment.nodeTo.childrenCount() == 1) {
                ltSegments.add(segment);
                
                // Traverse.
                segment = segment.nodeTo.getChildrenSegments().getFirst();
            }
            
            // Add last segment and update this segment to point to the end vertex.
            ltSegments.add(segment);
            this.vTo = segment.vTo;
            this.nodeTo = segment.nodeTo;
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
    
    /** This will draw this segment.
     */
    public void draw(Graphics2D g2d) {
        if (!isMultipleSegments()) {
            vFrom.drawGluePoint(g2d, false);
            vTo.drawGluePoint(g2d, false);
            segment.draw(g2d, false);
        } else {
            // Get the middle node to draw a line to and from it.
            TransformVertex midVertex = null;
            int middle = (ltSegments.size() + 1) / 2;
            
            // draw each segment.
            int i=0;
            for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); i++) {
                GeneralTreeSegment s = itr.next();
                s.draw(g2d);
                
                // Get the middle node.
                if (i == middle)
                    midVertex = s.vFrom;
            }
            
            // draw a line from start node to middle node to end node as red.
            Color c = g2d.getColor();
            g2d.draw(new Line2D.Float(vFrom.getPoint().x, vFrom.getPoint().y, midVertex.getPoint().x, midVertex.getPoint().y));
            g2d.draw(new Line2D.Float(midVertex.getPoint().x, midVertex.getPoint().y, vTo.getPoint().x, vTo.getPoint().y));
            g2d.setColor(c);
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public String toString() {
        return "{GeneralTreeSegment: vFrom: " + vFrom.getPoint() + " vTo: " + vTo.getPoint() + "}";
    }
    
    // </editor-fold>
    
}
