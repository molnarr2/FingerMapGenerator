/*
 * DrawingGraph.java
 *
 * Created on April 13, 2007, 7:43 PM
 *
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.area.Intersections;
import mlnr.fngr.FingerSamplingDistance;
import mlnr.gui.geom.RMolnarCubicCurve2D;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;
import mlnr.util.XmlUtil;
import org.w3c.dom.Element;

/** This class design goal is to perform complex operations on the LinePool. These involve adding,
 * deleting, adding points, etc.. This class does not transform the vertices in it, it only operates
 * at the line level. See the TransformGraph for transformation of the vertices. <br>
 * This class will always guarantee: <br>
 *  - It is a well-formed "graph" in graph theory, where all lines are adjacent to one another. You
 * can get from any line to any other line by traveling to it one line at a time. <br>
 *  - The above statement is even true when deleting lines from this graph because it will create
 * a graph pool of each well-formed "graph" except one and will return that graph pool. Thus this
 * guarantee will always stay true. <br>
 * <br>
 * Please check the guarantee of the DrawingLinePool too as those apply to this class too. <br>
 * <br> The other goal of this class is to provide a solid object that adheres to well-formed "graph"
 * theory. Secondary goals for this class is to provide easy debugging and testing, to keep this
 * class as simple as possible.
 *<br> This class is built around the idea that it needs to be completely black box. There cannot
 * be any holes in getting to the actual lines and vertices that make up this class.
 * @author Robert Molnar 2
 */
public class DrawingGraph implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the id to refer to this Graph. */
    private int id;
    
    /** Contains all lines for this Graph. */
    private DrawingLinePool lPoolLine;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors and Static Constructor Methods">
    
    /** Creates a new instance of DrawingGraph */
    public DrawingGraph() {
        lPoolLine = new DrawingLinePool();
    }
    
    /** Creates a new instance of DrawingGraph by using the list of lines.
     * @param ltLines must follow one rule, that is all lines must connect to each other. If there exists curves
     * that have a control not connected to another curve then the control point will be set to the respective end
     * point of the curve.
     * @throws IllegalArgumentException if the list of lines contain lines that are not connected to
     * each other. All lines must connect to each other.
     */
    public DrawingGraph(LinkedList<AbstractLineInfo> ltLines) {
        if (ltLines.isEmpty())
            lPoolLine = new DrawingLinePool();
        else
            lPoolLine = new DrawingLinePool(ltLines);
    }    
    
    /**  This will create a list of DrawingGraph from a TransformGraph by searching for an unvisited line
     * and traversing it until a DrawingGraph is completed. Each line traversed is set to visited. If
     * there does not exist any unvisited lines from the TransformGraph then it will return an empty list. Because
     * of the restrictiveness of a DrawingGraph and the loose nature of a TransformGraph, it is possible
     * that multiple DrawingGraphs can come from one TransformGraph. This function could create many DrawingGraphs
     * from the TransformGraph.
     * @param tGraph is the TransformGraph used to create a restrict DrawingGraphs.
     * @return a list of DrawingGraph made from the TransformGraph by traversing the unvisited lines while
     * setting them visited. This will return empty if the TransformGraph does not have anymore unvisited
     * lines.  */
    public static LinkedList<DrawingGraph> newGraphsFromUnvisited(TransformGraph tGraph) {
        LinkedList<DrawingGraph> ltGraphs = new LinkedList<DrawingGraph>();
        
        // Create as many seperate DrawingGraphs from the TransformGraph until TransformGraph has been completely visited.
        while (true) {
            
            // Get the first unvisited Line from the TransformGraph.
            AbstractLineInfo abLineInfo = tGraph.getNextUnvisitedLine();
            if (abLineInfo == null)
                break;
            
            // This will contain a list of new lines that will make up a new DrawingGraph.
            LinkedList<AbstractLineInfo> ltNewLines = new LinkedList<AbstractLineInfo>();
            
            // Build the list of new lines and set them visited in the TransformGraph.
            recursiveNewGraph(ltNewLines, tGraph, abLineInfo.getEndPoint1());
            
            // Create the new graph from the list of lines.
            ltGraphs.add(new DrawingGraph(ltNewLines));
        }
        
        return ltGraphs;
    }
    
    /** This is a recursive function used only by the newGraphFromUnvisited() function and addUnvisited() function.
     * It will get a list of lines at the point 'atPoint' from the 'tGraph' which are not visited yet. Each line
     * visited is added to the 'ltNewLines' and will be traversed (Set to visited in the 'tGraph').
     * @param ltNewLines is the list of lines traversed (the 'tGraph' will have these lines as visited).
     * @param tGraph is the TransformGraph being traversed.
     * @param atPoint is the current position in the TransformGraph being traversed.
     *
     */
    private static void recursiveNewGraph(LinkedList<AbstractLineInfo> ltNewLines, TransformGraph tGraph, FPointType atPoint) {
        LinkedList<AbstractLineInfo> ltLines = tGraph.getUnvisitedLines(atPoint);
        
        // Set all visited.
        for (Iterator itr = ltLines.iterator(); itr.hasNext(); ) {
            AbstractLineInfo abLineInfo = (AbstractLineInfo)itr.next();
            
            // Line is now visited.
            tGraph.setVisited(abLineInfo, true);
        }
        
        // Attempt to traverse each line.
        for (Iterator itr = ltLines.iterator(); itr.hasNext(); ) {
            AbstractLineInfo abLineInfo = (AbstractLineInfo)itr.next();
            
            // Traversed line.
            ltNewLines.add(abLineInfo);
            
            // Go on to next level.
            recursiveNewGraph(ltNewLines, tGraph, abLineInfo.getOppositePoint(atPoint));
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add/Contain/To Methods ">
    
    /** This will merge the graph g into this Graph.
     * @param g is the graph to add into this graph.
     * @return an undo for this operation on this graph.
     */
    public InterfaceUndoItem add(DrawingGraph g) {
        return lPoolLine.merge(g.toAbstractLineInfo());
    }
    
    /** This will add all unvisited lines from 'tGraph' that connect to this graph by searching for all lines that
     * have not been visited and connect to this graph. There could be multiple graphs in the 'tGraph' that
     * need to be connected to this graph. Each line traversed is set to visited. This function only needs to
     * be called one time per TransformGraph.
     * @param tGraph is the TransformGraph used to add to this graph.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem addUnvisited(TransformGraph tGraph) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Loop until all lines that connect to this graph are processed.
        while (true) {
            // This will get a connect point to this graph.
            FPointType fptConnect = lPoolLine.getVertexPool().getNextUnvisitedPoint(tGraph);
            
            if (fptConnect == null)
                break;
            
            // Get the first line which is connected between graphs.
            LinkedList<AbstractLineInfo> ltLineInfo = tGraph.getUnvisitedLines(fptConnect);
            if (ltLineInfo.isEmpty())
                break;
            AbstractLineInfo abLineInfo = ltLineInfo.getFirst();
            
            // This will contain a list of new lines that will make up graph which will be added
            // to this DrawingGraph.
            LinkedList<AbstractLineInfo> ltNewLines = new LinkedList<AbstractLineInfo>();
            
            // Build the list of new lines and set them visited in the TransformGraph.
            recursiveNewGraph(ltNewLines, tGraph, abLineInfo.getEndPoint1());
            
            // Now merge the list of lines.
            undoComplex.add(lPoolLine.merge(ltNewLines));
            
        }
        
        return undoComplex;
    }
    
    /** This will check to see if this graph contains any points in the graph g. The points are compared
     * by the vertex proximities.
     * @param g is the graph to see if there are any points in it's VertexPool that
     * match the ones in this graph's VertexPool.
     * @return true if there are points in this graph which connect to any points in the graph g within
     * vertex proximities.
     */
    boolean contains(DrawingGraph g) {
        for (Iterator<Vertex> itrAway = g.lPoolLine.getVertexPool().values().iterator(); itrAway.hasNext(); ) {
            Vertex vAway = itrAway.next();
            
            for (Iterator<Vertex> itrHome = lPoolLine.vertexValues().iterator(); itrHome.hasNext(); ) {
                Vertex vHome = itrHome.next();
                
                // The points match each other, therefore this VertexPool contains vp.
                if (vHome.isVertexSpace(vAway))
                    return true;
            }
        }
        
        return false;
    }
    
    /** This will check to see if the DrawingGraph contains any points in the TransformGraph which
     * have not been visited. It will check all vertices in the DrawingGraph to the unvisited vertices
     * in the TransformGraph.
     * @param g is the graph to see if there are any unvisited points in it's VertexPool that
     * are within proximity of the this graph's VertexPool points.
     */
    boolean containsUnvisited(TransformGraph g) {
        return lPoolLine.getVertexPool().containsUnvisited(g);
    }
    
    /** This will create a list of AbstractLineInfo for each line in this DrawingGraph.
     * @return a list of AbstractLineInfo of each line in this DrawingGraph.
     */
    public LinkedList<AbstractLineInfo> toAbstractLineInfo() {
        return lPoolLine.toLineInfo(DrawingLinePool.SEARCH_OFF);
    }
    
    /** This will create a list of AbstractLineInfo for each visible line in this DrawingGraph.
     * @return a list of AbstractLineInfo of each visible line in this DrawingGraph.
     */
    public LinkedList<AbstractLineInfo> toAbstractLineInfo(boolean visible) {
        if (visible)
            return lPoolLine.toLineInfo(DrawingLinePool.SEARCH_VISIBLE_ON);
        return lPoolLine.toLineInfo(DrawingLinePool.SEARCH_VISIBLE_OFF);
    }    
    
    /** This will produce a list of DrawingGraphs from the unselected lines. Each DrawingGraph produced
     * will not connect to another DrawingGraph and will be the largest possible graph. Therefore it will
     * produce the least amount of DrawingGraphs. <br>
     * WARNING: This will modify the visited flag for the lines.
     * @return a list of DrawingGraph(s) from the unselected lines. Can be empty and can be a single one.
     */
    LinkedList<DrawingGraph> toListUnselected() {
        LinkedList<DrawingGraph> ltGraphs = new LinkedList<DrawingGraph>();
        lPoolLine.set(DrawingLinePool.SEARCH_VISIT_OFF);
        
        // Visit each line that is unselected and create a list of AbstractLineInfo from them. Then create the
        // DrawingGraph from those lists.
        while(lPoolLine.has(DrawingLinePool.SEARCH_SELECT_OFF | DrawingLinePool.SEARCH_VISIT_OFF)) {
            AbstractLine abLine = lPoolLine.getFirst(DrawingLinePool.SEARCH_SELECT_OFF | DrawingLinePool.SEARCH_VISIT_OFF);
            
            // Traverse the graph and create a list of line information of each unselected line. Will not traverse
            // selected lines.
            LinkedList<AbstractLineInfo> ltLineInfo = new LinkedList<AbstractLineInfo>();
            lPoolLine.traverse(ltLineInfo, abLine.getFirstEndVertex(), DrawingLinePool.SEARCH_SELECT_OFF);
            
            // Create a new DrawingGraph.
            ltGraphs.add(new DrawingGraph(ltLineInfo));
        }
        
        return ltGraphs;
    }
    
    public String toString() {
        return "\n{DrawingGraph id[" + id + "] lPoolLine[\n" + lPoolLine + "]}";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
    
    /** This will draw the graph's lines to the 'g2D' graphics.
     * @param g2D is the Graphics2D to draw the lines into.
     */
    public void draw(Graphics2D g2D) {
        // Draw the lines.
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().draw(g2D);
        
        // Draw the glue points.
        if (Vertex.isControlPointsVisible()) {
            for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); )
                itr.next().drawGluePoint(g2D);
        }
        
        // Print the line and vertex numbers.
        if (DrawingDesign._DEBUG_PRINT_LINES) {
            Color oldColor = g2D.getColor();
            Font fOld = g2D.getFont();
            
            Font fNew = new Font(null, Font.PLAIN, 1);
            g2D.setFont(fNew.deriveFont(0.5f));
            
            // Loop through the vertices and print them out.
            for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); ) {
                itr.next().debugDrawNumber(g2D);
            }
            
            // Loop through the lines and print them out.
            for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
                itr.next().debugDrawNumbers(g2D);
            }
            
            g2D.setColor(oldColor);
            g2D.setFont(fOld);
        }
    }
    
    /** This will draw all lines in this LinePool.
     * @param g2d is the graphics class.
     */
    public void drawBitmap(Graphics2D g2d) {
        // Draw the lines.
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().draw(g2d);                
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eGraph is the element for the graph in the RXML file.
     * @return a list of DrawinGraph from the element information.
     */
    static LinkedList<DrawingGraph> loadVersion10(Element eGraph) throws Exception {
        return newGraphsFromUnvisited(TransformGraph.loadVersion10(XmlUtil.getElementByTagName(eGraph, "lineList"),
            XmlUtil.getElementByTagName(eGraph, "vertexList")));
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eGraph is the element for the graph in the RXML file.
     * @return Graph from the element information.
     */
    static LinkedList<DrawingGraph> loadVersion20(Element eGraph) throws Exception {
        return newGraphsFromUnvisited(TransformGraph.loadVersion20(XmlUtil.getElementByTagName(eGraph, "linePool")));
    }
    
    /** This will write out the Graph information.
     */
    void write(PrintWriter out) {        
        out.println("      <graph id='" + id + "'>");
        out.println("       <linePool>");
        
        // Write out the vertex pool.
        out.println("        <vertexPool>");
        for (Iterator<Vertex> itr=lPoolLine.vertexValues().iterator(); itr.hasNext(); )
            itr.next().write(out);
        out.println("        </vertexPool>");
        
        // Write out the lines.
        for (Iterator<AbstractLine> itr=lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().write(out);
        
        out.println("       </linePool>");
        out.println("      </graph>");
        return;
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Get/Status/Validate Methods ">
    
    /** @return The bounds of the graph by using the measurements of the lines (not the vertices).
     */
    public Rectangle2D.Float getBounds2D() {
        if (lPoolLine.size() == 0)
            return null;
        
        // Initial size of rectangle.
        Rectangle2D.Float fRectangle = lPoolLine.getFirst(DrawingLinePool.SEARCH_OFF).getBounds2D();
        if (fRectangle == null)
            return null;
        
        // Grow the rectangle.
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            Rectangle2D.Float fRect = abLine.getBounds2D();
            fRectangle.add(fRect);
        }
        
        return fRectangle;
    }
    
    /** @return the number of lines in this design.
     */
    public int getLineCount() {
        return lPoolLine.size();
    }
    
    /** This will get the layout of this DrawingGraph as a GeneralPath. It is the outer most part of the graph. It then can be used as a collision detection
     */
    GeneralPath getLayoutGeneralPath() {
        // Get an outer line in the graph.
        FPointType fptZero = new FPointType(0.0f, 0.0f);
        AbstractLine closestLine = getClosestLineToPoint(fptZero);
        
        
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            
        }
        
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    
    /** @return the number of vertices in this design.
     */
    public int getVertexCount() {
        return lPoolLine.getVertexPool().size();
    }
    
    /** This will set the list of AbstractLines to the flag value.
     * @param ltLines is the list of AbstractLines to set the flag value to.
     * @param flag is used to set each line the flag value. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific set of values.
     */
    private void set(LinkedList<AbstractLine> ltLines, int flag) {
        for (Iterator<AbstractLine> itr = ltLines.iterator(); itr.hasNext(); )
            itr.next().set(flag);
    }
    
    /** This will set every line and vertex to the select.
     * @param selected is the value that all lines and vertices should be set to.
     */
    public void setSelect(boolean selected) {
        int flag;
        
        if (selected)
            flag = DrawingLinePool.SEARCH_SELECT_ON;
        else
            flag = DrawingLinePool.SEARCH_SELECT_OFF;
        
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            abLine.set(flag);
        }
        
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); ) {
            itr.next().set(flag);
        }
    }
        
    /** This will set all lines and vertices to visible.
     * @param visible is the value to set the lines and vertices to.
     */
    public void setLinesAndVerticesVisible(boolean visible) {
        int flag;
        if (visible)
            flag = DrawingLinePool.SEARCH_VISIBLE_ON;
        else
            flag = DrawingLinePool.SEARCH_VISIBLE_OFF;
        
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            abLine.set(flag);
        }
        
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); ) {
            itr.next().set(flag);
        }
    }
    
    /** This will reset the statuses of the lines and vertices to visible and not selected and not visited.
     */
    public void resetStatus() {
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().resetStatuses();
        
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); )
            itr.next().resetStatuses();
    }
    
    /** This will validate the line by update its drawing line structure.
     */
    void validateLines() {
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().validate();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Select Methods ">
    
    /** This will copy the selected items leaving this DrawingGraph unchanged. Only the lines which are currently selected will be copied.
     * Note the TransformGraph will not have any of its items pointing back to any in this DrawingGraph.
     * @return a TransformGraph of the selected items and can be empty if the user has nothing selected.
     */
    public TransformGraph copySelectedItems() {
        // Get all lines that are selected.
        // Add them into the TransformGraph.
        TransformGraph tGraph = new TransformGraph();                
        for (Iterator<AbstractLine> itrLine = lPoolLine.toList(DrawingLinePool.SEARCH_SELECT_ON).iterator(); itrLine.hasNext(); ) {
            AbstractLine abLine = itrLine.next();            
            tGraph.add(abLine.getInfo());
        }
        
        // This will edit any curve that has a control point not connected to another line. It will make them as end curves then.
        tGraph.validateCurvesControlPoints();
        
        return tGraph;
    }
    
    /** This will delete the selected lines from this graph. After this operation, the graph could become empty, it
     * could also fragment the graph and possibly need to be broken into mulitple graphs.
     * @return an undo for this operation, can be empty.
     */
    public InterfaceUndoItem deleteSelectedLines() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        LinkedList<Vertex> ltNeedDelete = new LinkedList<Vertex>();
        
        // If there exists 1 selected vertex and 2 lines coming from that vertex then perform a merge delete on it.
        if (isSelectedOneVertexNeedMerge())
            return deleteSelectedLinesMerge();
        
        // Need to get a list of all selected items and delete them.
        // Them have the RMolnar curve control points relinked up.
        for (Iterator<AbstractLine> itr = lPoolLine.toList(DrawingLinePool.SEARCH_SELECT_ON).iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            undoComplex.add(lPoolLine.remove(abLine));
            
            // Clear the status of the vertices.
            abLine.getFirstEndVertex().resetStatuses();
            abLine.getLastEndVertex().resetStatuses();
            
            // Add the end vertices to see if they might need to be deleted.
            ltNeedDelete.add(abLine.getFirstEndVertex());
            ltNeedDelete.add(abLine.getLastEndVertex());
        }
        
        // Make sure all curves have their control points connected or else make then end curves.
        undoComplex.add(lPoolLine.validateCurves());
        
        // Check all end vertices. See if they might need to be deleted.
        VertexPool vp = lPoolLine.getVertexPool();
        for (Iterator<Vertex> itr = ltNeedDelete.iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            
            // Can contain duplicate vertices in the ltNeedDelete list.
            if (vp.contains(v) == false) 
                continue;
            
            // Does it need to be deleted? Only delete if the vertex does not have any lines connected to it.
            if (lPoolLine.search(v, DrawingLinePool.SEARCH_OFF).isEmpty())
                undoComplex.add(lPoolLine.getVertexPool().remove(v));
        }
        
        return undoComplex;
    }

    /** This will perform a special delete on the selected lines. The requirement before this is that 'isSelectedOneVertexNeedMerge()' is true.
     * It will delete the selected 2 lines and replace them with 1 line. Therefore the connectivity of the graph will stay intact.
     * @return an undo for this operation, will not be empty.
     * @throws IllegalArgumentException if isSelectedOneVertexNeedMerge() returns false.
     */
    public InterfaceUndoItem deleteSelectedLinesMerge() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Must meet the requirements of 1 vertex selected and 2 lines coming from it.
         if (isSelectedOneVertexNeedMerge() == false)
             throw new IllegalArgumentException("Cannot be called because there are more than 1 vertex selected.");
         
        // Get the selected vertex.
        Vertex vSelected = lPoolLine.getVertexPool().toList(DrawingLinePool.SEARCH_SELECT_ON).getFirst();
        
        // Get the two selected lines.
        LinkedList<AbstractLine> ltLines = lPoolLine.search(vSelected, DrawingLinePool.SEARCH_SELECT_ON);
        AbstractLine abSelected1 = ltLines.pop();
        AbstractLine abSelected2 = ltLines.pop();
        
        // Remove the selected lines.
        undoComplex.add(lPoolLine.remove(abSelected1));
        undoComplex.add(lPoolLine.remove(abSelected2));
        
        // Now delete the selected vertex.
        undoComplex.add(lPoolLine.getVertexPool().remove(vSelected));
        
        // Both AbstractLines are Lines or Beziers.
        if ((abSelected1 instanceof RMolnar == false) && (abSelected2 instanceof RMolnar == false))
            undoComplex.add(deleteSelectedLinesMergeLinesBeziers(vSelected, abSelected1, abSelected2));
        else
            undoComplex.add(deleteSelectedLinesMergeCurve(vSelected, abSelected1, abSelected2));
        
        return undoComplex;
     }       

    /** This will perform the needed operations to perform the deleted merge operation. It is assumed that atleast one 
     * of the inputted AbstractLines is a RMolnar curve.
     * @param vSelected is the vertex to be deleted.
     * @param abSelected1 is the first line adjacent to the vertex. There is no order to it.
     * @param abSelected2 is the second line adjacent to the vertex. There is no order to it.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem deleteSelectedLinesMergeCurve(Vertex vSelected, AbstractLine abSelected1, AbstractLine abSelected2) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Make abSelected1 the Bezier or Line.
        if (abSelected2 instanceof Line || abSelected2 instanceof Bezier) {
            AbstractLine abTemp = abSelected1;
            abSelected1 = abSelected2;
            abSelected2 = abTemp;
        }
        
        // The second one is always RMolnar.
        RMolnar rmolnar2 = (RMolnar)abSelected2;
        
        // End vertices used for the new line/bezier.
        Vertex vEnd1 = abSelected1.getOppositeEndVertex(vSelected);
        Vertex vEnd2 = abSelected2.getOppositeEndVertex(vSelected);
        
        // Create a new line that will represent both lines and curve.
        if (abSelected1 instanceof Line) {
            Line line = new Line(vEnd1, vEnd2);
            undoComplex.add(lPoolLine.add(line.getInfo()));
            // Make connected RMolnar end curves at the selected vertex.
            undoComplex.add(makeRMolnarEndCurvesAtVertex(vSelected));
            return undoComplex;
            
        } else if (abSelected1 instanceof Bezier) {
            Bezier deletedBezier = (Bezier)abSelected1;
            Bezier bezier = null;
            
            if (deletedBezier.isFirstEndVertex(vEnd1))
                bezier = new Bezier(vEnd1, vEnd2, deletedBezier.getFirstControlVertex().getPoint(), deletedBezier.getLastControlVertex().getPoint());
            else
                bezier = new Bezier(vEnd1, vEnd2, deletedBezier.getLastControlVertex().getPoint(), deletedBezier.getFirstControlVertex().getPoint());
            undoComplex.add(lPoolLine.add(bezier.getInfo()));
            // Make connected RMolnar end curves at the selected vertex.
            undoComplex.add(makeRMolnarEndCurvesAtVertex(vSelected));
            return undoComplex;
            
        } 

        // abSelected1 and abSelected2 are RMolnar curves.
        
        // Create the new RMolnar that will stetch between abSelected1 and abSelected2.
        RMolnar rmolnar1 = (RMolnar)abSelected1;            
        RMolnar rmolnarStretch = new RMolnar(vEnd1, vEnd2, rmolnar1.getControlVertex(vEnd1), rmolnar2.getControlVertex(vEnd2));
        undoComplex.add(lPoolLine.add(rmolnarStretch.getInfo()));
        
        // Need to relink curves that have the vSelected as a control vertex.
        for (Iterator<RMolnar> itr = lPoolLine.searchControl(vSelected).iterator(); itr.hasNext(); ) {
            RMolnar rmolnar = itr.next();
            
            // Remove the curve.
            undoComplex.add(lPoolLine.remove(rmolnar));
            
            // Add it back in but have the control point stretched to the correct end point.
            RMolnar rmolnarAdd = null;
            if (rmolnar.isEndVertex(vEnd1))
                rmolnarAdd = new RMolnar(vEnd1, rmolnar.getOppositeEndVertex(vEnd1), vEnd2, rmolnar.getControlVertex(rmolnar.getOppositeEndVertex(vEnd1)));
            else 
                rmolnarAdd = new RMolnar(vEnd2, rmolnar.getOppositeEndVertex(vEnd2), vEnd1, rmolnar.getControlVertex(rmolnar.getOppositeEndVertex(vEnd2)));
            undoComplex.add(lPoolLine.add(rmolnarAdd.getInfo()));
        }
                
        return undoComplex;
    }    
    
    /** This will perform the needed operations to perform the deleted merge operation. It is assumed that both inputted AbstractLines are Lines or Beziers.
     * @param vSelected is the vertex to be deleted.
     * @param abSelected1 is the first line adjacent to the vertex. There is no order to it.
     * @param abSelected2 is the second line adjacent to the vertex. There is no order to it.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem deleteSelectedLinesMergeLinesBeziers(Vertex vSelected, AbstractLine abSelected1, AbstractLine abSelected2) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // If there exists a Bezier and a Line, make the Line become the merged and delete the Bezier.
        if (abSelected1 instanceof Bezier && abSelected2 instanceof Line) {
            AbstractLine abTemp = abSelected1;
            abSelected1 = abSelected2;
            abSelected2 = abTemp;
        }
        
        // End vertices used for the new line/bezier.
        Vertex vEnd1 = abSelected1.getOppositeEndVertex(vSelected);
        Vertex vEnd2 = abSelected2.getOppositeEndVertex(vSelected);
        
        // Create a new line that will represent both lines.
        if (abSelected1 instanceof Line) {
            Line line = new Line(vEnd1, vEnd2);
            undoComplex.add(lPoolLine.add(line.getInfo()));
        } else {
            Bezier deletedBezier = (Bezier)abSelected1;
            Bezier bezier = null;
            
            if (deletedBezier.isFirstEndVertex(vEnd1))
                bezier = new Bezier(vEnd1, vEnd2, deletedBezier.getFirstControlVertex().getPoint(), deletedBezier.getLastControlVertex().getPoint());
            else
                bezier = new Bezier(vEnd1, vEnd2, deletedBezier.getLastControlVertex().getPoint(), deletedBezier.getFirstControlVertex().getPoint());
            undoComplex.add(lPoolLine.add(bezier.getInfo()));
        }
        
        return undoComplex;
    }
    
    /** This will deselect all and will set all lines and vertices as visible.
     */
    public void deselectAll() {
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); )
            itr.next().set(DrawingLinePool.SEARCH_VISIBLE_ON | DrawingLinePool.SEARCH_SELECT_OFF);
        
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); )
            itr.next().set(DrawingLinePool.SEARCH_VISIBLE_ON | DrawingLinePool.SEARCH_SELECT_OFF);
    }
    
    /** This will deselect lines from the selected items in the GraphPool.
     * @param r is the rectangle to deselect items from this GraphPool.
     */
    public void deselectLines(Rectangle2D.Float r) {
        // Each line within the rectangle needs to be deselected.
        for (Iterator<AbstractLine> itr = lPoolLine.search(r, false).iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            // Skip non-selected lines.
            if (abLine.isSelected() == false)
                continue;
            
            abLine.setSelected(false);
        }
        
        // Make sure all vertices that are selected have selected lines connected to them.
        deselectHelper();
    }
    
    /** This will deselect vertices from the selected items in the GraphPool.
     * @param r is the rectangle to deselect items from this GraphPool.
     */
    public void deselectVertices(Rectangle2D.Float r) {
        // Each vertex within the rectangle needs to be deselected.
        for (Iterator<Vertex> itr = lPoolLine.getVertexPool().search(r).iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            
            // Skip non-selected vertices.
            if (v.is(DrawingLinePool.SEARCH_SELECT_OFF))
                continue;
            
            // Deselect the Vertex and lines from the Vertex.
            v.set(DrawingLinePool.SEARCH_SELECT_OFF);
            set(lPoolLine.search(v, DrawingLinePool.SEARCH_OFF), DrawingLinePool.SEARCH_SELECT_OFF);
        }
        
        // Make sure all vertices that are selected have selected lines connected to them.
        deselectHelper();
    }
    
    /** This is a deselect helper function. It will check all selected vertices to make sure there exists selected lines to them.
     */
    void deselectHelper() {
        // Now all selected vertices need to be checked to make sure there exists selected lines to them.
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            if (v.is(DrawingLinePool.SEARCH_SELECT_OFF))
                continue;
            
            // There needs to exist a line which is selected that is connected to this vertex.
            if (lPoolLine.search(v, DrawingLinePool.SEARCH_SELECT_ON).isEmpty() == false)
                v.set(DrawingLinePool.SEARCH_SELECT_OFF);
        }
    }
    
    /** This will create a TransformGraph which will have the necessary lines and vertices that
     * were selected. This TransformGraph will have each line it as pointing back to the line in
     * this pool. <br>
     * As a result of this call:<br>
     * Lines from this DrawingGraph: Any line which is connected to a selected vertice or has a
     * control point connected to a selected vertex will be set to invisible.<br>
     * Vertices from this DrawingGraph: All selected vertices will be set to invisible.<br>
     * Lines placed into the TransformGraph: Will be visible and will be pointing back to the line
     * which it represents. <br>
     * Vertices placed into the TransformGraph: Will be invisible except for the selected vertices
     * from the DrawingGraph. Also the only ones that can move are the selected vertices.
     * Therefore if a vertex is selected then it will be visible and moveable, if not selected then it
     * will be invisible and non-moveable.<br>
     * <b>Note:</b> No stucture change will be performed on the DrawingGraph only internal
     * flags of the lines and vertices.
     * <br><br>Selection process.
     * - set items selected.
     * - get selected items into a TransformLinePool and set them pointing to the original items and then
     * - set the visible of the original items to false.
     * - when putting back the items delete original and place new into graph.
     *  @returns a TransformGraph of the selected items. The TransformGraph has a less restrictive
     * requirements on its structure therefore there could be mulitple broken up graphs (graph theory)
     * in the structure. */
    public TransformGraph getSelectedItems() {
        // Get all lines that are selected.
        // Add them into the TransformGraph and have those lines point back to the original ones.
        // Add the rest of lines that will move when the movable vertices move and make them point to the original ones.
        // Set all lines added to the TransformGraph to invisible in this DrawingGraph.
        // Set all vertices that are selected to invisible in this pool and those same vertices in the TransformGraph as visible.
        TransformGraph tGraph = new TransformGraph();
        
        // All lines(Line, Bezier, and RMolnar) that are connected to these vertices need to be added to the TransformGraph.
        // All RMolnar curves that have their control vertex as one of these vertices need to be added too.
        LinkedList<Vertex> ltSelectVertices = lPoolLine.getVertexPool().toList(DrawingLinePool.SEARCH_SELECT_ON);
        for (Iterator<Vertex> itr = ltSelectVertices.iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            
            // Add all lines to the TransformGraph and the line's id. The line added to the TransformGraph must have it's vertices invisible.
            for (Iterator<AbstractLine> itrLine = lPoolLine.search(v, DrawingLinePool.SEARCH_OFF).iterator(); itrLine.hasNext(); ) {
                AbstractLine abLine = itrLine.next();
                tGraph.add(abLine.getInfo(), abLine.getId(), abLine.isSelected(), false); // Adding invisible vertices.
                abLine.setVisible(false);
            }
            
            // All RMolnar curves that have their control vertex as one of these vertices need to be added too. The line added
            // to the TransfromGraph must have it's vertices invisible.
            for (Iterator<RMolnar> itrCurve = lPoolLine.searchControl(v).iterator(); itrCurve.hasNext(); ) {
                RMolnar curve = itrCurve.next();
                tGraph.add(curve.getInfo(), curve.getId(), curve.isSelected(), false); // Adding invisible vertices.
                curve.setVisible(false);
            }
            
            // Set as invisible for this graph and visible for the TransformGraph.
            v.set(DrawingLinePool.SEARCH_VISIBLE_OFF);
            tGraph.setVisiblePoint(v.getPoint(), true);
            tGraph.setTransformPoint(v.getPoint(), true);
        }
        
        return tGraph;
    }    
    
    /** @param selected is the flag value the line must be.
     * @return true if there are any 'selected' lines in this graph.
     */
    public boolean hasSelectedItems(boolean selected) {
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            if (itr.next().isSelected() == selected)
                return true;
        }
        return false;
    }
    
    /** This will check to see if this pool will stay connected as a graph (graph theory) where all lines
     * can connect without any of the selected lines. This function can be used to see if it is safe to
     * delete the selected lines from this pool. 
     * <br> WARNING:: this will change the visited flag on the lines.
     * @return true if it would be safe to delete the selected lines from this pool.
     */    
    public boolean isDeletingSelectSafe() {
        // Set all line's visited flag to false.
        lPoolLine.set(DrawingLinePool.SEARCH_VISIT_OFF);

        // Need to delete the graph.
        if (lPoolLine.getVertexPool().size() <= 2)
            return false;
        
        if (isSelectedOneVertexNeedMerge())
            return true;
    
        // Get a line that is not selected. If none exist then all are selected therefore they can be deleted.
        AbstractLine abLine = lPoolLine.getFirst(DrawingLinePool.SEARCH_SELECT_OFF);
        if (abLine == null)
            return true;
        
        // visit all lines that have not been selected if the unselected are.
        lPoolLine.traverse(abLine.getFirstEndVertex(), DrawingLinePool.SEARCH_SELECT_OFF);
        
        // Find an unvisited unselected line. If that exists then the selected lines cannot be deleted.
        if (lPoolLine.getFirst(DrawingLinePool.SEARCH_SELECT_OFF | DrawingLinePool.SEARCH_VISIT_OFF) == null)
            return true;
        return false;
    }
    
    /** @param fptMousePos is the position used to see if the point is within a vertex proximity.
     * @return true if the point is within a vertex proximity.
     */
    public boolean isPointWithinVertex(FPointType fptMousePos) {
        Rectangle2D.Float rect = Vertex.createDoubleProximity(fptMousePos);
        return (lPoolLine.getVertexPool().search(rect).isEmpty() == false);
    }
    
    /** This will check for 1 vertex to be selected and only 2 lines adjacent to it, if so then it will return true, else false.
     * <br> There are special cases where there are 3 or less vertices in the entire graph.
     * @return true if there exists only 1 vertex to be selected and only 2 lines coming from it.
     */
    public boolean isSelectedOneVertexNeedMerge() {        
        // The vertex will be deleted but the two lines will be connected together. However, if the vertex contains more than two
        // lines connected to it then they will be deleted (need to check the traverse case then).
        if (lPoolLine.getVertexPool().count(DrawingLinePool.SEARCH_SELECT_ON) == 1) {
            // If there only exists three or less than no merging. 
            if (lPoolLine.getVertexPool().size() <= 3)
                return false;            
            
            // This is the one and only vertex that is selected.
            Vertex vSelected = lPoolLine.getVertexPool().toList(DrawingLinePool.SEARCH_SELECT_ON).getFirst();
            
            // If there are only two lines then the vertex will be deleted and one line is deleted but the other line will replace those two.
            if (lPoolLine.search(vSelected, DrawingLinePool.SEARCH_OFF).size() == 2)
                return true;
        }
        
        return false;
    }
    
    /** This will make all curves (RMolnar) that has the vertex as a control vertex into end curves for the vertex.
     * @param v is the vertex used to search for RMolnar and make then end curves at that vertex.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem makeRMolnarEndCurvesAtVertex(Vertex v) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        for (Iterator<RMolnar> itr = lPoolLine.searchControl(v).iterator(); itr.hasNext(); ) {
            RMolnar rmolnar = itr.next();
            
            // Remove the curve.
            undoComplex.add(lPoolLine.remove(rmolnar));
            
            // Add it back in but now as an end curve.
            if (v == rmolnar.getFirstControlVertex()) {
                RMolnar rmolnarNew = new RMolnar(rmolnar.getFirstEndVertex(), rmolnar.getLastEndVertex(), rmolnar.getFirstEndVertex(), rmolnar.getLastControlVertex());
                undoComplex.add(lPoolLine.add(rmolnarNew.getInfo()));
            } else {
                RMolnar rmolnarNew = new RMolnar(rmolnar.getFirstEndVertex(), rmolnar.getLastEndVertex(), rmolnar.getFirstControlVertex(), rmolnar.getLastEndVertex());
                undoComplex.add(lPoolLine.add(rmolnarNew.getInfo()));
            }
        }
        
        return undoComplex;
    }
    
    /** This will merge the selected items from the tGraph into the items of this graph. Actually
     * the tGraph was created by this DrawingGraph for selection, therefore the lines in the
     * tGraph point back to lines in this DrawingLayer.
     * @param tGraph is the graph with selected items from this graph to merge into this graph.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem mergeSelected(TransformGraph tGraph) {
        VertexPool vpDrawing = lPoolLine.getVertexPool();
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Delete the selected lines from the DrawingGraph.
        for (Iterator<AbstractLine> itr = lPoolLine.toList(DrawingLinePool.SEARCH_VISIBLE_OFF).iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            undoComplex.add(lPoolLine.remove(abLine));
        }
        // Delete the invisible vertices from the DrawingGraph.
        for (Iterator<Vertex> itr = vpDrawing.toList(DrawingLinePool.SEARCH_SELECT_ON).iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            undoComplex.add(vpDrawing.remove(v));
        }
        
        // Now add the list of lines.
        undoComplex.add(lPoolLine.merge(tGraph.toListInfo()));
        
        return undoComplex;
    }
    
    /** This will select one Bezier curve. 
     * @param r is the rectangle to select the Bezier curve.
     * @return true if it selected the Bezier curve.
     */
    public boolean selectBezier(Rectangle2D.Float r) {
        boolean bSelect = false;
        
        // Get the first Bezier Line and select it.
        for (Iterator<AbstractLine> itr = lPoolLine.search(r, false).iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            if (abLine instanceof Bezier == false)
                continue;
            
            // Set the Bezier and vertices of bezier to selected.
            abLine.setSelected(true);
            abLine.getFirstEndVertex().set(DrawingLinePool.SEARCH_SELECT_ON);
            abLine.getLastEndVertex().set(DrawingLinePool.SEARCH_SELECT_ON);
            bSelect = true;
            break;
        }
        
        return bSelect;
    }
    
    /** This will select lines from this graph.
     * @param r is the rectangle to select the lines.
     * @param oneLine is true if only one line is to be selected.
     * @return true if selected anything.
     */
    public boolean selectLines(Rectangle2D.Float r, boolean oneLine) {
        boolean bSelected = false;
        
        // For each line in the rectangle set selected.
        for (Iterator<AbstractLine> itr = lPoolLine.search(r, oneLine).iterator(); itr.hasNext(); ) {
            bSelected = true;
            
            // Set line and vertices of line to selected.
            AbstractLine abLine = itr.next();
            abLine.setSelected(true);
            abLine.getFirstEndVertex().set(DrawingLinePool.SEARCH_SELECT_ON);
            abLine.getLastEndVertex().set(DrawingLinePool.SEARCH_SELECT_ON);
            
            // Only do one line.
            if (oneLine)
                break;
        }
        
        return bSelected;
    }
    
    /** This will select vertices from this graph.
     * @param r is the rectangle to select the vertices.
     * @param oneVertex is true if only one vertex is to be selected.
     * @return true if selected anything.
     */
    public boolean selectVertices(Rectangle2D.Float r, boolean oneVertex) {
        LinkedList<Vertex> lVertexSelected = lPoolLine.getVertexPool().search(r, oneVertex);
        
        LinkedList<AbstractLine> lLineSelected = new LinkedList<AbstractLine>();
        for (Iterator<Vertex> itr = lVertexSelected.iterator(); itr.hasNext(); )
            lLineSelected.addAll(lPoolLine.search(itr.next(), DrawingLinePool.SEARCH_OFF));
        
        // Set the vertices and lines to selected.
        for (Iterator<Vertex> itr = lVertexSelected.iterator(); itr.hasNext(); )
            itr.next().set(DrawingLinePool.SEARCH_SELECT_ON);
        for (Iterator<AbstractLine> itr = lLineSelected.iterator(); itr.hasNext(); )
            itr.next().set(DrawingLinePool.SEARCH_SELECT_ON);
        
        return !lLineSelected.isEmpty();
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc="  Other Methods ">
    
    /** @return true if any of the lines in this graph intersect the rectangle, else false.
     */
    public boolean intersects(Rectangle2D.Float r) {
        return lPoolLine.intersects(r);
    }
    
    /** This will filter the position to one of the vertices if it is close enough to one.
     * @param fpt is the floating point to be filtered, it's x,y coordinates will be modified if it is
     * close enough to another vertex.
     */
    public void filterPoint(FPointType fpt) {
        for (Iterator<Vertex> itr = lPoolLine.vertexValues().iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            
            // Is the point in the vertex space?
            if (v.is(DrawingLinePool.SEARCH_VISIBLE_ON) && v.isVertexSpace(fpt.x, fpt.y)) {
                fpt.x = v.getPoint().x;
                fpt.y = v.getPoint().y;
                break;
            }
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Mathematic Graph Methods ">
    
    /** This will get the line closest to the point fpt.
     *  @param fpt is the point used to retrieve the closest line to it.
     *  @return the line which is closest to the point fpt. The comparison will be performed on the line and not on the end points.
     *  @throw IllegalStateException does not contain any lines in this drawing.
     */
    public AbstractLine getClosestLineToPoint(FPointType fpt) {
        // Check for lines.
        if (lPoolLine.size() == 0)
            throw new IllegalStateException("No lines in the graph.");        
        
        // Get the initial close line and square distance.
        AbstractLine abLineClosest = lPoolLine.getFirst(DrawingLinePool.SEARCH_OFF);
        float segDistance = abLineClosest.ptSegDist(fpt);
        
        // Search through all the lines.
        for (Iterator<AbstractLine> itr = lPoolLine.values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            // See if the line is closer than the current line.
            float ptSegDist = abLine.ptSegDist(fpt);
            if (ptSegDist < segDistance) {
                abLineClosest = abLine;
                segDistance = ptSegDist;
            }
        }
        
        // Return the closest line.
        return abLineClosest;
    }

    /** This will get all intersections on the line 'abLineIntersect' from all of the graphs in the list.
     *  @param ltGraphs is the list of graphs used for getting the intersections.
     *  @param abLineIntersect is the AbstractLine which is used to perform the intersections on.
     *  @param toVertex must be in the abLineIntersect. It is used to get the lines connected to the abLineIntersect and at this vertex.
     *  @return a list of Intersections which are intersections on the abLineIntersect. Order of the parameteric values are always from the first vertex in the AbstractLines.
     */
    public static final LinkedList<Intersections> getIntersectionLines(LinkedList<DrawingGraph> ltGraphs, AbstractLine abLineIntersect, Vertex toVertex) {
        LinkedList<Intersections> ltIntersects = new LinkedList<Intersections>();
        
        // Look through all AbstractLine from the DrawingGraphs.
        for (Iterator<DrawingGraph> itrGraph = ltGraphs.iterator(); itrGraph.hasNext(); ) {
            DrawingGraph dGraph = itrGraph.next();
            
            // Look at the AbstractLines from the DrawingGraph.
            for (Iterator<AbstractLine> itr = dGraph.lPoolLine.values().iterator(); itr.hasNext(); ) {
                AbstractLine abLine = itr.next();
                if (abLine == abLineIntersect)
                    continue;
                
                Intersections intersections = Intersections.intersect(abLineIntersect, abLine);
                if (intersections != null)
                    ltIntersects.add(intersections);
            }
        }
        
        // Check the vertex and line. Make sure they do infact connect.
        if (abLineIntersect.isEndVertex(toVertex) == false)
            throw new IllegalArgumentException("The vertex " + toVertex + " does not exist in the line " + abLineIntersect);

        // Get the graph which the abLineIntersect line is in.
        for (Iterator<DrawingGraph> itrGraph = ltGraphs.iterator(); itrGraph.hasNext(); ) {
            DrawingGraph dGraph = itrGraph.next();
            if (dGraph.lPoolLine.contains(abLineIntersect) == false)
                continue;
            
            // Get the list of lines connected to the 'toVertex' vertex and convert them to IntersectLines.
            LinkedList<AbstractLine> ltLines = dGraph.lPoolLine.search(toVertex, DrawingLinePool.SEARCH_OFF);
            for (Iterator<AbstractLine> itr = ltLines.iterator(); itr.hasNext(); ) {
                AbstractLine abLine = itr.next();
                if (abLine == abLineIntersect)
                    continue;
                
                // Add the IntersectionLine to the list.
                ltIntersects.add(new Intersections(abLineIntersect, abLine, toVertex));
            }
        }
                
        return ltIntersects;
    }
    
    /** This will see if the graph intersects this DrawingGraph by seeing if any of the lines or curves intersects any of the
     *  lines or curves in that graph. This is a very intensive computation, because each line and curve must be calculated against each
     *  other in the graph. However, if a line or curve from this graph matches the first line or curve from the other graph then it will not
     *  require much computation.
     *  @param graph is the DrawingGraph used to see if it intersects this graph by comparing lines and curves together.
     *  @return true if graph intersects this graph by lines and curves, or false it does not.
     */
    public boolean intersects(DrawingGraph graph) {
        for (Iterator<AbstractLine> outer = lPoolLine.values().iterator(); outer.hasNext(); ) {
            AbstractLine lineOuter = outer.next();
            Shape s1 = lineOuter.getShape(lineOuter.getFirstEndVertex());
            
            for (Iterator<AbstractLine> inner = graph.lPoolLine.values().iterator(); inner.hasNext(); ) {
                AbstractLine lineInner = inner.next();
                
                if (MathLineCurve.intersect(s1, lineInner.getShape(lineInner.getFirstEndVertex())))
                    return true;
            }
        }
        
        return false;
    }
        
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" General Path Methods ">
    
    /** This will create a GeneralPath from the position fptMouseDown.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @return null or a GeneralPath containing the position fptMouseDown.
     */
    public GeneralPath createGeneralPath(FPointType fptMouseDown) {
        throw new UnsupportedOperationException("TODO");
//        return lPoolLine.createGeneralPath(fptMouseDown);
        // return null;
    }
    
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
    
    /** This will get the line information. It will search for the line within the fBounds.
     * @param fBounds is the rectangle to search for the line.
     */
    public String debugGetInfo(Rectangle2D.Float fBounds) {
//        return lPoolLine.debugGetInfo(fBounds);
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Finger Game Level Support ">
            
    /** This will create a path from one of the end points to the other end point. Assumed
     * the graph is a true path.
     * @return a list of points that represent the path.
     */
    public LinkedList<FPointType> finger_getPath() {
        LinkedList<FPointType> ltPoints = new LinkedList<FPointType>();
        
        // Find first vertex that has only one line from it.
        Vertex vOne = null;
        for (Iterator<Vertex> itr = lPoolLine.getVertexPool().values().iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            if (lPoolLine.search(v, DrawingLinePool.SEARCH_OFF).size() == 1) {
                vOne = v;
                break;
            }
        }
        if (vOne == null)
            return ltPoints;
        
        // Now traverse from that vertex and create a list of lines to traverse.
        lPoolLine.set(DrawingLinePool.SEARCH_VISIT_OFF);
        LinkedList<AbstractLineInfo> ltLineInfo = new LinkedList<AbstractLineInfo>();
        lPoolLine.traverse(ltLineInfo, vOne, DrawingLinePool.SEARCH_OFF);
        
        // Traverse the path.
        FPointType vCurr = vOne.getPoint();
        for (Iterator<AbstractLineInfo> itr = ltLineInfo.iterator(); itr.hasNext(); ) {
            AbstractLineInfo curr = itr.next();
            ltPoints.addAll(finger_getPath_sample(vCurr, curr));            
            vCurr = curr.getOppositePoint(vCurr);
        }
        
        return ltPoints;
    }
    
    /** This will create a sampling for the current line information.
     * @param ptPivot is the point where the sampling should start from.
     * @param abLineInfo is the line that needs to be sampled.
     * @return a list of points sampled from the line.
     */
    private LinkedList<FPointType> finger_getPath_sample(FPointType ptPivot, AbstractLineInfo abLineInfo) {
        LinkedList<FPointType> list = new LinkedList<FPointType>();
        
        if (abLineInfo instanceof LineInfo) {            
            // Create the line.
            Line2D.Float line;            
            if (ptPivot.equals(abLineInfo.getEndPoint1()))                
                line = new Line2D.Float(abLineInfo.getEndPoint1().x, abLineInfo.getEndPoint1().y, abLineInfo.getEndPoint2().x, abLineInfo.getEndPoint2().y);
            else
                line = new Line2D.Float(abLineInfo.getEndPoint2().x, abLineInfo.getEndPoint2().y, abLineInfo.getEndPoint1().x, abLineInfo.getEndPoint1().y);
            
            float distance = (float)Math.sqrt((line.x1 - line.x2) * (line.x1 - line.x2) + (line.y1 - line.y2) * (line.y1 - line.y2));
            int samplingCount = (int)((float)distance / 0.01f) + 1;
            
            // Perform the sampling.
            list.addAll(MathLineCurve.sampleLine(line, samplingCount));
            
        } else if (abLineInfo instanceof RMolnarInfo) {
            CubicCurve2D.Float curve;
            if (ptPivot.equals(abLineInfo.getEndPoint1()))
                curve = new RMolnarCubicCurve2D(abLineInfo.getEndPoint1(), abLineInfo.getEndPoint2(), 
                        ((RMolnarInfo)abLineInfo).getControlPoint1(), ((RMolnarInfo)abLineInfo).getControlPoint2());
            else
                curve = new RMolnarCubicCurve2D(abLineInfo.getEndPoint2(), abLineInfo.getEndPoint1(), 
                        ((RMolnarInfo)abLineInfo).getControlPoint2(), ((RMolnarInfo)abLineInfo).getControlPoint1());
                   
            float length = MathLineCurve.lengthCurve(curve, 0.0f, 1.0f);
            int samplingCount = (int)((float)length / 0.01f) + 1;
            
            // Perform the sampling.
            list.addAll(MathLineCurve.sampleCurve(curve, samplingCount));
            
        } else if (abLineInfo instanceof BezierInfo) {
            
            CubicCurve2D.Float curve;
            if (ptPivot.equals(abLineInfo.getEndPoint1()))
                curve = new CubicCurve2D.Float(abLineInfo.getEndPoint1().x, abLineInfo.getEndPoint1().y, 
                        ((BezierInfo)abLineInfo).getControlPoint1().x, ((BezierInfo)abLineInfo).getControlPoint1().y,
                        ((BezierInfo)abLineInfo).getControlPoint2().x, ((BezierInfo)abLineInfo).getControlPoint2().y,
                        abLineInfo.getEndPoint2().x, abLineInfo.getEndPoint2().y);
            else
                curve = new CubicCurve2D.Float(abLineInfo.getEndPoint2().x, abLineInfo.getEndPoint2().y, 
                        ((BezierInfo)abLineInfo).getControlPoint2().x, ((BezierInfo)abLineInfo).getControlPoint2().y,
                        ((BezierInfo)abLineInfo).getControlPoint1().x, ((BezierInfo)abLineInfo).getControlPoint1().y,
                        abLineInfo.getEndPoint1().x, abLineInfo.getEndPoint1().y);
                   
            float length = MathLineCurve.lengthCurve(curve, 0.0f, 1.0f);
            int samplingCount = (int)((float)length / 0.01f) + 1;
            
            // Perform the sampling.
            list.addAll(MathLineCurve.sampleCurve(curve, samplingCount));
            
        }
        
        return list;
    }
    
    // </editor-fold>

}

