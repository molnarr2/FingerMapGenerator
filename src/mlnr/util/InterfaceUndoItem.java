/*
 * InterfaceUndo.java
 *
 * Created on December 16, 2005, 2:09 PM
 */

package mlnr.util;

/** Anything that can be undone must implement this interface. This is the
 * interface for an undo/redo item.
 * @author Robert Molnar
 */
public interface InterfaceUndoItem {
    /** This is called on the item to undo the transaction. It will undo 
     * the action that was performed. 
     */
    public void undoItem();
    
    /** This is called on the item to redo the undo. Assumed that undo is
     * called first before this function is called. It will redo the undo 
     * of the action that was performed.
     */
    public void redoItem();
    
    /** @return true if this item can be undoable. false means that it has 
     * nothing to undo.
     */
    public boolean isUndoable();    
}