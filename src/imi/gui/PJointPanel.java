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

/**
 * This form contains widgets specific to the PJoint class. The available 
 * controls are two PMatrixWidgets.
 * @author  Ronald E Dahlgren
 */
public class PJointPanel extends javax.swing.JPanel implements PNodeSubtypeToolkit 
{
    // Widget dimensions
    public static int TOOLKIT_HEIGHT = 380;
    public static int TOOLKIT_WIDTH = 505;
    
    // The data model to operate on
    private PJoint  m_Model = null;
    
    /**
     * Operate on the provided joint.
     * @param joint The target
     */
    public PJointPanel(PJoint joint) 
    {
        m_Model = joint;
        initComponents();
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PMatrixWidget_Local = new PMatrixWidget(m_Model.getTransform().getLocalMatrix(true), "Local Transform");
        PMatrixWidget_Modifier = new PMatrixWidget(m_Model.getLocalModifierMatrix(), "Modification Matrix");

        setMaximumSize(new java.awt.Dimension(500, 370));
        setMinimumSize(new java.awt.Dimension(500, 370));
        setPreferredSize(new java.awt.Dimension(500, 370));

        PMatrixWidget_Local.setToolTipText("Local Transform Properties");

        PMatrixWidget_Modifier.setToolTipText("Modifier Matrix Properties");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(PMatrixWidget_Local, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(PMatrixWidget_Modifier, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, PMatrixWidget_Local, 0, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, PMatrixWidget_Modifier, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 350, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.PMatrixWidget PMatrixWidget_Local;
    private imi.gui.PMatrixWidget PMatrixWidget_Modifier;
    // End of variables declaration//GEN-END:variables

    public Integer getDefaultWidth() {
        return Integer.valueOf(TOOLKIT_WIDTH);
    }

    public void setDefaultWidth(Integer width) {
        // not implemented
    }

    public Integer getDefaultHeight() {
        return Integer.valueOf(TOOLKIT_HEIGHT);
    }

    public void setDefaultHeight(Integer height) {
        // Not implemented
    }

    public Integer getInstanceHeight() {
        return getDefaultHeight();
    }

    public void setInstanceHeight(Integer height) {
        // Not implemented
    }

    public Integer getInstanceWidth() {
        return getDefaultWidth();
    }

    public void setInstanceWidth(Integer width) {
        // Not implemented
    }

    public PNode getTarget() {
        return m_Model;
    }

    /**
     * Swaps out the data model, cascades to child widgets
     * @param targetNode The new data model.
     */
    public void setTarget(PNode targetNode) {
        if (targetNode != null && targetNode instanceof PJoint)
        {
            m_Model = (PJoint)targetNode;
            PMatrixWidget_Local.setTargetMatrix(targetNode.getTransform().getLocalMatrix(true));
            PMatrixWidget_Modifier.setTargetMatrix(((PJoint)targetNode).getLocalModifierMatrix());
            PMatrixWidget_Local.repaint();
            PMatrixWidget_Modifier.repaint();
        }
    }

}
