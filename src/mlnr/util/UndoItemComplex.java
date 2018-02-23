/*
 * UndoItemComplex.java
 *
 * Created on December 19, 2005, 12:40 PM
 *
 */

package mlnr.util;

import java.util.*;

/** This will undo/redo a complex operation.
 */
public class UndoItemComplex implements InterfaceUndoItem {
    LinkedList ltUndoItem;
    
    public UndoItemComplex() {
        ltUndoItem = new LinkedList();
    }
    
    public UndoItemComplex(LinkedList ltUndoItem) {
        this.ltUndoItem = ltUndoItem;
    }
    
    /** This will get the number of undo items in this complex.
     */
    public int size() {
        return ltUndoItem.size();
    }
    
    /** This will add the undo item to this complex undo item list. It will only add undos
     * that contain an undo. It will add the item at the very beginning of the list.
     * @param iUndoItem is the item which will be added to this complex undo item list. It can be null
     * in that case it will not be added.
     */
    public void add(InterfaceUndoItem iUndoItem) {
        if (iUndoItem == null)
            return;
        if (iUndoItem.isUndoable() == false)
            return;
        if (iUndoItem instanceof UndoItemComplex) {
            // create one big undo. add all items in it to this UndoItemComplex
            UndoItemComplex uItemComplex = (UndoItemComplex)iUndoItem;
            for (Iterator itr=uItemComplex.ltUndoItem.iterator(); itr.hasNext(); )
                addFirst ((InterfaceUndoItem)itr.next());
        } else
            ltUndoItem.addFirst(iUndoItem);
    }
    
    /** This will add the undo item to the front of this complex undo item list. It will only add undos
     * that contain an undo.
     * @param iUndoItem is the item which will be added to this complex undo item list. It can be null
     * in that case it will not be added.
     */
    public void addFirst(InterfaceUndoItem iUndoItem) {
        if (iUndoItem == null)
            return;
        if (iUndoItem.isUndoable() == false)
            return;
        if (iUndoItem instanceof UndoItemComplex) {
            // create one big undo. add all items in it to this UndoItemComplex
            UndoItemComplex uItemComplex = (UndoItemComplex)iUndoItem;
            for (Iterator itr=uItemComplex.ltUndoItem.iterator(); itr.hasNext(); )
                addFirst ((InterfaceUndoItem)itr.next());
        } else
            ltUndoItem.addFirst(iUndoItem);
    }
    
    public void undoItem() {        
        for (Iterator itr = ltUndoItem.iterator(); itr.hasNext(); ) {
            InterfaceUndoItem iUndoItem = (InterfaceUndoItem)itr.next();
            iUndoItem.undoItem();
        }
    }
    
    public void redoItem() {
        for (ListIterator itr = ltUndoItem.listIterator(ltUndoItem.size()); itr.hasPrevious(); ) {
            InterfaceUndoItem iUndoItem = (InterfaceUndoItem)itr.previous();
            iUndoItem.redoItem();
        }
    }
    
    public boolean isUndoable() {
        if (ltUndoItem.size() == 0)
            return false;
        return true;
    }
    
    public String toString() {
        StringBuffer strBuffer = new StringBuffer("{UndoItemComplex size[" + ltUndoItem.size() + "] ");
        
        for (Iterator itr = ltUndoItem.iterator(); itr.hasNext(); ) {
            strBuffer.append(itr.next().toString() + ", ");
        }
        
        strBuffer.append("}");
        
        return strBuffer.toString();
    }
    
    /** This will get the first type of undo class in this list of undos.
     * @pararm c
     */
    public InterfaceUndoItem getFirst(Class c) {
        for (Iterator itr = ltUndoItem.iterator(); itr.hasNext(); ) {
            InterfaceUndoItem iUndoItem = (InterfaceUndoItem)itr.next();
            if (c.isInstance(iUndoItem))
                return iUndoItem;
        }
        
        throw new IllegalArgumentException("UndoItemComplex::getFirst() unable to find the class " + c);
    }
}
