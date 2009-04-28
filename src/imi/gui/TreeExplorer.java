/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package imi.gui;

import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.utils.tree.ModelInstanceProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.utils.JTree_DataDumper;
import java.awt.Component;
import java.awt.Toolkit;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * TreeExploerer Class
 * @author Viet Nguyen Truong
 */
public class TreeExplorer extends javax.swing.JFrame {
    // Class Data Members
    private DefaultMutableTreeNode topNode  = null;
    private DefaultMutableTreeNode dmtSelectedNode = null;
    private PNode prevSelection = null;
    private PNode currentSelection = null;
    private DefaultTreeModel model = null;
    private TreePopUpMenu popupMenu = new TreePopUpMenu();
    private SceneEssentials sceneData = null;
    private Component identity  = this;

    TreeExplorer(SceneEssentials aThis) {
        this();
        setExplorer(aThis);
    }

    public void refresh() {
        this.setVisible(false);
        this.setVisible(true);
    }
    
    // Mutators
    /**
     * Sets the root node of the jTree
     * @param node (DefaultMutableTreeNode)
     */
    public void setTopNode(DefaultMutableTreeNode node) {
        topNode = node;
    }
    
    /**
     * Systematically goes through each row and expands the branches and leaves
     */
    public void expandTree() {
        for(int i = 0; i < jTree_TreeView.getRowCount(); i++)
            jTree_TreeView.expandRow(i);
    }
    
    /**
     * Un-highlights the selected joint.  Should be called on exit of the JTree
     * Explorer
     */
    public void nodeUnselect() {
        if(currentSelection != null && currentSelection instanceof PJoint)
            ((PJoint)currentSelection).unselect();
    }
    
    // Helper Functions
    /**
     * Returns the current PNode being selected
     * @return PJoint
     */
    public PNode getCurrentSelection() {
        return currentSelection;
    }
    
    /**
     * Processes the data of the treenode that is selecteded for use
     */
    public void processSelection() {
        //1- Get selected node data
        dmtSelectedNode = ((DefaultMutableTreeNode)jTree_TreeView.getLastSelectedPathComponent());
        PNode selectedNode = ((PNode)dmtSelectedNode.getUserObject());
        
        if(prevSelection != null && prevSelection instanceof PPolygonSkinnedMeshInstance)
            unselectGroup();
        
        //2- Highlight the selected joint
        if(selectedNode instanceof PJoint) {                                    // if it is a PJoint
            if(prevSelection == null) {                                         // if there was a no previous selection set it to the current selection and turn it on
                prevSelection = selectedNode;
                ((PJoint)selectedNode).select();
            }
            else if(prevSelection instanceof PJoint) {                          // if previous was a joint turn it off set it to the selected and turn selected on
                ((PJoint)prevSelection).unselect();
                prevSelection = selectedNode;
                ((PJoint)selectedNode).select();
            }
            else {
                prevSelection = selectedNode;                                   // if previous was not a joint set it to the selected and turn selected on
                ((PJoint)selectedNode).select();
            }
        }
        else {                                                                  // if it is not a PJoint
            if(prevSelection == null) {                                         // if there was no previous set it to current selection
                prevSelection = selectedNode;
            }
            else if(prevSelection instanceof PJoint) {                          // if previous was a joint turn it off set it to selected
                ((PJoint)prevSelection).unselect();
                prevSelection = selectedNode;
            }
            else {                                                              // if previous was not a joint set it to selected
                prevSelection = selectedNode;
            }
        }
        currentSelection = selectedNode;
        popupMenu.setPropertyPanel(selectedNode, dmtSelectedNode);
        
        if(currentSelection instanceof PPolygonSkinnedMeshInstance)
            selectGroup();
    }
    
    /**
     * Sets the current PScene for the Explorer and sets up the JTree
     */
    public void setTree() {
        ModelInstanceProcessor modelProcessor = new ModelInstanceProcessor();
        modelProcessor.setTopNode(sceneData.getPScene());
        TreeTraverser.breadthFirst(sceneData.getPScene(), modelProcessor);
        this.setTopNode(modelProcessor.getTopNode());
        model = new DefaultTreeModel(topNode);
        jTree_TreeView.setModel(model);
        for(int i = 0; i < jTree_TreeView.getRowCount(); i++) {
            jTree_TreeView.expandRow(i);
        }
    }
    
    /**
     * Sets all the Explorer specific scene components used for display and
     * manipulation
     * @param scene (SceneEssentials)
     */
    public void setExplorer(SceneEssentials scene) {
        sceneData = scene;
        setTree();
    }
    
    /**
     * Constructor for the TreeExplorer class used to create the form and set up
     * it's components
     */
    public TreeExplorer() {
        // Initialize the Frame components and set the window position
        initComponents();
        this.setTitle("PScene Explorer GUI");
        double dScreenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int iXPos           = (int)dScreenWidth - this.getWidth();        
        this.setLocation(iXPos, 0);
    }
    
    /**
     * Handles the event(s) of when a selection change occures in the jTree
     * @param evt (TreeSelectionEvent)
     */
    public void jTree_TreeViewNodeSelected(TreeSelectionEvent evt) {
        if(evt.getNewLeadSelectionPath() != null) {
            processSelection();
            popupMenu.setPopupMenu(sceneData, ((DefaultTreeModel)jTree_TreeView.getModel()), currentSelection, dmtSelectedNode);
        }
    }
    
    public void selectGroup() {
        if (currentSelection == null)
            return;

        if(currentSelection instanceof PPolygonSkinnedMeshInstance) {

            if (sceneData.getAvatar() == null || !sceneData.getAvatar().isInitialized())
                return;

            SkeletonNode skeleton = sceneData.getAvatar().getSkeleton();
            
            if(skeleton != null) {
                PPolygonSkinnedMeshInstance skinnedMeshInst = ((PPolygonSkinnedMeshInstance)currentSelection);
                int[] jointIndices = skinnedMeshInst.getInfluenceIndices();
                for(int k = 0; k < jointIndices.length; k++) {
                    ((PJoint)skeleton.getSkinnedMeshJoint(jointIndices[k])).select();
                }
            }
        }
    }
    
    public void unselectGroup() {
        if (prevSelection == null)
            return;

        if(prevSelection instanceof PPolygonSkinnedMeshInstance) {

            if (sceneData.getAvatar() == null || !sceneData.getAvatar().isInitialized())
                return;

            SkeletonNode skeleton = sceneData.getAvatar().getSkeleton();
            
            if(skeleton != null) {
                PPolygonSkinnedMeshInstance skinnedMeshInst = ((PPolygonSkinnedMeshInstance)prevSelection);
                int[] jointIndices = skinnedMeshInst.getInfluenceIndices();
                for(int k = 0; k < jointIndices.length; k++) {
                    ((PJoint)skeleton.getSkinnedMeshJoint(jointIndices[k])).unselect();
                }
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jScrollPane_TreeView = new javax.swing.JScrollPane();
        model = new DefaultTreeModel(topNode);
        jTree_TreeView = new javax.swing.JTree(model);
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu_SaveTreeData = new javax.swing.JMenu();
        jMenuItem_SaveAll = new javax.swing.JMenuItem();
        jMenuItem_SaveExpanded = new javax.swing.JMenuItem();

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTree_TreeView.setBorder(javax.swing.BorderFactory.createTitledBorder("PScene Hierarchy"));
        jTree_TreeView.putClientProperty("JTree.lineStyle", "Angled");
        jScrollPane_TreeView.setViewportView(jTree_TreeView);
        jTree_TreeView.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                jTree_TreeViewNodeSelected(evt);
            }
        });
        popupMenu.setPopupMenu(sceneData, model, currentSelection, dmtSelectedNode);
        jTree_TreeView.addMouseListener(popupMenu);
        jTree_TreeView.setCellRenderer(new SceneCellRenderer());

        jMenu_SaveTreeData.setText("Save Tree Data");

        jMenuItem_SaveAll.setText("Save All Nodes");
        jMenuItem_SaveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTree_DataDumper dataDumper = new JTree_DataDumper(identity);
                dataDumper.saveJTreeData(0, jTree_TreeView);
            }
        });
        jMenu_SaveTreeData.add(jMenuItem_SaveAll);

        jMenuItem_SaveExpanded.setText("Save Expanded Nodes");
        jMenuItem_SaveExpanded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTree_DataDumper dataDumper = new JTree_DataDumper(identity);
                dataDumper.saveJTreeData(1, jTree_TreeView);
            }
        });
        jMenu_SaveTreeData.add(jMenuItem_SaveExpanded);

        jMenuBar1.add(jMenu_SaveTreeData);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane_TreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane_TreeView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TreeExplorer().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem_SaveAll;
    private javax.swing.JMenuItem jMenuItem_SaveExpanded;
    private javax.swing.JMenu jMenu_SaveTreeData;
    private javax.swing.JScrollPane jScrollPane_TreeView;
    private javax.swing.JTree jTree_TreeView;
    // End of variables declaration//GEN-END:variables

}
