/*
 * TransformDesign.java
 *
 * Created on April 13, 2007, 7:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.type.FPointType;

/** This class is used to transform lines, curves, and vertices.<br>
 * Use the selectItems() function to begin a TransformDesign from the selected items in a DrawingDesign.  <br>
 *
 * @author Robert Molnar 2
 */
public class TransformDesign {
    
    // <editor-fold defaultstate="collapsed" desc=" Static Fields ">
    
    public static final int MIRROR_HORIZONTAL = 1;
    public static final int MIRROR_VERTICAL = 2;
    public static final int MIRROR_DEGREE = 3;
    
    /** This is the rectangle used to select everything. */
    private static final Rectangle2D.Float RECTANLE_MAX = new Rectangle2D.Float(-2000000000.0f, -2000000000.0f, 4000000000.0f, 4000000000.0f);

    /** Return code for connect(): Nothing connected/disconnected. */
    public static final int CONNECT_NOTHING_CONNECTED = 3;
    /** Return code for connect(): Too many curves at the point. */
    public static final int CONNECT_TOO_MANY_CURVES = 2;
    /** REturn code for connect(): Performed connection/disconnection. */
    public static final int CONNECT_OK = 1;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** Contains the transform layers for the design. */
    private TransformLayerPool tLayerPool;
    
    /** This is the point which the transforms will start from. */
    private FPointType fptStart = new FPointType();
    
    /** This is the bounds of the design. */
    private Rectangle2D.Float fRectBound;
    
    /** This is the radian for the rotating. */
    private float rotateRadian = 0.0f;
    
    /** This is the x scale for resizing. */
    private float xScale = 1.0f;
    
    /** This is the y scale for resizing. */
    private float yScale = 1.0f;
    
    /** This is the point which was used last for calculating the point to rotate, resize, mirror around. */
    private FPointType fptCenter = new FPointType();
    
    /** This is true if any items have moved in this design or false it has not moved any items yet.  */
    private boolean moved = false;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor and Duplicate Methods ">
    
    /** Creates a new instance of TransformDesign */
    public TransformDesign() {
        tLayerPool = new TransformLayerPool();
    }
    
    /** This will create a new TransformDesign with the TransformLayerPool.
     * @param tLayerPool is the layer pool that will be used by this design.
     */
    public TransformDesign(TransformLayerPool tLayerPool) {
        this.tLayerPool = tLayerPool;
    }
    
    /** This will create a duplicate of this TransformDesign. If this TransformDesign was
     * created from selected items from the DrawingDesign then the duplicated
     * TransformDesign will not have those links.
     * @return a TransformDesign that is aduplicate of this TransformDesign but without
     * the layer point to's and the line point to's.
     */
    public TransformDesign duplicate() {
        TransformLayerPool tNew = new TransformLayerPool();
        
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); ) {
            TransformLayer tLayer = itr.next();
            tNew.add(tLayer.duplicate());
        }
        
        return new TransformDesign(tNew);
    }
    
    /** This will create a TransformDesign from the DrawingDesign. All items in the TransformDesign 
     * will point back to the DrawingDesign.
     * <br> WARNING: This will modify the selected value of each line and vertex. After this function 
     * is called all lines and vertices will be set to selected of false.
     * @param design is the DrawingDesign used to create a clone of except that it will be a TransformDesign.
     */
    public static TransformDesign valueOf(DrawingDesign design) {
        // Make sure all are selected.
        design.setLinesAndVerticesSelected(true);
        
        // Create the TransformDesign from the DrawingDesign and de-attach all of its layers, lines, etc..
        TransformDesign tDesign = design.getSelectedItems();
        
        // Finished, now select to false and show the design again.
        design.setLinesAndVerticesSelected(false);
        design.setLinesAndVerticesVisible(true);
        
        
        return tDesign;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Selected Methods ">
    
//    /** This will create a TransformDesign from the DrawingPad.
//     * @param drawingPad is the DrawingPad to get the DrawingDesign from.
//     * @return a TransformDesign of the selected items in the DrawingDesign or null if
//     * there are no selected items.
//     */
//    static public TransformDesign selectItems(DrawingPad drawingPad) {
//        if (drawingPad.getDesign().isSelectedItems() == false)
//            return null;
//        return drawingPad.getDesign().getSelectedItems();
//    }
//    
//    /** This will select something from the drawing. A.k.a. set it selected in the DrawingDesign.
//     * @param drawingPad is the DrawingPad which is used to draw on.
//     * @param deletePanel contains the user selectable options.
//     * @param fBounds is the rectangle that will be used to select items.
//     */
//    static public void select(DrawingPad drawingPad, DeletePanel deletePanel, Rectangle2D.Float fBounds) {
//        // Current layer?
//        boolean currLayer = deletePanel.isCurrentLayer();
//        
//        if (deletePanel.isSelectVertex())
//            drawingPad.getDesign().selectVertices(fBounds, true, currLayer);
//        else if (deletePanel.isSelectLine())
//            drawingPad.getDesign().selectLines(fBounds, true, currLayer);
//        else if (deletePanel.isSelectObject())
//            drawingPad.getDesign().selectGraphs(fBounds, true, currLayer);
//        else
//            throw new IllegalArgumentException("Unknown select type.");
//    }
//    
//    /** This will select something from the drawing. A.k.a. set it selected in the DrawingDesign.
//     * @param drawingPad is the DrawingPad which is used to draw on.
//     * @param selectPanel contains the user selectable options.
//     * @param fBounds is the rectangle that will be used to select items.
//     * @param oneItem is true if it should get one item or multiple items.
//     */
//    static public void select(DrawingPad drawingPad, SelectPanel selectPanel, Rectangle2D.Float fBounds, boolean oneItem) {
//        // Current layer?
//        boolean bCurrentLayer = selectPanel.isCurrentLayer();
//        
//        // Get the selected design.
//        if (selectPanel.isSelectAllItems())
//            drawingPad.getDesign().selectGraphs(RECTANLE_MAX, false, bCurrentLayer);
//        else if (selectPanel.isSelectGraph())
//            drawingPad.getDesign().selectGraphs(fBounds, oneItem, bCurrentLayer);
//        else if (selectPanel.isSelectLine())
//            drawingPad.getDesign().selectLines(fBounds, oneItem, bCurrentLayer);
//        else if (selectPanel.isSelectPoint())
//            drawingPad.getDesign().selectVertices(fBounds, oneItem, bCurrentLayer);
//        else
//            throw new IllegalArgumentException("Unknown select type.");
//    }
//    
//    /** This will deselect the selected items in the rectangle.
//     * @param drawingPad is the DrawingPad which is used to draw on.
//     * @param selectPanel contains the user selectable options.
//     * @param fBounds is the rectangle that will be used to select items.
//     */
//    static public void deselect(DrawingPad drawingPad, SelectPanel selectPanel, Rectangle2D.Float fBounds) {
//        // Current layer.
//        boolean bCurrentLayer = selectPanel.isCurrentLayer();
//        
//        // Get the selected design.
//        if (selectPanel.isSelectAllItems())
//            drawingPad.getDesign().setLinesAndVerticesSelected(false);
//        else if (selectPanel.isSelectGraph())
//            drawingPad.getDesign().deselectGraphs(fBounds, bCurrentLayer);
//        else if (selectPanel.isSelectLine())
//            drawingPad.getDesign().deselectLines(fBounds, bCurrentLayer);
//        else if (selectPanel.isSelectPoint())
//            drawingPad.getDesign().deselectVertices(fBounds, bCurrentLayer);
//        else
//            throw new IllegalArgumentException("ToolSimpleMover::deselect() Unknown select type.");
//    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Grab Method ">
    
    /** This will grab a line from the graph and disconnect it from the vertex. Depends on how
     * close the grab is to the ends of the line to determine which end will be disconnected.
     * @param drawingPad is the DrawingPad which is used to draw on.
     * @param fptNewPoint is the position to place the new point.
     * @param r is the rectangle to select the line.
     * @param currLayerOnly is true if it should select from the current layer only.
     * @return a new DesignTransform if it grabbed a line, else null.
     */
//    public static TransformDesign grabLine(DrawingPad drawingPad, FPointType fptNewPoint, java.awt.geom.Rectangle2D.Float r, boolean currLayerOnly) {
//        throw new UnsupportedOperationException("TODO");
////        DrawingDesign dSelect = drawingPad.getDesign().grabLine(fptNewPoint, r, currLayerOnly);
////
////        // Create new DesignTransform.
////        if (dSelect == null)
////            return null;
////        return new DesignTransform(dSelect);
//    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw/Status/IsGet/To Methods ">
   
    /** This will draw the items in this TransformDesign container.
     */
    public void draw(Graphics2D g2d, boolean erase) {
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().draw(g2d, erase);
    }
    
    /** This will filter the point. If it is close to a vertex than it will change the point to that vertex position. It
     * will search all layers to filter the point.
     * @param fpt is the point to be filtered. This will modify the point if need be.
     */
    public void filterPoint(FPointType fpt) {
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().filterPoint(fpt);
    }    

    /** This will get the bezier information by getting the first Bezier within the r.
     * @param r is the area to get the bezier from.
     * @return the bezier information of the bezier found in the r.
     * @throws IllegalStateException no bezier exist in the area r.
     */
    public BezierInfo getBezierInfo(Rectangle2D.Float r) {
        return tLayerPool.getFirst().getBezierInfo(r);
    }
    
    /** @return The bounds of this TransformDesign by using the measurements of the LayerPool (the bounds of all lines). Can be null.
     */
    public Rectangle2D.Float getBounds2D() {
        if (tLayerPool.size() == 0)
            return null;
        
        // Grow the rectangle.
        Rectangle2D.Float fRectangle = null;
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); ) {
            Rectangle2D.Float fRect = itr.next().getBounds2D();
            if (fRect == null)
                continue;
            
            if (fRectangle == null)
                fRectangle = fRect;
            else
                fRectangle.add(fRect);
        }
        
        return fRectangle;
    }

    /** @return the number of layers in this TransformDesign.
     */
    public int getLayerCount() {
        return tLayerPool.size();
    }
    
    /** @return the number of lines in this TransformDesign.
     */
    public int getLineCount() {
        int count = 0;
        
        for (Iterator<TransformLayer> itr =tLayerPool.values().iterator(); itr.hasNext(); ) {
            TransformLayer layer = itr.next();
            count += layer.getLineCount();
        }
        
        return count;
    }
    
    /** @return true if any of the items have moved in this TransformDesign, else false they have not.
     */
    public boolean hasMoved() {
        return moved;
    }

    /** @return true if this TransformDesign is attached to a DrawingDesign, in that this TransformDesign was created by
     * the selected items in a DrawingDesign AND that its lines are pointing to another DrawingLayer. Will return false even
     *  if the TransformDesign is pointing to another DrawingDesign but does not have it's lines as pointing to another DrawingLayer.
     */
    public boolean isCompletelyAttached() {
        TransformLayer tLayer = tLayerPool.getFirst();
        if (tLayer == null)
            return false;
        else if (tLayer.isCompletelyAttached() == false)
            return false;
        return true;
    }
    
    /** This will set all Vertices to moveable.
     */
    public void setAllTransformable() {
        for (Iterator<TransformLayer> itr =tLayerPool.values().iterator(); itr.hasNext(); ) {
            TransformLayer layer = itr.next();
            layer.setAllTransformable();
        }
    }
    
    /** This will set the TransformDesign as it was moved or not.
     *  @param moved is true if the TransformDesign moved flag should be set, which means that it has moved since it was created.
     */
    public void setMoved(boolean moved) {
        this.moved = moved;
    }
    
    /** This will set all bezier control points as transmoveable.
     * @param transformable is the value which the bezier control points are to be set as.
     */
    public void setBeizerControls(boolean transformable) {
        for (Iterator<TransformLayer> itr =tLayerPool.values().iterator(); itr.hasNext(); ) {
            TransformLayer layer = itr.next();
            layer.setBeizerControls(transformable);
        }
    }
    
    /** @return the layers as a list of layers.
     */
    LinkedList<TransformLayer> toList() {
        return tLayerPool.toList();
    }
    
    // </editor-fold>
            
    // <editor-fold defaultstate="collapsed" desc=" Operational Methods ">

    /** This will add the point to the selected line (must contain only one line selected).
     * @param fpt is the point which a vertex will be placed at on the line/curve/bezier curve
     * and then two lines/curves/beziers will replace that selected line.
     * @throws IllegalStateException No layers exist in this TransformDesign
     */
    public void addPoint(FPointType fpt) {
        moved = true;
        if (tLayerPool.getFirst() == null)
            throw new IllegalStateException("No layers exist in this TransformDesign.");
        tLayerPool.getFirst().addPoint(fpt);
    }

    /** This will connect two RMolnar curves at the point fpt. This will attempt to perform the
     * connecting, however, there is not gurantee that two curves connect to the point at fpt. 
     * Also if there exists more than two non-connected RMolnar curves at that point then it will not
     * be able to connect them.
     * <br> Note that this will throw exceptions and also a graph exception could be thrown check
     * calling functions.
     * @param fpt is the point where the RMolnar curves need to be connected at.
     * @return CONNECT_OK or CONNECT_TOO_MANY_CURVES or CONNECT_NOTHING_CONNECTED.
     * @throws IllegalStateException More than one layer.
     */
    public int connect(FPointType fpt) {
        moved = true;
        if (tLayerPool.size() != 1)
            throw new IllegalStateException("Must only be one layer in this TransformDesign: "  + tLayerPool.size());
        return tLayerPool.getFirst().connect(fpt);
    }
    
    /**
     */
    public void deattach() {
        
    }

    
    /** This will disconnect all RMolnar curves at the point fpt. 
     * <br> Note that this will throw exceptions and also a graph exception could be thrown check
     * calling functions.
     * @param fpt is the point where the RMolnar curves need to be disconnected at.
     * @throws IllegalStateException More than one layer.
     */
    public void disconnect(FPointType fpt) {
        moved = true;
        if (tLayerPool.size() != 1)
            throw new IllegalStateException("Must only be one layer in this TransformDesign: "  + tLayerPool.size());
        tLayerPool.getFirst().disconnect(fpt);
    }

    /** This will perform the pulling of the line apart and set it up to allow the fpt to be moveable.
     * @param fpt is the point where the user clicked on the line which needs to be pulled apart.
     * @throws IllegalStateException More than one layer.
     */
    public void pullLineApart(FPointType fpt) {
        moved = true;
        if (tLayerPool.size() != 1)
            throw new IllegalStateException("Must only be one layer in this TransformDesign: "  + tLayerPool.size());
        tLayerPool.getFirst().pullLineApart(fpt);
    }
    
    /** This will update the bezier where the end points match this bezierInfo's ones.
     * @param the bezierInfo information about the bezier curve in this TransformDesign. It will only update
     * the control points.
     * @throws IllegalStateException bezier curve does not exist in pool.
     */
    public void updateBezierInfo(BezierInfo bezierInfo) {
        moved = true;
        tLayerPool.getFirst().updateBezierInfo(bezierInfo);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Transformation Methods ">
    
    /** This will calculate the design to the center of the design.
     * @return the calculated position.
     */
    public FPointType calculateCenter() {
        fRectBound = getBounds2D();
        FPointType fptCenter = new FPointType();
        fptCenter.x = fRectBound.x + fRectBound.width / 2.0f;
        fptCenter.y = fRectBound.y + fRectBound.height / 2.0f;
        calculate(fptCenter);
        
        rotateRadian = 0.0f;
        
        return fptCenter;
    }
    
    /** this will calculate the design to the upper-left of the design.
     * @return the calculated position.
     */
    public FPointType calculateUpperLeft() {
        fRectBound = getBounds2D();
        FPointType fptUpperLeft = new FPointType(fRectBound.x, fRectBound.y);
        calculate(fptUpperLeft);
        
        rotateRadian = 0.0f;
        
        return fptUpperLeft;
    }
    
    /** This will calculate the vertices in the design from the position fptCenter.
     */
    public void calculate(FPointType fptCenter) {
        this.fptCenter.x = fptCenter.x;
        this.fptCenter.y = fptCenter.y;
        
        rotateRadian = 0.0f;
        fRectBound = getBounds2D();
        
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().calculate(fptCenter);
    }
    
    /** This will finalize the movements of the vertices and lines.
     */
    public void finalizeMovement() {
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); ) {
            itr.next().finalizeMovement();
        }
    }

    /** This will rotate the design by the radOffet. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param radOffset is the offset radians to rotate.
     */
    public void rotate(float radOffset) {
        moved = true;
        
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().rotate(fptCenter, radOffset);
    }
    
    /** This will move the design by the xOffset, yOffset. No pre-compute needed.
     * @param xOffset is the offset in the x direction to move in.
     * @param yOffset is the offset in the x direction to move in.
     */
    public void translate(float xOffset, float yOffset) {
        moved = true;
        
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().translate(xOffset, yOffset);
    }
    
    /** This will mirror the design. No pre-compute needed.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param mirrorType can be MIRROR_HORIZONTAL, MIRROR_VERTICAL, MIRROR_DEGREE.
     * @param line2D is the line which the vertex will be mirrored from.
     * @param rad is the degree to mirror at, only used for MIRROR_DEGREE.
     */
    public void mirror(int mirrorType, float rad) {
        moved = true;
        
        float x1 = fptCenter.x - (float)Math.cos(rad) * 500.0f;
        float y1 = fptCenter.y - (float)Math.sin(rad) * 500.0f;
        float x2 = fptCenter.x + (float)Math.cos(rad) * 500.0f;
        float y2 = fptCenter.y + (float)Math.sin(rad) * 500.0f;
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().mirror(fptCenter, mirrorType, new Line2D.Float(x1, y1, x2, y2), rad);
    }
    
    /** This will scale the design by xScale, yScale. Need to precompute by calling calculate.
     * @param fptCenter is the position where each vertex should be calculated from.
     * @param xScale is x scale (1.0 is no change in x size).
     * @param yScale is y scale (1.0 is no change in y size).
     */
    public void resize(float xScale, float yScale) {
        moved = true;
        
        for (Iterator<TransformLayer> itr = tLayerPool.values().iterator(); itr.hasNext(); )
            itr.next().resize(fptCenter, xScale, yScale);
    }
    
    /** @return the calculated position for the design.
     */
    public FPointType getCalculate() {
        return fptCenter;
    }
    
    // These are functions used as an interface for this class to perform the needed operations.
    
    /** This should be called at the start of the scaling tool. It will set the scales to 1.0f, 1.0f.
     */
    public void beginScaling() {
        xScale = 1.0f;
        yScale = 1.0f;
    }
    
    /** This is the starting position where the transformations will start from.
     * @param fpt is the starting position.
     */
    public void setBeginPosition(FPointType fpt) {
        fptStart.x = fpt.x;
        fptStart.y = fpt.y;
    }
    
    /** This will translate the design.
     * @param fpt is the position to translate the design to.
     * @param centerOrUpperLeft is true if should translate the design at the center of it, or false it should translate the design
     * at the upper left.
     */
    public void onTranslateAbsolute(FPointType fpt, boolean centerOrUpperLeft) {
        FPointType fDesign;
        if (centerOrUpperLeft)
            fDesign = calculateCenter();
        else
            fDesign = calculateUpperLeft();
        
        // Translate to the position fpt.
        float xOffset = fpt.x - fDesign.x;
        float yOffset = fpt.y - fDesign.y;
        translate(xOffset, yOffset);
    }
    
    /** This will translate the design.
     * @param fptPos is the current mouse position in the design.
     */
    public void onTranslateAnyDirection(FPointType fptPos) {
        float xOffset = fptPos.x - fptStart.x;
        float yOffset = fptPos.y - fptStart.y;
        translate(xOffset, yOffset);
    }
    
    /** This will translate the design.
     * @param xPos is the current x mouse position in the design.
     */
    public void onTranslateHorizontal(float xPos) {
        float xOffset = xPos - fptStart.x;
        translate(xOffset, 0);
    }
    
    /** This will translate the design.
     * @param yPos is the current y mouse position in the design.
     */
    public void onTranslateVertical(float yPos) {
        float yOffset = yPos - fptStart.y;
        translate(0, yOffset);
    }
    
    /** This will translate the design.
     * @param fptPos is the current mouse position in the design.
     * @param rad is the degree which it should move in direction.
     */
    public void onTranslateDegree(FPointType fptPos, float rad) {
        // Get the distance from fptStart to fptPos.
        float distance = (fptPos.x - fptStart.x);
        
        // Get the offset now.
        float xTemp = distance * (float)Math.cos(rad);
        float yTemp = distance * (float)Math.sin(rad);
        
        translate(xTemp, yTemp);
    }
    
    /** This will rotate the design.
     * @param fptPos is the current mouse position in the design.
     * @param offset is the radian offset to add to the rotate.
     * @return the radian of rotation.
     */
    public float onRotate(FPointType fptPos, float offset) {
        FPointType fptCalculate = getCalculate();
        
        float radPos = calculateRadian(fptPos, fptCalculate);
        float radBegin = calculateRadian(fptStart, fptCalculate);
        
        rotateRadian = radPos - radBegin + offset;
        rotate(rotateRadian);
        return rotateRadian;
    }
    
    /** This will calculate the radian from fptCalculate to fptPos.
     * @param fptPos is the point to use to calculate the radian.
     * @param fptCalculate is the point to use to calculate the radian.
     * @return the radian between the two points.
     */
    private float calculateRadian(FPointType fptPos, FPointType fptCalculate) {
        // Need to get the radian of the triangle from fptCalculate to fptPos.
        float xDelta = fptPos.x - fptCalculate.x;
        float yDelta = fptPos.y - fptCalculate.y;
        
        float rad = 0.0f;
        if (xDelta == 0.0f) {
            if (yDelta > 0.0f)
                rad = (float)Math.PI / 2;
            else
                rad = ((float)Math.PI * 2) - ((float)Math.PI / 2);
        } else {
            rad = (float)Math.atan(yDelta / xDelta);
            if (xDelta < 0.0f)
                rad += (float)Math.PI;
        }
        
        return rad;
    }
    
    /** This will rotate the design.
     * @param rad is the degree to rotate the design to.
     */
    public void onRotate(float rad) {
        rotateRadian = rad;
        rotate(rotateRadian);
    }
    
    /** @return te rotate of the design.
     */
    public float getRadian() {
        return rotateRadian;
    }
    
    /** This will resize the design.
     * @param fptPos is the current mouse position in the design.
     * @param xOffsetScale is offset scale size in x.
     * @param yOffsetScale is offset scale size in y.
     */
    public void onResizeAnyDirectionUpperLeft(FPointType fptPos, float xOffsetScale, float yOffsetScale) {
        float xNewSizeOffset = fptPos.x - fptStart.x;
        float yNewSizeOffset = fptPos.y - fptStart.y;
        
        
        if (fRectBound.getWidth() == 0.0)
            xScale = 1.0f;
        else {
            xScale = 1.0f + xOffsetScale + (xNewSizeOffset / (float)fRectBound.getWidth());
        }
        
        if (fRectBound.getHeight() == 0.0)
            yScale = 1.0f;
        else {
            yScale = 1.0f + yOffsetScale + (yNewSizeOffset / (float)fRectBound.getHeight());
        }
        
        resize(xScale, yScale);
    }
    
    /** This will resize the design.
     * @param fptPos is the current mouse position in the design.
     * @param xOffsetScale is offset scale size in x.
     * @param yOffsetScale is offset scale size in y.
     */
    public void onResizeAnyDirection(FPointType fptPos, float xOffsetScale, float yOffsetScale) {
        float xNewSizeOffset = fptPos.x - fptStart.x;
        float yNewSizeOffset = fptPos.y - fptStart.y;
        
        
        if (fRectBound.getWidth() == 0.0)
            xScale = 1.0f;
        else {
            float radiusWidth = (float)fRectBound.getWidth() / 2.0f;
            xScale = 1.0f + xOffsetScale + (xNewSizeOffset / radiusWidth);
        }
        
        if (fRectBound.getHeight() == 0.0)
            yScale = 1.0f;
        else {
            float radiusHeight = (float)fRectBound.getHeight() / 2.0f;
            yScale = 1.0f + yOffsetScale + (yNewSizeOffset / radiusHeight);
        }
        
        resize(xScale, yScale);
    }
    
    /** This will resize the design.
     * @param xPos is the current x mouse position in the design.
     * @param xOffsetScale is offset scale size in x.
     */
    public void onResizeHorizontal(float xPos, float xOffsetScale) {
        float xNewSizeOffset = xPos - fptStart.x;
        
        if (fRectBound.getWidth() == 0.0)
            xScale = 1.0f;
        else {
            float radiusWidth = (float)fRectBound.getWidth() / 2.0f;
            xScale = 1.0f + xOffsetScale + (xNewSizeOffset / radiusWidth);
        }
        
        resize(xScale, yScale);
    }
    
    /** This will resize the design.
     * @param yPos is the current y mouse position in the design.
     * @param yOffsetScale is offset scale size in y.
     */
    public void onResizeVertical(float yPos, float yOffsetScale) {
        float yNewSizeOffset = yPos - fptStart.y;
        
        if (fRectBound.getHeight() == 0.0)
            yScale = 1.0f;
        else {
            float radiusHeight = (float)fRectBound.getHeight() / 2.0f;
            yScale = 1.0f + yOffsetScale + (yNewSizeOffset / radiusHeight);
        }
        
        resize(xScale, yScale);
    }
    
    /** This will resize the design.
     * @param fptPos is the current mouse position in the design.
     * @param xOffsetScale is offset scale size in x.
     * @param yOffsetScale is offset scale size in y.
     */
    public void onResizeUniform(FPointType fptPos, float xOffsetScale, float yOffsetScale) {
        float xNewSizeOffset = fptPos.x - fptStart.x;
        float yNewSizeOffset = fptPos.y - fptStart.y;
        
        if (fRectBound.getWidth() != 0.0){
            float radiusWidth = (float)fRectBound.getWidth() / 2.0f;
            xScale = 1.0f + xOffsetScale + (xNewSizeOffset / radiusWidth);
            yScale = xScale;
        } else if (fRectBound.getHeight() != 0.0) {
            float radiusHeight = (float)fRectBound.getHeight() / 2.0f;
            yScale = 1.0f + yOffsetScale + (yNewSizeOffset / radiusHeight);
            xScale = yScale;
        }
        
        resize(xScale, yScale);
    }
    
    /** This will resize the design.
     * @param fptPos is the current mouse position in the design.
     * @param xOffsetScale is offset scale size in x.
     * @param yOffsetScale is offset scale size in y.
     */
    public void onResizeUniformUpperLeft(FPointType fptPos, float xOffsetScale, float yOffsetScale) {
        float xNewSizeOffset = fptPos.x - fptStart.x;
        float yNewSizeOffset = fptPos.y - fptStart.y;
        
        if (fRectBound.getWidth() != 0.0){
            xScale = 1.0f + xOffsetScale + (xNewSizeOffset / (float)fRectBound.getWidth());
            yScale = xScale;
        } else if (fRectBound.getHeight() != 0.0) {
            yScale = 1.0f + yOffsetScale + (yNewSizeOffset / (float)fRectBound.getHeight());
            xScale = yScale;
        }
        
        resize(xScale, yScale);
    }
    
    /** @return This will get the resize xScale.
     */
    public float getXScale() {
        return xScale;
    }
    
    /** @return This will get the resize xScale.
     */
    public float getYScale() {
        return yScale;
    }
    
    /** This will resize the design.
     * @param xSize is the size in measurements the design should be.
     * @param ySize is the size in measurements the design should be.
     */
    public void onResizeAbsolute(float xSize, float ySize) {
        float xScale = xSize / (float)fRectBound.getWidth();
        float yScale = ySize / (float)fRectBound.getHeight();
        
        resize(xScale, yScale);
    }
    
    /** This will mirror the design vertically.
     */
    public void onMirrorHorizontal() {
        mirror(TransformDesign.MIRROR_HORIZONTAL, 0.0f);
    }
    
    /** This will mirror the design horizontal.
     */
    public void onMirrorVertical() {
        mirror(TransformDesign.MIRROR_VERTICAL, 0.0f);
    }
    
    /** This will mirror the design horizontal on a degree.
     * @param rad is the degree to mirror on.
     */
    public void onMirrorRadian(float rad) {
        mirror(TransformDesign.MIRROR_DEGREE, rad);
    }
    
    // </editor-fold>
    
}
