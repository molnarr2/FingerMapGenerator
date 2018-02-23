/*
 * FPointType.java
 *
 * Created on July 14, 2006, 2:59 PM
 *
 */

package mlnr.type;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Robert Molnar II
 */
public class FPointType {
    public float x;
    public float y;
    
    public FPointType() {
        x = 0f;
        y = 0f;
    }
    
    /** Creates a new instance of FPointType
     */
    public FPointType(double x, double y) {
        this.x = (float)x;
        this.y = (float)y;
    }
    
    /** Creates a new instance of FPointType */
    public FPointType(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public FPointType(java.awt.Point pt) {
        this.x = pt.x;
        this.y = pt.y;
    }
    
    public FPointType(FPointType fpt) {
        this.x = fpt.x;
        this.y = fpt.y;
    }   
    
    public String toString() {
        return "{FPointType (" + x + "," + y + ")}";
    }

    /** This will check to see if the point 'fpt' matches x=x and y=y with delta room of error.
     * @param fpt is the point to check against this point. 
     * @return true: If they match x=x and y=y with delta room of error, else false.
     * 
     */
    public boolean equals(FPointType fpt) {
        float temp = Math.abs(x - fpt.x);
        if (temp < 0.0001) {
            temp = Math.abs(y - fpt.y);
            if (temp < 0.0001)
                return true;
        }
        
        return false;
    }

    /** This will get the distance from this point to point fpt.
     * @param fpt is the point to get the distance to.
     * @return the distance.
     */
    public float distance(FPointType fpt) {
        float xDiff = x - fpt.x;
        float yDiff = y - fpt.y;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /** This will get the distance from this point to point (x1, y1).
     * @param x1 is the x position.
     *  @param y1 is the y position.
     * @return the distance.
     */
    public float distance(float x1, float y1) {
        float xDiff = x - x1;
        float yDiff = y - y1;
        return (float)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
    
    /** This will calculate the center of all points in the LinkedList.
     * @param ltPoints is a list of points {FPointType}.
     * @return the center point of all points in the list.
     */
    public static FPointType calculateCenter(LinkedList ltPoints) {
        FPointType fptReturn = new FPointType();
        
        // Sum all points.
        for (Iterator itr = ltPoints.iterator(); itr.hasNext(); ) {
            FPointType fpt = (FPointType)itr.next();
            fptReturn.x += fpt.x;
            fptReturn.y += fpt.y;            
        }
        
        // Divide by the count of points.
        float countOfPoints = ltPoints.size();
        fptReturn.x /= countOfPoints;
        fptReturn.y /= countOfPoints;
        
        return fptReturn;
    }
    
    /** This will compare all points and get the minimum values from them.
     * @param array is a list of points to compare and get the minimum values from.
     * @return point that contains the minimum values.
     */
    public static FPointType min(FPointType []array) {
        FPointType minimum = new FPointType(Float.MAX_VALUE, Float.MAX_VALUE);
        
        for (int i=0; i < array.length; i++) {
            if (array[i].x < minimum.x)
                minimum.x = array[i].x;
            if (array[i].y < minimum.y)
                minimum.y = array[i].y;
        }
        
        return minimum;
    }
    
    /** This will compare all points and get the maximum values from them.
     * @param array is a list of points to compare and get the maximum values from.
     * @return point that contains the maximum values.
     */
    public static FPointType max(FPointType []array) {
        FPointType maximum = new FPointType(Float.MIN_VALUE, Float.MIN_VALUE);
        
        for (int i=0; i < array.length; i++) {
            if (array[i].x > maximum.x)
                maximum.x = array[i].x;
            if (array[i].y > maximum.y)
                maximum.y = array[i].y;
        }
        
        return maximum;
    }
    
}