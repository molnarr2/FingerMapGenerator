/*
 * InterfaceFrameOperation.java
 *
 * Created on August 7, 2006, 9:42 PM
 *
 */

package mlnr.gui;

import javax.swing.JFrame;
import mlnr.util.gui.FileMenuList;

/**
 *
 * @author Robert Molnar II
 */
public interface InterfaceFrameOperation {
    /** zoom in on the drawing. */
    public static final String ITEM_DRAWING = "Drawing";    
    /** zoom in on the current layer. */
    public static final String ITEM_LAYER = "Layer";    
    /** Zoom in to 100%. */
    public static final String ITEM_100 = "100percent";
    
    /** This is the gui stage for color editing. */
    public static final int GUISTAGE_COLOR = 1;
    
    /** This is the gui stage for vector editing. */
    public static final int GUISTAGE_VECTOR = 2;
    
    /** This is the gui stage when no DesignPanel is loaded. */
    public static final int GUISTAGE_EMPTY = 0;
            
    /** This will turn on/off the undo button and undo menu.
     * @param enable is true if the undo button and undo menu should be clickable, else false not clickable.
     */
    abstract public void enableUndoable(boolean enable);
    
    /** This will turn on/off the redo button and redo menu.
     * @param enable is true if the redo button and redo menu should be clickable, else false not clickable.
     */
    abstract public void enableRedoable(boolean enable);
    
    /** This will enable the advanced tools. 
     * @param enabled is true if the advanced tools can be used.
     */
    abstract public void enableAdvanceTools(boolean enabled);
    
    /** This will enable the copy and cut.
     * @param enabled is true if the Copy and Cut tools can be used.
     */
    abstract public void enableCopyCut(boolean enabled);
    
    /** This will enable the paste features.
     * @param enableDesign is true if it is possible to paste into the design.
     * @param enableNew is true if it is possible to paste into a new design.
     */    
    abstract public void enablePaste(boolean enableDesign, boolean enableNew);
    
    /** This will validate the layer panel for it to update it's layer panel information.
     */
    abstract public void validateLayerPanel();
    
    /** This will validate the image panel for it to update it's image panel information.
     * @param reloadTransform is true if it should reload the images and transform them, else false do not do that.
     */
    abstract public void validateImagePanel();
    
    /** This will get the frame which this interface Operates on.
     * @return the frame which this interface operates on.
     */
    abstract public JFrame getFrame();
    
    /** This is called to notify that the document has changed since it was saved.
     * @param changed is true if the document has been changed since it was saved, else false document is saved.
     */
    abstract public void notifyDocumentChanged(boolean changed);
    
    /** This is called to notify that a different tool besides the current one is
     * being used now. Use the EmbroideryDraw.getToolType() function to get the new
     * tool type.
     */
    abstract public void notifyToolChanged();
    
    /** This is called to set the zoom for the current design.
     * @percentage is the percentage to zoom in for the current design. If a -777.0 then zoom in on
     * the drawing.
     */
    abstract public void setZoom(float percentage);
    
    /** This is called to set the zoom for the current design.
     * @param item should be a ITEM_* constant to indicate which item to zoom on.
     */
    abstract public void zoomItem(String item);
        
    /** @return the file menu list which is used to open up previous files.
     */
    abstract public FileMenuList getFileMenuList();
    
    /** @return the current stage the drawing is at, GUISTAGE_*.
     */
    abstract public int getStage();
}
