/*
 * Design.java
 *
 * Created on July 24, 2006, 1:36 PM
 *
 */

package mlnr.draw;

import mlnr.draw.area.FillGraphSystem;
import java.awt.geom.*;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import org.w3c.dom.*;
import mlnr.gui.InterfaceFrameOperation;
import mlnr.type.*;
import mlnr.util.*;

/** This is the class used to contain a design. It is pretty much a static container where items are added/deleted. Lines 
 *  and vertices are added/deleted. The Vertices will not be moved. This class is only used for the actual drawing you 
 * see on the screen. For transformations see the TransformDesign.
 * @author Robert Molnar II
 */
public class DrawingDesign {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** This is turned on by the debug menu, if turned on then it will print line information. */
    static boolean _DEBUG_PRINT_LINES = false;
    
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the system used to fill in the graph. When null, not in use (Vector mode), else it is in use (Fill graph mode). */
    FillGraphSystem fillGraphSystem = null;
    
    /** The size of the design in measurements. */
    float designWidth;
    
    /** The size of the design in measurements. */
    float designHeight;
    
    /** This is the pool that contains all layers.*/
    DrawingLayerPool lPool = null;
    
    /** This is the undo system that is used to do undos for this Design for the vector stage. */
    UndoSystemDesign undoSystem = new UndoSystemDesign();
    
    /** This is the undo system that is used to do undos for color filling stage. */
    UndoSystemColorFill undoColorFillSystem = new UndoSystemColorFill();
    
    /** This is the interface used to operation the main frame. */
    InterfaceFrameOperation iFrameOperator;

    /** This is the point which was used last for calculating the point to rotate, resize, mirror around. */
    FPointType fptCenter = new FPointType();
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    /** Creates a new instance of Design  
     * @param createMaster is true if it should create a Master layer.
     */
    public DrawingDesign(InterfaceFrameOperation iFrameOperator, boolean createMaster) {
        this.iFrameOperator = iFrameOperator;
        lPool = new DrawingLayerPool(createMaster);
    }
    
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Support ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eRoot is the element for the rxml in the RXML file.
     * @param iFrameOperator is the interface used to operation the main frame.
     */
    public static DrawingDesign loadVersion10(Element eRoot, InterfaceFrameOperation iFrameOperator) throws Exception {
        DrawingDesign design = new DrawingDesign(iFrameOperator, false);
        
        // Get the design size.
        Element eCanvas = XmlUtil.getElementByTagName(eRoot, "canvas");        
        design.designWidth = (float)XmlUtil.getAttributeInteger(eCanvas, "x") / 20.0f;
        design.designHeight = (float)XmlUtil.getAttributeInteger(eCanvas, "y") / 20.0f;        
        design.lPool.loadVersion10(eRoot);
        return design;
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eDesign is the element for the design in the RXML file.
     * @param iFrameOperator is the interface used to operation the main frame.
     */
    public static DrawingDesign loadVersion20(Element eDesign, InterfaceFrameOperation iFrameOperator) throws Exception {
        DrawingDesign design = new DrawingDesign(iFrameOperator, false);
        design.designWidth = (float)XmlUtil.getAttributeDouble(eDesign, "width");
        design.designHeight = (float)XmlUtil.getAttributeDouble(eDesign, "height");
        design.lPool.loadVersion20(XmlUtil.getElementByTagName(eDesign, "layerPool"));
        return design;
    }

    /** This will write out the Design in xml file format.
     * @param lInfo is the layer to write out, or null if the entire design is to be written out.
     */
    public void write(LayerInfo lInfo, PrintWriter out) {
        out.println("  <design width='" + designWidth + "' height='" + designHeight + "'>");
        lPool.write(lInfo, out);
        out.println("  </design>");
        
        // Design is saved if saving the entire design. When saving a layer you can think of it as exporting from the design.
        if (lInfo == null)
            iFrameOperator.notifyDocumentChanged(false);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Design Methods ">
    
    /**
     * @param layerSave is the layer to save or if null than all.
     * @return list of GeneralTrees of this Layer.
     */
    public LinkedList<GeneralTree> buildGeneralTrees(LayerInfo layerSave) {
        // Build only using one layer.
        if (layerSave != null) {
            DrawingLayer l = lPool.getLayer(layerSave.getId());
            return l.buildGeneralTrees();
        }
        
        // Build using all the layers.
        LinkedList<GeneralTree> ltGeneralTree = new LinkedList<GeneralTree>();
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            LinkedList<GeneralTree> ltLayerGeneral = itr.next().buildGeneralTrees();
            ltGeneralTree.addAll(ltLayerGeneral);
        }
        
        return ltGeneralTree;
    }
    
    /** Call this when the design becomes useable.
     */
    public void enableGUIForDesign() {
        iFrameOperator.enableUndoable(undoSystem.isUndoPossible());
        iFrameOperator.enableRedoable(undoSystem.isRedoPossible());
    }
    
    /** @return The bounds of the design by using the measurements of the LayerPool (the bounds of all lines, beziers, and curves). Can be null.
     */
    public Rectangle2D.Float getBounds2D() {
        if (lPool.getLayerCount() == 0)
            return null;
        
        // Grow the rectangle.
        Rectangle2D.Float fRectangle = null;
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer tLayer = itr.next();
            
            // Get the bounds of the layer.
            Rectangle2D.Float fRect = tLayer.getBounds2D();
            if (fRect == null)
                continue;
            
            // Use the first one as the initial size.
            if (fRectangle == null) {
                fRectangle = tLayer.getBounds2D();
                continue;
            }
            
            fRectangle.add(fRect);
        }
        
        return fRectangle;
    }
    
    /** This will get the height of the design. The size of the drawing pad.
     * @return height of the design (drawing pad size).
     */
    public float getHeight() {
        return designHeight;
    }
    
    /** @return the number of layers in this design.
     */
    public int getLayerCount() {        
        return lPool.getLayerCount();
    }
    
    /** @return the number of lines in this design.
     */
    public int getLineCount() {
        int count = 0;
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
            count += itr.next().getLineCount();
        
        return count;
    }
    
    /** @return the number of vertices in this design.
     */
    public int getVertexCount() {
        int count = 0;
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
            count += itr.next().getVertexCount();
        
        return count;
    }
    
    /** This will get the width of the design. The size of the drawing pad.
     * @return width of the design (drawing pad size).
     */
    public float getWidth() {
        return designWidth;
    }
    
    /** This will resize the design.
     * @param fSize is the new size of the design.
     */
    public void resizeDesign(FPointType fSize) {
        designWidth = fSize.x;
        designHeight = fSize.y;
    }
    
    /** This will set the frame operator.
     * @param iFrameOperator is the new frame operator.
     */
    void setFrameOperator(InterfaceFrameOperation iFrameOperator) {
        this.iFrameOperator = iFrameOperator;
    }

    public String toString() {
        return "{Design designWidth[" + designWidth + "] designHeight[" + designHeight + "] \n LayerPool[" + lPool + "]}";
    }
    
    // </editor-fold>       
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
        
    /** This will draw lines.
     * @param g2d is the graphics class.
     * @param erase is true if the selected lines will be erased.
     */
    public void draw(Graphics2D g2d) {
        Color cOld = g2d.getColor();
        
        // Draw in the Areas for filling (Color mode only).
        if (fillGraphSystem != null)
            fillGraphSystem.draw(g2d);

        DrawingLayer currLayer = lPool.getCurrentLayer();
        
        // Draw the layers, but not the current one.
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer layer = itr.next();
            if (layer != currLayer)
                layer.draw(g2d);
        }
        
        currLayer.draw(g2d);       
        
        g2d.setColor(cOld);
    }
    
    /** This is used to draw into a bitmap.
     * @param g2D is the graphics class.
     * @param lInfo is the layer to write out, or null if the entire design is to be written out.
     * @param changeColor is true if it should change color for the layers.
     * @param fillColorOnly is true if only the fill should be drawn.
     */
    public void drawAllBitmap(Graphics2D g2d, LayerInfo lInfo, boolean changeColor, boolean fillColorOnly) {
        if (fillGraphSystem != null)
            fillGraphSystem.drawAllBitmap(g2d, lInfo, changeColor);
        
        // Only need to draw with the fill-in.
        if (fillColorOnly)
            return;
                
        Color cOld = g2d.getColor();
        
        // Draw only one layer or all of them.
        if (lInfo != null) 
            lPool.getLayer(lInfo.getId()).drawBitmap(g2d, changeColor);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
                itr.next().drawBitmap(g2d, changeColor);
        }
        
        g2d.setColor(cOld);
    }
    
    /** @return a list of Colors used in this design. This will include colors from the general path pool if it exists.
     */
    public LinkedList getColors() {
        LinkedList<Color> ltColors = lPool.getColors();
        if (fillGraphSystem != null)
            ltColors.addAll(fillGraphSystem.getColors());
        return ltColors;
    }

    /** This will validate the line by update its drawing line structure.
     */
    private void validateLines() {
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
            itr.next().validateLines();
    }
    
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" Transform{Layer|Design|Graph} Methods/ Selected Item Methods ">

    /** This will add the graph to this Design. It will be added to the current layer.
     * @param g is the graph to be added to this DrawingDesign.
     */
    public void add(TransformGraph g) {
        undoSystem.add(lPool.getCurrentLayer().add(g));
    }
    
    /** This will add the TransformDesign into this DrawingDesign (disregards the layer point to and
     * line point to in the TransformDesign). If only one layer in the TransformDesign then add it to the
     * current layer or if more than two layers then add as new layers.
     * @param design is the TransformDesign to be added into this DrawingDesign.
     * @param forceLoadIntoCurrentLayer is true then it should always load into the current layer.
     * 
     */
    public void add(TransformDesign design, boolean forceLoadIntoCurrentLayer) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Since there is only one layer in the transform design therefore place it in the current design.
        if (design.getLayerCount() == 1)
            forceLoadIntoCurrentLayer = true;
        
        // Add each TransformLayer as a new layer or into the current layer.
        for (Iterator<TransformLayer> itr = design.toList().iterator(); itr.hasNext(); ) {
            TransformLayer tLayer = itr.next();
            
            if (forceLoadIntoCurrentLayer) {
                undoComplex.add(lPool.getCurrentLayer().addAll(tLayer.toList()));
                
            } else if (tLayer.getLayerPointTo() != TransformLayer.LAYERTO_NOPOINT) {
                // Layer might not exist therefore create a new one.
                if (lPool.exists(tLayer.getLayerPointTo())== false)
                    undoComplex.add(lPool.addLayer(new DrawingLayer(tLayer)));
                else
                    undoComplex.add(lPool.getLayer(tLayer.getLayerPointTo()).addAll(tLayer.toList()));
                
            } else
                undoComplex.add(lPool.addLayer(new DrawingLayer(tLayer)));
        }
        
        // Add the operation to the undo system.
        undoSystem.add(undoComplex);        
        
        // Make sure the layer panel is updated.
        iFrameOperator.validateLayerPanel();
    }

    /** This will finalize an add point operation. It will must perform special operations that addSelect() cannot perform. 
     * This is due to the nature of the add point operation where the graph could possibly be broken into two graphs.
     * @param tDesign is the design with the selected items from one layer that need to be merged back into the corresponding layer.
     */
    public void addPointFinalize(TransformDesign tDesign) {
        if (tDesign.getLayerCount() != 1)
            throw new IllegalArgumentException("TransformDesign must only contain one layer. Count: " + tDesign.getLayerCount());
        TransformLayer tLayer = tDesign.toList().getFirst();
        
        if (tLayer.getLayerPointTo() == TransformLayer.LAYERTO_NOPOINT)
            throw new IllegalArgumentException("Layer must have layer-point-to id but didn't.");
        
        // Perform the add point finalize.
        DrawingLayer dLayer = lPool.getLayer(tLayer.getLayerPointTo());
        undoSystem.add(dLayer.addPointFinalize(tLayer));
    }
    
    /** This will merge the 'tDesign' which has all it's layers pointing to layers in this DrawingDesign.
     * @param tDesign is the TransformDesign to be merged into this DrawingDesign.
     * @throw IllegalArgumentException for layers that do not exist or if a layer does not have a layer-point to.
     */
    public void addSelect(TransformDesign tDesign) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Merge each TransformLayer.
        for (Iterator<TransformLayer> itr = tDesign.toList().iterator(); itr.hasNext(); ) {
            TransformLayer tLayer = itr.next();
            
            if (tLayer.getLayerPointTo() == TransformLayer.LAYERTO_NOPOINT)
                throw new IllegalArgumentException("Layer must have layer-point-to id but didn't.");
            else
                undoComplex.add(lPool.getLayer(tLayer.getLayerPointTo()).mergeSelected(tLayer));
        }
        
        undoSystem.add(undoComplex);
    }    
    
    /** This will copy the selected items leaving this DrawingDesign unchanged. Only the lines which are currently selected will be copied.
     * None of the items in the TransformDesign will point back to any from this DrawingDesign.
     * @return a TransformDesign of the selected items and can be empty if the user has nothing selected.
     */
    public TransformDesign copySelectedItems() {
        return new TransformDesign(lPool.copySelectedItems());
    }
    
    /** This will delete the selected lines. If the graph becomes fragmented then the graph will be broken up into muliple graphs.
     * @param r is the rectangle to find the items to delete.
     * @param useCurrentLayer is true if it should use the current layer or all visible layers.
     */
    public void deleteSelectedLines(boolean useCurrentLayer) {
        // Only delete from the current layer if useCurrentLayer is true.
        if (useCurrentLayer)
            undoSystem.add(lPool.getCurrentLayer().deleteSelectedLines());
        
        // Attempt to delete from all the other layers.
        UndoItemComplex undoComplex = new UndoItemComplex();
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer dLayer = itr.next();
            if (dLayer.isVisible() == false)
                continue;
            
            undoComplex.add(dLayer.deleteSelectedLines());
        }        
        undoSystem.add(undoComplex);
    }
    
    /** This will deselect all and will set all lines and vertices as visible.
     */
    public void deselectAll() {
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            itr.next().deselectAll();
        }
    }
    
    /** This will deselect Graphs from the selected items in the Design.
     * @param r is the rectangle to deselect items from this Design.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void deselectGraphs(Rectangle2D.Float r, boolean currLayerOnly) {
        if (currLayerOnly) {
            lPool.getCurrentLayer().deselectGraphs(r);
            return;
        }
        
        // Run through all layers to deselect from.
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer dLayer = itr.next();
            if (dLayer.isVisible() == false)
                continue;
            
            dLayer.deselectGraphs(r);
        }
    }
    
    /** This will deselect Graphs from the selected items in the Design.
     * @param r is the rectangle to deselect items from this Design.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void deselectLines(Rectangle2D.Float r, boolean currLayerOnly) {
        if (currLayerOnly) {
            lPool.getCurrentLayer().deselectLines(r);
            return;
        }
        
        // Run through all layers to deselect from.
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer dLayer = itr.next();
            if (dLayer.isVisible() == false)
                continue;
            
            dLayer.deselectLines(r);
        }
    }
    
    /** This will deselect Graphs from the selected items in the Design.
     * @param r is the rectangle to deselect items from this Design.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void deselectVertices(Rectangle2D.Float r, boolean currLayerOnly) {
        if (currLayerOnly) {
            lPool.getCurrentLayer().deselectVertices(r);
            return;
        }
        
        // Run through all layers to deselect from.
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            DrawingLayer dLayer = itr.next();
            if (dLayer.isVisible() == false)
                continue;
            
            dLayer.deselectVertices(r);
        }
    }
    
    /** This will filter the point. If it is close to a vertex than it will change the point to that vertex position.
     * If the snap-to-grid is turned on then it will snap the point to the grid. This will filter on the current layer only.
     * @param fpt is the point to be filtered. This will modify the point if need be.
     * @param currLayerOnly is true if it should use only the current layer to filter the point.
     */
    public void filterPoint(FPointType fpt, boolean currLayerOnly) {
        if (currLayerOnly)
            lPool.getCurrentLayer().filterPoint(fpt);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();
                if (dLayer.isVisible() == false)
                    continue;
                
                dLayer.filterPoint(fpt);
            }
        }
    }
    
    /** See DrawingLinePool.getSelectedItems() for more details.
     * @return a TransformDesign of the selected items. Can be empty.
     */
    public TransformDesign getSelectedItems() {
        return new TransformDesign(lPool.getSelectedItems());
    }
    
    /**  @param fptMousePos is the position used to see if the point is within a vertex proximity.
     *  @param currLayerOnly is true if to only use the current layer.
     * @return true if the point is within a vertex proximity.
     */
    public boolean isPointWithinVertex(FPointType fptMousePos, boolean currLayerOnly) {
        if (currLayerOnly)
            return lPool.getCurrentLayer().isPointWithinVertex(fptMousePos);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();
                if (dLayer.isVisible() == false)
                    continue;
                
                boolean hit = dLayer.isPointWithinVertex(fptMousePos);
                if (hit)
                    return true;
            }
        }
        
        return false;
    }
    
    /** @return true if there are any selected items currently selected.
     */
    public boolean isSelectedItems() {
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            if (itr.next().isSelectedItems())
                return true;
        }
        
        return false;
    }
    
    /** This will select one Bezier curve. Use getSelectedItems() function to get the TransformDesign.
     * @param r is the rectangle to select the Bezier curve.
     * @param currLayerOnly is true if it should select from the current layer only.
     * @return true if a bezier curve was selected.
     */
    public boolean selectBezier(Rectangle2D.Float r, boolean currLayerOnly) {
        if (currLayerOnly)
            return lPool.getCurrentLayer().selectBezier(r);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();
                if (dLayer.isVisible() && dLayer.selectBezier(r))
                    return true;
            }
        }
        
        return false;
    }
        
    /** This will select lines from this graph. Use getSelectedItems() function to get the TransformDesign.
     * @param r is the rectangle to select the lines.
     * @param oneLine is true if only one line is to be selected.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void selectLines(Rectangle2D.Float r, boolean oneLine, boolean currLayerOnly) {
        if (currLayerOnly)
            lPool.getCurrentLayer().selectLines(r, oneLine);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();
                
                if (dLayer.isVisible() && dLayer.selectLines(r, oneLine) && oneLine)
                    break;
            }
        }
    }
    
    /** This will set all lines and vertices to selected.
     * @param selected is the value to set the lines and vertices to.
     */
    public void setLinesAndVerticesSelected(boolean selected) {
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
            itr.next().setLinesAndVerticesSelected(selected);       
    }
    
    /** This will set all lines and vertices to visible.
     * @param visible is the value to set the lines and vertices to.
     */
    public void setLinesAndVerticesVisible(boolean visible) {
        for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); )
            itr.next().setLinesAndVerticesVisible(visible);       
    }
    
    /** This will select vertices from this design. Use getSelectedItems() function to get the TransformDesign.
     * @param r is the rectangle to select the vertices.
     * @param oneVertex is true if only one vertex is to be selected.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void selectVertices(Rectangle2D.Float r, boolean oneVertex, boolean currLayerOnly) {
        if (currLayerOnly)
            lPool.getCurrentLayer().selectVertices(r, oneVertex);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();                
                if (dLayer.isVisible() && dLayer.selectVertices(r, oneVertex) && oneVertex)
                    break;
            }
        }
    }
    
    /** This will select Graphs from this design. Use getSelectedItems() function to get the TransformDesign.
     * @param r is the rectangle to select the graphs.
     * @param oneGraph is true if only one graph is to be selected.
     * @param currLayerOnly is true if it should select from the current layer only.
     */
    public void selectGraphs(Rectangle2D.Float r, boolean oneGraph, boolean currLayerOnly) {
        if (currLayerOnly)
            lPool.getCurrentLayer().selectGraphs(r, oneGraph);
        else {
            for (Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
                DrawingLayer dLayer = itr.next();
                
                if (dLayer.isVisible() && dLayer.selectGraphs(r, oneGraph) && oneGraph)
                    break;
            }
        }
    }
       
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc=" General Path Methods ">
    
    /** This will fill in a closed area of the layer.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @param c is the color to paint the GeneralPath.
     *  @throws IllegalStateException not color mode.
     */
    public void fill(FPointType fptMousePosition, Color c) {
        if (fillGraphSystem == null)
            throw new IllegalArgumentException("Not in color mode.");
        undoColorFillSystem.add(fillGraphSystem.fill(fptMousePosition, c));        
    }
    
    /** This will get a Area's color (Color Mode only).
     * @param fptMouseDown is the mouse position used to get the color.
     * @return the color of the Area if user clicked on it, else null no color.
     *  @throws IllegalStateException not color mode.
     */
    public Color getAreaColor(FPointType fptMousePosition) {
        if (fillGraphSystem == null)
            throw new IllegalArgumentException("Not in color mode.");
        return fillGraphSystem.getColor(fptMousePosition);
    }
    
    /** This will set the state to color fill-in.
     */
    public void setStateToColorFill() {
         fillGraphSystem = new FillGraphSystem(lPool.toGraphs());
    }
    
    /** This will set the state to vector drawing only.
     */
    public void setStateToVector() {
        fillGraphSystem = null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Layer Methods ">
    
    /** This will get the bounds of the layer (done by using the bounds of the lines).
     * @param lInfo is the layer to get the bounds on.
     * @return the bounds of the layer.
     */    
    public Rectangle2D.Float getLayerBounds2D(LayerInfo lInfo) {
        DrawingLayer layer = lPool.getLayer(lInfo.getId());
        return layer.getBounds2D();
    }

    
    /** @return the current layer.
     */
    public LayerInfo getCurrentLayer() {
        return lPool.getCurrentLayer().getInfo();
    }
    
    /** This will get all the layer infos where the first index is the lowest zDepth, it
     * is sorted by the zDepth of each layer.
     * @return array of LayerInfo sorted by zDepth.
     */
    public LayerInfo[] getLayerInfos() {
        return lPool.getLayerInfos();
    }
    
    /** This will get the current layer that is selected.
     * @return current layer selected information.
     */
    public LayerInfo getSelectedLayerInfo() {
        return lPool.getCurrentLayer().getInfo();
    }
    
    /** This will delete all the layers and recreate the master one.
     */
    public void deleteAllLayers() {
        undoSystem.add(lPool.deleteAllLayers());
    }
    
    /** This will turn all the layers visability to on or off.
     * @param visability is true then all layers visability is turned on, else false then all layers visability is turned off.
     */
    public void setAllLayersVisability(boolean visability) {
        for(Iterator<DrawingLayer> itr = lPool.values().iterator(); itr.hasNext(); ) {
            itr.next().setVisability(visability);
        }
    }
    
    /** This will add the layer to the design and will set it as the current layer.
     * @param lInfo is the layer specs to be created with.
     */
    public void addLayer(LayerInfo lInfo) {
        // Add the layer.
        InterfaceUndoItem iUndo = lPool.addLayer(lInfo);
        undoSystem.add(iUndo);
        
        // Now valdiate the layer panel.
        iFrameOperator.validateLayerPanel();
    }

    
    public void deleteLayer(LayerInfo lInfo) {
        undoSystem.add(lPool.deleteLayer(lInfo));
    }
    
    public void updateLayer(LayerInfo lInfo) {
        lPool.updateLayer(lInfo);
    }
    
    /** This will merge the source layer to the destination layer. 
     * <br> Warning this will modify the selected values of the lines. Once it is finished all lines will be deselected.
     * @param lSource is the source layer that will be merged into the destination.
     * @param lDestination is the destination layer that will have the source merged into it.
     */
    public void mergeLayers(LayerInfo lSource, LayerInfo lDestination) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        DrawingLayer layerSource = lPool.getLayer(lSource.getId());
        DrawingLayer layerDestination = lPool.getLayer(lDestination.getId());
        
        // Need to change the current layer.
        if (layerSource == lPool.getCurrentLayer())
            lPool.selectLayer(lDestination);
        
        // Now create a TransformLayer out of the DrawingLayer.
        layerSource.setLinesAndVerticesSelected(true);
        TransformLayer tLayer = layerSource.getSelectedItems();
        layerSource.setLinesAndVerticesSelected(false);
        layerSource.setLinesAndVerticesVisible(true);
        
        // Add all the TransformGraphs into the destination.        
        undoComplex.add(layerDestination.addAll(tLayer.toList()));
        
        // Now remove the source layer.
        undoComplex.add(lPool.deleteLayer(lSource));
        
        undoSystem.add(undoComplex);
    }
    
    /** This will merge all layers together into one layer.
     * <br> Warning this will modify the selected values of the lines. Once it is finished all lines will be deselected.
     * @param lDestinate is the layer which all the others will be merged to.
     */
    public void mergeAll(LayerInfo lDestination) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        DrawingLayer layerDestination = lPool.getLayer(lDestination.getId());
        
        // Add each other layer into the destination.
        for (Iterator<DrawingLayer> itr = lPool.toList().iterator(); itr.hasNext(); ) {
            DrawingLayer layer = itr.next();
            
            if (layer == layerDestination)
                continue;
            
            // Create a TransformLayer out of the DrawingLayer.
            layer.setLinesAndVerticesSelected(true);
            TransformLayer transformLayer = layer.getSelectedItems();
            layer.setLinesAndVerticesSelected(false);
            layer.setLinesAndVerticesVisible(true);
            
            // Add all the TransformGraphs into the destination.
            undoComplex.add(layerDestination.addAll(transformLayer.toList()));
            
            // Now remove the layer.
            undoComplex.add(lPool.deleteLayer(layer.getInfo()));
        }
        
        // Save the undo operation.
        undoSystem.add(undoComplex);
    }
    
    /** This will select the layer lSelect.
     * @param lSelect is the new layer to be selected.
     */
    public void selectLayer(LayerInfo lSelect) {
        lPool.selectLayer(lSelect);
    }
    
    /** This will select the first visible layer.
     */
    public void selectVisibleLayer() {
        lPool.selectVisibleLayer();
    }
    
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Methods ">
    
    /** This will clear the undo and redo lists.
     */
    public void clearUndo() {
        undoSystem.clear();
    }
    
    /** This should be called to create an undo with all undo items that are between the last undo
     * and the undo marker. addUndoMarker() must be called before this, that will create an undo marker.
     */
    public void completeUndo() {
            undoSystem.createUndoFromMarker();
            
            // Design is changed.
            iFrameOperator.notifyDocumentChanged(true);
    }
    
    /** This will place an undo marker in the undo list. The completeUndo() function must be called after all the
     * undos that need to be grouped together outside this class are done.
     */
    public void addUndoMarker() {
        undoSystem.add(new UndoItemMarker());
    }
    
    /** @return true if there is an undo marker in the undo list, else false there is none.
     */
    public boolean isUndoMarker() {
        return undoSystem.isUndoMarker();
    }
    
    /** @return true if there are more undos.
     */
    public boolean isUndoPossible() {
        if (iFrameOperator.getStage() == InterfaceFrameOperation.GUISTAGE_VECTOR)
            return undoSystem.isUndoPossible();
        else
            return undoColorFillSystem.isUndoPossible();
    }
    
    /** This will undo one operation.
     */
    public void undo() {
        if (iFrameOperator.getStage() == InterfaceFrameOperation.GUISTAGE_VECTOR) {
            undoSystem.undo();
            iFrameOperator.enableUndoable(undoSystem.isUndoPossible());
            iFrameOperator.validateLayerPanel();
            validateLines();
        } else {
            undoColorFillSystem.undo();
            iFrameOperator.enableUndoable(undoColorFillSystem.isUndoPossible());
        }
    }
    
    /** This will undo one operation and not create a redo for the operation, thus this undo will
     * be lost.
     */
    public void undoNoRedo() {
        undoSystem.undoNoRedo();
        iFrameOperator.enableUndoable(undoSystem.isUndoPossible());
        iFrameOperator.validateLayerPanel();
        validateLines();
    }
    
    /** This will redo one operation.
     */
    public void redo() {
        if (iFrameOperator.getStage() == InterfaceFrameOperation.GUISTAGE_VECTOR) {
            undoSystem.redo();
            iFrameOperator.enableRedoable(undoSystem.isRedoPossible());
            iFrameOperator.validateLayerPanel();
            validateLines();
        } else {
            undoColorFillSystem.redo();
            iFrameOperator.enableRedoable(undoColorFillSystem.isRedoPossible());            
        }
    }
    
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will toggle the printing out the line information on the drawing pad.
     */
    public static void debugTogglePrintLines() {
        _DEBUG_PRINT_LINES = !_DEBUG_PRINT_LINES;
    }

    /** This will get the line information. It will search for the line within the fBounds.
     * @param fBounds is the rectangle to search for the line.
     * @param currLayerOnly is true if it should only search in the current layer.
     */
    public String debugGetInfo(Rectangle2D.Float fBounds, boolean currLayerOnly) {
        return lPool.debugGetInfo(fBounds, currLayerOnly);
    }

    /** This will print the undo/redo list.
     */
    public void debugPrintUndo() {
        undoSystem.print();
    }
    
    /** This will print the undo/redo list log.
     */
    public void debugPrintUndoLog() {
        undoSystem.printLog();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Class UndoSystemDesign ">
    
    class UndoSystemDesign extends UndoSystem {
        public void add(InterfaceUndoItem iUndoItem) {
            super.add(iUndoItem);
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
            
            // Design is changed.
            iFrameOperator.notifyDocumentChanged(true);
        }
        
        /** This will undo the operation with no redo adding it, thus it is lost.
         */
        public void undoNoRedo() {            
            InterfaceUndoItem iUndo = (InterfaceUndoItem)ltUndo.removeFirst();
            iUndo.undoItem();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }
        
        public void undo() {
            super.undo();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
            
            // Design is changed.
            iFrameOperator.notifyDocumentChanged(true);
        }

        public void redo() {
            super.redo();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
            
            // Design is changed.
            iFrameOperator.notifyDocumentChanged(true);
        }    
        
    }
                
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Class UndoSystemColorFill ">
    
    class UndoSystemColorFill extends UndoSystem {
        public void add(InterfaceUndoItem iUndoItem) {
            super.add(iUndoItem);
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }
        
        /** This will undo the operation with no redo adding it, thus it is lost.
         */
        public void undoNoRedo() {            
            InterfaceUndoItem iUndo = (InterfaceUndoItem)ltUndo.removeFirst();
            iUndo.undoItem();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }
        
        public void undo() {
            super.undo();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }

        public void redo() {
            super.redo();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }    
        
        public void clear() {
            super.clear();
            
            iFrameOperator.enableUndoable(isUndoPossible());
            iFrameOperator.enableRedoable(isRedoPossible());
        }
        
    }
                
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Finger Game Level Support ">
    
    /** This will get the starting points from the "Start" layer.
     * @return a list of starting points.
     */
    public LinkedList<FPointType> finger_getStartPoints() {
        LayerInfo[] layers = getLayerInfos();
        for (int i=0; i < layers.length; i++) {
            if (layers[i].getName().equals("Start"))
                return lPool.getLayer(layers[i].getId()).finger_getCenterPoints();
        }
        
        return new LinkedList();
    }
    
    /** This will get the starting points from the "Multiplier" layer.
     * @return a list of starting points.
     */
    public LinkedList<FPointType> finger_getMultipilerPoints() {
        LayerInfo[] layers = getLayerInfos();
        for (int i=0; i < layers.length; i++) {
            if (layers[i].getName().equals("Multiplier"))
                return lPool.getLayer(layers[i].getId()).finger_getCenterPoints();
        }
        
        return new LinkedList();
    }
        
    /** This will get the starting points from the "Multiplier" layer.
     * @return a list of starting points.
     */
    public LinkedList<LinkedList<FPointType>> finger_getPaths() {
        LayerInfo[] layers = getLayerInfos();
        for (int i=0; i < layers.length; i++) {
            if (layers[i].getName().equals("Path"))
                return lPool.getLayer(layers[i].getId()).finger_getPaths();
        }
        
        return new LinkedList();
    }
    
    // </editor-fold>
    
}

