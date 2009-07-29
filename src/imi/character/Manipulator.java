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

package imi.character;

import com.jme.math.Vector3f;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import imi.scene.animation.AnimationGroup;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.utils.FileUtils;
import imi.utils.MaterialMeshUtils;
import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class Manipulator {

////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    static final String[] szLeftHand        = new String[] { "leftHand",           "leftHandThumb1",   "leftHandThumb2",   "leftHandThumb3",   "leftHandThumb4",
                                                             "leftPalm",           "leftHandIndex1",   "leftHandIndex2",   "leftHandIndex3",   "leftHandIndex4",
                                                             "leftHandMiddle1",    "leftHandMiddle2",  "leftHandMiddle3",  "leftHandMiddle4",
                                                             "leftHandRing1",      "leftHandRing2",    "leftHandRing3",    "leftHandRing4",
                                                             "leftHandPinky1",     "leftHandPinky2",   "leftHandPinky3",   "leftHandPinky4" };
    static final String[] szRightHand       = new String[] { "rightHand",         "rightHandThumb1",  "rightHandThumb2",  "rightHandThumb3",  "rightHandThumb4",
                                                             "rightPalm",         "rightHandIndex1",  "rightHandIndex2",  "rightHandIndex3",  "rightHandIndex4",
                                                             "rightHandMiddle1",  "rightHandMiddle2", "rightHandMiddle3", "rightHandMiddle4",
                                                             "rightHandRing1",    "rightHandRing2",   "rightHandRing3",   "rightHandRing4",
                                                             "rightHandPinky1",   "rightHandPinky2",  "rightHandPinky3",  "rightHandPinky4"};
    static final String[] szLeftLowerArm    = new String[] { "leftForeArm",     "leftForeArmRoll" };
    static final String[] szRightLowerArm   = new String[] { "rightForeArm",    "rightForeArmRoll"};
    static final String[] szLeftUpperArm    = new String[] { "leftArm",     "leftArmRoll" };
    static final String[] szRightUpperArm   = new String[] { "rightArm",    "rightArmRoll" };
    static final String[] szLeftShoulder    = new String[] { "leftShoulder" };
    static final String[] szRightShoulder   = new String[] { "rightShoulder" };
    static final String[] szTorso           = new String[] { "Spine", "Spine1", "Spine2" };
    static final String[] szLeftUpperLeg    = new String[] { "leftUpLeg",   "leftUpLegRoll" };
    static final String[] szRightUpperLeg   = new String[] { "rightUpLeg",  "rightUpLegRoll" };
    static final String[] szLeftLowerLeg    = new String[] { "leftLeg",     "leftLegRoll" };
    static final String[] szRightLowerLeg   = new String[] { "rightLeg",    "rightLegRoll" };
    static final String[] szLeftFoot        = new String[] { "leftFoot",    "leftFootBall" };
    static final String[] szRightFoot       = new String[] { "rightFoot",   "rightFootBall" };
    static final String[] szHead            = new String[] { "Head",            "Jaw",              "Tongue",           "Tongue1",
                                                             "leftLowerLip",    "rightLowerLip",    "leftInnerBrow",    "leftEyeLid",
                                                             "leftOuterBrow",   "leftCheek",        "leftUpperLip",     "leftOuterLip",
                                                             "rightInnerBrow",  "rightOuterBrow",   "rightCheek",       "rightOuterLip",
                                                             "rightUpperLip",   "rightEyeLid",      "leftEye",          "rightEye"};
    static final String[] szNeck            = new String[] { "Neck" };
    static final String[] szEyes            = new String[] { "EyeL_Adjust",     "leftEye",     "leftEyeLid",       "leftInnerBrow",    "leftOuterBrow",    "leftCheek",
                                                             "EyeR_Adjust",     "rightEye",    "rightEyeLid",      "rightInnerBrow",   "rightOuterBrow",   "rightCheek" };
    static final String[] szLips            = new String[] { "leftLowerLip",    "rightLowerLip",    "leftUpperLip",     "rightUpperLip",    "leftCheek",
                                                             "rightCheek",      "leftOuterLip",     "rightOuterLip",    "UpperLip_Adjust",  "LowerLip_Adjust" };
    static final String[] szEars            = new String[] { "LeftEar_Adjust", "RightEar_Adjust" };
    static final String[] szNose            = new String[] { "Nose_Adjust" };
    static final String[] szHips            = new String[] { "Hips" };

    public static enum Eyes {
        leftEye(0),
        rightEye(1),
        allEyes(2);

        int eyeIndex;

        Eyes(int index) {
            this.eyeIndex   = index;
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Generic Joint Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Adjusts the position of the specified character's joint by the
     * displacement (delta) provided.  This method adjusts the joint's BINDPOSE
     * transform.
     * @param character     - the character with the skeleton to use
     * @param jointName     - name of the joint to affect
     * @param displacement  - delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustJointPosition(Character character, String jointName, Vector3f displacement) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkeletonNode skeleton   = character.getSkeleton();
        return skeleton.displaceJoint(jointName, displacement);
    }

    /**
     * Adjusts the scale of the specified character's joint by the scale (delta)
     * provided.  This method adjusts the joint's BINDPOSE transform.
     * @param character     - the character with the skeleton to use
     * @param jointName     - name of the joint to affect
     * @param scale         - delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustJointScale(Character character, String jointName, Vector3f scale) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkinnedMeshJoint joint  = character.getSkeleton().getSkinnedMeshJoint(jointName);

        if (joint == null)
            return false;

        Vector3f jointScale     = joint.getBindPoseRef().getScaleVector();
        jointScale.addLocal(scale);
        joint.getBindPoseRef().setScale(jointScale);
        return true;
    }

    /**
     * Sets the position of the specified character's joint by the actual position
     * provided.  This method manipulates the joint's BINDPOSE transform.
     * @param character     - the character with the skeleton to use
     * @param jointName     - name of the joint to affect
     * @param newPosition   - new position to set
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean setJointPosition(Character character, String jointName, Vector3f newPosition) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkinnedMeshJoint joint  = character.getSkeleton().getSkinnedMeshJoint(jointName);

        if (joint == null)
            return false;

        joint.getBindPoseRef().setTranslation(newPosition);
        return true;
    }

    /**
     * Sets the scale of the specified character's joint by the actual scale
     * provided.  This method manipulates the joint's BINDPOSE scale.
     * @param character     - the character with the skeleton to use
     * @param jointName     - name of the joint to affect
     * @param newScale      - new scale to set
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean setJointScale(Character character, String jointName, Vector3f newScale) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkinnedMeshJoint joint  = character.getSkeleton().getSkinnedMeshJoint(jointName);

        if (joint == null)
            return false;

        joint.getBindPoseRef().setScale(newScale);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Specific Joint Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Adjusts the joints catagorized as the left eye with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftEyePosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szEyes[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the left eye scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftEyeScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szEyes[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the right eye with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightEyePosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szEyes[6], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right eye scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightEyeScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szEyes[6], scale);
    }

    /**
     * Adjusts the joints catagorized as the left hand with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftHandLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftHand[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the left hand scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftHandScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szLeftHand[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the right hand with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightHandLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightHand[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right hand scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightHandScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szRightHand[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the left foot with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftFootLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftFoot[1], displacement);
    }

    /**
     * Adjusts the joints catagorized as the left foot scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftFootScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szLeftFoot[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the right foot with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightFootLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightFoot[1], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right foot scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightFootScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szRightFoot[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the head with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustHeadPosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szHead[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the head scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustHeadScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szHead[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the left ear with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftEarPosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szEars[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the left ear scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftEarScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szEars[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the right ear with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightEarPosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szEars[1], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right ear scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightEarScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szEars[1], scale);
    }

    /**
     * Adjusts the joints catagorized as the nose with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustNosePosition(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szNose[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the nose scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustNoseScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szNose[0], scale);
    }

    /**
     * Adjusts the joints catagorized as the lips with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @param opposite      - true to have left and right lip corner go opposite
     *                        dir false to go same dir
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLipsPosition(Character character, Vector3f displacement, boolean opposite) {
        boolean left   = false;
        boolean right  = false;

        if (opposite) {
            left    = adjustJointPosition(character, szLips[6], displacement);
            displacement.multLocal(-1.0f);
            right   = adjustJointPosition(character, szLips[7], displacement);
        } else {
            left    = adjustJointPosition(character, szLips[6], displacement);
            right   = adjustJointPosition(character, szLips[7], displacement);
        }

        if (left && right)
            return true;
        else
            return false;
    }

    /**
     * Adjusts the joints catagorized as the upper lip scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustUpperLipScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szLips[8], scale);
    }

    /**
     * Adjusts the joints catagorized as the lower lip scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLowerLipScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szLips[9], scale);
    }

    /**
     * Adjusts the joints catagorized as the body scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustBodyScale(Character character, Vector3f scale) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkeletonNode skeleton   = character.getSkeleton();
        Vector3f curScale       = skeleton.getTransform().getLocalMatrix(true).getScaleVector();
        curScale.addLocal(scale);
        skeleton.getTransform().getLocalMatrix(true).setScale(curScale);

        return true;
    }

    /**
     * Adjusts the joints catagorized as the shoulders with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustShoulderBroadness(Character character, Vector3f displacement) {
        boolean left   = false;
        boolean right  = false;

        left    = adjustJointPosition(character, szLeftShoulder[0], displacement);
        displacement.multLocal(-1.0f);
        right   = adjustJointPosition(character, szRightShoulder[1], displacement);

        if (left && right)
            return true;
        else
            return false;
    }

    /**
     * Adjusts the joints catagorized as the Torso with the displacement (delta)
     * provided.  Value for displacment.y adjusts the vertical orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustTorsoLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szTorso[1], displacement);
    }

    /**
     * Adjusts the joints catagorized as the Torso scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustTorsoScale(Character character, Vector3f scale) {
        return adjustJointScale(character, szTorso[1], scale);
    }

    /**
     * Adjusts the joints catagorized as the Stomache scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustStomacheRoundness(Character character, Vector3f scale) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkeletonNode skeleton   = character.getSkeleton();
        SkinnedMeshJoint torso  = skeleton.getSkinnedMeshJoint(szTorso[0]);
        SkinnedMeshJoint hips   = skeleton.getSkinnedMeshJoint(szHips[0]);

        Vector3f translation = torso.getLocalModifierMatrix().getTranslation();
        translation.z += scale.z * 0.06f;
        torso.getLocalModifierMatrix().setTranslation(translation);

        Vector3f curScale = torso.getLocalModifierMatrix().getScaleVector();
        curScale.addLocal(scale.multLocal(4.0f));
        torso.getLocalModifierMatrix().setScale(scale);

        Vector3f hiptrans = hips.getLocalModifierMatrix().getTranslation();
        hiptrans.z += scale.z * 0.06f;
        hips.getLocalModifierMatrix().setTranslation(hiptrans);
        hips.getLocalModifierMatrix().setScale(scale);

        return true;
    }

    /**
     * Adjusts the joints catagorized as the left forearm scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftForearmScale(Character character, Vector3f scale) {
        for (String foreArmJoint : szLeftLowerArm) {
            adjustJointScale(character, foreArmJoint, scale);
        }
        return true;
    }

    /**
     * Adjusts the joints catagorized as the right forearm scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightForearmScale(Character character, Vector3f scale) {
        for (String foreArmJoint : szRightLowerArm) {
            adjustJointScale(character, foreArmJoint, scale);
        }
        return true;
    }

    /**
     * Adjusts the joints catagorized as the left upperarm scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftUpperarmScale(Character character, Vector3f scale) {
        for (String upperArmJoint : szLeftUpperArm) {
            adjustJointScale(character, upperArmJoint, scale);
        }
        return true;
    }

    /**
     * Adjusts the joints catagorized as the right upperarm scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightUpperarmScale(Character character, Vector3f scale) {
        for (String upperArmJoint : szRightUpperArm) {
            adjustJointScale(character, upperArmJoint, scale);
        }
        return true;
    }

    /**
     * Adjusts the joints catagorized as the left calf scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftCalfScale(Character character, Vector3f scale) {
        for (String calfJoint : szLeftLowerLeg) {
            adjustJointScale(character, calfJoint, scale);
        }
        return true;
    }    

    /**
     * Adjusts the joints catagorized as the right calf scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightCalfScale(Character character, Vector3f scale) {
        for (String calfJoint : szRightLowerLeg) {
            adjustJointScale(character, calfJoint, scale);
        }
        return true;
    }
    
    /**
     * Adjusts the joints catagorized as the left thigh scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftThighScale(Character character, Vector3f scale) {
        for (String thighJoint : szLeftUpperLeg) {
            adjustJointScale(character, thighJoint, scale);
        }
        return true;
    }
    
    /**
     * Adjusts the joints catagorized as the right thigh scale (delta) provided.
     * Value for the scale.x adjust the scale along the x-axis, scaled.y adjusts
     * the scale along the y-axis and scale.z adjusts the scale along the z-axis
     * @param character     - the character with the skeleton to use
     * @param scale         - the delta to adjust the joint scaling
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightThighScale(Character character, Vector3f scale) {
        for (String thighJoint : szRightUpperLeg) {
            adjustJointScale(character, thighJoint, scale);
        }
        return true;
    }

    /**
     * Adjusts the joints catagorized as the left forearm with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftForearmLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftLowerArm[0], displacement);
    }


    /**
     * Adjusts the joints catagorized as the left upperarm with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftUpperarmLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftUpperArm[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right forearm with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightForearmLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightLowerArm[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right upperarm with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightUpperarmLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightUpperArm[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the left calf with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftCalfLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftLowerLeg[0], displacement);
    }


    /**
     * Adjusts the joints catagorized as the left thigh with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustLeftThighLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szLeftUpperLeg[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right calf with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightCalfLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightLowerLeg[0], displacement);
    }

    /**
     * Adjusts the joints catagorized as the right thigh with the displacement (delta)
     * provided.  Value for displacement.x adjusts the horizontal orientation,
     * displacment.y adjusts the vertical orientation and displacement.z adjusts
     * the depth orientation.
     * @param character     - the character with the skeleton to use
     * @param displacement  - the delta to adjust the joint position
     * @return boolean      - true if all went well and false if it failed
     */
    public static boolean adjustRightThighLength(Character character, Vector3f displacement) {
        return adjustJointPosition(character, szRightUpperLeg[0], displacement);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Color Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the color modulation and the HairShader on the meshinstance that is
     * the hair mesh.  Grabs the meshInstance of the hair from the character passed
     * in and sets a HairShader to the material with the color modulation.
     * @param character - the character to modify
     * @param color     - color to set the hair
     */
    public static void setHairColor(Character character, Color color) {
        paramErrorCheck(character, color);

        WorldManager worldManager       = character.getWorldManager();
        PNode hairParent                = character.getSkeleton().findChild("HairAttachmentJoint");
        PPolygonMeshInstance meshInst   = null;

        if (hairParent.getChild(0) == null)
            throw new IllegalArgumentException("SEVERE ERROR: Attempting to modulate color on NON-EXISTANT hair mesh");

        meshInst = (PPolygonMeshInstance) hairParent.getChild(0);        
        MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, MaterialMeshUtils.ShaderType.HairShader, color);
        character.getCharacterParams().setHairColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Sets the color modulation and the HairShader on the meshinstance that is
     * the hair mesh.  Grabs the meshInstance of the hair from the character passed
     * in and sets a HairShader to the material with the color modulation.
     * @param character - the character to modify
     * @param color     - color to set the hair
     */
    public static void setFacialHairColor(Character character, Color color) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        if (color == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either color passed in was null");
        }

        WorldManager worldManager       = character.getWorldManager();
        PNode hairParent                = character.getSkeleton().findChild("FacialHair");
        PPolygonMeshInstance meshInst   = null;
        if (hairParent.getChild(0) instanceof PPolygonModelInstance)
            meshInst = (PPolygonMeshInstance) hairParent.getChild(0);
        else
            throw new IllegalArgumentException("SEVERE ERROR: Attempting to modulate color on NON-EXISTANT hair mesh");

        MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, MaterialMeshUtils.ShaderType.HairShader, color);
    }

    /**
     * Sets the color modulation and the FleshShader on the meshinstances that is
     * identified as skin (contains the string "head" or "nude").  Cycles through
     * the skinned mesh instances and sets the FleshShader to the materials with
     * the color modulation.
     * @param character
     * @param color
     */
    public static void setSkinTone(Character character, Color color) {
        paramErrorCheck(character, color);
        if (!character.getCharacterParams().isUsingPhongLightingForHead())
            setMeshColor(character, "Head", "head", null, MaterialMeshUtils.ShaderType.FleshShader, color, 0);
        setMeshColor(character, "Hands", null, null, MaterialMeshUtils.ShaderType.FleshShader, color, 0);
        setMeshColor(character, "UpperBody", "nude", "arms", MaterialMeshUtils.ShaderType.FleshShader, color, 0);
        setMeshColor(character, "LowerBody", "nude", "legs", MaterialMeshUtils.ShaderType.FleshShader, color, 0);
        setMeshColor(character, "Feet", "nude", "foot", MaterialMeshUtils.ShaderType.FleshShader, color, 0);
        character.getCharacterParams().setSkinTone(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Sets the color modulation and the ClothingShader on the meshinstances that is
     * identified as clothing (does not contain the strings "nude" or "arms").
     * Cycles through the skinned mesh instances and sets the ClothingShader to the
     * material with the color modulation
     * @param character - the character to modify
     * @param color     - the color to set the shirt
     */
    public static void setShirtColor(Character character, Color color) {
        paramErrorCheck(character, color);
        setMeshColor(character, "UpperBody", "nude", "arms", MaterialMeshUtils.ShaderType.ClothingShaderSpecColor, color, 1);
        character.getCharacterParams().setShirtColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Sets the color modulation and the ClothingShader on the meshinstances that is
     * identified as clothing (does not contain the strings "nude" or "legs").
     * Cycles through the skinned mesh instances and sets the ClothingShader to the
     * material with the color modulation
     * @param character - the character to modify
     * @param color     - the color to set the pants
     */
    public static void setPantsColor(Character character, Color color) {
        paramErrorCheck(character, color);
        setMeshColor(character, "LowerBody", "nude", "legs", MaterialMeshUtils.ShaderType.ClothingShaderSpecColor, color, 1);
        character.getCharacterParams().setPantsColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Sets the color modulation and the ClothingShader on the meshinstances that is
     * identified as clothing (does not contain the strings "nude" or "foot").
     * Cycles through the skinned mesh instances and sets the ClothingShader to the
     * material with the color modulation
     * @param character - the character to modify
     * @param color     - the color to set the shoes
     */
    public static void setShoesColor(Character character, Color color) {
        paramErrorCheck(character, color);
        setMeshColor(character, "Feet", "nude", "foot", MaterialMeshUtils.ShaderType.ClothingShaderSpecColor, color, 1);
        character.getCharacterParams().setShoesColor(color.getRed(), color.getGreen(), color.getBlue());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private Helper Color Manipulation Methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Private method that does the actual color & shader logic.  Uses the
     * characters skeleton to get the meshes in the specified subgroup and then
     * uses the filters (based on the check implementation) to filter the data to
     * get the right meshinstances so then the shader based on the shadertype can
     * be applied to the material of the meshinstance with the color modulation.
     * @param character     - the character to manipulate
     * @param subGroup      - the subgroup to find the right mesh
     * @param filter1       - the filter used to find the mesh (can be null)
     * @param filter2       - the filter used to find the mesh (can be null)
     * @param shaderType    - type of shader to creae and apply
     * @param color         - the color to modulate with the shader
     * @param check         - how to use the filters
     */
    private static void setMeshColor(Character character, String subGroup, String filter1,
                                     String filter2, MaterialMeshUtils.ShaderType shaderType,
                                     Color color, int check) {
        List<PPolygonSkinnedMeshInstance> group;
        WorldManager worldManager   = character.getWorldManager();
        group                       = character.getSkeleton().retrieveSkinnedMeshes(subGroup);

        switch(check)
        {
            case 0:
            {
                for (PPolygonSkinnedMeshInstance meshInst : group) {
                    if (filter2 != null) {
                        if (meshInst.getName().toLowerCase().contains(filter1) || meshInst.getName().toLowerCase().contains(filter2)) {
                           MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, shaderType, color);
                        }
                    } else if (filter1 != null){
                        if (meshInst.getName().toLowerCase().contains(filter1)) {
                           MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, shaderType, color);
                        }
                    } else {
                        MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, shaderType, color);
                    }
                }
                break;
            }
            case 1:
            {
                for (PPolygonSkinnedMeshInstance meshInst : group) {
                    if (!meshInst.getName().toLowerCase().contains(filter1) && !meshInst.getName().toLowerCase().contains(filter2)) {
                       MaterialMeshUtils.setColorOnMeshInstance(worldManager, meshInst, shaderType, color);
                    }
                }
                break;
            }
        }
    }

    /**
     * Checks the paramaters coming in (character && color) to make sure they are
     * valid before proceeding with the rest of the method.
     * @param character - the character to check for validity
     * @param color     - the color to check for validity
     */
    private static void paramErrorCheck(Character character, Color color) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        if (color == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either color passed in was null");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Texture Manipulation
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets a texture to the one the hair mesh at the specified textureindex. This
     * method searches for the hair meshinstance and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     */
    public static void setHairTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setTexture(character, relativeTexpath, "HairAttach", null, null, textureIndex);
    }
    
    /**
     * Sets a texture to the one the head mesh at the specified textureindex. This
     * method searches for the head meshinstance and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     */
    public static void setFaceTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setTexture(character, relativeTexpath, "Head", "head", null, textureIndex);
    }
    
    /**
     * Sets a texture to the one the eye mesh(s) at the specified textureindex. This
     * method searches for the eye meshinstance(s) and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     * @param whichEye          - enum representing the leftEye, rightEye or allEyes
     */
    public static void setEyesTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex, Eyes whichEye) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        
        switch(whichEye)
        {
            case leftEye:
            {
                setTexture(character, relativeTexpath, "Head", "lefteye", null, textureIndex);
                break;
            }
            case rightEye:
            {
                setTexture(character, relativeTexpath, "Head", "righteye", null, textureIndex);
                break;
            }
            case allEyes:
            {
                setTexture(character, relativeTexpath, "Head", "eye", null, textureIndex);
                break;
            }
        }
    }

    /**
     * Sets a texture to the one the shirt mesh at the specified textureindex. This
     * method searches for the shirt meshinstance and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     */
    public static void setShirtTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setTexture(character, relativeTexpath, "UpperBody", "nude", "arms", textureIndex);
    }

    /**
     * Sets a texture to the one the pants mesh at the specified textureindex. This
     * method searches for the pants meshinstance and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     */
    public static void setPantsTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setTexture(character, relativeTexpath, "LowerBody", "nude", "legs", textureIndex);
    }

    /**
     * Sets a texture to the one the shoes mesh at the specified textureindex. This
     * method searches for the shoes meshinstance and attempts to set a texture
     * into the texture index of the material.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - relative path to the texture
     * @param textureIndex      - texture index  to place the texture on the material (values 0-3)
     */
    public static void setShoesTexture(Character character, String relativeTexpath, MaterialMeshUtils.TextureType textureIndex) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setTexture(character, relativeTexpath, "Feet", "nude", "foot", textureIndex);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private Helper Texture Manipulation Methods
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Private method that does the actual texture setting.  Meshinstances found
     * under the specified subgroup from the specified character are filtered
     * using the two filter strings and the material of that meshinstance will get
     * the texture applied at the specified texture index.
     * @param character         - the character to manipulate
     * @param relativeTexpath   - the relative path to the texture
     * @param subGroup          - the subgroup to filter for the correct mesh
     * @param filter1           - filter used to find the mesh (can be null)
     * @param filter2           - filter used to find the mesh (can be null)
     * @param textureIndex      - the index to add the texture to on the material
     */
    private static void setTexture(Character character, String relativeTexpath,
                                   String subGroup, String filter1, String filter2,
                                   MaterialMeshUtils.TextureType textureIndex) {
        List<PPolygonSkinnedMeshInstance> group;
        group  = character.getSkeleton().retrieveSkinnedMeshes(subGroup);

        if (filter2 == null && filter1 != null) {
            for (PPolygonSkinnedMeshInstance meshInst : group) {
                if (meshInst.getName().toLowerCase().contains(filter1)) {
                   MaterialMeshUtils.setTextureOnMeshInstance(meshInst, relativeTexpath, textureIndex);
                }
            }
        } else if (filter2 == null && filter1 == null) {
            PNode hairParent                = character.getSkeleton().findChild(subGroup);
            PPolygonMeshInstance meshInst   = null;
            if (hairParent.getChild(0) instanceof PPolygonMeshInstance)
                meshInst = (PPolygonMeshInstance) hairParent.getChild(0);
            else
                throw new IllegalArgumentException("SEVERE ERROR: Attempting to modulate color on NON-EXISTANT hair mesh");

            MaterialMeshUtils.setTextureOnMeshInstance(meshInst, relativeTexpath, textureIndex);
        } else {
            for (PPolygonSkinnedMeshInstance meshInst : group) {
                if (!meshInst.getName().toLowerCase().contains(filter1) && !meshInst.getName().toLowerCase().contains(filter2)) {
                   MaterialMeshUtils.setTextureOnMeshInstance(meshInst, relativeTexpath, textureIndex);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Shader Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Sets a shader based on the shaderType on the hair mesh of the specified
     * character.  This method searches the character for the hair mesh and then
     * creates a new shader of shaderType and applys it to the material of the
     * hair mesh.
     * @param character     - the character to modifiy
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnHair(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setAccesoryShader(character, shaderType, "HairAttachmentJoint");
    }

    /**
     * Sets a shader based on the shaderType on the head mesh of the specified
     * character.  This method searches the character for the head mesh and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnFace(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setShader(character, shaderType, "Head", "head", null, 0);
    }

    /**
     * Sets a shader based on the shaderType on the flesh type meshes of the
     * specified character.  This method searches the character for the flesh
     * type meshes and then creates a new shader of shaderType and applys it to
     * the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnSkin(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        if (!character.getCharacterParams().isUsingPhongLightingForHead())
            setShader(character, shaderType, "Head", "head", null, 0);
        setShader(character, shaderType, "Hands", null, null, 0);
        setShader(character, shaderType, "UpperBody", "nude", "arms", 0);
        setShader(character, shaderType, "LowerBody", "nude", "legs", 0);
        setShader(character, shaderType, "Feet", "nude", "foot", 0);
    }

    /**
     * Sets a shader based on the shaderType on the eye mesh(es) of the specified
     * character.  This method searches the character for the eye mesh(es) and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     * @param whichEye      - enum  representing the leftEye, rightEye or allEyes
     */
    public static void setShaderOnEyes(Character character, MaterialMeshUtils.ShaderType shaderType, Eyes whichEye) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        
        switch(whichEye)
        {
            case leftEye:
            {
                setShader(character, shaderType, "Head", "lefteye", null, 0);
                break;
            }
            case rightEye:
            {
                setShader(character, shaderType, "Head", "righteye", null, 0);
                break;
            }
            case allEyes:
            {
                setShader(character, shaderType, "Head", "eye", null, 0);
                break;
            }
        }
    }

    /**
     * Sets a shader based on the shaderType on the tongue mesh of the specified
     * character.  This method searches the character for the eye mesh(es) and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnTongue(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        setShader(character, shaderType, "Head", "tongue", null, 0);
    }

    /**
     * Sets a shader based on the shaderType on the teeth mesh(es) of the specified
     * character.  This method searches the character for the eye mesh(es) and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnTeeth(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        setShader(character, shaderType, "Head", "teeth", null, 0);
    }

    /**
     * Sets a shader based on the shaderType on the shirt mesh of the specified
     * character.  This method searches the character for the shirt mesh and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnShirt(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setShader(character, shaderType, "UpperBody", "nude", "arms", 1);
    }

    /**
     * Sets a shader based on the shaderType on the pants mesh of the specified
     * character.  This method searches the character for the pants mesh and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnPants(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setShader(character, shaderType, "LowerBody", "nude", "legs", 1);
    }

    /**
     * Sets a shader based on the shaderType on the shoes mesh of the specified
     * character.  This method searches the character for the shoes mesh and then
     * creates a new shader of shaderType and applys it to the material of the
     * mesh.
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnShoes(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setShader(character, shaderType, "Feet", "nude", "foot", 1);
    }

    /**
     * Sets a shader based on the shaderType on the hat mesh of the specified
     * character.  This method searchs the character for the hat mesh and then
     * creates a new shader of shaderType and applys it to the material of the mesh
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnHat(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setAccesoryShader(character, shaderType, "Hats");
    }

    /**
     * Sets a shader based on the shaderType on the glasses mesh of the specified
     * character.  This method searchs the character for the glasses mesh and then
     * creates a new shader of shaderType and applys it to the material of the mesh
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnGlasses(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setAccesoryShader(character, shaderType, "Glasses");
    }

    /**
     * Sets a shader based on the shaderType on the hands mesh(es) of the specified
     * character.  This method searchs the character for the hands mesh(es) and then
     * creates a new shader of shaderType and applys it to the material of the mesh
     * @param character     - the character to modify
     * @param shaderType    - the type of shader to create and apply
     */
    public static void setShaderOnHands(Character character, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        setShader(character, shaderType, "Hands", null, null, 0);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private Helper Shader Manipulation Methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Private method that does the actual shader application.  The character
     * provides the skeleton which is used to get the meshinstances listed under
     * the specified subgroup which is then filtered by the filter strings based
     * on the check type to find the correct meshinstance which that material
     * will have the shader applied.
     * @param character     - the character to manipulate
     * @param shaderType    - the shadertype to create and apply
     * @param subGroup      - the subgroup to search for the mesh in
     * @param filter1       - the filter to find the mesh (can be null)
     * @param filter2       - the filter to find the mesh (can be null)
     * @param check         - defines how the filters are used
     */
    private static void setShader(Character character, MaterialMeshUtils.ShaderType shaderType,
                                  String subGroup, String filter1, String filter2, int check) {
        List<PPolygonSkinnedMeshInstance> group;
        WorldManager worldManager   = character.getWorldManager();
        group                       = character.getSkeleton().retrieveSkinnedMeshes(subGroup);

        switch(check) {
            case 0:
            {
                if (filter2 == null && filter1 != null) {
                    for (PPolygonSkinnedMeshInstance meshInst : group) {
                        if (meshInst.getName().toLowerCase().contains(filter1)) {
                           MaterialMeshUtils.setShaderOnMeshInstance(worldManager, meshInst, shaderType);
                        }
                    }
                } else if (filter2 != null && filter1 != null) {
                    for (PPolygonSkinnedMeshInstance meshInst : group) {
                        if (meshInst.getName().toLowerCase().contains(filter1) || meshInst.getName().toLowerCase().contains(filter2)) {
                           MaterialMeshUtils.setShaderOnMeshInstance(worldManager, meshInst, shaderType);
                        }
                    }
                } else if (filter2 == null && filter1 == null) {
                    for (PPolygonSkinnedMeshInstance meshInst : group) {
                        MaterialMeshUtils.setShaderOnMeshInstance(worldManager, meshInst, shaderType);
                    }
                }
                break;
            }
            case 1:
            {
                for (PPolygonSkinnedMeshInstance meshInst : group) {
                    if (!meshInst.getName().toLowerCase().contains(filter1) && !meshInst.getName().toLowerCase().contains(filter2)) {
                       MaterialMeshUtils.setShaderOnMeshInstance(worldManager, meshInst, shaderType);
                    }
                }
                break;
            }
        }
    }

    /**
     * Sets the name on the specified character.
     * @param character A character to operate on
     * @param newName The name to set
     */
    public static void setName(Character character, String newName) {
        character.characterParams.setName(newName);
    }

    /**
     * Private method that does the actual shader application.  The method searches
     * the character skeleton for the attatchment joint where the nonskinned mesh
     * is located and then sets the shader onto the material and applies it.
     * @param character     - the character to manipulate
     * @param shaderType    - the shadertype to create and apply
     * @param attatchJoint  - the attatchment joint the mesh is on
     */
    private static void setAccesoryShader(Character character, MaterialMeshUtils.ShaderType shaderType,
                                          String attatchJoint) {
        WorldManager worldManager   = character.getWorldManager();
        PNode parent                = character.getSkeleton().findChild(attatchJoint);
        PPolygonMeshInstance meshInst   = null;
        if (parent.getChild(0) instanceof PPolygonMeshInstance)
            meshInst = (PPolygonMeshInstance) parent.getChild(0);
        else
            throw new IllegalArgumentException("SEVERE ERROR: Attempting to modulate color on NON-EXISTANT mesh");

        MaterialMeshUtils.setShaderOnMeshInstance(worldManager, meshInst, shaderType);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Animation Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Plays a body animation for the specified character specified by the index
     * passed in.  This method will start the animation from the start and will
     * not transition from the current animation to the one specified; so if this
     * method is called during an animation, it will start the animation immediately
     * and ignore the rest of the other animation.
     * @param character         - the character to be manipulated
     * @param animationIndex    - the animationIndex to play
     */
    public static void playBodyAnimation(Character character, int animationIndex) {
        playAnimation(character, 0, animationIndex, -1, 0, false);
    }

    /**
     * Plays a body animation for the specified character specified by the index
     * passed in.  This method will transition from the currently playing animation
     * to the animation specified in this method.
     * @param character         - the character to be manipulated
     * @param animationIndex    - the animationIndex to play
     */
    public static void transitionToBodyAnimation(Character character, int animationIndex, boolean reverse) {
        playAnimation(character, 0, animationIndex, 0, -1, reverse);
    }

    /**
     * Plays a facial animation for the specified character specified by the index
     * passed in.  This method will transition from the default idle facial animation
     * to this specified facial animation in this method.
     * @param character         - the character to be manipulated
     * @param animationIndex    - the animationIndex to play
     * @param transitionTime    - the time it takes to transition from the default
     *                            anim to the specified anim
     * @param durationTime      - the time it takes to finish the specified anim
     */
    public static void playFacialAnimation(Character character, int animationIndex, float transitionTime, float durationTime) {
        playAnimation(character, 1, animationIndex, transitionTime, durationTime, false);
    }

    /**
     * Pauses/Unpauses the current body animation.  Pausing an animation will stop
     * the animation from progressing.  Unpausing an animation will allow the
     * animation to continue from it's last frame.
     * @param character - the character to manipulate
     * @param stop      - true to pause the animation and false to continue the animation
     */
    public static void pauseBodyAnimation(Character character, boolean stop) {
        pauseAnimation(character, 0, stop);
    }

    /**
     * Pauses/Unpauses the current facial animation.  Pausing an animation will stop
     * the animation from progressing.  Unpausing an animation will allow the
     * animation to continue from it's last frame.
     * @param character - the character to manipulate
     * @param stop      - true to pause the animation and false to continue the animation
     */
    public static void pauseFacialAnimation(Character character, boolean stop) {
        pauseAnimation(character, 1, stop);
    }

    /**
     * Adds the specified collada file containing the animation data into the
     * body animation grouping (0) to be cycled through for use by the character.
     * @param character         - the character to manipulate
     * @param relativeAnimPath  - the relative file path for the collada animation file
     */
    public static void addBodyAnimation(Character character, String relativeAnimPath) {
        addAnimation(character, 0, relativeAnimPath);
    }

    /**
     * Adds the specified collada file containing the animation data into the
     * facial animation grouping (1) to be cycled through for use by the character.
     * @param character         - the character to manipulate
     * @param relativeAnimPath  - the relative file path for the collada animation file
     */
    public static void addFacialAnimation(Character character, String relativeAnimPath) {
        addAnimation(character, 1, relativeAnimPath);
    }
    
    /**
     * Removes the specifed animation from the body animations grouping.
     * @param character         - the character to manipulate
     * @param animIndexToRemove - the index of the animation to remove
     */
    public static void removeBodyAnimation(Character character, int animIndexToRemove) {
        removeAnimation(character, 0, animIndexToRemove);
    }

    /**
     * Removes the specifed animation from the facial animations grouping.
     * @param character         - the character to manipulate
     * @param animIndexToRemove - the index of the animation to remove
     */
    public static void removeFacialAnimation(Character character, int animIndexToRemove) {
        removeAnimation(character, 1, animIndexToRemove);
    }

    /**
     * Sets the animation speed of the current animation in the body animation group
     * @param character         - the character to manipulate
     * @param animationSpeed    - the speed as a float to set the current animation to
     */
    public static void setBodyAnimationSpeed(Character character, float animationSpeed) {
        setAnimationSpeed(character, animationSpeed, 0);
    }

    /**
     * Sets the animation speed of the current animation in the facial animation group
     * @param character         - the character to manipulate
     * @param animationSpeed    - the speed as a float to set the current animation to
     */
    public static void setFacialAnimationSpeed(Character character, float animationSpeed) {
        setAnimationSpeed(character, animationSpeed, 1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private Helper Animation Manipulation Methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Private method that does the actual animation manipulation.  For regular
     * playback set the transitionTime to -1.  For transition during animation use
     * -1 for durationTIme.  For facial animation use it normally.
     * @param character         - the character to manipulate
     * @param animGroup         - the animation group to manipulate
     * @param animationIndex    - the animation to play
     * @param transitionTime    - how long it takes to transition from default to specified animation (facial only)
     * @param durationTime      - how long the animation takes to complete (facial only)
     * @param reverse           - true to play the animation in reverse false to play normally (transition only)
     */
    private static void playAnimation(Character character, int animGroup, int animationIndex, float transitionTime, float durationTime, boolean reverse) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        SkeletonNode skeleton   = character.getSkeleton();

        if (skeleton.getAnimationState(animGroup) == null) {
            throw new RuntimeException("SEVERE ERROR: Characer has no animations loaded");
        }
        if (animationIndex < 0 || animationIndex > skeleton.getAnimationGroup(animGroup).getCycleCount()) {
            throw new IllegalArgumentException("SEVERE ERROR: animationIndex out of range");
        }

        skeleton.getAnimationState(animGroup).setPauseAnimation(false);  // just in case animations are paused

        if (transitionTime == -1) {
            skeleton.getAnimationState(animGroup).setCurrentCycleTime(0.0f);
            skeleton.getAnimationState(animGroup).setCurrentCycle(animationIndex);
        } else if (durationTime == -1) {
            String animName = skeleton.getAnimationGroup(animGroup).getCycle(animationIndex).getName();
            skeleton.transitionTo(animName, reverse);
        } else {
            character.initiateFacialAnimation(animationIndex, transitionTime, durationTime);
        }
    }
    
    /**
     * Private method that does the actual animation manipulation.  To manipulate
     * the body antimations use subgroup 0 to manipulate facial animations use
     * subgroup 1.  Unpauseing continues from the frame were the animation was
     * originally paused.
     * @param character - the character to manipulate
     * @param animGroup - the animation group to manipulate
     * @param stop      - true to pause the animation and false to unpause the animation
     */
    private static void pauseAnimation(Character character, int animGroup, boolean stop) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        if (character.getSkeleton().getAnimationGroup(animGroup) == null) {
            throw new IllegalArgumentException("SEVERE ERROR: The specified animation group does not exist");
        }
        character.getSkeleton().getAnimationState(animGroup).setPauseAnimation(stop);
    }

    /**
     * Private method that does the actual animation loading.  The animGroup specifies
     * to load either body animatiion (0) or facial animation (1).  Instruction
     * processor is created and the apporpriate load animationn instruction is added
     * with a url to the animation file and then the processer executes.
     * @param character         - the character to manipulate
     * @param animGroup         - the animation group to manipulate
     * @param relativeAnimPath  - the collada animation file to load
     */
    private static void addAnimation(Character character, int animGroup, String relativeAnimPath) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }

        if (character.getSkeleton().getAnimationGroup(animGroup) == null) {
            if (animGroup == 0)
                character.getSkeleton().getAnimationComponent().addGroup(new AnimationGroup("BodyAnimations"));
            else if (animGroup == 1)
                character.getSkeleton().getAnimationComponent().addGroup(new AnimationGroup("FacialAnimations"));
        }

        String urlBase  = "file:///" + System.getProperty("user.dir") + File.separatorChar;
        try {
            URL animURL = new URL(urlBase + relativeAnimPath);
            InstructionProcessor pProcessor = new InstructionProcessor(character.getWorldManager());
            Instruction pRootInstruction    = new Instruction();
            pRootInstruction.addChildInstruction(InstructionType.setSkeleton, character.getSkeleton());

            if (animGroup == 0)
                pRootInstruction.addChildInstruction(InstructionType.loadAnimation, animURL);
            else if (animGroup == 1)
                pRootInstruction.addChildInstruction(InstructionType.loadFacialAnimation, animURL);

            pProcessor.execute(pRootInstruction);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Manipulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Private method that does the actual animation removal.  The animGroup specifies
     * body animation group (0) or facial animation group (1).  The method removes
     * specified animation from the specified group from the specified character.
     * @param character     - the character to manipulate
     * @param animGroup     - the animation group to modify
     * @param animToRemove  - the animation cycle to remove
     */
    private static void removeAnimation(Character character, int animGroup, int animToRemove) {
        if (character == null || character.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character was null or the skeletonnode was null");
        }
        if (character.getSkeleton().getAnimationGroup(animGroup) == null) {
            throw new IllegalArgumentException("SEVERE ERROR: The specified animation group does not exist");
        }

        character.getSkeleton().getAnimationGroup(animGroup).removeCycle(animToRemove);
    }

    /**
     * Sets the animation playback speed of the current animation.
     * @param character         - the character to manipulate
     * @param animationSpeed    - the speed as a float to set the animation to
     */
    private static void setAnimationSpeed(Character character, float animationSpeed, int animationGroup) {
        character.getSkeleton().getAnimationState(animationGroup).setAnimationSpeed(animationSpeed);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Mesh Swapping Manipulation
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Swaps out the meshes in the UpperBody region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapShirtMesh(Character character, boolean useRepository, File colladaFile) {
        boolean success = swapSkinnedMesh(character, useRepository, colladaFile, "UpperBody");
        setShaderOnShirt(character, MaterialMeshUtils.ShaderType.ClothingShaderSpecColor);
        return success;
    }

    /**
     * Swaps out the meshes in the LowerBody region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapPantsMesh(Character character, boolean useRepository, File colladaFile) {
        boolean success = swapSkinnedMesh(character, useRepository, colladaFile, "LowerBody");
        setShaderOnPants(character, MaterialMeshUtils.ShaderType.ClothingShaderSpecColor);
        return success;
    }

    /**
     * Swaps out the meshes in the Feet region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapShoesMesh(Character character, boolean useRepository, File colladaFile) {
        boolean success = swapSkinnedMesh(character, useRepository, colladaFile, "Feet");
        setShaderOnShoes(character, MaterialMeshUtils.ShaderType.ClothingShaderSpecColor);
        return success;
    }

    /**
     * Swaps out the meshes in the Jacket region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapJacketMesh(Character character, boolean useRepository, File colladaFile) {
        boolean success = swapSkinnedMesh(character, useRepository, colladaFile, "Jackets");
        setShaderOnShirt(character, MaterialMeshUtils.ShaderType.ClothingShaderSpecColor);
        return success;
    }

    /**
     * Swaps out the meshes in the Hands region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapHandsMesh(Character character, boolean useRepository, File colladaFile) {
        boolean success = swapSkinnedMesh(character, useRepository, colladaFile, "Hands");
        setShaderOnHands(character, MaterialMeshUtils.ShaderType.FleshShader);
        return success;
    }

    /**
     * Swaps out the meshes in the Head region of the specified character
     * with the new geometry in the specified collada file.  Loads a new head and
     * replaces the old old head and then applies the shaders on the head.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param headFile      - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapHeadMesh(Character character, boolean useRepository, File headFile, MaterialMeshUtils.ShaderType shaderType) {
        if (character == null || character.getSkeleton() == null || !headFile.exists()) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character data is bad or collada file does not exist");
        }

        character.getPScene().setUseRepository(useRepository);

        String base = System.getProperty("user.dir");
        String path = FileUtils.getRelativePath(new File(base), headFile);
        if (shaderType == MaterialMeshUtils.ShaderType.PhongFleshShader)
            character.characterParams.setUsePhongLightingForHead(true);
        else
            character.characterParams.setUsePhongLightingForHead(false);
        
        URL modelURL;
        boolean result = false;
        try {
            modelURL = headFile.toURI().toURL();
            character.installHead(modelURL);
            character.getCharacterParams().setHeadAttachment(path);
            character.initializeMeshInstanceMaterialStates();

            PPolygonSkinnedMeshInstance[] meshes = character.getSkeleton().getMeshesBySubGroup("Head");
            for (PPolygonSkinnedMeshInstance smInstance : meshes) {
                smInstance.applyMaterial();
            }

            setShaderOnFace(character, shaderType);
            setShaderOnEyes(character, MaterialMeshUtils.ShaderType.EyeballShader, Eyes.allEyes);
            setShaderOnTongue(character, MaterialMeshUtils.ShaderType.EyeballShader);
            setShaderOnTeeth(character, MaterialMeshUtils.ShaderType.EyeballShader);
            result = true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Manipulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Swaps out the meshes in the Hair region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapHairMesh(Character character, boolean useRepository, File colladaFile, String meshName) {
        boolean success = swapNonSkinnedMesh(character, useRepository, colladaFile, meshName, "HairAttach", PMatrix.IDENTITY, "HairAttachmentJoint");
        setShaderOnHair(character, MaterialMeshUtils.ShaderType.HairShader);
        PNode hairSmoothNormalsHack = character.getSkeleton().findChild(meshName);
        if (hairSmoothNormalsHack instanceof PPolygonMeshInstance)
        {
            ((PPolygonMeshInstance)hairSmoothNormalsHack).getGeometry().setSmoothNormals(true);
             character.getWorldManager().addRenderUpdater(new RenderUpdater() {
                public void update(Object geometry) {
                    ((PPolygonMesh)geometry).submit();
                }
            }, ((PPolygonMeshInstance)hairSmoothNormalsHack).getGeometry());
        }
        return success;
    }

    /**
     * Swaps out the meshes in the Facial Hair region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @param meshName      - the name of the hair mesh to be loaded
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapFacialHairMesh(Character character, boolean useRepository, File colladaFile, String meshName) {
        boolean success = swapNonSkinnedMesh(character, useRepository, colladaFile, meshName, "Head", PMatrix.IDENTITY, "FacialHair");
        setShaderOnHair(character, MaterialMeshUtils.ShaderType.HairShader);
        return success;
    }

    /**
     * Swaps out the meshes in the Hat region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @param meshName      - the name of the hat mesh to be loaded
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapHatMesh(Character character, boolean useRepository, File colladaFile, String meshName) {
        boolean success = swapNonSkinnedMesh(character, useRepository, colladaFile, meshName, "Head", PMatrix.IDENTITY, "Hats");
        setShaderOnHat(character, MaterialMeshUtils.ShaderType.SimpleTNLWithAmbient);
        return success;
    }

    /**
     * Swaps out the meshes in the Glasses region of the specified character
     * with the new geometry in the specified collada file.
     * @param character     - the character to manipulate
     * @param useRepository - true to use the repository, false to disable the repository
     * @param colladaFile   - the model file to load (collada)
     * @param meshName      - the name of the glasses mesh to be loaded
     * @return boolean      - true if there were no problems and will throw an exception otherwise
     */
    public static boolean swapGlassesMesh(Character character, boolean useRepository, File colladaFile, String meshName) {
        boolean success = swapNonSkinnedMesh(character, useRepository, colladaFile, meshName, "Head", PMatrix.IDENTITY, "Glasses");
        setShaderOnGlasses(character, MaterialMeshUtils.ShaderType.SimpleTNLWithAmbient);
        return success;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Private Helper Mesh Swapping Manipulation Methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Private method that does the actual swaping of the skinned meshes.  This
     * method creates an instruction processor and instructions to delete the meshes
     * that are no longer used and instructions to load the meshes from the collada file.
     * This method also updates the character paramaters for this character by removing
     * the skinned mesh instructions and loading instructions that are no longer neeeded
     * and updates it with the new skinnedMeshParams.
     * @param character     - character to be manipulated
     * @param useRepository - true if using the repository and false if not using repository
     * @param colladaFile   - the collada file to load and swap geometry
     * @param subGroup      - the subgroup the meshes belong to
     * @return boolean      - true if there was no problems and will throw an exception otherwise
     */
    public static boolean swapSkinnedMesh(Character character, boolean useRepository, File colladaFile,
                                           String subGroup) {
        if (character == null || character.getSkeleton() == null || !colladaFile.exists()) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character data is bad or collada file does not exist");
        }
        if (subGroup == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Null parameters -subGroup = " + subGroup);
        }

        WorldManager worldManager   = character.getWorldManager();
        character.getPScene().setUseRepository(useRepository);
        SkeletonNode skeleton       = character.getSkeleton();

        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
        Instruction pRootInstruction    = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skeleton);

        removeSkinnedMesh(character, pRootInstruction, subGroup);
        boolean result = false;
        try {
            String base = System.getProperty("user.dir");
            String path = FileUtils.getRelativePath(new File(base), colladaFile);
            pRootInstruction.addLoadGeometryToSubgroupInstruction(colladaFile.toURI().toURL(), subGroup);
            pProcessor.execute(pRootInstruction);
            updateSkinnedMeshParams(character, subGroup, path);
            result = true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Manipulator.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        PPolygonSkinnedMeshInstance[] meshes = character.getSkeleton().getMeshesBySubGroup(subGroup);
        for (PPolygonSkinnedMeshInstance smInstance : meshes) {
            smInstance.applyMaterial();
        }
        return result;
    }

    /**
     * Private method that does the actual swaping of the non-skinned meshes.  This
     * method removes the attatchmentJoints with the attachment and creates a
     * processor and instructions to load the geometry from the colladafile.  Afterwards
     * the characterParams are updated with the new loading instructions and attatchment
     * instructions
     * Note : Call ApplyMaterial() on the character when done!
     * @param character         - character to be manipulated
     * @param useRepository     - true if using the repository and false if not using repository
     * @param colladaFile       - the collada file to load and swap geometry
     * @param meshName          - the name of the mesh to load (non skinned meshes reside in packages)
     * @param parentJoint       - the joint to attatch onto
     * @param transform         - the transform for the mesh (should be identity by default)
     * @param attchmentJoint    - the attatchmentjoint to attatch to the parent (mesh attatchs to this joint)
     * @return boolean          - true if there was no problems otherwise an exception is thrown.
     */
    public static boolean swapNonSkinnedMesh(Character character, boolean useRepository, File colladaFile,
                                              String meshName, String parentJoint, PMatrix transform, String attchmentJoint) {
        if (character == null || character.getSkeleton() == null || !colladaFile.exists()) {
            throw new IllegalArgumentException("SEVERE ERROR: Either character data is bad or collada file does not exist");
        }
        if (meshName == null || parentJoint == null || attchmentJoint == null) {
            throw new IllegalArgumentException("SEVERE ERROR: Null parameters -meshName = " + meshName
                                               + " -parentJoint = " + parentJoint
                                               + " -attchmentJoint = " + attchmentJoint);
        }

        WorldManager worldManager       = character.getWorldManager();
        character.getPScene().setUseRepository(useRepository);
        SkeletonNode skeleton           = character.getSkeleton();

        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
        Instruction pRootInstruction    = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skeleton);

        removeAttatchment(character, attchmentJoint);

        boolean result = false;
        try {
            String base = System.getProperty("user.dir");
            String path = FileUtils.getRelativePath(new File(base), colladaFile);
            pRootInstruction.addChildInstruction(InstructionType.loadGeometry, colladaFile.toURI().toURL());
            pRootInstruction.addAttachmentInstruction( meshName, parentJoint, transform, attchmentJoint );
            pProcessor.execute(pRootInstruction);
            character.getCharacterParams().addAttachmentInstruction(new AttachmentParams(meshName, parentJoint, transform, attchmentJoint, meshName));
            character.getCharacterParams().addLoadInstruction(path);
            result = true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Manipulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        PNode mesh = character.getSkeleton().findChild(meshName);
        if (mesh instanceof PPolygonMeshInstance)
            ((PPolygonMeshInstance)mesh).applyMaterial();
        return result;
    }

    /**
     * Private method that does the actual removing of the old attatchments.  This method
     * removes the attatchment joints containing the old mesh(es) and then goes through
     * the attatchmentInstructions and removes the attatchment instruction matching the
     * passed in attatchmentJoint.  When it removes that joint it also removes the loading
     * instructions associated with that attatchment param.
     * @param character         - the character to be modified
     * @param attatchmentJoint  - the attatchment joint containing the targetmesh
     */
    private static void removeAttatchment(Character character, String attatchmentJoint) {
        Iterable attatchmentInst    = character.getCharacterParams().getAttachmentsInstructions();
        Iterable loadingInst        = character.getCharacterParams().getLoadInstructions();

        // Removes the attatchment from the skeleton
        PNode joint = character.getSkeleton().findChild(attatchmentJoint);
        character.getSkeleton().findAndRemoveChild(joint);

        // Removes the loading param and the attatchment params
        for (Iterator it = attatchmentInst.iterator(); it.hasNext();) {
            AttachmentParams attatch = (AttachmentParams) it.next();
            if (attatch.getAttachmentJointName().equals(attatchmentJoint)) {
                String loadParam    = attatch.getOwningFileName();
                for (Iterator it1 = loadingInst.iterator(); it1.hasNext();) {
                    String loading = (String) it1.next();
                    if (loading.equals(loadParam))
                        it1.remove();
                }
                it.remove();
            }
        }
    }

    /**
     * Private method that does the actual removing of the old Skinned meshes.  This method
     * creates child instructions to delete skinned meshes used to remove the old meshes and
     * then updates the loading and skinned mesh params to reflect the deletion.
     * @param character - the character to be modified
     * @param instruct  - the instruction to add the deleteInstruction to
     * @param subGroup  - the subgroup the mesh(es) old meshes belong to
     */
    private static void removeSkinnedMesh(Character character, Instruction instruct, String subGroup) {
        Iterable smInstruct     = character.getCharacterParams().getSkinnedMeshInstructions();
        Iterable loadingInst    = character.getCharacterParams().getLoadInstructions();
        
        // Removes the skinned mesh from the skeleton
        String[] meshes = character.getSkeleton().getMeshNamesBySubGroup(subGroup);
        for (String meshName : meshes) {
            instruct.addChildInstruction(InstructionType.deleteSkinnedMesh, meshName);
        }
        
        // Removes the loading param and the skinnedMesh params
        for (Iterator it = smInstruct.iterator(); it.hasNext();) {
            CharacterParams.SkinnedMeshParams skinnedMeshParam = (CharacterParams.SkinnedMeshParams)it.next();
            for (String meshNames : meshes) {
                if (skinnedMeshParam.getMeshName().equals(meshNames)) {
                    String loadParam    = skinnedMeshParam.getOwningFileName();
                    for (Iterator it1 = loadingInst.iterator(); it1.hasNext();) {
                        String loading = (String)it1.next();
                        if (loading.equals(loadParam))
                            it1.remove();
                    }
                    it.remove();
                    break;
                }
            }            
        }
    }

    /**
     * Private mehtod that does the actual updating of the skinnedmesh params.  This method
     * updates the loadInstruction in the character params with the loadingFilePath as well as
     * creates new skinnedmesh params and adds them to the characterParams.
     * @param character         - the character to be modified
     * @param subGroup          - the subgroup the new meshes are located
     * @param loadingFilePath   - the loading file associated to the meshes (used in updating the skinnedmesh param)
     */
    private static void updateSkinnedMeshParams(Character character, String subGroup, String loadingFilePath) {
        String[] meshNames  = character.getSkeleton().getMeshNamesBySubGroup(subGroup);
        for (String mesh : meshNames) {
            CharacterParams.SkinnedMeshParams smParam   = character.getCharacterParams().createSkinnedMeshParams(mesh, subGroup, loadingFilePath);
            character.getCharacterParams().addSkinnedMeshParams(smParam);
        }
        character.getCharacterParams().addLoadInstruction(loadingFilePath);
    }
}
