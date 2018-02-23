/*
 * GraphLinePool.java
 *
 * Created on April 12, 2007, 5:07 PM
 *
 */

package mlnr.draw;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;

/** This class is used to maintain a pool of AbstractLines. <br>
 * This class will guarantee: <br>
 *  - At any time all AbstractLine's Vertices will be in this LinePool. <br>
 *  - At any time all RMolnars will have there control vertices connected to an adjacent RMolnar
 *  end vertex or to its own end vertex. <br>
 *  However, this guarantee does not apply between a complex update, but after the complex update
 * is performed will it guarantee completeness. <br>
 * <br>
 * This class performs simple operations on this pool of AbstactLines, such as, add, delete,
 * search, draw, etc.. <br>
 *
 *
 * @author Robert Molnar 2
 */
public class DrawingLinePool extends AbstractPool implements GraphTheoryInterface {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** flag parameter for search: no flag requirements. */
    public static final int SEARCH_OFF = 0;
    /** flag parameter for search:  select flag on required. */
    public static final int SEARCH_SELECT_ON = 1;
    /** flag parameter for search:  select flag off required. */
    public static final int SEARCH_SELECT_OFF = 2;
    /** flag parameter for search:  visit flag on required. */
    public static final int SEARCH_VISIT_ON = 4;
    /** flag parameter for search:  visit flag off required. */
    public static final int SEARCH_VISIT_OFF = 8;
    /** flag parameter for search:  visible flag on required. */
    public static final int SEARCH_VISIBLE_ON = 16;
    /** flag parameter for search:  visible flag off required. */
    public static final int SEARCH_VISIBLE_OFF = 32;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the underlying pool of vertices used by this LinePool. */
    private VertexPool vpInternal = new VertexPool();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor and Static Constructor Methods ">
    
    /** Creates a new instance of LinePool. */
    public DrawingLinePool() {
    }
    
    /** Creates a new instance of DrawingLinePool by using the list of lines.
     * @param ltLines must follow one rule, that is all lines must connect to each other. If there exists curves
     * that have a control not connected to another curve then the control point will be set to the respective end
     * point of the curve.
     * @throws IllegalArgumentException if the list of lines contain lines that are not connected to
     * each other. All lines must connect to each other.
     */
    public DrawingLinePool(LinkedList<AbstractLineInfo> ltLines) {
        merge(ltLines);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying line and vertex ">
    
    /** @returns a collection view of this AbstractLine of the underlying values in this pool.
     */
    final public Collection<AbstractLine> values() {
        return super.values();
    }
    
    /** @return the VertexPool for this pool.
     */
    final public VertexPool getVertexPool() {
        return vpInternal;
    }
    
    /** @return a collection view of the Vertices that are used in this DrawingLinePool.
     */
    final public Collection<Vertex> vertexValues() {
        return vpInternal.values();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Validation Methods ">
    
    /** This will check connectivity of all lines to make sure they do in fact
     * connect to other lines. The test is to see if any line can be taken to
     * any other line. <br>
     * WARNING:: This will modify the line's visited flag.<br>
     * @return true if one line can be used to travel to all other lines, else
     * false there exist lines that are not connected to all the others. Also
     * returns true even if there are no lines in this DrawingLinePool.
     */
    public boolean validateLineConnectivity() {
        // No lines there all would be connected.
        if (size() == 0)
            return true;
        
        // Visit all lines starting at a random point.
        set(DrawingLinePool.SEARCH_VISIT_ON);
        AbstractLine abLine = (AbstractLine)getFirst();
        validateLineConnectivityHelper(abLine.getFirstEndVertex());
        validateLineConnectivityHelper(abLine.getLastEndVertex());
        
        // All lines should be visited. If there exist any line not visited then it is not connected to this Graph.
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            abLine = itr.next();
            if (abLine.isVisited() == false)
                return false;
        }
        
        // All lines have been visited.
        return true;
    }
    
    /** This is a helper function for validateLineConnectivity(). It will
     * travel from 'vEnd' to all lines, while setting them to visited.
     * @param vEnd is the vertex to travel from.
     */
    void validateLineConnectivityHelper(Vertex vEnd) {
        LinkedList<AbstractLine> ltSearch = search(vEnd, SEARCH_VISIT_OFF);
        
        // Visit all lines.
        for (Iterator<AbstractLine> itr = ltSearch.iterator(); itr.hasNext(); )
            itr.next().setVisited(true);
        
        // Traverse all lines now.
        for (Iterator<AbstractLine> itr = ltSearch.iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            validateLineConnectivityHelper(abLine.getOppositeEndVertex(vEnd));
        }
    }
    
    /** This will validate all curves (RMolnar) to make sure they adhere to the restrict standard of all curve
     * control points connecting to another curve (RMolnar) or set to the respective end point. <br>
     * Operations performed: <br>
     * For all control points that do not connect to a line (vertex that is not adjacent to a line) they will be deleted.<br>
     * For all control points that do not connect to an adjacent curve (RMolnar) properly, they will set to the respective
     * end point of it's curve. <br>
     *  A properly connected control point is set to an adjacent curve (RMolnar): <br>
     *  Curve 1:   c1  e1  e2  c2<br>
     *  Curve 2:   x1  y1  y2  x2<br>
     *  Where c1, c2, x1, x2 are control points and e1, e2, y1, y2 are end points.<br>
     *  There are only two ways c1 can connect to curve 2.<br>
     *   (e1 = y2 and c1 = y1) OR (e1 = y1 and c1 = y2) <br>
     *  There are only two ways c2 can connect to curve 2.<br>
     *   (e2 = y2 and c2 = y1) OR (e2 = y1 and c2 = y2) <br>
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem validateCurves() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Search through all RMolnar curves.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            if (abLine instanceof RMolnar == false)
                continue;
            
            // Validate end control points.
            undoComplex.add(validateCurvesHelper((RMolnar)abLine, true));
            undoComplex.add(validateCurvesHelper((RMolnar)abLine, false));
            
            abLine.validate();
        }
        
        return undoComplex;
    }
    
    /** This is a helper function to the validateCurves() method. It will validate on the curve's control point. See
     * main function for details.
     * @param curve is the curve to validate on.
     * @param firstControlPt is true if it should validate the first control point, else false it should validate
     * the last control point.
     * @return anundo item for this operation.
     */
    private InterfaceUndoItem validateCurvesHelper(RMolnar curve, boolean firstControlPt) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the control vertex to work on.
        Vertex vControl = null;
        Vertex vEnd = null;
        if (firstControlPt) {
            vControl = curve.getFirstControlVertex();
            vEnd = curve.getFirstEndVertex();
        } else {
            vControl = curve.getLastControlVertex();
            vEnd = curve.getLastEndVertex();
        }
        
        // Search for all lines connected to this control point.
        LinkedList<AbstractLine> ltSearch = search(vControl, DrawingLinePool.SEARCH_OFF);
        
        // This control point does not connect to another line. Delete it and set the 'curve' control vertex to the respective end point.
        if (ltSearch.isEmpty()) {
            if (vpInternal.contains(vControl))
                undoComplex.add(vpInternal.remove(vControl));
            
            if (firstControlPt)
                undoComplex.add(curve.setFirstControlVertex(vEnd));
            else
                undoComplex.add(curve.setLastControlVertex(vEnd));
            return undoComplex;
        }
        
        // See if the curve's control point does in fact connect to another curve properly.
        boolean bFound = false;
        for (Iterator<AbstractLine> itrSearch = ltSearch.iterator(); itrSearch.hasNext(); ) {
            AbstractLine abSearch = itrSearch.next();
            if (abSearch instanceof RMolnar == false || abSearch == curve)
                continue;
            
            // Does this abSearch curve connect properly to the passed in curve?
            if ((abSearch.isFirstEndVertex(vControl) && abSearch.isLastEndVertex(vEnd)) ||
                (abSearch.isFirstEndVertex(vEnd) && abSearch.isLastEndVertex(vControl))) {
                bFound = true;
                break;
            }
        }
        
        // The control point does not properly connect to another curve.
        if (bFound == false) {
            if (firstControlPt)
                undoComplex.add(curve.setFirstControlVertex(vEnd));
            else
                undoComplex.add(curve.setLastControlVertex(vEnd));
        }
        
        return undoComplex;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add/Merge Methods ">
    
    /** This will add the line to this graph. The line must connect to another line in this graph
     * or be the first line in the graph.
     * @param abLineInfo is the line data to add to this graph, it must connect to another line.
     * @return an undo for this operation, can be empty.
     * @throws IllegalArgumentException Line does not conect to graph.
     */
    public InterfaceUndoItem add(AbstractLineInfo abLineInfo) {
        // If it is already in the LinePool then do not add it.
        if (contains(abLineInfo))
            return new UndoItemComplex();
        
        // Check to see if the end points match.
        Vertex v = new Vertex(abLineInfo.getEndPoint1());
        if (v.isVertexSpace(abLineInfo.getEndPoint2()))
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
    
    /** This will add the line to this graph. The line must connect to another line in this graph
     * or be the first line in the graph.
     * @param lineInfo is the line to be added to this graph.
     * @return an undo for this operation, can be empty.
     * @throws IllegalArgumentException Line does not conect to graph.
     */
    private InterfaceUndoItem addLine(LineInfo lineInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points for the line.
        Vertex vEnd1 = getNewVertex(lineInfo.getEndPoint1(), undoComplex);
        Vertex vEnd2 = getNewVertex(lineInfo.getEndPoint2(), undoComplex);
        
        // Create the line and add it to the LinePool.
        Line l = new Line(vEnd1, vEnd2);
        super.add(l);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(l));
        return undoComplex;
    }
    
    /** This will add the bezier to this graph. The bezier must connect to another line in this graph
     * or be the first line in the graph.
     * @param bezierInfo contains the information to create a bezier in this graph.
     * @return an undo for this operation, can be empty.
     */
    private InterfaceUndoItem addBezier(BezierInfo bezierInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points for the line.
        Vertex vEnd1 = getNewVertex(bezierInfo.getEndPoint1(), undoComplex);
        Vertex vEnd2 = getNewVertex(bezierInfo.getEndPoint2(), undoComplex);
        
        // Create the line and add it to the LinePool.
        Bezier b = new Bezier(vEnd1, vEnd2, bezierInfo.getControlPoint1(), bezierInfo.getControlPoint2());
        super.add(b);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(b));
        return undoComplex;
    }
    
    /** This will add the rmolnar to this graph. The rmolnar must connent to another rmolnar curve in this
     * graph or be the first rmolnar in the graph.
     * @param rmolnar contains the information to create a rmolnar in this graph. It will must connect to
     * another rmolnar.
     * @return an undo for this operation, can be empty.
     */
    private InterfaceUndoItem addRMolnar(RMolnarInfo rmolnarInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Get the end points and control points for the rmolnar.
        Vertex vEnd1 = getNewVertex(rmolnarInfo.getEndPoint1(), undoComplex);
        Vertex vEnd2 = getNewVertex(rmolnarInfo.getEndPoint2(), undoComplex);
        Vertex vControl1 = getNewVertex(rmolnarInfo.getControlPoint1(), undoComplex);
        Vertex vControl2 = getNewVertex(rmolnarInfo.getControlPoint2(), undoComplex);
        
        // Create the rmolnar.
        RMolnar r = new RMolnar(vEnd1, vEnd2, vControl1, vControl2);
        super.add(r);
        
        // Finish the undo and return the undo.
        undoComplex.add(new UndoItemNewLine(r));
        return undoComplex;
    }
    
    /** This will add each line into this pool of lines.
     * @param ltLines must follow one rule, that is all lines must connect to each other. If there exists curves
     * that have a control not connected to another curve then the control point will be set to the respective end
     * point of the curve.
     * @returns an undo item for this operation.
     * @throws IllegalArgumentException if the list of lines contain lines that are not connected to
     * each other. All lines must connect to each other.
     */
    InterfaceUndoItem merge(LinkedList<AbstractLineInfo> ltLines) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Add all AbstractLineInfo into this line pool.
        for (Iterator<AbstractLineInfo> itr = ltLines.iterator(); itr.hasNext(); )
            undoComplex.add(add(itr.next()));
        
        // This will validate the curve's control points. It will also update the curve's control points if need be.
        undoComplex.add(validateCurves());
        
        // All lines must connect to each other.
        if (validateLineConnectivity() == false)
            throw new IllegalArgumentException("The list of lines from ltLines is in valid.");
        
        return undoComplex;
    }
    
    // </editor-fold>       
                    
    // <editor-fold defaultstate="collapsed" desc=" Remove Methods ">
    
    /** This will remove the line from this LinePool.
     * @param abLineRemove is the line to be removed from this LinePool.
     * @return an undo for this operation, can be empty.
     */
    public InterfaceUndoItem remove(AbstractLine abLineRemove) {
        UndoItemComplex undoComplex = new UndoItemComplex();

        // Clear the statuses on the line.
        abLineRemove.resetStatuses();
        
        // Remove the line.
        super.remove(abLineRemove);
        undoComplex.add(new UndoItemDeleteLine(abLineRemove));
        
        return undoComplex;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Has/Is/Intersects/Contains/Status Methods ">
    
    /** This will see if the abLine is in this DrawingLinePool.
     *  @return true if the abLine exists in this DrawingLinePool, else false it does not.
     */
    public boolean contains(AbstractLine abLine) {
        AbstractLine abFind = (AbstractLine)super.find(abLine.getId());
        return (abLine == abFind);
    }
    
    /** This will search for this line in this LinePool.
     * @return true if it found the line in this LinePool, else line does not exist.
     */
    private boolean contains(AbstractLineInfo abLineInfo) {
        // Search to see if this line exists.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            if (abLine.isDupliate(abLineInfo))
                return true;
        }
        
        return false;
    }
    
    /** This will set all lines and vertices to the flag value.
     * @param flag is used to set each line and vertex to the flag value. See the SEARCH_* flag. They
     * can be OR'd together for a more specific set of values.
     */
    public void set(int flag) {
        // Set all lines visited.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            abLine.set(flag);
        }
        
        // Set all vertices visited.
        vpInternal.set(flag);
    }
    
    /** @return the number of lines in this pool..
     */
    public int size() {
        return super.size();
    }
    
    /** This will search for the first AbstractLine which contains the matching flag types.
     * @param flag is used to perform this search for one of its kind. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search.
     * @return true if there exists an AbstractLine with the matching flag type.
     */
    public boolean has(int flag) {
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); )
            if (itr.next().is(flag))
                return true;
        
        return false;
    }
    
    /**  This will check all AbstractLines in this LinePool to see if any intersect the rectangle.
     * @return true if any of the AbstractLines in this LinePool intersect the rectangle, else false.
     */
    public boolean intersects(Rectangle2D.Float r) {
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); )
            if (itr.next().intersects(r))
                return true;        
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" toList/Search/Traverse Methods ">
    
    /**  This will get the first AbstractLine which matches the flag values.
     * @param flag is used to perform this search for one of its kind. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search. The AbstractLine must match the flag values.
     * @return the first AbstractLine that matches the flag values or null if it does not find one.
     */
    public AbstractLine getFirst(int flag) {
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            if (abLine.is(flag))
                return abLine;
        }
        
        return null;
    }
    
    /** This will get a vertex from the VertexPool by creating one or finding one already at the place where
     * the point is at (it must be within the vertex space).
     * @param fpt is the point the new vertex will be at, or within the vertex space.
     * @param undoComplex is used to add an undo if a vertex was added to the VertexPool.
     * @return a Vertex at the point of fpt.
     */
    private Vertex getNewVertex(FPointType fpt, UndoItemComplex undoComplex) {
        // See if the vertex exists.
        Vertex v = vpInternal.getVertexWithinVertexSpace(fpt);
        
        // Vertex does not exist therefore create a new one.
        if (v == null) {
            v = new Vertex(fpt);
            undoComplex.add(vpInternal.add(v));
        }
        
        return v;
    }
    
    /** This will search for any AbstractLines that has the 'vEnd' as an end vertex and the line
     * containing the required flag settings. This is a heavy duty general purpose search function.
     * @param vEnd is the vertex used to search for any AbstractLines that contain it as an
     * end vertex.
     * @param serachFlag is a constant value (SEARCH_* from this class) which can be OR'd
     * together with other flag requirements. Unknown resuls if OR'd together like flag requirements
     * that are opposite.
     * @return a list of AbstractLines that has the 'vEnd' as an end vertex and meets the requirements
     * of the 'searchFlag'.
     */
    public LinkedList<AbstractLine> search(Vertex vEnd, int searchFlag) {
        LinkedList<AbstractLine> list = new LinkedList();
        
        // Search through all AbstractLines.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            if (abLine.contain(vEnd, searchFlag))
                list.add(abLine);
        }
        
        return list;
    }
    
    /** This will search for any AbstractLine intersecting the rectangle r.
     * @param r is the rectangle to search for intersections.
     * @param oneLine is the true if it should only search for one line.
     * @return a list containing the AbstractLines that intersect the rectangle r. Can be empty if nothing was found.
     */
    public LinkedList<AbstractLine> search(Rectangle2D.Float r, boolean oneLine) {
        LinkedList<AbstractLine> list = new LinkedList();
        
        // Search through all lines.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            if (abLine.intersects(r)) {
                list.add(abLine);
                
                // Since we found a line, break out. Our job is finished here.
                if (oneLine)
                    break;
            }
        }
        
        return list;
    }
    
    /** This will search for any RMolnar that has 'vControl' as a control vertex.
     * @param vControl is the vertex that is used to search for any RMolnar curves that contain
     * it as a control vertex.
     * @return a LinkedList containing only RMolnar curves that have vControl as a control vertex.
     * Can return an empty list.
     */
    public LinkedList<RMolnar> searchControl(Vertex vControl) {
        LinkedList<RMolnar> ltCurves = new LinkedList();
        
        // Search only for RMolnars.
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            // Must be RMolnar and is a control vertex.
            if (abLine instanceof  RMolnar && ((RMolnar)abLine).isControlVertex(vControl))
                ltCurves.add((RMolnar)abLine);
        }
        
        return ltCurves;
    }
    
    /** This will create a list of AbstractLine where each AbstractLine matches the flag values.
     * @param flag is used to perform this search for one of its kind. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search. Each AbstractLine must match the flag values.
     * @return a list of all AbstractLine which match the flag values. Can be empty if there does not exist
     * any lines that match the flag values.
     */
    public LinkedList<AbstractLine> toList(int flag) {
        LinkedList<AbstractLine> ltLines = new LinkedList();
        
        for (Iterator<AbstractLine> itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            if (abLine.is(flag))
                ltLines.add(abLine);                
        }
        
        return ltLines;
    }
    
    /** This will create a list of AbstractLineInfo where each AbstractLine matches the flag values.
     * @return a list of AbstractLineInfo of each line in this line pool.
     * @param flag is used to perform this search for one of its kind. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search. Each AbstractLine must match the flag values.
     * @return a list of all AbstractLine's information which match the flag values. Can be empty if there
     * does not exist any lines that match the flag values.
     */
    public LinkedList<AbstractLineInfo> toLineInfo(int flag) {
        LinkedList<AbstractLineInfo> ltLineInfo = new LinkedList();
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            AbstractLine abLine = (AbstractLine)itr.next();
            if (abLine.is(flag))
                ltLineInfo.add(abLine.getInfo());
        }
        
        return ltLineInfo;
    }
    
    /** This will traverse this line pool while setting the visited flag to true. It will only travel on lines
     * that match the flag value. 
     * <br> WARNING: This will modify the line's visited flag.
     * @param v is the vertex to start the traversing.
     * @param flag is the SEARCH_* flag. Do not use the visit flags. Before a line can be traversed it
     * must be of the flag types. They can be OR'd together for a more specific search. Each AbstractLine 
     * must match the flag value to travel on it.
     */
    public void traverse(Vertex v, int flag) {
        // Search for unvisited lines that match the flag requirement.
        flag = flag | SEARCH_VISIT_OFF;
        LinkedList<AbstractLine> ltTraverse = search(v, flag);

        // Set all lines to visited and add to the list of AbstractLineInfo.
        for (Iterator<AbstractLine> itr = ltTraverse.iterator(); itr.hasNext(); )
            itr.next().set(SEARCH_VISIT_ON);

        // Traverse each line..
        for (Iterator<AbstractLine> itr = ltTraverse.iterator(); itr.hasNext(); )
            traverse(itr.next().getOppositeEndVertex(v), flag);
    }
    
    /** This will traverse this line pool picking up lines and setting the visited flag to true. It will
     * only travel on lines that match the flag value. 
     * <br>WARNING: This will modify the line's visited flag.
     * @param ltLineInfo is the list which will be filled up with lines from this LinePool.
     * @param v is the vertex to start the traversing.
     * @param flag is the SEARCH_* flag. Do not use the visit flags. Before a line can be traversed it
     * must be of the flag types. They can be OR'd together for a more specific search. Each AbstractLine 
     * must match the flag value to travel on it.
     */
    public void traverse(LinkedList<AbstractLineInfo> ltLineInfo, Vertex v, int flag) {

        // Search for unvisited lines that match the flag requirement.
        flag = flag | SEARCH_VISIT_OFF;
        LinkedList<AbstractLine> ltTraverse = search(v, flag);

        // Set all lines to visited and add to the list of AbstractLineInfo.
        for (Iterator<AbstractLine> itr = ltTraverse.iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            
            // Visit line.
            abLine.set(SEARCH_VISIT_ON);
            
            // Add to the list of line information.
            ltLineInfo.add(abLine.getInfo());
        }

        // Traverse each line..
        for (Iterator<AbstractLine> itr = ltTraverse.iterator(); itr.hasNext(); ) {
            AbstractLine abLine = itr.next();
            traverse(ltLineInfo, abLine.getOppositeEndVertex(v), flag);
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    public String toString() {
        return "{DrawingLinePool: " + super.toString() + " VertexPool: " + vpInternal.toString() + "}";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Line ">
    
    /** This will undo/redo a new line created.
     */
    class UndoItemNewLine implements InterfaceUndoItem {
        AbstractLine abLine;
        
        UndoItemNewLine(AbstractLine abLine) {
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
            return "{LinePool.UndoItemNewLine AbstractLine[" + abLine + "]}";
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Delete Line ">
    
    /** This will undo/redo a deleted line.
     */
    class UndoItemDeleteLine implements InterfaceUndoItem {
        AbstractLine abLine;
        
        UndoItemDeleteLine(AbstractLine abLine) {
            this.abLine = abLine;
        }
        
        public void undoItem() {
            restore(abLine);
        }
        
        public void redoItem() {
            remove(abLine);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{LinePool.UndoItemDeleteLine AbstractLine[" + abLine + "]}";
        }
    }
    
    // </editor-fold>
    
}
