/*
 * AreaVertex.java
 *
 * Created on March 23, 2007, 10:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw.abld;

import java.util.LinkedList;
import mlnr.type.FPointType;

/**
 *
 * @author Robert Molnar 2
 */
public class AreaVertex {
    FPointType fpt;
    LinkedList<AreaLine> ltLines = new LinkedList();
    
    /** Creates a new instance of AreaVertex */
    public AreaVertex(FPointType fpt) {
        this.fpt = fpt;
    }
    
    /** This will make the line adjacent to this vertex.
     * @param line is the line to add to this vertex.
     */
    void add(AreaLine line) {
        ltLines.add(line);
    }
}
