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
    private int shoulder = 18; // -0.25008845 (bind pose x values)
    private int upperArm = 37; // -0.38327518 // 0.1331867 distance between shoulder and upperArm
    private int elbow    = 43; // -0.49928188 // 0.2491934 distance between shoulder and elbow
    private int foreArm  = 46; // -0.5855795  // 0.0862977 distance between elbow and forArm
    private int wrist    = 48; // -0.73043364 // 0.1448541 distance between the elbow and the wrist
    
    private VerletArm       arm         = null;
    private SkeletonNode    skeleton    = null;
    
    public VerletJointManipulator(VerletArm verletArm, SkeletonNode skeletonNode)
    {
        arm      = verletArm;
        skeleton = skeletonNode;
    }
    
    public void postAnimationModifiedMeshSpaceMatrixHook(PMatrix matrix, int jointIndex) 
    {
        switch (jointIndex)
        {
            case 37:
                modifyUpperArm(matrix);
                break;
            case 43:
                modifyElbow(matrix);
                break;
            case 46:
                modifyForeArm(matrix);
                break;
            case 48:
                modifyWrist(matrix);
                break;
        }
    }

    private void modifyUpperArm(PMatrix matrix) 
    {
        SkinnedMeshJoint shoulderJoint = skeleton.getSkinnedMeshJoint(shoulder);
        Vector3f shoulderPosition = shoulderJoint.getMeshSpace().getTranslation();
        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
        arm.getInverseModelWorldMatrix().transformPoint(elbowPosition);
        Vector3f offsetFromShoulder   = elbowPosition.subtract(shoulderPosition).normalize();
        offsetFromShoulder.multLocal(0.1331867f);
        
        matrix.set(shoulderJoint.getMeshSpace());
        matrix.setTranslation(shoulderPosition.add(offsetFromShoulder));
        matrix.mul(skeleton.getJointLocalModifier(upperArm));
    }
    
    private void modifyElbow(PMatrix matrix) 
    {
        matrix.set(skeleton.getSkinnedMeshJoint(shoulder).getMeshSpace());
        Vector3f elbowPosition = new Vector3f(arm.getElbowPosition());
        arm.getInverseModelWorldMatrix().transformPoint(elbowPosition);
        matrix.setTranslation(elbowPosition);
        matrix.mul(skeleton.getJointLocalModifier(elbow));
    }

    private void modifyForeArm(PMatrix matrix) 
    {
        Vector3f elbowPosition    = new Vector3f(arm.getElbowPosition());
        Vector3f wristPosition    = new Vector3f(arm.getWristPosition());
        arm.getInverseModelWorldMatrix().transformPoint(elbowPosition);
        arm.getInverseModelWorldMatrix().transformPoint(wristPosition);
        Vector3f offsetFromElbow   = wristPosition.subtract(elbowPosition).normalize();
        offsetFromElbow.multLocal(0.1331867f);
        
        matrix.set(skeleton.getSkinnedMeshJoint(shoulder).getMeshSpace());
        matrix.setTranslation(elbowPosition.add(offsetFromElbow));
        matrix.mul(skeleton.getJointLocalModifier(foreArm));
    }

    private void modifyWrist(PMatrix matrix) 
    {
        matrix.set(skeleton.getSkinnedMeshJoint(shoulder).getMeshSpace());
        Vector3f wristPosition = new Vector3f(arm.getWristPosition());
        arm.getInverseModelWorldMatrix().transformPoint(wristPosition);
        matrix.setTranslation(wristPosition);
        matrix.mul(skeleton.getJointLocalModifier(wrist));
    }

}
