/*
 * GeneralTreeNode.java
 *
 * Created on November 17, 2006, 3:05 PM
 *
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;

/**
 *
 * @author Robert Molnar II
 */
public class GeneralTreeNode {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the parent node to this node. */
    GeneralTreeNode parent;
    
    /** This is the vertex which this GeneralTreeNode represents. Can be an AutoLinkVertex.  */
    TransformVertex vNode;
    
    /** This is the list of segments from this node. */
    LinkedList<GeneralTreeSegment> ltSegments = new LinkedList();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor and static constructor methods ">
    
    /**
     * Creates a new instance of GeneralTreeNode
     */
    public GeneralTreeNode(GeneralTreeNode parent, TransformVertex vNode) {
        this.parent = parent;
        this.vNode = vNode;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
    
    /** This will draw the segments under this node and then have those segment's nodes draw etc..
     */
    public void draw(Graphics2D g2d) {
        for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) {
            GeneralTreeSegment segment = itr.next();
            segment.draw(g2d);
            segment.getToNode().draw(g2d);
        }
    }
    
    // </editor-fold>
            
    // <editor-fold defaultstate="collapsed" desc=" Add/Get/Is Methods ">
    
    /** This will add the segment to this node.
     * @param segment is the segment to add to this node.
     */
    public void add(GeneralTreeSegment segment) {
        ltSegments.add(segment);
    }
    
    /** @return the first parent who is not a dummy. If parent is a dummy then it will get the parent's parent until it reaches a non-dummy node.
     */
    public GeneralTreeNode getParentNotDummy() {
        GeneralTreeNode parent = getParent();
        while (parent.isDummyNode()) {
            parent = parent.getParent();
        }
        
        return parent;
    }

    /** @return the TransformVertex of this node.
     */
    TransformVertex getNode() {
        return vNode;
    }
    
    /** @return the current position of this node's vertex which this node represents.
     */
    public FPointType getNodePosition() {
        return vNode.getPoint();
    }
    
    /** @return the parent node to this one. If this is the root then it will return null.
     */
    public GeneralTreeNode getParent() {
        return parent;
    }
    
    /** This will get the children segments.
     * @return A list of children segments, can be empty if no segments are under this node.
     */
    public LinkedList<GeneralTreeSegment> getChildrenSegments() {
        return ltSegments;
    }
    
    /** This will get the children segment's nodes.
     * @return A list of child segment's nodes, can be empty if no segments are under this node.
     */
    public LinkedList<GeneralTreeNode> getChildrenNodes() {
        LinkedList<GeneralTreeNode> ltTemp = new LinkedList();
        
        for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) {
            GeneralTreeSegment segment = itr.next();
            ltTemp.add(segment.getToNode());
        }
        
        return ltTemp;
    }

    /** @return true if this node is a dummy, else false it is not a dummy.
     */
    public boolean isDummyNode() {
        if (vNode == null)
            return true;
        return false;
    }
    
    /** @return true if this is the root node.
     */
    public boolean isRootNode() {
        if (parent == null)
            return true;
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Operation Methods ">
    
    /** @return the number of children under the node.
     */
    public int childrenCount() {
        return ltSegments.size();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public String toString() {
        if (parent == null)
            return "{GeneralTreeNode ROOT pt: " + vNode.getPoint() + " segments: " + ltSegments.toString() + " }";
        else
            return "{GeneralTreeNode parent pt:" + parent.vNode.getPoint() + " pt: " + vNode.getPoint() + " segments: " + ltSegments.toString() + " }";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Build And Convert Methods ">
       
    /** If this node contains more than 3 children nodes then it will rework it so that it will only contain 3 children nodes
     * under this node. Note that it will use GeneralTreeDummyNode to break the node's children down to only 3.
     */
    void convertToTrinaryTree() {
        // Break this node up so that it will contain only 3 nodes.
        if (childrenCount() > 3) {
            // Create the dummy segment and node.
            GeneralTreeSegment dummySegment = new GeneralTreeSegment(null, vNode, null);
            GeneralTreeNode dummyNode = new GeneralTreeNode(this, null);
            
            // Set the dummy segment to go from this node to the dummy.
            dummySegment.setNodes(this, dummyNode);
            
            // Add the rest of the segments to the dummy node and remove them from this node.
            while (childrenCount() > 2)
                dummyNode.ltSegments.add(ltSegments.removeLast());
            
            // Now add the dummy segment to this node.
            ltSegments.add(dummySegment);
        }
        
        // Traverse the nodes under this node.
        for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) {
            itr.next().getToNode().convertToTrinaryTree();
        }
    }
    
    /** If this node contains one segment then it will convert the segments under it until it reaches a node which
     * does not contain 1 segment.
     */
    void convertToCompressedTree() {
        for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) {
            GeneralTreeSegment segment = itr.next();
            
            // See if the current segment should become a multiple segment.
            if (segment.getToNode().getChildrenSegments().size() == 1)
                segment.convertToCompressedTree();
            
            // go to next segment and see if it needs to become a multiple segment.
            segment.getToNode().convertToCompressedTree();
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will draw only this node.
     */
    public void debugDraw(Graphics2D g2d) {
        for (Iterator<GeneralTreeSegment> itr = ltSegments.iterator(); itr.hasNext(); ) {
            GeneralTreeSegment segment = itr.next();
            segment.draw(g2d);
        }
    }
    
    // </editor-fold>
    
}
