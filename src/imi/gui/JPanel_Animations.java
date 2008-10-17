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
 * @author  ptruong
 */
public class JPanel_Animations extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    /** Scene Data */
    imi.scene.PScene pscene = null;
    /** Timer */
    javax.swing.Timer animTimer;
    /** Animation Data */
    private boolean bStopped = false;    

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
        animTimer = new javax.swing.Timer(1, new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateAnimTime();
            }
        });
        animTimer.setInitialDelay(1);        
    }
    
    /**
     * Scans the current scene for all model instances and then populates the
     * combobox with references to all the model instances in the scene
     */
    public void reloadModelInstances() {
        imi.scene.utils.tree.InstanceSearchProcessor proc = new imi.scene.utils.tree.InstanceSearchProcessor();
        proc.setProcessor();
        imi.scene.utils.tree.TreeTraverser.breadthFirst(pscene, proc);
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

                    int iNumAnimations = skeleton.getAnimationGroup().getCycles().length;
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
                jSlider_Animations.setValue(((int) skeleton.getAnimationState().getAnimationSpeed() * 10));
                jSlider_Animations.setEnabled(true);
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
                    if(!bStopped) {
                        elapsedTimeMil = (skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime()) * 1000F;
                        elapsedTimeSec = skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime();
                        elapsedTimeMin = (skeleton.getAnimationState().getCurrentCycleTime() - skeleton.getAnimationGroup().getCycle(curIndex).getStartTime()) / 60F;
                    }                
                    Integer minutes = 0;    Integer seconds = 0;    Integer millisec = 0;
                    String time = null;

                    // Update the fields for the time display based on if the animation is paused or stopped
                    if(bStopped) {
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
                        if(bStopped) {
                            skeleton.getAnimationState().setCurrentCycle(jComboBox_Animations.getSelectedIndex());
                            float time = skeleton.getAnimationGroup().getCycle(skeleton.getAnimationState().getCurrentCycle()).getStartTime();
                            skeleton.getAnimationState().setCurrentCycleTime(time);
                            bStopped = false;
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
                        bStopped = true;
                        skeleton.getAnimationState().setPauseAnimation(true);
                        break;
                    }
                }
            }
        }        
    }
    
////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////
    public javax.swing.Timer getAnimTimer() { return animTimer; }
    public imi.scene.PNode getSelectedModelInstanceNode() {return (imi.scene.PNode) jComboBox_ModelInstances.getSelectedItem(); }
    public imi.scene.polygonmodel.PPolygonModelInstance getSelectedModelInstance() {
        return ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////    
    public void setAnimTimer(javax.swing.Timer animTime) { animTimer = animTime; }
    public void setPScene(imi.scene.PScene pScene) { pscene = pScene; }
    public void startTimer() { animTimer.start(); }
    public void setPanel(imi.scene.PScene pScene) {
        pscene = pScene;
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

        jToolBar_ElapsedTime = new javax.swing.JToolBar();
        jLabel_ElapsedTime = new javax.swing.JLabel();
        java.text.Format time = new java.text.SimpleDateFormat("mm:ss:SS");
        jFormattedTextField_Time = new javax.swing.JFormattedTextField(time);
        jToolBar_CurrentCycleTime = new javax.swing.JToolBar();
        jLabel_CurCycleTime = new javax.swing.JLabel();
        java.text.Format cycleTime = new java.text.SimpleDateFormat("mm:ss:SS");
        jFormattedTextField_CycleTime = new javax.swing.JFormattedTextField(cycleTime);
        jToolBar_AnimSpeedGroup = new javax.swing.JToolBar();
        jLabel_AnimSpeed = new javax.swing.JLabel();
        jSlider_Animations = new javax.swing.JSlider();
        jToolBar_ModelInstances = new javax.swing.JToolBar();
        jComboBox_ModelInstances = new javax.swing.JComboBox();
        jToolBar_Animations = new javax.swing.JToolBar();
        jComboBox_Animations = new javax.swing.JComboBox();
        jToolBar_MediaControls = new javax.swing.JToolBar();
        jButton_Play = new javax.swing.JButton();
        jButton_Pause = new javax.swing.JButton();
        jButton_Stop = new javax.swing.JButton();
        jToolBar_Reload = new javax.swing.JToolBar();
        jButton_Reload = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Model Animations", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(230, 260));
        setMinimumSize(new java.awt.Dimension(230, 260));
        setPreferredSize(new java.awt.Dimension(230, 260));

        jToolBar_ElapsedTime.setFloatable(false);
        jToolBar_ElapsedTime.setRollover(true);
        jToolBar_ElapsedTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jToolBar_ElapsedTime.setMinimumSize(new java.awt.Dimension(113, 25));
        jToolBar_ElapsedTime.setPreferredSize(new java.awt.Dimension(230, 25));

        jLabel_ElapsedTime.setText("Elapsed Time: ");
        jLabel_ElapsedTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jToolBar_ElapsedTime.add(jLabel_ElapsedTime);

        jFormattedTextField_Time.setText("mm:ss:SS");
        jFormattedTextField_Time.setMaximumSize(new java.awt.Dimension(230, 25));
        jFormattedTextField_Time.setMinimumSize(new java.awt.Dimension(14, 25));
        jFormattedTextField_Time.setPreferredSize(new java.awt.Dimension(98, 25));
        jToolBar_ElapsedTime.add(jFormattedTextField_Time);

        jToolBar_CurrentCycleTime.setFloatable(false);
        jToolBar_CurrentCycleTime.setRollover(true);
        jToolBar_CurrentCycleTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jToolBar_CurrentCycleTime.setMinimumSize(new java.awt.Dimension(95, 25));
        jToolBar_CurrentCycleTime.setPreferredSize(new java.awt.Dimension(230, 25));

        jLabel_CurCycleTime.setText("Cycle Time:    ");
        jLabel_CurCycleTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jLabel_CurCycleTime.setMinimumSize(new java.awt.Dimension(89, 25));
        jLabel_CurCycleTime.setPreferredSize(new java.awt.Dimension(89, 25));
        jToolBar_CurrentCycleTime.add(jLabel_CurCycleTime);

        jFormattedTextField_CycleTime.setText("mm:ss:SS");
        jFormattedTextField_CycleTime.setMaximumSize(new java.awt.Dimension(230, 25));
        jFormattedTextField_CycleTime.setMinimumSize(new java.awt.Dimension(14, 25));
        jFormattedTextField_CycleTime.setPreferredSize(new java.awt.Dimension(98, 25));
        jToolBar_CurrentCycleTime.add(jFormattedTextField_CycleTime);

        jToolBar_AnimSpeedGroup.setFloatable(false);
        jToolBar_AnimSpeedGroup.setRollover(true);
        jToolBar_AnimSpeedGroup.setMaximumSize(new java.awt.Dimension(230, 25));
        jToolBar_AnimSpeedGroup.setMinimumSize(new java.awt.Dimension(150, 25));
        jToolBar_AnimSpeedGroup.setPreferredSize(new java.awt.Dimension(230, 25));

        jLabel_AnimSpeed.setText("Animation Speed");
        jToolBar_AnimSpeedGroup.add(jLabel_AnimSpeed);

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
        jToolBar_AnimSpeedGroup.add(jSlider_Animations);

        jToolBar_ModelInstances.setFloatable(false);
        jToolBar_ModelInstances.setRollover(true);

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
        jToolBar_ModelInstances.add(jComboBox_ModelInstances);

        jToolBar_Animations.setFloatable(false);
        jToolBar_Animations.setRollover(true);

        jComboBox_Animations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_Animations.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_Animations.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_Animations.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_Animations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_AnimationsActionPerformed(evt);
            }
        });
        jToolBar_Animations.add(jComboBox_Animations);

        jToolBar_MediaControls.setFloatable(false);
        jToolBar_MediaControls.setRollover(true);
        jToolBar_MediaControls.setMaximumSize(new java.awt.Dimension(230, 25));
        jToolBar_MediaControls.setMinimumSize(new java.awt.Dimension(121, 25));
        jToolBar_MediaControls.setPreferredSize(new java.awt.Dimension(230, 25));

        jButton_Play.setBackground(new java.awt.Color(0, 255, 0));
        jButton_Play.setText("PLAY");
        jButton_Play.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Play.setFocusable(false);
        jButton_Play.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Play.setMaximumSize(new java.awt.Dimension(35, 25));
        jButton_Play.setMinimumSize(new java.awt.Dimension(35, 25));
        jButton_Play.setPreferredSize(new java.awt.Dimension(100, 25));
        jButton_Play.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(0);
            }
        });
        jToolBar_MediaControls.add(jButton_Play);

        jButton_Pause.setBackground(new java.awt.Color(255, 255, 0));
        jButton_Pause.setText("PAUSE");
        jButton_Pause.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Pause.setFocusable(false);
        jButton_Pause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Pause.setMaximumSize(new java.awt.Dimension(42, 25));
        jButton_Pause.setMinimumSize(new java.awt.Dimension(42, 25));
        jButton_Pause.setPreferredSize(new java.awt.Dimension(100, 25));
        jButton_Pause.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(1);
            }
        });
        jToolBar_MediaControls.add(jButton_Pause);

        jButton_Stop.setBackground(new java.awt.Color(255, 0, 0));
        jButton_Stop.setText("STOP");
        jButton_Stop.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Stop.setFocusable(false);
        jButton_Stop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Stop.setMaximumSize(new java.awt.Dimension(36, 25));
        jButton_Stop.setMinimumSize(new java.awt.Dimension(36, 25));
        jButton_Stop.setPreferredSize(new java.awt.Dimension(100, 25));
        jButton_Stop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mediaFunction(2);
            }
        });
        jToolBar_MediaControls.add(jButton_Stop);

        jToolBar_Reload.setFloatable(false);
        jToolBar_Reload.setRollover(true);
        jToolBar_Reload.setMaximumSize(new java.awt.Dimension(86, 25));
        jToolBar_Reload.setMinimumSize(new java.awt.Dimension(25, 25));
        jToolBar_Reload.setPreferredSize(new java.awt.Dimension(86, 25));

        jButton_Reload.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jButton_Reload.setText("Reload PScene");
        jButton_Reload.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_Reload.setFocusable(false);
        jButton_Reload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_Reload.setMaximumSize(new java.awt.Dimension(220, 25));
        jButton_Reload.setMinimumSize(new java.awt.Dimension(25, 25));
        jButton_Reload.setPreferredSize(new java.awt.Dimension(220, 25));
        jButton_Reload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_Reload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ReloadActionPerformed(evt);
            }
        });
        jToolBar_Reload.add(jButton_Reload);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jToolBar_CurrentCycleTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_AnimSpeedGroup, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_ElapsedTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_ModelInstances, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_Animations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_MediaControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jToolBar_Reload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar_ElapsedTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_CurrentCycleTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_AnimSpeedGroup, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_ModelInstances, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Animations, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_MediaControls, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar_Reload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void jComboBox_AnimationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_AnimationsActionPerformed
if (jComboBox_Animations.isEnabled()) {
        imi.scene.polygonmodel.PPolygonModelInstance instance = ((imi.scene.polygonmodel.PPolygonModelInstance)jComboBox_ModelInstances.getSelectedItem());
        imi.scene.PNode node = ((imi.scene.PNode)instance.findChild("skeletonRoot"));
        imi.scene.polygonmodel.parts.skinned.SkeletonNode skeleton = ((imi.scene.polygonmodel.parts.skinned.SkeletonNode)node.getParent());
        skeleton.getAnimationState().setPauseAnimation(false);
        skeleton.transitionTo(jComboBox_Animations.getSelectedItem().toString());
    }
}//GEN-LAST:event_jComboBox_AnimationsActionPerformed

private void jButton_ReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ReloadActionPerformed
    reloadModelInstances();
    reloadSelectedModelAnimations();
}//GEN-LAST:event_jButton_ReloadActionPerformed

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
    private javax.swing.JToolBar jToolBar_AnimSpeedGroup;
    private javax.swing.JToolBar jToolBar_Animations;
    private javax.swing.JToolBar jToolBar_CurrentCycleTime;
    private javax.swing.JToolBar jToolBar_ElapsedTime;
    private javax.swing.JToolBar jToolBar_MediaControls;
    private javax.swing.JToolBar jToolBar_ModelInstances;
    private javax.swing.JToolBar jToolBar_Reload;
    // End of variables declaration//GEN-END:variables

}
