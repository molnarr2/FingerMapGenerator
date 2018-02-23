/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mlnr.fngr;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Iterator;
import java.util.LinkedList;
import mlnr.draw.DrawingDesign;
import mlnr.draw.MetaDrawingInfo;
import mlnr.type.FPointType;

/**
 *
 * @author rmolnar
 */
public class MapLevel {
    LinkedList<LinkedList<FPointType>> ltPaths;
    LinkedList<FPointType> ltStarts;
    LinkedList<FPointType> ltMultipliers;
    DrawingDesign design;
    MetaDrawingInfo metaInfo;
    
    /** Assumed the MapLoader is already loaded.
     */
    public MapLevel(MapLoader loadedMap) {
        design = loadedMap.getDesign();
        metaInfo = loadedMap.getMetaInfo();
    }
    
    /** This will generate the data structure for the map level.
     */
    public void generate() {
        ltStarts = design.finger_getStartPoints();
        ltMultipliers = design.finger_getMultipilerPoints();
        
        // Get a list of paths and then sample it down for usage on the iPhone.
        LinkedList<LinkedList<FPointType>> list = design.finger_getPaths();
        ltPaths = new LinkedList<LinkedList<FPointType>>();
        for (Iterator<LinkedList<FPointType>> itr = list.iterator(); itr.hasNext(); ) {
            LinkedList<FPointType> sampled = samplePath(itr.next());
            
            // Now determine the starting point of the path, if need to reverse it.
            FPointType first = sampled.getFirst();
            boolean bNeedReverse = true;
            for (Iterator<FPointType> itrStart = ltStarts.iterator(); itrStart.hasNext(); ) {
                FPointType ptStart = itrStart.next();
                if (ptStart.distance(first) < 0.5) {
                    bNeedReverse = false;
                    break;
                }
            }            
            if (bNeedReverse) 
                Collections.reverse(sampled);

            
            ltPaths.add(sampled);
        }
        
    }
    
    /** This will write out the level information.
     * @param id is an unique id number for the set, used for declaring unique variables.
     */
    public void write(PrintWriter out, int id) {
        out.println("LEVELNAME=" + metaInfo.getSetName());
        
        // Now add the paths in.
        for (Iterator<LinkedList<FPointType>> itr = ltPaths.iterator(); itr.hasNext(); ) {
            LinkedList<FPointType> ltsPoints = itr.next();
            
            // Print out a path of points.
            out.print("LEVELPATH=");            
            for (Iterator<FPointType> itrPoint = ltsPoints.iterator(); itrPoint.hasNext(); ) {
                FPointType pt = itrPoint.next();
                int x = (int)(pt.x * 10.0f + .5);
                int y = (int)(pt.y * 10.0f + .5);
                
                out.print(x + " " + y + " ");
            }
            out.println();
        }

        // Print out the multipliers.
        out.print("LEVELMULTIPLIER=");
        for (Iterator<FPointType> itr = ltMultipliers.iterator(); itr.hasNext(); ) {
            FPointType pt = itr.next();
            int x = (int)(pt.x * 10.0f + .5);
            int y = (int)(pt.y * 10.0f + .5);

            out.print(x + " " + y + " ");            
        }
        out.println();
    }
    
    private LinkedList<FPointType> samplePath(LinkedList<FPointType> ltIn) {
        LinkedList<FPointType> list = new LinkedList<FPointType>();
        
        if (ltIn.size() == 0)
            return list;
        
        FPointType prev = ltIn.getFirst();
        list.add(ltIn.getFirst());
        for (Iterator<FPointType> itr = ltIn.iterator(); itr.hasNext(); ) {
            FPointType curr = itr.next();
            if (prev.distance(curr) >= FingerSamplingDistance.SAMPLING_DISTANCE) {
                list.add(curr);
                prev = curr;
            }
        }        
        
        return list;
    }
}
