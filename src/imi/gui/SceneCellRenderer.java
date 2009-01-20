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

////////////////////////////////////////////////////////////////////////////////
// Imports
////////////////////////////////////////////////////////////////////////////////
import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author Paul Viet Nguyen Truong
 */
public class SceneCellRenderer extends DefaultTreeCellRenderer {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    /** Icons for the Nodes */
    Icon pscene     = new ImageIcon("assets/icons/pscene.png");
    Icon pnode_r    = new ImageIcon("assets/icons/pnode.png");
    Icon model_r    = new ImageIcon("assets/icons/model_r.png");
    Icon model_s    = new ImageIcon("assets/icons/model_s.png");
    Icon mesh_r     = new ImageIcon("assets/icons/mesh_r.png");
    Icon mesh_s     = new ImageIcon("assets/icons/mesh_s.png");
    Icon joint_r    = new ImageIcon("assets/icons/joint_r.png");
    Icon joint_s    = new ImageIcon("assets/icons/joint_s.png");
    /** Rendering Components */
    JPanel renderPanel;
    JLabel nodeInfo;
    DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor calls the parent's constructor before it executes it's
     * own initializations.  Sets a panel ad a label as the the cell rendering bg
     */
    public SceneCellRenderer() {
        super();
        renderPanel = new JPanel(new GridLayout(0, 1));
        nodeInfo = new JLabel("");
        renderPanel.add(nodeInfo);
    }

    /**
     * Sets a icon to be rendered based on the node being rendered for that cell
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     * @param arg5
     * @param arg6
     * @return
     */
    @Override
    public Component getTreeCellRendererComponent(JTree arg0, Object arg1, boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
        super.getTreeCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)arg1;
        PNode pnode = ((PNode)node.getUserObject());
        int index = pnode.getClass().getName().lastIndexOf(".");
        String derivedtype = pnode.getClass().getName().substring(index+1);
        String descriptor = pnode.getName();
        
        if(pnode instanceof PScene)
            nodeInfo.setIcon(pscene);
            //setIcon(pscene);
        else if(pnode instanceof PPolygonModelInstance)
            nodeInfo.setIcon(model_s);
            //setIcon(model_s);
        else if(pnode instanceof PPolygonSkinnedMeshInstance)
            nodeInfo.setIcon(mesh_s);
            //setIcon(mesh_s);
        else if(pnode instanceof PPolygonMeshInstance)
            nodeInfo.setIcon(mesh_r);
            //setIcon(mesh_r);        
        else if(pnode instanceof SkinnedMeshJoint)
            nodeInfo.setIcon(joint_s);
            //setIcon(joint_s);
        else if(pnode instanceof PJoint)
            nodeInfo.setIcon(joint_r);
            //setIcon(joint_r);
        else
            nodeInfo.setIcon(pnode_r);
            //setIcon(pnode_r);

        if(arg2)
            renderPanel.setBackground(defaultRenderer.getBackgroundSelectionColor());
        else
            renderPanel.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
        
        nodeInfo.setText("[" + derivedtype + "]  " + descriptor);
        return renderPanel;
    }
}
