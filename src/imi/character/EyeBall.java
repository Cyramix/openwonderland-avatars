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
import imi.utils.PMathUtils;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides eyeball behavior by wrapping the mesh and setting particular
 * attributes on it. The EyeBall class is capable of tracking a point within
 * constraints (to avoid unnatural gazing angles).
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 * @author Shawn Kendall
 */
public class EyeBall extends PPolygonSkinnedMeshInstance
{
    /** The Character who owns this eyeball **/
    private Character character = null;
    /** The Model Instance that owns this eyeball **/
    private PPolygonModelInstance modelInst  = null;
    /** World space coordinates of the view target **/
    private Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f);

    /** Used as a limiting factor to restrict the range of motion of the eyeball **/
    private float limitCone = 0.57f;
    /**
     * This is used to warp space such that the eye's rotational range of motion
     * is more ellipsoid rather than circular. Humans have a broader range
     * horizontally than they do vertically.
     **/
    private float yScale    = 2.0f;
    
    private boolean bInCone = false;
    private EyeBall otherEye = null;
    
    /**
     * Construct a new eyeball using the provided mesh as the eye mesh, the model
     * instance provided is the eyeball's owner, and the pscene containing both.
     * @param meshInstance The eyeball mesh
     * @param modelInst The owning model
     * @param pscene The owning pscene
     */
    public EyeBall(PPolygonSkinnedMeshInstance meshInstance, Character character)
    {
        super(meshInstance, character.getPScene(), false); // Material will be applied later
        this.modelInst = character.getModelInst();
        this.character = character;
    }

    /**
     * Performs the eyeball lookAt behavior.
     * @param matrix The matrix being modified
     * @param jointIndex Joint to modify
     */
    protected void lookAtTarget(PMatrix matrix)
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

        //boolean inConeCheck = bInCone;
        
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
        {
            bInCone = false;
        }
        
//        if (inConeCheck != bInCone)
//            System.out.println("Now in cone: " + bInCone + "  target: " + target + "   direction: " + directionToTarget);
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

    /**
     * Package private method to apply particular shading effects to the eyeball
     * instance.
     * @param wm
     */
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
