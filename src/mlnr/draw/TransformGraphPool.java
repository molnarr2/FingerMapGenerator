/*
 * TransformGraphPool.java
 *
 * Created on August 3, 2007, 1:53 PM
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
public class TransformGraphPool extends AbstractPool {
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors/Duplicate Methods ">
    
    /** Creates a new instance of TransformGraphPool */
    public TransformGraphPool() {
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying Layers ">
    
    /** @returns a collection view of this TransformGraphPool of the underlying values in this pool.
     */
    final public Collection<TransformGraph> values() {
        return super.values();
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Add/Remove Methods ">
    
    /** This will add the tGraph to this pool.
     * @param tGraph is the graph to add to this pool.
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem add(TransformGraph tGraph) {        
        super.add((InterfacePoolObject)tGraph);
        return new UndoItemNewGraph(tGraph);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw/Status/Get/To Methods ">
    
    /** This will get the first TransformGraph in this pool.
     * @return the first TransformGraph or null if this pool is empty.
     */
    public TransformGraph getFirst() {
        return (TransformGraph)super.getFirst();
    }
        
    /** @return the number of TransformGraphs in this pool.
     */
    public int size() {
        return super.size();
    }

    /** @return a list of TransformGraph from this pool.
     */
    LinkedList<TransformGraph> toList() {
        LinkedList<TransformGraph> ltGraphs = new LinkedList();
        for (Iterator<TransformGraph> itr = values().iterator(); itr.hasNext(); ) {
            ltGraphs.add(itr.next());
        }
        return ltGraphs;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Graph ">
    
    /** This will undo/redo a new graph created.
     */
    class UndoItemNewGraph implements InterfaceUndoItem {
        TransformGraph g;
        
        UndoItemNewGraph(TransformGraph g) {
            this.g = g;
        }
        
        public void undoItem() {
            remove(g);
        }
        
        public void redoItem() {
            restore(g);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{TransformGraphPool.UndoItemNewGraph graph[" + g.getId() + "]}";
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Delete Graph ">
    
    /** This will undo/redo a delete graph.
     */
    class UndoItemDeleteGraph implements InterfaceUndoItem {
        TransformGraph g;
        
        UndoItemDeleteGraph(TransformGraph g) {
            this.g = g;
        }
        
        public void undoItem() {
            restore(g);
        }
        
        public void redoItem() {
            remove(g);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{TransformGraphPool.UndoItemDeleteGraph graph[" + g.getId() + "]}";
        }
    }
    
    // </editor-fold>
    
}
