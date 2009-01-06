/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanel_SimpArmsLegsOptions.java
 *
 * Created on Jan 5, 2009, 4:47:49 PM
 */

package imi.gui;

import javax.swing.JFrame;

/**
 *
 * @author ptruong
 */
public class JPanel_SimpArmsLegsOptions extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    private JFrame  m_baseFrame     =   null;

    /** Creates new form JPanel_SimpArmsLegsOptions */
    public JPanel_SimpArmsLegsOptions() {
        initComponents();
    }

    public JPanel_SimpArmsLegsOptions(JFrame baseFrame) {
        m_baseFrame = baseFrame;
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
        java.awt.GridBagConstraints gridBagConstraints;

        ArmsNLegs = new javax.swing.JTabbedPane();
        jPanel_Arms = new javax.swing.JPanel();
        ArmsLength = new javax.swing.JPanel();
        LeftArm_Length = new imi.gui.JPanel_VerticalSliderT();
        RightArm_Length = new imi.gui.JPanel_VerticalSliderT();
        ArmsScale = new javax.swing.JPanel();
        LeftArm_Scale = new imi.gui.JPanel_HorizontalSliderS();
        RightArm_Scale = new imi.gui.JPanel_HorizontalSliderS();
        jPanel_Legs = new javax.swing.JPanel();
        LegsLength = new javax.swing.JPanel();
        LeftLeg_Length = new imi.gui.JPanel_VerticalSliderT();
        RightLeg_Length = new imi.gui.JPanel_VerticalSliderT();
        LegsScale = new javax.swing.JPanel();
        LeftLeg_Scale = new imi.gui.JPanel_HorizontalSliderS();
        RightLeg_Scale = new imi.gui.JPanel_HorizontalSliderS();

        setMaximumSize(new java.awt.Dimension(270, 600));
        setMinimumSize(new java.awt.Dimension(270, 600));
        setPreferredSize(new java.awt.Dimension(270, 600));

        ArmsNLegs.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        ArmsNLegs.setMaximumSize(new java.awt.Dimension(270, 600));
        ArmsNLegs.setMinimumSize(new java.awt.Dimension(270, 600));
        ArmsNLegs.setPreferredSize(new java.awt.Dimension(270, 600));

        jPanel_Arms.setLayout(new java.awt.GridBagLayout());

        ArmsLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Arm Lengths", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        ArmsLength.setMinimumSize(new java.awt.Dimension(220, 230));
        ArmsLength.setPreferredSize(new java.awt.Dimension(220, 230));
        ArmsLength.setLayout(new java.awt.GridBagLayout());
        ArmsLength.add(LeftArm_Length, new java.awt.GridBagConstraints());
        ArmsLength.add(RightArm_Length, new java.awt.GridBagConstraints());

        jPanel_Arms.add(ArmsLength, new java.awt.GridBagConstraints());

        ArmsScale.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Arm Bulk", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        ArmsScale.setMinimumSize(new java.awt.Dimension(220, 230));
        ArmsScale.setPreferredSize(new java.awt.Dimension(220, 230));
        ArmsScale.setLayout(new java.awt.GridBagLayout());

        LeftArm_Scale.setMinimumSize(new java.awt.Dimension(105, 57));
        LeftArm_Scale.setPreferredSize(new java.awt.Dimension(105, 57));
        ArmsScale.add(LeftArm_Scale, new java.awt.GridBagConstraints());

        RightArm_Scale.setMinimumSize(new java.awt.Dimension(105, 57));
        RightArm_Scale.setPreferredSize(new java.awt.Dimension(105, 57));
        ArmsScale.add(RightArm_Scale, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Arms.add(ArmsScale, gridBagConstraints);

        ArmsNLegs.addTab("Arms", jPanel_Arms);

        jPanel_Legs.setLayout(new java.awt.GridBagLayout());

        LegsLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Leg Lengths", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        LegsLength.setMinimumSize(new java.awt.Dimension(220, 230));
        LegsLength.setPreferredSize(new java.awt.Dimension(220, 230));
        LegsLength.setLayout(new java.awt.GridBagLayout());
        LegsLength.add(LeftLeg_Length, new java.awt.GridBagConstraints());
        LegsLength.add(RightLeg_Length, new java.awt.GridBagConstraints());

        jPanel_Legs.add(LegsLength, new java.awt.GridBagConstraints());

        LegsScale.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Left & Right Leg Bulk", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        LegsScale.setMinimumSize(new java.awt.Dimension(220, 230));
        LegsScale.setPreferredSize(new java.awt.Dimension(220, 230));
        LegsScale.setLayout(new java.awt.GridBagLayout());

        LeftLeg_Scale.setMinimumSize(new java.awt.Dimension(105, 57));
        LeftLeg_Scale.setPreferredSize(new java.awt.Dimension(105, 57));
        LegsScale.add(LeftLeg_Scale, new java.awt.GridBagConstraints());

        RightLeg_Scale.setMinimumSize(new java.awt.Dimension(105, 57));
        RightLeg_Scale.setPreferredSize(new java.awt.Dimension(105, 57));
        LegsScale.add(RightLeg_Scale, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel_Legs.add(LegsScale, gridBagConstraints);

        ArmsNLegs.addTab("Legs", jPanel_Legs);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(ArmsNLegs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(ArmsNLegs, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ArmsLength;
    private javax.swing.JTabbedPane ArmsNLegs;
    private javax.swing.JPanel ArmsScale;
    private imi.gui.JPanel_VerticalSliderT LeftArm_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftArm_Scale;
    private imi.gui.JPanel_VerticalSliderT LeftLeg_Length;
    private imi.gui.JPanel_HorizontalSliderS LeftLeg_Scale;
    private javax.swing.JPanel LegsLength;
    private javax.swing.JPanel LegsScale;
    private imi.gui.JPanel_VerticalSliderT RightArm_Length;
    private imi.gui.JPanel_HorizontalSliderS RightArm_Scale;
    private imi.gui.JPanel_VerticalSliderT RightLeg_Length;
    private imi.gui.JPanel_HorizontalSliderS RightLeg_Scale;
    private javax.swing.JPanel jPanel_Arms;
    private javax.swing.JPanel jPanel_Legs;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

    public void setParentFrame(JFrame frame) {
        m_baseFrame = frame;
    }

    public void setSliderControls() {
        LeftLeg_Length.setObjectRef(GUI_Enums.m_sliderControl.leftlegLength);
        LeftLeg_Scale.setObjectRef(GUI_Enums.m_sliderControl.leftlegScale);
        RightLeg_Length.setObjectRef(GUI_Enums.m_sliderControl.rightlegLength);
        RightLeg_Scale.setObjectRef(GUI_Enums.m_sliderControl.rightlegScale);

        LeftArm_Length.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        LeftArm_Scale.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        RightArm_Length.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        RightArm_Scale.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
    }

    public void setConstraints() {

    }
}
