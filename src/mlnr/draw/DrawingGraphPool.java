/*
 * GraphPool.java
 *
 * Created on July 14, 2006, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.*;
import org.w3c.dom.*;
import mlnr.util.*;
import mlnr.type.*;

/**
 *
 * @author Robert Molnar II
 */
class DrawingGraphPool extends AbstractPool {
                    
    // <editor-fold defaultstate="collapsed" desc=" Methods for getting to the underlying Graph ">
    
    /** @return the first DrawingGraph (This isn't always the first one added) or null if it does not contain one.
     */
    final public DrawingGraph getFirst() {
        return (DrawingGraph)super.getFirst();
    }
    
    /** @returns a collection view of this DrawingGraph of the underlying values in this pool.
     */
    final public Collection<DrawingGraph> values() {
        return super.values();
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Get/Set/Exists Methods ">
    
    /** This will get the graph by the id of the graph.
     * @param id is the graph's id to find.
     * @return the graph.
     */
    public DrawingGraph getGraph(int id) {
        return (DrawingGraph)get(id);
    }
    
    /** This will see if the DrawingGraph exists in this pool.
     * @param dGraph is the DrawingGraph to see if it exists in this pool.
     * @return true if it does exist, else false it does not.
     */
    boolean exists(DrawingGraph dGraph) {
        if (super.find(dGraph.getId()) == null)
            return false;
        return true;
    }    
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add/Remove Methods ">
    
    /** This will add the drawing graph to this pool.
     * @param gAdd is the graph to add to this pool (Simply adds it to the pool, no checking at all).
     * @return an undo item for the operation.
     */
    public InterfaceUndoItem add(DrawingGraph gAdd) {     
        // Add the graph to this pool.
        super.add(gAdd);
        return new UndoItemNewGraph(gAdd);
    }

    /** This will remove the drawing graph from this pool.
     * @param gRemove is the graph to remove from this pool (Simply removes it from the pool, no checking at all).
     * @return an undo item for this operation.
     */
    public InterfaceUndoItem remove(DrawingGraph gRemove) {
        super.remove(gRemove);
        return new UndoItemDeleteGraph(gRemove);
    }    
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" General Path Methods ">
    
    /** This will create a GeneralPath from the position fptMouseDown.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @return null or a GeneralPath containing the position fptMouseDown.
     */
    public GeneralPath createGeneralPath(FPointType fptMouseDown) {
        // The smallest GeneralPath is the one we need.
        GeneralPath gpSmallest = null;
        double areaGpSmallest = 0;
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            DrawingGraph g = (DrawingGraph)itr.next();
            GeneralPath gp = g.createGeneralPath(fptMouseDown);            
            if (gp != null) {
                if (gpSmallest == null) {
                    gpSmallest = gp;
                    Rectangle2D r = new Area(gpSmallest).getBounds2D();
                    areaGpSmallest = r.getWidth() * r.getHeight();
                } else {
                    Rectangle2D r = new Area(gp).getBounds2D();
                    double areaGp = r.getWidth() * r.getHeight();
                    if (areaGp < areaGpSmallest) {
                        gpSmallest = gp;
                        areaGpSmallest = areaGp;
                    }
                }
            }
        }
        
        return gpSmallest;
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">

    public String toString() {
        return "{DrawingGraphPool " + super.toString() + "}";
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Graph ">
    
    /** This will undo/redo a new graph created.
     */
    class UndoItemNewGraph implements InterfaceUndoItem {
        DrawingGraph g;
        
        UndoItemNewGraph(DrawingGraph g) {
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
            return "{DrawingGraphPool.UndoItemNewGraph graph[" + g.getId() + "]}";
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Delete Graph ">
    
    /** This will undo/redo a delete graph.
     */
    class UndoItemDeleteGraph implements InterfaceUndoItem {
        DrawingGraph g;
        
        UndoItemDeleteGraph(DrawingGraph g) {
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
            return "{DrawingGraphPool.UndoItemDeleteGraph graph[" + g.getId() + "]}";
        }
    }
    
    // </editor-fold>
    
}
