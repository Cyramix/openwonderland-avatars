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
import imi.scene.polygonmodel.PPolygonMeshInstance;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.jdesktop.mtgame.WorldManager;

/**
 * This form shows the appropriate knobs and switches
 * for a particular PNode (or derivative)
 * @author  Ronald E Dahlgren
 */
public class PNodePropertyPanel extends javax.swing.JPanel 
{

    private PNode               m_targetNode    = null; // The node to operate on
    private Timer               m_refreshTimer  = null; // Sends events to refresh the components
    private PNodeSubtypeToolkit m_nodeToolKit   = null; // Derived type widgets
    private int                 iHeight         = 500;  // Minimum window height
    private int                 iWidth          = 320;  // Minimum window width
    private WorldManager        wm              = null;
    
    /**
     * Creates default values with refresh mechanisms.
     * TODO: Add constructor overloads for receiving timer inputs.
     * @param targetNode The data
     */
    public PNodePropertyPanel(PNode targetNode) 
    {
        m_targetNode = targetNode;
        initComponents(); // Netbeans code
        populateSpecificControlPane(); // Determine the derived type of m_targetNode and insert the appropriate tool widget
        refreshComponents(); // Synchronize widget/model state
        // get our timer started
        m_refreshTimer = new  Timer(2000, new ActionListener() // TODO: abstract magic numbers
                            {
                                public void actionPerformed(ActionEvent e) 
                                {
                                    refreshComponents();
                                }
                            }); 
        m_refreshTimer.setInitialDelay(3000);
        m_refreshTimer.start();
    }
    
    /**
     * This method causes the state of all GUI objects to
     * be resynchronized to the target node (if they are not in focus currently)
     */
    public void refreshComponents()
    {
        if (m_targetNode == null) // No target, leave hideous defaults
            return;
        // Otherwise, let's update our state
        if (JTextField_NodeName.hasFocus() == false)
            JTextField_NodeName.setText(m_targetNode.getName());
        // do we have a parent?
        if (m_targetNode.getParent() == null)
            JLabel_ParentNodeName.setText("(null)");
        else
            JLabel_ParentNodeName.setText(m_targetNode.getParent().getName());
        JLabel_ChildCount.setText(Integer.toString(m_targetNode.getChildrenCount()));
        
        int iIndex  = m_targetNode.getClass().getName().lastIndexOf(".");
        int iLength = m_targetNode.getClass().getName().length();
        String szName = m_targetNode.getClass().getName().substring(iIndex+1, iLength);
        JLabel_NodeType.setText(szName);
        
        // init check boxes if not in focus
        if (JCheckBox_Dirty.hasFocus() == false)
            JCheckBox_Dirty.setSelected(m_targetNode.isDirty());
        if (JCheckBox_RenderStop.hasFocus() == false)
            JCheckBox_RenderStop.setSelected(m_targetNode.getRenderStop());
        
        // Reference count
        JLabel_RefCount.setText(Integer.toString(m_targetNode.getReferenceCount()));

    }
    
    /**
     * This method clears the JPanel_SpecificControls of old components
     * and inserts a widget toolkit for the appropriate derived type.
     */
    public void populateSpecificControlPane()
    {
        // remove old controls
        JPanel_SpecificControls.removeAll();
        m_nodeToolKit = null;

        if (m_targetNode == null) // no target, no toolkit
            return;
        else if (m_targetNode instanceof PJoint) // Only implementation so far
        {
            PJointPanel panel = new PJointPanel((PJoint)m_targetNode);
            panel.setSize(new Dimension(panel.getDefaultWidth(), panel.getDefaultHeight()));
            JPanel_SpecificControls.add(panel);
            JPanel_SpecificControls.setSize(panel.getDefaultWidth(), panel.getDefaultHeight());
            m_nodeToolKit = (PNodeSubtypeToolkit)panel;
            panel.setVisible(true);
        }
        else if (m_targetNode instanceof PPolygonMeshInstance)
        {
            MeshInstancePropertyPanel panel = new MeshInstancePropertyPanel((PPolygonMeshInstance)m_targetNode, wm);
            //panel.setWM(wm);
            panel.setSize(new Dimension(panel.getDefaultWidth(), panel.getDefaultHeight()));
            JPanel_SpecificControls.add(panel);
            JPanel_SpecificControls.setSize(panel.getDefaultWidth(), panel.getDefaultHeight());
            m_nodeToolKit = (PNodeSubtypeToolkit)panel;
            panel.setVisible(true);
        }
        else // Must be a standard PNode or a currently unsupported type, let's query
        {
            if (m_targetNode.getTransform() != null)
            {
                // give a matrix widget
                PMatrixWidget matWidget = new PMatrixWidget(m_targetNode.getTransform().getLocalMatrix(false), "Local Transform");
                matWidget.setSize(245, 375);
                matWidget.setVisible(true);
                JPanel_SpecificControls.add(matWidget);
                JPanel_SpecificControls.setSize(matWidget.getWidth(), matWidget.getHeight());
                this.firePropertyChange("RESIZED", Integer.valueOf(500), Integer.valueOf(matWidget.getHeight() + 150));
                return;
            }
            JPanel_SpecificControls.removeAll();
            this.firePropertyChange("RESIZED", Integer.valueOf(500), Integer.valueOf(200));
            return;
        }

        if(m_nodeToolKit != null) 
        {
            int iComponentHeight = 150 + JPanel_SpecificControls.getHeight();
            int iComponentWidth  = JPanel_SpecificControls.getWidth();
//            JPanel_SpecificControls.setSize(new Dimension(iComponentWidth, iComponentHeight));
            if(iComponentHeight < iHeight)
                iComponentHeight = iHeight;
            if(iComponentWidth < iWidth)
                iComponentWidth = iWidth;
            this.firePropertyChange("RESIZED", iComponentWidth, iComponentHeight);
        }
    }
    
    /**
     * This method sets the target for this node. The node to "operate" on.
     * @param targetNode
     */
    public void setTargetNode(PNode targetNode)
    {
        m_targetNode = targetNode;
        populateSpecificControlPane();
        if (m_nodeToolKit != null)
            m_nodeToolKit.setTarget(targetNode);
    }
    
    public void setWorldManager(WorldManager worldm) { wm = worldm; } 
    
    /**
     * Returns the node currently being tweaked.
     * @return m_targetNode (PNode)
     */
    public PNode getTargetNode()
    {
        return m_targetNode;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        JPanel_BaseNodeProperties = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        JTextField_NodeName = new javax.swing.JTextField();
        jToolBar2 = new javax.swing.JToolBar();
        JCheckBox_Dirty = new javax.swing.JCheckBox();
        jToolBar3 = new javax.swing.JToolBar();
        jLabel2 = new javax.swing.JLabel();
        JLabel_ParentNodeName = new javax.swing.JLabel();
        jToolBar4 = new javax.swing.JToolBar();
        JCheckBox_RenderStop = new javax.swing.JCheckBox();
        jToolBar5 = new javax.swing.JToolBar();
        jLabel4 = new javax.swing.JLabel();
        JLabel_ChildCount = new javax.swing.JLabel();
        jToolBar6 = new javax.swing.JToolBar();
        jLabel3 = new javax.swing.JLabel();
        JLabel_RefCount = new javax.swing.JLabel();
        jToolBar7 = new javax.swing.JToolBar();
        jLabel5 = new javax.swing.JLabel();
        JLabel_NodeType = new javax.swing.JLabel();
        JPanel_SpecificControls = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(2000, 2000));
        setMinimumSize(new java.awt.Dimension(300, 300));
        setPreferredSize(new java.awt.Dimension(670, 365));

        JPanel_BaseNodeProperties.setMinimumSize(new java.awt.Dimension(321, 150));
        JPanel_BaseNodeProperties.setPreferredSize(new java.awt.Dimension(321, 150));
        JPanel_BaseNodeProperties.setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMinimumSize(new java.awt.Dimension(200, 32));
        jToolBar1.setPreferredSize(new java.awt.Dimension(200, 32));

        jLabel1.setText("Name: ");
        jLabel1.setFocusCycleRoot(true);
        jLabel1.setFocusTraversalPolicyProvider(true);
        jToolBar1.add(jLabel1);

        JTextField_NodeName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTextField_NodeNameActionPerformed(evt);
            }
        });
        jToolBar1.add(JTextField_NodeName);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar1, gridBagConstraints);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setMaximumSize(new java.awt.Dimension(100, 32));
        jToolBar2.setMinimumSize(new java.awt.Dimension(100, 32));
        jToolBar2.setPreferredSize(new java.awt.Dimension(100, 32));

        JCheckBox_Dirty.setText("Dirty          ");
        JCheckBox_Dirty.setToolTipText("Sets the dirty boolean for this node.");
        JCheckBox_Dirty.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        JCheckBox_Dirty.setIconTextGap(6);
        JCheckBox_Dirty.setMargin(new java.awt.Insets(0, 0, 0, 0));
        JCheckBox_Dirty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JCheckBox_DirtyActionPerformed(evt);
            }
        });
        jToolBar2.add(JCheckBox_Dirty);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar2, gridBagConstraints);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);
        jToolBar3.setMaximumSize(new java.awt.Dimension(200, 32));
        jToolBar3.setMinimumSize(new java.awt.Dimension(200, 32));
        jToolBar3.setPreferredSize(new java.awt.Dimension(200, 32));

        jLabel2.setText("Parent: ");
        jToolBar3.add(jLabel2);

        JLabel_ParentNodeName.setFont(new java.awt.Font("Courier New", 1, 13));
        JLabel_ParentNodeName.setForeground(new java.awt.Color(0, 0, 255));
        JLabel_ParentNodeName.setText("PARENT NODE NAME");
        jToolBar3.add(JLabel_ParentNodeName);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar3, gridBagConstraints);

        jToolBar4.setFloatable(false);
        jToolBar4.setRollover(true);
        jToolBar4.setMaximumSize(new java.awt.Dimension(100, 32));
        jToolBar4.setMinimumSize(new java.awt.Dimension(100, 32));
        jToolBar4.setPreferredSize(new java.awt.Dimension(100, 32));

        JCheckBox_RenderStop.setText("RenderStop");
        JCheckBox_RenderStop.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        JCheckBox_RenderStop.setIconTextGap(0);
        JCheckBox_RenderStop.setMargin(new java.awt.Insets(0, 0, 0, 0));
        JCheckBox_RenderStop.setMaximumSize(new java.awt.Dimension(88, 23));
        JCheckBox_RenderStop.setMinimumSize(new java.awt.Dimension(88, 23));
        JCheckBox_RenderStop.setPreferredSize(new java.awt.Dimension(88, 23));
        JCheckBox_RenderStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JCheckBox_RenderStopActionPerformed(evt);
            }
        });
        jToolBar4.add(JCheckBox_RenderStop);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar4, gridBagConstraints);

        jToolBar5.setFloatable(false);
        jToolBar5.setRollover(true);
        jToolBar5.setMaximumSize(new java.awt.Dimension(200, 32));
        jToolBar5.setMinimumSize(new java.awt.Dimension(200, 32));
        jToolBar5.setPreferredSize(new java.awt.Dimension(200, 32));

        jLabel4.setText("Children:");
        jLabel4.setMaximumSize(new java.awt.Dimension(66, 16));
        jLabel4.setMinimumSize(new java.awt.Dimension(66, 16));
        jLabel4.setPreferredSize(new java.awt.Dimension(66, 16));
        jToolBar5.add(jLabel4);

        JLabel_ChildCount.setFont(new java.awt.Font("Courier New", 1, 12));
        JLabel_ChildCount.setText("##");
        jToolBar5.add(JLabel_ChildCount);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar5, gridBagConstraints);

        jToolBar6.setFloatable(false);
        jToolBar6.setRollover(true);
        jToolBar6.setMaximumSize(new java.awt.Dimension(200, 32));
        jToolBar6.setMinimumSize(new java.awt.Dimension(200, 32));
        jToolBar6.setPreferredSize(new java.awt.Dimension(200, 32));

        jLabel3.setText("RefCount: ");
        jToolBar6.add(jLabel3);

        JLabel_RefCount.setFont(new java.awt.Font("Courier New", 1, 12));
        JLabel_RefCount.setText("##");
        jToolBar6.add(JLabel_RefCount);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar6, gridBagConstraints);

        jToolBar7.setFloatable(false);
        jToolBar7.setRollover(true);
        jToolBar7.setMaximumSize(new java.awt.Dimension(300, 32));
        jToolBar7.setMinimumSize(new java.awt.Dimension(300, 32));
        jToolBar7.setPreferredSize(new java.awt.Dimension(300, 32));

        jLabel5.setText("Node Type: ");
        jToolBar7.add(jLabel5);

        JLabel_NodeType.setFont(new java.awt.Font("Courier New", 1, 13));
        JLabel_NodeType.setForeground(new java.awt.Color(255, 0, 0));
        JLabel_NodeType.setText("TYPE OF NODE GOES HERE");
        jToolBar7.add(JLabel_NodeType);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        JPanel_BaseNodeProperties.add(jToolBar7, gridBagConstraints);

        JPanel_SpecificControls.setMinimumSize(new java.awt.Dimension(321, 150));
        JPanel_SpecificControls.setPreferredSize(new java.awt.Dimension(670, 365));

        org.jdesktop.layout.GroupLayout JPanel_SpecificControlsLayout = new org.jdesktop.layout.GroupLayout(JPanel_SpecificControls);
        JPanel_SpecificControls.setLayout(JPanel_SpecificControlsLayout);
        JPanel_SpecificControlsLayout.setHorizontalGroup(
            JPanel_SpecificControlsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 670, Short.MAX_VALUE)
        );
        JPanel_SpecificControlsLayout.setVerticalGroup(
            JPanel_SpecificControlsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 365, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(JPanel_BaseNodeProperties, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(JPanel_SpecificControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(JPanel_BaseNodeProperties, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(JPanel_SpecificControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void JTextField_NodeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JTextField_NodeNameActionPerformed
    // set the node's name to the current value of the text field
    m_targetNode.setName(JTextField_NodeName.getText());
}//GEN-LAST:event_JTextField_NodeNameActionPerformed

private void JCheckBox_DirtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCheckBox_DirtyActionPerformed
    m_targetNode.setDirty(JCheckBox_Dirty.isSelected(), true);
}//GEN-LAST:event_JCheckBox_DirtyActionPerformed

private void JCheckBox_RenderStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JCheckBox_RenderStopActionPerformed
    m_targetNode.setRenderStop(JCheckBox_RenderStop.isSelected());
}//GEN-LAST:event_JCheckBox_RenderStopActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox JCheckBox_Dirty;
    private javax.swing.JCheckBox JCheckBox_RenderStop;
    private javax.swing.JLabel JLabel_ChildCount;
    private javax.swing.JLabel JLabel_NodeType;
    private javax.swing.JLabel JLabel_ParentNodeName;
    private javax.swing.JLabel JLabel_RefCount;
    private javax.swing.JPanel JPanel_BaseNodeProperties;
    private javax.swing.JPanel JPanel_SpecificControls;
    private javax.swing.JTextField JTextField_NodeName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar5;
    private javax.swing.JToolBar jToolBar6;
    private javax.swing.JToolBar jToolBar7;
    // End of variables declaration//GEN-END:variables

}
