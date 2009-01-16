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

/**
 *
 * @author  Paul Viet Ngueyn Truong (ptruong)
 */
public class JPanel_ModelRotation extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Rotation Data */
    private imi.scene.PMatrix xRotation = new imi.scene.PMatrix();
    private imi.scene.PMatrix yRotation = new imi.scene.PMatrix();
    private imi.scene.PMatrix zRotation = new imi.scene.PMatrix();
    private boolean bSpin = false;
    /** Scene Data */
    private imi.scene.PScene pscene = null;
    private imi.scene.PNode modelInst = null;
    /** Timer */
    private javax.swing.Timer refreshTimer;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form JPanel_ModelRotation */
    public JPanel_ModelRotation() {
        initComponents();
        initTimer();
    }
    
    /**
     * Sets up a timer and adds an actionlistener to the panel
     */
    public void initTimer() {
        refreshTimer = new javax.swing.Timer(50, new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (bSpin) {
                    AutoSpin();
                }
            }
        });
        refreshTimer.setInitialDelay(50);
        refreshTimer.start();
    }

    /**
     * Through the use of the timer, the method call updates the rotation of the
     * avatar along the YAxis in a positive spin
     */    
    public void AutoSpin() {
        int value = jSlider_YAxis.getValue() + 1;
        if(value > 359)
            value = 1;
        jSlider_YAxis.setValue(value);
        RotateOnAxis(1);
        bSpin = true;
    }
    
    /**
     * Rotates the model based on the slider (axis) that is used
     * @param axis (Integer)
     */
    public void RotateOnAxis(int axis) {
        imi.scene.PMatrix rotMatrix = new imi.scene.PMatrix();
        int degree = 0;
        float radians = 0;
        switch (axis) {
            case 0: // XAxis
            {
                degree = ((Integer) jSlider_XAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                xRotation.buildRotationX(radians);
                break;
            }
            case 1: // YAxis
            {
                degree = ((Integer) jSlider_YAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                yRotation.buildRotationY(radians);
                break;
            }
            case 2: // ZAxis
            {
                degree = ((Integer) jSlider_ZAxis.getValue()).intValue();
                if (degree >= 180) {
                    degree -= 180;
                } else {
                    degree += 180;
                }
                radians = (float) java.lang.Math.toRadians((double) degree);
                zRotation.buildRotationZ(radians);
                break;
            }
        }
        rotMatrix.mul(yRotation);
        rotMatrix.mul(xRotation);
        rotMatrix.mul(zRotation);
        
        if (modelInst != null) {
            modelInst.getTransform().getLocalMatrix(true).setRotation(rotMatrix.getRotation());
            modelInst.setDirty(true, true);
        }
    }    

    /**
     * Resets the axes (X,Y,Z) by resetting the axes sliders to the center (180),
     * reseting the data members to the identity matrix, and then resets the 
     * models rotation matrix;
     */
    public void ResetAxes() {
        jSlider_XAxis.setValue(180);
        jSlider_YAxis.setValue(180);
        jSlider_ZAxis.setValue(180);

        xRotation.setIdentity();
        yRotation.setIdentity();
        zRotation.setIdentity();

        if (modelInst != null) {
            modelInst.getTransform().getLocalMatrix(true).setRotation(new com.jme.math.Matrix3f());
        }        
    }
    
////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public imi.scene.PMatrix getXRotMatrix() { return xRotation; }

    public imi.scene.PMatrix getYRotMatrix() { return yRotation; }

    public imi.scene.PMatrix getZRotMatrix() { return zRotation; }

    public boolean isSpinning() { return bSpin; }

    public javax.swing.Timer getSpinTimer() { return refreshTimer; }
    
////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////
    public void setXRotMatrix(imi.scene.PMatrix xMatrix) { xRotation = xMatrix; }

    public void setYRotMatrix(imi.scene.PMatrix yMatrix) { yRotation = yMatrix; }

    public void setZRotMatrix(imi.scene.PMatrix zMatrix) { zRotation = zMatrix; }

    public void setSpin(boolean spin) { bSpin = spin; }

    public void setSpinTimer(javax.swing.Timer spinTime) { refreshTimer = spinTime; }

    public void setPScene(imi.scene.PScene pScene) { pscene = pScene; }

    public void setModelInst(imi.scene.PNode pNode) { modelInst = pNode; }

    public void setPanel(imi.scene.PScene pScene, imi.scene.PNode pNode) {
        pscene = pScene;    modelInst = pNode;
    }

    public void resetPanel() { bSpin = false;   jCheckBox_AutoSpinY.setSelected(false); ResetAxes(); }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar_XAxisControls = new javax.swing.JToolBar();
        jLabel_XAxis = new javax.swing.JLabel();
        jSlider_XAxis = new javax.swing.JSlider();
        jToolBar_YAxisControls = new javax.swing.JToolBar();
        jLabel_YAxis = new javax.swing.JLabel();
        jSlider_YAxis = new javax.swing.JSlider();
        jToolBar_ZAxisControls = new javax.swing.JToolBar();
        jLabel_ZAxis = new javax.swing.JLabel();
        jSlider_ZAxis = new javax.swing.JSlider();
        jToolBar_AutoRotateControl = new javax.swing.JToolBar();
        jCheckBox_AutoSpinY = new javax.swing.JCheckBox();
        jToolBar_ResetAxesControl = new javax.swing.JToolBar();
        jButton_RotationsReset = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Model Rotation", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(230, 175));
        setMinimumSize(new java.awt.Dimension(230, 175));
        setPreferredSize(new java.awt.Dimension(230, 175));

        jToolBar_XAxisControls.setFloatable(false);
        jToolBar_XAxisControls.setRollover(true);
        jToolBar_XAxisControls.setMaximumSize(new java.awt.Dimension(32815, 25));
        jToolBar_XAxisControls.setMinimumSize(new java.awt.Dimension(84, 25));
        jToolBar_XAxisControls.setPreferredSize(new java.awt.Dimension(148, 25));

        jLabel_XAxis.setText("X Axis");
        jToolBar_XAxisControls.add(jLabel_XAxis);

        jSlider_XAxis.setMaximum(359);
        jSlider_XAxis.setMinimum(1);
        jSlider_XAxis.setValue(180);
        jSlider_XAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_XAxis.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent e) {
                RotateOnAxis(0);
            }
        });
        jToolBar_XAxisControls.add(jSlider_XAxis);

        jToolBar_YAxisControls.setFloatable(false);
        jToolBar_YAxisControls.setRollover(true);
        jToolBar_YAxisControls.setMaximumSize(new java.awt.Dimension(32815, 25));
        jToolBar_YAxisControls.setMinimumSize(new java.awt.Dimension(84, 25));
        jToolBar_YAxisControls.setPreferredSize(new java.awt.Dimension(148, 25));

        jLabel_YAxis.setText("Y Axis");
        jToolBar_YAxisControls.add(jLabel_YAxis);

        jSlider_YAxis.setMaximum(359);
        jSlider_YAxis.setMinimum(1);
        jSlider_YAxis.setValue(180);
        jSlider_YAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_YAxis.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent e) {
                RotateOnAxis(1);
            }
        });
        jToolBar_YAxisControls.add(jSlider_YAxis);

        jToolBar_ZAxisControls.setFloatable(false);
        jToolBar_ZAxisControls.setRollover(true);
        jToolBar_ZAxisControls.setMaximumSize(new java.awt.Dimension(32815, 25));
        jToolBar_ZAxisControls.setMinimumSize(new java.awt.Dimension(84, 25));
        jToolBar_ZAxisControls.setPreferredSize(new java.awt.Dimension(148, 25));

        jLabel_ZAxis.setText("Z Axis");
        jToolBar_ZAxisControls.add(jLabel_ZAxis);

        jSlider_ZAxis.setMaximum(359);
        jSlider_ZAxis.setMinimum(1);
        jSlider_ZAxis.setValue(180);
        jSlider_ZAxis.setPreferredSize(new java.awt.Dimension(100, 29));
        jSlider_ZAxis.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent e) {
                RotateOnAxis(2);
            }
        });
        jToolBar_ZAxisControls.add(jSlider_ZAxis);

        jToolBar_AutoRotateControl.setFloatable(false);
        jToolBar_AutoRotateControl.setRollover(true);

        jCheckBox_AutoSpinY.setText("Auto Rotate YAxis                  ");
        jCheckBox_AutoSpinY.setFocusable(false);
        jCheckBox_AutoSpinY.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox_AutoSpinY.setMaximumSize(new java.awt.Dimension(230, 25));
        jCheckBox_AutoSpinY.setPreferredSize(new java.awt.Dimension(230, 25));
        jCheckBox_AutoSpinY.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox_AutoSpinYItemStateChanged(evt);
            }
        });
        jToolBar_AutoRotateControl.add(jCheckBox_AutoSpinY);

        jToolBar_ResetAxesControl.setFloatable(false);
        jToolBar_ResetAxesControl.setRollover(true);

        jButton_RotationsReset.setText("Reset Axes");
        jButton_RotationsReset.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_RotationsReset.setFocusable(false);
        jButton_RotationsReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_RotationsReset.setMaximumSize(new java.awt.Dimension(230, 25));
        jButton_RotationsReset.setMinimumSize(new java.awt.Dimension(42, 25));
        jButton_RotationsReset.setPreferredSize(new java.awt.Dimension(230, 25));
        jButton_RotationsReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_RotationsReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetAxes();
            }
        });
        jToolBar_ResetAxesControl.add(jButton_RotationsReset);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar_XAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_YAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_ZAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_AutoRotateControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(jToolBar_ResetAxesControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar_XAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_YAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_ZAxisControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_AutoRotateControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_ResetAxesControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Enables/Disables the model from spinning on the Y-Axis
     * @param evt
     */
private void jCheckBox_AutoSpinYItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox_AutoSpinYItemStateChanged
    if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
        bSpin = true;
    } else {
        bSpin = false;
    }
}//GEN-LAST:event_jCheckBox_AutoSpinYItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_RotationsReset;
    private javax.swing.JCheckBox jCheckBox_AutoSpinY;
    private javax.swing.JLabel jLabel_XAxis;
    private javax.swing.JLabel jLabel_YAxis;
    private javax.swing.JLabel jLabel_ZAxis;
    private javax.swing.JSlider jSlider_XAxis;
    private javax.swing.JSlider jSlider_YAxis;
    private javax.swing.JSlider jSlider_ZAxis;
    private javax.swing.JToolBar jToolBar_AutoRotateControl;
    private javax.swing.JToolBar jToolBar_ResetAxesControl;
    private javax.swing.JToolBar jToolBar_XAxisControls;
    private javax.swing.JToolBar jToolBar_YAxisControls;
    private javax.swing.JToolBar jToolBar_ZAxisControls;
    // End of variables declaration//GEN-END:variables
}
