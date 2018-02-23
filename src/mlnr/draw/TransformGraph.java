/*
 * TransformGraph.java
 *
 * Created on April 13, 2007, 7:44 PM
 *
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.crypto.dsig.Transform;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;
import mlnr.util.XmlUtil;
import org.w3c.dom.Element;

/** This class is used to maintain a pool of TransformAbstractLines. It is used as transform container
 *  for the AbstractLines. <br>
 * This class will guarantee: <br>
 *  - At any time all AbstractLine's Vertices will be in this LinePool. <br>
 *  - All lines must point to another line or none point any lines. It is either all or none.<br>
 * Other import attributes: <br>
 *  - Lines can point to other lines in the DrawingLinePool. <br>
 *  - Because the RMolnar control vertex must be in this LinePool, there will be Vertices which
 * cannot be reached by any line in this pool.<br>
 *  - It is basically a storage of lines and vertices for transformation.<br>
 * <br>
 * This class performs simple operations on this pool of AbstactLines, such as, add, delete,
 * search, draw, etc.. <br>
 * There is no guarantee that all lines connect to each other. This class is more of a loose
 * collection of AbstractLines to operate on.
 * <br> From TransformLinePool(old): <br>
 * This class is very limited in what it can do with the lines in it. However, it does have the
 * capacity to transform the vertices. It can and will have lines not adjacent to each other, the
 * idea here is there can be multiple "graph"'s in this storage. <br>
 * Another important aspect is the ability of its pool of AbstractLines to point to other AbstractLines
 * in a DrawingLinePool. Therefore when this class is added to a DrawingLinePool, the lines in this
 * pool which point to the lines in the DrawingLinePool will replace those lines in the DrawingLinePool. <br>
 * This class must be flexible enough to be used for the Tools which add Lines, Beziers, and RMolnars lines
 * and to select items from the DrawingLinePool and to be used in the pattern generator. Its job is for
 * all operations on AbstractLines and Vertices outside the Drawing Design System. <br>
 * See TransformDesign and TransformLayer for more details. <br>
 *
 * Note: The TransformVertexPool can contain Bezier control points.
 *
 * @author Robert Molnar 2
 */
public class TransformGraph implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the id to refer to this TransformGraph. */
    private int id;
    
    /** This is the graph pointed to in the DrawingGraphPool. Only used when selecting lines and then adding
      * them back into the DrawingGraphPool. See DrawingLinePool.getSelectedItems(). */
    private int graphTo;
    
    /** Contains all lines for this TransformGraph. */
    private TransformLinePool tLinePool = new TransformLinePool();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" CreatePolygon/Constructors/Duplicate Methods ">
    
    /** Creates a new instance of TransformGraph */
    public TransformGraph() {
    }
    
    /** Create a new instance of TransformGraph with the line pool.
     * @param tLinePool will become this line pool.
     */
    private TransformGraph(TransformLinePool tLinePool) {
        this.tLinePool = tLinePool;
    }
    
    /** This will create a duplicate of this TransformGraph. If this TransformGraph was
     * created from selected items from the DrawingDesign then the duplicated
     * TransformGraph will not have those links.
     * @return a TransformGraph that is a duplicate of this TransformGraph but without
     * the line point to's.
     */
    public TransformGraph duplicate() {
        TransformLinePool tlPoolNew = new TransformLinePool();
        
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            tlPoolNew.add(tLine.getInfo());
        }
        
        return new TransformGraph(tlPoolNew);
    }
    
    /** This will create a new instance of Graph as a polygon.
     * @param ltPoints is a list of points {FPointType} for the polygon. The last point is always connected to the first one. The list must contain at least
     * three points.
     * @return a new TransformGraph that contains the polygon.
     */
    public static TransformGraph createPolygon(LinkedList<FPointType> ltPoints) {
        if (ltPoints.size() < 3)
            throw new IllegalArgumentException("createPolygon unable to create polygon of less than 3 points.");
        
        // Get the first point.
        FPointType ptPrev = ltPoints.getFirst();
        
        // Skip the first one.
        Iterator<FPointType> itr = ltPoints.iterator();
        itr.next();
        
        // Create lines from the points.
        TransformGraph tGraph = new TransformGraph();
        for (; itr.hasNext(); ) {
            FPointType ptCurr = itr.next(); 
            
            // Add the line.
            tGraph.add(new LineInfo(ptPrev, ptCurr));
            
            ptPrev = ptCurr;
        }        
        
        // Add the line from the last point to the starting point to complete the polygon.
        tGraph.add(new LineInfo(ptPrev, ltPoints.getFirst()));
        
        return tGraph;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eLineList is the element for the lineList in the RXML file.
     * @param eVertexList is the element for the vertexList in the RXML file.
     * @return LinePool from the element information.
     */
    static TransformGraph loadVersion10(Element eLineList, Element eVertexList) throws Exception {
        return new TransformGraph(TransformLinePool.loadVersion10(eLineList, eVertexList));
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eLinePool is the element for the linePool in the RXML file.
     * @return LinePool from the element information.
     */
    static TransformGraph loadVersion20(Element eLinePool) throws Exception {
        return new TransformGraph(TransformLinePool.loadVersion20(eLinePool));
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add/Remove Methods ">
    
    /** This will add an AbstractLineInfo to this TransformGraph.
     * @param abLineInfo is the AbstractLineInfo to add to this TransformGraph.
     */
    public InterfaceUndoItem add(AbstractLineInfo abLineInfo) {
        return tLinePool.add(abLineInfo);
    }
    
    /** This will add the abLineInfo to this Graph.
     * @param abLineInfo is the AbstractLineInfo to add to this Graph.
     * @param id is the line id from the DrawingGraph which the abLineInfo represents.
     * @param lineSelected is true if the added line should be selected.
     * @param vertexVisible is true if the added vertices should be visible or not. This will set all the vertices created
     * by this method to visible or invisible, including the control vertices.
     */
    void add(AbstractLineInfo abLineInfo, int id, boolean lineSelected, boolean vertexVisible) {
        tLinePool.add(abLineInfo, id, lineSelected, vertexVisible);
    }
    
    /** This will add all of the AbstractLineInfo to this TransformGraph.
     * @param ltAbstractLine is the list of AbstractLineInfo to add all of them to this TransformGraph.
     */
    public InterfaceUndoItem addAll(LinkedList<AbstractLineInfo> ltAbstractLine) {
        UndoItemComplex complexItem = new UndoItemComplex();
        
        for (Iterator<AbstractLineInfo> itr = ltAbstractLine.iterator(); itr.hasNext(); )
            complexItem.add(tLinePool.add(itr.next()));
        
        return complexItem;
    }   
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Get/Set/Is Methods ">
    
    /** This will get the AbstractLineInfo about this undo item. Must be an undo
     * item from the TransformLinePool.
     * @param iUndo must be an undo item from the TransformLinePool.
     * @return the AbstractLineInfo about this undo item.
     */
    public AbstractLineInfo getAbstractLineInfo(InterfaceUndoItem iUndo) {
        return tLinePool.getAbstractLineInfo(iUndo);
    }

    /** This will get the bezier information by getting the first Bezier within the r.
     * @param r is the area to get the bezier from.
     * @return the bezier information of the bezier found in the r.
     * @throws IllegalStateException no bezier exist in the area r.
     */
    public BezierInfo getBezierInfo(Rectangle2D.Float r) {
        for (Iterator<TransformAbstractLine> itr = tLinePool.search(r).iterator(); itr.hasNext(); ) {
            TransformAbstractLine abLine = itr.next();
            if (abLine instanceof TransformBezier) {
                return (BezierInfo)abLine.getInfo();
            }
        }
        
        throw new IllegalStateException("Bezier does not exist in the area of r: " + r + ".");
    }
    
    /** @return The bounds of the graph by using the measurements of the lines (not the vertices).
     */
    public Rectangle2D.Float getBounds2D() {
        if (tLinePool.size() == 0)
            return null;
        
        // Initial size of rectangle.
        Rectangle2D.Float fRectangle = tLinePool.getFirst(TransformLinePool.SEARCH_OFF).getBounds2D();
        if (fRectangle == null)
            return null;
        
        // Grow the rectangle.
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            Rectangle2D.Float fRect = tLine.getBounds2D();
            fRectangle.add(fRect);
        }
        
        return fRectangle;
    }
    
    /** This will get the graph pointed to.
     * @return the graph pointed to.
     */
    int getGraphTo() {
        return graphTo;
    }
    
    /** @return the number of lines in this graph.
     */
    public int getLineCount() {
        return tLinePool.size();
    }
    
    /** @return the line pool to this TransformGraph.
     */
    TransformLinePool getLinePool() {
        return tLinePool;
    }
    
    /** This will get the point from the vertex which 'fpt' is within the vertex's proximity.
     * @param fpt is the point used to search for a vertex.
     * @return the vertex's point which 'fpt' is within the vertex's proximity, or null.
     */
    public FPointType getPoint(FPointType fpt) {
        return tLinePool.getVertexPool().getPoint(fpt);
    }
    
    /** This will set the graph pointed to. It is used only when selecing lines from the DrawingGraphPool.
     * See DrawingLinePool.getSelectedItems().
     * @param id is the graph which this graph will point to. (Represents)
     */
    void setGraphTo(int id) {
        this.graphTo = id;
    }
    
    /** @return true if this graph is does not contain any lines.
     */
    public boolean isEmpty() {
        return tLinePool.isEmpty();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw/Status/To Methods ">
    
    /** This will draw the lines in this Graph.
     */
    public void draw(Graphics2D g2d, boolean erase) {
        // Loop through all lines.
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = (TransformAbstractLine)itr.next();
            tLine.draw(g2d, erase);
        }
        
        // Draw the glue points.
        if (Vertex.isControlPointsVisible()) {
            for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
                TransformVertex v = itr.next();
                v.drawGluePoint(g2d, erase);
            }
        }
    }
    
    /** This will filter the point. If it is close to a vertex than it will change the point to that vertex position.
     * @param fpt is the point to be filtered. This will modify the point if need be.
     */
    public void filterPoint(FPointType fpt) {
        FPointType filterPt = getPoint(fpt);
        if (filterPt != null) {
            fpt.x = filterPt.x;
            fpt.y = filterPt.y;
        }
    }
    
    /** @return a list of all line info from the TransformGraph.
     */
    public LinkedList<AbstractLineInfo> toListInfo() {
        LinkedList<AbstractLineInfo> ltList = new LinkedList();
        
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            ltList.add(itr.next().getInfo());
        }
        
        return ltList;
    }

    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Operational Methods ">

    /** This will add the point to the selected line (must contain only one line selected).
     * @param fpt is the point which a vertex will be placed at on the line/curve/bezier curve
     * and then two lines/curves/beziers will replace that selected line.
     * @throw IlegalStateException Unable to find a selected line.
     */
    public void addPoint(FPointType fpt) {
        // Get the first line which is closest and is selected.
        TransformAbstractLine tLine = tLinePool.getClosest(fpt, TransformLinePool.SEARCH_SELECT_ON);
        if (tLine == null)
            throw new IllegalStateException("Unable to find a selected line.");
        
        // Get the new vertex.
        if (tLinePool.getVertexPool().searchVertexSpace(fpt).isEmpty() == false) // Vertex already exists.
            return;
        FPointType newPoint = tLine.getPointOn(fpt);
        if (tLinePool.getVertexPool().searchVertexSpace(newPoint).isEmpty() == false) // Vertex already exists.
            return;
        TransformVertex tVertex = tLinePool.getNewTransformVertex(newPoint);
        
        // Remove the line from the pool (will be replacing it with two new ones).
        tLinePool.remove(tLine);
        
        // Now create the new lines that will make up for the deleted one.
        if (tLine instanceof TransformLine) {
            tLinePool.add(new TransformLine(tLine.getFirstEndVertex(), tVertex));
            tLinePool.add(new TransformLine(tVertex, tLine.getLastEndVertex()));
            
        } else if (tLine instanceof TransformBezier) {
            FPointType []arrPoints = ((TransformBezier)tLine).getDividedControlPoints(newPoint);
            // First half of the bezier curve.
            tLinePool.add(new TransformBezier(tLine.getFirstEndVertex(), tVertex, tLinePool.getBezierVertex(arrPoints[0]), tLinePool.getBezierVertex(arrPoints[1])));
            // Second half of the bezier curve.
            tLinePool.add(new TransformBezier(tVertex, tLine.getLastEndVertex(), tLinePool.getBezierVertex(arrPoints[2]), tLinePool.getBezierVertex(arrPoints[3])));
            
        } else if (tLine instanceof TransformRMolnar) {
            addPointRMolnar((TransformRMolnar)tLine, tVertex);
        }
    }
    
    /** This will perform the needed steps to add the point on the deleted tRMolnar line. It will also delete the 
     * connected RMolnars to the tRMolnar curve. Finally adding new RMolnars in place of those deleted ones.
     * @param tRMolnar is the RMolnar which needs the point added to it. It is deleted already.
     * @param tVertex is the point added.
     */
    private void addPointRMolnar(TransformRMolnar tRMolnar, TransformVertex tVertex) {
        TransformVertex tvEnd1 = tRMolnar.getFirstEndVertex();
        TransformVertex tvEnd2 = tRMolnar.getLastEndVertex();
                
        // Get all curves that have the end points as control vertices. Not this will get curves that do not have an 
        // end vertex connected to the tRMolnar this are not used later on.
        LinkedList<TransformRMolnar> ltVertices = tLinePool.searchControl(tvEnd1);
        ltVertices.addAll(tLinePool.searchControl(tvEnd2));
        
        // Update each curve with the tVertex.
        for (Iterator<TransformRMolnar> itr = ltVertices.iterator(); itr.hasNext(); ) {
            TransformRMolnar curve = itr.next();
            
            //  Remove the curve and then add it back in with the updated tVertex. First or last control vertex needs 
            // to be replaced. Only update the ones that are connected to tRMolnar by end vertex.
            if (curve.getFirstEndVertex() == tvEnd1 || curve.getFirstEndVertex() == tvEnd2) {
                tLinePool.remove(curve);
                tLinePool.add(new TransformRMolnar(curve.getFirstEndVertex(), curve.getLastEndVertex(), tVertex, curve.getLastControlVertex()));
            } else if (curve.getLastEndVertex() == tvEnd1 || curve.getLastEndVertex() == tvEnd2) {
                tLinePool.remove(curve);
                tLinePool.add(new TransformRMolnar(curve.getFirstEndVertex(), curve.getLastEndVertex(), curve.getFirstControlVertex(), tVertex));
            }
        }
        
        // Now that tRMolnar is deleted it will need to be replaced with two RMolnar curves.
        tLinePool.add(new TransformRMolnar(tRMolnar.getFirstEndVertex(), tVertex, tRMolnar.getFirstControlVertex(), tRMolnar.getLastEndVertex()));
        tLinePool.add(new TransformRMolnar(tVertex, tRMolnar.getLastEndVertex(), tRMolnar.getFirstEndVertex(), tRMolnar.getLastControlVertex()));
    }
    
    /** This will build GeneralTrees out of this TransformGraph. Note that this function will modify the TranformGraph
     * lines and vertices. First the visited flags will be modified and the autolinking code will be enabled.
     * @return a list of GeneralTrees out of this TransformGraph.
     */
    public LinkedList<GeneralTree> buildTrees() {
        BuildGeneralTree build = new BuildGeneralTree();
        return build.buildTrees();
    }    

    /** This will connect two RMolnar curves at the point fpt. This will attempt to perform the
     * connecting, however, there is not gurantee that two curves connect to the point at fpt. 
     * Also if there exists more than two non-connected RMolnar curves at that point then it will not
     * be able to connect them.
     * @param fpt is the point where the RMolnar curves need to be connected at.
     * @return TransformDesign.CONNECT_OK or TransformDesign.CONNECT_TOO_MANY_CURVES or TransformDesign.CONNECT_NOTHING_CONNECTED.
     */
    public int connect(FPointType fpt) {
        // Get the vertex to perform on.
        LinkedList<TransformVertex> ltVertices = tLinePool.getVertexPool().searchVertexSpace(fpt);
        if (ltVertices.size() == 0)
            return TransformDesign.CONNECT_NOTHING_CONNECTED;
        TransformVertex tVertex = ltVertices.getFirst();
        
        // List of RMolnar curves at the point of tVertex that are end-curves.
        LinkedList<TransformRMolnar> ltRMolnars = TransformRMolnar.filterEndAt(tLinePool.search(tVertex, TransformLinePool.SEARCH_OFF), tVertex);

        // Make sure number of curves are correct.
        if (ltRMolnars.size() < 2)
            return TransformDesign.CONNECT_NOTHING_CONNECTED;
        else if (ltRMolnars.size() > 2)
            return TransformDesign.CONNECT_TOO_MANY_CURVES;

        // These two curves are end-curves that need to be connected to.
        TransformRMolnar curve1 = ltRMolnars.pop();
        TransformRMolnar curve2 = ltRMolnars.pop();

        // Remove them from the line pool.
        tLinePool.remove(curve1);
        tLinePool.remove(curve2);

        // Add the updated curves back into the line pool.
        tLinePool.add(new TransformRMolnar(curve1.getOppositeEndVertex(tVertex), tVertex, 
            curve1.getControlVertex(curve1.getOppositeEndVertex(tVertex)), curve2.getOppositeEndVertex(tVertex)));
        tLinePool.add(new TransformRMolnar(curve2.getOppositeEndVertex(tVertex), tVertex, 
            curve2.getControlVertex(curve2.getOppositeEndVertex(tVertex)), curve1.getOppositeEndVertex(tVertex)));
        
        return TransformDesign.CONNECT_OK;
    }
    
    /** This will disconnect all RMolnar curves at the point fpt. 
     * @param fpt is the point where the RMolnar curves need to be disconnected at.
     */
    public void disconnect(FPointType fpt) {
        // Get the vertex to perform on.
        LinkedList<TransformVertex> ltVertices = tLinePool.getVertexPool().searchVertexSpace(fpt);
        if (ltVertices.size() == 0)
            return;
        TransformVertex tVertex = ltVertices.getFirst();
        
        // List of RMolnar curves at the point of tVertex.
        LinkedList<TransformRMolnar> ltRMolnars = TransformRMolnar.filterRMolnar(tLinePool.search(tVertex, TransformLinePool.SEARCH_OFF));
        
        // For each curve. Delete it. Add it back in as an end-curve at the point of fpt.
        for (Iterator<TransformRMolnar> itr = ltRMolnars.iterator(); itr.hasNext(); ) {
            TransformRMolnar curve = itr.next();
            
            // Remove curve.
            tLinePool.remove(curve);
            
            // Now add it back in but as an end-curve.
            tLinePool.add(new TransformRMolnar(curve.getOppositeEndVertex(tVertex), tVertex,
                curve.getControlVertex(curve.getOppositeEndVertex(tVertex)), tVertex));
        }
    }    

    /** This will perform the pulling of the line apart and set it up to allow the fpt to be moveable.
     * @param fpt is the point where the user clicked on the line which needs to be pulled apart.
     */
    public void pullLineApart(FPointType fpt) {
        // Get the two vertices that are moveable.
        LinkedList<TransformVertex> ltTransforms = tLinePool.getVertexPool().search(TransformLinePool.SEARCH_TRANSFORMABLE_ON);
        
        // Remove any beizer control movable points and set them to non-movable.
        for (Iterator<TransformVertex> itrV = ltTransforms.iterator(); itrV.hasNext(); ) {
            TransformVertex tv = itrV.next();
            if (tv.is(TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON)) {
                tLinePool.getVertexPool().get(tv.getId()).set(TransformLinePool.SEARCH_TRANSFORMABLE_OFF);
                itrV.remove();
            }
        }
        
        if (ltTransforms.size() != 2)
            throw new IllegalStateException("Must contain only two moveable vertices. size: " + ltTransforms.size());
        
        // Get the Line which will be pulled apart.
        LinkedList<TransformAbstractLine> ltPullApart = tLinePool.search(ltTransforms.getFirst(), ltTransforms.getLast(), TransformLinePool.SEARCH_OFF);
        if (ltPullApart.isEmpty())
            throw new IllegalStateException("The two moveable vertices do not have a line connected between them.");
        TransformAbstractLine tLinePullApart = ltPullApart.getFirst();
        
        // Get the closest moveable vertex to the point fpt and the other moveable vertex (opposite).
        TransformVertex tClosest = ltTransforms.getFirst();
        if (ltTransforms.getFirst().getPoint().distance(fpt) > ltTransforms.getLast().getPoint().distance(fpt))
            tClosest = ltTransforms.getLast();
        TransformVertex tOpposite = tLinePullApart.getOppositeEndVertex(tClosest);
        
        // This is the vertex that will represent the fpt point.
        TransformVertex tNewVertex = new TransformVertex(fpt);
        tLinePool.getVertexPool().add(tNewVertex);
        
        // Remove the line to be pulled apart. Add the Line back in.
        tLinePool.remove(tLinePullApart);        
        if (tLinePullApart instanceof TransformLine) {
            tLinePool.add(new TransformLine(tOpposite, tNewVertex));
            
        } else if (tLinePullApart instanceof TransformBezier) {
            TransformBezier tBezier = (TransformBezier)tLinePullApart;
            tLinePool.add(new TransformBezier(tOpposite, tNewVertex, tBezier.getControlVertex(tOpposite), tBezier.getControlVertex(tClosest)));
            
        } else if (tLinePullApart instanceof TransformRMolnar) {
            TransformRMolnar tRMolnar = (TransformRMolnar)tLinePullApart;
            tLinePool.add(new TransformRMolnar(tOpposite, tNewVertex, tRMolnar.getControlVertex(tOpposite), tNewVertex));
            
            // Delete connected curve at the closest vertex (tClosest) and then add it back in but as an end curve.
            TransformRMolnar tConnectRMolnar = tLinePool.getConnectCurve(tRMolnar, tClosest);
            if (tConnectRMolnar != null) {
                tLinePool.remove(tConnectRMolnar);
                tLinePool.add(new TransformRMolnar(tClosest, tConnectRMolnar.getOppositeEndVertex(tClosest), 
                    tClosest, tConnectRMolnar.getControlVertex(tConnectRMolnar.getOppositeEndVertex(tClosest))));
            }
            
            // Delete the connected curve at the opposite vertex (tOpposite) and then add it back in but with the updated new vertex.
            tConnectRMolnar = tLinePool.getConnectCurve(tRMolnar, tOpposite);
            if (tConnectRMolnar != null) {
                tLinePool.remove(tConnectRMolnar);
                tLinePool.add(new TransformRMolnar(tOpposite, tConnectRMolnar.getOppositeEndVertex(tOpposite),
                    tNewVertex, tConnectRMolnar.getControlVertex(tConnectRMolnar.getOppositeEndVertex(tOpposite))));
            }            
        }
        
        // Turn original movable vertices to off and the new added one to moveable.
        tClosest.set(TransformLinePool.SEARCH_TRANSFORMABLE_OFF);
        tOpposite.set(TransformLinePool.SEARCH_TRANSFORMABLE_OFF);
        tNewVertex.set(TransformLinePool.SEARCH_TRANSFORMABLE_ON);
    }
    
    /** This will validate the RMolnar curve's control points. If they do not connect to another curve then the curve will become an end curve at that control points
     * (control point will be set to the end point).
     */
    public void validateCurvesControlPoints() {
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine abLine = itr.next();            
            if (abLine instanceof TransformRMolnar == false)
                continue;
            TransformRMolnar trmolnar = (TransformRMolnar)abLine;
            
            // The Control Vertex does not connect to any other curve then remove the vertex from the pool and set the control point as the end point.
            if (tLinePool.search(trmolnar.getFirstControlVertex(), TransformLinePool.SEARCH_OFF).size() == 0) {
                TransformVertex v = trmolnar.getFirstControlVertex();
                
                // Remove it from the pool, but make sure it exists already. There is a case where the vertex to be removed was already removed because it
                // was used by another curve and that curve had it removed (thus two curves had the same control point).
                if (tLinePool.getVertexPool().contains(v))
                    tLinePool.getVertexPool().remove(v);
                
                // Now set control point to the end point.
                trmolnar.makeIntoEndCurve(v);
            }
        }
    }
        
    /** This will update the bezier where the end points match this bezierInfo's ones.
     * @param the bezierInfo information about the bezier curve in this TransformDesign. It will only update
     * the control points.
     * @throws IllegalStateException bezier curve does not exist in pool.
     */
    public void updateBezierInfo(BezierInfo bezierInfo) {
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine abLine = itr.next();
            if (abLine instanceof TransformBezier == false)
                continue;
            
            if (abLine.getFirstEndVertex().getPoint().equals(bezierInfo.getEndPoint1()) &&
                abLine.getLastEndVertex().getPoint().equals(bezierInfo.getEndPoint2())) {
                ((TransformBezier)abLine).update(bezierInfo);
                return;
            }
        }
        
        throw new IllegalStateException("Bezier curve " + bezierInfo + " does not exist in this pool.");
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Select/Visited/Visible/Transform Methods ">
    
    /** This will set each vertex transformable.
     * @param transformable is true if vertices should be transformable or false they should not be transformable.
     */
    public void setAllTransformable(boolean transformable) {
        int flag = (transformable == true ? TransformLinePool.SEARCH_TRANSFORMABLE_ON : TransformLinePool.SEARCH_TRANSFORMABLE_OFF);
        
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) 
            itr.next().set(flag);
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); )
            itr.next().set(flag);
    }
        
    /** This will set all bezier control points as transmoveable.
     * @param transformable is the value which the bezier control points are to be set as.
     */
    public void setBeizerControls(boolean transformable) {
        int flag = TransformLinePool.SEARCH_TRANSFORMABLE_ON;
        if (!transformable)
            flag = TransformLinePool.SEARCH_TRANSFORMABLE_OFF;
        
        // Set the transformable flag.
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); )  {
            TransformVertex tVertex = itr.next();
            if (!tVertex.isControlVertex())
                continue;
            tVertex.set(flag);
        }
    }    
    
    /** This will set the vertex at 'fpt' to transform.
     * @param fpt is the position within the vertex proximity to set as transformable.
     * @param transformable is true if the vertex should be transformable or false it should not be.
     * @throws IllegalArgumentException if fpt does not match a vertex point.
     */
    void setTransformPoint(FPointType fpt, boolean transformable) {
        LinkedList<TransformVertex> ltVertices = tLinePool.getVertexPool().searchVertexSpace(fpt);        
        
        if (ltVertices.isEmpty())
            throw new IllegalArgumentException("The point: " + fpt + " does not match a vertex in pool.");
        
        int flag = ((transformable == true) ? TransformLinePool.SEARCH_TRANSFORMABLE_ON : TransformLinePool.SEARCH_TRANSFORMABLE_OFF);
        for (Iterator<TransformVertex> itr = ltVertices.iterator(); itr.hasNext(); ) 
            itr.next().set(flag);        
    }
    
    /** This will set the vertex at 'fpt' to visible.
     * @param fpt is the position within the vertex proximity to set as visible.
     * @param visible is true if the vertex should be visible, else false it should not be.
     * @throws IllegalArgumentException if fpt does not match a vertex point.
     */
    void setVisiblePoint(FPointType fpt, boolean visible) {
        LinkedList<TransformVertex> ltVertices = tLinePool.getVertexPool().searchVertexSpace(fpt);        
        
        if (ltVertices.isEmpty())
            throw new IllegalArgumentException("The point: " + fpt + " does not match a vertex in pool.");
        
        int flag = ((visible == true) ? TransformLinePool.SEARCH_VISIBLE_ON : TransformLinePool.SEARCH_VISIBLE_OFF);
        for (Iterator<TransformVertex> itr = tLinePool.getVertexPool().searchVertexSpace(fpt).iterator(); itr.hasNext(); )
            itr.next().set(flag);
    }
    
    /** @return true if all lines in this Graph have been visited, else false there exists lines that have not been visited.
     */
    boolean isVisited() {
        if (tLinePool.getFirst(TransformLinePool.SEARCH_VISIT_OFF) != null)
            return false;
        return true;
    }
    
    /** This will set the visited flag for all lines and vertices in this TransformGraph.
     * @param visited is true if all lines and vertices should be visited, else false they should be set to false.
     */
    public void setVisited(boolean visited) {
        int flag = ((visited == true) ? TransformLinePool.SEARCH_VISIT_ON : TransformLinePool.SEARCH_VISIT_OFF);
                
        // Set lines visited.
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); )
            itr.next().set(flag);
        
        // Set vertices visited.
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); )
            itr.next().set(flag);
    }
    
    /** This will check to see if this point is contained in the TransformGraph which
     * have not been visited. This point 'fpt' will be checked against all unvisited points and will
     * return true if that point is within proximity.
     * @param fpt is the point to check against.
     * @return true, if the point is within proximity of an unvisited vertex in this TransformGraph,
     * else false.
     */
    public boolean containsUnvisited(FPointType fpt) {
        for (Iterator<TransformVertex> itr = tLinePool.getVertexPool().searchVertexSpace(fpt).iterator(); itr.hasNext(); ) {
           TransformVertex v = itr.next();
           
           if (v.is(TransformLinePool.SEARCH_VISIT_OFF))
               return true;
        }
        
        return false;
    }
    
    /** This will get the next unvisited line. There is no guarantee which line will be returned.
     * @return the next available line that has not been visited, else NULL no more unvisited lines.
     */
    AbstractLineInfo getNextUnvisitedLine() {
        TransformAbstractLine tLine = tLinePool.getFirst(TransformLinePool.SEARCH_VISIT_OFF);
        if (tLine == null)
            return null;
        return tLine.getInfo();
    }
    
    /** This will get a list of line information for all lines that are connected at the point 'atPoint'
     * and the lines must not be visited.
     * @param atPoint is the position to check for unvisited lines at.
     * @return a list of line information for each line found to have 'atPoint' as an end point. Can
     * be empty if none were found.
     */
    LinkedList<AbstractLineInfo> getUnvisitedLines(FPointType atPoint) {
        LinkedList<AbstractLineInfo> list = new LinkedList();
        
        // Search all lines.
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            
            // Add lines that connect to the vertex and the line has not been visited.
            if (tLine.is(TransformLinePool.SEARCH_VISIT_OFF) && tLine.containsEnd(atPoint))
                list.add(tLine.getInfo());
        }
        
        return list;
    }
    
    /** This will set the line which the 'AbstractLineInfo' represents to 'visited'. It will also
     * set the end point vertices to visited too.
     * @param abLineInfo is the line information which should represent a line in this graph. Its
     * points must match and control points must match a line in this graph.
     * @param visited is true if it should set the line found as visited, or false if it should be unset.
     * @throws IllegalArgumentException if a matching line cannot be found for the 'AbstractLineInfo'.
     */
    void setVisited(AbstractLineInfo abLineInfo, boolean visited) {
        int flag = ((visited == true) ? TransformLinePool.SEARCH_VISIT_ON : TransformLinePool.SEARCH_VISIT_OFF);
        
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine tLine = itr.next();
            
            // If the line matches the line information then set as visited and exit.
            if (tLine.equals(abLineInfo)) {
                tLine.set(flag);
                return;
            }
        }
        
        throw new IllegalArgumentException("Unable to match the line information to any line.");
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Calculate And Transform Methods ">
        
    /** This will center the graph to the position fptCenterPt.
     * @param fptCenterPt is the position to center this graph to.
     */
    public void center(FPointType fptCenterPt) {
        Rectangle2D.Float rect = getBounds2D();
        FPointType fptCenterGraph = new FPointType((float)rect.getCenterX(), (float)rect.getCenterY());
        translate(fptCenterPt.x - fptCenterGraph.x, fptCenterPt.y - fptCenterGraph.y);
    }
        
    /** This will finalize the movements of the vertices and lines.
     */
    public void finalizeMovement() {
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            itr.next().saveCurrent();
        }
    }
    
    /** This will rotate the design by the radOffet. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param radOffset is the offset radians to rotate.
     */
    public void rotate(FPointType fptCenter, float radOffset) {
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            TransformVertex v = itr.next();
            v.rotate(fptCenter, radOffset);
        }
        
        validateDrawing();
    }
    
    /** This will move the design by the xOffset, yOffset. No pre-compute needed.
     * @param xOffset is the offset in the x direction to move in.
     * @param yOffset is the offset in the x direction to move in.
     */
    public void translate(float xOffset, float yOffset) {        
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            TransformVertex v = itr.next();
            v.translate(xOffset, yOffset);
        }
        
        validateDrawing();
    }
    
    /** This will mirror the design. No pre-compute needed.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param mirrorType can be MIRROR_HORIZONTAL, MIRROR_VERTICAL, MIRROR_DEGREE.
     * @param line2D is the line which the vertex will be mirrored from.
     * @param rad is the degree to mirror at, only used for MIRROR_DEGREE.
     */
    public void mirror(FPointType fptCenter, int mirrorType, Line2D.Float line2D, float rad) {
        if (mirrorType == TransformDesign.MIRROR_HORIZONTAL) {
            for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
                TransformVertex v = itr.next();
                v.mirrorHorizontal(fptCenter);
            }
        } else if (mirrorType == TransformDesign.MIRROR_VERTICAL) {
            for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
                TransformVertex v = itr.next();
                v.mirrorVertical(fptCenter);
            }
        } else if (mirrorType == TransformDesign.MIRROR_DEGREE) {
            for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
                TransformVertex v = itr.next();
                v.mirrorDegree(line2D, rad);
            }
        } else
            throw new IllegalArgumentException("Unknown mirror type " + mirrorType + ".");
        
        validateDrawing();
    }
    
    /** This will scale the design by xScale, yScale. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param xScale is x scale (1.0 is no change in x size).
     * @param yScale is y scale (1.0 is no change in y size).
     */
    public void resize(FPointType fptCenter, float xScale, float yScale) {
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            TransformVertex v = itr.next();
            v.resize(fptCenter, xScale, yScale);
        }
        
        validateDrawing();
    }
    
    /** This will pre-compute each vertex in this design to the fptCenter.
     * @param fptCenter is the position where each vertex should be calculated from.
     */
    public void calculate(FPointType fptCenter) {
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            TransformVertex v = itr.next();
            v.calculate(fptCenter);
        }
    }
    
    /** This will validate each Line, RMolnar, and Bezier. It will update the line's structures for drawing.
     */
    public void validateDrawing() {
        for (Iterator<TransformAbstractLine> itr = tLinePool.values().iterator(); itr.hasNext(); ) {
            TransformAbstractLine abLine = itr.next();
            abLine.validate();
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Auto Link Methods ">
    
    /** This will build the auto linking lists for each line where a vertex is sitting on a line that needs to 
     * be linked to it. This should be called before building the GeneralTrees if auto linking of vertices to
     * lines that are not connected should be enabled.
     */
    public void buildAutoLink() {
        // For each vertex that sits on a line will need to be added to that line's list of auto-connected lines.
        for (Iterator<TransformVertex> itr = tLinePool.vertexValues().iterator(); itr.hasNext(); ) {
            TransformVertex v = itr.next();
            
            if (v.is(TransformLinePool.SEARCH_BEZIER_CONTROL_PT_ON))
                continue;
            
            // Check all lines to see if this vertex is sitting on the line but is not an end vertex.
            for (Iterator<TransformAbstractLine> itrLine = tLinePool.search(v.getGluePoint()).iterator(); itrLine.hasNext(); ) {
                TransformAbstractLine tLine = itrLine.next();
                
                // Do not auto link vertices that are end point vertices.
                if (tLine.containsEnd(v))
                    continue;
                
                // Get the new position this vertex should be at.
                float parametericT = tLine.getClosestParameterT(v.getPoint());
                FPointType newPosition = tLine.getParameterValue(parametericT);
                v.setAutoLinkInfo(tLine, parametericT);
                
                // Move the vertex to this position.
                v.translateTo(newPosition);
                v.saveCurrent();
                
                // Add the vertex to the parameteric list of vertices to the line.
                tLine.addAutoLinkVertex(v);
                
                // Vertex can only rest on one line at a time.
                break;
            } 
        }
    }
    
    // </editor-fold>
     
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
   
    public String toString() {
        return "\n{TransformGraph id: " + id + " graphTo: " + graphTo + " tLinePool[\n" + tLinePool + "]}";
    }
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" InterfacePoolObject Methods ">
    
    public int getId() {
        return this.id;
    }
    
    public int getZDepth() {
        return 1;
    }
    
    public void setZDepth(int zDepth) {
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int compareTo(Object o) {
        return 0;
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc="Class BuildGeneralTree">

    /** This class is used to build the GeneralTrees from the TransformGraph.
     */
    class BuildGeneralTree {
        
        /** This will build GeneralTrees out of this TransformGraph.
         * @return a list of GeneralTrees built out of the TransformGraph.
         */
        LinkedList<GeneralTree> buildTrees() {
            LinkedList<GeneralTree> ltGeneralTrees = new LinkedList();
            buildAutoLink();
            setVisited(false);
            
            // Create a GeneralTree out of each unvisited line.
            TransformAbstractLine abRoot;            
            while ((abRoot = tLinePool.getFirst(TransformLinePool.SEARCH_VISIT_OFF)) != null) {
                // Create the tree and set the root of it.
                GeneralTree gTree = new GeneralTree();
                GeneralTreeNode root = new GeneralTreeNode(null, abRoot.getFirstEndVertex());
                gTree.setRoot(root);                
                
                // Process the root.
                buildNode(root);
                
                // Add the tree to the list of trees.
                ltGeneralTrees.add(gTree);
            }
            
            return ltGeneralTrees;
        }
        
        /** This will build the node for this GeneralTree.
         * @param parent is the GeneralTreeNode that needs to be built.
         */
        private void buildNode(GeneralTreeNode parent) {
            // Get a list of lines that have not been visited at the point of the parent node.
            LinkedList<TransformAbstractLine> ltLines = tLinePool.search(parent.getNode(), TransformLinePool.SEARCH_VISIT_OFF);
                
            // Make sure to add in the line this vertex is auto linked to and is not visited.
            if (parent.getNode().isAutoLinkVertex()) { 
                TransformVertex aVertex = parent.getNode();
                if (aVertex.getParametericLine().is(TransformLinePool.SEARCH_VISIT_OFF))
                    ltLines.add(aVertex.getParametericLine());
            }
            
            // Get lines that are not visited and are connected to this vNode.
            for (Iterator<TransformAbstractLine> itr = ltLines.iterator(); itr.hasNext(); ) {
                TransformAbstractLine abLine = itr.next();
                
                // Traverse the line to the next vertex. Incase the vNode is an auto linked vertex and the current line could go either
                // way then the line could possibly be traversed twice since the vNode breaks up the line.
                TransformVertex v = null;
                while ((v = abLine.getNextNonVisitedSegment(parent.getNode(), true)) != null) {
                    // Create the child node and segment to the child node.
                    GeneralTreeNode childNode = new GeneralTreeNode(parent, v);
                    GeneralTreeSegment segment = new GeneralTreeSegment(abLine, parent.getNode(), v);
                    segment.setNodes(parent, childNode);
                    
                    // Now add the segment to this node.
                    parent.add(segment);
                    
                    // Traverse the current vertex.
                    buildNode(childNode);

                }
            }
        }
    }
    
    // </editor-fold>
        
}
