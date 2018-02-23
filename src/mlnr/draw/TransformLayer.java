/*
 * TransformLayer.java
 *
 * Created on August 3, 2007, 2:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;

/**
 *
 * @author Robert Molnar 2
 */
public class TransformLayer implements InterfacePoolObject {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    /** Indicates this layer does not point to a DrawingLayer. */
    public static final int LAYERTO_NOPOINT = -1;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    private int id;
    
    /** This is the layer which this TransformLayer was created from. It is only used for selecting operations. */
    private int layerTo;
    
    /** This is true if the layer's lines and vertices point to lines and vertices in a DrawingLayer, else false they are not. */
    private boolean completelyAttached = false;
    
    /** Contains the TransformGraphs. */
    private TransformGraphPool tGraphPool;
    
    /** This is the name of the layer. */
    private String name;
    
    /** This is the color outline of the layer. */
    private Color color;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructors/Duplicate Methods ">
    
    /** Creates a new instance of TransformLayer */
    public TransformLayer() {
        tGraphPool = new TransformGraphPool();
        layerTo = LAYERTO_NOPOINT;
        
        // Default values.
        name = "Untitled";
        color = Color.BLACK;
    }
    
    /** This will create a new instance of TransformLayer
     * @param tGraphPool is the graph pool for this layer.
     * @param layerTo is the DrawingLayer which was used to create this TransformLayer.
     * @param name is the name of the DrawingLayer.
     * @param color is the name of the DrawingLayer.
     */
    public TransformLayer(TransformGraphPool tGraphPool, int layerTo, String name, Color color) {
        this.tGraphPool = tGraphPool;
        this.layerTo = layerTo;
        this.name = name;
        this.color = color;
    }
    
    /** This will create a duplicate of this TransformLayer. If this TransformLayer was
     * created from selected items from the DrawingDesign then the duplicated
     * TransformLayer will not have those links. HOWEVER, it will keep the link between
     *  layers. So that if this TransformLayer is actually from a DrawingLayer it will then
     *  keep the layer's point to id.
     * @return a TransformLayer that is a duplicate of this TransformLayer but without
     * the layer point to's and the line point to's.
     */
    public TransformLayer duplicate() {
        TransformGraphPool tNew = new TransformGraphPool();
        
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); ) {
            TransformGraph tGraph = itr.next();
            tNew.add(tGraph.duplicate());
        }
        
        return new TransformLayer(tNew, layerTo, name, color);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw/Status/Get/To Methods ">
    
    /** This will draw the items in this TransformDesign container.
     */
    public void draw(Graphics2D g2d, boolean erase) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().draw(g2d, erase);
    }
    
    /** This will filter the point. If it is close to a vertex than it will change the point to that vertex position. It
     * will search all layers to filter the point.
     * @param fpt is the point to be filtered. This will modify the point if need be.
     */
    public void filterPoint(FPointType fpt) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().filterPoint(fpt);
    }    

    /** This will get the bezier information by getting the first Bezier within the r.
     * @param r is the area to get the bezier from.
     * @return the bezier information of the bezier found in the r.
     * @throws IllegalStateException no bezier exist in the area r.
     */
    public BezierInfo getBezierInfo(Rectangle2D.Float r) {
        return tGraphPool.getFirst().getBezierInfo(r);
    }
    
    /** @return The bounds of the Layer by using the measurements of the graphs, else null.
     */
    public Rectangle2D.Float getBounds2D() {
        if (tGraphPool.size() == 0)
            return null;
        
        // Grow the rectangle.
        Rectangle2D.Float fRectangle = null;
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); ) {
            TransformGraph g = itr.next();
            
            // DrawingGraph doesn't have a bounds.
            if (g.isEmpty())
                continue;
            
            Rectangle2D.Float fRect = g.getBounds2D();
            if (fRect == null)
                continue;
            
            // Get the first graph not empty.
            if (fRectangle == null) {
                fRectangle = fRect;
                continue;
            }
            
            // Grow the rectangle.
            fRectangle.add(fRect);
        }
        
        return fRectangle;
    }

    /** @return color of this TransformLayer.
     */
    Color getColor() {
        return color;
    }
            
    /** This will get the count of graphs in this layer.
     * @return the number of graphs in this layer.
     */
    int getGraphCount() {
        return tGraphPool.size();
    }
    
    /** @return the DrawingLayer which this TransformLayer points to, else LAYERTO_NOPOINT.
     */
    int getLayerPointTo() {
        return layerTo;
    }
    
    /** @return the number of lines in this TransformLayer.
     */
    public int getLineCount() {
        int count = 0;
        
        for (Iterator<TransformGraph> itr =tGraphPool.values().iterator(); itr.hasNext(); ) {
            TransformGraph graph = itr.next();
            count += graph.getLineCount();
        }
        
        return count;
    }    
    
    /** @return the name of this layer.
     */
    String getName() {
        return name;
    }
    
    /** @return true if this TransformLayer's lines are connected to a DrawingLayer, else false they are not. Returns false even if this TransformLayer is
     *  pointing to another DrawingLayer.
     */
    public boolean isCompletelyAttached() {
        return completelyAttached;
    }
    
    /** @param connected is true if all lines in this layer are connected to a DrawingLayer, else false they are not.
     */
    public void setCompletelyConnected(boolean connected) {
        this.completelyAttached = connected;
    }
    
    /** This will set all Vertices to moveable.
     */
    public void setAllTransformable() {
        for (Iterator<TransformGraph> itr =tGraphPool.values().iterator(); itr.hasNext(); ) {
            TransformGraph graph = itr.next();
            graph.setAllTransformable(true);
        }
    }
    
    /** This will set all bezier control points as transmoveable.
     * @param transformable is the value which the bezier control points are to be set as.
     */
    public void setBeizerControls(boolean transformable) {
        for (Iterator<TransformGraph> itr =tGraphPool.values().iterator(); itr.hasNext(); ) {
            TransformGraph graph = itr.next();
            graph.setBeizerControls(transformable);
        }
    }
    
    /** @return a list of TransformGraph from this layer.
     */
    public LinkedList<TransformGraph> toList() {
        return tGraphPool.toList();
    }
    
    // </editor-fold>
        
    // <editor-fold defaultstate="collapsed" desc=" Operational Methods ">

    /** This will add the point to the selected line (must contain only one line selected).
     * @param fpt is the point which a vertex will be placed at on the line/curve/bezier curve
     * and then two lines/curves/beziers will replace that selected line.
     * @throws IllegalStateException No graphs exist in this TransformLayer
     */
    public void addPoint(FPointType fpt) {
        if (tGraphPool.getFirst() == null)
            throw new IllegalStateException("No graphs exist in this TransformLayer.");
        tGraphPool.getFirst().addPoint(fpt);
    }

    /** This will connect two RMolnar curves at the point fpt. This will attempt to perform the
     * connecting, however, there is not gurantee that two curves connect to the point at fpt. 
     * Also if there exists more than two non-connected RMolnar curves at that point then it will not
     * be able to connect them.
     * @param fpt is the point where the RMolnar curves need to be connected at.
     * @return TransformDesign.CONNECT_OK or TransformDesign.CONNECT_TOO_MANY_CURVES or TransformDesign.CONNECT_NOTHING_CONNECTED.
     * @throws IllegalStateException More than one layer.
     */
    public int connect(FPointType fpt) {
        if (tGraphPool.size() != 1)
            throw new IllegalStateException("Must only be one graph in this TransformLayer: "  + tGraphPool.size());
        return tGraphPool.getFirst().connect(fpt);
    }
    
    /** This will disconnect all RMolnar curves at the point fpt. 
     * @param fpt is the point where the RMolnar curves need to be disconnected at.
     * @throws IllegalStateException More than one graph.
     */
    public void disconnect(FPointType fpt) {
        if (tGraphPool.size() != 1)
            throw new IllegalStateException("Must only be one layer in this TransformLayer: "  + tGraphPool.size());
        tGraphPool.getFirst().disconnect(fpt);
    }

    /** This will perform the pulling of the line apart and set it up to allow the fpt to be moveable.
     * @param fpt is the point where the user clicked on the line which needs to be pulled apart.
     * @throws IllegalStateException More than one graph.
     */
    public void pullLineApart(FPointType fpt) {
        if (tGraphPool.size() != 1)
            throw new IllegalStateException("Must only be one layer in this TransformLayer: "  + tGraphPool.size());
        tGraphPool.getFirst().pullLineApart(fpt);
    }
    
    /** This will update the bezier where the end points match this bezierInfo's ones.
     * @param the bezierInfo information about the bezier curve in this TransformDesign. It will only update
     * the control points.
     * @throws IllegalStateException bezier curve does not exist in pool.
     */
    public void updateBezierInfo(BezierInfo bezierInfo) {
        tGraphPool.getFirst().updateBezierInfo(bezierInfo);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Calculate And Transform Methods ">
      
    /** This will perform per-calculations before performing transformations.
     * @param fptCenter is the point to perform the calculations around.
     */
    public void calculate(FPointType fptCenter) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); ) 
            itr.next().calculate(fptCenter);
    }
    
    /** This will finalize the movements of the vertices and lines.
     */
    public void finalizeMovement() {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); ) {
            itr.next().finalizeMovement();
        }
    }
    
    /** This will rotate the design by the radOffet. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param radOffset is the offset radians to rotate.
     */
    public void rotate(FPointType fptCenter, float radOffset) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().rotate(fptCenter, radOffset);
    }
    
    /** This will move the design by the xOffset, yOffset. No pre-compute needed.
     * @param xOffset is the offset in the x direction to move in.
     * @param yOffset is the offset in the x direction to move in.
     */
    public void translate(float xOffset, float yOffset) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().translate(xOffset, yOffset);
    }
    
    /** This will mirror the design. No pre-compute needed.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param mirrorType can be MIRROR_HORIZONTAL, MIRROR_VERTICAL, MIRROR_DEGREE.
     * @param line2D is the line which the vertex will be mirrored from.
     * @param rad is the degree to mirror at, only used for MIRROR_DEGREE.
     */
    public void mirror(FPointType fptCenter, int mirrorType, Line2D.Float line2D, float rad) {
        float x1 = fptCenter.x - (float)Math.cos(rad) * 500.0f;
        float y1 = fptCenter.y - (float)Math.sin(rad) * 500.0f;
        float x2 = fptCenter.x + (float)Math.cos(rad) * 500.0f;
        float y2 = fptCenter.y + (float)Math.sin(rad) * 500.0f;
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().mirror(fptCenter, mirrorType, new Line2D.Float(x1, y1, x2, y2), rad);
    }
    
    /** This will scale the design by xScale, yScale. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param xScale is x scale (1.0 is no change in x size).
     * @param yScale is y scale (1.0 is no change in y size).
     */
    public void resize(FPointType fptCenter, float xScale, float yScale) {
        for (Iterator<TransformGraph> itr = tGraphPool.values().iterator(); itr.hasNext(); )
            itr.next().resize(fptCenter, xScale, yScale);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Interface InterfacePoolObject">
    
    public int getId() {
        return id;
    }
    
    public int getZDepth() {
        return 1;
    }
    
    public void setZDepth(int zDepth) {
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int compareTo(Object o) {
        return 0;
    }
    
    // </editor-fold>
    
}
