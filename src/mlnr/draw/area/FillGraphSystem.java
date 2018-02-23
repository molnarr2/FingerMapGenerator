/*
 * FillGraph.java
 *
 * Created on Oct 2, 2007, 2:47:50 PM
 *
 */
package mlnr.draw.area;

import mlnr.draw.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import mlnr.util.UndoItemComplex;

/** This class is responsible for filling in a drawing (passing in a list of DrawingGraph) with colors. It does not modify the DrawingGraphs. Instead it
 *  will build areas to color in. The way it performs this is by creating the largest bounds of graphs, this is defined by taking all DrawingGraphs that
 *  intersect each other and from those DrawingGraphs an outer bound is created. Once these bounds are created they are used to know if the user
 *  clicked instead of an area to fill-in in the drawing. Once a user clicks instead a color bound is created inside the graph with the smallest bounding
 *  area possible around that user clicking.
 *  <br> This system makes use of intersecting lines and curves. Therefore this system is heavily mathematical in nature. 
 * @author Rob
 */
public class FillGraphSystem {

    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">

    /** This is true if to print out information. */
    private static boolean DEBUG_MODE = false;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Fields ">

    /** This is the list of color bounds. */
    private LinkedList<ColorBound> ltColors = new LinkedList<ColorBound>();
    /** This is the list of outter bounds of each complete graph. */
    private LinkedList<NegativeBound> ltNegativeBounds = new LinkedList<NegativeBound>();
    // private LinkedList<NegativeBounds>

    /** temporary storage of coordinates, use by functions. */
    float[] _tmpCoords = new float[4];

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Constructors ">

    /** Create a new FillGraphSystem.
     *  @param ltDrawingGraphs is a list of DrawingGraphs for all layers. They are used to fill them in.
     */
    public FillGraphSystem(LinkedList<DrawingGraph> ltDrawingGraphs) {
        this.ltColors = new LinkedList<ColorBound>();
        this.ltNegativeBounds = new LinkedList<NegativeBound>();

        // Build the negative bounds.
        buildNegativeBounds(ltDrawingGraphs);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Public Methods ">

    /** This will draw the filled in bounds.
     */
    public void draw(Graphics2D g2d) {
        Color old = g2d.getColor();

        // DEBUG
        g2d.setColor(Color.RED);
        for (Iterator<NegativeBound> itr = ltNegativeBounds.iterator(); itr.hasNext();) {
            NegativeBound bound = itr.next();
            g2d.fill(bound.area);
        }

        for (Iterator<ColorBound> itr = ltColors.iterator(); itr.hasNext();) {
            ColorBound bound = itr.next();
            g2d.setColor(bound.color);
            g2d.fill(bound.area);
        }

        if (DEBUG_MODE) {
            drawDebug(g2d);
        }

        g2d.setColor(old);
    }

    public void drawAllBitmap(Graphics2D g2d, LayerInfo lInfo, boolean changeColor) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** This will fill in an area if it is enclosed.
     *  @param fpt is the point where the user clicked at.
     *  @param c is the color to fill in the enclosed area.
     */
    public InterfaceUndoItem fill(FPointType fpt, Color c) {
        // Does the area already exist?
        for (Iterator<ColorBound> itr = ltColors.iterator(); itr.hasNext();) {
            ColorBound bound = itr.next();

            // Found the bounds then update the color.
            if (bound.area.contains(fpt.x, fpt.y)) {
                Color oldColor = bound.color;
                bound.color = c;
                return new UndoItemChangeColor(bound, oldColor, c);
            }
        }

        // Did the user click inside an area?
        for (Iterator<NegativeBound> itr = ltNegativeBounds.iterator(); itr.hasNext();) {
            NegativeBound bound = itr.next();

            // Found the bounds.
            if (bound.area.contains(fpt.x, fpt.y)) {
                // Build the area around the fpt.
                Area area = buildInnerBoundArea(fpt, bound);

                // Create a color bound.
                ColorBound colorBound = new ColorBound(area, c);
                ltColors.add(colorBound);

                // Update the negative bounds to have that bound subtracted from that area.
                // bound.area.subtract(area);

                // Negative is gone.
                if (bound.area.isEmpty()) {
                    itr.remove();
                }

                return new UndoItemChangeColor(colorBound, new Color(255, 255, 255), c);
            }
        }

        // User didn't click inside any area. Do nothing then.
        return new UndoItemComplex();
    }

    /** @return a list of all the colors which represent all the layers.
     */
    public LinkedList<Color> getColors() {
        throw new UnsupportedOperationException("TODO");
    //        LinkedList ltColors = new LinkedList();
//        for (Iterator itr=ltGeneralPath.iterator(); itr.hasNext();) {
//            Color c = ((RMGeneralPath)itr.next()).getColor();
//            ltColors.add(c);
//        }
//        
//        return ltColors;
    }

    /** This will get a GeneralPath's color for the layer.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @param currLayerOnly is true if to search only the current layer to fill in.
     * @return the color of the GeneralPath if user clicked on it, else null no color.
     */
    public Color getColor(FPointType fpt) {
        throw new UnsupportedOperationException("TODO");
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Build Methods ">    

    /** This will build an area from the fpt point and it will be the innermost part of the graph. Any NegativeBounds within the innermost part of the
     *  graph will be negated out of the Area.
     *  @param fpt is the point used to create the area.
     *  @param bound is the NegativeBound area the Area should be built from. 
     *  @return an area which will fill in the innermost part of the graph. 
     */
    Area buildInnerBoundArea(FPointType fpt, NegativeBound bound) {
        if (DEBUG_MODE) {
            System.out.println();
            System.out.println();
            System.out.println("buildInnerBoundArea()");
        }

        // Get the starting position.
        TraversePosition tStart = getFirstTraversePosition(fpt, bound);

        // Traverse Graph.
        TraversePosition tCurrent = tStart;
        TraverseSegment tPrevious = null;
        GeneralPath generalPath = new GeneralPath();
        boolean bFirst = true;
        boolean updatedSegmentToStart = false;
        while (updatedSegmentToStart == false) {
            // Traverse the next segment.
            TraverseSegment segment = ProduceTraverseSegmentOp.traverseSegment(tCurrent, bound.ltGraphInfo, tPrevious);

            // This will update the segment to the starting position if need be and thus this segment will be the last one.
            if (bFirst == false) {
                updatedSegmentToStart = segment.performToStart(tStart);
            }

            if (DEBUG_MODE) {
                System.out.println(segment.toStringCompact());
            } // Print out the segment.

            // Append the segment to the GeneralPath.
            generalPath.append(segment.getShape(), true);

            // Next becomes current.
            bFirst = false;
            tCurrent = segment.getNext();
            tPrevious = segment;
        }

        return new Area(generalPath);
    }

    /** This will build the outer bounds of a graph, by traversing the lines, curves to create it. This will be the most outer part of graphs.
     *  @param ltGraphs is a list of DrawingGraph to create the outer bound from.
     *  @return an CurveLinePath of the outer bound of the DrawingGraph.
     */
    CurveLinePath buildOuterBoundPath(LinkedList<DrawingGraph> ltGraphs) {
        if (DEBUG_MODE) {
            System.out.println();
            System.out.println();
            System.out.println("buildOuterBoundArea()");
        }

        // Get the starting position.
        TraversePosition tStart = getFirstTraversePosition(ltGraphs);

        // Traverse Graph.
        TraversePosition tCurrent = tStart;
        CurveLinePath path = new CurveLinePath();
        boolean bFirst = true;
        boolean updatedSegmentToStart = false;
        TraverseSegment tPrevious = null;
        while (updatedSegmentToStart == false) {
            // Traverse the next segment.
            TraverseSegment segment = ProduceTraverseSegmentOp.traverseSegment(tCurrent, ltGraphs, tPrevious);

            // This will update the segment to the starting position if need be and thus this segment will be the last one.
            if (bFirst == false) {
                updatedSegmentToStart = segment.performToStart(tStart);
            }

            if (DEBUG_MODE) {
                System.out.println(segment.toStringCompact());
            } // Print out the segment.

            // Append the segment to the GeneralPath.
            path.append(segment.getShape());

            // Next becomes current.
            bFirst = false;
            tCurrent = segment.getNext();
            tPrevious = segment;
        }

        return path;
    }

    /** This will build a list of GraphInfo used to build the NegativeBound graphs.
     *  @param ltDrawingGraphs is the list DrawingGraph used to build the list of GraphInfo.
     *  @return a list of GraphInfo. Contains information needed to build the NegativeBound graphs.
     */
    LinkedList<GraphInfo> buildGraphInfo(LinkedList<DrawingGraph> ltDrawingGraphs) {
        // Convert the DrawingGraph list into a list of GraphInfo. Used only as a cache for the bounds of the DrawingGraph and a list of intersected graphs.
        LinkedList<GraphInfo> ltGraphInfo = new LinkedList<GraphInfo>();
        for (Iterator<DrawingGraph> itr = ltDrawingGraphs.iterator(); itr.hasNext();) {
            ltGraphInfo.add(new GraphInfo(itr.next()));
        }

        // Build the intersected lists.
        for (Iterator<GraphInfo> itrOuter = ltGraphInfo.iterator(); itrOuter.hasNext(); ) {
            GraphInfo outer = itrOuter.next();
            
            for (Iterator<GraphInfo> itrInner = ltGraphInfo.iterator(); itrInner.hasNext(); ) {
                GraphInfo inner = itrInner.next();
                if (outer == inner)
                    continue;
                
                // If the inner intersects the outer then add it to its list of intersects.
                if (outer.bound2d.intersects(inner.bound2d) && outer.graph.intersects(inner.graph))
                    outer.addIntersect(inner);                
            }
        }
        
        return ltGraphInfo;
    }
    
    /** This will build a list of DrawingGraph where each DrawingGraph is connected to each other. Thus it becomes a graph of DrawingGraph. They are
     * connected by lines or curves intersecting each other. After this function is finished any GraphInfo used in creating the list of GraphInfo will have their
     * used flag set to true. This includes the passed in 'info' GraphInfo.
     *  @param ltGraphInfo is the list of GraphInfo used to traverse to create the list of DrawingGraph.
     *  @param info is the starting point used to create the list of DrawingGraph. It's used flag will be set to true.
     *  @return a list of DrawingGraph where each DrawingGraph is connected to each other. Thus it becomes a graph of DrawingGraph.
     */
    LinkedList<DrawingGraph> buildIntersectList(LinkedList<GraphInfo> ltGraphInfo, GraphInfo info) {
        LinkedList<DrawingGraph> ltGraphs = new LinkedList<DrawingGraph>();
        
        // Make sure flag is set and add it to the list.
        info.used = true;
        ltGraphs.add(info.graph);
        
        for (Iterator<GraphInfo> itr = info.ltIntersectedGraphs.iterator(); itr.hasNext(); ) {
            GraphInfo curr = itr.next();            
            buildIntersectListHelper(ltGraphs, ltGraphInfo, curr);
        }
        
        return ltGraphs;
    }
    
    /** This is a helper function for the buildIntersectList() function. It is recursive and will traverse each GraphInfo. Cannot traverse a graph that was
     *  already traversed..
     */
    void buildIntersectListHelper(LinkedList<DrawingGraph> ltGraphs, LinkedList<GraphInfo> ltGraphInfo, GraphInfo info) {
        // If traversed them do not traverse it again.
        if (info.used)
            return;
        
        // Make sure flag is set and add it to the list.
        info.used = true;
        ltGraphs.add(info.graph);
        
        for (Iterator<GraphInfo> itr = info.ltIntersectedGraphs.iterator(); itr.hasNext(); ) {
            GraphInfo curr = itr.next();            
            buildIntersectListHelper(ltGraphs, ltGraphInfo, curr);
        }
    }
    
    /** This will build the negative bounds for this system. It will first create a list of all DrawingGraphs that intersect each other by a line or curve. Then
     *  it will create the area of the outermost part of the graphs.
     */
    void buildNegativeBounds(LinkedList<DrawingGraph> ltDrawingGraphs) {        
        // Get the GraphInfo list. Each GraphInfo contains a list of each graph that it intersects.
        LinkedList<GraphInfo> ltGraphInfo = buildGraphInfo(ltDrawingGraphs);
        
        // Build the NegativeBounds.
        for (Iterator<GraphInfo> itr = ltGraphInfo.iterator(); itr.hasNext(); ) {
            GraphInfo info = itr.next();
            if (info.used)
                continue;
            
            // Create the NegativeBound.
            // This will create a list of DrawingGraph where each one is in somehow connected to each other by intersections. For each
            // GraphInfo that traversed by this function it's used flag will be set to true.
            LinkedList<DrawingGraph> ltGraphs = buildIntersectList(ltGraphInfo, info);
            Area area = buildOuterBoundPath(ltGraphs).toArea();
            NegativeBound bound = new NegativeBound(ltGraphs, area);
            ltNegativeBounds.add(bound);
        }
    }

    /**This will calculate the bounds of the list of graphs.
     *  @param ltGraphs is a list of graphs.
     *  @return The bounds of the list of graphs.
     */
    Rectangle2D.Float calculateBounds(LinkedList<DrawingGraph> ltGraphs) {
        Rectangle2D.Float bounds = new Rectangle2D.Float();

        for (Iterator<DrawingGraph> itr = ltGraphs.iterator(); itr.hasNext();) {
            DrawingGraph dGraph = itr.next();
            Rectangle2D.Float graphBounds = dGraph.getBounds2D();
            if (graphBounds == null) {
                continue;
            }
            bounds.add(graphBounds);
        }

        return bounds;
    }

    /**This will get the first traverse position for an inner graph traversal.
     *  @param fpt is the point to start the search from.
     *  @param bound is the negative bound where the point is within.
     *  @return the first traverse position. It will be in the direction to perform an inner graph traversal.
     */
    TraversePosition getFirstTraversePosition(FPointType fpt, NegativeBound bound) {
        // Create a line from fpt to (0,0).
        AbstractLine startLine = new Line(new Vertex(new FPointType(fpt.x, fpt.y)), new Vertex(new FPointType(0.0f, 0.0f)));
        float startParametric = 0.0f;

        // Starting position is from (fpt) and direction is to (0,0).
        TraversePosition tStart = new TraversePosition(startLine, startParametric, startLine.getLastEndVertex());

        // Traverse until the first intersection. This will be the starting position of building the outer bound of the area.
        TraverseSegment segment = ProduceTraverseSegmentOp.traverseSegment(tStart, bound.ltGraphInfo, null);
        tStart = segment.getNext();

        if (DEBUG_MODE) {
            System.out.println("Starting Position: " + tStart);
        } // Print out the starting position.

        // Invert the direction.
        return tStart;
    }

    /** This will get the first traverse position for an outer graph traversal.
     *  @param ltGraphs is the list of graphs used to get the first TraversePosition.
     *  @return the first traverse position. It will be in the direction to perform an outer graph traversal.
     */
    TraversePosition getFirstTraversePosition(LinkedList<DrawingGraph> ltGraphs) {
        Rectangle2D.Float bounds = calculateBounds(ltGraphs);
        float x = (float) bounds.getX() + (float) bounds.getWidth() + 10.0f; // The 10 is for extra padding
        float y = (float) bounds.getY() + (float) bounds.getHeight() + 10.0f; // The 10 is for extra padding

        // Create a line from (0,0) to the edge of the bounds of the graph. 
        AbstractLine startLine = new Line(new Vertex(new FPointType(0.0f, 0.0f)), new Vertex(new FPointType(x, y)));
        float startParametric = 0.0f;

        // Starting position is from (0,0) and direction is to the bounds of the graph.
        TraversePosition tStart = new TraversePosition(startLine, startParametric, startLine.getLastEndVertex());

        // Traverse until the first intersection. This will be the starting position of building the outer bound of the area.
        TraverseSegment segment = ProduceTraverseSegmentOp.traverseSegment(tStart, ltGraphs, null);
        tStart = segment.getNext();

        if (DEBUG_MODE) {
            System.out.println("Starting Position: " + tStart);
        } // Print out the starting position.

        return tStart;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" DEBUG Methods ">

    /** This will draw out the debug information. Should be called after drawing the bounds.
     */
    private void drawDebug(Graphics2D g2d) {
        Color oldColor = g2d.getColor();
        Font fOld = g2d.getFont();

        Font fNew = new Font(null, Font.PLAIN, 1);
        g2d.setFont(fNew.deriveFont(0.5f));
        g2d.setColor(Color.BLACK);
        // Print out the color bounds.
        for (Iterator<NegativeBound> itr = ltNegativeBounds.iterator(); itr.hasNext();) {
            NegativeBound bound = itr.next();
            Rectangle2D fBounds = bound.area.getBounds2D();
            g2d.drawString("Negative", (float) fBounds.getCenterX(), (float) fBounds.getCenterY());
        }

        g2d.setColor(oldColor);
        g2d.setFont(fOld);

    }

    public static boolean isDebug() {
        return DEBUG_MODE;
    }

    public static void setDebug(boolean debugMode) {
        DEBUG_MODE = debugMode;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Layer ">

    /** This will undo/redo a change in color.
     */
    class UndoItemChangeColor implements InterfaceUndoItem {

        ColorBound bound;
        Color oldColor;
        Color newColor;

        /** @param bound is the ColorBound that had its color changed.
         *  @param oldColor is the previous color.
         *  @param newColor is the new color changed to.
         */
        UndoItemChangeColor(ColorBound bound, Color oldColor, Color newColor) {
            this.bound = bound;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        public void undoItem() {
            bound.color = oldColor;
        }

        public void redoItem() {
            bound.color = newColor;
        }

        public boolean isUndoable() {
            return true;
        }

        public String toString() {
            return "{FillGraphSystem.UndoItemChangeColor bound[" + bound + "] new color[" + newColor + "] old color[" + oldColor + "]}";
        }
    }

    // </editor-fold>

}

// <editor-fold defaultstate="collapsed" desc=" Class GraphInfo ">

class GraphInfo {
    /** This is the list of intersected graphs.  */
    LinkedList<GraphInfo> ltIntersectedGraphs = new LinkedList<GraphInfo>();    
    DrawingGraph graph;
    Rectangle2D.Float bound2d;
    /** True if this GraphInfo was already used in building a NegativeBound. */
    boolean used = false;

    GraphInfo(DrawingGraph graph) {
        this.graph = graph;
        this.bound2d = graph.getBounds2D();
    }
    
    /** This will add the graph to the list of intersected graphs.
     */
    void addIntersect(GraphInfo graph) {
        ltIntersectedGraphs.add(graph);
    }    
}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" Class ColorBound ">

class ColorBound {

    Area area;
    Color color;

    ColorBound(Area area, Color color) {
        this.area = area;
        this.color = color;
    }
}

// </editor-fold>

// <editor-fold defaultstate="collapsed" desc=" Class NegativeBound ">

/** This class is used to represent a negative bound area. It is the outermost area of multiple graphs that intersect each other.
*/
class NegativeBound {
    LinkedList<DrawingGraph> ltGraphInfo;
    Area area;
    
    NegativeBound(LinkedList<DrawingGraph> ltGraphInfo, Area area) {
        this.ltGraphInfo = ltGraphInfo;
        this.area = area;
    }
}

// </editor-fold>
