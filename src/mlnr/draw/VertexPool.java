/*
 * VertexPool.java
 *
 * Created on July 14, 2006, 2:43 PM
 *
 */

package mlnr.draw;

import java.awt.geom.Rectangle2D;
import java.util.*;
import org.w3c.dom.*;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;

/** This class is used to maintain a pool of Vertex. <br>
 * This class will perform the following operations: add lines, search and find, remove lines, and status functions. <br>
 * @author Robert Molnar 2
 */
public class VertexPool extends AbstractPool {

    // <editor-fold defaultstate="collapsed" desc=" Constructor and Clone Methods">
    
    /** Creates a new instance of VertexPool.  */
    public VertexPool() {
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Has/Is/Intersects/Contains/Status Methods ">
    
    /** This will check to see if the vertex is contained in this VertexPool. 
     * @param v is the vertex to check to see if it is contained in this VertexPool.
     * @return true if this contains the vertex, must be the exact vertex a.k.a. same memory location.
     */
    public boolean contains(Vertex v) {
        InterfacePoolObject iPoolObject = find(v.getId());
        if (iPoolObject == v)
            return true;
        return false;
    }
    
    /** Get a count of the items with the flag type.
     * @param flag is used to set each vertex flag values. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search.
     * @return the number of items which has that flag.
     */
    public int count(int flag) {
        int count = 0;
        for (Iterator<Vertex> itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            if (v.is(flag))
                count++;
        }
        
        return count;
    }
    
    /** This will check to see if this VertexPool contains any points in the TransformGraph which
     * have not been visited. It will check all vertices in this VertexPool to the unvisited vertices
     * in the TransformGraph.
     * @param g is the graph to see if there are any unvisited points in it's VertexPool that
     * are within proximity of the this VertexPool's points.
     */
    boolean containsUnvisited(TransformGraph g) {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = (Vertex) itr.next();
            
            if (g.containsUnvisited(v.getPoint()))
                return true;
        }
        
        return false;
    }    

    /** This will set all vertices to the flag value.
     * @param flag is used to set each vertex flag values. See the SEARCH_* flag. They
     * can be OR'd together for a more specific search.
     */
    void set(int flag) {
        for (Iterator<Vertex> itr = values().iterator(); itr.hasNext(); )
            itr.next().set(flag);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" getVertex/toList/Search Methods ">
    
    /** This will get the next unvisited point from the TransformGraph where it connects to this pool of vertices.
     * @param g is the graph to see if there are any unvisited points which connect to this pool within vertex spaces.
     * @return the point from the TransformGraph which is unvisited and connects to a vertex from this
     * vertex pool and is within the vertex space. Or null if there does not exist such a point.
     */
    FPointType getNextUnvisitedPoint(TransformGraph g) {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = (Vertex) itr.next();
            
            if (g.containsUnvisited(v.getPoint()))
                return g.getPoint(v.getPoint());
        }
        
        return null;
    }
    
    /** This will get the vertex at the point fpt that is within the vertex space. 
     * @param fpt is the point to search for a vertex.
     * @return the vertex at the point fpt which is within the vertex's space. return
     * null if it didn't find any at the point.
     */
    public Vertex getVertexWithinVertexSpace(FPointType fpt) {
        Vertex vSearchFor = new Vertex(fpt);
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = (Vertex)itr.next();
            if (v.isVertexSpace(vSearchFor))
                return v;
        }
        
        return null;
    }
    
    /** This will create a list of vertices within the rectangle. The vertices returned will point to the ones 
     * within this VertexPool. 
     * @param r is the rectangle that is used to search for vertices.
     * @return a LinkedLsit with the vertices in the rectangle and it could be empty.
     */
    public LinkedList<Vertex> search(Rectangle2D.Float r) {
        return search(r, false);
    }
    
    /** This will create a list of vertices within the rectangle. The vertices returned will point to the ones 
     * within this VertexPool. 
     * @param r is the rectangle that is used to search for vertices.
     * @param oneVertex is true if it should only search for 1 vertex.
     * @return a LinkedLsit with the vertices in the rectangle and it could be empty.
     */   
    public LinkedList<Vertex> search(Rectangle2D.Float r, boolean oneVertex) {
        LinkedList<Vertex> ltVertex = new LinkedList<Vertex>();
        
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = (Vertex)itr.next();
            
            // See if the vertex is in the rectangle.
            FPointType fpt = v.getPoint();
            if (r.contains(fpt.x, fpt.y)) {
                ltVertex.add(v);
                
                // Found the one.
                if (oneVertex)
                    break;
            }
        }
        
        return ltVertex;
    }
    
    /** This will create a list of Vertex where each Vertex matches the flag values.
     * @param flag is used to perform this search for one of its kind. See the DrawingLinePool.SEARCH_* flag. They
     * can be OR'd together for a more specific search. Each Vertex must match the flag values.
     * @return a list of all Vertex which match the flag values. Can be empty if there does not exist
     * any lines that match the flag values.
     */
    public LinkedList<Vertex> toList(int flag) {
        LinkedList<Vertex> ltLines = new LinkedList<Vertex>();
        
        for (Iterator<Vertex> itr = values().iterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            if (v.is(flag))
                ltLines.add(v);                
        }
        
        return ltLines;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add/Remove Methods ">
    
    /** This will add a new vertex to this VertexPool.
     * @param v is the vertex to be added to this VertexPool.
     * @return an undo operation.
     */
    public InterfaceUndoItem add(Vertex v) {
        super.add(v);
        return new UndoItemNewVertex(v);
    }
    
    /** This will remove the vertex from this VertexPool.
     * @param vDelete is the vertex to be deleted.
     * @return a undo operation.
     * @throws IllegalArugmentException id does not exist in this VertexPool.
     */
    public InterfaceUndoItem remove(Vertex vDelete) {
        // Delete this vertex from this VertexPool.
        vDelete.resetStatuses();
        super.remove(vDelete);            
        return new UndoItemDeleteVertex(vDelete);
    }    
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Standard Methods ">
    
    /** @return a string of information about this VertexPool.
     */
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("{VertexPool size[" + size() + "] Vertices[");
        
        for (Iterator itr=values().iterator(); itr.hasNext(); ) {
            Vertex v = (Vertex)itr.next();
            sbuf.append(v.toString() + " ");
        }
        
        sbuf.append("]}");
        return sbuf.toString();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Vertex ">
    
    /** This will undo/redo a new vertex created.
     */
    class UndoItemNewVertex implements InterfaceUndoItem {
        Vertex vertex;
       
        /** @param vertexId is the vertex's id.
         */
        UndoItemNewVertex(Vertex v) {
            this.vertex = v;
        }
        
        public void undoItem() {
            remove(vertex);
        }
        
        public void redoItem() {
            restore(vertex);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{VertexPool.UndoItemNewVertex Vertex[" + vertex + "]}";
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Delete Vertex ">
    
    /** This will undo/redo a delete vertex.
     */
    class UndoItemDeleteVertex implements InterfaceUndoItem {
        Vertex vertex;
       
        /** @param vertexId is the vertex's id.
         */
        UndoItemDeleteVertex(Vertex v) {
            this.vertex = v;
        }
        
        public void undoItem() {
            restore(vertex);
        }
        
        public void redoItem() {
            remove(vertex);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{VertexPool.UndoItemDeleteVertex Vertex[" + vertex + "]}";
        }
    }
    
    // </editor-fold>
    
}

// <editor-fold defaultstate="collapsed" desc=" DistanceComparator class ">

class DistanceComparator implements Comparator {
    FPointType fpt;
    
    DistanceComparator(FPointType fpt) {
        this.fpt = fpt;
    }
    public int compare(Object o1, Object o2) {
        float d1 = ((Vertex)o1).getPoint().distance(fpt);
        float d2 = ((Vertex)o2).getPoint().distance(fpt);
        d1 = d1 - d2;
        if (d1 > 0)
            return 1;
        else if (d1 == 0.0f)
            return 0;
        else
            return -1;
    }            
}

// </editor-fold>

