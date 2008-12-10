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

import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.MeshColorModulation;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.utils.PMathUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 * @author Shawn Kendall
 */
public class EyeBall extends PPolygonSkinnedMeshInstance
{
    private PPolygonModelInstance modelInst  = null;
    
    private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f);
    
    private float limitCone = 0.57f;
    private float yScale    = 2.0f;
    
    private boolean bInCone = false;
    private EyeBall otherEye = null;
    
    public EyeBall(PPolygonSkinnedMeshInstance meshInstance, PPolygonModelInstance modelInst, PScene pscene)
    {
        super(meshInstance, pscene);
        this.modelInst = modelInst;
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

    void applyShader(WorldManager wm) {
        // change textures to not use mip maps... leads to freaky eyeballs
        PMeshMaterial myMaterial = getMaterialRef().getMaterial();
        myMaterial.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
        
//        GLSLShaderProgram shader = new VertDeformerWithSpecAndNormalMap(wm);
//        shader.addEffect(new MeshColorModulation());
//
//        try {
//            shader.compile();
//            float[] matColor = new float[3];
//            matColor[0] = 0.4f;
//            matColor[1] = 1.0f;
//            matColor[2] = 0.4f;
//            shader.setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(0.45f)));
//            shader.setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
//            shader.setProperty(new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
//            shader.setProperty(new ShaderProperty("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
//            shader.setProperty(new ShaderProperty("specularExponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(32.0f)));
//            shader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, matColor));
//        } catch (GLSLCompileException ex) {
//            Logger.getLogger(EyeBall.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchPropertyException ex) {
//            Logger.getLogger(EyeBall.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        myMaterial.setShader(shader);
        applyMaterial();
    }
}
