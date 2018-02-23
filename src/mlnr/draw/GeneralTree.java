/*
 * BinaryTree.java
 *
 * Created on November 17, 2006, 3:05 PM
 *
 */

package mlnr.draw;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author Robert Molnar II
 */
public class GeneralTree {
    
    // <editor-fold defaultstate="collapsed" desc=" Fields ">
    
    /** This is the root node of this tree. */
    GeneralTreeNode root;
            
    /** This is the debug current node to draw from. */
    LinkedList<GeneralTreeNode> ltDebugNodes = null;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Constructor and static Constructors ">
    
    /** Creates a new instance of GeneralTree */
    public GeneralTree() {
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Get/Set Methods ">
    
    /** @return the root node of this GeneralTre.
     */
    public GeneralTreeNode getRoot() {
        return root;
    }

    /** @param root is the GeneralTreeNode that will be the root of this GeneralTree.
     */
    void setRoot(GeneralTreeNode root) {
        this.root = root;
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Operation Methods ">
    
    /** This will convert the general tree into a trinary tree (3 nodes per parent node). This will place GeneralTreeDummyNodes
     * into tree if a parent node has more than 3 nodes. Note that a GeneralTreeDummyNode counts as 1 node.
     */
    public void convertToTrinaryTree() {
        root.convertToTrinaryTree();
    }
    
    /** This will compress the tree by converting multiple segments which are a single path into one segment. This will produce a
     * tree where each node contains 2 or more segments or none. If convertToTrinaryTree was called before this then the tree
     * will contain nodes that have 0,2,3 segments coming out of the them. 
     */
    public void convertToCompressedTree() {
        root.convertToCompressedTree();
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Draw Methods ">
    
    /** This will draw this GeneralTree using the current pen and color.
     */
    public void draw(Graphics2D g2d) {
        root.draw(g2d);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc=" Debug Methods ">
    
    /** This will begin a visual debug of this GeneralTree.
     */
    public void debug() {
        GeneralTreeFrame frame = new GeneralTreeFrame(this);
        frame.setSize(1000, 1000);
        frame.setVisible(true);
    }
    
    /** This will setup this GeneralTree to begin animation. This can be called to restart the animation.
     */
    void debugSetupAnimate() {
        ltDebugNodes = new LinkedList();
        ltDebugNodes.add(root);
    }    
    
    /** Debug animated drawing.
     */
    void debugAnimate(Graphics2D g2d) {
        LinkedList<GeneralTreeNode> ltTemp = new LinkedList();
        
        // Draw only the first segments from the nodes.
        for (Iterator<GeneralTreeNode> itr = ltDebugNodes.iterator(); itr.hasNext(); ) {
            GeneralTreeNode node = itr.next();
            node.debugDraw(g2d);
            ltTemp.addAll(node.getChildrenNodes());
        }
        
        // Next list of nodes to print out.
        ltDebugNodes = ltTemp;
    }
    
    // </editor-fold>
    
}

////////////////////////////////////////////////////////////////////////////////
// ShowPointsSimple ////////////////////////////////////////////////////////////
// This will visually show the GraphPointList structure. The ///////////////////
// GraphPointList.debugShowGraphPoints() will show this frame. /////////////////

/** This class will step through the GeneralTree and print it out.
 */
class GeneralTreeFrame extends JFrame implements MouseListener, ActionListener {
    GeneralTree gTree;
    
    javax.swing.Timer animationTimer;
    
    JMenuItem startTimer = new JMenuItem("Start Timer");
    JMenuItem stopTimer = new JMenuItem("Stop Timer");
    
    GeneralTreeFrame(GeneralTree gTree) {        
        this.gTree = gTree;
        
        startTimer.addActionListener(this);
        stopTimer.addActionListener(this);
        
        JMenu menu = new JMenu("Operations");
        menu.add(startTimer);
        menu.add(stopTimer);
        
        JMenuBar bar = new JMenuBar();
        bar.add(menu);
        
        this.setTitle("Show Points");
        this.setJMenuBar(bar);
        
        addMouseListener(this);
    }
    
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
        if (animationTimer == null || animationTimer.isRunning() == false)
            g2d.clearRect(0, 50, getWidth(), getHeight() - 50);
        
        // Set the font size.
        g2d.setFont(new Font("Serif", Font.PLAIN, 36));
        g2d.setStroke(new BasicStroke(0.0f));
        
        // Set resolution to 12 pixels per measurement.
        g2d.scale(10, 10);
        
        
        // Draw the graph point list.
        if (animationTimer != null && animationTimer.isRunning())
            gTree.debugAnimate(g2d);
        else
            gTree.draw(g2d);
    }
    
    public void mouseClicked(MouseEvent e) {
        
    }
    
    public void mousePressed(MouseEvent e) {
        
    }
    
    public void mouseReleased(MouseEvent e) {
    }
    
    public void mouseEntered(MouseEvent e) {
        
    }
    
    public void mouseExited(MouseEvent e) {
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startTimer) {
            this.getGraphics().clearRect(0, 0, getWidth(), getHeight());
            gTree.debugSetupAnimate();
            
            if (animationTimer == null) {
                animationTimer = new javax.swing.Timer(1000, this);
                animationTimer.start();
            } else {
                if (! animationTimer.isRunning())
                    animationTimer.start();
            }            
        } else if (e.getSource() == stopTimer) {
            animationTimer.stop();
        } else if (e.getSource() == animationTimer) {
            repaint();
        }
    }
    
}
