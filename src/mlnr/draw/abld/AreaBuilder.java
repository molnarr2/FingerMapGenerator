/*
 * AreaBuilder.java
 *
 * Created on March 19, 2007, 7:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mlnr.draw.abld;

import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.GraphTheoryInterface;

/**
 *
 * @author Robert Molnar 2
 */
public class AreaBuilder {
//    /** List of AreaGraph built from the list of GraphTheoryInterface. */
//    LinkedList<AreaGraph> ltAreaGraph = new LinkedList();
//    
//    /** Creates a new instance of AreaBuilder */
//    public AreaBuilder() {
//    }
//    
//    /** This will build the AreaGraphs.
//     * @param ltIGraphTheory is the an interfaces used to create the graphs from.
//     */
//    public void buildGraphs(LinkedList<GraphTheoryInterface> ltIGraphTheory) {
//        LinkedList<AreaGraph> ltTemp = new LinkedList();
//        
//        // Clear the visited lines.
//        for (Iterator<GraphTheoryInterface> itr = ltIGraphTheory.iterator(); itr.hasNext(); ) {
//            itr.next().setVisited(false);
//        }
//        
//        // Convert the GraphTheoryInterface into an AreaGraph.
//        for (Iterator<GraphTheoryInterface> itr = ltIGraphTheory.iterator(); itr.hasNext(); ) {
//            GraphTheoryInterface iGraph = itr.next();
//            
//            AreaGraph areaGraph = new AreaGraph();
//            areaGraph.build(iGraph);
//            ltTemp.add(areaGraph);
//        }
//        
//        // Merge AreaGraphs together.
//        while (ltTemp.isEmpty() == false) {
//            // Add this AreaGraph to the merged list.
//            AreaGraph areaGraphMerge = ltTemp.removeFirst();
//            ltAreaGraph.add(areaGraphMerge);
//            
//            // Merge the other AreaGraphs to this one if possible.
//            for (Iterator<AreaGraph> itr = ltTemp.iterator(); itr.hasNext(); ) {
//                AreaGraph areaGraph = itr.next();
//                                
//                // Attempt to merge the AreaGraph. If it gets merged then remove it from the list.
//                if (areaGraphMerge.merge(areaGraph))
//                    itr.remove();                
//            }
//        }
//    }
//    
}
