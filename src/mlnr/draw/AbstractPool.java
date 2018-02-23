/*
 * AbstractHashPool.java
 *
 * Created on July 14, 2006, 12:26 PM
 *
 */

package mlnr.draw;

import java.util.*;

/** Class responsible for managing a list of objects through a HashMap or TreeMap.
 */
abstract public class AbstractPool {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Key: {uniqueId}, Value: {InterfacePoolObject}.
     * Constrants: All objects must have a unique Id. */
    private AbstractMap<Integer, InterfacePoolObject> abMap = new HashMap();
    
    /** All Objects {Interface} must have a unique Id. */
    private int uniqueId=0;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** Uses a HashMap for this pool type. */
    protected static final int POOLTYPE_HASHMAP = 1;
    /** Uses a TreeMap for this pool type (Keeps the order of the items). */
    protected static final int POOLTYPE_TREEMAP = 2;
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" ZDepth Methods ">

    /** This will move the pool object's zDepth up to the next higher value, swapping the two pool object values.
     * @param id is the pool object that needs it's zDepth to go up to the next highest value.
     * @return true if it moved higher.
     */
    public boolean moveHigher(int id) {
        InterfacePoolObject iCurr = get(id);
        int zDepthCurr = iCurr.getZDepth();        
        int zDepth = zDepthCurr;
        InterfacePoolObject iSwap = null;
        
        // Get the next higher zDepth value and swap zDepths.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPool = (InterfacePoolObject)itr.next();
            if (iPool == iCurr)
                continue;
            if (iPool.getZDepth() > zDepthCurr) {
                if (zDepth == zDepthCurr || iPool.getZDepth() < zDepth) {
                    zDepth = iPool.getZDepth();
                    iSwap = iPool;
                }
            }
        }
        
        // Swap the zDepth values.
        if (iSwap != null) {
            int tempZDepth = iSwap.getZDepth();
            iSwap.setZDepth(iCurr.getZDepth());
            iCurr.setZDepth(tempZDepth);
            return true;
        }
        
        return false;
    }
    
    /** This will move the pool object's zDepth up to the next lower value, swapping the two pool object values.
     * @param id is the pool object that needs it's zDepth to go up to the next highest value.
     * @return true if it moved lower.
     */
    public boolean moveLower(int id) {
        InterfacePoolObject iCurr = get(id);
        int zDepthCurr = iCurr.getZDepth();        
        int zDepth = zDepthCurr;
        InterfacePoolObject iSwap = null;
        
        // Get the next lower zDepth value and swap zDepths.
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPool = (InterfacePoolObject)itr.next();
            if (iPool == iCurr)
                continue;
            if (iPool.getZDepth() < zDepthCurr) {
                if (zDepth == zDepthCurr || iPool.getZDepth() > zDepth) {
                    zDepth = iPool.getZDepth();
                    iSwap = iPool;
                }
            }
        }
        
        // Swap the zDepth values.
        if (iSwap != null) {
            int tempZDepth = iSwap.getZDepth();
            iSwap.setZDepth(iCurr.getZDepth());
            iCurr.setZDepth(tempZDepth);
            return true;
        }
        
        return false;
    }
    
    /** This will bump the pool object's zDepth to the highest value while moving each of the values from the highest to that pool object 
     * down one. It will call moveHigher until it gets to the top.
     * @param id is the pool object id.
     * @return true if it moved to top.
     */
    public boolean moveTop(int id) {
        boolean higher = moveHigher(id);        
        if (higher == false)
            return higher;
        
        // Keep moving up until to the top position.
        while (higher) {
            higher = moveHigher(id);
        }
        
        return true;
    }
    
    /** This will bump the pool object's zDepth to the lowest value while moving each of the values from the lowest to that pool object 
     * up one. It will call moveLower until it gets to the bottom.
     * @param id is the pool object id.
     * @return true if it moved to bottom.
     */
    public boolean moveBottom(int id) {
        boolean higher = moveLower(id);        
        if (higher == false)
            return higher;
        
        // Keep moving up until to the top position.
        while (higher) {
            higher = moveLower(id);
        }
        
        return true;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Public Methods ">
    
    /** @return the number of objects in this pool.
     */
    public int size() {
        return abMap.size();
    }
    
    /** @return the first InterfacePoolObect (This isn't always the first one added) or null if it does not contain one.
     */
    public InterfacePoolObject getFirst() {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            return (InterfacePoolObject)itr.next();
        }
        
        return null;
    }
    
    /** This will pop the first item in the pool (This isn't always the first one added).
     * @return the first item in the pool.
     * @throws IllegalStateException no more items in pool.
     */
    public InterfacePoolObject popFirst() {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPoolObject = (InterfacePoolObject)itr.next();
            itr.remove();
            return iPoolObject;
        }
        
        throw new IllegalStateException("AbstractPool::popFirst() does not contain any more items.");
    }
    
    /** @return A string of the list of the object's toString().
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(250);

        buf.append("\n{" + getClass().getName() + "\n");
        
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            buf.append(itr.next() + "\n");
        }
        
        buf.append("}\n");
        
        return buf.toString();
    }    
    
    /** @return a string of the key value and then the value's id. Should match.
     */
    public String toStringKeys() {
        StringBuffer buf = new StringBuffer(250);

        buf.append("{" + getClass().getName() + ":: " + abMap);
        
        buf.append("}\n");
        
        return buf.toString();
    }
    
    /** This will update the changes made to this AbstractHashPool from outside. This function should be 
     * called on an AbstractHashPool that had some of its value's ids changed from an external function.
     */
    public void update() {
        HashMap newHashMap = new HashMap(abMap.size() * 2);
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPool = (InterfacePoolObject)itr.next();
            
            // The new hash map must contain unique ids.
            if (newHashMap.get(iPool.getId()) != null)
                throw new IllegalStateException("AbstractHashPool::update() contains duplicate Ids [" + iPool.getId() + "].");
            
            newHashMap.put(iPool.getId(), iPool);
        }
        
        // Now change the hashmap.
        abMap = newHashMap;
    }
   
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Protected Methods ">
        
    /** This will add a new object into this HashMap Pool.
     * @param iPoolObject to be added to this HashMap pool.
     */
    protected void add(InterfacePoolObject iPoolObject) {
        iPoolObject.setId(getNextUniqueId());
        abMap.put(iPoolObject.getId(), iPoolObject);
    }
        
    /** This will restore an object that was deleted from this HashMap. It will
     * put the object into the hashmap without changing the id of it.
     * @param iPoolObject to be restored to this HashMap pool.
     */
    protected void restore(InterfacePoolObject iPoolObject) {
        abMap.put(iPoolObject.getId(), iPoolObject);
        if (iPoolObject.getId() > uniqueId)
            uniqueId = iPoolObject.getId();
    }
    
    /** This will add all objects from the abPool to this pool.
     * @param abPool is the pool to add those objects to this pool. Note that this will not clone the objects in the abPool.
     * @return A LinkedList of Ids that were added. Not null, but can be empty.
     */
    protected LinkedList add(AbstractPool abPool) {
        LinkedList ltIds = new LinkedList();        
        for (Iterator itr = abPool.values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPoolObject = (InterfacePoolObject)itr.next();            
            add(iPoolObject);
            
            ltIds.add(iPoolObject.getId());
        }        
        return ltIds;
    }
    
    /** This will replace the iPoolObject with the object in the pool that contains the same id number.
     * @param iPoolObject is the object to be used to replace an object into the pool.
     * @return true if the object was replaced.
     */
    protected boolean replace(InterfacePoolObject iPoolObject) {
        if(find(iPoolObject.getId()) != null) {
            abMap.put(iPoolObject.getId(), iPoolObject);
            return true;
        }
        
        return false;
    }
    
    /** This will search for the object with the id, if it is not found then null is retured. Use get() if the object must exist.
     * @param id is the id of the object.
     * @return Object of the id, or null if not found;
     */
    protected InterfacePoolObject find(int id) {
        return (InterfacePoolObject)abMap.get(id);
    }
    
    /** This will get the object with the id.
     * @param id is the id of the object.
     * @return Object of the id.
     * @exception IllegalArgumentException Id[] does not exist in object pool.
     */
    protected InterfacePoolObject get(int id) {
        InterfacePoolObject iPool = (InterfacePoolObject)abMap.get(id);
        if (iPool == null)
            throw new IllegalArgumentException("Id[" + id + "] does not exist in object pool.");        
        return iPool;
    }

    /** This will remove the iPool from the hashMap.
     * @param iPool is the object to remove from the hash map.
     * @exception IllegalArgumentException Id does not exist.
     */
    protected void remove(InterfacePoolObject iPool){
        InterfacePoolObject iRemove = (InterfacePoolObject)abMap.remove(iPool.getId());
        if (iRemove == null)
            throw new IllegalArgumentException ("Id[" + iPool.getId() + "] does not exist in this pool.");
    }
    
    /** This will remove all items from this hashMap (creates a new hashMap).
     */
    protected void removeAllItems() {
        abMap = new HashMap();
    }
    
    /** @return Collection of the values of the HashMap. InterfacePool will be the values.
     */
    protected Collection values() {
        return abMap.values();
    }
    
    /** This will create a new LinkedList of the values sorted. First value is the highest.
     * @return A linkedlist of the values sorted.
     */
    protected LinkedList valuesSorted() {
        LinkedList ltSortedValues = new LinkedList(values());
        Collections.sort(ltSortedValues);
        return ltSortedValues;
    }
    
    /** This will create a new LinkedList of the values sorted reverse. First value is the lowest.
     * @return A linkedlist of the values sorted.
     */
    protected LinkedList valuesSortedReverse() {
        LinkedList ltSortedValues = new LinkedList(values());
        Collections.sort(ltSortedValues);
        Collections.reverse(ltSortedValues);
        return ltSortedValues;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Private Methods ">    
    
    /** This will get the next unique id. Guarenteed not to be in the Hashmap.
     * @return next uniqueId.
     */
    private int getNextUniqueId() {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            InterfacePoolObject iPoolObject = (InterfacePoolObject)itr.next();
            if (iPoolObject.getId() > uniqueId)
                uniqueId = iPoolObject.getId();
        }        
        return ++uniqueId;
     }    
    
    // </editor-fold>
    
 }