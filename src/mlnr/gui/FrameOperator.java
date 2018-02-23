/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlnr.gui;

import javax.swing.JFrame;
import mlnr.util.gui.FileMenuList;

/**
 *
 * @author rmolnar
 */
public class FrameOperator implements InterfaceFrameOperation {

    public void enableUndoable(boolean enable) {
        
    }

    public void enableRedoable(boolean enable) {
        
    }

    public void enableAdvanceTools(boolean enabled) {
        
    }

    public void enableCopyCut(boolean enabled) {
        
    }

    public void enablePaste(boolean enableDesign, boolean enableNew) {
        
    }

    public void validateLayerPanel() {
       
    }

    public void validateImagePanel() {
       
    }

    public JFrame getFrame() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void notifyDocumentChanged(boolean changed) {
       
    }

    public void notifyToolChanged() {
       
    }

    public void setZoom(float percentage) {
       
    }

    public void zoomItem(String item) {
       
    }

    public FileMenuList getFileMenuList() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getStage() {
        return 0;
    }

}
