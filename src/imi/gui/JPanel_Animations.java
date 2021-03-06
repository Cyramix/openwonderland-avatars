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

import imi.character.Character;
import imi.character.Manipulator;
import imi.scene.PScene;
import imi.utils.FileUtils;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author  Paul Viet Nguyen Truong (ptruong)
 */
public class JPanel_Animations extends javax.swing.JPanel {
////////////////////////////////////////////////////////////////////////////////
// Data Members
////////////////////////////////////////////////////////////////////////////////
    private WorldManager    worldManager    = null;
    private Character       character       = null;
    private PScene          pscene          = null;

    /** Timer */
    javax.swing.Timer       m_animTimer;
    /** Animation Data */
    private boolean         m_bStopped      = false;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    /**
     * Default constructor initializes all the GUI components and starts the update
     * timer
     */
    private JPanel_Animations(Builder builder) {
        this.worldManager   = builder.worldManager;
        this.character      = builder.character;
        this.pscene         = builder.character.getPScene();

        initComponents();
        resetPanel();
        initTimer();
    }

    public static class Builder {
        private WorldManager    worldManager    = null;
        private Character       character       = null;

        public Builder(WorldManager worldManager, Character character) {
            this.worldManager   = worldManager;
            this.character      = character;
        }

        public JPanel_Animations build() {
            return new JPanel_Animations(this);
        }
    }

    public void avatarCheck() {
        if (character == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character is null");
        }
        if (character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: character has no SkeletonNode");
        }
        if (!character.isInitialized()) {
            throw new IllegalArgumentException("SEVERE ERROR: character has not been initialized");
        }
    }

    public void managerCheck() {
        if (worldManager == null) {
            throw new IllegalArgumentException("SEVERE ERROR: worldManager is null");
        }
    }

    public void sceneCheck() {
        if (pscene == null) {
            throw new IllegalArgumentException("SEVERE ERROR: pscene is null");
        }
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
        sceneCheck();

        imi.scene.utils.traverser.InstanceSearchProcessor proc = new imi.scene.utils.traverser.InstanceSearchProcessor();
        proc.setProcessor();
        imi.scene.utils.traverser.TreeTraverser.breadthFirst(pscene, proc);
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
                imi.scene.SkeletonNode skeleton = ((imi.scene.SkeletonNode)node.getParent());

                if (skeleton.getAnimationGroup(0) != null) {

                    int iNumAnimations = skeleton.getAnimationGroup(0).getCycleCount();
                    int iCurrentAnim = skeleton.getAnimationState(0).getCurrentCycle();
                    String[] szAnimations = new String[iNumAnimations];

                    for (int i = 0; i < szAnimations.length; i++) {
                        szAnimations[i] = skeleton.getAnimationGroup(0).getCycle(i).getName();
                    }

                    jComboBox_BodyAnimations.setEnabled(true);
                    jSlider_Animations.setEnabled(true);
                    jComboBox_BodyAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_BodyAnimations.setSelectedIndex(iCurrentAnim);
                } else {
                    String[] szAnimations = new String[1];
                    szAnimations[0] = "No Animations";
                    jComboBox_BodyAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_BodyAnimations.setEnabled(false);
                    jComboBox_BodyAnimations.setSelectedIndex(0);
                    jSlider_Animations.setEnabled(false);
                }

                if (skeleton.getAnimationComponent().getGroupCount() > 1) {
                    if (skeleton.getAnimationGroup(1) != null) {

                        int iNumAnimations = skeleton.getAnimationGroup(1).getCycleCount();
                        int iCurrentAnim = skeleton.getAnimationState(1).getCurrentCycle();
                        String[] szAnimations = new String[iNumAnimations];

                        for (int i = 0; i < szAnimations.length; i++) {
                            szAnimations[i] = skeleton.getAnimationGroup(1).getCycle(i).getName();
                        }

                        jComboBox_FacialAnimations.setEnabled(true);
                        jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                        jComboBox_FacialAnimations.setSelectedIndex(iCurrentAnim);
                    } else {
                        String[] szAnimations = new String[1];
                        szAnimations[0] = "No Animations";
                        jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                        jComboBox_FacialAnimations.setEnabled(false);
                        jComboBox_FacialAnimations.setSelectedIndex(0);
                    }
                } else {
                    String[] szAnimations = new String[1];
                    szAnimations[0] = "No Animations";
                    jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_FacialAnimations.setEnabled(false);
                    jComboBox_FacialAnimations.setSelectedIndex(0);
                }

                if (skeleton.getAnimationGroup(0) == null && skeleton.getAnimationGroup(1) == null) {

                    String[] szAnimations = new String[1];
                    szAnimations[0] = "No Animations";
                    jComboBox_BodyAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_BodyAnimations.setEnabled(false);
                    jComboBox_BodyAnimations.setSelectedIndex(0);
                    jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
                    jComboBox_FacialAnimations.setEnabled(false);
                    jComboBox_FacialAnimations.setSelectedIndex(0);
                    jSlider_Animations.setEnabled(false);
                }
            }
        } else {
            String[] szAnimations = new String[1];
            szAnimations[0] = "No Animations";
            jComboBox_BodyAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
            jComboBox_BodyAnimations.setEnabled(false);
            jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(szAnimations));
            jComboBox_FacialAnimations.setEnabled(false);
            jSlider_Animations.setEnabled(false);
            jComboBox_BodyAnimations.setSelectedIndex(0);
            jComboBox_FacialAnimations.setSelectedIndex(0);
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
                imi.scene.SkeletonNode skeleton = ((imi.scene.SkeletonNode)node.getParent());
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
                imi.scene.SkeletonNode skeleton = ((imi.scene.SkeletonNode)node.getParent());
                if (skeleton.getAnimationGroup() != null) {
                    jFormattedTextField_Time.setEnabled(true);
                    jFormattedTextField_CycleTime.setEnabled(true);

                    // Determine the what are the animation indices
                    float cycleTimeMil, cycleTimeSec, cycleTimeMin, elapsedTimeMil, elapsedTimeSec, elapsedTimeMin;
                    elapsedTimeMil = elapsedTimeSec = elapsedTimeMin = 0.0f;

                    // Get Time in the current animation cycle
                    cycleTimeSec = skeleton.getAnimationState().getCurrentCycleTime();
                    cycleTimeMil = cycleTimeSec * 1000F;
                    cycleTimeMin = cycleTimeSec / 60F;

                    if(!m_bStopped) {
                        elapsedTimeSec = skeleton.getAnimationState().getCurrentCycleTime();
                        elapsedTimeMil = elapsedTimeSec * 1000F;
                        elapsedTimeMin = elapsedTimeSec / 60F;
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
                imi.scene.SkeletonNode skeleton = ((imi.scene.SkeletonNode)node.getParent());
                
                switch(button)
                {
                    case 0:
                    {
                        if(m_bStopped) {
                            skeleton.getAnimationState().setCurrentCycle(jComboBox_BodyAnimations.getSelectedIndex());
                            skeleton.getAnimationState().setCurrentCycleTime(0);
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

    /**
     * Uses the scene essentials file I/O to let the user select a facial or body
     * animation and then subsequently load that selected file into the appropriate
     * animation group in the avatar.  Only available if an avatar is loaded
     * @param type - integer 1 for body animations and 2 for facial animations
     */
    public void loadAnimations(int type) {
        FileFilter filter           = FileUtils.createFileFilter("*.dae", "Collada File (*.dae)");
        JFileChooser fileChooser    = FileUtils.getFileChooser();
        switch(type)
        {
            case 1:
            {
                FileUtils.setFileChooserProperty(fileChooser, filter, "Load Body Animation File", new File("./assets/models/collada/Animations"));
                break;
            }
            case 2:
            {
                FileUtils.setFileChooserProperty(fileChooser, filter, "Load Facial Animation File", new File("./assets/models/collada/Animations"));
                break;
            }
        }

        int retVal = fileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File animFile   = fileChooser.getSelectedFile();
            String relPath  = FileUtils.getRelativePath(new File(System.getProperty("user.dir")), animFile);
            switch(type)
            {
                case 1:
                {
                    Manipulator.addBodyAnimation(character, relPath);
                    break;
                }
                case 2:
                {
                    Manipulator.addFacialAnimation(character, relPath);
                    break;
                }
            }
        }
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
        pscene = pScene;
    }

    public void startTimer() {
        m_animTimer.start();
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
        jComboBox_BodyAnimations = new javax.swing.JComboBox();
        jComboBox_FacialAnimations = new javax.swing.JComboBox();
        jButton_Play = new javax.swing.JButton();
        jButton_Pause = new javax.swing.JButton();
        jButton_Stop = new javax.swing.JButton();
        jButton_Reload = new javax.swing.JButton();
        jButton_AddBodyAnim = new javax.swing.JButton();
        jButton_AddFaceAnim = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Model Animations", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        setMaximumSize(new java.awt.Dimension(266, 250));
        setMinimumSize(new java.awt.Dimension(266, 250));
        setPreferredSize(new java.awt.Dimension(266, 300));
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 160;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox_ModelInstances, gridBagConstraints);

        jComboBox_BodyAnimations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_BodyAnimations.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_BodyAnimations.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_BodyAnimations.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_BodyAnimations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_BodyAnimationsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 160;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jComboBox_BodyAnimations, gridBagConstraints);

        jComboBox_FacialAnimations.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox_FacialAnimations.setMaximumSize(new java.awt.Dimension(230, 25));
        jComboBox_FacialAnimations.setMinimumSize(new java.awt.Dimension(85, 25));
        jComboBox_FacialAnimations.setPreferredSize(new java.awt.Dimension(85, 25));
        jComboBox_FacialAnimations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_FacialAnimationsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jComboBox_FacialAnimations, gridBagConstraints);

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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 6;
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
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 98;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jButton_Reload, gridBagConstraints);

        jButton_AddBodyAnim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAnimations(0);
                reloadSelectedModelAnimations();
            }
        });
        jButton_AddBodyAnim.setText("Add Body Animation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton_AddBodyAnim, gridBagConstraints);

        jButton_AddFaceAnim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadAnimations(1);
                reloadSelectedModelAnimations();
            }
        });
        jButton_AddFaceAnim.setText("Add Facial Animation");
        jButton_AddFaceAnim.setMaximumSize(new java.awt.Dimension(138, 29));
        jButton_AddFaceAnim.setMinimumSize(new java.awt.Dimension(138, 29));
        jButton_AddFaceAnim.setPreferredSize(new java.awt.Dimension(138, 29));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jButton_AddFaceAnim, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Switches to the user selected body animation from the combobox containing
     * animations.
     * @param evt
     */
    private void jComboBox_BodyAnimationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_BodyAnimationsActionPerformed
        if (jComboBox_BodyAnimations.isEnabled()) {
            if (jComboBox_BodyAnimations.getSelectedIndex() < 0)
                jComboBox_BodyAnimations.setSelectedIndex(0);
            Manipulator.transitionToBodyAnimation(character, jComboBox_BodyAnimations.getSelectedIndex(), false);
        }
}//GEN-LAST:event_jComboBox_BodyAnimationsActionPerformed

    /**
     * Switches to the user selected body animation from the combobox containing
     * facial animations
     * @param evt
     */
    private void jComboBox_FacialAnimationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_FacialAnimationsActionPerformed
        if (jComboBox_FacialAnimations.isEnabled()) {
            if (jComboBox_FacialAnimations.getSelectedIndex() < 0)
                jComboBox_FacialAnimations.setSelectedIndex(0);
            Manipulator.playFacialAnimation(character, jComboBox_FacialAnimations.getSelectedIndex(), 0.2f, 1.5f);
        }
    }//GEN-LAST:event_jComboBox_FacialAnimationsActionPerformed

    /**
     * Change the speed of the animation
     * @param e (ChangeEvent)
     */
    private void jSlider_AnimationsStateChanged(javax.swing.event.ChangeEvent e) {
        if (jSlider_Animations.isEnabled()) {
            float fAnimSpeed = (jSlider_Animations.getValue() * 0.10f);
            Manipulator.setBodyAnimationSpeed(character, fAnimSpeed);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_AddBodyAnim;
    private javax.swing.JButton jButton_AddFaceAnim;
    private javax.swing.JButton jButton_Pause;
    private javax.swing.JButton jButton_Play;
    private javax.swing.JButton jButton_Reload;
    private javax.swing.JButton jButton_Stop;
    private javax.swing.JComboBox jComboBox_BodyAnimations;
    private javax.swing.JComboBox jComboBox_FacialAnimations;
    private javax.swing.JComboBox jComboBox_ModelInstances;
    private javax.swing.JFormattedTextField jFormattedTextField_CycleTime;
    private javax.swing.JFormattedTextField jFormattedTextField_Time;
    private javax.swing.JLabel jLabel_AnimSpeed;
    private javax.swing.JLabel jLabel_CurCycleTime;
    private javax.swing.JLabel jLabel_ElapsedTime;
    private javax.swing.JSlider jSlider_Animations;
    // End of variables declaration//GEN-END:variables

}
