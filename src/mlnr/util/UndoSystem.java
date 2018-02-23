/*
 * UndoSystem.java
 *
 * Created on August 4, 2006, 7:22 PM
 */

package mlnr.util;

import java.util.*;
      
/** This class is an undo system. It will store undos and redos to be performed on.
 */
public class UndoSystem {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is a list of InterfaceUndoItem. New undos are placed at the end of 
     * the list. To undo, pop the first one. */
    protected LinkedList ltUndo = new LinkedList();
    
    /** This is a list of InterfaceUndoItem. Redos are placed at the end of the 
     * list. To redo, pop the first one. */
    protected LinkedList ltRedo = new LinkedList();
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors ">
    
    public UndoSystem() {
        
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo/Redo Methods ">
    
    /** This will undo one item and add it to the redo list.
     */
    public void undo() {
        InterfaceUndoItem iUndo = (InterfaceUndoItem)ltUndo.removeFirst();
        ltRedo.addFirst(iUndo);
        iUndo.undoItem();
    }
    
    /** This will redo one item and add it to the undo list.
     */
    public void redo() {
        InterfaceUndoItem iRedo = (InterfaceUndoItem)ltRedo.removeFirst();
        ltUndo.addFirst(iRedo);
        iRedo.redoItem();
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Mutator Methods ">
    
    /** this will clear both undo/redo lists.
     */
    public void clear() {
        ltUndo.clear();
        ltRedo.clear();
    }
    
    /** This willl create one big undo for all undos between the start of the list to the undo marker.
     */
    public void createUndoFromMarker() {
        UndoItemComplex undoComplex = new UndoItemComplex();
        Iterator itr = ltUndo.iterator();
        while (itr.hasNext()) {
            InterfaceUndoItem iUndoItem = (InterfaceUndoItem)itr.next();
            itr.remove();
            
            // Found the marker now add the complex undo to the list.
            if (iUndoItem instanceof UndoItemMarker) {
                add(undoComplex);
                return;
            }
            
            undoComplex.add(iUndoItem);
        }
        
        throw new IllegalStateException("UndoSystem: createUndoFromMaker did not find the undo marker. Undo list is corrupt.");
    }
    
    /** This will add an undo item to this UndoSystem.
     * @param iUndoItem is the item to add to this UndoSystem.
     */
    public void add(InterfaceUndoItem iUndoItem) {
        // Don't add if it is an empty undo.
        if (!iUndoItem.isUndoable())
            return;
        // Add the undo to the list of undos.
        ltUndo.addFirst(iUndoItem);
        
        // Don't clear the redo list if the undo item is an UndoItemMarker.
        if (iUndoItem instanceof UndoItemMarker == false)
            ltRedo.clear();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Status Methods ">
    
    /** @return true if undo possible.
     */
    public boolean isUndoPossible() {
        if (ltUndo.size() > 0)
            return true;
        return false;
    }
    
    /** @return true if redo possible.
     */
    public boolean isRedoPossible() {
        if (ltRedo.size() > 0)
            return true;
        return false;
    }
    
    /** @return true if there is an undo marker in the undo list.
     */
    public boolean isUndoMarker() {
        for (Iterator itr = ltUndo.iterator(); itr.hasNext(); ) {
            InterfaceUndoItem iUndoItem = (InterfaceUndoItem)itr.next();
            
            // Found an undo marker.
            if (iUndoItem instanceof UndoItemMarker)
                return true;
        }
        
        return false;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will print out the undo and redo lists.
     */
    public void print() {
        System.out.println("--------- UndoSystem: BEGIN UndoList (First is the first one to be undo)");
        for (Iterator itr = ltUndo.iterator(); itr.hasNext(); ) {
            System.out.println(itr.next().toString());
        }
        System.out.println("--------- UndoSystem: END UndoList (First is the first one to be undo)");
        System.out.println("---------");
        System.out.println("--------- UndoSystem: BEGIN RedoList (First is the first one to be redo)");
        for (Iterator itr = ltRedo.iterator(); itr.hasNext(); ) {
            System.out.println(itr.next().toString());
        }
        System.out.println("--------- UndoSystem: END RedoList (First is the first one to be redo)");
        System.out.println();
        System.out.println();
        System.out.println();
    }
    
    /** This will print out the undo and redo lists into the error log file.
     */
    public void printLog() {
        StringBuffer sBuffer = new StringBuffer();
        
        sBuffer.append("--------- UndoSystem: BEGIN UndoList (First is the first one to be undo)\n");
        for (Iterator itr = ltUndo.iterator(); itr.hasNext(); ) {
            sBuffer.append(itr.next().toString());
            sBuffer.append("\n");
        }
        sBuffer.append("--------- UndoSystem: END UndoList (First is the first one to be undo)\n");
        sBuffer.append("---------");
        sBuffer.append("--------- UndoSystem: BEGIN RedoList (First is the first one to be redo)\n");
        for (Iterator itr = ltRedo.iterator(); itr.hasNext(); ) {
            sBuffer.append(itr.next().toString());
        }
        sBuffer.append("--------- UndoSystem: END RedoList (First is the first one to be redo)\n\n\n");
        System.out.println(sBuffer.toString());
    }
    
    // </editor-fold>
}

