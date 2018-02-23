/*
 * Layer.java
 *
 * Created on July 24, 2006, 1:36 PM
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import org.w3c.dom.*;
import mlnr.util.*;
import mlnr.type.*;

/** This class is a wrapper around a layer. A layer can have a color assigned to it, name assigned to it,
 * and position ordering assigned to it.
 * @author Robert Molnar II
 */
public class DrawingLayer implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the layer id, it should be unique to all other layers in a design. */
    private int id;
    
    /** This is the name of the layer. */
    private String name;
    
    /** This is the color outline of the layer. */
    private Color color;
    
    /** True if the layer is visible, else false not visible. */
    private boolean visible=true;
    
    /** This is the z-depth for sorting the layers to know which ones to draw before the others. */
    private int zDepth;
    
    /** This is the GraphPool that contains all graphs for this Layer. */
    DrawingGraphPool gPool = new DrawingGraphPool();
    
    /** This is the layer that this layer corresponds to. It is used when selecting items from this layer, if there is
     * anything selected than it will create a new layer and set this lPointTo to that layer. */
    DrawingLayer lPointTo = null;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" User-Setting Fields ">
    
    /** This is the color the first layer begins as. */
    private static Color masterColor = Color.BLUE;
    
    /** This is the name of the first layer begins as. */
    private static final String masterName = "Untitled";
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    /** Creates a new instance of Layer
     * @param name is the name of this layer.
     * @param color is the color of this layer.
     * @param zDepth is the depth for sorting the layers in the LayerPool.
     */
    public DrawingLayer(String name, Color color, int zDepth) {
        this.name = name;
        this.color = color;
        this.zDepth = zDepth;
    }
    
    /** Create a layer out of the TransformLayer.
     * @param tLayer is the TransformLayer to create this DrawingLayer from.
     */
    public DrawingLayer(TransformLayer tLayer) {
        this.name = tLayer.getName();
        this.color = tLayer.getColor();
        this.zDepth = 1;
        
        for (Iterator<TransformGraph> itr = tLayer.toList().iterator(); itr.hasNext(); ) {
            TransformGraph tGraph = itr.next();
            add(tGraph);
        }
        
    }
    
    /** This will create the init layer with the default settings.
     * @return A layer setup as default layer.
     */
    public static DrawingLayer createDefaultLayer() {
        DrawingLayer l = new DrawingLayer(masterName, masterColor, 0);
        return l;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Get/Set/Status/toString Methods ">
        
    /** @return The bounds of the Layer by using the measurements of the graphs, else null.
     */
    public Rectangle2D.Float getBounds2D() {
        if (gPool.size() == 0)
            return null;
        
        // Initial size of rectangle.
        Rectangle2D.Float fRectangle = gPool.getFirst().getBounds2D();
        if (fRectangle == null)
            return null;
        
        // Grow the rectangle.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph tLayer = itr.next();
            Rectangle2D.Float fRect = tLayer.getBounds2D();
            fRectangle.add(fRect);
        }
        
        return fRectangle;
    }    
    
    public Color getColor() {
        return color;
    }
        
    public LayerInfo getInfo() {
        return new LayerInfo(id, zDepth, name, color, visible);
    }
    
    public String getName() {
        return name;
    }
    
    /** @return true if layer is visible.
     */
    public boolean isVisible() {
        return visible;
    }
    
    /** This will set the visability of the layer.
     * @param visability is what the layer should be set to.
     */
    public void setVisability(boolean visability) {
        this.visible = visability;
    }        
        
    public String toString() {
        return "{Layer id[" + id + "] name[" + name + "] color[" + color + "] visible[" + visible + "] zDepth[" 
                + zDepth + "] \ngPool[" + gPool + "]}";
    }
    
    /** This will update the layer information such as name, color, visible, and zDepth.
     */
    public void updateLayer(LayerInfo lInfo) {
        this.name = lInfo.getName();
        this.color = lInfo.getColor();
        this.visible = lInfo.isVisible();
        this.zDepth = lInfo.getZDepth();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eLayer is the element for the layer in the RXML file.
     * @param zDepth is the z-depth for the layer, needed since the RXML file does not supply it. 
     * @return layer from the element information. Must add the layer since the id does not exist.
     */
    static DrawingLayer loadVersion10(Element eLayer, int zDepth) throws Exception {
        String name = XmlUtil.getAttributeString(eLayer, "name");
        int red = XmlUtil.getAttributeInteger(eLayer, "red");
        int green = XmlUtil.getAttributeInteger(eLayer, "green");
        int blue = XmlUtil.getAttributeInteger(eLayer, "blue");
        Color color = new Color(red, green, blue);
       
        // Set up the new layer.
        DrawingLayer l = new DrawingLayer(name, color, zDepth);
        
        // List of loaded graphs from the file. Now load in the graphPool.
        Element eGraphPool = XmlUtil.getElementByTagName(eLayer, "graph");
        for (Iterator<DrawingGraph> itr = DrawingGraph.loadVersion10(eGraphPool).iterator(); itr.hasNext();  )
            l.gPool.add(itr.next());
        
        return l;
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eLayer is the element for the layer in the RXML file.
     * @return layer from the element information.
     */
    static DrawingLayer loadVersion20(Element eLayer) throws Exception {
        int id = XmlUtil.getAttributeInteger(eLayer, "id");
        String name = XmlUtil.getAttributeString(eLayer, "name");
        Color color = new Color(XmlUtil.getAttributeInteger(eLayer, "color"));
        int zDepth = XmlUtil.getAttributeInteger(eLayer, "zDepth");

        // Set up the new layer.
        DrawingLayer l = new DrawingLayer(name, color, zDepth);
        l.id = id;
        
        // Now load in the graphPool.
        Element eGraphPool = XmlUtil.getElementByTagName(eLayer, "graphPool");        
        NodeList nList = eGraphPool.getElementsByTagName("graph");
        int length = nList.getLength();        
        for (int i=0; i < length; i++) {
            for (Iterator<DrawingGraph> itr = DrawingGraph.loadVersion20((Element)nList.item(i)).iterator(); itr.hasNext();  )
                l.gPool.add(itr.next());
        }
        
        return l;
    }

    /** This will write out each layer information.
     */
    void write(PrintWriter out) {
        out.println("    <layer id='" + id + "' name='" + XmlUtil.fixup(name) + "' color='" + color.getRGB() + "' zDepth='" + zDepth + "'>");
        out.println("     <graphPool>");
        for (Iterator<DrawingGraph> itr=gPool.values().iterator(); itr.hasNext(); )
            itr.next().write(out);
        out.println("     </graphPool>");
        out.println("    </layer>");        
    }
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
        
    /** This will draw lines in the LinePool.
     * @param g2d is the graphics class.
     */
    public void draw(Graphics2D g2d) {
        if (visible) {
            g2d.setColor(color);
            
            // Draw the Graphs.
            for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )                
                itr.next().draw(g2d);
        }
    }
    
    /** This is used to draw into a bitmap.
     * @param g2D is the graphics class.
     * @param changeColor is true if it should change color for the layers. 
     */
    public void drawBitmap(Graphics2D g2d, boolean changeColor) {
        if (changeColor)
            g2d.setColor(color);
            
        // Draw the Graphs.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )                
            itr.next().drawBitmap(g2d);
    }
    
    // </editor-fold>       
        
    // <editor-fold defaultstate="collapsed" desc=" Select Methods ">

    /** This will finalize an add point operation. It will must perform special operations that mergeSelected() cannot perform. 
     * This is due to the nature of the add point operation where the graph could possibly be broken into two graphs.
     * @param tLayer is the layer with the selected items from this layer that need to be merged back into this layer.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem addPointFinalize(TransformLayer tLayer) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // There should only be one TransformGraph in the TransformLayer. And get that TransformGraph.
        if (tLayer.getGraphCount() != 1)
            throw new IllegalArgumentException("TransformLayer does not have 1 graph. Size: " + tLayer.getGraphCount());
        TransformGraph tGraph = tLayer.toList().getFirst();
        
        // Get the DrawingGraph which corresponds to the TransformGraph.
        DrawingGraph dGraph = gPool.getGraph(tGraph.getGraphTo());
        
        // Do not even alter the graph, just delete it.
        undoComplex.addFirst(gPool.remove(dGraph));

        // Now add in the visible lines into the TransformGraph.
        tGraph.addAll(dGraph.toAbstractLineInfo(true));

        // Now add in the TransformGraph.
        undoComplex.addFirst(add(tGraph));
        dGraph.resetStatus();
        
        return undoComplex;
    }
    
    /** @param fptMousePos is the position used to see if the point is within a vertex proximity.
     * @return true if the point is within a vertex proximity.
     */
    public boolean isPointWithinVertex(FPointType fptMousePos) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            boolean hit = itr.next().isPointWithinVertex(fptMousePos);
            if (hit)
                return true;
        }
        
        return false;
    }
    
    /** @return true if there are any selected items currently selected.
     */
    public boolean isSelectedItems() {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            if (itr.next().hasSelectedItems(true))
                return true;
        }
        
        return false;
    }
    
    /** This will copy the selected items leaving this DrawingLayer unchanged. Only the lines which are currently selected will be copied.
     * @return a TransformLayer of the selected items and can be empty if the user has nothing selected.
     */
    public TransformLayer copySelectedItems() {
        TransformGraphPool tgPool = new TransformGraphPool();
        
        // Return empty if nothing is selected.
        if (isSelectedItems() == false)
            return new TransformLayer(tgPool, id, name, color);
                        
        // Search all DrawingGraphs.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();

            // Does it contain any selected items?
            if (g.hasSelectedItems(true) == false)
                continue;
            
            // Create a TransformGraph of the selected items.
            TransformGraph gNew = g.copySelectedItems();
            tgPool.add(gNew);
        }
        
        return new TransformLayer(tgPool, id, name, color);
    }
    
    /** This will deselect all and will set all lines and vertices as visible.
     */
    public void deselectAll() {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            itr.next().deselectAll();
        }
    }
    
    /** This will deselect the Graphs from the selected items in this Layer.
     * @param r is the rectangle to deselect items from this Layer.
     */
    public void deselectGraphs(Rectangle2D.Float r) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            
            // If the graph intersects the rectangle then deselect it.
            if (g.intersects(r))
                g.setSelect(false);
        }
    }
    
    /** This will deselect Graphs from the selected items in the Layer.
     * @param r is the rectangle to deselect items from this Layer.
     */
    public void deselectLines(Rectangle2D.Float r) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) 
            itr.next().deselectLines(r);
    }
    
    /** This will deselect Graphs from the selected items in the Layer.
     * @param r is the rectangle to deselect items from this Layer.
     */
    public void deselectVertices(Rectangle2D.Float r) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) 
            itr.next().deselectVertices(r);
    }
    
    /** See DrawingLinePool.getSelectedItems() for more details.
     * @return a TransformLayer of the selected items. Can be empty.
     */
    public TransformLayer getSelectedItems() {
        TransformGraphPool tgPool = new TransformGraphPool();
        
        // Return empty if nothing is selected.
        if (isSelectedItems() == false) {
            TransformLayer tLayer = new TransformLayer(tgPool, id, name, color);
            tLayer.setCompletelyConnected(true);
            return tLayer;
        }
                        
        // Search all DrawingGraphs.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();

            // Does it contain any selected items?
            if (g.hasSelectedItems(true) == false)
                continue;
            
            // Create a TransformGraph of the selected items.
            TransformGraph gNew = g.getSelectedItems();
            gNew.setGraphTo(g.getId());
            tgPool.add(gNew);
        }
        
        TransformLayer tLayer = new TransformLayer(tgPool, id, name, color);
        tLayer.setCompletelyConnected(true);
        return tLayer;
    }
    
    /** This will merge the selected items from the tLayer into the items of this layer. Actually
     * the tLayer was created by this DrawingLayer for selection, therefore the lines in the
     * tLayer point back to lines in this DrawingLayer.
     * @param tLayer is the layer with selected items from this layer to merge into this layer.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem mergeSelected(TransformLayer tLayer) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        LinkedList<DrawingGraph> ltUpdated = new LinkedList<DrawingGraph>();
        
        // Merge each TransformGraph into the corresponding DrawingGraph.
        for (Iterator<TransformGraph> itr = tLayer.toList().iterator(); itr.hasNext(); ) {
            TransformGraph tGraph = itr.next();
            
            // Get the graph.
            DrawingGraph gMerge = gPool.getGraph(tGraph.getGraphTo());
            ltUpdated.add(gMerge);
            
            // Merge in the selected items from the TransformGraph into the DrawingGraph.
            undoComplex.add(gMerge.mergeSelected(tGraph));
        }
                
        // Attempt to merge the DrawingGraphs together that were updated.
        undoComplex.add(merge(ltUpdated));
        
        // Remove graphs with no lines.
        undoComplex.add(removeNoLinesGraphs(ltUpdated));
        
        return undoComplex;
    }
    
    /** This will remove all DrawingGraphs that contain no lines by looking only at the graphs in
     * the ltGraphs list.
     * @param ltGraphs is a list of DrawingGraph that have been updated.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem removeNoLinesGraphs(LinkedList<DrawingGraph> ltGraphs) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        for (Iterator<DrawingGraph> itr = ltGraphs.iterator(); itr.hasNext(); ) {
            DrawingGraph dGraph = itr.next();
            if (dGraph.getLineCount() == 0)
                undoComplex.add(gPool.remove(dGraph));
        }
        
        return undoComplex;
    }    
        
    /** This will select one Bezier curve. 
     * @param r is the rectangle to select the Bezier curve.
     * @return true if it selected the Bezier curve.
     */
    public boolean selectBezier(Rectangle2D.Float r) {
        boolean bSelect = false;
        
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            if (g.selectBezier(r)) {
                bSelect = true;
                break;
            }
        }
        
        return bSelect;
    }
    
    /** This will select Graphs from this graph.
     * @param r is the rectangle to select the graphs.
     * @param oneGraph is true if only one graph is to be selected.
     */
    public boolean selectGraphs(Rectangle2D.Float r, boolean oneGraph) {
        boolean bSelect = false;
        
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            
            // See if the graph selected anything.
            if (g.intersects(r)) {
                bSelect = true;
                g.setSelect(true);
                
                // if only select one item then no more selecting.
                if (oneGraph)
                    break;
            }
        }
        
        return bSelect;
    }
    
    /** This will select lines from this graph.
     * @param r is the rectangle to select the lines.
     * @param oneLine is true if only one line is to be selected.
     */
    public boolean selectLines(Rectangle2D.Float r, boolean oneLine) {
        boolean bSelect = false;
        
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            if (g.selectLines(r, oneLine)) {
                bSelect = true;
                
                if (oneLine)
                    break;
            }
        }
        
        return bSelect;
    }
    
    /** This will select vertices from this graph.
     * @param r is the rectangle to select the vertices.
     * @param oneVertex is true if only one vertex is to be selected.
     */
    public boolean selectVertices(Rectangle2D.Float r, boolean oneVertex) {
        boolean bSelect = false;
        
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            if (g.selectVertices(r, oneVertex)) {
                bSelect = true;
                
                if (oneVertex)
                    break;
            }
        }
        
        return bSelect;
    }
       
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Graph Methods ">
    
    /** This will add the graph to this Layer. 
     * @param gAdd is the graph to be added to this Design.
     * @return undo item for this operation.
     */
    public InterfaceUndoItem add(TransformGraph gAdd) {
        // Foreach DrawingGraph dg
        //   if dg contains gAdd
        //      Add connecting lines
        //      if gAdd is finished visiting break
        //   end
        // End
        // if gAdd is not finished then create DrawingGraph out of graphs in the gAdd
        // Foeach DrawingGraph updated
        //   Foreach DrawingGraph updated
        //     Attempt to Merge
        //   End
        // End
        
        UndoItemComplex undoComplex = new UndoItemComplex();        
        LinkedList<DrawingGraph> ltUpdated = new LinkedList<DrawingGraph>();
        
        // Clear all lines and vertices in the graph to visited of false.
        gAdd.setVisited(false);
        
        // Loop through each DrawingGraph and see if any of the points connect to the 'gAdd' graph.
        // If so then add each line connecting to the graph.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph dg = itr.next();
            
            // Does the current DrawingGraph connect to the 'gAdd' graph?'
            if (dg.containsUnvisited(gAdd)) {
                // Add the Lines that connect to this DrawingGraph.
                undoComplex.add(dg.addUnvisited(gAdd));
                ltUpdated.add(dg);                
                
                // All lines have been accounting for.
                if (gAdd.isVisited())
                    break;
            }
        }
        
        // Create as many seperate DrawingGraphs from the TransformGraph until TransformGraph has been completely visited.
        // Add them to the GraphPool.
        for (Iterator<DrawingGraph> itr = DrawingGraph.newGraphsFromUnvisited(gAdd).iterator(); itr.hasNext(); )
            undoComplex.add(gPool.add(itr.next()));
        
        // Attempt to merge the DrawingGraphs together.
        undoComplex.add(merge(ltUpdated));
        
        return undoComplex;               
    }
    
    /** This will add each graph to this Layer.
     * @param ltGraphs is a list of graphs to be added to this layer.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem addAll(LinkedList<TransformGraph> ltGraphs) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        for (Iterator<TransformGraph> itr = ltGraphs.iterator(); itr.hasNext(); ) {
            undoComplex.add(add(itr.next()));
        }
        
        return undoComplex;
    }
    
    /** @return list of GeneralTrees of this Layer.
     */
    public LinkedList<GeneralTree> buildGeneralTrees() {
        // Create a transform graph of all items in this layer and then have that create the GeneralTree.
        TransformGraph tGraph = new TransformGraph();
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            tGraph.addAll(itr.next().toAbstractLineInfo());
        }
        
        // Build the genreal tree out of the TransformGraph.
        return tGraph.buildTrees();
    }
    
    /** This will delete the selected lines. This could result in a break up of graphs. 
     * There are two possible cases here: <br>
     * 1. The selected lines in the DrawingGraph can safely be deleted, which in this case the lines
     *  would simply be deleted from the DrawingGraph.<br>
     * 2. The selected lines in the DrawingGraph can not be safely deleted (it would no longer be a graph in graph theory). 
     * In this case the DrawingGraph is simply deleted from the graph pool and then replaced with multiple DrawingGraphs
     * of the unselected lines only. 
     *  @return an undo for this operation, 
     */
    public InterfaceUndoItem deleteSelectedLines() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        LinkedList<DrawingGraph> ltDeleteOk = new LinkedList<DrawingGraph>();
        LinkedList<DrawingGraph> ltDivide = new LinkedList<DrawingGraph>();
        
        // First step, check to see which DrawingGraphs can be safely deleted from and which ones need to be divided.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph dGraph = itr.next();
            if (dGraph.hasSelectedItems(true) == false)
                continue;
            if (dGraph.isDeletingSelectSafe())
                ltDeleteOk.add(dGraph);
            else
                ltDivide.add(dGraph);
        }
        
        // Second step, perform the deleting on the DrawingGraph safely.
        for (Iterator<DrawingGraph> itr = ltDeleteOk.iterator(); itr.hasNext(); ) {
            DrawingGraph dGraph = itr.next();
            undoComplex.add(dGraph.deleteSelectedLines());
            
            // Remove empty graphs.
            if (dGraph.getLineCount() == 0)
                undoComplex.add(gPool.remove(dGraph));
        }
        
        // Third step, remove the DrawingGraph from this pool and replace it with mutliple DrawingGraph of the unselected
        // lines from the deleted DrawingGraph.
        for (Iterator<DrawingGraph> itr = ltDivide.iterator(); itr.hasNext(); ) {
            DrawingGraph dGraph = itr.next();
            
            // Remove the DrawingGraph.
            undoComplex.add(gPool.remove(dGraph));
            
            // Create DrawingGraphs out of the unselected lines until no more can be made.
            for (Iterator<DrawingGraph> itrUG = dGraph.toListUnselected().iterator(); itrUG.hasNext(); ) {
                undoComplex.add(gPool.add(itrUG.next()));
            }
            
            // Restore the flags of the lines to default.
            dGraph.deselectAll();
        }
        
        return undoComplex;
    }
    
    /** This will filter the point. If it is close to a vertex than it will change the point to that vertex position.
     * If the snap-to-grid is turned on then it will snap the point to the grid. This will filter on the current layer only.
     * @param fpt is the point to be filtered. This will modify the point if need be.
     */
    public void filterPoint(FPointType fpt) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )
            itr.next().filterPoint(fpt);
    }
    
    /** @return the number of lines in this design.
     */
    public int getLineCount() {
        int count = 0;
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )
            count += itr.next().getLineCount();
        
        return count;
    }
    
    /** @return the number of vertices in this design.
     */
    public int getVertexCount() {
        int count = 0;
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )
            count += itr.next().getVertexCount();
        
        return count;
    }
    
    /** This will perform a merge on the DrawingLayer. The passed in list is a list of DrawingGraph that have
     * been updated. This function will look only at this list to see if they need to be merged with any other graph
     * in the graph pool.
     * @param ltGraphs is a list of DrawingGraph that have been updated.
     * @return an undo item for this operation.
     */
    private InterfaceUndoItem merge(LinkedList<DrawingGraph> ltGraphs) {
        UndoItemComplex undoComplex = new UndoItemComplex();        
        
        // Now attempt to merge the DrawingGraphs together.
        for (Iterator<DrawingGraph> itrUpdated = ltGraphs.iterator(); itrUpdated.hasNext(); ) {
            DrawingGraph dgUpdated = itrUpdated.next();
            
            // Incase it gets deleted, do not process it.
            if (gPool.exists(dgUpdated) == false)
                continue;
            
            // Keep searching until no more merging.
            boolean bMerged = false;
            do {
                bMerged = false;

                // See if the updated graph connects to any other Graph.
                for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
                    DrawingGraph dg = itr.next();

                    if (dgUpdated == dg)
                        continue;

                    // Updated graph connects to another Graph.
                    if (dgUpdated.contains(dg)) {
                        bMerged = true;
                        undoComplex.add(gPool.remove(dg));
                        undoComplex.add(dgUpdated.add(dg));                        
                        break;
                    }
                }
            } while (bMerged);
        }        
        
        return undoComplex;
    }
        
    /** This will set all lines and vertices to selected.
     * @param selected is the value to set the lines and vertices to.
     */
    public void setLinesAndVerticesSelected(boolean selected) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            itr.next().setSelect(selected);
        }
    }
    
    /** This will set all lines and vertices to visible.
     * @param visible is the value to set the lines and vertices to.
     */
    public void setLinesAndVerticesVisible(boolean visible) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) 
            itr.next().setLinesAndVerticesVisible(visible);       
    }
    
    /** This will create a list of all the DrawingGraphs in this Layer.
     *  @return a list of DrawingGraph from all the layers in this pool.
     */
    public LinkedList<DrawingGraph> toGraphs() {
        LinkedList<DrawingGraph> ltGraphs = new LinkedList<DrawingGraph>();
        
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph graph = itr.next();
            ltGraphs.add(graph);
        }
        
        return ltGraphs;
    }    
    
    /** This will validate the line by update its drawing line structure.
     */
    void validateLines() {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); )
            itr.next().validateLines();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" General Path Methods ">
    
    /** This will create a GeneralPath from the position fptMouseDown.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @return null or a GeneralPath containing the position fptMouseDown.
     */
    public GeneralPath createGeneralPath(FPointType fptMouseDown) {
        return gPool.createGeneralPath(fptMouseDown);
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">

    /** This will get the line information. It will search for the line within the fBounds.
     * @param fBounds is the rectangle to search for the line.
     */
    public String debugGetInfo(Rectangle2D.Float fBounds) {
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = itr.next();
            String s = g.debugGetInfo(fBounds);
            if (s != null)
                return s;
        }
        
        return null;
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
        return zDepth;
    }
    
    public void setZDepth(int zDepth) {
        this.zDepth = zDepth;
    }

    public int compareTo(Object o) {
        return ((DrawingLayer) o).zDepth - zDepth;
    }

    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" User Settings Methods ">
    
    /** This will set the master color.
     * @param c is the new master color.
     */
    static public void setMasterColor(Color c) {
        masterColor = c;
    }
    
    /** @return the master color.
     */
    static public Color getMasterColor() {
        return masterColor;
    }
    
    /** This is the default color of the master color.
     */
    static public Color getDefaultMasterColor() {
        return Color.BLUE;
    }
    
    /** @return the layer settings.
     */
    static public InterfaceSettings getLayerSettings() {
        return new LayerSettings();
    }
    
    /** This will restore the factory settings.
     */
    static public void restoreFactorSettings() {
        setMasterColor(getDefaultMasterColor());
        getLayerSettings().save();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Finger Game Level Support ">
    
    /** This will get the center points from each graph in the layer.
     * @return a list of center points from each graph in the layer.
     */
    public LinkedList<FPointType> finger_getCenterPoints() {
        LinkedList<FPointType> list = new LinkedList<FPointType>();
        
        // For each drawing graph, at the center is a point that is the starting position.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph graph = itr.next();
            Rectangle2D.Float rect = graph.getBounds2D();
            
            // Create the center point.
            FPointType pt = new FPointType();
            pt.x = (float)rect.getX() + (float)rect.getWidth() / 2.0f;
            pt.y = (float)rect.getY() + (float)rect.getHeight() / 2.0f;
            list.add(pt);
        }
        
        return list;
    }
            
    /** This will create a list of lists of paths
     * @return a list of paths.
     */
    public LinkedList<LinkedList<FPointType>> finger_getPaths() {
        LinkedList<LinkedList<FPointType>> list = new LinkedList<LinkedList<FPointType>>();
        
        
        // For each drawing graph, at the center is a point that is the starting position.
        for (Iterator<DrawingGraph> itr = gPool.values().iterator(); itr.hasNext(); ) {
            DrawingGraph graph = itr.next();
            list.add(graph.finger_getPath());
        }
                
        return list;
    }    
    
    // </editor-fold>
    
}
 
// <editor-fold defaultstate="collapsed" desc=" Class LayerSettings ">

/** This will save the vertex settings
 */
class LayerSettings implements InterfaceSettings {
    static private String MASTER_COLOR = "LayerSettings_MasterColor";  // String

    public LayerSettings() {  
    }
    
    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        ColorSave.saveColor(prefs, MASTER_COLOR, DrawingLayer.getMasterColor());
    }
    
    public void load() {
        Preferences prefs = Preferences.userNodeForPackage(mlnr.embd.Version.getVersion());
        DrawingLayer.setMasterColor(ColorSave.loadColor(prefs, MASTER_COLOR, DrawingLayer.getDefaultMasterColor()));
    }      
}

// </editor-fold>
