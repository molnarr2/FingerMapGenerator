/*
 * LayerInfo.java
 *
 * Created on September 20, 2005, 11:43 AM
 *
 */

package mlnr.draw;

import java.awt.*;

/**
 *
 * @author Robert Molnar II
 */
public class LayerInfo implements Comparable {    
    static final private int LAYER_NO=-1;
    static final public int ZDEPTH_UNINITIALIZE=-1;
    
    private int id;
    private int zDepth;
    private String name;
    private Color color;
    private boolean visible;
    
    /** Creates a new instance of LayerInfo */
    public LayerInfo(int id, int zDepth, String name, Color color, boolean visible) {
        this.id = id;
        this.zDepth = zDepth;
        this.name = name;
        this.color = color;
        this.visible = visible;
    }

    public LayerInfo(String name, Color color) {
        this.zDepth = ZDEPTH_UNINITIALIZE;
        this.name = name;
        this.color = color;
        this.id = LAYER_NO;
        this.visible = true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public String toString() {
        return name;
    }
    
    public String getString() {
        return "{LayerInfo: id[" + id + "] zDepth[" + zDepth + "] name[" + name + "] color[" + color + "] visible[" + visible + "]}";
    }
    
    /** This will get a copy of this class.
     * @return a complete copy of this class.
     */
    LayerInfo getCopy() {
        return new LayerInfo(id, zDepth, name, color, visible);
    }

    public int getZDepth() {
        return zDepth;
    }

    public void setZDepth(int zDepth) {
        this.zDepth = zDepth;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof LayerInfo == false)
            throw new IllegalArgumentException("o is not a LayerInfo: " + o.toString());
        LayerInfo l = (LayerInfo)o;
        
        return this.zDepth - l.zDepth;
    }
}
