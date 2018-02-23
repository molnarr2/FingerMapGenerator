/*
 * TransformLinePool.java
 *
 * Created on April 12, 2007, 5:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;
import mlnr.util.XmlUtil;
import org.w3c.dom.Element;

/** This class is used to maintain a pool of TransformAbstractLines and to transform the vertices. <br>
 * This class will perform the following operations: add lines, search and find, remove lines, and status functions. <br>
 * This class assumes the TransformGraph will use the add/remove operations of this class for all manipulations of
 * line's vertices. That way this class can assume all lines contain their vertices in this pool.
 *
 * Note: The TransformVertexPool can contain Bezier control points.
 *
 * @author Robert Molnar 2
 */
public class TransformLinePool extends AbstractPool  {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /* The following flags can be used to perform a specific search or comparison. They can be OR'd together
     * to perform complicated searching. Unknown results if two search flags of the same type are OR'd together. */
    
    /** flag parameter for search: no flag requirements. */
    public static final int SEARCH_OFF = 0;
    /** TransformAbstractLine ONLY: flag parameter for search:  select flag on required. */
    public static final int SEARCH_SELECT_ON = 1;
    /** TransformAbstractLine ONLY: flag parameter for search:  select flag off required. */
    public static final int SEARCH_SELECT_OFF = 2;
    /** flag parameter for search:  visit flag on required. */
    public static final int SEARCH_VISIT_ON = 4;
    /** flag parameter for search:  visit flag off required. */
    public static final int SEARCH_VISIT_OFF = 8;
    /** Vertex ONLY: flag parameter for search:  visible flag on required. */
    public static final int SEARCH_VISIBLE_ON = 16;
    /** Vertex ONLY: flag parameter for search:  visible flag off required.  */
    public static final int SEARCH_VISIBLE_OFF = 32;
    /** Vertex ONLY: flag parameter for search: transformable flag on required. */
    public static final int SEARCH_TRANSFORMABLE_ON = 64;
    /** Vertex ONLY: flag parameter for search: transformable flag off required. */
    public static final int SEARCH_TRANSFORMABLE_OFF = 128;
    /** Vertex ONLY: flag parameter for search: bezier control point on required. Vertex is a bezier control point. */
    public static final int SEARCH_BEZIER_CONTROL_PT_ON = 256;
    /** Vertex ONLY: flag parameter for search: bezier control point off required. Vertex is not a bezier control point. */
    public static final int SEARCH_BEZIER_CONTROL_PT_OFF = 512;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is a pool vertices used by this TransformLinePool. */
    private TransformVertexPool vpInternal = new TransformVertexPool();
    
    /** This is a list of control vertices that will be transformed. */
    private LinkedList<TransformVertex> ltControlVertices = new LinkedList();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eLineList is the element for the lineList in the RXML file.
     * @param eVertexList is the element for the vertexList in the RXML file.
     * @return LinePool from the element information.
     */
    static TransformLinePool loadVersion10(Element eLineList, Element eVertexList) throws Exception {
        UndoItemComplex undoComplex = new UndoItemComplex(); // Temporary do nothing for getting vertices for bezier control points.
        TransformLinePool tlPool = new TransformLinePool();
        TransformVertexPool tvPool = tlPool.getVertexPool();
        
        // Load the vertexPool.
        tvPool.loadVersion10(eVertexList);
        
        // Add each line, rmolnar, and bezier to the pool.
        LinkedList<Element> lt = XmlUtil.getChildrenElements(eLineList);
        for (Iterator<Element> itr = lt.iterator(); itr.hasNext(); ) {
            Element eAbstractLine = itr.next();            
            String nodeName = eAbstractLine.getNodeName();
            
            // Get the common attributes.
            int id = XmlUtil.getAttributeInteger(eAbstractLine, "id");
            int v0 = XmlUtil.getAttributeInteger(eAbstractLine, "vertexId0");
            int v1 = XmlUtil.getAttributeInteger(eAbstractLine, "vertexId1");
            
            // Create the Transform item.
            TransformAbstractLine abLine = null;
            if ("line".equals(nodeName)) {            
                abLine = new TransformLine(tvPool.get(v0), tvPool.get(v1));
                
            } else if ("rmolnar".equals(nodeName)) {
                int v2 = XmlUtil.getAttributeInteger(eAbstractLine, "vertexId2");
                int v3 = XmlUtil.getAttributeInteger(eAbstractLine, "vertexId3");
                abLine = new TransformRMolnar(tvPool.get(v1), tvPool.get(v2), tvPool.get(v0), tvPool.get(v3));
                
            } else if ("bezier".equals(nodeName)) {
                float c1x = (float)XmlUtil.getAttributeDouble(eAbstractLine, "xPt0") / 20.0f;
                float c1y = (float)XmlUtil.getAttributeDouble(eAbstractLine, "yPt0") / 20.0f;
                float c2x = (float)XmlUtil.getAttributeDouble(eAbstractLine, "xPt1") / 20.0f;
                float c2y = (float)XmlUtil.getAttributeDouble(eAbstractLine, "yPt1") / 20.0f;
                abLine = new TransformBezier(tvPool.get(v0), tvPool.get(v1), 
                    tlPool.getBezierVertex(new FPointType(c1x, c1y), undoComplex), tlPool.getBezierVertex(new FPointType(c2x, c2y), undoComplex));
                
            } else
                throw new Exception ("Element [" + eAbstractLine.getNodeName() + "] illegal tag in rxml file.");
                        
            // Set the id and restore the line.
            abLine.id = id;            
            tlPool.restore(abLine);
        }

        return tlPool;
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eLinePool is the element for the linePool in the RXML file.
     * @return LinePool from the element information.
     */
    static TransformLinePool loadVersion20(Element eLinePool) throws Exception {
        UndoItemComplex undoComplex = new UndoItemComplex(); // Temporary do nothing for getting vertices for bezier control points.
        TransformLinePool tlPool = new TransformLinePool();
        TransformVertexPool tvPool = tlPool.getVertexPool();
        
        // Load the vertexPool.
        Element eVertexPool = XmlUtil.getElementByTagName(eLinePool, "vertexPool");
        tvPool.loadVersion20(eVertexPool);
        
        // Add each line, rmolnar, and bezier to the pool.
        LinkedList<Element> lt = XmlUtil.getChildrenElements(eLinePool);
        for (Iterator<Element> itr = lt.iterator(); itr.hasNext(); ) {
            Element eAbstractLine = itr.next();                        
            String nodeName = eAbstractLine.getNodeName();
            
            // Don't process the vertexPool.
            if (nodeName.equals("vertexPool"))
                continue;
            
            // Get the common attributes.
            int id = XmlUtil.getAttributeInteger(eAbstractLine, "id");
            int v1 = XmlUtil.getAttributeInteger(eAbstractLine, "v1");
            int v2 = XmlUtil.getAttributeInteger(eAbstractLine, "v2");
            
            // Create the Transform item.
            TransformAbstractLine abLine = null;
            if ("line".equals(nodeName)) {
                abLine = new TransformLine(tvPool.get(v1), tvPool.get(v2));
                
            } else if ("rmolnar".equals(nodeName)) {
                int c1 = XmlUtil.getAttributeInteger(eAbstractLine, "c1");
                int c2 = XmlUtil.getAttributeInteger(eAbstractLine, "c2");        
                abLine = new TransformRMolnar(tvPool.get(v1), tvPool.get(v2), tvPool.get(c1), tvPool.get(c2));
                
            } else if ("bezier".equals(nodeName)) {
                float c1x = (float)XmlUtil.getAttributeDouble(eAbstractLine, "c1x");
                float c1y = (float)XmlUtil.getAttributeDouble(eAbstractLine, "c1y");
                float c2x = (float)XmlUtil.getAttributeDouble(eAbstractLine, "c2x");
                float c2y = (float)XmlUtil.getAttributeDouble(eAbstractLine, "c2y");        
                abLine = new TransformBezier(tvPool.get(v1), tvPool.get(v2), 
                    tlPool.getBezierVertex(new FPointType(c1x, c1y), undoComplex), tlPool.getBezierVertex(new FPointType(c2x, c2y), undoComplex));
                
            } else
                throw new Exception ("Element [" + eAbstractLine.getNodeName() + "] illegal tag in rxml file.");
                        
            // Set the id and restore the line.
            abLine.id = id;            
            tlPool.restore(abLine);
        }

        return tlPool;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add Methods ">
    
    /** This will add the abLineInfo to this Graph.
     * @param abLineInfo is the AbstractLineInfo to add to this Graph.
     * @param id is the line id from the DrawingGraph which the abLineInfo represents.
     * @param lineSelected is true if the added line should be selected.
     * @param vertexVisible is true if the added vertices should be visible or not. This will set all the vertices created
     * by this method to visible or invisible, including the control vertices.
     */
    void add(AbstractLineInfo abLineInfo, int id, boolean lineSelected, boolean vertexVisible) {
        // Add the line.
        InterfaceUndoItem iUndo = add(abLineInfo);
        
        // Already added.
        if (iUndo.isUndoable() == false)
            return;
        
        // Get the line that was just added. And set it's attributes.
        TransformAbstractLine abAdded = search(abLineInfo);
        if (abAdded == null)
            throw new IllegalStateException("Search did not come back with added line.");
        abAdded.setLineTo(id);
        abAdded.set(SEARCH_SELECT_ON);
        abAdded.setVertices(SEARCH_VISIBLE_ON);
    }
    
    /** This will add the AbstractLineInfo to this pool. It will not add a duplicate to this pool.
     * @param abLineInfo is AbstractLineInfo to add to this pool.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem add(AbstractLineInfo abLineInfo) {
        // Do not add duplicates to this pool.
        if (contains(abLineInfo))
            return new UndoItemComplex();
        
        // Add the AbstractLine.
        if (abLineInfo instanceof LineInfo)
            return addLine((LineInfo)abLineInfo);
        else if (abLineInfo instanceof BezierInfo)
            return addBezier((BezierInfo)abLineInfo);
        else if (abLineInfo instanceof RMolnarInfo)
            return addRMolnar((RMolnarInfo)abLineInfo);
        
        throw new IllegalArgumentException("Unknown type of AbstractLineInfo[" + abLineInfo + "]. ");
    }
    
    /** This will add the line to this graph.
     * @param lineInfo is the line to be added to this graph.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem addLine(LineInfo lineInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points for the line.
        TransformVertex vEnd1 = getNewTransformVertex(lineInfo.getEndPoint1(), undoComplex);
        TransformVertex vEnd2 = getNewTransformVertex(lineInfo.getEndPoint2(), undoComplex);
        
        // Create the line and add it to the LinePool.
        TransformLine l = new TransformLine(vEnd1, vEnd2);
        super.add(l);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(l));
        return undoComplex;
    }
    
    /** This will add the bezier to this graph.
     * @param bezierInfo contains the information to create a bezier in this graph.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem addBezier(BezierInfo bezierInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points for the line.
        TransformVertex vEnd1 = getNewTransformVertex(bezierInfo.getEndPoint1(), undoComplex);
        TransformVertex vEnd2 = getNewTransformVertex(bezierInfo.getEndPoint2(), undoComplex);
        TransformVertex vControl1 = getBezierVertex(bezierInfo.getControlPoint1(), undoComplex);        
        TransformVertex vControl2 = getBezierVertex(bezierInfo.getControlPoint2(), undoComplex);
        
        // Create the line and add it to the LinePool.
        TransformBezier b = new TransformBezier(vEnd1, vEnd2, vControl1, vControl2);
        super.add(b);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(b));
        return undoComplex;
    }
    
    /** This will add the rmolnar to this graph.
     * @param rmolnar contains the information to create a rmolnar in this graph.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem addRMolnar(RMolnarInfo rmolnarInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points and control points for the rmolnar.
        TransformVertex vEnd1 = getNewTransformVertex(rmolnarInfo.getEndPoint1(), undoComplex);
        TransformVertex vEnd2 = getNewTransformVertex(rmolnarInfo.getEndPoint2(), undoComplex);
        TransformVertex vControl1 = getNewTransformVertex(rmolnarInfo.getControlPoint1(), undoComplex);        
        TransformVertex vControl2 = getNewTransformVertex(rmolnarInfo.getControlPoint2(), undoComplex);
        
        // Create the rmolnar.
        TransformRMolnar r = new TransformRMolnar(vEnd1, vEnd2, vControl1, vControl2);
        super.add(r);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(r));
        return undoComplex;
    }
    
    /** This will ALWAYS create a new TransformVertex for the bezier control point. The TransfromVertex
     * will be set as being a bezier control point. This will add the TransformVertex to the pool of vertices.
     * <br> The vertex will be invisible and will be transformable.
     * @param fpt is the point for the new TransformVertex will be at.
     * @param undoComplex is used to add an undo for this operation.
     * @return a TransformVertex at the point of fpt.
     */
    TransformVertex getBezierVertex(FPointType fpt, UndoItemComplex undoComplex) {
        TransformVertex v = new TransformVertex(fpt);
        v.set(SEARCH_VISIBLE_OFF | SEARCH_TRANSFORMABLE_ON | SEARCH_BEZIER_CONTROL_PT_ON);
        undoComplex.add(vpInternal.add(v));
        
        return v;
    }
    
    /** This will ALWAYS create a new TransformVertex for the bezier control point. The TransfromVertex
     * will be set as being a bezier control point. This will add the TransformVertex to the pool of vertices.
     * <br> The vertex will be invisible and will be transformable.
     * @param fpt is the point for the new TransformVertex will be at.
     * @return a TransformVertex at the point of fpt.
     */
    TransformVertex getBezierVertex(FPointType fpt) {
        TransformVertex v = new TransformVertex(fpt);
        v.set(SEARCH_VISIBLE_OFF | SEARCH_TRANSFORMABLE_ON | SEARCH_BEZIER_CONTROL_PT_ON);
        vpInternal.add(v);
        
        return v;
    }
    
    /** This will get a TransformVertex from the VertexPool by creating one or finding one already at the place where
     * the point is at (it must be within the vertex space).
     * @param fpt is the point the new TransformVertex will be at, or within the vertex space.
     * @param undoComplex is used to add an undo if a vertex was added to the VertexPool.
     * @return a TransformVertex at the point of fpt.
     */
    public TransformVertex getNewTransformVertex(FPointType fpt, UndoItemComplex undoComplex) {
        // See if the vertex exists.
        TransformVertex v = vpInternal.getTransformVertexWithinVertexSpace(fpt);
        
        // Vertex does not exist therefore create a new one.
        if (v == null) {
            v = new TransformVertex(fpt);
            undoComplex.add(vpInternal.add(v));
        }
        
        return v;
    }
    
    /** This will get a TransformVertex from the VertexPool by creating one or finding one already at the place where
     * the point is at (it must be within the vertex space).
     * @param fpt is the point the new TransformVertex will be at, or within the vertex space.
     * @return a TransformVertex at the point of fpt.
     */
    public TransformVertex getNewTransformVertex(FPointType fpt) {
        // See if the vertex exists.
        TransformVertex v = vpInternal.getTransformVertexWithinVertexSpace(fpt);
        
        // Vertex does not exist therefore create a new one.
        if (v == null) {
            v = new TransformVertex(fpt);
            vpInternal.add(v);
        }
        
        return v;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Search/Contain Methods ">
    
    /** This will get the closest line to the point which matches the flag value.
     * @param fpt is the point used to perform the operation.
     * @param flag is used to matched against the searched lines. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return the closest line to the point which matches the flag values or null if there doesn't
     * exist a line with the matching flag value.
     */
    public TransformAbstractLine getClosest(FPointType fpt, int flag) {
        float closestDistance = 2000000.0f;
        int lineId = -1;
        
        // Search for a line which matches the flag and is the closest to the point fpt.
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            if (tLine.is(flag) && tLine.ptSegDist(fpt) < closestDistance) {
                closestDistance = tLine.ptSegDist(fpt);
                lineId = tLine.getId();
            }
        }
        
        // Didn't find a line at all.
        if (lineId == -1)
            return null;
        
        return (TransformAbstractLine)get(lineId);
    }
    
    /** This will get the first TransformAbstractLine in this pool that match the value of the flag.
     * @param flag is used to see if this TransformAbstractLine is of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return the first TransformAbstractLine in this pool. Can be null if none exist.
     */
    public TransformAbstractLine getFirst(int flag) {
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            if (tLine.is(flag))
                return tLine;
        }
        
        return null;
    }
    
    /** This will get the connected curve at the tVertex for the tRMolnar or null if there doesn't exist one.
     * @param tRMolnar is the curve used to find another curve connected to it at the vertex tVertex.
     * @param tVertex is used to search for other curves at that point which are connected to tRMolnar.
     * @return a curve that is connected to tRMolnar at tVertex or null if there doesn't exit one.
     * @throws IllegalStateException if tRMolnar should have a curve connected to it. Does not throw an 
     * exception if tRMolnar is an end-curve at the point of tVertex.
     */
    public TransformRMolnar getConnectCurve(TransformRMolnar tRMolnar, TransformVertex tVertex) {
        if (tRMolnar.getControlVertex(tVertex) == tVertex)
            return null;
        
        // Get the opposite vertex and the control vertex of tVertex from the tRMolnar.
        TransformVertex tOpposite = tRMolnar.getOppositeEndVertex(tVertex);
        TransformVertex tControl = tRMolnar.getControlVertex(tVertex);
        
        // Search for a curve that has it's control point as the opposite vertex on the tRMolnar and is connected to tRMolnar at the
        // point of tVertex and the tRMolnar control point matches the opposite vertex on the curve.
        for (Iterator<TransformRMolnar> itr = searchControl(tOpposite).iterator(); itr.hasNext(); ) {
            TransformRMolnar curve = itr.next();
            if (curve.getEndVertex(tOpposite) == tVertex && tControl == curve.getOppositeEndVertex(tVertex))
                return curve;
        }
        
        throw new IllegalStateException("Unable to find a connected curve for TransformRMolnar[" + tRMolnar + "] and TransformVertex[" + tVertex + "].");
    }    
    
    /** This will get a list of TransformAbstractLines in this pool that match the value of the flag.
     * @param flag is used to search for TransformAbstractLine of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return a list of TransformAbstractLine which match the flag. Can be empty if none are found.
     */
    public LinkedList<TransformAbstractLine> search(int flag) {
        LinkedList<TransformAbstractLine> ltLines = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            if (tLine.is(flag))
                ltLines.add(tLine);
        }
        
        return ltLines;
    }

    /** This will get a list of TransformAbstractLines in this pool that match the value of the flag and
     * is connected to both vertices.
     * @param tVertex1 is the TransformVertex that each vertex found must be connected to as
     * an end vertex point.
     * @param tVertex2 is the TransformVertex that each vertex found must be connected to as
     * an end vertex point.
     * @param flag is used to search for TransformAbstractLine of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return a list of TransformAbstractLine which match the flag. Can be empty if none are found. Must connect 
     * to tVertex1 and tVertex2.
     */
    public LinkedList<TransformAbstractLine> search(TransformVertex tVertex1, TransformVertex tVertex2, int flag) {
        LinkedList<TransformAbstractLine> ltLines = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();            
            
            if (tLine.containsEnd(tVertex1) && tLine.containsEnd(tVertex2) && tLine.is(flag))
                ltLines.add(tLine);
        }
        
        return ltLines;
    }
    
    /** This will get a list of TransformAbstractLines in this pool that match the value of the flag and
     * is connected to the vertex.
     * @param tVertex is the TransformVertex that each vertex found must be connected to as
     * an end vertex point.
     * @param flag is used to search for TransformAbstractLine of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return a list of TransformAbstractLine which match the flag. Can be empty if none are found. Must connect
     * to tVertex
     */
    public LinkedList<TransformAbstractLine> search(TransformVertex tVertex, int flag) {
        LinkedList<TransformAbstractLine> ltLines = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();            
            if (tLine.containsEnd(tVertex) && tLine.is(flag))
                ltLines.add(tLine);
        }
        
        return ltLines;
    }

    /** This will get a list of TransformAbstractLines in this pool that are within the rectangle.
      * @param r is the rectangle to search for intersections.
     * @return a list containing the TransformAbstractLines that intersect the rectangle r. Can be empty if nothing was found.
     */
    public LinkedList<TransformAbstractLine> search(Rectangle2D.Float r) {
        LinkedList<TransformAbstractLine> ltLines = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            if (tLine.intersects(r))
                ltLines.add(tLine);
        }
        
        return ltLines;
    }
    
    /** This will search for this line in this LinePool.
     * @return the line that the abLineInfo represents or null.
     */
    public TransformAbstractLine search(AbstractLineInfo abLineInfo) {
        // Search for the line.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = (TransformAbstractLine)itr.next();
            if (tLine.isDupliate(abLineInfo))
                return tLine;
        }
        
        return null;
    }
    
    /** This will search for any TransformRMolnar that has 'vControl' as a control vertex.
     * @param vControl is the vertex that is used to search for any TransformRMolnar curves that contain
     * it as a control vertex.
     * @return a LinkedList containing only TransformRMolnar curves that have vControl as a control vertex.
     * Can return an empty list.
     */
    public LinkedList<TransformRMolnar> searchControl(TransformVertex vControl) {
        LinkedList<TransformRMolnar> ltCurves = new LinkedList();
        
        // Search only for RMolnars.
        for (Iterator<TransformAbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine abLine = itr.next();
            
            // Must be RMolnar and is a control vertex.
            if (abLine instanceof  TransformRMolnar && ((TransformRMolnar)abLine).isControlVertex(vControl))
                ltCurves.add((TransformRMolnar)abLine);
        }
        
        return ltCurves;
    }
    
    /** This will search for this line in this LinePool.
     * @return true if it found the line in this LinePool, else line does not exist.
     */
    public boolean contains(AbstractLineInfo abLineInfo) {
        // Search to see if this line exists.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = (TransformAbstractLine)itr.next();
            if (tLine.isDupliate(abLineInfo))
                return true;
        }
        
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying line and vertex ">
    
    /** @returns a collection view of this TransformLinePool of the underlying values in this pool.
     */
    final public Collection<TransformAbstractLine> values() {
        return super.values();
    }
    
    /** @return the TransformVertexPool for this pool.
     */
    final public TransformVertexPool getVertexPool() {
        return vpInternal;
    }
    
    /** @return a collection view of the Vertices that are used in this TransformLinePool.
     */
    final public Collection<TransformVertex> vertexValues() {
        return vpInternal.values();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Status Methods ">
    
    /** @return true if this graph is does not contain any lines.
     */
    public boolean isEmpty() {
        if (size() == 0)
            return true;
        return false;
    }
    
    /** @return the number of lines in this pool.
     */
    public int size() {
        return super.size();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public String toString() {
        return "{TransformLinePool: " + super.toString() + " VertexPool: " + vpInternal.toString() + "}";       
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Methods ">
    
    /** This will get the AbstractLineInfo about this undo item. Must be an UndoItemComplex
     * where one of its first generation children is a AbstractLineInfo.
     * @param iUndo must be an UndoItemComplex.
     * @return the AbstractLineInfo about this undo item.
     * @throws IllegalArgumentException LinePool:: Unknown InterfaceUndoItem sub class.
     */
    public AbstractLineInfo getAbstractLineInfo(InterfaceUndoItem iUndo) {
        if (iUndo instanceof UndoItemComplex == false)
            throw new IllegalArgumentException("TransformLinePool:: Unknown InterfaceUndoItem sub class.");
        
        // Convert the AbstractLine into one of the line types.
        InterfaceUndoItem undoItemNew = ((UndoItemComplex)iUndo).getFirst(UndoItemNewLine.class);
        TransformAbstractLine abLine = ((UndoItemNewLine)undoItemNew).abLine;
        return abLine.getInfo();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Line ">
    
    /** This will undo/redo a new line created.
     */
    class UndoItemNewLine implements InterfaceUndoItem {
        TransformAbstractLine abLine;
        
        UndoItemNewLine(TransformAbstractLine abLine) {
            this.abLine = abLine;
        }
        
        public void undoItem() {
            remove(abLine);
        }
        
        public void redoItem() {
            restore(abLine);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{TransformLinePool.UndoItemNewLine TransformAbstractLine[" + abLine + "]}";
        }
    }
    
    // </editor-fold>
    
}
