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
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.SkeletonFlatteningManipulator;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import javolution.util.FastMap;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * Overrides animation matrices of specific joints on the right time to enable
 * the verlet arms demonstration.
 * @author Lou Hayt
 */
@ExperimentalAPI
public class VerletSkeletonFlatteningManipulator implements SkeletonFlatteningManipulator
{
    /**
     * List of the joints as they are named in the avatar's skeleton
     */
    private enum Joints
    {
        rightShoulder("rightArm"),
        rightUpperArm("rightArmRoll"),
        rightElbow("rightForeArm"),
        rightForearm("rightForeArmRoll"),
        rightWrist("rightHand"),
        leftShoulder("leftArm"),
        leftUpperArm("leftArmRoll"),
        leftElbow("leftForeArm"),
        leftForearm("leftForeArmRoll"),
        leftWrist("leftHand"),
        rightEye("rightEye"),
        leftEye("leftEye");
        
        String jointName;
        int    jointIndex = -1;
        Joints(String jointName) {
            this.jointName = jointName;
        }
    }

    private final FastMap<Integer, Joints> jointMap = new FastMap<Integer, Joints>();

    private float distanceFromShoulderToUpperArm = 0.1331867f;
    private float distanceFromElbowToForeArm     = 0.0862977f;
    
    protected EyeBall    leftEyeBall   = null;
    protected EyeBall    rightEyeBall  = null;
    private VerletArm    rightArm      = null;
    private VerletArm    leftArm       = null;
    private SkeletonNode skeleton      = null;
    private PPolygonModelInstance characterModelInst = null;
    
    private boolean manualDriveReachUp = true; // otherwise reaching forward
    private boolean armsEnabled        = false;
    private boolean leftArmEnabled     = false;
    private boolean rightArmEnabled    = false;

    private final Vector3f rightShoulderLocalX = new Vector3f();
    private final Vector3f leftShoulderLocalX  = new Vector3f();

    /**
     * Construct a new manipulator to affect the provided verlet arms, eyeballs,
     * and skeleton.
     * @param leftVerletArm
     * @param rightVerletArm
     * @param left The left eye
     * @param right The right eye
     * @param skeletonNode Skeleton owning the above
     * @param modelInstance Model instance owning the above :)
     * @throws IllegalArgumentException If any param is null
     */
    public VerletSkeletonFlatteningManipulator( VerletArm leftVerletArm,
                                                VerletArm rightVerletArm,
                                                EyeBall left,
                                                EyeBall right,
                                                SkeletonNode skeletonNode,
                                                PPolygonModelInstance modelInstance)
    {
        if (leftVerletArm == null || rightVerletArm == null ||
                left == null || right == null || skeletonNode == null ||
                modelInstance == null)
                throw new IllegalArgumentException("Null param encountered!");
        leftEyeBall  = left;
        rightEyeBall = right;
        rightArm     = rightVerletArm;
        leftArm      = leftVerletArm;
        characterModelInst = modelInstance;
        skeleton     = skeletonNode;

        for (Joints j : Joints.values())
        {
            j.jointIndex = skeleton.getSkinnedMeshJointIndex(j.jointName);
            jointMap.put(j.jointIndex, j);
        }
        skeleton.setFlatteningHook(this);
    }

    /**
     * {@inheritDoc SkeletonFlatteningManipulator}
     */
    @InternalAPI
    public void processSkeletonNode(PNode current) 
    {
        if (!(current instanceof SkinnedMeshJoint))
            return;

        SkinnedMeshJoint joint = (SkinnedMeshJoint)current;
        PMatrix matrix = joint.getMeshSpace();
        int jointIndex = skeleton.getSkinnedMeshJointIndex(joint);
        Joints j = jointMap.get(jointIndex);
        if (j == null)
            return;
        switch (j)
        {
            case rightEye:
                if (rightEyeBall != null)
                    rightEyeBall.lookAtTarget(matrix);
                break;
            case leftEye:
            if (leftEyeBall != null)
                    leftEyeBall.lookAtTarget(matrix);
                break;
        }
        
        if (!armsEnabled)
            return;
        
        switch (j)
        {
            case rightShoulder:
                if (rightArmEnabled)
                    modifyShoulder(matrix, true);
                break;
            case rightUpperArm:
                if (rightArmEnabled)
                    modifyUpperArm(matrix, true);
                break;
            case rightElbow:
                if (rightArmEnabled)
                    modifyElbow(matrix, true);
                break;
            case rightForearm:
                if (rightArmEnabled)
                    modifyForeArm(matrix, true);
                break;
            case rightWrist:
                if (rightArmEnabled)
                    modifyWrist(matrix, true);
                break;
                
            case leftShoulder:
                if (leftArmEnabled)
                    modifyShoulder(matrix, false);
                break;
            case leftUpperArm:
                if (leftArmEnabled)
                    modifyUpperArm(matrix, false);
                break;
            case leftElbow:
                if (leftArmEnabled)
                    modifyElbow(matrix, false);
                break;
            case leftForearm:
                if (leftArmEnabled)
                    modifyForeArm(matrix, false);
                break;
            case leftWrist:
                if (leftArmEnabled)
                    modifyWrist(matrix, false);
                break;
        }
    }

    private void modifyShoulder(PMatrix matrix, boolean right) 
    {
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.rightShoulder.jointIndex);
            Vector3f         elbowPosition = new Vector3f();
            rightArm.getElbowPosition(elbowPosition);
            modifyShoulder(matrix, shoulderJoint, elbowPosition, rightShoulderLocalX);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.leftShoulder.jointIndex);
            Vector3f         elbowPosition = new Vector3f();
            leftArm.getElbowPosition(elbowPosition);
            modifyShoulder(matrix, shoulderJoint, elbowPosition, leftShoulderLocalX);
        }
    }
    
    private void modifyShoulder(PMatrix matrix, SkinnedMeshJoint shoulderJoint, Vector3f elbowPosition, Vector3f shoulderLocalX) 
    {
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        calculateInverseModelWorldMatrix().transformPoint(elbowPosition);
        Vector3f localY = elbowPosition.subtract(shoulderPosition).normalize();
        shoulderLocalX.set(shoulderJoint.getMeshSpace().getLocalXNormalized());
        Vector3f localZ = new Vector3f(Vector3f.UNIT_Y);
        shoulderLocalX.set(localY.cross(localZ).normalize());
        localZ.set(shoulderLocalX.cross(localY).normalize());
        
        matrix.setLocalX(shoulderLocalX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(shoulderPosition);   
    }

    private void modifyUpperArm(PMatrix matrix, boolean right) 
    {
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.rightShoulder.jointIndex);
            Vector3f         elbowPosition = new Vector3f();
            rightArm.getElbowPosition(elbowPosition);
            modifyUpperArm(matrix, shoulderJoint, elbowPosition, rightShoulderLocalX);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.leftShoulder.jointIndex);
            Vector3f         elbowPosition = new Vector3f();
            leftArm.getElbowPosition(elbowPosition);
            modifyUpperArm(matrix, shoulderJoint, elbowPosition, leftShoulderLocalX);
        } 
    }
    
    private void modifyUpperArm(PMatrix matrix, SkinnedMeshJoint shoulderJoint, Vector3f elbowPosition, Vector3f shoulderLocalX) 
    {
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        calculateInverseModelWorldMatrix().transformPoint(elbowPosition);
        Vector3f offsetFromShoulder = elbowPosition.subtract(shoulderPosition).normalize();
        
        // The Y-Axis is aligned with the shoulder offset
        Vector3f localY = new Vector3f(offsetFromShoulder);
        
        offsetFromShoulder.multLocal(distanceFromShoulderToUpperArm);
        
        // generate the local Z axis
        Vector3f localZ = shoulderLocalX.cross(localY).normalize();
        
        matrix.setLocalX(shoulderLocalX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(shoulderPosition.add(offsetFromShoulder));
    }
    
    private void modifyElbow(PMatrix matrix, boolean right) 
    {   
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.rightShoulder.jointIndex);
            Vector3f elbowPosition = new Vector3f();
            rightArm.getElbowPosition(elbowPosition);
            Vector3f wristPosition = new Vector3f();
            rightArm.getWristPosition(wristPosition);
            modifyElbow(matrix, shoulderJoint, elbowPosition, wristPosition);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.leftShoulder.jointIndex);
            Vector3f elbowPosition = new Vector3f();
            leftArm.getElbowPosition(elbowPosition);
            Vector3f wristPosition = new Vector3f();
            leftArm.getWristPosition(wristPosition);
            modifyElbow(matrix, shoulderJoint, elbowPosition, wristPosition);
        } 
    }
    
    private void modifyElbow(PMatrix matrix, SkinnedMeshJoint shoulderJoint, Vector3f elbowPosition, Vector3f wristPosition) 
    {   
        PMatrix inverseModelWorldMatrix = calculateInverseModelWorldMatrix();
        
        inverseModelWorldMatrix.transformPoint(elbowPosition);
        
        // The Y-Axis is aligned with the shoulder offset
        
        inverseModelWorldMatrix.transformPoint(wristPosition);
        Vector3f localY   = wristPosition.subtract(elbowPosition).normalize();
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        
        Vector3f shoulderToWrist = wristPosition.subtract(shoulderPosition).normalize();
        Vector3f localX = shoulderToWrist.cross(Vector3f.UNIT_Y).normalize();
                
        Vector3f localZ = localX.cross(localY);
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(elbowPosition);
    }

    private void modifyForeArm(PMatrix matrix, boolean right) 
    {
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.rightShoulder.jointIndex);
            Vector3f elbowPosition = new Vector3f();
            rightArm.getElbowPosition(elbowPosition);
            Vector3f wristPosition = new Vector3f();
            rightArm.getWristPosition(wristPosition);
            modifyForeArm(matrix, shoulderJoint, elbowPosition, wristPosition);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.leftShoulder.jointIndex);
            Vector3f elbowPosition = new Vector3f();
            leftArm.getElbowPosition(elbowPosition);
            Vector3f wristPosition = new Vector3f();
            leftArm.getWristPosition(wristPosition);
            modifyForeArm(matrix, shoulderJoint, elbowPosition, wristPosition);
        } 
    }
    
    private void modifyForeArm(PMatrix matrix, SkinnedMeshJoint shoulderJoint, Vector3f elbowPosition, Vector3f wristPosition) 
    {
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        PMatrix inverseModelWorldMatrix = calculateInverseModelWorldMatrix();
        inverseModelWorldMatrix.transformPoint(elbowPosition);
        inverseModelWorldMatrix.transformPoint(wristPosition);
        
        Vector3f elbowToWrist   = wristPosition.subtract(elbowPosition).normalize();
        
        // The Y-Axis is aligned with the elbow offset
        Vector3f localY = new Vector3f(elbowToWrist);
        
        elbowToWrist.multLocal(distanceFromElbowToForeArm);
        
        Vector3f shoulderToWrist   = wristPosition.subtract(shoulderPosition).normalize();
        Vector3f localX = shoulderToWrist.cross(localY).normalize();
        
        Vector3f localZ = localX.cross(localY).normalize();
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(elbowPosition.add(elbowToWrist));
    }

    private void modifyWrist(PMatrix matrix, boolean right)
    {
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.rightShoulder.jointIndex);
            Vector3f wristPosition = new Vector3f();
            rightArm.getWristPosition(wristPosition);
            modifyWrist(matrix, shoulderJoint, wristPosition, right);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(Joints.leftShoulder.jointIndex);
            Vector3f wristPosition = new Vector3f();
            leftArm.getWristPosition(wristPosition);
            modifyWrist(matrix, shoulderJoint, wristPosition, right);
        } 
    }
    
    private void modifyWrist(PMatrix matrix, SkinnedMeshJoint shoulderJoint, Vector3f wristPosition, boolean right)
    {
        PMatrix inverseModelWorldMatrix = calculateInverseModelWorldMatrix();
        inverseModelWorldMatrix.transformPoint(wristPosition);
                
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        Vector3f localY = wristPosition.subtract(shoulderPosition).normalize();
        
        Vector3f localZ = new Vector3f();
        Vector3f localX = new Vector3f();
        
        if (manualDriveReachUp)
        {
            localZ.set(localY);
            localY.set(Vector3f.UNIT_Y);
            localX.set(localZ.cross(localY).normalize());
            localZ.set(localX.cross(localY).normalize());
        }
        else
        {
            localX.set(localY.cross(Vector3f.UNIT_Y).normalize());
            localZ.set(localX.cross(localY).normalize());
        }
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(wristPosition);
        
        if (right)
        {
            PMatrix fixRotation = new PMatrix();
            fixRotation.buildRotationZ((float)Math.toRadians(40));
            matrix.fastMul(fixRotation);
        }
        else
        {
            PMatrix fixRotation = new PMatrix();
            fixRotation.buildRotationZ((float)Math.toRadians(-40));
            matrix.fastMul(fixRotation);
        }
    }

    /**
     * Enable / Disable the verlet arms.
     * @param bEnabled True to enable
     */
    public void setArmsEnabled(boolean bEnabled) {
        this.armsEnabled = bEnabled;
    }

    /**
     * Determine if the verlet arms are enabled.
     * @return True if enabled
     */
    public boolean isArmsEnabled() {
        return this.armsEnabled;
    }

    /**
     * Determine if the left arm is enabled.
     * @return True if enabled
     */
    public boolean isLeftArmEnabled() {
        return leftArmEnabled;
    }

    /**
     * Enable / Disable the left arm
     * @param leftArmEnabled True to enable
     */
    public void setLeftArmEnabled(boolean leftArmEnabled) {
        this.leftArmEnabled = leftArmEnabled;
        if (rightArmEnabled || leftArmEnabled)
            armsEnabled = true;
        else
            armsEnabled = false;
    }

    /**
     * Determine if the right arm is enabled.
     * @return true if enabled
     */
    public boolean isRightArmEnabled() {
        return rightArmEnabled;
    }

    /**
     * Enable / Disable the right arm
     * @param rightArmEnabled True to enable
     */
    public void setRightArmEnabled(boolean rightArmEnabled) {
        this.rightArmEnabled = rightArmEnabled;
        if (rightArmEnabled || leftArmEnabled)
            armsEnabled = true;
        else
            armsEnabled = false;
    }

    /**
     * Sets the arm behavior to reach up rather than forward.
     * @param manualDriveReachUp True to reach up
     */
    public void setManualDriveReachUp(boolean manualDriveReachUp)
    {
        this.manualDriveReachUp = manualDriveReachUp;
    }
    
    private PMatrix calculateInverseModelWorldMatrix()
    {
        return characterModelInst.getTransform().getWorldMatrix(false).inverse();
    }

    //////// Package level access //////////
    EyeBall getLeftEyeBall() {
        return leftEyeBall;
    }

    void setLeftEyeBall(EyeBall leftEyeBall) {
        this.leftEyeBall = leftEyeBall;
    }

    EyeBall getRightEyeBall() {
        return rightEyeBall;
    }

    void setRightEyeBall(EyeBall rightEyeBall) {
        this.rightEyeBall = rightEyeBall;
    }

}
