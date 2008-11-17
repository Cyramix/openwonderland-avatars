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
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.utils.PMathUtils;

/**
 *
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 */
public class EyeBall extends PPolygonSkinnedMeshInstance
{
    protected PPolygonModelInstance m_modelInst  = null;
    
    public EyeBall(PPolygonSkinnedMeshInstance meshInstance, PPolygonModelInstance modelInst, PScene pscene)
    {
        super(meshInstance, pscene);
        m_modelInst = modelInst;
        applyMaterial();
    }
    
    @Override
    protected void postAnimationMatrixModifier(PMatrix matrix, PMatrix inverseBindPose, int index)
    {
//        PMatrix rot = new PMatrix();
//        rot.fromAngleAxis((float)Math.toRadians(90.0f), getTransform().getWorldMatrix(false).getLocalX());
//        rot.mul(inverseBindPose);
//        matrix.mul(rot, matrix);
        
//        Vector3f targetInWorldSpace = new Vector3f(0.0f, 0.0f, 0.0f);
//        //m_modelInst.getTransform().getWorldMatrix(false).transformPoint(targetInWorldSpace);
//        
//        // Perform lookAt to target
//        PMatrix eyeWorldXForm = PMathUtils.lookAt(
//                targetInWorldSpace,
//                m_modelInst.getTransform().getWorldMatrix(false).getTranslation(),
//                Vector3f.UNIT_Y);
//        matrix.set(eyeWorldXForm);
//        setDirty(true, true);
        
        //matrix.
    }
    
    private void performEyeballLookAt(Vector3f targetInWorldSpace)
    {
        // Perform lookAt to target
        PMatrix leftEyeWorldXForm = PMathUtils.lookAt(
                targetInWorldSpace,
                getTransform().getWorldMatrix(false).getTranslation(),
                Vector3f.UNIT_Y);
        getTransform().getWorldMatrix(true).set(leftEyeWorldXForm);
        getTransform().setDirtyWorldMat(false);
    }
}
