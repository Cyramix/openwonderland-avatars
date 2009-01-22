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

/*
 * JFrame_InstrumentationGUI.java
 *
 * Created on Jan 22, 2009, 1:35:46 PM
 */

package imi.gui;

import com.jme.math.Vector3f;
import imi.character.CharacterAttributes;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.utils.instruments.Instrumentation;
import imi.utils.instruments.Instrumentation.InstrumentedSubsystem;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Paul Viet Ngueyn Truong (ptruong)
 */
public class JFrame_InstrumentationGUI extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    private WorldManager                    m_worldManager      = null;
    private Instrumentation                 m_instrumentation   = null;
    private ArrayList<CharacterAttributes>  m_attributes        = null;
    private int                             m_numMaleAttribs    = 10;
    private int                             m_numFeamleAttribs  = 10;

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor only initializes the default GUI components (visuals).
     * Required manual setting of worldmanager, instrumentation, and method calls
     * to initAttributesBox(), setLists().  Best not to call this method of
     * construction.
     */
    public JFrame_InstrumentationGUI() {
        initComponents();
    }

    /**
     * Overloaded constructor initializes and sets up the GUI components.  If
     * more random attributes are required, call method setNumAttributes()
     * @param wm
     */
    public JFrame_InstrumentationGUI(WorldManager wm) {
        m_worldManager = wm;
        m_instrumentation = (Instrumentation) wm.getUserData(Instrumentation.class);
        initComponents();
        initAttributesBox();
        setLists();
    }

    /**
     * Adds an instanced avatar at the translation vector set by the user.
     */
    public void addInstancedAvatar() {
        float x = Float.valueOf(jFormattedTextField_X1.getValue().toString());
        float y = Float.valueOf(jFormattedTextField_Y1.getValue().toString());
        float z = Float.valueOf(jFormattedTextField_Z1.getValue().toString());

        Vector3f translation = new Vector3f( x, y, z );

        m_instrumentation.addInstancedAvatar(translation);
    }

    /**
     * Adds an non-instanced avatar at the translation vector set by the user.
     * if selection of attribute is set, Avatar will load with that attribute.
     * @param attributes
     */
    public void addNonInstancedAvatar(boolean attributes) {

        if (attributes) {
            float x = Float.valueOf(jFormattedTextField_X2.getValue().toString());
            float y = Float.valueOf(jFormattedTextField_Y2.getValue().toString());
            float z = Float.valueOf(jFormattedTextField_Z2.getValue().toString());
            Vector3f translation = new Vector3f( x, y, z );

            CharacterAttributes attribs = (CharacterAttributes)jComboBox_Attributes.getSelectedItem();
            m_instrumentation.addNonInstancedAvatar(attribs, translation);

        } else {
            float x = Float.valueOf(jFormattedTextField_X3.getValue().toString());
            float y = Float.valueOf(jFormattedTextField_Y3.getValue().toString());
            float z = Float.valueOf(jFormattedTextField_Z3.getValue().toString());
            Vector3f translation = new Vector3f( x, y, z );
            
            m_instrumentation.addNonInstancedAvatar(translation);
        }
    }

    /**
     * Enables either the selected instrumentation or all the instrumentation.
     * @param enableall
     */
    public void enableInstrumentation(boolean enableall) {
        DefaultListModel Emodel = (DefaultListModel) jList_Enabled.getModel();
        DefaultListModel Dmodel = (DefaultListModel) jList_Disabled.getModel();

        if (enableall) {

            m_instrumentation.enableAllSubsystems();
            while (Dmodel.getSize() > 0) {
                Instrumentation.InstrumentedSubsystem instrumentation = (InstrumentedSubsystem) Dmodel.getElementAt(0);
                Dmodel.removeElement(instrumentation);
                Emodel.addElement(instrumentation);
            }

        } else {

            Instrumentation.InstrumentedSubsystem instrumentation = (InstrumentedSubsystem) jList_Disabled.getSelectedValue();
            if (instrumentation == null)
                return;

            m_instrumentation.enableSubsystem(instrumentation);
            Dmodel.removeElement(instrumentation);
            Emodel.addElement(instrumentation);

        }
    }

    /**
     * Disables either the selected instrumentation or all the instrumentation
     * @param disableall
     */
    public void disableInstrumentaiton(boolean disableall) {
        DefaultListModel Emodel = (DefaultListModel) jList_Enabled.getModel();
        DefaultListModel Dmodel = (DefaultListModel) jList_Disabled.getModel();

        if (disableall) {

            m_instrumentation.disableAllSubsystems();
            while (Emodel.getSize() > 0) {
                Instrumentation.InstrumentedSubsystem instrumentation = (InstrumentedSubsystem) Emodel.getElementAt(0);
                Emodel.removeElement(instrumentation);
                Dmodel.addElement(instrumentation);
            }

        } else {

            Instrumentation.InstrumentedSubsystem instrumentation = (InstrumentedSubsystem) jList_Enabled.getSelectedValue();
            if (instrumentation == null)
                return;
            
            m_instrumentation.disableSubsytem(instrumentation);
            Emodel.removeElement(instrumentation);
            Dmodel.addElement(instrumentation);

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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel_MainPanel = new javax.swing.JPanel();
        jPanel_AddInstancedAvatar = new javax.swing.JPanel();
        jFormattedTextField_X1 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Y1 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Z1 = new javax.swing.JFormattedTextField();
        jButton_InstancedAvatar = new javax.swing.JButton();
        jPanel_AddNoninstancedAvatar1 = new javax.swing.JPanel();
        jFormattedTextField_X2 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Y2 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Z2 = new javax.swing.JFormattedTextField();
        jButton_NoninstancedAvatar1 = new javax.swing.JButton();
        jPanel_AddNoninstacedAvatar2 = new javax.swing.JPanel();
        jFormattedTextField_X3 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Y3 = new javax.swing.JFormattedTextField();
        jFormattedTextField_Z3 = new javax.swing.JFormattedTextField();
        jComboBox_Attributes = new javax.swing.JComboBox();
        jButton_NoninstancedAvatar2 = new javax.swing.JButton();
        jPanel_EnableDisable = new javax.swing.JPanel();
        jScrollPane_Enabled = new javax.swing.JScrollPane();
        jList_Enabled = new javax.swing.JList();
        jPanel_EnableDisableBttns = new javax.swing.JPanel();
        jButton_Enable = new javax.swing.JButton();
        jButton_Disable = new javax.swing.JButton();
        jButton_EnableAll = new javax.swing.JButton();
        jButton_DisableAll = new javax.swing.JButton();
        jScrollPane_Disabled = new javax.swing.JScrollPane();
        jList_Disabled = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Instrumentation Panel"); // NOI18N
        setResizable(false);

        jPanel_MainPanel.setLayout(new java.awt.GridBagLayout());

        jPanel_AddInstancedAvatar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AddInstancedAvatar.setOpaque(false);
        jPanel_AddInstancedAvatar.setLayout(new java.awt.GridBagLayout());

        jFormattedTextField_X1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_X1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_X1.setText("0.0");
        jFormattedTextField_X1.setValue(0.0);
        jFormattedTextField_X1.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddInstancedAvatar.add(jFormattedTextField_X1, gridBagConstraints);

        jFormattedTextField_Y1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Y1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Y1.setText("0.0");
        jFormattedTextField_Y1.setValue(0.0);
        jFormattedTextField_Y1.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddInstancedAvatar.add(jFormattedTextField_Y1, gridBagConstraints);

        jFormattedTextField_Z1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Z1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Z1.setText("0.0");
        jFormattedTextField_Z1.setValue(0.0);
        jFormattedTextField_Z1.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddInstancedAvatar.add(jFormattedTextField_Z1, gridBagConstraints);

        jButton_InstancedAvatar.setText("+ Instanced Avatar");
        jButton_InstancedAvatar.setPreferredSize(new java.awt.Dimension(200, 28));
        jButton_InstancedAvatar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInstancedAvatar();
            }
        });
        jPanel_AddInstancedAvatar.add(jButton_InstancedAvatar, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_MainPanel.add(jPanel_AddInstancedAvatar, gridBagConstraints);

        jPanel_AddNoninstancedAvatar1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AddNoninstancedAvatar1.setLayout(new java.awt.GridBagLayout());

        jFormattedTextField_X2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_X2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_X2.setText("0.0");
        jFormattedTextField_X2.setValue(0.0);
        jFormattedTextField_X2.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstancedAvatar1.add(jFormattedTextField_X2, gridBagConstraints);

        jFormattedTextField_Y2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Y2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Y2.setText("0.0");
        jFormattedTextField_Y2.setValue(0.0);
        jFormattedTextField_Y2.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstancedAvatar1.add(jFormattedTextField_Y2, gridBagConstraints);

        jFormattedTextField_Z2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Z2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Z2.setText("0.0");
        jFormattedTextField_Z2.setValue(0.0);
        jFormattedTextField_Z2.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstancedAvatar1.add(jFormattedTextField_Z2, gridBagConstraints);

        jButton_NoninstancedAvatar1.setText("+ Non-instanced Avatar");
        jButton_NoninstancedAvatar1.setPreferredSize(new java.awt.Dimension(200, 28));
        jButton_NoninstancedAvatar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNonInstancedAvatar(false);
            }
        });
        jPanel_AddNoninstancedAvatar1.add(jButton_NoninstancedAvatar1, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_MainPanel.add(jPanel_AddNoninstancedAvatar1, gridBagConstraints);

        jPanel_AddNoninstacedAvatar2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_AddNoninstacedAvatar2.setLayout(new java.awt.GridBagLayout());

        jFormattedTextField_X3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_X3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_X3.setText("0.0");
        jFormattedTextField_X3.setValue(0.0);
        jFormattedTextField_X3.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstacedAvatar2.add(jFormattedTextField_X3, gridBagConstraints);

        jFormattedTextField_Y3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Y3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Y3.setText("0.0");
        jFormattedTextField_Y3.setValue(0.0);
        jFormattedTextField_Y3.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstacedAvatar2.add(jFormattedTextField_Y3, gridBagConstraints);

        jFormattedTextField_Z3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#,##0.0######"))));
        jFormattedTextField_Z3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextField_Z3.setText("0.0");
        jFormattedTextField_Z3.setValue(0.0);
        jFormattedTextField_Z3.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel_AddNoninstacedAvatar2.add(jFormattedTextField_Z3, gridBagConstraints);

        jComboBox_Attributes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel_AddNoninstacedAvatar2.add(jComboBox_Attributes, gridBagConstraints);

        jButton_NoninstancedAvatar2.setText("+ Non-instanced Avatar");
        jButton_NoninstancedAvatar2.setPreferredSize(new java.awt.Dimension(200, 28));
        jButton_NoninstancedAvatar2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNonInstancedAvatar(true);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel_AddNoninstacedAvatar2.add(jButton_NoninstancedAvatar2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_MainPanel.add(jPanel_AddNoninstacedAvatar2, gridBagConstraints);

        jPanel_EnableDisable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel_EnableDisable.setLayout(new java.awt.GridBagLayout());

        jScrollPane_Enabled.setPreferredSize(new java.awt.Dimension(150, 150));

        jList_Enabled.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jList_Enabled.setForeground(new java.awt.Color(0, 255, 0));
        jList_Enabled.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Enabled.setViewportView(jList_Enabled);

        jPanel_EnableDisable.add(jScrollPane_Enabled, new java.awt.GridBagConstraints());

        jPanel_EnableDisableBttns.setLayout(new java.awt.GridBagLayout());

        jButton_Enable.setText("<< Enable");
        jButton_Enable.setPreferredSize(new java.awt.Dimension(140, 28));
        jButton_Enable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableInstrumentation(false);
            }
        });
        jPanel_EnableDisableBttns.add(jButton_Enable, new java.awt.GridBagConstraints());

        jButton_Disable.setText("Disable >>");
        jButton_Disable.setPreferredSize(new java.awt.Dimension(140, 28));
        jButton_Disable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableInstrumentaiton(false);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_EnableDisableBttns.add(jButton_Disable, gridBagConstraints);

        jButton_EnableAll.setText("<< Enable All");
        jButton_EnableAll.setPreferredSize(new java.awt.Dimension(140, 28));
        jButton_EnableAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableInstrumentation(true);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel_EnableDisableBttns.add(jButton_EnableAll, gridBagConstraints);

        jButton_DisableAll.setText("Disable All >>");
        jButton_DisableAll.setPreferredSize(new java.awt.Dimension(140, 28));
        jButton_DisableAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableInstrumentaiton(true);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel_EnableDisableBttns.add(jButton_DisableAll, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        jPanel_EnableDisable.add(jPanel_EnableDisableBttns, gridBagConstraints);

        jScrollPane_Disabled.setPreferredSize(new java.awt.Dimension(150, 150));

        jList_Disabled.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        jList_Disabled.setForeground(new java.awt.Color(255, 0, 0));
        jList_Disabled.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane_Disabled.setViewportView(jList_Disabled);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel_EnableDisable.add(jScrollPane_Disabled, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_MainPanel.add(jPanel_EnableDisable, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_MainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 448, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_MainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_InstrumentationGUI().setVisible(true);
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setWorldManager(WorldManager wm) {
        m_worldManager = wm;
    }
    
    public void setInstrumentation(Instrumentation inst) {
        m_instrumentation = inst;
    }

    public void setNumberOfAttributes(int numMaleAttrib, int numFemaleAttrib) {
        m_numMaleAttribs    = numMaleAttrib;
        m_numFeamleAttribs  = numFemaleAttrib;
        initAttributesBox();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Disable;
    private javax.swing.JButton jButton_DisableAll;
    private javax.swing.JButton jButton_Enable;
    private javax.swing.JButton jButton_EnableAll;
    private javax.swing.JButton jButton_InstancedAvatar;
    private javax.swing.JButton jButton_NoninstancedAvatar1;
    private javax.swing.JButton jButton_NoninstancedAvatar2;
    private javax.swing.JComboBox jComboBox_Attributes;
    private javax.swing.JFormattedTextField jFormattedTextField_X1;
    private javax.swing.JFormattedTextField jFormattedTextField_X2;
    private javax.swing.JFormattedTextField jFormattedTextField_X3;
    private javax.swing.JFormattedTextField jFormattedTextField_Y1;
    private javax.swing.JFormattedTextField jFormattedTextField_Y2;
    private javax.swing.JFormattedTextField jFormattedTextField_Y3;
    private javax.swing.JFormattedTextField jFormattedTextField_Z1;
    private javax.swing.JFormattedTextField jFormattedTextField_Z2;
    private javax.swing.JFormattedTextField jFormattedTextField_Z3;
    private javax.swing.JList jList_Disabled;
    private javax.swing.JList jList_Enabled;
    private javax.swing.JPanel jPanel_AddInstancedAvatar;
    private javax.swing.JPanel jPanel_AddNoninstacedAvatar2;
    private javax.swing.JPanel jPanel_AddNoninstancedAvatar1;
    private javax.swing.JPanel jPanel_EnableDisable;
    private javax.swing.JPanel jPanel_EnableDisableBttns;
    private javax.swing.JPanel jPanel_MainPanel;
    private javax.swing.JScrollPane jScrollPane_Disabled;
    private javax.swing.JScrollPane jScrollPane_Enabled;
    // End of variables declaration//GEN-END:variables
////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

    /**
     * Randomly generates an avatar attribute based on the gender specified.
     * Called internally by class when the attribute combobox is initiated
     * @param iGender
     * @param appendNumber
     * @return
     */
    private CharacterAttributes createAttributes(int iGender, int appendNumber) {
        CharacterAttributes attribs = null;
        switch(iGender)
        {
            case 1:
            {
                attribs = new MaleAvatarAttributes("MaleAvatar" + appendNumber, true);
                break;
            }
            case 2:
            {
                attribs = new FemaleAvatarAttributes("FemaleAvatar" + appendNumber, true);
                break;
            }
        }

        return attribs;
    }

    /**
     * Initializes the combobox full of randomly generated attributes.  The number
     * of attributes is default set to 10 male attributes and 10 female attributes.
     * Calling the setNumAttributes method will all the setting of the number of male
     * and the number of female avatars
     */
    private void initAttributesBox() {
        m_attributes = new ArrayList<CharacterAttributes>();

        for (int i = 0; i < m_numMaleAttribs; i++) {
            m_attributes.add(createAttributes(1, i));
        }
        
        for (int i = 0; i < m_numFeamleAttribs; i++) {
            m_attributes.add(createAttributes(2, i));
        }
        
        jComboBox_Attributes.setModel(new javax.swing.DefaultComboBoxModel(m_attributes.toArray()));
    }

    /**
     * When the window is created, calling this method will set the jlist containing
     * which instrumentations are enabled or disabled.
     */
    private void setLists() {
        Instrumentation.InstrumentedSubsystem[]          iSubSystem = Instrumentation.InstrumentedSubsystem.values();
        ArrayList<Instrumentation.InstrumentedSubsystem> enabled    = new ArrayList<Instrumentation.InstrumentedSubsystem>();
        ArrayList<Instrumentation.InstrumentedSubsystem> disabled   = new ArrayList<Instrumentation.InstrumentedSubsystem>();
        DefaultListModel enabledListMod     = new DefaultListModel();
        DefaultListModel disabledListMod    = new DefaultListModel();

        for (int i = 0; i < iSubSystem.length; i++) {
            if (m_instrumentation.isSubsystemEnabled(iSubSystem[i])) {
                enabledListMod.addElement(iSubSystem[i]);
            } else {
                disabledListMod.addElement(iSubSystem[i]);
            }
        }
        
        jList_Enabled.setModel(enabledListMod);
        jList_Disabled.setModel(disabledListMod);
    }
}
