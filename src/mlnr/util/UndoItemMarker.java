/*
 * UndoItemMarker.java
 *
 * Created on August 4, 2006, 7:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.util;

/** This is an undo item marker class.
 */
public class UndoItemMarker implements InterfaceUndoItem {
    
    public UndoItemMarker() {
    }
    
    public void undoItem() {
        throw new UnsupportedOperationException("UndoItemMarker: cannot perform an undo, undo list has become corrupt.");
    }
    
    public void redoItem() {
        throw new UnsupportedOperationException("UndoItemMarker: cannot perform a redo, redo list has become corrupt.");
    }
    
    public boolean isUndoable() {
        return true;
    }
    
    public String toString() {
        return "{Design.UndoItemMarker Marker}";
    }
}
