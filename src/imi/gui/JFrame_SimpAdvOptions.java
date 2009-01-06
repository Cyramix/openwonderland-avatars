/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JFrame_SimpAdvOptions.java
 *
 * Created on Jan 5, 2009, 4:45:17 PM
 */

package imi.gui;

import com.jme.math.Vector3f;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ptruong
 */
public class JFrame_SimpAdvOptions extends javax.swing.JFrame {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    private Map<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>   m_skeleton;
    private SceneEssentials                                 m_sceneData;
    private NumberFormat                                    m_format;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    /** Creates new form JFrame_SimpAdvOptions */
    public JFrame_SimpAdvOptions() {
        initComponents();
    }

    public JFrame_SimpAdvOptions(SceneEssentials sceneinfo) {
        m_sceneData = sceneinfo;
        initComponents();
        createJointCatalog();
    }

    private synchronized void adjustEyes(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        SkeletonNode        skelnode        = m_sceneData.getAvatar().getSkeleton();
        String              formattedNumber = null;
        if (skelnode == null) { return; }
        SkinnedMeshJoint[]  eyes            = m_skeleton.get(GUI_Enums.m_bodyPart.Eyes);
        Vector3f            ladjust         = new Vector3f();
        Vector3f            radjust         = new Vector3f();
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case lefteyeHPos:
            {
                for (int i = 0; i < 5; i++) {
                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
                    curr.x = start.x;
                    if (eyes[i].getName().contains("leftEye")) {
                        ladjust.x = (actualval / 15);
                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
                    }
                    else {
                        ladjust.x = (actualval / 4);
                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
                    }
                }

                float y = scale.y += actualval * 4;
                formattedNumber = m_format.format(y);
                scale.x = Float.valueOf(formattedNumber);
                eyes[1].getLocalModifierMatrix().setScale(scale);

                break;
            }
            case righteyeHPos:
            {
                for (int i = 5; i < eyes.length; i++) {
                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
                    curr.x = start.x;
                    if (eyes[i].getName().contains("rightEye")) {
                        radjust.x = (actualval / 15);
                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
                    }
                    else {
                        radjust.x = (actualval / 4);
                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
                    }
                }

                float y = scale.y += actualval * 4;
                formattedNumber = m_format.format(y);
                scale.x = Float.valueOf(formattedNumber);
                eyes[6].getLocalModifierMatrix().setScale(scale);

                break;
            }
            case lefteyeVPos:
            {
                for (int i = 0; i < 5; i++) {
                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
                    curr.y = start.y;
                    if (eyes[i].getName().contains("leftEye")) {
                        ladjust.y = (actualval / 15);
                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
                    }
                    else {
                        ladjust.y = (actualval / 4);
                        eyes[i].getBindPose().setTranslation(curr.add(ladjust));
                    }
                }

                break;
            }
            case righteyeVPos:
            {
                for (int i = 5; i < eyes.length; i++) {
                    Vector3f start = eyes[i].getTransform().getLocalMatrix(false).getTranslation();
                    Vector3f curr  = new Vector3f(eyes[i].getBindPose().getTranslation());
                    curr.y = start.y;
                    if (eyes[i].getName().contains("rightEye")) {
                        radjust.y = (actualval / 15);
                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
                    }
                    else {
                        radjust.y = (actualval / 4);
                        eyes[i].getBindPose().setTranslation(curr.add(radjust));
                    }
                }

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
                ladjust.x = -mod;
                skelnode.displaceJoint(eyes[3].getName(), ladjust);
                System.out.println(ladjust.x);

                float y = scale.y += actualval * 3;
                formattedNumber = m_format.format(y);
                scale.x = Float.valueOf(formattedNumber);
                eyes[0].getLocalModifierMatrix().setScale(scale);
                eyes[1].getLocalModifierMatrix().setScale(scale);

                break;
            }
            case righteyeWidth:
            {
                radjust.x = mod;
                skelnode.displaceJoint(eyes[8].getName(), radjust);

                float y = scale.y += actualval * 3;
                formattedNumber = m_format.format(y);
                scale.x = Float.valueOf(formattedNumber);
                eyes[5].getLocalModifierMatrix().setScale(scale);
                eyes[6].getLocalModifierMatrix().setScale(scale);

                break;
            }
        }
    }

    private synchronized void adjustArms(GUI_Enums.m_sliderControl type, float mod, float actualval) {

    }

    private synchronized void adjustLegs(GUI_Enums.m_sliderControl type, float mod, float actualval) {

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        HeadOptions = new imi.gui.JPanel_HeadOptions();
        ArmsNLegsOptions = new imi.gui.JPanel_SimpArmsLegsOptions();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.addTab("Head", HeadOptions);
        jTabbedPane1.addTab("Arms & Legs", ArmsNLegsOptions);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFrame_SimpAdvOptions().setVisible(true);
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////
// ACCESSORS
////////////////////////////////////////////////////////////////////////////////

    public SceneEssentials getSceneData() {
        return m_sceneData;
    }

    public Map<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]> getSkeletonJoints() {
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
    private imi.gui.JPanel_SimpArmsLegsOptions ArmsNLegsOptions;
    private imi.gui.JPanel_HeadOptions HeadOptions;
    private javax.swing.JTabbedPane jTabbedPane1;
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Hand, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Hand, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_LowerArm, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_LowerArm, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_UpperArm, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_UpperArm, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Shoulder, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Shoulder, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Torso, torso);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_UpperLeg, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_UpperLeg, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_LowerLeg, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_LowerLeg, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Foot, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Foot, right);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Head, head);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Neck, neck);
    }

    public void catalogEyes() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szEyes     = new String[] { "leftEye",     "leftEyeLid",       "leftInnerBrow",    "leftOuterBrow",    "leftCheek",
                                             "rightEye",    "rightEyeLid",      "rightInnerBrow",   "rightOuterBrow",   "rightCheek" };

        SkinnedMeshJoint[]  eyes     = new SkinnedMeshJoint[szEyes.length];
        int                 iSize    = szEyes.length;

        for (int i = 0; i < iSize; i++) {
            eyes[i]     = (SkinnedMeshJoint) skeleton.findChild(szEyes[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Eyes, eyes);
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
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Lips, lips);
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

    public void parseModification(GUI_Enums.m_sliderControl control, float mod, float actualval) {
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
            case leftarmLength:
            {
                adjustArms(control, mod, actualval);
                break;
            }
            case leftarmScale:
            {
                adjustArms(control, mod, actualval);
                break;
            }
            case rightarmLength:
            {
                adjustArms(control, mod, actualval);
                break;
            }
            case rightarmScale:
            {
                adjustArms(control, mod, actualval);
                break;
            }
            case leftlegLength:
            {
                adjustLegs(control, mod, actualval);
                break;
            }
            case leftlegScale:
            {
                adjustLegs(control, mod, actualval);
                break;
            }
            case rightlegLength:
            {
                adjustLegs(control, mod, actualval);
                break;
            }
            case rightlegScale:
            {
                adjustLegs(control, mod, actualval);
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
