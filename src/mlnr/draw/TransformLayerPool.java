/*
 * TransformLayerPool.java
 *
 * Created on August 3, 2007, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.util.InterfaceUndoItem;

/**
 *
 * @author Robert Molnar 2
 */
public class TransformLayerPool extends AbstractPool {
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    /** Creates a new instance of TransformLayerPool */
    public TransformLayerPool() {
    }
    
    // </editor-fold>
            
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying Layers ">
    
    /** @returns a collection view of this TransformLayerPool of the underlying values in this pool.
     */
    final public Collection<TransformLayer> values() {
        return super.values();
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Add/Remove Methods ">
    
    /** This will add the TransformLayer to this TransformLayerPool.
     * @param transformLayer to be added to this TransformLayerPool.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem add(TransformLayer transformLayer) {
        super.add((InterfacePoolObject)transformLayer);
        return new UndoItemNewLayer(transformLayer);
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Status/Get/To Methods ">
    
    /** This will get the first TransformLayer in this pool.
     * @return the first TransformLayer or null if this pool is empty.
     */
    public TransformLayer getFirst() {
        return (TransformLayer)super.getFirst();
    }
    
    /** @return the number of TransformLayer in this pool.
     */
    public int size() {
        return super.size();
    }    

    /** @return a list of layers of these layers.
     */
    LinkedList<TransformLayer> toList() {
        LinkedList<TransformLayer> ltList = new LinkedList();
        for (Iterator<TransformLayer> itr = values().iterator(); itr.hasNext(); )
            ltList.add(itr.next());        
        return ltList;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Layer ">
    
    /** This will undo/redo a new vertex created.
     */
    class UndoItemNewLayer implements InterfaceUndoItem {
        TransformLayer l;
        
        /** @param vertexId is the vertex's id.
         */
        UndoItemNewLayer(TransformLayer l) {
            this.l = l;
        }
        
        public void undoItem() {
            remove(l);
        }
        
        public void redoItem() {
            restore(l);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{TransformLayerPool.UndoItemNewLayer Layer[" + l + "]}";
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Remove Layer ">
    
    /** This will undo/redo a delete layer created.
     */
    class UndoItemDeleteLayer implements InterfaceUndoItem {
        TransformLayer l;
        
        /** @param l is the layer that is to be delete.
         */
        UndoItemDeleteLayer(TransformLayer l) {
            this.l = l;
        }
        
        public void undoItem() {
            restore(l);
        }
        
        public void redoItem() {
            remove(l);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{TransformLayerPool.UndoItemDeleteLayer Layer[" + l + "]}";
        }
    }
    
    // </editor-fold>
    
}
