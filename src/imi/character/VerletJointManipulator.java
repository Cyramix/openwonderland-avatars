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
package imi.character;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PostAnimationJointManipulator;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;

/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class VerletJointManipulator implements PostAnimationJointManipulator
{
    private final int shoulder = 18; // -0.25008845 (bind pose x values)
    private final int upperArm = 37; // -0.38327518 // 0.1331867 distance between shoulder and upperArm
    private final int elbow    = 43; // -0.49928188 // 0.2491934 distance between shoulder and elbow
    private final int foreArm  = 46; // -0.5855795  // 0.0862977 distance between elbow and forArm
    private final int wrist    = 48; // -0.73043364 // 0.1448541 distance between the elbow and the wrist
    
    private VerletArm       arm         = null;
    private SkeletonNode    skeleton    = null;
    
    private Vector3f localX = new Vector3f();
    private PMatrix  wristAnimationDelta = new PMatrix();
    
    public VerletJointManipulator(VerletArm verletArm, SkeletonNode skeletonNode)
    {
        arm      = verletArm;
        skeleton = skeletonNode;
    }
    
    public void postAnimationModifiedMeshSpaceMatrixHook(PMatrix matrix, int jointIndex) 
    {
        switch (jointIndex)
        {
            case shoulder:
                modifyShoulder(matrix);
                break;
            case upperArm:
                modifyUpperArm(matrix);
                break;
            case elbow:
                modifyElbow(matrix);
                break;
            case foreArm:
                modifyForeArm(matrix);
                break;
            case wrist:
                modifyWrist(matrix);
                break;
            // Catch all hand cases
            case 51: case 52: case 58: case 59: case 60: case 61: case 62:
            case 68: case 69: case 70: case 71: case 72: case 78: case 79:
            case 80: case 81: case 82: case 87: case 88: case 89: case 90:
                modifyHand(matrix);
                break;
        }
    }

    private void modifyShoulder(PMatrix matrix) 
    {
        SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(shoulder);
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
        arm.calculateInverseModelWorldMatrix().transformPoint(elbowPosition);
        Vector3f localY = elbowPosition.subtract(shoulderPosition).normalize();
        localX.set(shoulderJoint.getMeshSpace().getLocalXNormalized());
        Vector3f localZ = localX.cross(localY).normalize();
        localX.set(localY.cross(localZ).normalize());
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(shoulderPosition);
        matrix.mul(skeleton.getJointLocalModifier(shoulder));
    }

    private void modifyUpperArm(PMatrix matrix) 
    {
        SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(shoulder);
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
        arm.calculateInverseModelWorldMatrix().transformPoint(elbowPosition);
        Vector3f offsetFromShoulder = elbowPosition.subtract(shoulderPosition).normalize();
        
        // The Y-Axis is aligned with the shoulder offset
        Vector3f localY = new Vector3f(offsetFromShoulder);
        
        offsetFromShoulder.multLocal(0.1331867f);
        
        // generate the local Z axis
        Vector3f localZ = localX.cross(localY).normalize();
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(shoulderPosition.add(offsetFromShoulder));
        matrix.mul(skeleton.getJointLocalModifier(upperArm));
    }
    
    private void modifyElbow(PMatrix matrix) 
    {   
        Vector3f elbowPosition = new Vector3f(arm.getElbowPosition());
        arm.calculateInverseModelWorldMatrix().transformPoint(elbowPosition);
        
        // The Y-Axis is aligned with the shoulder offset
        SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(shoulder);
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        Vector3f localY   = elbowPosition.subtract(shoulderPosition).normalize();
        // generate the local Z axis
        Vector3f localZ = localX.cross(localY).normalize();
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(elbowPosition);
        matrix.mul(skeleton.getJointLocalModifier(elbow));
    }

    private void modifyForeArm(PMatrix matrix) 
    {
        PMatrix elbowMatrix = new PMatrix();
        modifyElbow(elbowMatrix);
        
        matrix.set(elbowMatrix);
        matrix.mul(skeleton.getSkinnedMeshJoint(foreArm).getBindPose());
        
        ////////////////////////////////////////////////////////////////////
        
//        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
//        Vector3f wristPosition    = new Vector3f(arm.getWristPosition());
//        PMatrix inverseModelWorldMatrix = arm.calculateInverseModelWorldMatrix();
//        inverseModelWorldMatrix.transformPoint(elbowPosition);
//        inverseModelWorldMatrix.transformPoint(wristPosition);
//        Vector3f offsetFromElbow   = wristPosition.subtract(elbowPosition).normalize();
//        
//        // The Y-Axis is aligned with the elbow offset
//        Vector3f localY = new Vector3f(offsetFromElbow);
//        
//        offsetFromElbow.multLocal(0.1331867f);
//        
//        // generate the local Z axis
//        Vector3f localZ = localX.cross(localY).normalize();
//        
//        matrix.setLocalX(localX);
//        matrix.setLocalY(localY);
//        matrix.setLocalZ(localZ);
//        matrix.setTranslation(elbowPosition.add(offsetFromElbow));
//        matrix.mul(skeleton.getJointLocalModifier(foreArm));
    }

    private void modifyWrist(PMatrix matrix) 
    {
        wristAnimationDelta.set(matrix.inverse());
        
        PMatrix inverseModelWorldMatrix = arm.calculateInverseModelWorldMatrix();
        
        Vector3f wristPosition = new Vector3f(arm.getWristPosition());
        inverseModelWorldMatrix.transformPoint(wristPosition);
        
        // The Y-Axis is aligned with the elbow offset
        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
        inverseModelWorldMatrix.transformPoint(elbowPosition);
        Vector3f offsetFromElbow   = wristPosition.subtract(elbowPosition).normalize();
        Vector3f localY = new Vector3f(offsetFromElbow);
                
        // generate the local Z axis
        Vector3f localZ = localX.cross(localY).normalize();
        
        matrix.setLocalX(localX);
        matrix.setLocalY(localY);
        matrix.setLocalZ(localZ);
        matrix.setTranslation(wristPosition);
        matrix.mul(skeleton.getJointLocalModifier(wrist));
        
        
//        PMatrix fixRotation = new PMatrix();
//        fixRotation.buildRotationZ((float)Math.toRadians(45));
//        fixRotation.mul(matrix, fixRotation);
//        wristAnimationDelta.mul(fixRotation, wristAnimationDelta);   
        
        wristAnimationDelta.mul(matrix, wristAnimationDelta);   
    }
    
    private void modifyHand(PMatrix matrix)
    {
        matrix.mul(wristAnimationDelta, matrix);
    }

}
