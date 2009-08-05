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
 * JPanel_LegsFeetOption.java
 *
 * Created on Dec 17, 2008, 3:08:34 PM
 */

package imi.gui;

import javax.swing.JFrame;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_LegsFeetOption extends javax.swing.JPanel {
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
    public JPanel_LegsFeetOption() {
        initComponents();
    }

    /**
     * Overloaded constructor initializes the GUI components and sets the parent
     * frame and the slider associations
     * @param baseFrame
     */
    public JPanel_LegsFeetOption(JFrame baseFrame) {
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

        jTabbedPane_LegsFeet = new javax.swing.JTabbedPane();
        jPanel_Upperlegs = new javax.swing.JPanel();
        jPanel_ULLength = new javax.swing.JPanel();
        jCheckBox_SyncULLength = new javax.swing.JCheckBox();
        LeftUpperleg_Length = new imi.gui.JPanel_VerticalSliderT();
        RightUpperleg_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_ULThickness = new javax.swing.JPanel();
        jCheckBox_SyncULThickness = new javax.swing.JCheckBox();
        LeftUpperleg_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightUpperleg_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        jPanel_Lowerlegs = new javax.swing.JPanel();
        jPanel_LLLength = new javax.swing.JPanel();
        jCheckBox_SyncLLLength = new javax.swing.JCheckBox();
        LeftLowerleg_Length = new imi.gui.JPanel_VerticalSliderT();
        RightLowerleg_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_LLThickness = new javax.swing.JPanel();
        jCheckBox_SyncLLThickness = new javax.swing.JCheckBox();
        LeftLowerleg_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightLowerleg_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        jPanel_Feet = new javax.swing.JPanel();
        jPanel_FeetLength = new javax.swing.JPanel();
        jCheckBox_SyncFeetLength = new javax.swing.JCheckBox();
        LeftFoot_Length = new imi.gui.JPanel_VerticalSliderT();
        RightFoot_Length = new imi.gui.JPanel_VerticalSliderT();
        jPanel_FeetThickness = new javax.swing.JPanel();
        jCheckBox_SyncFeetThickness = new javax.swing.JCheckBox();
        LeftFoot_Thickness = new imi.gui.JPanel_HorizontalSliderS();
        RightFoot_Thickness = new imi.gui.JPanel_HorizontalSliderS();

        setMinimumSize(new java.awt.Dimension(270, 600));
        setPreferredSize(new java.awt.Dimension(270, 600));

        jTabbedPane_LegsFeet.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel_Upperlegs.setLayout(new java.awt.GridBagLayout());

        jPanel_ULLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Thigh Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_ULLength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_ULLength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_ULLength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncULLength.setText("Lock Sliders");
        jCheckBox_SyncULLength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncULLength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_ULLength.add(jCheckBox_SyncULLength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_ULLength.add(LeftUpperleg_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_ULLength.add(RightUpperleg_Length, gridBagConstraints);

        jPanel_Upperlegs.add(jPanel_ULLength, new java.awt.GridBagConstraints());

        jPanel_ULThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Thigh Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_ULThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_ULThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_ULThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncULThickness.setText("Lock Sliders");
        jCheckBox_SyncULThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncULThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_ULThickness.add(jCheckBox_SyncULThickness, gridBagConstraints);

        LeftUpperleg_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftUpperleg_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_ULThickness.add(LeftUpperleg_Thickness, gridBagConstraints);

        RightUpperleg_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightUpperleg_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_ULThickness.add(RightUpperleg_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Upperlegs.add(jPanel_ULThickness, gridBagConstraints);

        jTabbedPane_LegsFeet.addTab("Thighs", jPanel_Upperlegs);

        jPanel_Lowerlegs.setLayout(new java.awt.GridBagLayout());

        jPanel_LLLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Calf Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_LLLength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_LLLength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_LLLength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncLLLength.setText("Lock Sliders");
        jCheckBox_SyncLLLength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncLLLength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_LLLength.add(jCheckBox_SyncLLLength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_LLLength.add(LeftLowerleg_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_LLLength.add(RightLowerleg_Length, gridBagConstraints);

        jPanel_Lowerlegs.add(jPanel_LLLength, new java.awt.GridBagConstraints());

        jPanel_LLThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Clalf Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_LLThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_LLThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_LLThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncLLThickness.setText("Lock Sliders");
        jCheckBox_SyncLLThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncLLThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_LLThickness.add(jCheckBox_SyncLLThickness, gridBagConstraints);

        LeftLowerleg_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftLowerleg_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_LLThickness.add(LeftLowerleg_Thickness, gridBagConstraints);

        RightLowerleg_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightLowerleg_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_LLThickness.add(RightLowerleg_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Lowerlegs.add(jPanel_LLThickness, gridBagConstraints);

        jTabbedPane_LegsFeet.addTab("Calves", jPanel_Lowerlegs);

        jPanel_Feet.setLayout(new java.awt.GridBagLayout());

        jPanel_FeetLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Feet Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_FeetLength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_FeetLength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_FeetLength.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncFeetLength.setText("Lock Sliders");
        jCheckBox_SyncFeetLength.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncFeetLength.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_FeetLength.add(jCheckBox_SyncFeetLength, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_FeetLength.add(LeftFoot_Length, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_FeetLength.add(RightFoot_Length, gridBagConstraints);

        jPanel_Feet.add(jPanel_FeetLength, new java.awt.GridBagConstraints());

        jPanel_FeetThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Feet Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_FeetThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_FeetThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_FeetThickness.setLayout(new java.awt.GridBagLayout());

        jCheckBox_SyncFeetThickness.setText("Lock Sliders");
        jCheckBox_SyncFeetThickness.setMinimumSize(new java.awt.Dimension(120, 23));
        jCheckBox_SyncFeetThickness.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        jPanel_FeetThickness.add(jCheckBox_SyncFeetThickness, gridBagConstraints);

        LeftFoot_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        LeftFoot_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel_FeetThickness.add(LeftFoot_Thickness, gridBagConstraints);

        RightFoot_Thickness.setMinimumSize(new java.awt.Dimension(108, 57));
        RightFoot_Thickness.setPreferredSize(new java.awt.Dimension(108, 57));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel_FeetThickness.add(RightFoot_Thickness, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Feet.add(jPanel_FeetThickness, gridBagConstraints);

        jTabbedPane_LegsFeet.addTab("Feet", jPanel_Feet);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane_LegsFeet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(jTabbedPane_LegsFeet, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 600, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

////////////////////////////////////////////////////////////////////////////////
// MUTATORS
////////////////////////////////////////////////////////////////////////////////

    public void setParentFrame(JFrame frame) {
        m_baseFrame = frame;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.JPanel_VerticalSliderT LeftFoot_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftFoot_Thickness;
    private imi.gui.JPanel_VerticalSliderT LeftLowerleg_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftLowerleg_Thickness;
    private imi.gui.JPanel_VerticalSliderT LeftUpperleg_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftUpperleg_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightFoot_Length;
    private imi.gui.JPanel_HorizontalSliderS RightFoot_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightLowerleg_Length;
    private imi.gui.JPanel_HorizontalSliderS RightLowerleg_Thickness;
    private imi.gui.JPanel_VerticalSliderT RightUpperleg_Length;
    private imi.gui.JPanel_HorizontalSliderS RightUpperleg_Thickness;
    private javax.swing.JCheckBox jCheckBox_SyncFeetLength;
    private javax.swing.JCheckBox jCheckBox_SyncFeetThickness;
    private javax.swing.JCheckBox jCheckBox_SyncLLLength;
    private javax.swing.JCheckBox jCheckBox_SyncLLThickness;
    private javax.swing.JCheckBox jCheckBox_SyncULLength;
    private javax.swing.JCheckBox jCheckBox_SyncULThickness;
    private javax.swing.JPanel jPanel_Feet;
    private javax.swing.JPanel jPanel_FeetLength;
    private javax.swing.JPanel jPanel_FeetThickness;
    private javax.swing.JPanel jPanel_LLLength;
    private javax.swing.JPanel jPanel_LLThickness;
    private javax.swing.JPanel jPanel_Lowerlegs;
    private javax.swing.JPanel jPanel_ULLength;
    private javax.swing.JPanel jPanel_ULThickness;
    private javax.swing.JPanel jPanel_Upperlegs;
    private javax.swing.JTabbedPane jTabbedPane_LegsFeet;
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
        LeftFoot_Length.setObjectRef(GUI_Enums.sliderControl.leftfootLength);
        LeftFoot_Thickness.setObjectRef(GUI_Enums.sliderControl.leftfootThickness);
        LeftLowerleg_Length.setObjectRef(GUI_Enums.sliderControl.leftlowerlegLength);
        LeftLowerleg_Thickness.setObjectRef(GUI_Enums.sliderControl.leftlowerlegThickness);
        LeftUpperleg_Length.setObjectRef(GUI_Enums.sliderControl.leftupperlegLength);
        LeftUpperleg_Thickness.setObjectRef(GUI_Enums.sliderControl.leftupperlegThickness);
        RightFoot_Length.setObjectRef(GUI_Enums.sliderControl.rightfootLength);
        RightFoot_Thickness.setObjectRef(GUI_Enums.sliderControl.rightfootThickness);
        RightLowerleg_Length.setObjectRef(GUI_Enums.sliderControl.rightlowerlegLength);
        RightLowerleg_Thickness.setObjectRef(GUI_Enums.sliderControl.rightlowerlegThickness);
        RightUpperleg_Length.setObjectRef(GUI_Enums.sliderControl.rightupperlegLength);
        RightUpperleg_Thickness.setObjectRef(GUI_Enums.sliderControl.rightupperlegThickness);

        LeftFoot_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftFoot_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftLowerleg_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftLowerleg_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftUpperleg_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        LeftUpperleg_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightFoot_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightFoot_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightLowerleg_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightLowerleg_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightUpperleg_Length.setParentFrame((JFrame_AdvOptions) m_baseFrame);
        RightUpperleg_Thickness.setParentFrame((JFrame_AdvOptions) m_baseFrame);
    }
}
