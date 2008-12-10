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
    
    private final ArrayList<AbstractShaderProgram> m_defaultShaders = new ArrayList<AbstractShaderProgram>(6);
    
    /**
     * Construct a new shader property panel!
     * @param shader The shader to operate on
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jToolBar1 = new javax.swing.JToolBar();
        JLabel_Defaults = new javax.swing.JLabel();
        JComboBox_DefaultShaderPrograms = new javax.swing.JComboBox();
        JButton_LoadDefaultShader = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(420, 311));
        setMinimumSize(new java.awt.Dimension(420, 311));
        setPreferredSize(new java.awt.Dimension(420, 311));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        JLabel_Defaults.setText("Defaults:");
        jToolBar1.add(JLabel_Defaults);

        String[] labels = new String[m_defaultShaders.size()];
        for (int i = 0; i < m_defaultShaders.size(); ++i)
        labels[i] = m_defaultShaders.get(i).getClass().getSimpleName();
        JComboBox_DefaultShaderPrograms.setModel(new DefaultComboBoxModel(labels));
        jToolBar1.add(JComboBox_DefaultShaderPrograms);

        JButton_LoadDefaultShader.setText("Load");
        JButton_LoadDefaultShader.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSelectedDefaultShader(evt);
            }
        });
        jToolBar1.add(JButton_LoadDefaultShader);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 264, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20))
        );
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
