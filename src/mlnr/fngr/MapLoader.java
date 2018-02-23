/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mlnr.fngr;

import java.io.File;
import java.io.FileInputStream;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mlnr.draw.DrawingDesign;
import mlnr.draw.MetaDrawingInfo;
import mlnr.util.XmlUtil;
import org.w3c.dom.*;
import mlnr.gui.*;

/**
 *
 * @author rmolnar
 */
public class MapLoader {

    DrawingDesign design;
    MetaDrawingInfo metaDrawingInfo = new MetaDrawingInfo();

    public MapLoader() {

    }

    public DrawingDesign getDesign() {
        return design;
    }    
    
    public MetaDrawingInfo getMetaInfo() {
        return metaDrawingInfo;
    }
    
    public void open(File fRxml) throws Exception {
        // Start the xml parsing.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(new FileInputStream(fRxml));

        // This should be the <rxml> element.
        Element root = doc.getDocumentElement();
        if ("rxml".equals(root.getNodeName()) == false) {
            throw new Exception("Missing root element [rxml]. Not rxml file.");
        }

        // Process the drawing information.
        openRXML(root, new FrameOperator());

        // Version 2.0 introduced new items such as stages and meta drawing needed by this class.
        if ("2.0".equals(XmlUtil.getAttributeString(root, "v"))) {

            // Get the meta drawing information.
            Element eDrawing = XmlUtil.getElementByTagName(root, "metaDrawingInfo");
            metaDrawingInfo.loadVersion20(eDrawing);
        }

    }

    /** This will load the drawing from a file.
     * @param root is the root element, should be "rxml".
     * @param iFrameOperator is the interface used to operation the main frame.
     * @return true if the file opened ok, else false errored out when trying to open the file.
     */
    private void openRXML(Element root, InterfaceFrameOperation iFrameOperator) throws Exception {
        if ("rxml".equals(root.getNodeName()) == false) {
            throw new Exception("Missing root element [rxml]. Not rxml file.");
        }

        // Each version is different therefore they required different loading techniques.
        String versionNumber = XmlUtil.getAttributeString(root, "v");
        if ("1.0".equals(versionNumber)) {
            openRXMLVersion10(root, iFrameOperator);
        } else if ("1.1".equals(versionNumber)) {
            openRXMLVersion11(root, iFrameOperator);
        } else if ("2.0".equals(versionNumber)) {
            openRXMLVersion20(root, iFrameOperator);
        } else {
            new JOptionPane().showMessageDialog(null, "Unknown version number " + versionNumber + ". If number is greater than 2.0 then you will need to upgrade your software to load in this file.",
                    "Error Message", JOptionPane.ERROR_MESSAGE);
            throw new Exception("Unknown version number: " + versionNumber);
        }
    }

    /** This will open version 1.0 rxml file. Coordinates are in integers and at a resolution of 20, therefore all coordinates
     * need to be converted to floating point numbers by dividing by 20.
     * @param iFrameOperator is the interface used to operation the main frame.
     * @param root is the root element of the rxml file.
     */
    private void openRXMLVersion10(Element root, InterfaceFrameOperation iFrameOperator) throws Exception {
        // Load the design in.
        design = DrawingDesign.loadVersion10(root, iFrameOperator);
    }

    /** This will open version 1.1 rxml file. The difference between version 1.0 and 1.1 is the image tag is
     * added to version 1.1. Coordinates are in integers and at a resolution of 20, therefore all coordinates
     * need to be converted to floating point numbers by dividing by 20.
     * @param iFrameOperator is the interface used to operation the main frame.
     * @param root is the root element of the rxml file.
     */
    private void openRXMLVersion11(Element root, InterfaceFrameOperation iFrameOperator) throws Exception {
        // Load the design in.
        design = DrawingDesign.loadVersion10(root, iFrameOperator);

    // Get the image tag and load it in.
    //Element eImage = XmlUtil.getElementByTagName(root, "image");
    //imagePool.loadVersion11(eImage);
    }

    /** This will open version 2.0 rxml file.
     * @param iFrameOperator is the interface used to operation the main frame.
     * @param root is the root element of the rxml file.
     */
    private void openRXMLVersion20(Element root, InterfaceFrameOperation iFrameOperator) throws Exception {
        // Get the stage element.
        //Element eStage = XmlUtil.getElementByTagName(root, "stage");
        //setStage(XmlUtil.getAttributeInteger(eStage, "value"));

        // Get the design element and load it.
        Element eDesign = XmlUtil.getElementByTagName(root, "design");
        design = DrawingDesign.loadVersion20(eDesign, iFrameOperator);

    // Get the imagePool element and load it.
    //Element eImagePool = XmlUtil.getElementByTagName(root, "imagePool");
    //imagePool.loadVersion20(eImagePool);
    }
}
