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

import imi.character.Character;
import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.SkinnedMeshJoint;
import imi.scene.utils.traverser.ModelInstanceProcessor;
import imi.scene.utils.traverser.TreeTraverser;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jdesktop.mtgame.WorldManager;

/**
 * TreePopUpMenu Class
 * @author Viet Nguyen Truong
 */
public class TreePopUpMenu extends MouseAdapter implements ActionListener {
    // Class Data Members
    private String[] menuItems = null;
    private DefaultTreeModel treeModel = null;
    private DefaultMutableTreeNode selectedDMT = null;
    private PNode currentSelection = null;
    // Popup Menu GUI
    private JPopupMenu menu = null;
    private PNodePropertyPanel editorPanel = null;
    private JFrame editor = null;
    private JFrame_MatrixExplorer matrixExplorer = null;
    // Scene
    private PScene          pscene;
    private WorldManager    worldManager;
    // File IO GUI
    private JFileChooser jFileChooser_LoadModels = null;
    private String jointName = "NewJoint";
    private static int jointCount = 1;
    private static int modelCount = 1;
    // Copy Information
    private PNode copyNode = null;
    
    /**
     * Sets the selected node in the tree
     * @param selection
     */
    public void setTarget(PNode selection) { currentSelection = selection; }
    
    /**
     * Tells the popupmenu what the model is in the JTree so it can add and
     * prune things from the tree
     * @param model (DefaultTreeModel)
     */
    public void setTreeModel(DefaultTreeModel model) { treeModel = model; }
    
    /**
     * Tells the popupmenu what the DefaultMutableTreeNode that is currently
     * selected
     * @param node (DefaultMutableTreeNode)
     */
    public void setDMT(DefaultMutableTreeNode node) { selectedDMT = node; }
    
    /**
     * Refreshes the property panel based on the node selected in the JTree.
     * (Explicted call of this function will force the property panel to update)
     * @param selection
     * @param dmtselected
     */
    public void setPropertyPanel(PNode selection, DefaultMutableTreeNode dmtselected) {
        currentSelection = selection;
        selectedDMT = dmtselected;
        if(editorPanel != null) {
            editorPanel.setTargetNode(selection);
            editorPanel.setWorldManager(worldManager);
            editorPanel.refreshComponents();
        }
        if(matrixExplorer != null) {
            matrixExplorer.setTargetNode(selection);
        }
    }
    
    /**
     * Sets up the popupmenu's data members that are used for selection 
     * addition to and deletion from the PScene (and tree)
     * @param scene (SceneEssentials)
     * @param treemodel (DefaultTreeModel)
     * @param pnodeselection (PNode)
     * @param dmtselected (DefaultMutableTreeNode)
     */
    public void setPopupMenu(WorldManager wm, PScene scene, DefaultTreeModel treemodel, PNode pnodeselection, DefaultMutableTreeNode dmtselected) {
        worldManager        = wm;
        pscene              = scene;
        treeModel           = treemodel;
        currentSelection    = pnodeselection;
        selectedDMT         = dmtselected;
    }
    
    /**
     * Opens up the NodeProperty panel of the selected node which allows the
     * viewiing of the node data as well as modifying PJoints
     */
    public void actionNodeProperties() {
        if(currentSelection == null) {
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(editor != null)
            editor.dispose();

        editor = new JFrame();

        editorPanel = new PNodePropertyPanel(null);
        editorPanel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent arg0) {
                if(arg0.getPropertyName().equals("RESIZED")) {
                    editor.setSize(((Integer)arg0.getOldValue()).intValue(), ((Integer)arg0.getNewValue()).intValue());
                }
            }
        });

        editorPanel.setWorldManager(worldManager);
        editorPanel.setTargetNode(currentSelection);
        editorPanel.refreshComponents();

        double dScreenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        double dScreenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        int iXPos = (int) dScreenWidth - 500;//editor.getSize().width * 2;
        int iYPos = (int) dScreenHeight - 500;//editor.getSize().height * 2;

        editor.setLocation(iXPos, iYPos);
        editor.setVisible(true);
        editor.add(editorPanel);
        editorPanel.setVisible(true);
    }
    
    /**
     * Adds a PJoint node to the selected node
     */
    public void actionAddPJoint() {
        if(currentSelection == null) {
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        jointName = (String)JOptionPane.showInputDialog(new Frame(), "Please input the joint name",
                                                          "ADD A PJOINT", JOptionPane.YES_NO_CANCEL_OPTION,
                                                          null, null, "NewJoint"+jointCount);
        
        if( (jointName != null) && (jointName.length() > 0)  ) {
            if(jointName.equals("NewJoint"+jointCount))
                jointCount++;
            
            PJoint newJoint = new PJoint(new PTransform());
            newJoint.setName(jointName);

            DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(newJoint);

            currentSelection.addChild(newJoint);

            selectedDMT.add(dmt);
            treeModel.nodeStructureChanged(selectedDMT);
        }
        
        if(jointName == null || jointName.length() < 0)
            jointName = "NewJoint";
    }
    
    /**
     * Adds a ModelInstance at the selected node as a child of the selected node
     * WARNING: there is a race condition where the SharedAsset is ready before
     * it's asset and so it is not created
     */
    public void actionAddModelInstance() {
//        if(currentSelection == null) {
//            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        modelName = (String)JOptionPane.showInputDialog(new Frame(), "Please input the modelinstance name",
//                                                  "ADD A MODELINSTANCE", JOptionPane.YES_NO_CANCEL_OPTION,
//                                                  null, null, "NewModel"+modelCount);
//
//        if( (modelName != null) && (modelName.length() > 0)  ) {
////            int retValModel = jFileChooser_LoadModels.showOpenDialog(editor);
////            if (retValModel == JFileChooser.APPROVE_OPTION) {
////                File fileModel = jFileChooser_LoadModels.getSelectedFile();
////                sceneData.setfileModel(fileModel);
////                sceneData.setModelName(modelName);
////                if (fileModel.getName().endsWith(".ms3d")) {
////                    sceneData.loadMS3DFile(0, false, menu);
////                } else {
////                    sceneData.loadMeshDAEFile(true, menu);
////                }
////                addToNode();
////            }
//
//            sceneData.loadMeshDAEFile(true, menu, currentSelection);
//            if(modelName.equals("NewModel"+modelCount))
//                modelCount++;
//        }
//
//        if(modelName == null || modelName.length() < 0)
//            modelName = "NewModel";
    }
    
    /**
     * Delete the current node and all subsuquent child nodes below it (prunes)
     */
    public void actionDeleteNode() {
        if(currentSelection == null)
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if( !(currentSelection instanceof PScene) && !(currentSelection instanceof SkinnedMeshJoint) && !(currentSelection.getName().equals("m_TransformHierarchy")) ){
            treeModel.removeNodeFromParent(selectedDMT);
            pscene.findAndRemoveChild(currentSelection);
            PNode temp = pscene.findChild(currentSelection.getName());
            if(temp == null) { System.out.println("node has been removed"); }
        } else {
            JOptionPane.showMessageDialog(new Frame(), "You can not delete this node", "WARNING", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Make a copy of the current selected node (minus children) in a temporary
     * container so it can be placed elsewhere later
     */
    public void actionCopyNode() {
        if(currentSelection == null)
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if(!menu.getComponent(4).isVisible())
            JOptionPane.showMessageDialog(new Frame(), "This option is not yet enabled...", "WARNING", JOptionPane.WARNING_MESSAGE);
        else {
            copyNode = currentSelection;
            //copyNode.removeAllChildren();
        }
    }
    
    /**
     * Make a copy of the current selected node and everything under it in a
     * temporary container so it can be placed elsewhere later
     */
    public void actionCopyBranch() {
        if(currentSelection == null)
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if(!menu.getComponent(5).isVisible())
            JOptionPane.showMessageDialog(new Frame(), "This option is not yet enabled...", "WARNING", JOptionPane.WARNING_MESSAGE);
        else {
            copyNode = currentSelection;
        }
    }
    
    /**
     * Removes the selected node and everything under it from the PScene and
     * places it in a temporary containter so it can be placed elsewhere later
     */
    public void actionCutBranch() {
        if(currentSelection == null)
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if(!menu.getComponent(6).isVisible())
            JOptionPane.showMessageDialog(new Frame(), "This option is not yet enabled...", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if( !(currentSelection instanceof PScene) && !(currentSelection instanceof SkinnedMeshJoint) && !(currentSelection.getName().equals("m_TransformHierarchy")) ){
            copyNode = currentSelection;
            ((PPolygonModelInstance)copyNode).setRenderStop(true);
            treeModel.removeNodeFromParent(selectedDMT);
        } else {
            JOptionPane.showMessageDialog(new Frame(), "You can not copy/delete this node", "WARNING", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Takes the item stored in the temporary container and adds it into the
     * scene at the selected node
     */
    public void actionPaste() {
        if(currentSelection == null)
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if(!menu.getComponent(7).isVisible())
            JOptionPane.showMessageDialog(new Frame(), "This option is not yet enabled...", "WARNING", JOptionPane.WARNING_MESSAGE);
        else if(copyNode != null && menu.getComponent(7).isVisible()) {
            DefaultMutableTreeNode model = null;
            if(copyNode.getChildrenCount() > 0) {
                ModelInstanceProcessor proc = new ModelInstanceProcessor();
                proc.setTopNode(copyNode);
                TreeTraverser.breadthFirst(copyNode, proc);
                model = proc.getTopNode();
            } else {
                model = new DefaultMutableTreeNode(copyNode);
            }
            
            selectedDMT.add(model);
            treeModel.nodeStructureChanged(selectedDMT);
            
            currentSelection.addChild(copyNode);
            ((PPolygonModelInstance)copyNode).setRenderStop(false);
        }            
    }

    public void actionMatrixView() {
        if(currentSelection == null) {
            JOptionPane.showMessageDialog(new Frame(), "You have not selected a valid node", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(matrixExplorer != null)
            matrixExplorer.dispose();

        matrixExplorer = new JFrame_MatrixExplorer(currentSelection);
        matrixExplorer.setVisible(true);
    }
    
    /**
     * Initializes the GUI components that make up the popup menu
     */
    public void initComponents() {
                // POPUP MENU GUI
        menuItems = new String[9];
        menu = new JPopupMenu();
        
        JMenuItem matrixEditor = new JMenuItem("Node Properties");
        matrixEditor.addActionListener(this);
        menu.add(matrixEditor);
        menuItems[0] = matrixEditor.getText();
        
        JMenuItem addPJoint = new JMenuItem("Add PJoint");
        addPJoint.addActionListener(this);
        menu.add(addPJoint);
        menuItems[1] = addPJoint.getText();
        
        JMenuItem addModel = new JMenuItem("Add ModelInstance");
        addModel.addActionListener(this);
        menu.add(addModel);
        menuItems[2] = addModel.getText();
        
        JMenuItem deleteNode = new JMenuItem("Delete Node");
        deleteNode.addActionListener(this);
        menu.add(deleteNode);
        menuItems[3] = deleteNode.getText();
        
        JMenuItem nodeCopy = new JMenuItem("Copy Node");
        nodeCopy.addActionListener(this);
        nodeCopy.setVisible(false);
        menu.add(nodeCopy);
        menuItems[4] = nodeCopy.getText();
        
        JMenuItem copyBranch = new JMenuItem("Copy Branch");
        copyBranch.addActionListener(this);
        copyBranch.setVisible(false);
        menu.add(copyBranch);
        menuItems[5] = copyBranch.getText();
        
        JMenuItem cutBranch = new JMenuItem("Cut Branch");
        cutBranch.addActionListener(this);
        menu.add(cutBranch);
        menuItems[6] = cutBranch.getText();
        
        JMenuItem paste = new JMenuItem("Paste");
        paste.addActionListener(this);
        paste.setVisible(false);
        menu.add(paste);
        menuItems[7] = paste.getText();

        JMenuItem MatrixView = new JMenuItem("View Matrices");
        MatrixView.addActionListener(this);
        menu.add(MatrixView);
        menuItems[8] = MatrixView.getText();
        
        // FILE IO GUI
        FileFilter modelFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".ms3d") || f.getName().toLowerCase().endsWith(".dae")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                String szDescription = "Models (*.ms3d, *.dae)";
                return szDescription;
            }
        };
        jFileChooser_LoadModels = new javax.swing.JFileChooser();

        jFileChooser_LoadModels.setDialogTitle("Load Model File");
        java.io.File modDirectory = new java.io.File("./assets/models");
        jFileChooser_LoadModels.setCurrentDirectory(modDirectory);
        jFileChooser_LoadModels.setDoubleBuffered(true);

        jFileChooser_LoadModels.setDragEnabled(true);
        jFileChooser_LoadModels.addChoosableFileFilter(((FileFilter)modelFilter));
    }
    
    /**
     * Default constructor that creates a JPopupMenu and adds in JMenuItems.
     * Adds listeners to the individual JMenuItems.
     */
    public TreePopUpMenu() {
        initComponents();
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        displayMenu(e);
    }
    
    /**
     * Displays the JPopupMenu for the selected component at the components
     * location
     * @param e (MouseEvent)
     */
    public void displayMenu(MouseEvent e) {
        if(e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e)) {
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
            actionNodeProperties();
    }
    
    /**
     * Based on the menu item selected; this method will perform the specified
     * action selected
     * @param arg0 (ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
        int iSwitch = 0;
        for(int i = 0; i < menuItems.length; i++) {
            if(arg0.getActionCommand().equals(menuItems[i])) {
                iSwitch = i;
                break;
            }
        }
        
        switch(iSwitch)
        {
            case 0:
            {
                actionNodeProperties();
                break;
            }
            case 1:
            {
                actionAddPJoint();
                break;
            }
            case 2:
            {
                actionAddModelInstance();
                break;
            }
            case 3:
            {
                actionDeleteNode();
                break;
            }
            case 4:
            {
                actionCopyNode();
                break;
            }
            case 5:
            {
                actionCopyBranch();
                break;
            }
            case 6:
            {
                actionCutBranch();
                menu.getComponent(7).setVisible(true);
                break;
            }
            case 7:
            {
                actionPaste();
                menu.getComponent(7).setVisible(false);
                break;
            }
            case 8:
            {
                actionMatrixView();
                break;
            }
        }
    }
}
