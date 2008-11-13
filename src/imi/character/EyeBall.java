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

import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;

/**
 *
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 */
public class EyeBall extends PPolygonSkinnedMeshInstance
{
    public EyeBall(PPolygonSkinnedMeshInstance meshInstance, PScene pscene)
    {
        super(meshInstance, pscene);
    }
    
    @Override
    protected void postAnimationMatrixModifier(PMatrix matrix, int index)
    {
        matrix.setIdentity();
    }
    
//    private void performEyeballLookAt(Vector3f targetInWorldSpace)
//    {
//        // ensure that we have a character, and that the character has a skeleton
//        if (character == null || character.getSkeleton() == null)
//            return; // try again later
//        // grab the appropriate joints to look at
//        final String leftEyeballJointName = "leftEye";
//        final String rightEyeballJointName = "rightEye";
//
//        SkinnedMeshJoint leftEyeJoint = character.getSkeleton().findSkinnedMeshJoint(leftEyeballJointName);
//        SkinnedMeshJoint rightEyeJoint = character.getSkeleton().findSkinnedMeshJoint(rightEyeballJointName);
//
//        // Perform lookAt to target
//        // Left eyeball
//        PMatrix leftEyeWorldXForm = PMathUtils.lookAt(
//                targetInWorldSpace,
//                leftEyeJoint.getTransform().getWorldMatrix(false).getTranslation(),
//                Vector3f.UNIT_Y);
//        leftEyeJoint.getTransform().getWorldMatrix(true).set(leftEyeWorldXForm);
//        leftEyeJoint.getTransform().setDirtyWorldMat(false);
//        // Right eyeball
//        PMatrix rightEyeWorldXForm = PMathUtils.lookAt(
//                targetInWorldSpace,
//                rightEyeJoint.getTransform().getWorldMatrix(false).getTranslation(),
//                Vector3f.UNIT_Y);
//        rightEyeJoint.getTransform().getWorldMatrix(true).set(rightEyeWorldXForm);
//        rightEyeJoint.getTransform().setDirtyWorldMat(false);
//    }
}
