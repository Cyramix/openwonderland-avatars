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

import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import org.jdesktop.mtgame.WorldManager;

/**
 * This widget is intended to be used for tweaking the state of a 
 * PPolygonMeshInstance or derivative. It is intended for use as a 
 * PNodeSubtypeToolkit by the PNodePropertyPanel.
 * @author  Ronald E Dahlgren
 */
public class MeshInstancePropertyPanel extends javax.swing.JPanel implements PNodeSubtypeToolkit
{   
    // Widget dimensions for the class
    public static int TOOLKIT_HEIGHT = 400;
    public static int TOOLKIT_WIDTH = 670;
    
    private PPolygonMeshInstance    m_meshInst = null; // The data model
    private WorldManager            m_wm = null;
    /** Creates new form MeshInstanceWidget */
    public MeshInstancePropertyPanel() 
    {
        initComponents();
    }
    
    public MeshInstancePropertyPanel(PPolygonMeshInstance meshInst)
    {
        m_meshInst = meshInst;
        initComponents();
    }
    
    public MeshInstancePropertyPanel(PPolygonMeshInstance meshInst, WorldManager wm)
    {
        m_meshInst = meshInst;
        setWM(wm);
        initComponents();
    }

    public void setWM(WorldManager worldm) { m_wm = worldm; }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_matrixWidget = new imi.gui.PMatrixWidget();
        m_materialPanel = new PMeshMaterialPanel(m_wm);

        setMaximumSize(new java.awt.Dimension(670, 345));
        setMinimumSize(new java.awt.Dimension(670, 345));
        setPreferredSize(new java.awt.Dimension(670, 345));
        setLayout(new java.awt.GridBagLayout());
        add(m_matrixWidget, new java.awt.GridBagConstraints());
        add(m_materialPanel, new java.awt.GridBagConstraints());
        m_materialPanel.setOwningMesh(m_meshInst);
        m_materialPanel.setTargetMaterial(m_meshInst.getMaterialRef().getMaterial());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.PMeshMaterialPanel m_materialPanel;
    private imi.gui.PMatrixWidget m_matrixWidget;
    // End of variables declaration//GEN-END:variables


    private void refreshComponents()
    {
        // update the matrix widget
        m_matrixWidget.setTargetMatrix(m_meshInst.getTransform().getLocalMatrix(true));
        // update the material widget
        m_materialPanel.setTargetMaterial(m_meshInst.getMaterialRef().getMaterial());
        m_materialPanel.setOwningMesh(m_meshInst);
    }

    public PNode getTarget()
    {
        return m_meshInst;
    }

    public void setTarget(PNode targetNode)
    {
        if (targetNode instanceof PPolygonMeshInstance)
            m_meshInst = (PPolygonMeshInstance) targetNode;
        else
            m_meshInst = null;
        refreshComponents();
    }

    public Integer getDefaultWidth()
    {
        return TOOLKIT_WIDTH;
    }

    public void setDefaultWidth(Integer width)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Integer getDefaultHeight()
    {
        return TOOLKIT_HEIGHT;
    }

    public void setDefaultHeight(Integer height)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Integer getInstanceHeight()
    {
        return TOOLKIT_HEIGHT;
    }

    public void setInstanceHeight(Integer height)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Integer getInstanceWidth()
    {
        return TOOLKIT_WIDTH;
    }

    public void setInstanceWidth(Integer width)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}
