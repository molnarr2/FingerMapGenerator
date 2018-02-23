/*
 * IntersectionLineDirection.java
 * 
 * Created on Oct 11, 2007, 1:56:47 PM
 * 
 */

package mlnr.draw;

/** This class is used to store the intesection position and the direction as a vertex. It will also store the from's 
 * AbstractLine and the parametric position of the intersection.
 * @author Rob
 */
public class IntersectionLineDirection {
    private AbstractLine abFrom;
    private float parametricFrom;
    private AbstractLine abIntersection;
    private float parametricIntersection;
    private Vertex vDirection;
        
    /** @param abFrom this is the AbstractLine the intersection is from.
     *  @param parametricFrom is the parameteric position on the from line.
     *  @param abIntersection this is the line which is the intersection with the direction.
     *  @param parametricIntersection is the position on the intersection line.
     *  @param vDirection is the vertex to go to next.
     */
    public IntersectionLineDirection(AbstractLine abFrom, float parametricFrom, AbstractLine abIntersection, float parametricIntersection, Vertex vDirection) {
        this.abFrom = abFrom;
        this.parametricFrom = parametricFrom;
        this.abIntersection = abIntersection;
        this.parametricIntersection = parametricIntersection;
        this.vDirection = vDirection;
    }
    
    public AbstractLine getAbIntersection() {
        return abIntersection;
    }

    public float getParametricIntersection() {
        return parametricIntersection;
    }

    public Vertex getVDirection() {
        return vDirection;
    }

    public AbstractLine getAbFrom() {
        return abFrom;
    }

    /*
     *  @return parameteric position on the from line where the intersection occured.
     */
    public float getParametricFrom() {
        return parametricFrom;
    }

    public String toString() {
        return "from: [" + abFrom.getId() + "] parametric from: [" + parametricFrom + "] intersection: [" + abIntersection.getId() + "] parametric intersection: [" 
                + parametricIntersection + "] direction vertex: [" + vDirection.getId() + "]";
    }
    
    /** This will print out the intersection information.
     */
    public String toStringIntersection() {
        return "{Intersection at: Line: " + abFrom.getId() + " t: " + parametricFrom + " and Line: " + abIntersection.getId() + " t: " + parametricIntersection + "}";
    }
    
    //public Shape2D 
}
