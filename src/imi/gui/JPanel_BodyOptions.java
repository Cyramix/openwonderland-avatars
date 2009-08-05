/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanel_BodyOptions.java
 *
 * Created on Jan 23, 2009, 5:38:37 PM
 */

package imi.gui;

import javax.swing.JFrame;

/**
 *
 * @author ptruong
 */
public class JPanel_BodyOptions extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    private JFrame  m_baseFrame     =   null;


////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form JPanel_SimpBodyOptions */
    public JPanel_BodyOptions() {
        initComponents();
    }

    public JPanel_BodyOptions(JFrame baseFrame) {
        m_baseFrame = baseFrame;
        initComponents();
        setSliderControls();
    }

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public void setParentFrame(JFrame frame) {
        m_baseFrame = frame;
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

        Uniform = new javax.swing.JTabbedPane();
        Torso = new javax.swing.JPanel();
        jPanel_TorsoLength = new javax.swing.JPanel();
        TorsoLength = new imi.gui.JPanel_VerticalSliderT();
        jPanel_TorsoThickness = new javax.swing.JPanel();
        TorsoThickness = new imi.gui.JPanel_HorizontalSliderS();
        Abs = new javax.swing.JPanel();
        jPanel_StomachRoundness = new javax.swing.JPanel();
        StomachRoundness = new imi.gui.JPanel_HorizontalSliderS();
        Body = new javax.swing.JPanel();
        jPanel_Height = new javax.swing.JPanel();
        AvatarHeight = new imi.gui.JPanel_VerticalSliderS();
        jPanel_Weight = new javax.swing.JPanel();
        AvatarThickness = new imi.gui.JPanel_HorizontalSliderS();

        Uniform.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        Uniform.setMaximumSize(new java.awt.Dimension(270, 600));
        Uniform.setMinimumSize(new java.awt.Dimension(270, 600));

        Torso.setLayout(new java.awt.GridBagLayout());

        jPanel_TorsoLength.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Torso Length", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_TorsoLength.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_TorsoLength.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_TorsoLength.setLayout(new java.awt.GridBagLayout());
        jPanel_TorsoLength.add(TorsoLength, new java.awt.GridBagConstraints());

        Torso.add(jPanel_TorsoLength, new java.awt.GridBagConstraints());

        jPanel_TorsoThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Torso Thickness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_TorsoThickness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_TorsoThickness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_TorsoThickness.setLayout(new java.awt.GridBagLayout());
        jPanel_TorsoThickness.add(TorsoThickness, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        Torso.add(jPanel_TorsoThickness, gridBagConstraints);

        Uniform.addTab("Torso", Torso);

        Abs.setLayout(new java.awt.GridBagLayout());

        jPanel_StomachRoundness.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Stomach Roundness", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_StomachRoundness.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_StomachRoundness.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_StomachRoundness.setLayout(new java.awt.GridBagLayout());
        jPanel_StomachRoundness.add(StomachRoundness, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        Abs.add(jPanel_StomachRoundness, gridBagConstraints);

        Uniform.addTab("Abs", Abs);

        Body.setLayout(new java.awt.GridBagLayout());

        jPanel_Height.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Height", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_Height.setMaximumSize(new java.awt.Dimension(230, 230));
        jPanel_Height.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_Height.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_Height.setLayout(new java.awt.GridBagLayout());
        jPanel_Height.add(AvatarHeight, new java.awt.GridBagConstraints());

        Body.add(jPanel_Height, new java.awt.GridBagConstraints());

        jPanel_Weight.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Weight", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jPanel_Weight.setMaximumSize(new java.awt.Dimension(230, 230));
        jPanel_Weight.setMinimumSize(new java.awt.Dimension(230, 230));
        jPanel_Weight.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanel_Weight.setLayout(new java.awt.GridBagLayout());
        jPanel_Weight.add(AvatarThickness, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        Body.add(jPanel_Weight, gridBagConstraints);

        Uniform.addTab("Uniform", Body);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 270, Short.MAX_VALUE)
            .addComponent(Uniform, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
            .addComponent(Uniform, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Abs;
    private imi.gui.JPanel_VerticalSliderS AvatarHeight;
    private imi.gui.JPanel_HorizontalSliderS AvatarThickness;
    private javax.swing.JPanel Body;
    private imi.gui.JPanel_HorizontalSliderS StomachRoundness;
    private javax.swing.JPanel Torso;
    private imi.gui.JPanel_VerticalSliderT TorsoLength;
    private imi.gui.JPanel_HorizontalSliderS TorsoThickness;
    private javax.swing.JTabbedPane Uniform;
    private javax.swing.JPanel jPanel_Height;
    private javax.swing.JPanel jPanel_StomachRoundness;
    private javax.swing.JPanel jPanel_TorsoLength;
    private javax.swing.JPanel jPanel_TorsoThickness;
    private javax.swing.JPanel jPanel_Weight;
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
        AvatarHeight.setObjectRef(GUI_Enums.sliderControl.uniformHeight);
        AvatarThickness.setObjectRef(GUI_Enums.sliderControl.uniformThickness);
        TorsoLength.setObjectRef(GUI_Enums.sliderControl.torsoLength);
        TorsoThickness.setObjectRef(GUI_Enums.sliderControl.torsoThickness);
        StomachRoundness.setObjectRef(GUI_Enums.sliderControl.stomachRoundness);

        AvatarHeight.setParentFrame(m_baseFrame);
        AvatarThickness.setParentFrame(m_baseFrame);
        TorsoLength.setParentFrame(m_baseFrame);
        TorsoThickness.setParentFrame(m_baseFrame);
        StomachRoundness.setParentFrame(m_baseFrame);
    }
}
