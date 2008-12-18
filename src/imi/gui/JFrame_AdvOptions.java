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
 * JFrame_AdvOptions.java
 *
 * Created on Dec 17, 2008, 11:37:01 AM
 */

package imi.gui;

import com.jme.math.Vector3f;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.util.HashMap;
import java.util.Map;
import org.jdesktop.mtgame.processor.EyeSelectionProcessor;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class JFrame_AdvOptions extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private Map<m_bodyPart, SkinnedMeshJoint[]> m_skeleton;
    private SceneEssentials                     m_sceneData;
    private float                               m_baseLen = 10.0f;
    private float                               m_baseScale = 20.0f;
////////////////////////////////////////////////////////////////////////////////
// Enumerations for the skeleton and body
////////////////////////////////////////////////////////////////////////////////
    public  enum m_bodyPart { Left_UpperLeg, Left_LowerLeg, Left_Foot, Left_UpperArm, Left_LowerArm, Left_Hand, Left_Shoulder,
                              Right_UpperLeg, Right_LowerLeg, Right_Foot, Right_UpperArm, Right_LowerArm, Right_Hand, Right_Shoulder,
                              Head, Torso, Neck, Eyes, Lips };

    public enum m_sliderControl { lefteyeHPos, lefteyeSize, lefteyeVPos, lefteyeWidth, righteyeHPos, righteyeSize, righteyeVPos, righteyeWidth,
                                  lowerlipSize, upperlipSize, mouthWidth, lefthandLength, lefthandThickness, leftlowerarmLength, leftlowerarmThickness,
                                  leftupperarmLength, leftupperarmThickness, righthandLength, righthandThickness, rightlowerarmLength, rightlowerarmThickness,
                                  rightupperarmLength, rightupperarmThickness, leftfootLength, leftfootThickness, leftlowerlegLength, leftlowerlegThickness,
                                  leftupperlegLength, leftupperlegThickness, rightfootLength, rightfootThickness, rightlowerlegLength, rightlowerlegThickness,
                                  rightupperlegLength, rightupperlegThickness, headDepth, headHeight, headWidth};

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form JFrame_AdvOptions */
    public JFrame_AdvOptions() {
        initComponents();
        HeadOptions.setParentFrame(this);
    }

    public JFrame_AdvOptions(SceneEssentials scene) {
        m_sceneData = scene;
        initComponents();
        createJointCatalog();
    }


    private void adjustEyes(m_sliderControl type, float mod, float actualval) {
        SkeletonNode skelnode   = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }
        SkinnedMeshJoint[] eyes = m_skeleton.get(m_bodyPart.Eyes);

        switch(type)
        {
            case lefteyeHPos:
            {
                break;
            }
            case righteyeHPos:
            {
                break;
            }
            case lefteyeVPos:
            {
                break;
            }
            case righteyeVPos:
            {
                break;
            }
            case lefteyeSize:
            {
                break;
            }
            case righteyeSize:
            {
                break;
            }
            case lefteyeWidth:
            {
                break;
            }
            case righteyeWidth:
            {

            }
        }
    }

    private void adjustHands(m_sliderControl type, float mod, float actualval) {
        SkeletonNode skelnode           = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[] lefthand     = m_skeleton.get(m_bodyPart.Left_Hand);
        SkinnedMeshJoint[] righthand    = m_skeleton.get(m_bodyPart.Right_Hand);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);

        switch(type)
        {
            case lefthandLength:
            {
                skelnode.displace(lefthand[0].getName(), ladjust);
                break;
            }
            case righthandLength:
            {
                skelnode.displace(righthand[0].getName(), radjust);
                break;
            }
            case lefthandThickness:
            {
                for (int i = 0; i < lefthand.length; i++) {
                    ladjust = new Vector3f(lefthand[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    lefthand[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case righthandThickness:
            {
                for (int i = 0; i < righthand.length; i++) {
                    radjust = new Vector3f(righthand[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    righthand[i].getLocalModifierMatrix().setScale(radjust);
                }

                break;
            }
        }
    }

    private void adjustForearms(m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftforearm     = m_skeleton.get(m_bodyPart.Left_LowerArm);
        SkinnedMeshJoint[]  rightforearm    = m_skeleton.get(m_bodyPart.Right_LowerArm);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);

        switch(type)
        {
            case leftlowerarmLength:
            {
                for (int i = 0; i < leftforearm.length; i++) {
                    leftforearm[i].getLocalModifierMatrix().getScale(scale);
                    scale.y += mod * 5;
                    leftforearm[i].getLocalModifierMatrix().setScale(scale);
                }
                skelnode.displace(leftforearm[1].getName(), ladjust);
                break;
            }
            case rightlowerarmLength:
            {
                for (int i = 0; i < rightforearm.length; i++) {
                    rightforearm[i].getLocalModifierMatrix().getScale(scale);
                    scale.y += mod * 5;
                    rightforearm[i].getLocalModifierMatrix().setScale(scale);
                }
                skelnode.displace(rightforearm[1].getName(), radjust);
                break;
            }
            case leftlowerarmThickness:
            {
                for (int i = 0; i < leftforearm.length; i++) {
                    ladjust = new Vector3f(leftforearm[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    leftforearm[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightlowerarmThickness:
            {
                for (int i = 0; i < rightforearm.length; i++) {
                    radjust = new Vector3f(rightforearm[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    rightforearm[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private void adjustUpperarms(m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftupperarm    = m_skeleton.get(m_bodyPart.Left_UpperArm);
        SkinnedMeshJoint[]  rightupperarm   = m_skeleton.get(m_bodyPart.Right_UpperArm);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);

        switch(type)
        {
            case leftupperarmLength:
            {
                for (int i = 0; i < leftupperarm.length; i++) {
                    leftupperarm[i].getLocalModifierMatrix().getScale(scale);
                    scale.y += mod * 5;
                    leftupperarm[i].getLocalModifierMatrix().setScale(scale);
                }
                skelnode.displace(leftupperarm[1].getName(), ladjust);
                break;
            }
            case rightupperarmLength:
            {
                for (int i = 0; i < rightupperarm.length; i++) {
                    rightupperarm[i].getLocalModifierMatrix().getScale(scale);
                    scale.y += mod * 5;
                    rightupperarm[i].getLocalModifierMatrix().setScale(scale);
                }
                skelnode.displace(rightupperarm[1].getName(), radjust);
                break;
            }
            case leftupperarmThickness:
            {
                for (int i = 0; i < leftupperarm.length; i++) {
                    ladjust = new Vector3f(leftupperarm[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    leftupperarm[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightupperarmThickness:
            {
                for (int i = 0; i < rightupperarm.length; i++) {
                    radjust = new Vector3f(rightupperarm[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    rightupperarm[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private void adjustFeet(m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftfoot    = m_skeleton.get(m_bodyPart.Left_Foot);
        SkinnedMeshJoint[]  rightfoot   = m_skeleton.get(m_bodyPart.Right_Foot);
        Vector3f            ladjust     = new Vector3f(0.0f, 0.0f, mod);
        Vector3f            radjust     = new Vector3f(0.0f, 0.0f, mod);

        switch(type)
        {
            case leftfootLength:
            {
                skelnode.displace(leftfoot[1].getName(), ladjust);
                break;
            }
            case rightfootLength:
            {
                skelnode.displace(rightfoot[1].getName(), radjust);
                break;
            }
            case leftfootThickness:
            {
                for (int i = 0; i < leftfoot.length; i++) {
                    ladjust = new Vector3f(leftfoot[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    leftfoot[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightfootThickness:
            {
                for (int i = 0; i < rightfoot.length; i++) {
                    radjust = new Vector3f(rightfoot[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    rightfoot[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private void adjustCalves(m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftlowerleg    = m_skeleton.get(m_bodyPart.Left_LowerLeg);
        SkinnedMeshJoint[]  rightlowerleg   = m_skeleton.get(m_bodyPart.Right_LowerLeg);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);

        switch(type)
        {
            case leftlowerlegLength:
            {
                skelnode.displace(leftlowerleg[1].getName(), ladjust);
                break;
            }
            case rightlowerlegLength:
            {
                skelnode.displace(rightlowerleg[1].getName(), radjust);
                break;
            }
            case leftlowerlegThickness:
            {
                for (int i = 0; i < leftlowerleg.length; i++) {
                    ladjust = new Vector3f(leftlowerleg[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    leftlowerleg[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightlowerlegThickness:
            {
                for (int i = 0; i < rightlowerleg.length; i++) {
                    radjust = new Vector3f(rightlowerleg[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    rightlowerleg[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private void adjustThighs(m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftupperleg    = m_skeleton.get(m_bodyPart.Left_UpperLeg);
        SkinnedMeshJoint[]  rightupperleg   = m_skeleton.get(m_bodyPart.Right_UpperLeg);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);

        switch(type)
        {
            case leftupperlegLength:
            {
                skelnode.displace(leftupperleg[1].getName(), ladjust);
                break;
            }
            case rightupperlegLength:
            {
                skelnode.displace(rightupperleg[1].getName(), radjust);
                break;
            }
            case leftupperlegThickness:
            {
                for (int i = 0; i < leftupperleg.length; i++) {
                    ladjust = new Vector3f(leftupperleg[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += mod;
                    ladjust.z = 1.0f;   ladjust.z += mod;
                    leftupperleg[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightupperlegThickness:
            {
                for (int i = 0; i < rightupperleg.length; i++) {
                    radjust = new Vector3f(rightupperleg[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += mod;
                    radjust.z = 1.0f;   radjust.z += mod;
                    rightupperleg[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private void adjustChest(m_sliderControl type, float mod, float actualval) {

    }

    private void adjustStomach(m_sliderControl type, float mod, float actualval) {

    }

    private void adjustGluts(m_sliderControl type, float mod, float actualval) {

    }

    private void adjustBody(m_sliderControl type, float mod, float actualval) {

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane_Options = new javax.swing.JTabbedPane();
        HeadOptions = new imi.gui.JPanel_HeadOptions(this);
        ArmsHandsOptions = new imi.gui.JPanel_ArmsHandsOption(this);
        LegsFeetOptions = new imi.gui.JPanel_LegsFeetOption(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTabbedPane_Options.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane_Options.setMinimumSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.setPreferredSize(new java.awt.Dimension(300, 650));
        jTabbedPane_Options.addTab("Head", HeadOptions);
        jTabbedPane_Options.addTab("Arms/Hands", ArmsHandsOptions);
        jTabbedPane_Options.addTab("Legs/Feet", LegsFeetOptions);

        getContentPane().add(jTabbedPane_Options, new java.awt.GridBagConstraints());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_AdvOptions().setVisible(true);
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
// ACCESSORS
////////////////////////////////////////////////////////////////////////////////

    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

    public Map<m_bodyPart, SkinnedMeshJoint[]> getSkeletonJoints() {
        return m_skeleton;
    }

    public SkinnedMeshJoint[] getSkeletonJointsBySection(int section) {
        return m_skeleton.get(section);
    }

////////////////////////////////////////////////////////////////////////////////
// MUTATORS
////////////////////////////////////////////////////////////////////////////////

    public void setSceneData(SceneEssentials scene) {
        m_sceneData = scene;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private imi.gui.JPanel_ArmsHandsOption ArmsHandsOptions;
    private imi.gui.JPanel_HeadOptions HeadOptions;
    private imi.gui.JPanel_LegsFeetOption LegsFeetOptions;
    private javax.swing.JTabbedPane jTabbedPane_Options;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void catalogHands() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftHand = new String[] { "leftHand",    "leftHandThumb1",   "leftHandThumb2",   "leftHandThumb3",   "leftHandThumb4",
                                             "leftPalm",    "leftHandIndex1",   "leftHandIndex2",   "leftHandIndex3",   "leftHandIndex4",
                                                            "leftHandMiddle1",  "leftHandMiddle2",  "leftHandMiddle3",  "leftHandMiddle4",
                                                            "leftHandRing1",    "leftHandRing2",    "leftHandRing3",    "leftHandRing4",
                                                            "leftHandPinky1",   "leftHandPinky2",   "leftHandPinky3",   "leftHandPinky4" };
        String[] szRightHand = new String[] { "rightHand",  "rightHandThumb1",  "rightHandThumb2",  "rightHandThumb3",  "rightHandThumb4",
                                              "rightPalm",  "rightHandIndex1",  "rightHandIndex2",  "rightHandIndex3",  "rightHandIndex4",
                                                            "rightHandMiddle1", "rightHandMiddle2", "rightHandMiddle3", "rightHandMiddle4",
                                                            "rightHandRing1",   "rightHandRing2",   "rightHandRing3",   "rightHandRing4",
                                                            "rightHandPinky1",  "rightHandPinky2",  "rightHandPinky3",  "rightHandPinky4"};

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftHand.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightHand.length];
        int                 iSize    = szLeftHand.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftHand[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightHand[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_Hand, left);
        m_skeleton.put(m_bodyPart.Right_Hand, right);
    }

    public void catalogLowerArms() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftLowerArm     = new String[] { "leftForeArm",     "leftForeArmRoll" };
        String[] szRightLowerArm    = new String[] { "rightForeArm",    "rightForeArmRoll"};

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftLowerArm.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightLowerArm.length];
        int                 iSize    = szLeftLowerArm.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftLowerArm[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightLowerArm[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_LowerArm, left);
        m_skeleton.put(m_bodyPart.Right_LowerArm, right);
    }

    public void catalogUpperArms() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftUpperArm     = new String[] { "leftArm",     "leftArmRoll" };
        String[] szRightUpperArm    = new String[] { "rightArm",    "rightArmRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftUpperArm.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightUpperArm.length];
        int                 iSize    = szLeftUpperArm.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftUpperArm[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightUpperArm[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_UpperArm, left);
        m_skeleton.put(m_bodyPart.Right_UpperArm, right);
    }

    public void catalogShoulders() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftShoulder     = new String[] { "leftShoulder" };
        String[] szRightShoulder    = new String[] { "rightShoulder" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftShoulder.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightShoulder.length];
        int                 iSize    = szLeftShoulder.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftShoulder[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightShoulder[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_Shoulder, left);
        m_skeleton.put(m_bodyPart.Right_Shoulder, right);
    }

    public void catalogTorso() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szTorso     = new String[] { "Spine", "Spine1", "Spine2" };

        SkinnedMeshJoint[]  torso     = new SkinnedMeshJoint[szTorso.length];
        int                 iSize    = szTorso.length;

        for (int i = 0; i < iSize; i++) {
            torso[i]     = (SkinnedMeshJoint) skeleton.findChild(szTorso[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Torso, torso);
    }

    public void catalogUpperLegs() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftUpperLeg     = new String[] { "leftUpLeg",   "leftUpLegRoll" };
        String[] szRightUpperLeg    = new String[] { "rightUpLeg",  "rightUpLegRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftUpperLeg.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightUpperLeg.length];
        int                 iSize    = szLeftUpperLeg.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftUpperLeg[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightUpperLeg[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_UpperLeg, left);
        m_skeleton.put(m_bodyPart.Right_UpperLeg, right);
    }

    public void catalogLowerLegs() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftLowerLeg     = new String[] { "leftLeg",     "leftLegRoll" };
        String[] szRightLowerLeg    = new String[] { "rightLeg",    "rightLegRoll" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftLowerLeg.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightLowerLeg.length];
        int                 iSize    = szLeftLowerLeg.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftLowerLeg[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightLowerLeg[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_LowerLeg, left);
        m_skeleton.put(m_bodyPart.Right_LowerLeg, right);
    }

    public void catalogFeet() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftFoot     = new String[] { "leftFoot",    "leftFootBall" };
        String[] szRightFoot    = new String[] { "rightFoot",   "rightFootBall" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftFoot.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightFoot.length];
        int                 iSize    = szLeftFoot.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftFoot[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightFoot[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Left_Foot, left);
        m_skeleton.put(m_bodyPart.Right_Foot, right);
    }

    public void catalogHead() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szHead     = new String[] { "Head",            "Jaw",              "Tongue",           "Tongue1",
                                             "leftLowerLip",    "rightLowerLip",    "leftInnerBrow",    "leftEyeLid",
                                             "leftOuterBrow",   "leftCheek",        "leftUpperLip",     "leftOuterLip",
                                             "rightInnerBrow",  "rightOuterBrow",   "rightCheek",       "rightOuterLip",
                                             "rightUpperLip",   "rightEyeLid",      "leftEye",          "rightEye"};

        SkinnedMeshJoint[]  head     = new SkinnedMeshJoint[szHead.length];
        int                 iSize    = szHead.length;

        for (int i = 0; i < iSize; i++) {
            head[i]     = (SkinnedMeshJoint) skeleton.findChild(szHead[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Head, head);
    }

    public void catalogNeck() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szNeck     = new String[] { "Neck" };

        SkinnedMeshJoint[]  neck     = new SkinnedMeshJoint[szNeck.length];
        int                 iSize    = szNeck.length;

        for (int i = 0; i < iSize; i++) {
            neck[i]     = (SkinnedMeshJoint) skeleton.findChild(szNeck[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Neck, neck);
    }

    public void catalogEyes() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szEyes     = new String[] { "leftInnerBrow",   "rightInnerBrow",  "leftOuterBrow",    "rightOuterBrow",
                                             "leftEyeLid",      "rightEyeLid",     "leftEye",          "rightEye",
                                             "leftCheek",       "rightCheek" };

        SkinnedMeshJoint[]  eyes     = new SkinnedMeshJoint[szEyes.length];
        int                 iSize    = szEyes.length;

        for (int i = 0; i < iSize; i++) {
            eyes[i]     = (SkinnedMeshJoint) skeleton.findChild(szEyes[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Eyes, eyes);
    }

    public void catalogLips() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLips     = new String[] { "leftLowerLip",    "rightLowerLip",    "leftUpperLip",     "rightUpperLip",
                                             "leftCheek",       "rightCheek",       "leftOuterLip",     "rightOuterLip" };

        SkinnedMeshJoint[]  lips     = new SkinnedMeshJoint[szLips.length];
        int                 iSize    = szLips.length;

        for (int i = 0; i < iSize; i++) {
            lips[i]     = (SkinnedMeshJoint) skeleton.findChild(szLips[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(m_bodyPart.Lips, lips);
    }

    public void createJointCatalog() {
        if (m_sceneData.getAvatar() == null)
            return;

        catalogHands();
        catalogLowerArms();
        catalogUpperArms();
        catalogShoulders();
        catalogTorso();
        catalogUpperLegs();
        catalogLowerLegs();
        catalogFeet();
        catalogHead();
        catalogNeck();
        catalogEyes();
        catalogLips();
    }

    public void parseModification(m_sliderControl control, float mod, float actualval) {
        switch(control)
        {
            case lefteyeHPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeSize:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeVPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lefteyeWidth:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeHPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeSize:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeVPos:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case righteyeWidth:
            {
                adjustEyes(control, mod, actualval);
                break;
            }
            case lowerlipSize:
            {
                // TODO: adjustMouth(control, mod, actualval);
                break;
            }
            case upperlipSize:
            {
                // TODO: adjustMouth(control, mod, actualval);
                break;
            }
            case mouthWidth:
            {
                // TODO: adjustMouth(control, mod, actualval);
                break;
            }
            case lefthandLength:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case lefthandThickness:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case leftlowerarmLength:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case leftlowerarmThickness:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case leftupperarmLength:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case leftupperarmThickness:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case righthandLength:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case righthandThickness:
            {
                adjustHands(control, mod, actualval);
                break;
            }
            case rightlowerarmLength:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case rightlowerarmThickness:
            {
                adjustForearms(control, mod, actualval);
                break;
            }
            case rightupperarmLength:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case rightupperarmThickness:
            {
                adjustUpperarms(control, mod, actualval);
                break;
            }
            case leftfootLength:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case leftfootThickness:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case leftlowerlegLength:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case leftlowerlegThickness:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case leftupperlegLength:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case leftupperlegThickness:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case rightfootLength:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case rightfootThickness:
            {
                adjustFeet(control, mod, actualval);
                break;
            }
            case rightlowerlegLength:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case rightlowerlegThickness:
            {
                adjustCalves(control, mod, actualval);
                break;
            }
            case rightupperlegLength:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case rightupperlegThickness:
            {
                adjustThighs(control, mod, actualval);
                break;
            }
            case headDepth:
            {
                // TODO: adjustHead(control, mod, actualval);
                break;
            }
            case headHeight:
            {
                // TODO: adjustHead(control, mod, actualval);
                break;
            }
            case headWidth:
            {
                // TODO: adjustHead(control, mod, actualval);
                break;
            }
        }
    }
}
