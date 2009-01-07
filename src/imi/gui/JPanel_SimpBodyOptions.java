/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JPanel_SimpBodyOptions.java
 *
 * Created on Jan 6, 2009, 10:37:22 AM
 */

package imi.gui;

import javax.swing.JFrame;

/**
 *
 * @author ptruong
 */
public class JPanel_SimpBodyOptions extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
    private JFrame  m_baseFrame     =   null;


////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form JPanel_SimpBodyOptions */
    public JPanel_SimpBodyOptions() {
        initComponents();
    }

    public JPanel_SimpBodyOptions(JFrame baseFrame) {
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

        Uniform = new javax.swing.JTabbedPane();
        Body = new javax.swing.JPanel();
        jPanel_Height = new javax.swing.JPanel();
        AvatarHeight = new imi.gui.JPanel_VerticalSliderS();
        jPanel_Weight = new javax.swing.JPanel();
        AvatarThickness = new imi.gui.JPanel_HorizontalSliderS();
        Torso = new javax.swing.JPanel();
        jPanel_TorsoLength = new javax.swing.JPanel();
        TorsoLength = new imi.gui.JPanel_VerticalSliderT();
        jPanel_TorsoThickness = new javax.swing.JPanel();
        TorsoThickness = new imi.gui.JPanel_HorizontalSliderS();

        setMaximumSize(new java.awt.Dimension(270, 600));
        setMinimumSize(new java.awt.Dimension(270, 600));
        setPreferredSize(new java.awt.Dimension(270, 600));

        Uniform.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        Uniform.setMaximumSize(new java.awt.Dimension(270, 600));
        Uniform.setMinimumSize(new java.awt.Dimension(270, 600));
        Uniform.setPreferredSize(new java.awt.Dimension(270, 600));

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(Uniform, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 270, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(Uniform, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 600, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.JPanel_VerticalSliderS AvatarHeight;
    private imi.gui.JPanel_HorizontalSliderS AvatarThickness;
    private javax.swing.JPanel Body;
    private javax.swing.JPanel Torso;
    private imi.gui.JPanel_VerticalSliderT TorsoLength;
    private imi.gui.JPanel_HorizontalSliderS TorsoThickness;
    private javax.swing.JTabbedPane Uniform;
    private javax.swing.JPanel jPanel_Height;
    private javax.swing.JPanel jPanel_TorsoLength;
    private javax.swing.JPanel jPanel_TorsoThickness;
    private javax.swing.JPanel jPanel_Weight;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////

    public void setParentFrame(JFrame frame) {
        m_baseFrame = frame;
    }

    public void setSliderControls() {
        AvatarHeight.setObjectRef(GUI_Enums.m_sliderControl.uniformHeight);
        AvatarThickness.setObjectRef(GUI_Enums.m_sliderControl.uniformThickness);
        TorsoLength.setObjectRef(GUI_Enums.m_sliderControl.torsoLength);
        TorsoThickness.setObjectRef(GUI_Enums.m_sliderControl.torsoThickness);

        AvatarHeight.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        AvatarThickness.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        TorsoLength.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
        TorsoThickness.setParentFrame((JFrame_SimpAdvOptions) m_baseFrame);
    }

    public void setConstraints() {

    }
}