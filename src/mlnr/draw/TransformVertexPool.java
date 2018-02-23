/*
 * TransformVertexPool.java
 *
 * Created on April 27, 2007, 12:56 PM
 *
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;
import mlnr.util.InterfaceUndoItem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** This class is used to contain a pool Vertices. There is no guarantees made about how
 * the Vertices should be in this pool, only that it contains a list of Vertices. Also can contain vertices from beizers which makes
 * it a real pain.
 * @author Robert Molnar 2
 */
public class TransformVertexPool extends AbstractPool {
        
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of TransformVertexPool */
    public TransformVertexPool() {
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Serialize Methods ">

    /** This will load the version 1.0 of RXML file.
     * @param eVertexList is the element for the vertexList in the RXML file.
     */
    void loadVersion10(Element eVertexList) throws Exception {
        NodeList nList = eVertexList.getElementsByTagName("vertex");
        int length = nList.getLength();
        for (int i=0; i < length; i++) {
            Element eVertex = (Element)nList.item(i);
            restore(TransformVertex.loadVersion10(eVertex));
        }
    }

    /** This will load the version 2.0 of RXML file.
     * @param eVertexPool is the element for the vertexPool in the RXML file.
     */
    void loadVersion20(Element eVertexPool) throws Exception {
        NodeList nList = eVertexPool.getElementsByTagName("vertex");
        int length = nList.getLength();
        for (int i=0; i < length; i++) {
            Element eVertex = (Element)nList.item(i);
            restore(TransformVertex.loadVersion20(eVertex));
        }
    }

    /** This will write out the TransformVertexPool information.
     */
    void write(PrintWriter out) {
        out.println("        <vertexPool>");
        for (Iterator<TransformVertex> itr=values().iterator(); itr.hasNext(); )
            itr.next().write(out);
        out.println("        </vertexPool>");
    }
    
    // </editor-fold>        
    
    // <editor-fold defaultstate="collapsed" desc=" Get Methods ">
    
    /** This will get the vertex.
     * @param vertexId is the vertex id to get from this vertex pool.
     * @return the vertex that matches the vertex id.
     * @exception IllegalArgumentException Id[] does not exist in object pool.
     */
    public TransformVertex get(int vertexId) {
        return (TransformVertex)super.get(vertexId);
    }
    
    /** This will get the TransformVertex at the point fpt that is within the vertex space. Do not get bezier control vertices.
     * @param fpt is the point to search for a TransformVertex.
     * @return the TransformVertex at the point fpt which is within the vertex's space. return
     * null if it didn't find any at the point.
     */
    public TransformVertex getTransformVertexWithinVertexSpace(FPointType fpt) {
        TransformVertex vSearchFor = new TransformVertex(fpt);
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            TransformVertex v = (TransformVertex)itr.next();
            if (!v.isControlVertex() && v.isVertexSpace(vSearchFor))
                return v;
        }
        
        return null;
    }
    
    /** This will get the point from the vertex which 'fpt' is within the vertex's proximity.    
     * @param fpt is the point used to search for a vertex.
     * @return the vertex's point which 'fpt' is within the vertex's proximity, or null.
     */
    public FPointType getPoint(FPointType fpt) {
        for (Iterator itr = values().iterator(); itr.hasNext(); ) {
            TransformVertex vertex = (TransformVertex)itr.next();
            
            if (!vertex.isControlVertex() && vertex.isVertexSpace(fpt))
                return vertex.getPoint();
        }
        
        return null;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Add Methods ">
    
    /** This will add a new vertex to this TransformVertexPool.
     * @param v is the vertex to be added to this TransformVertexPool.
     * @return an undo operation.
     */
    public InterfaceUndoItem add(TransformVertex v) {
        super.add(v);
        return new UndoItemNewVertex(v);
    }

    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc=" Contain/Search Methods">

    /** This will see if the vertex 'v' is in this TransformVertexPool. Uses memory location to see if it does exist therefore it must be 
     * physically present in this pool.
     * @param v is the vertex to see if it exists in this TransformVertexPool.
     * @return true if it does exist, else false it does not.
     */
    public boolean contains(TransformVertex v) {
        for (Iterator<TransformVertex> itr = values().iterator(); itr.hasNext(); ) {
            TransformVertex vertex = itr.next();
            
            if (v == vertex)
                return true;
        }
        
        return false;
    }
    
    /** This will get a list of TransformVertex from this pool that match the value of the flag. 
     *  <br> NOTE:: Control beziers are NOT included in this search.
     * @param flag is used to search for TransformVertex of a certain type. See the 
     * TransformLinePool.SEARCH_* flag. They can be OR'd together for a more specific search.
     * @return a list of TransformVertex which match the flag. Can be empty if none are found.
     */
    public LinkedList<TransformVertex> search(int flag) {
        LinkedList<TransformVertex> ltVertices = new LinkedList();
        
        for (Iterator<TransformVertex> itr = values().iterator(); itr.hasNext(); ) {
            TransformVertex vertex = itr.next();            
            if (!vertex.isControlVertex() && vertex.is(flag))
                ltVertices.add(vertex);
        }
        
        return ltVertices;
    }
    
    /** This will search for vertices in this pool. 
     *  <br> NOTE:: Control beziers are NOT included in this search.
     * @param fpt is the point used to search for. 
     * @return the a list of vertices which this point is within it's vertex space, or null if none are found.
     */
    public LinkedList<TransformVertex> searchVertexSpace(FPointType fpt) {
        LinkedList ltVertices = new LinkedList();
        
        // Get all vertices within the vertex space.
        for (Iterator<TransformVertex> itr = values().iterator(); itr.hasNext(); ) {
            TransformVertex tVertex = itr.next();
            if (!tVertex.isControlVertex() && tVertex.isVertexSpace(fpt))
                ltVertices.add(tVertex);
        }
        
        return ltVertices;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Add Vertex ">
    
    /** This will undo/redo a new vertex created.
     */
    class UndoItemNewVertex implements InterfaceUndoItem {
        TransformVertex vertex;
       
        /** @param vertexId is the vertex's id.
         */
        UndoItemNewVertex(TransformVertex v) {
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
            return "{TransformVertexPool.UndoItemNewVertex Vertex[" + vertex + "]}";
        }
    }
    
    // </editor-fold>
    
}
