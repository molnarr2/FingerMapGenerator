/*
 * InterfacePool.java
 *
 * Created on July 14, 2005, 1:29 PM
 *
 */

package mlnr.draw;

import java.util.Comparator;

/** Used by the AbstractHashPool and AbstractSortedPool. If an object is to use one
 * of the AbstractPools then it needs to implement this interface.
 * @author Robert Molnar II
 */
public interface InterfacePoolObject extends Comparable {
    public int getId();
    public int getZDepth();
    public void setZDepth(int zDepth);
    public void setId(int id);
    public int compareTo(Object o);
}