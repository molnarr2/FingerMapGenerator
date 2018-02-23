/*
 * LayerPool.java
 *
 * Created on July 24, 2006, 1:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.*;
import java.awt.geom.*;
import java.io.PrintWriter;
import java.util.*;
import org.w3c.dom.*;
import mlnr.util.*;
import mlnr.type.*;

/**
 *
 * @author Robert Molnar II
 */
public class DrawingLayerPool extends AbstractPool {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the current layer being operated on. */
    DrawingLayer lCurr = null;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of LayerPool
     * @param defaultMaster is true if it should create a default layer.
     */
    public DrawingLayerPool(boolean defaultMaster) {
        
        // Add the master layer.
        if (defaultMaster) {
            DrawingLayer l = DrawingLayer.createDefaultLayer();
            super.add(l);
            lCurr = l;
        }
    }
    
    // </editor-fold>
            
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying Layers ">
    
    /** @returns a collection view of this AbstractLine of the underlying values in this pool.
     */
    final public Collection<DrawingLayer> values() {
        return super.values();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">
    
    /** This will load the version 1.0 of RXML file.
     * @param eRoot is the element for the rxml in the RXML file.
     */
    void loadVersion10(Element eRoot) throws Exception {
        NodeList nList = eRoot.getElementsByTagName("layer");
        
        // Load each layer in.
        int length = nList.getLength();        
        for (int i=0; i < length; i++) {
            DrawingLayer l = DrawingLayer.loadVersion10((Element)nList.item(i), getHighestZDepth() + 100);
            super.add(l);
            
            // First layer should be current.
            if (i == 0)
                lCurr = l;
        }
    }
    
    /** This will load the version 2.0 of RXML file.
     * @param eLayerPool is the element for the layerPool in the RXML file.
     */
    void loadVersion20(Element eLayerPool) throws Exception {
        int currId = XmlUtil.getAttributeInteger(eLayerPool, "currId");
        NodeList nList = eLayerPool.getElementsByTagName("layer");
        
        // Load each layer in.
        int length = nList.getLength();        
        for (int i=0; i < length; i++) {
            DrawingLayer l = DrawingLayer.loadVersion20((Element)nList.item(i));
            super.restore(l);
            
            // This is the current layer.
            if (l.getId() == currId)
                lCurr = l;
        }
    }

    /** This will write out each layer information.
     * @param lInfo is the layer to write out, or null if the entire design is to be written out.
     */
    void write(LayerInfo lInfo, PrintWriter out) {
        out.println("   <layerPool currId='" + lCurr.getId() + "'>");
        
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = (DrawingLayer)itr.next();
            
            // Write out the single layer.
            if (lInfo != null && l.getId() == lInfo.getId()) {
                l.write(out);
                break;
            } else if (lInfo == null)            
                l.write(out);
        }
        
        out.println("   </layerPool>");
    }
    
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Select Methods ">
    
    /** See DrawingLinePool.getSelectedItems() for more details.
     * @return a TransformLayerPool of the selected items. Can be empty.
     */
    public TransformLayerPool getSelectedItems() {
        TransformLayerPool tLayerPool = new TransformLayerPool();
        
        for (Iterator<DrawingLayer> itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = itr.next();
            if (l.isSelectedItems())
                tLayerPool.add(l.getSelectedItems());
        }
        
        return tLayerPool;
    }
    
    /** This will copy the selected items leaving this DrawingLayerPool unchanged. Only the lines which are currently selected will be copied.
     * @return a TransformLayerPool of the selected items and can be empty if the user has nothing selected.
     */
    public TransformLayerPool copySelectedItems() {
        TransformLayerPool tLayerPool = new TransformLayerPool();
        
        for (Iterator<DrawingLayer> itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = itr.next();
            if (l.isSelectedItems())
                tLayerPool.add(l.copySelectedItems());
        }
        
        return tLayerPool;
    }
        
    /** This will select the layer lSelect.
     * @param lSelect is the new layer to be selected.
     */
    public void selectLayer(LayerInfo lSelect) {
        lCurr = (DrawingLayer)get(lSelect.getId());
    }
    
    /** This will select the first visible layer.
     */
    public void selectVisibleLayer() {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = (DrawingLayer)itr.next();
            if (l.isVisible()) {
                lCurr = l;
                return;
            }
        }
        
        // None are visible therefore set the first one visible and select it.
        DrawingLayer lFirst = getFirst();
        if (lFirst == null)
            throw new IllegalStateException("There are no layers in this LayerPool to select from.");
        lFirst.setVisability(true);
        lCurr = lFirst;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Layer Methods ">
    
    /** This will add the layer to this DrawingLayerPool. This will not set it as the current layer.
     * @param dLayer is the layer to be added to this DrawingLayerPool.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem addLayer(DrawingLayer dLayer) {        
        super.add(dLayer);
        return new UndoItemNewLayer(dLayer);
    }
    
    /** This will add the layer to this LayerPool at the bottom of
     * the zDepth pool. It will also select the new layer as the current
     * layer.
     * @param lInfo is the new layer.
     */
    public InterfaceUndoItem addLayer(LayerInfo lInfo) {
        if (lInfo.getZDepth() == LayerInfo.ZDEPTH_UNINITIALIZE)
            lInfo.setZDepth(getHighestZDepth() + 100);
        DrawingLayer l = new DrawingLayer(lInfo.getName(), lInfo.getColor(), lInfo.getZDepth());
        super.add(l);
        
        // Becomes the new current layer.
        lCurr = l;
        
        return new UndoItemNewLayer(l);
    }

    
    /** This will delete the layer and if it is the current layer than it will select the master layer.
     * @param lInfo is the layer to be deleted.
     * @return an undo item.
     */
    public InterfaceUndoItem deleteLayer(LayerInfo lInfo) {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // Remove the layer from the LayerPool.
        DrawingLayer lDelete = (DrawingLayer)get(lInfo.getId());
        super.remove(lDelete);
        undoComplex.add(new UndoItemDeleteLayer(lDelete));
        
        // If the number of layers is zero then create a default layer and add it to the LayerPool.
        if (size() == 0) {
            DrawingLayer lAdd = DrawingLayer.createDefaultLayer();
            super.add(lAdd);
            undoComplex.add(new UndoItemNewLayer(lAdd));
        }
        
        // Set the current layer to the first layer.
        lCurr = getFirst();
        
        return undoComplex;
    }
    
    /** This will delete all the layers and recreate the master one. It will set the master as current.
     * @return an undo item.
     */
    public InterfaceUndoItem deleteAllLayers() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        
        // delete every layer in this LayerPool.
        while (size() != 0) {
            DrawingLayer l = getFirst();
            super.remove(l);
            undoComplex.add(new UndoItemDeleteLayer(l));
        }
        
        // create a new default layer.
        DrawingLayer lNew = DrawingLayer.createDefaultLayer();
        super.add(lNew);
        undoComplex.addFirst(new UndoItemNewLayer(lNew));
        
        // Current layer is now the master layer.
        lCurr = lNew;
        
        return undoComplex;
    }
    
    /** @param layerId is the layer too look up.
     * @return true if the layer exists in this pool.
     */
    public boolean exists(int layerId) {
        if (super.find(layerId) == null)
            return false;
        return true;
    }
    
    
    /** @return a list of all the colors which represent all the layers.
     */
    public LinkedList<Color> getColors() {
        LinkedList<Color> ltColors = new LinkedList<Color>();
        for (Iterator itr=values().iterator(); itr.hasNext();) {
            Color c = ((DrawingLayer)itr.next()).getColor();
            ltColors.add(c);
        }
        
        return ltColors;
    }
    
    /** @return the current layer.
     */
    public DrawingLayer getCurrentLayer() {
        return lCurr;
    }
    
    /** @return the first DrawingLayer (This isn't always the first one added) or null if it does not contain one.
     */
    public DrawingLayer getFirst() {
        return (DrawingLayer)super.getFirst();
    }
    
    /** This will get the highest z depth value of the layer pool.
     */
    private int getHighestZDepth() {
        int zDepth = 0;
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = (DrawingLayer)itr.next();
            if (l.getZDepth() > zDepth)
                zDepth = l.getZDepth();
        }
        
        return zDepth;
    }
    
    /** This will get the layer by the id of the layer.
     * @param id is the layer's id to find.
     * @return the layer.
     */
    public DrawingLayer getLayer(int id) {
        return (DrawingLayer)get(id);
    }
    
    /** @return the number of layers in this design.
     */
    public int getLayerCount() {     
        return size();
    }
    
    /** This will get all the layer's informatiom.
     * @return all the layer's information in order of their z-depth.
     */
    public LayerInfo[] getLayerInfos() {
        LayerInfo[] lArrLayerInfo = new LayerInfo[values().size()];
        
        int i=0;
        for (Iterator itr = values().iterator(); itr.hasNext(); i++) {
            lArrLayerInfo[i] = ((DrawingLayer)itr.next()).getInfo();
        }
        
        Arrays.sort(lArrLayerInfo);
        
        return lArrLayerInfo;
    }
    
    /** This will create a list of all the DrawingGraphs in all of the layers in this pool.
     *  @return a list of DrawingGraph from all the layers in this pool.
     */
    public LinkedList<DrawingGraph> toGraphs() {
        LinkedList<DrawingGraph> ltGraphs = new LinkedList<DrawingGraph>();
        
        for (Iterator<DrawingLayer> itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer layer = itr.next();
            ltGraphs.addAll(layer.toGraphs());
        }
        
        return ltGraphs;
    }
    
    /** @return a list of all the drawing layers in this pool.
     */
    public LinkedList<DrawingLayer> toList() {
        LinkedList<DrawingLayer> list = new LinkedList<DrawingLayer>();       
        for (Iterator<DrawingLayer> itr = values().iterator(); itr.hasNext(); )
            list.add(itr.next());
        return list;
    }
    
    /** This will update the layer information such as name, color, visible, and zDepth.
     */
    public void updateLayer(LayerInfo lInfo) {
        DrawingLayer l = (DrawingLayer)get(lInfo.getId());
        l.updateLayer(lInfo);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" General Methods ">
        
    public String toString() {
        return "{LayerPool lCurr[" + lCurr.getId() + "] Super[" + super.toString() + "]}";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fill General Path Methods ">
    
    /** This will fill in a closed area of the layer.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @return an array of Layer and GeneralPath, else null.
     */
    public Object[] fill(FPointType fptMousePosition) {
        DrawingLayer lGenP = null;
        GeneralPath gpSmallest = null;
        double areaGpSmallest = 0;
        // The smallest GeneralPath is the one we need.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = (DrawingLayer)itr.next();
            
            GeneralPath gp = l.createGeneralPath(fptMousePosition);
            if (gp != null) {
                if (gpSmallest == null) {
                    gpSmallest = gp;
                    Rectangle2D r = new Area(gpSmallest).getBounds2D();
                    areaGpSmallest = r.getWidth() * r.getHeight();
                    lGenP = l;
                } else {
                    Rectangle2D r = new Area(gp).getBounds2D();
                    double areaGp = r.getWidth() * r.getHeight();
                    if (areaGp < areaGpSmallest) {
                        gpSmallest = gp;
                        areaGpSmallest = areaGp;
                        lGenP = l;
                    }
                }
            }
        }
        
        // Found a GeneralPath.
        if (lGenP != null) {
            // Return the GeneralPath.
            Object[] arr = new Object[2];
            arr[0] = lGenP;
            arr[1] = gpSmallest;
            return arr;
        }
        
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will get the line information. It will search for the line within the fBounds.
     * @param fBounds is the rectangle to search for the line.
     * @param currLayerOnly is true if it should only search in the current layer.
     */
    public String debugGetInfo(Rectangle2D.Float fBounds, boolean currLayerOnly) {
        if (currLayerOnly)
            return lCurr.debugGetInfo(fBounds);
        
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingLayer l = (DrawingLayer)itr.next();
            String s = l.debugGetInfo(fBounds);
            if (s != null)
                return s;
        }
        
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Layer ">
    
    /** This will undo/redo a new vertex created.
     */
    class UndoItemNewLayer implements InterfaceUndoItem {
        DrawingLayer l;
        
        /** @param vertexId is the vertex's id.
         */
        UndoItemNewLayer(DrawingLayer l) {
            this.l = l;
        }
        
        public void undoItem() {
            remove(l);
            
            // Need to select a new layer for current if it will be deleted.
            if (l == lCurr)
                lCurr = getFirst();
        }
        
        public void redoItem() {
            restore(l);
            
            // Need to select a new layer for current if it is null.
            if (lCurr == null)
                lCurr = l;
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{LayerPool.UndoItemNewLayer Layer[" + l + "]}";
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Remove Layer ">
    
    /** This will undo/redo a delete layer created.
     */
    class UndoItemDeleteLayer implements InterfaceUndoItem {
        DrawingLayer l;
        
        /** @param l is the layer that is to be delete.
         */
        UndoItemDeleteLayer(DrawingLayer l) {
            this.l = l;
        }
        
        public void undoItem() {
            restore(l);
            
            // Need to select a new layer for current if it is null.
            if (lCurr == null)
                lCurr = l;
        }
        
        public void redoItem() {
            remove(l);
            
            // Need to select a new layer for current if it will be deleted.
            if (l == lCurr)
                lCurr = getFirst();
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{LayerPool.UndoItemDeleteLayer Layer[" + l + "]}";
        }
    }
    
    // </editor-fold>
    
}
