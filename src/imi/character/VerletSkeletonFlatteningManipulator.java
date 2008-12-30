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
import imi.scene.polygonmodel.parts.skinned.SkeletonFlatteningManipulator;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;

/**
 *
 * @author Lou Hayt
 */
public class VerletSkeletonFlatteningManipulator implements SkeletonFlatteningManipulator
{
    private final int rightShoulder = 18; // -0.25008845 (bind pose x values)
    private final int rightUpperArm = 37; // -0.38327518 // 0.1331867 distance between shoulder and upperArm
    private final int rightElbow    = 43; // -0.49928188 // 0.2491934 distance between shoulder and elbow
    private final int rightForeArm  = 46; // -0.5855795  // 0.0862977 distance between elbow and forArm
    private final int rightWrist    = 48; // -0.73043364 // 0.1448541 distance between the elbow and the wrist
    
    private final int leftShoulder  = 16;
    private final int leftUpperArm  = 21;
    private final int leftElbow     = 39;
    private final int leftForeArm   = 44;
    private final int leftWrist     = 47;
    
    private final float distanceFromShoulderToUpperArm = 0.1331867f;
    private final float distanceFromElbowToForeArm     = 0.0862977f;
    
    private final int rightEye      = 36;
    private final int leftEye       = 35;
    
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

    private Vector3f rightShoulderLocalX = new Vector3f();
    private Vector3f leftShoulderLocalX  = new Vector3f();
    
    public VerletSkeletonFlatteningManipulator(VerletArm leftVerletArm, VerletArm rightVerletArm, EyeBall left, EyeBall right, SkeletonNode skeletonNode,  PPolygonModelInstance modelInstance)
    {
        leftEyeBall  = left;
        rightEyeBall = right;
        rightArm     = rightVerletArm;
        leftArm      = leftVerletArm;
        characterModelInst = modelInstance;
        skeleton = skeletonNode;
        skeleton.setFlatteningHook(this);
    }
    
    public void processSkeletonNode(PNode current) 
    {   
        if (!(current instanceof SkinnedMeshJoint))
            return;
        
        SkinnedMeshJoint joint = (SkinnedMeshJoint)current;
        PMatrix matrix = joint.getMeshSpace();
        int jointIndex = skeleton.getSkinnedMeshJointIndex(joint);
        
        switch (jointIndex)
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
        
        switch (jointIndex)
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
            case rightForeArm:
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
            case leftForeArm:
                if (leftArmEnabled)
                    modifyForeArm(matrix, false);
                break;
            case leftWrist:
                if (leftArmEnabled)
                    modifyWrist(matrix, false);
                break;
                
                
//            // Catch all right hand cases
//            case 51: case 52: case 58: case 59: case 60: case 61: case 62:
//            case 68: case 69: case 70: case 71: case 72: case 78: case 79:
//            case 80: case 81: case 82: case 87: case 88: case 89: case 90:
//                modifyHand(matrix);
//                break;
        }
    }

    private void modifyShoulder(PMatrix matrix, boolean right) 
    {
        if (right)
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(rightShoulder);
            Vector3f         elbowPosition = new Vector3f(rightArm.getElbowPosition());
            modifyShoulder(matrix, shoulderJoint, elbowPosition, rightShoulderLocalX);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(leftShoulder);
            Vector3f         elbowPosition = new Vector3f(leftArm.getElbowPosition());
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
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(rightShoulder);
            Vector3f         elbowPosition = new Vector3f(rightArm.getElbowPosition());
            modifyUpperArm(matrix, shoulderJoint, elbowPosition, rightShoulderLocalX);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(leftShoulder);
            Vector3f         elbowPosition = new Vector3f(leftArm.getElbowPosition());
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
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(rightShoulder);
            Vector3f elbowPosition = new Vector3f(rightArm.getElbowPosition());
            Vector3f wristPosition = new Vector3f(rightArm.getWristPosition());
            modifyElbow(matrix, shoulderJoint, elbowPosition, wristPosition);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(leftShoulder);
            Vector3f elbowPosition = new Vector3f(leftArm.getElbowPosition());
            Vector3f wristPosition = new Vector3f(leftArm.getWristPosition());
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
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(rightShoulder);
            Vector3f elbowPosition = new Vector3f(rightArm.getElbowPosition());
            Vector3f wristPosition = new Vector3f(rightArm.getWristPosition());
            modifyForeArm(matrix, shoulderJoint, elbowPosition, wristPosition);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(leftShoulder);
            Vector3f elbowPosition = new Vector3f(leftArm.getElbowPosition());
            Vector3f wristPosition = new Vector3f(leftArm.getWristPosition());
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
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(rightShoulder);
            Vector3f wristPosition = new Vector3f(rightArm.getWristPosition());
            modifyWrist(matrix, shoulderJoint, wristPosition, right);
        }
        else
        {
            SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(leftShoulder);
            Vector3f wristPosition = new Vector3f(leftArm.getWristPosition());
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
            matrix.mul(fixRotation);
        }
        else
        {
            PMatrix fixRotation = new PMatrix();
            fixRotation.buildRotationZ((float)Math.toRadians(-40));
            matrix.mul(fixRotation);   
        }
    }
    
    public void setArmsEnabled(boolean bEnabled) {
        this.armsEnabled = bEnabled;
    }
    
    public boolean isArmsEnabled() {
        return this.armsEnabled;
    }

    public boolean isLeftArmEnabled() {
        return leftArmEnabled;
    }

    public void setLeftArmEnabled(boolean leftArmEnabled) {
        this.leftArmEnabled = leftArmEnabled;
        if (rightArmEnabled || leftArmEnabled)
            armsEnabled = true;
        else
            armsEnabled = false;
    }

    public boolean isRightArmEnabled() {
        return rightArmEnabled;
    }

    public void setRightArmEnabled(boolean rightArmEnabled) {
        this.rightArmEnabled = rightArmEnabled;
        if (rightArmEnabled || leftArmEnabled)
            armsEnabled = true;
        else
            armsEnabled = false;
    }

    public void setManualDriveReachUp(boolean manualDriveReachUp) 
    {
        this.manualDriveReachUp = manualDriveReachUp;
    }
    
    private PMatrix calculateInverseModelWorldMatrix()
    {
        return characterModelInst.getTransform().getWorldMatrix(false).inverse();
    }

    public EyeBall getLeftEyeBall() {
        return leftEyeBall;
    }

    public void setLeftEyeBall(EyeBall leftEyeBall) {
        this.leftEyeBall = leftEyeBall;
    }

    public EyeBall getRightEyeBall() {
        return rightEyeBall;
    }

    public void setRightEyeBall(EyeBall rightEyeBall) {
        this.rightEyeBall = rightEyeBall;
    }

}
