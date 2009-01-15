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
 * JPanel_ArmsHandsOption.java
 *
 * Created on Dec 17, 2008, 2:26:27 PM
 */

package imi.gui;

import javax.swing.JFrame;

/**
 *
 * @author Paul Viet Ngueyn Truong (ptruong)
 */
public class JPanel_ArmsHandsOption extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    private JFrame  m_baseFrame     =   null;

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes the GUI components.  Before frame is usable
     * the parent frame needs to be set and the sliders need to be associated with
     * the joint/grouping it controls.
     */
    public JPanel_ArmsHandsOption() {
        initComponents();
    }

    /**
     * Overloaded constructor initializes the GUI components and sets the parent
     * frame and the slider associations
     * @param baseFrame
     */
    public JPanel_ArmsHandsOption(JFrame baseFrame) {
        m_baseFrame = baseFrame;
        initComponents();
        setSliderControls();
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

        jTabbedPane_ArmsHands = new javax.swing.JTabbedPane();
        jPanel_UpperArms = new javax.swing.JPanel();
        jPanel_UALength = new javax.swing.JPanel();
        jCheckBox_SyncUALength = new javax.swing.JCheckBox();
        LeftUpperarm_Length = new imi.gui.JPanel_VerticalSliderT();
        RightUpperarm_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_UAThickness = new javax.swing.JPanel();
        jCheckBox_SyncUAThickness = new javax.swing.JCheckBox();
        LeftUpperarm_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightUpperarm_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        jPanel_LowerArms = new javax.swing.JPanel();
        jPanel_LALength = new javax.swing.JPanel();
        jCheckBox_SyncLALength = new javax.swing.JCheckBox();
        LeftLowerarm_Length = new imi.gui.JPanel_VerticalSliderT();
        RightLowerarm_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_LAThickness = new javax.swing.JPanel();
        jCheckBox_SyncLAThickness = new javax.swing.JCheckBox();
        LeftLowerarm_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightLowerarm_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        jPanel_Hands = new javax.swing.JPanel();
        jPanel_FingerLength = new javax.swing.JPanel();
        jCheckBox_SyncHandLength = new javax.swing.JCheckBox();
        LeftHand_Length = new imi.gui.JPanel_VerticalSliderT();
        RightHand_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_HandThickness = new javax.swing.JPanel();
        jCheckBox_SyncHandThickness = new javax.swing.JCheckBox();
        LeftHand_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightHand_Thickness = new imi.gui.JPanel_HorizontalSliderS();

        setMinimumSize(new java.awt.Dimension(270, 600));
        setPreferredSize(new java.awt.Dimension(270, 600));

        jTabbedPane_ArmsHands.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel_UpperArms.setLayout(new java.awt.GridBagLayout());

        jPanel_UALength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Upperarm Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_UALength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_UALength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_UALength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncUALength.setText("Lock Sliders");
        jCheckBox_SyncUALength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncUALength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_UALength.add(jCheckBox_SyncUALength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_UALength.add(LeftUpperarm_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_UALength.add(RightUpperarm_Length, gridBagConstraints);

        jPanel_UpperArms.add(jPanel_UALength, new java.awt.GridBagConstraints());

        jPanel_UAThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Upperarm Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_UAThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_UAThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_UAThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncUAThickness.setText("Lock Sliders");
        jCheckBox_SyncUAThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncUAThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_UAThickness.add(jCheckBox_SyncUAThickness, gridBagConstraints);

        LeftUpperarm_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftUpperarm_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_UAThickness.add(LeftUpperarm_Thickness, gridBagConstraints);

        RightUpperarm_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightUpperarm_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_UAThickness.add(RightUpperarm_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_UpperArms.add(jPanel_UAThickness, gridBagConstraints);

        jTabbedPane_ArmsHands.addTab("Upperarms", jPanel_UpperArms);

        jPanel_LowerArms.setLayout(new java.awt.GridBagLayout());

        jPanel_LALength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Forearm Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_LALength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_LALength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_LALength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncLALength.setText("Lock Sliders");
        jCheckBox_SyncLALength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncLALength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_LALength.add(jCheckBox_SyncLALength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_LALength.add(LeftLowerarm_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_LALength.add(RightLowerarm_Length, gridBagConstraints);

        jPanel_LowerArms.add(jPanel_LALength, new java.awt.GridBagConstraints());

        jPanel_LAThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Forearm Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_LAThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_LAThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_LAThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncLAThickness.setText("Lock Sliders");
        jCheckBox_SyncLAThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncLAThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_LAThickness.add(jCheckBox_SyncLAThickness, gridBagConstraints);

        LeftLowerarm_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftLowerarm_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_LAThickness.add(LeftLowerarm_Thickness, gridBagConstraints);

        RightLowerarm_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightLowerarm_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_LAThickness.add(RightLowerarm_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_LowerArms.add(jPanel_LAThickness, gridBagConstraints);

        jTabbedPane_ArmsHands.addTab("Forearms", jPanel_LowerArms);

        jPanel_Hands.setLayout(new java.awt.GridBagLayout());

        jPanel_FingerLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Hand Finger Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_FingerLength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_FingerLength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_FingerLength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncHandLength.setText("Lock Sliders");
        jCheckBox_SyncHandLength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncHandLength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_FingerLength.add(jCheckBox_SyncHandLength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_FingerLength.add(LeftHand_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_FingerLength.add(RightHand_Length, gridBagConstraints);

        jPanel_Hands.add(jPanel_FingerLength, new java.awt.GridBagConstraints());

        jPanel_HandThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Hand Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_HandThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_HandThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_HandThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncHandThickness.setText("Lock Sliders");
        jCheckBox_SyncHandThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncHandThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_HandThickness.add(jCheckBox_SyncHandThickness, gridBagConstraints);

        LeftHand_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftHand_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_HandThickness.add(LeftHand_Thickness, gridBagConstraints);

        RightHand_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightHand_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_HandThickness.add(RightHand_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Hands.add(jPanel_HandThickness, gridBagConstraints);

        jTabbedPane_ArmsHands.addTab("Hands", jPanel_Hands);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jTabbedPane_ArmsHands, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jTabbedPane_ArmsHands, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 600, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

////////////////////////////////////////////////////////////////////////////////
// MUTATORS
////////////////////////////////////////////////////////////////////////////////

    public void setParentFrame(JFrame frame) {
        m_baseFrame = frame;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.JPanel_VerticalSliderT LeftHand_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftHand_Thickness;
    private imi.gui.JPanel_VerticalSliderT LeftLowerarm_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftLowerarm_Thickness;
    private imi.gui.JPanel_VerticalSliderT LeftUpperarm_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftUpperarm_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightHand_Length;
    private imi.gui.JPanel_HorizontalSliderS RightHand_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightLowerarm_Length;
    private imi.gui.JPanel_HorizontalSliderS RightLowerarm_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightUpperarm_Length;
    private imi.gui.JPanel_HorizontalSliderS RightUpperarm_Thickness;
    private javax.swing.JCheckBox jCheckBox_SyncHandLength;
    private javax.swing.JCheckBox jCheckBox_SyncHandThickness;
    private javax.swing.JCheckBox jCheckBox_SyncLALength;
    private javax.swing.JCheckBox jCheckBox_SyncLAThickness;
    private javax.swing.JCheckBox jCheckBox_SyncUALength;
    private javax.swing.JCheckBox jCheckBox_SyncUAThickness;
    private javax.swing.JPanel jPanel_FingerLength;
    private javax.swing.JPanel jPanel_HandThickness;
    private javax.swing.JPanel jPanel_Hands;
    private javax.swing.JPanel jPanel_LALength;
    private javax.swing.JPanel jPanel_LAThickness;
    private javax.swing.JPanel jPanel_LowerArms;
    private javax.swing.JPanel jPanel_UALength;
    private javax.swing.JPanel jPanel_UAThickness;
    private javax.swing.JPanel jPanel_UpperArms;
    private javax.swing.JTabbedPane jTabbedPane_ArmsHands;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the slider's joint associations and parent windows.  The associations
     * specify which joints the slider controls.  The parent window allows the slider
     * data to be sent to the switchboard found in the base window.
     */
    public void setSliderControls() {
        LeftHand_Length.setObjectRef(GUI_Enums.m_sliderControl.lefthandLength);
        LeftHand_Thickness.setObjectRef(GUI_Enums.m_sliderControl.lefthandThickness);
        LeftLowerarm_Length.setObjectRef(GUI_Enums.m_sliderControl.leftlowerarmLength);
        LeftLowerarm_Thickness.setObjectRef(GUI_Enums.m_sliderControl.leftlowerarmThickness);
        LeftUpperarm_Length.setObjectRef(GUI_Enums.m_sliderControl.leftupperarmLength);
        LeftUpperarm_Thickness.setObjectRef(GUI_Enums.m_sliderControl.leftupperarmThickness);
        RightHand_Length.setObjectRef(GUI_Enums.m_sliderControl.righthandLength);
        RightHand_Thickness.setObjectRef(GUI_Enums.m_sliderControl.righthandThickness);
        RightLowerarm_Length.setObjectRef(GUI_Enums.m_sliderControl.rightlowerarmLength);
        RightLowerarm_Thickness.setObjectRef(GUI_Enums.m_sliderControl.rightlowerarmThickness);
        RightUpperarm_Length.setObjectRef(GUI_Enums.m_sliderControl.rightupperarmLength);
        RightUpperarm_Thickness.setObjectRef(GUI_Enums.m_sliderControl.rightupperarmThickness);

        LeftHand_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftHand_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftLowerarm_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftLowerarm_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftUpperarm_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftUpperarm_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightHand_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightHand_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightLowerarm_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightLowerarm_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightUpperarm_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightUpperarm_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
    }
}
