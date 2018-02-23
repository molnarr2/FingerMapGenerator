/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlnr.draw.area;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.MathLineCurve;

/** This class is used to represent a path of CubicCurve2D.Float and Line2D.Float.
 * @author Rob
 */
public class CurveLinePath {
    LinkedList<Shape> ltShapes = new LinkedList<Shape>();
    
    public CurveLinePath() {
        
    }
    
    /** @param shape is the Shape of CubicCurve2D.Float or Line2D.Float to append to this path.
     */
    public void append(Shape shape) {
        if (shape instanceof Line2D.Float)
            append((Line2D.Float)shape);
        else if (shape instanceof CubicCurve2D.Float)
            append((CubicCurve2D.Float)shape);
    }
    
    /** @param line is the Line2D.Float to apend to this path.
     */
    public void append(Line2D.Float line) {
        ltShapes.add(line);
    }
    
    /** @param curve is the CubicCurve2D.Float to apend to this path.
     */
    public void append(CubicCurve2D.Float curve) {
        ltShapes.add(curve);
    }
    
    /** @return an area of this path.
     */
    public Area toArea() {
        return new Area(toGeneralPath());
    }
    
    /** @return a GeneralPath of this path.
     */
    public GeneralPath toGeneralPath() {        
        GeneralPath generalPath = new GeneralPath();        
        
        for (Iterator<Shape> itr = ltShapes.iterator(); itr.hasNext(); )
            generalPath.append(itr.next(), true);
        
        return generalPath;        
    }
    
    /** @param path is another CurveLinePath used to see if it intersects this CurveLinePath.
     * @return true if this CurveLinePath intersects the path, else false it does not.
     */
    public boolean intersect(CurveLinePath path) {
        for (Iterator<Shape> itrThis = ltShapes.iterator(); itrThis.hasNext(); ) {
            Shape s1 = itrThis.next();
            
            for (Iterator<Shape> itrPath = path.ltShapes.iterator(); itrPath.hasNext(); ) {
                Shape s2 = itrPath.next();
                
                if (MathLineCurve.intersect(s1, s2))
                    return true;
            }
        }
        
        return false;
    }    
}
