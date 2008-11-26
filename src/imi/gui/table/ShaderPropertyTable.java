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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.gui.table;

import imi.gui.table.JTableExtended;
import imi.gui.table.EdittableRowModel;
import imi.gui.table.ClassBasedCellRenderer;
import imi.gui.table.celleditor.FloatTextField;
import imi.gui.table.celleditor.FloatVectorCellEditor;
import imi.gui.table.celleditor.IntegerSpinBox;
import imi.gui.table.celleditor.IntegerVectorCellEditor;
import imi.gui.table.cellrenderer.BooleanCellRenderer;
import imi.gui.table.cellrenderer.FloatCellRenderer;
import imi.gui.table.cellrenderer.FloatVectorCellRenderer;
import imi.gui.table.cellrenderer.IntegerCellRenderer;
import imi.gui.table.cellrenderer.IntegerVectorCellRenderer;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  Ronald E Dahlgren
 */
public class ShaderPropertyTable extends javax.swing.JPanel
{
    private AbstractShaderProgram   m_targetShader  = null;
    private ShaderProperty[]        m_properties    = null;
    /** Creates new form ShaderPropertyTable */
    public ShaderPropertyTable()
    {
        initialize();
    }
    public ShaderPropertyTable(AbstractShaderProgram targetShader)
    {
        initComponents();
        initialize(); // Hard to live alongside netbeans =/
        setTargetShader(targetShader);
    }
    
    private void initialize()
    {
        // Shader property specific bindings
        JTableExtended jtx = (JTableExtended)JTable_Properties;
        jtx.setRowEditorModel(new EdittableRowModel());
        jtx.getRowEditorModel().addClassEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
        jtx.getRowEditorModel().addClassEditor(Integer.class, new IntegerSpinBox());
        jtx.getRowEditorModel().addClassEditor(Float.class, new FloatTextField(new JTextField(12)));
        jtx.getRowEditorModel().addClassEditor(int[].class, new IntegerVectorCellEditor());
        jtx.getRowEditorModel().addClassEditor(float[].class, new FloatVectorCellEditor());
        
        ClassBasedCellRenderer render = new ClassBasedCellRenderer();
        render.setClassRenderer(Boolean.class, new BooleanCellRenderer());
        render.setClassRenderer(Integer.class, new IntegerCellRenderer());
        render.setClassRenderer(Float.class, new FloatCellRenderer());
        render.setClassRenderer(int[].class, new IntegerVectorCellRenderer());
        render.setClassRenderer(float[].class, new FloatVectorCellRenderer());
        jtx.getColumnModel().getColumn(1).setCellRenderer(render);
    }
    
    public void setTargetShader(AbstractShaderProgram shader)
    {
        clear();
        m_targetShader = shader;
        repopulateTable();
    }
    
    public void repopulateTable()
    {
        if (m_targetShader == null)
        {
            System.out.println("null shader repopulated!");
            return;
        }
        else
        {
            m_properties = m_targetShader.getProperties();
            JTableExtended jtx = (JTableExtended)JTable_Properties;
            // resize
            ((DefaultTableModel)jtx.getModel()).setRowCount(m_properties.length);
            
            for (int i = 0; i < m_properties.length; ++i) // for each property
            {
                jtx.setValueAt(m_properties[i].name, i, 0);
                Object value = m_properties[i].getValue();
                if (value == null) // set a default!
                {
                    if (m_properties[i].type.getJavaType() == Integer.class)
                        jtx.setValueAt(Integer.valueOf(-1), i, 1);
                    else if (m_properties[i].type.getJavaType() == Float.class)
                        jtx.setValueAt(Float.valueOf(0.0f), i, 1);
                    else if (m_properties[i].type.getJavaType() == int[].class)
                        jtx.setValueAt(new int[1], i, 1);
                    else // float array
                        jtx.setValueAt(new float[1], i, 1);
                }
                else
                    jtx.setValueAt(value, i, 1);
            }
        }
    }
    
    public void clear()
    {
        m_properties = null;
        m_targetShader = null;
        ((DefaultTableModel)JTable_Properties.getModel()).setRowCount(0);
    }
    
    public boolean setPropertiesOnShader()
    {
        if (m_targetShader == null || m_properties == null)
            return false;
        for (ShaderProperty prop : m_properties)
        {
            try
            {
                m_targetShader.setProperty(prop);
            } catch (NoSuchPropertyException ex)
            {
                Logger.getLogger(ShaderPropertyTable.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }
    
    private void applyTableToShader()
    {
        if (m_properties == null)
            return;
        for (int i = 0; i < m_properties.length; ++i)
            m_properties[i].setValue(JTable_Properties.getValueAt(i, 1));
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        JTable_Properties = new imi.gui.table.JTableExtended();
        JButton_Apply = new javax.swing.JButton();

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JTable_Properties.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null}
            },
            new String [] {
                "Property Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        JTable_Properties.setRowHeight(30);
        jScrollPane1.setViewportView(JTable_Properties);

        JButton_Apply.setText("Apply");
        JButton_Apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(JButton_Apply)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(JButton_Apply)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void applyButtonClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonClicked
    applyTableToShader();
    setPropertiesOnShader();
}//GEN-LAST:event_applyButtonClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton JButton_Apply;
    private javax.swing.JTable JTable_Properties;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}


