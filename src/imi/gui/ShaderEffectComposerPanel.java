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

import imi.shader.ShaderUtils;
import imi.shader.dynamic.GLSLCompileException;
import imi.shader.dynamic.GLSLShaderEffect;
import imi.shader.dynamic.GLSLShaderProgram;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

/**
 * This widget is used to handle to concatenation and compilation of 
 * shader effects into a complete vertex / fragment program pair.
 * @author  Ronald E Dahlgren
 */
public class ShaderEffectComposerPanel extends javax.swing.JPanel 
{
    /** The program to operate on **/
    private GLSLShaderProgram   m_shader = null;
    /** This flag indicates that a compile is needed **/
    private boolean             m_bNeedsCompile = false;
    /** Convenience reference for notifying of compile **/
    private JPanel_ShaderProperties m_owningPanel = null; // TODO: FIND A BETTER WAY!
    
    /** Creates new form ShaderEffectComposerPanel */
    public ShaderEffectComposerPanel() 
    {
        initComponents();
        populateListModel();
    }
    
    /**
     * This method sets the shader program currently being composed
     * @param shader
     */
    public void setShaderProgram(GLSLShaderProgram shader)
    {
        m_shader = shader;
        if (shader != null)
            JToggle_UseDefaultInitializers.setSelected(m_shader.isUsingDefaultInitializers());
        populateListModel();
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

        JDialog_ChooseEffect = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        JList_DialogEffectList = new javax.swing.JList();
        JButton_DialogOK = new javax.swing.JButton();
        JButton_DialogCancel = new javax.swing.JButton();
        JFrame_SourceCode = new javax.swing.JFrame();
        jScrollPane3 = new javax.swing.JScrollPane();
        JTextPane_SourceCode = new javax.swing.JTextPane();
        jPanel_Effects = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        JList_EffectList = new javax.swing.JList();
        jPanel_EffectControls = new javax.swing.JPanel();
        JButton_MoveUp = new javax.swing.JButton();
        JButton_RemoveEffect = new javax.swing.JButton();
        JButton_MoveDown = new javax.swing.JButton();
        JButton_AddEffect = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel_CompileControls = new javax.swing.JPanel();
        JButton_Compile = new javax.swing.JButton();
        JButton_ViewSource = new javax.swing.JButton();
        JToggle_UseDefaultInitializers = new javax.swing.JToggleButton();

        JDialog_ChooseEffect.setTitle("Choose Effect");
        JDialog_ChooseEffect.setAlwaysOnTop(true);
        JDialog_ChooseEffect.setModal(true);
        JDialog_ChooseEffect.setName("ChooseEffectToAddDialog"); // NOI18N

        JList_DialogEffectList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(JList_DialogEffectList);

        JButton_DialogOK.setText("OK");
        JButton_DialogOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogOKClicked(evt);
            }
        });

        JButton_DialogCancel.setText("CANCEL");
        JButton_DialogCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelDialogAction(evt);
            }
        });

        org.jdesktop.layout.GroupLayout JDialog_ChooseEffectLayout = new org.jdesktop.layout.GroupLayout(JDialog_ChooseEffect.getContentPane());
        JDialog_ChooseEffect.getContentPane().setLayout(JDialog_ChooseEffectLayout);
        JDialog_ChooseEffectLayout.setHorizontalGroup(
            JDialog_ChooseEffectLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(JDialog_ChooseEffectLayout.createSequentialGroup()
                .addContainerGap()
                .add(JDialog_ChooseEffectLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .add(JDialog_ChooseEffectLayout.createSequentialGroup()
                        .add(JButton_DialogOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 109, Short.MAX_VALUE)
                        .add(JButton_DialogCancel)))
                .addContainerGap())
        );
        JDialog_ChooseEffectLayout.setVerticalGroup(
            JDialog_ChooseEffectLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(JDialog_ChooseEffectLayout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(JDialog_ChooseEffectLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(JButton_DialogOK)
                    .add(JButton_DialogCancel))
                .addContainerGap(35, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(JTextPane_SourceCode);

        org.jdesktop.layout.GroupLayout JFrame_SourceCodeLayout = new org.jdesktop.layout.GroupLayout(JFrame_SourceCode.getContentPane());
        JFrame_SourceCode.getContentPane().setLayout(JFrame_SourceCodeLayout);
        JFrame_SourceCodeLayout.setHorizontalGroup(
            JFrame_SourceCodeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
        );
        JFrame_SourceCodeLayout.setVerticalGroup(
            JFrame_SourceCodeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
        );

        setPreferredSize(new java.awt.Dimension(380, 150));
        setLayout(new java.awt.GridBagLayout());

        jPanel_Effects.setLayout(new java.awt.GridBagLayout());

        JList_EffectList.setModel(new DefaultListModel());
        JList_EffectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(JList_EffectList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_Effects.add(jScrollPane1, gridBagConstraints);

        jPanel_EffectControls.setLayout(new java.awt.GridBagLayout());

        JButton_MoveUp.setText("MoveUp");
        JButton_MoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemUp(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_EffectControls.add(JButton_MoveUp, gridBagConstraints);

        JButton_RemoveEffect.setForeground(new java.awt.Color(128, 0, 0));
        JButton_RemoveEffect.setText("Remove");
        JButton_RemoveEffect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedEffect(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_EffectControls.add(JButton_RemoveEffect, gridBagConstraints);

        JButton_MoveDown.setText("MoveDown");
        JButton_MoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveSelectedItemDown(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_EffectControls.add(JButton_MoveDown, gridBagConstraints);

        JButton_AddEffect.setForeground(new java.awt.Color(0, 128, 0));
        JButton_AddEffect.setText("Add");
        JButton_AddEffect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEffectToList(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_EffectControls.add(JButton_AddEffect, gridBagConstraints);

        jPanel_Effects.add(jPanel_EffectControls, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel_Effects, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        jPanel_CompileControls.setLayout(new java.awt.GridBagLayout());

        JButton_Compile.setText("COMPILE");
        JButton_Compile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileInitiated(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel_CompileControls.add(JButton_Compile, gridBagConstraints);

        JButton_ViewSource.setText("ViewSource");
        JButton_ViewSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSourceCode(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel_CompileControls.add(JButton_ViewSource, gridBagConstraints);

        JToggle_UseDefaultInitializers.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        JToggle_UseDefaultInitializers.setText("Default Initializers");
        JToggle_UseDefaultInitializers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultInitializerToggled(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel_CompileControls.add(JToggle_UseDefaultInitializers, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel_CompileControls, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void moveSelectedItemUp(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveSelectedItemUp
    if (JList_EffectList.getSelectedIndex() == -1)
    {
        Toolkit.getDefaultToolkit().beep();
    }
    else
    {
        int firstIndex = JList_EffectList.getSelectedIndex();
        int secondIndex = firstIndex - 1;
        if (secondIndex < 0) // At the top, do nothing
            Toolkit.getDefaultToolkit().beep();
        else
        {
            swap(firstIndex, secondIndex, (DefaultListModel)JList_EffectList.getModel());
            JList_EffectList.setSelectedIndex(secondIndex);
            setNeedsCompile(true);
        }
    }
}//GEN-LAST:event_moveSelectedItemUp

private void moveSelectedItemDown(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveSelectedItemDown
    if (JList_EffectList.getSelectedIndex() == -1)
    {
        Toolkit.getDefaultToolkit().beep();
    }
    else
    {
        int firstIndex = JList_EffectList.getSelectedIndex();
        int secondIndex = firstIndex + 1;
        if (secondIndex >= JList_EffectList.getModel().getSize()) // At the top, do nothing
            Toolkit.getDefaultToolkit().beep();
        else
        {
            swap(firstIndex, secondIndex, (DefaultListModel)JList_EffectList.getModel());
            JList_EffectList.setSelectedIndex(secondIndex);
            setNeedsCompile(true);
        }
    }
}//GEN-LAST:event_moveSelectedItemDown

private void removeSelectedEffect(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedEffect
    if (JList_EffectList.getSelectedIndex() == -1)
        Toolkit.getDefaultToolkit().beep();
    else
    {
        ((DefaultListModel)JList_EffectList.getModel()).remove(JList_EffectList.getSelectedIndex());
        setNeedsCompile(true);
    }
}//GEN-LAST:event_removeSelectedEffect

private void addEffectToList(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEffectToList
    // set up the dialog
    DefaultListModel listModel = new DefaultListModel();
    // query the list of effects available
    GLSLShaderEffect[] effectArray = ShaderUtils.getDefaultEffects();
    for (int i = 0; i < effectArray.length; ++i)
        listModel.add(i, effectArray[i]);
    JList_DialogEffectList.setModel(listModel);
    JList_DialogEffectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    // show it
    JDialog_ChooseEffect.setSize(250, 300);
    JDialog_ChooseEffect.setVisible(true);
    // retrieve the results
}//GEN-LAST:event_addEffectToList

private void dialogOKClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialogOKClicked
    if (JList_DialogEffectList.getSelectedIndex() != -1)
    {
        Object[] objArray = JList_DialogEffectList.getSelectedValues();
        for (Object obj : objArray)
            ((DefaultListModel)JList_EffectList.getModel()).addElement((GLSLShaderEffect)obj);
        setNeedsCompile(true);
    }
    JDialog_ChooseEffect.setVisible(false);
}//GEN-LAST:event_dialogOKClicked

private void compileInitiated(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileInitiated
    // clear all the old effects from the shader
    m_shader.getEffects().clear();
    // add all the current effects
    for (Object obj : ((DefaultListModel)JList_EffectList.getModel()).toArray())
        m_shader.addEffect((GLSLShaderEffect)obj);
    // compile, watching for exceptions
    boolean success = false;
    try { m_shader.clearPropertyMap(); m_shader.compile(); success = true; }
    catch (GLSLCompileException ex)
    {
        success = false;
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Compilation Error!", JOptionPane.OK_OPTION);
    }
    // if successful, clear needs compile flag
    if (success)
    {
        setNeedsCompile(false);
       
        // let someone know that we are ready for application
        if (m_owningPanel != null)
            m_owningPanel.setShader(m_shader);
    }
}//GEN-LAST:event_compileInitiated

private void showSourceCode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSourceCode
    if (m_bNeedsCompile == true)
    {
        Toolkit.getDefaultToolkit().beep();
        return;
    }
    if (m_shader != null)
        JTextPane_SourceCode.setText(m_shader.getVertexProgramSource() + System.getProperty("line.separator") + m_shader.getFragmentProgramSource());
    JFrame_SourceCode.setSize(350,350);
    JFrame_SourceCode.setVisible(true);
}//GEN-LAST:event_showSourceCode

private void defaultInitializerToggled(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultInitializerToggled
    if (m_shader != null)
        m_shader.setUseDefaultInitializers(JToggle_UseDefaultInitializers.isSelected());
}//GEN-LAST:event_defaultInitializerToggled

private void cancelDialogAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelDialogAction
    JDialog_ChooseEffect.setVisible(false);
}//GEN-LAST:event_cancelDialogAction

public void setNeedsCompile(boolean bNeeded)
{
    m_bNeedsCompile = bNeeded;
    if (m_bNeedsCompile)
    {
        JButton_Compile.setText("*COMPILE*");
        JButton_Compile.setForeground(Color.RED);
    }
    else
    {
        JButton_Compile.setText("COMPILE");
        JButton_Compile.setForeground(Color.BLACK);
    }
}

public GLSLShaderProgram getShader()
{
    return m_shader;
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton JButton_AddEffect;
    private javax.swing.JButton JButton_Compile;
    private javax.swing.JButton JButton_DialogCancel;
    private javax.swing.JButton JButton_DialogOK;
    private javax.swing.JButton JButton_MoveDown;
    private javax.swing.JButton JButton_MoveUp;
    private javax.swing.JButton JButton_RemoveEffect;
    private javax.swing.JButton JButton_ViewSource;
    private javax.swing.JDialog JDialog_ChooseEffect;
    private javax.swing.JFrame JFrame_SourceCode;
    private javax.swing.JList JList_DialogEffectList;
    private javax.swing.JList JList_EffectList;
    private javax.swing.JTextPane JTextPane_SourceCode;
    private javax.swing.JToggleButton JToggle_UseDefaultInitializers;
    private javax.swing.JPanel jPanel_CompileControls;
    private javax.swing.JPanel jPanel_EffectControls;
    private javax.swing.JPanel jPanel_Effects;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

    //Swap two elements in the list.
    private void swap(int a, int b, DefaultListModel listModel) 
    {
        Object aObject = listModel.getElementAt(a);
        Object bObject = listModel.getElementAt(b);
        listModel.set(a, bObject);
        listModel.set(b, aObject);
    }

    /**
     * This method should be called to repopulate the list of effects with 
     * those contained by the current shader.
     */
    public void populateListModel()
    {
        // out with the old
        ((DefaultListModel)JList_EffectList.getModel()).removeAllElements();
        if (m_shader == null)
            return;
        for (GLSLShaderEffect effect : m_shader.getEffects())
            ((DefaultListModel)JList_EffectList.getModel()).addElement(effect);
    }
    
    /**
     * Set the owning panel of this widget
     * @param panel The shader properties panel containing this.
     */
    public void setOwningPanel(JPanel_ShaderProperties panel)
    {
        m_owningPanel = panel;
    }
}
