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

import imi.gui.table.ShaderPropertyTable;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.programs.ClothingShaderDiffuseAsSpec;
import imi.scene.shader.programs.ClothingShaderSpecColor;
import imi.scene.shader.programs.EyeballShader;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.NormalMapShader;
import imi.scene.shader.programs.SimpleTNLShader;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.shader.programs.VertexDeformer;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.mtgame.WorldManager;

/**
 * This widget is used to expose the properties of an AbstractShaderProgram.
 * @author  Ronald E Dahlgren
 */
public class JPanel_ShaderProperties extends javax.swing.JPanel
{
    /**
     * Widget pointers
     */
    private ShaderPropertyTable m_propertyTable         = null;
    private ShaderEffectComposerPanel m_effectComposer  = null;
    /**
     * The data model
     */
    private AbstractShaderProgram       m_shader = null;
    
    private ArrayList<ShaderProperty>   m_shaderProperties = null;
    
    private WorldManager                m_wm = null;
    
    private final ArrayList<AbstractShaderProgram> m_defaultShaders = new ArrayList<AbstractShaderProgram>(8);
    
    /**
     * Default Constructor does nothing...
     * DO NOT USE THIS!!
     */
    public JPanel_ShaderProperties() {

    }

    /**
     * Construct a new shader property panel!
     * @param shader The shader to operate on
     * @param wm the worldmanager
     */
    public JPanel_ShaderProperties(AbstractShaderProgram shader, WorldManager wm) 
    {
        m_shader = shader;
        m_wm = wm;
        // add in default shaders
        // NOTE - These are intentionally hardcoded, as they are defaults!
        // The system could be modified to handle this dynamically using
        // SimpleClassPackageExplorer, unfortunately it does not support
        // sealed jars
        m_defaultShaders.add(new NormalAndSpecularMapShader(wm));
        m_defaultShaders.add(new NormalMapShader(wm));
        m_defaultShaders.add(new SimpleTNLShader(wm));
        m_defaultShaders.add(new SimpleTNLWithAmbient(wm));
        m_defaultShaders.add(new VertDeformerWithSpecAndNormalMap(wm));
        m_defaultShaders.add(new VertexDeformer(wm));
        m_defaultShaders.add(new EyeballShader(wm));
        m_defaultShaders.add(new ClothingShaderSpecColor(wm));
        m_defaultShaders.add(new FleshShader(wm));
        m_defaultShaders.add(new ClothingShaderDiffuseAsSpec(wm));
        initComponents(); // <-- do not trust netbeans auto-code garbage!
        loadPropertyTable();
        populatePanel();
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

        jToolBar1 = new javax.swing.JToolBar();
        JLabel_Defaults = new javax.swing.JLabel();
        JComboBox_DefaultShaderPrograms = new javax.swing.JComboBox();
        JButton_LoadDefaultShader = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        setMaximumSize(new java.awt.Dimension(380, 200));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(380, 200));
        setLayout(new java.awt.GridBagLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(380, 30));
        jToolBar1.setMinimumSize(new java.awt.Dimension(0, 0));
        jToolBar1.setPreferredSize(new java.awt.Dimension(150, 27));

        JLabel_Defaults.setText("Defaults:");
        jToolBar1.add(JLabel_Defaults);

        String[] labels = new String[m_defaultShaders.size()];
        for (int i = 0; i < m_defaultShaders.size(); ++i)
        labels[i] = m_defaultShaders.get(i).getClass().getSimpleName();
        JComboBox_DefaultShaderPrograms.setModel(new DefaultComboBoxModel(labels));
        JComboBox_DefaultShaderPrograms.setMinimumSize(new java.awt.Dimension(0, 0));
        JComboBox_DefaultShaderPrograms.setPreferredSize(new java.awt.Dimension(47, 25));
        jToolBar1.add(JComboBox_DefaultShaderPrograms);

        JButton_LoadDefaultShader.setText("Load");
        JButton_LoadDefaultShader.setMinimumSize(new java.awt.Dimension(0, 0));
        JButton_LoadDefaultShader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSelectedDefaultShader(evt);
            }
        });
        jToolBar1.add(JButton_LoadDefaultShader);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jToolBar1, gridBagConstraints);

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(380, 90));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void loadSelectedDefaultShader(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSelectedDefaultShader
    if (JComboBox_DefaultShaderPrograms.getSelectedIndex() != -1)
        setShader(m_defaultShaders.get(JComboBox_DefaultShaderPrograms.getSelectedIndex()));
}//GEN-LAST:event_loadSelectedDefaultShader


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton JButton_LoadDefaultShader;
    private javax.swing.JComboBox JComboBox_DefaultShaderPrograms;
    private javax.swing.JLabel JLabel_Defaults;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

    protected void loadPropertyTable()
    {
        // populate the property table if neeeded
        if (m_shader != null)
        {
            if (m_propertyTable == null)
            {
                m_propertyTable = new ShaderPropertyTable(m_shader);
                m_propertyTable.setSize(380, 125);
                m_propertyTable.setVisible(true);
                jTabbedPane1.add("Properties", m_propertyTable);
            }
            else
            {
                m_propertyTable.clear();
                m_propertyTable.setTargetShader(m_shader);
            }
        }
    }
    
    protected void populatePanel()
    {
        if (m_effectComposer == null)
        {
            m_effectComposer = new ShaderEffectComposerPanel();
            m_effectComposer.setOwningPanel(this);
            m_effectComposer.setVisible(true);
            jTabbedPane1.add("Effect Composer", m_effectComposer);
        }
        if (m_shader instanceof GLSLShaderProgram)
            m_effectComposer.setShaderProgram((GLSLShaderProgram)m_shader);
        else
            m_effectComposer.setShaderProgram(new GLSLShaderProgram());
    }
    
    public void setShader(AbstractShaderProgram shader)
    {
        m_shader = shader;
        loadPropertyTable();
        populatePanel();
    }
    
    public AbstractShaderProgram getShader()
    {
        return m_shader;
    }
}
