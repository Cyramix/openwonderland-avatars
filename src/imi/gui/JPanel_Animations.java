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
package imi.gui;

/**
 *
 * @author  Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_Animations extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Scene Data */
    imi.scene.PScene        m_pscene        = null;
    imi.gui.SceneEssentials m_sceneInfo     = null;
    /** Timer */
    javax.swing.Timer       m_animTimer;
    /** Animation Data */
    private boolean         m_bStopped      = false;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /** Creates new form JPanel_Animations */
    public JPanel_Animations() {
        initComponents();
        initTimer();
    }

    /**
     * Sets up a timer and adds an action listener to the panel
     */
    public void initTimer() {
        m_animTimer = new javax.swing.Timer(1, new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateAnimTime();
            }
        });
        m_animTimer.setInitialDelay(1);
    }
    
    /**
     * Scans the current scene for all model instances and then populates the
     * combobox with references to all the model instances in the scene
     */
    public void reloadModelInstances() {
        imi.scene.utils.tree.InstanceSearchProcessor proc = new imi.scene.utils.tree.InstanceSearchProcessor();
        proc.setProcessor();
        imi.scene.utils.tree.TreeTraverser.breadthFirst(m_pscene, proc);
        java.util.Vector<imi.scene.PNode> instances = proc.getModelInstances();
        jComboBox_ModelInstances.setModel(new javax.swing.DefaultComboBoxModel(instances));
    }
    
    /**
     * Scans the selected model instance (if skinned) for animation data, and
     * then loads the names of the animations in the combobox
     */
    public void reloadSelectedModelAnimations() {
        if(jComboBox_ModelInstances.getSelectedIndex() >= 0) {
            imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
            imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                if (skeleton.getAnimationGroup() != null) {

                    int iNumAnimations = skeleton.getAnimationGroup().getCycleCount();
                    int iCurrentAnim = skeleton.getAnimationState().getCurrentCycle();
                    String[] szAnimations = new String[iNumAnimations];

                    for (int i = 0; i < szAnimations.length; i++) {
                        szAnimations[i] = skeleton.getAnimationGroup().getCycle(i).getName();
                    }

                    jComboBox_Animations.setEnabled(true);
                    jSlider_Animations.setEnabled(true);
                    jComboBox_Animations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_Animations.setSelectedIndex(iCurrentAnim);

                } else {
                    String[] szAnimations = new String[1];
                    szAnimations[0] = "No Animations";
                    jComboBox_Animations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_Animations.setEnabled(false);
                    jSlider_Animations.setEnabled(false);
                    jComboBox_Animations.setSelectedIndex(0);
                }
            }
        } else {
            String[] szAnimations = new String[1];
            szAnimations[0] = "No Animations";
            jComboBox_Animations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
            jComboBox_Animations.setEnabled(false);
            jSlider_Animations.setEnabled(false);
            jComboBox_Animations.setSelectedIndex(0);
        }
    }
    
    /**
     * Resets the speed slider based on the loaded modelinstance
     */
    public void setSpeedSlider() {
        if(jComboBox_ModelInstances.getSelectedIndex() >= 0) {
            imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
            imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                if (skeleton.getAnimationGroup() != null) {
                    jSlider_Animations.setValue(((int) skeleton.getAnimationState().getAnimationSpeed() * 10));
                    jSlider_Animations.setEnabled(true);
                } else {
                    jSlider_Animations.setEnabled(false);
                }
            } else {
                jSlider_Animations.setEnabled(false);
            }
        }
    }    
    
    /**
     * Based on if the there is skinned based animations, the elapsed time for
     * the current animation as well as the time section of for the animation is
     * updated (primary use to split up animations)
     */
    public void updateAnimTime() {
        if(jComboBox_ModelInstances.getSelectedIndex() >= 0) {
            imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
            imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                if (skeleton.getAnimationGroup() != null) {
                    jFormattedTextField_Time.setEnabled(true);
                    jFormattedTextField_CycleTime.setEnabled(true);

                    // Determine the what are the animation indices
                    int curIndex = jComboBox_Animations.getSelectedIndex();
                    float cycleTimeMil, cycleTimeSec, cycleTimeMin, elapsedTimeMil, elapsedTimeSec, elapsedTimeMin;
                    elapsedTimeMil = elapsedTimeSec = elapsedTimeMin = 0.0f;

                    // Get Time in the current animation cycle
                    cycleTimeMil = skeleton.getAnimationState().getCurrentCycleTime() * 1000F;
                    cycleTimeSec = skeleton.getAnimationState().getCurrentCycleTime();
                    cycleTimeMin = skeleton.getAnimationState().getCurrentCycleTime() / 60F;
                    if(!m_bStopped) {
                        elapsedTimeMil = (skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime()) * 1000F;
                        elapsedTimeSec = skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime();
                        elapsedTimeMin = (skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime()) / 60F;
                    }                
                    Integer minutes = 0;    Integer seconds = 0;    Integer millisec = 0;
                    String time = null;

                    // Update the fields for the time display based on if the animation is paused or stopped
                    if(m_bStopped) {
                        minutes = 0;    seconds = 0;    millisec = 0;
                        time = minutes.toString() + ":" + seconds.toString() + ":" + millisec.toString();
                        jFormattedTextField_Time.setText(time);
                        jFormattedTextField_CycleTime.setText(time);
                        return;
                    } else if (skeleton.getAnimationState().isPauseAnimation()) {
                        return;
                    }

                    // Get rid of the floating points and update the displayed animation time
                    minutes = (int)elapsedTimeMin;
                    seconds = (int)elapsedTimeSec - ((int)elapsedTimeMin * 60);
                    millisec = (int)elapsedTimeMil - ((int)elapsedTimeSec * 1000);
                    time = minutes.toString() + ":" + seconds.toString() + ":" + millisec.toString();
                    jFormattedTextField_Time.setText(time);
                    minutes = (int)cycleTimeMin;
                    seconds = (int)cycleTimeSec - ((int)cycleTimeMin * 60);
                    millisec = (int)cycleTimeMil - ((int)cycleTimeSec * 1000);
                    time = minutes.toString() + ":" + seconds.toString() + ":" + millisec.toString();
                    jFormattedTextField_CycleTime.setText(time);
                } else {
                    jFormattedTextField_Time.setEnabled(false);
                    jFormattedTextField_CycleTime.setEnabled(false);
                }
            }
        } else {
            jFormattedTextField_Time.setEnabled(false);
            jFormattedTextField_CycleTime.setEnabled(false);
        }
    }
    
    /**
     * Updates the animation based on the button pressed (Play (0), Pause (1),
     * Stop (2))
     * @param button (Integer representing which button is pressed)
     */
    public void mediaFunction(int button) {
        if(jComboBox_ModelInstances.getSelectedIndex() >= 0) {
            imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
            imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
            if(node != null) {
                imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
                
                switch(button)
                {
                    case 0:
                    {
                        if(m_bStopped) {
                            skeleton.getAnimationState().setCurrentCycle(jComboBox_Animations.getSelectedIndex());
                            float time = skeleton.getAnimationGroup().getCycle(skeleton.getAnimationState().getCurrentCycle()).getStartTime();
                            skeleton.getAnimationState().setCurrentCycleTime(time);
                            m_bStopped = false;
                        }
                        skeleton.getAnimationState().setPauseAnimation(false);
                        break;
                    }
                    case 1:
                    {
                        skeleton.getAnimationState().setPauseAnimation(true);
                        break;
                    }
                    case 2:
                    {
                        m_bStopped = true;
                        skeleton.getAnimationState().setPauseAnimation(true);
                        break;
                    }
                }
            }
        }        
    }

    public void loadAnimations() {
        m_sceneInfo.loadDAEAnimationFile(true, this);
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public javax.swing.Timer getAnimTimer() { return m_animTimer; }
    public imi.scene.PNode getSelectedModelInstanceNode() {return (imi.scene.PNode) jComboBox_ModelInstances.getSelectedItem(); }
    public imi.scene.polygonmodel.PPolygonModelInstance getSelectedModelInstance() {
        if (jComboBox_ModelInstances.getSelectedIndex() >= 0) {
            return ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
        }
        return null;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////    
    public void setAnimTimer(javax.swing.Timer animTime) { 
        m_animTimer = animTime;
    }
    public void setPScene(imi.scene.PScene pScene) {
        m_pscene = pScene;
    }
    public void startTimer() { 
        m_animTimer.start();
    }
    public void setPanel(imi.gui.SceneEssentials sceneData) {
        m_sceneInfo = sceneData;
        m_pscene    = sceneData.getPScene();
        reloadModelInstances();
        reloadSelectedModelAnimations();
        setSpeedSlider();
    }
    public void resetPanel() {
        reloadModelInstances();
        reloadSelectedModelAnimations();
        setSpeedSlider();
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

        jLabel_ElapsedTime = new javax.swing.JLabel();
        java.text.Format time = new java.text.SimpleDateFormat("mm:ss:SS");
        jFormattedTextField_Time = new javax.swing.JFormattedTextField(time);
        jLabel_CurCycleTime = new javax.swing.JLabel();
        java.text.Format cycleTime = new java.text.SimpleDateFormat("mm:ss:SS");
        jFormattedTextField_CycleTime = new javax.swing.JFormattedTextField(cycleTime);
        jLabel_AnimSpeed = new javax.swing.JLabel();
        jSlider_Animations = new javax.swing.JSlider();
        jComboBox_ModelInstances = new javax.swing.JComboBox();
        jComboBox_Animations = new javax.swing.JComboBox();
        jButton_Play = new javax.swing.JButton();
        jButton_Pause = new javax.swing.JButton();
        jButton_Stop = new javax.swing.JButton();
        jButton_Reload = new javax.swing.JButton();
        jButton_AddAnim = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Model Animations", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(266, 250));
        setMinimumSize(new java.awt.Dimension(266, 250));
        setPreferredSize(new java.awt.Dimension(266, 250));
        setLayout(new java.awt.GridBagLayout());

        jLabel_ElapsedTime.setText("Elapsed Time: ");
        jLabel_ElapsedTime.setMaximumSize(new java.awt.Dimension(230, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jLabel_ElapsedTime, gridBagConstraints);

        jFormattedTextField_Time.setText("mm:ss:SS");
        jFormattedTextField_Time.setMaximumSize(new java.awt.Dimension(230, 25));
        jFormattedTextField_Time.setMinimumSize(new java.awt.Dimension(98, 25));
        jFormattedTextField_Time.setPreferredSize(new java.awt.Dimension(98, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jFormattedTextField_Time, gridBagConstraints);

        jLabel_CurCycleTime.setText("Cycle Time:    ");
        jLabel_CurCycleTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jLabel_CurCycleTime.setMinimumSize(new java.awt.Dimension(89, 25));
        jLabel_CurCycleTime.setPreferredSize(new java.awt.Dimension(89, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jLabel_CurCycleTime, gridBagConstraints);

        jFormattedTextField_CycleTime.setText("mm:ss:SS");
        jFormattedTextField_CycleTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jFormattedTextField_CycleTime.setMinimumSize(new java.awt.Dimension(14, 25));
        jFormattedTextField_CycleTime.setPreferredSize(new java.awt.Dimension(98, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jFormattedTextField_CycleTime, gridBagConstraints);

        jLabel_AnimSpeed.setText("Animation Speed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(jLabel_AnimSpeed, gridBagConstraints);

        jSlider_Animations.setMaximum(40);
        jSlider_Animations.setMinimum(1);
        jSlider_Animations.setValue(10);
        jSlider_Animations.setMaximumSize(new java.awt.Dimension(200, 29));
        jSlider_Animations.setPreferredSize(new java.awt.Dimension(120, 29));
        jSlider_Animations.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent e) {
                jSlider_AnimationsStateChanged(e);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jSlider_Animations, gridBagConstraints);

        jComboBox_ModelInstances.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_ModelInstances.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_ModelInstances.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_ModelInstances.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_ModelInstances.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadSelectedModelAnimations();
                setSpeedSlider();
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 160;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox_ModelInstances, gridBagConstraints);

        jComboBox_Animations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Animations.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_Animations.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_Animations.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_Animations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_AnimationsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 160;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox_Animations, gridBagConstraints);

        jButton_Play.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(0);
            }
        });
        jButton_Play.setText("PLAY");
        jButton_Play.setMaximumSize(new java.awt.Dimension(83, 29));
        jButton_Play.setMinimumSize(new java.awt.Dimension(83, 29));
        jButton_Play.setPreferredSize(new java.awt.Dimension(83, 29));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton_Play, gridBagConstraints);

        jButton_Pause.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(1);
            }
        });
        jButton_Pause.setText("PAUSE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, -28, 0, 0);
        add(jButton_Pause, gridBagConstraints);

        jButton_Stop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(2);
            }
        });
        jButton_Stop.setText("STOP");
        jButton_Stop.setMaximumSize(new java.awt.Dimension(83, 29));
        jButton_Stop.setMinimumSize(new java.awt.Dimension(83, 29));
        jButton_Stop.setPreferredSize(new java.awt.Dimension(83, 29));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton_Stop, gridBagConstraints);

        jButton_Reload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadModelInstances();
                reloadSelectedModelAnimations();
            }
        });
        jButton_Reload.setText("Reload Scene Information");
        jButton_Reload.setMaximumSize(new java.awt.Dimension(150, 29));
        jButton_Reload.setMinimumSize(new java.awt.Dimension(150, 29));
        jButton_Reload.setPreferredSize(new java.awt.Dimension(150, 29));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 98;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton_Reload, gridBagConstraints);

        jButton_AddAnim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAnimations();
                reloadSelectedModelAnimations();
            }
        });
        jButton_AddAnim.setText("Add Animation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton_AddAnim, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void jComboBox_AnimationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_AnimationsActionPerformed
if (jComboBox_Animations.isEnabled()) {
        imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
        imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
        imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
        skeleton.getAnimationState().setPauseAnimation(false);
        skeleton.transitionTo(jComboBox_Animations.getSelectedItem().toString(), false);
    }
}//GEN-LAST:event_jComboBox_AnimationsActionPerformed

/**
 * Change the speed of the animation
 * @param e (ChangeEvent)
 */
private void jSlider_AnimationsStateChanged(javax.swing.event.ChangeEvent e) {
    if (jSlider_Animations.isEnabled()) {
        float fAnimSpeed = (jSlider_Animations.getValue() * 0.10f);
        imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
        imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
        imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
        skeleton.getAnimationState().setAnimationSpeed(fAnimSpeed);
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_AddAnim;
    private javax.swing.JButton jButton_Pause;
    private javax.swing.JButton jButton_Play;
    private javax.swing.JButton jButton_Reload;
    private javax.swing.JButton jButton_Stop;
    private javax.swing.JComboBox jComboBox_Animations;
    private javax.swing.JComboBox jComboBox_ModelInstances;
    private javax.swing.JFormattedTextField jFormattedTextField_CycleTime;
    private javax.swing.JFormattedTextField jFormattedTextField_Time;
    private javax.swing.JLabel jLabel_AnimSpeed;
    private javax.swing.JLabel jLabel_CurCycleTime;
    private javax.swing.JLabel jLabel_ElapsedTime;
    private javax.swing.JSlider jSlider_Animations;
    // End of variables declaration//GEN-END:variables

}
