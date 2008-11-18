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
    private PPolygonModelInstance modelInst  = null;
    
    private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f);
    
    private float limitCone = 0.57f;
    private float yScale    = 1.0f;
    
    private boolean bInCone = false;
    private EyeBall otherEye = null;
    
    public EyeBall(PPolygonSkinnedMeshInstance meshInstance, PPolygonModelInstance modelInst, PScene pscene)
    {
        super(meshInstance, pscene);
        this.modelInst = modelInst;
        applyMaterial();
    }
    
    @Override
    protected void postAnimationModifiedMeshSpaceMatrixHook(PMatrix matrix, int jointIndex) 
    {
        PMatrix modelWorldRef = modelInst.getTransform().getWorldMatrix(false);
        
        // Get eye world space
        PMatrix eyeWorld = new PMatrix();
        eyeWorld.mul(modelWorldRef, matrix);
        
        // Check limits
        Vector3f forwardVec = modelWorldRef.getLocalZ();
        Vector3f directionToTarget = target.subtract(eyeWorld.getTranslation());
        directionToTarget.y *= yScale;
        directionToTarget.normalizeLocal();

        // Check if inside the cone
        float dot = directionToTarget.dot(forwardVec);
        if (dot > limitCone)
        {
            bInCone = true;
            if (otherEye.isInCone())
            {
                // Perform lookAt to target
                Vector3f scale = matrix.getScaleVector();
                PMatrix eyeWorldXForm = PMathUtils.lookAt(
                        target,
                        eyeWorld.getTranslation(),
                        Vector3f.UNIT_Y);
                matrix.set(eyeWorldXForm);
                matrix.setScale(scale);

                matrix.mul(modelWorldRef.inverse(), matrix);
            }
        }
        else
            bInCone = false;
        
    }

    public Vector3f getTarget() {
        return target;
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public PPolygonModelInstance getModelInst() {
        return modelInst;
    }

    public void setOtherEye(EyeBall otherOne) {
        otherEye = otherOne;
    }
    
    public boolean isInCone()
    {
        return bInCone;
    }
}
