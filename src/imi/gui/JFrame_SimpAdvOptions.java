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
        SkeletonNode    skelnode        = m_sceneData.getAvatar().getSkeleton();
        String          formattedNumber = null;
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftarm    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_Arm);
        SkinnedMeshJoint[]  rightarm   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_Arm);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            scale           = new Vector3f(1.0f, 1.0f, 1.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftarmLength:
            {
                skelnode.displaceJoint(leftarm[1].getName(), ladjust);
                skelnode.displaceJoint(leftarm[3].getName(), ladjust);
                skelnode.displaceJoint(leftarm[4].getName(), ladjust);
//                if (actualval <= 0.05f) {
//                    for (int i = 0; i < 6; i++) {
//                        float y = scale.y += actualval *3;
//                        formattedNumber = m_format.format(y);
//                        scale.y = Float.valueOf(formattedNumber);
//                        leftarm[i].getLocalModifierMatrix().setScale(scale);
//                    }
//                }

                break;
            }
            case rightarmLength:
            {
                skelnode.displaceJoint(rightarm[1].getName(), radjust);
                skelnode.displaceJoint(rightarm[3].getName(), radjust);
                skelnode.displaceJoint(rightarm[4].getName(), radjust);
//                if (actualval <= 0.05f) {
//                    for (int i = 0; i < 6; i++) {
//                        float y = scale.y += actualval *3;
//                        formattedNumber = m_format.format(y);
//                        scale.y = Float.valueOf(formattedNumber);
//                        rightarm[i].getLocalModifierMatrix().setScale(scale);
//                    }
//                }

                break;
            }
            case leftarmScale:
            {
                for (int i = 0; i < leftarm.length; i++) {
                    ladjust = new Vector3f(leftarm[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval;
                    ladjust.z = 1.0f;   ladjust.z += actualval;
                    leftarm[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightarmScale:
            {
                for (int i = 0; i < rightarm.length; i++) {
                    radjust = new Vector3f(rightarm[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval;
                    radjust.z = 1.0f;   radjust.z += actualval;
                    rightarm[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private synchronized void adjustLegs(GUI_Enums.m_sliderControl type, float mod, float actualval) {
        SkeletonNode    skelnode    = m_sceneData.getAvatar().getSkeleton();
        if (skelnode == null) { return; }

        SkinnedMeshJoint[]  leftleg    = m_skeleton.get(GUI_Enums.m_bodyPart.Left_Leg);
        SkinnedMeshJoint[]  rightleg   = m_skeleton.get(GUI_Enums.m_bodyPart.Right_Leg);
        Vector3f            ladjust         = new Vector3f(0.0f, mod, 0.0f);
        Vector3f            radjust         = new Vector3f(0.0f, mod, 0.0f);
        m_format                            = new DecimalFormat("0.00");

        switch(type)
        {
            case leftlegLength:
            {
                skelnode.displaceJoint(leftleg[0].getName(), ladjust);
                break;
            }
            case rightlegLength:
            {
                skelnode.displaceJoint(rightleg[0].getName(), radjust);
                break;
            }
            case leftlegScale:
            {
                for (int i = 0; i < leftleg.length; i++) {
                    ladjust = new Vector3f(leftleg[i].getLocalModifierMatrix().getScaleVector());
                    ladjust.x = 1.0f;   ladjust.x += actualval;
                    ladjust.z = 1.0f;   ladjust.z += actualval;
                    leftleg[i].getLocalModifierMatrix().setScale(ladjust);
                }
                break;
            }
            case rightlegScale:
            {
                for (int i = 0; i < rightleg.length; i++) {
                    radjust = new Vector3f(rightleg[i].getLocalModifierMatrix().getScaleVector());
                    radjust.x = 1.0f;   radjust.x += actualval;
                    radjust.z = 1.0f;   radjust.z += actualval;
                    rightleg[i].getLocalModifierMatrix().setScale(radjust);
                }
                break;
            }
        }
    }

    private synchronized void adjustBody(GUI_Enums.m_sliderControl type, float mod, float actualval) {

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
        HeadOptions = new imi.gui.JPanel_HeadOptions(this);
        ArmsNLegsOptions = new imi.gui.JPanel_SimpArmsLegsOptions(this);
        jPanel_SimpBodyOptions1 = new imi.gui.JPanel_SimpBodyOptions(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTabbedPane1.addTab("Head", HeadOptions);
        jTabbedPane1.addTab("Arms & Legs", ArmsNLegsOptions);
        jTabbedPane1.addTab("Body", jPanel_SimpBodyOptions1);

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
    private imi.gui.JPanel_SimpBodyOptions jPanel_SimpBodyOptions1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void catalogArms() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftArm     = new String[] { "leftArm",          "leftArmRoll",      "leftForeArm",      "leftForeArmRoll",  "leftHand",
                                                "leftPalm",         "leftHandThumb1",   "leftHandThumb2",   "leftHandThumb3",   "leftHandThumb4",
                                                "leftHandIndex1",   "leftHandIndex2",   "leftHandIndex3",   "leftHandIndex4",   "leftHandMiddle1",
                                                "leftHandMiddle2",  "leftHandMiddle3",  "leftHandMiddle4",  "leftHandRing1",    "leftHandRing2",
                                                "leftHandRing3",    "leftHandRing4",    "leftHandPinky1",   "leftHandPinky2",   "leftHandPinky3",
                                                "leftHandPinky4" };
        String[] szRightArm    = new String[] { "rightArm",         "rightArmRoll",     "rightForeArm",     "rightForeArmRoll", "rightHand",
                                                "rightPalm",        "rightHandThumb1",  "rightHandThumb2",  "rightHandThumb3",  "rightHandThumb4",
                                                "rightHandIndex1",  "rightHandIndex2",  "rightHandIndex3",  "rightHandIndex4",  "rightHandMiddle1",
                                                "rightHandMiddle2", "rightHandMiddle3", "rightHandMiddle4", "rightHandRing1",   "rightHandRing2",
                                                "rightHandRing3",   "rightHandRing4",   "rightHandPinky1",  "rightHandPinky2",  "rightHandPinky3",
                                                "rightHandPinky4" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftArm.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightArm.length];
        int                 iSize    = szLeftArm.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftArm[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightArm[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Arm, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Arm, right);
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

    public void catalogLegs() {
        if (m_sceneData.getAvatar() == null)
            return;

        SkeletonNode skeleton   = m_sceneData.getAvatar().getSkeleton();

        String[] szLeftLeg     = new String[] { "leftUpLeg",   "leftUpLegRoll",     "leftLeg",  "leftLegRoll",  "leftFoot",    "leftFootBall" };
        String[] szRightLeg    = new String[] { "rightUpLeg",  "rightUpLegRoll",    "rightLeg", "rightLegRoll", "rightFoot",   "rightFootBall" };

        SkinnedMeshJoint[]  left     = new SkinnedMeshJoint[szLeftLeg.length];
        SkinnedMeshJoint[]  right    = new SkinnedMeshJoint[szRightLeg.length];
        int                 iSize    = szLeftLeg.length;

        for (int i = 0; i < iSize; i++) {
            left[i]     = (SkinnedMeshJoint) skeleton.findChild(szLeftLeg[i]);
            right[i]    = (SkinnedMeshJoint) skeleton.findChild(szRightLeg[i]);
        }

        if (m_skeleton == null)
            m_skeleton = new HashMap<GUI_Enums.m_bodyPart, SkinnedMeshJoint[]>();

        m_skeleton.put(GUI_Enums.m_bodyPart.Left_Leg, left);
        m_skeleton.put(GUI_Enums.m_bodyPart.Right_Leg, right);
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

        catalogArms();
        catalogShoulders();
        catalogTorso();
        catalogLegs();
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
            case uniformHeight:
            {
                adjustBody(control, mod, actualval);
                break;
            }
            case uniformThickness:
            {
                adjustBody(control, mod, actualval);
                break;
            }
            case torsoLength:
            {
                adjustBody(control, mod, actualval);
                break;
            }
            case torsoThickness:
            {
                adjustBody(control, mod, actualval);
                break;
            }
        }
    }
}
