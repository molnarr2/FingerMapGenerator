/*
 * GeneralPathPool.java
 *
 * Created on July 24, 2006, 1:21 PM
 *
 */

package mlnr.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.IndexColorModel;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.JOptionPane;
import org.w3c.dom.*;
import mlnr.type.FPointType;
import mlnr.util.*;

/** This class is used to contain all GeneralPaths for a Layer.
 * @author Robert Molnar II
 */
public class GeneralPathPool {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** First one is the lowest z-depth, meaning that it will be drawn first so it will therefore
     * be the most likely one to be hiddened if another GeneralPath is draw after it. */
    LinkedList ltGeneralPath = new LinkedList();
    
    /** This is the layer pool which contains all the layers from the Design. */
    DrawingLayerPool lPool;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor ">
    
    /** Creates a new instance of GeneralPathPool 
     * @param lPool is the layer pool which contains all the layers from the Design.
     */
    public GeneralPathPool(DrawingLayerPool lPool) {
        this.lPool = lPool;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc=" Serialize Support ">
    
    /** This will load the version 2.0 of RXML file.
     * @param eGeneralPath is the element for the generalPathPool in the RXML file.
     * @param lPool is the LayerPool which these general paths are constructed from.
     */
    public void loadVersion20(Element eGeneralPath, DrawingLayerPool lPool) throws Exception {
        NodeList nList = eGeneralPath.getElementsByTagName("rmGeneralPath");
        int length = nList.getLength();
        for (int i=0; i < length; i++) {
            Element e = (Element)nList.item(i);
            RMGeneralPath rGeneralPath = RMGeneralPath.loadVersion20(e, lPool);
            add(rGeneralPath.getLayer(), rGeneralPath.getGeneralPath(), rGeneralPath.getColor());
        }
    }

    /** This will write out the GeneralPathPool.
     * @param lInfo is the layer to write out, or null if the entire design is to be written out.
     */
    void write(LayerInfo lInfo, PrintWriter out) {
        out.println("   <generalPathPool>");
        for (Iterator itr = ltGeneralPath.iterator(); itr.hasNext(); ) {
            RMGeneralPath gen = (RMGeneralPath)itr.next();
            
            //write out only the one for the layer.
            if (lInfo != null && gen.getLayer().getId() == lInfo.getId())
                gen.write(out);
            if (lInfo == null)
                gen.write(out);
        }
        out.println("   </generalPathPool>");
    }
    
    // </editor-fold>
    
    /** This will draw all GeneralPaths in this pool of collection.
     */
    public void draw(Graphics2D g2d) {
        Color c = g2d.getColor();
        for (Iterator itr = ltGeneralPath.iterator(); itr.hasNext(); ) {
            ((RMGeneralPath)itr.next()).draw(g2d);
        }
        g2d.setColor(c);
    }
    
    /** This is used to draw into a bitmap.
     * @param g2D is the graphics class.
     * @param lInfo is the layer to write out, or null if the entire design is to be written out.
     * @param changeColor is true if it should change color for the layers. If it is false then no general paths are filled in.
     */
    public void drawAllBitmap(Graphics2D g2d, LayerInfo lInfo, boolean changeColor) {
        Color c = g2d.getColor();
        for (Iterator itr = ltGeneralPath.iterator(); itr.hasNext(); ) {
            RMGeneralPath rmGeneralPath = (RMGeneralPath)itr.next();
            if (lInfo != null && rmGeneralPath.getLayer().getId() != lInfo.getId())
                continue;
            else
                rmGeneralPath.drawAllBitmap(g2d, changeColor);
        }
        g2d.setColor(c);
    }

    /** This will add a GeneralPath to this pool, however if it is the same as a general path already in the pool then it
     * will only update the color.
     * @param layer is the layer which the GeneralPath came from.
     * @param gp is the GeneralPath to add.
     * @param c is the color to fill in the GeneralPath.
     */
    InterfaceUndoItem add(DrawingLayer layer, GeneralPath gp, Color c) {
        Rectangle2D bounds2d = gp.getBounds2D();
        
        // Since the first one is the lowest z-depth, search backwards.
        // If found one then just update it's color.
        for (ListIterator itr = ltGeneralPath.listIterator(ltGeneralPath.size()); itr.hasPrevious(); ) {
            RMGeneralPath rmGPath = (RMGeneralPath)itr.previous();
            if (rmGPath.equals(bounds2d)) {
                return rmGPath.change(c);
            }
        }

        // Create the new RMGeneralPath.
        RMGeneralPath rgpNew = new RMGeneralPath(layer, gp, c);
        
        // Need to insert so that the lowest z-depth stays true for the list.
        int index = 0;
        boolean inserted = false;
        for (Iterator itr = ltGeneralPath.iterator(); itr.hasNext(); index++) {
            RMGeneralPath curr = (RMGeneralPath)itr.next();
            if (rgpNew.contains(curr)) {
                ltGeneralPath.add(index, rgpNew);
                inserted = true;
                break;
            }
        }
        
        // Append at the end since it does not contain any of the current GeneralPaths.
        if (inserted == false)
            ltGeneralPath.add(rgpNew);
        
        return new UndoItemRMGeneralPath(rgpNew);
    }
    
    /** This will get a GeneralPath's.
     * @param fptMouseDown is the mouse position to create a GeneralPath at.
     * @return the color of the GeneralPath if user clicked on it, else null no color.
     */
    public Color getGeneralPathColor(FPointType fptMousePosition) {
        // Since the first one is the lowest z-depth
        for (ListIterator itr = ltGeneralPath.listIterator(ltGeneralPath.size()); itr.hasPrevious(); ) {
            RMGeneralPath rmGPath = (RMGeneralPath)itr.previous();
            if (rmGPath.contains(fptMousePosition))
                return rmGPath.getColor();
        }
        
        return null;
    }
    
    /** @return a list of all the colors which represent all the layers.
     */
    public LinkedList getColors() {
        LinkedList ltColors = new LinkedList();
        for (Iterator itr=ltGeneralPath.iterator(); itr.hasNext();) {
            Color c = ((RMGeneralPath)itr.next()).getColor();
            ltColors.add(c);
        }
        
        return ltColors;
    }

    /** This will clear all GeneralPaths from this pool.
     */
    void clear() {
        ltGeneralPath = new LinkedList();
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Adding a RMGeneralPath">
    
    /** This will undo/redo a new RMGeneralPath created.
     */
    class UndoItemRMGeneralPath implements InterfaceUndoItem {
        RMGeneralPath rgp;
        int index=-1;
        
        /** @param cOld is the old color.
         */
        UndoItemRMGeneralPath(RMGeneralPath rgp) {
            this.rgp = rgp;
        }
        
        public void undoItem() {
            index = ltGeneralPath.indexOf(rgp);
            ltGeneralPath.remove(rgp);
        }
        
        public void redoItem() {
            ltGeneralPath.add(index, rgp);
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{GeneralPathPool.UndoItemRMGeneralPath rgp[" + rgp + "] index[" + index + "]}";
        }
    }
    
    // </editor-fold>
    
}
    
// <editor-fold defaultstate="collapsed" desc=" Class RMGeneralPath ">

class RMGeneralPath {
    /** This is the Layer which this GeneralPath was made from. */
    private DrawingLayer layer;
    /** This is the GeneralPath. */
    private GeneralPath gp;
    /** This is the color used to fill the GeneralPath. */
    private Color c;
    /** This is the bounds produced by the GeneralPath (includes the control points, thus it is not very accurate). */
    private Rectangle2D bound2d;
    /** This is the bounds produced by the Area of the GeneralPath (very accurate bounds even with bezier control points). */
    private Rectangle2D tightBounds2d;
    
    RMGeneralPath(DrawingLayer layer, GeneralPath gp, Color c) {
        this.gp = gp;
        this.c = c;
        this.bound2d = gp.getBounds2D();
        this.tightBounds2d = new Area(gp).getBounds2D();
        this.layer = layer;
    }

    // <editor-fold defaultstate="collapsed" desc=" Serialize Support ">
    
    /** This will load the version 2.0 of RXML file.
     * @param eRMGeneralPath is the element for the rmGeneralPath in the RXML file.
     * @param lPool is the LayerPool which these general paths are constructed from.
     */
    public static RMGeneralPath loadVersion20(Element eRMGeneralPath, DrawingLayerPool lPool) throws Exception {
        int layerId = XmlUtil.getAttributeInteger(eRMGeneralPath, "layerId");
        Color c = new Color(XmlUtil.getAttributeInteger(eRMGeneralPath, "color"));
        
        // Create the GeneralPath.
        GeneralPath gp = new GeneralPath();
        NodeList nList = eRMGeneralPath.getElementsByTagName("seg");
        int length = nList.getLength();
        float xPt1, yPt1, xPt2, yPt2, xPt3, yPt3;
        for (int i=0; i < length; i++) {
            Element eSeg = (Element)nList.item(i);
            int type = XmlUtil.getAttributeInteger(eSeg, "type");
            switch (type) {
                case PathIterator.SEG_CLOSE:
                    gp.closePath();
                    break;
                case PathIterator.SEG_LINETO:
                    xPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt1");
                    yPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt1");
                    gp.lineTo(xPt1, yPt1);
                    break;
                case PathIterator.SEG_MOVETO:
                    xPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt1");
                    yPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt1");
                    gp.moveTo(xPt1, yPt1);
                    break;
                case PathIterator.SEG_QUADTO:
                    xPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt1");
                    yPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt1");
                    xPt2 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt2");
                    yPt2 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt2");
                    gp.quadTo(xPt1, yPt1, xPt2, yPt2);
                    break;
                case PathIterator.SEG_CUBICTO:
                    xPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt1");
                    yPt1 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt1");
                    xPt2 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt2");
                    yPt2 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt2");
                    xPt3 = (float)XmlUtil.getAttributeDouble(eSeg, "xPt3");
                    yPt3 = (float)XmlUtil.getAttributeDouble(eSeg, "yPt3");
                    gp.curveTo(xPt1, yPt1, xPt2, yPt2, xPt3, yPt3);
                    break;                
            }
        }
        
        return new RMGeneralPath(lPool.getLayer(layerId), gp, c);
    }
    
    /** This will write out this RMGeneralPath.
     */
    void write(PrintWriter out) {
        double []arr = new double[6];
        out.println("    <rmGeneralPath color='" + c.getRGB() + "' layerId='" + layer.getId() + "'>");
        for (PathIterator itr = gp.getPathIterator(null); !itr.isDone(); itr.next()) {
            int segType = itr.currentSegment(arr);
            switch (segType) {
                case PathIterator.SEG_CLOSE:
                    out.println("     <seg type='" + segType + "'/>");
                    break;
                case PathIterator.SEG_LINETO:
                case PathIterator.SEG_MOVETO:
                    out.println("     <seg type='" + segType + "' xPt1='" + arr[0] + "' yPt1='" + arr[1] + "'/>");
                    break;
                case PathIterator.SEG_QUADTO:
                    out.println("     <seg type='" + segType + "' xPt1='" + arr[0] + "' yPt1='" + arr[1] + "' xPt2='" + arr[2] + "' yPt2='" + arr[3] + "'/>");
                    break;
                case PathIterator.SEG_CUBICTO:
                    out.println("     <seg type='" + segType + "' xPt1='" + arr[0] + "' yPt1='" + arr[1] + "' xPt2='" + arr[2] + "' yPt2='" + arr[3] + "' xPt3='" + arr[4] + "' yPt3='" + arr[5] + "'/>");
                    break;
            }
        }        
        out.println("    </rmGeneralPath>");
    }
    
    // </editor-fold>    
    
    /** @param is the rgp in question to see if it is contained within this RMGeneralPath.
     * @return true if this RMGeneralPath contains the rgp (using the tight bounds), else false it does not.
     */
    public boolean contains(RMGeneralPath rgp) {
        return bound2d.contains(rgp.bound2d);
    }
    
    /** This will check to see if the obj equals this RMGeneralPath.
     * @param obj can be a GeneralPath, it will see if it equals by checking the bounds of the GeneralPaths.
     * @return true if they are equal.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle2D)
            return ((Rectangle2D)obj).equals(this.bound2d);
        else if (obj instanceof GeneralPath)
            return ((GeneralPath)obj).getBounds2D().equals(this.bound2d);
        else if (obj instanceof RMGeneralPath)
            return ((RMGeneralPath)obj).bound2d.equals(this.bound2d);
        return false;
    }
    
    void draw(Graphics2D g2d) {        
        if (layer.isVisible()) {
            g2d.setColor(c);
            g2d.fill(gp);
        }
    }
    
    /** This is used to draw into a bitmap.
     * @param g2D is the graphics class.
     * @param changeColor is true if it should change color for the layers. If it is false then no general paths are filled in.
     */
    void drawAllBitmap(Graphics2D g2d, boolean changeColor) {
        if (changeColor)
            g2d.setColor(c);
        g2d.fill(gp);
    }
    
    /** This will get the color of the GeneralPath.
     * @return color of the GeneralPath.
     */
    Color getColor() {
        return c;
    }

    /** @return the layer which this GeneralPath came from.
     */
    DrawingLayer getLayer() {
        return layer;
    }
    
    /** @return the general path.
     */
    GeneralPath getGeneralPath() {
        return gp;
    }
    
    /** @param fptMouseDown is the in question position that is in or outside the GeneralPath.
     * @return true if it contains the position within the GeneralPath.
     */
    boolean contains(FPointType fptMouseDown) {
        return gp.contains(fptMouseDown.x, fptMouseDown.y);
    }
    
    /** This will change the color of the GeneralPath.
     * @param cNew is the new color to change the GeneralPath fill.
     * @return an undo item for this operation.
     */
    InterfaceUndoItem change(Color cNew) {
        InterfaceUndoItem iUndoItem = new UndoItemChangeColor(c, cNew);
        c = cNew;
        
        return iUndoItem;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Undo Item for Changing Color ">
    
    /** This will undo/redo a new vertex created.
     */
    class UndoItemChangeColor implements InterfaceUndoItem {
        Color cNew;
        Color cOld;
        
        /** @param cOld is the old color.
         * @param cNew is the new color.
         */
        UndoItemChangeColor(Color cOld, Color cNew) {
            this.cNew = cNew;
            this.cOld = cOld;
        }
        
        public void undoItem() {
            c = cOld;
        }
        
        public void redoItem() {
            c = cNew;
        }
        
        public boolean isUndoable() {
            return true;
        }
        
        public String toString() {
            return "{GeneralPathPool.UndoItemChangeColor cOld[" + cOld + "] cNew[" + cNew + "]}";
        }
    }
    
    // </editor-fold>    
}

// </editor-fold>

