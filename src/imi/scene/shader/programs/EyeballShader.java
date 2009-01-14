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
package imi.scene.shader.programs;

import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.BaseShaderProgram;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.ShaderUtils;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Create an eyeball effect. This is simply a single texture with specular.
 * @author Ronald E Dahlgren
 */
public class EyeballShader extends BaseShaderProgram implements AbstractShaderProgram, Serializable
{
    // The following two strings are the default source code for this effect
    private static final String VertexSource = new String(
        "attribute vec4 boneIndices;" +
        "uniform mat4 pose[55];" +
        "varying vec3 ToLight; " +
        "varying vec3 position;" +
        "varying vec3 VNormal;" +
        "void main(void)" +
        "{" +
        "    	gl_TexCoord[0] = gl_MultiTexCoord0; " +
        "    	vec3 weight = vec4(gl_Color).rgb;" +
        "    	float weight4 = 1.0 - ( weight.x + weight.y + weight.z);" +
        "    	mat4 poseBlend = (  (pose[int(boneIndices.x)]) * weight.x + " +
        "                            (pose[int(boneIndices.y)]) * weight.y + " +
        "                            (pose[int(boneIndices.z)]) * weight.z +" +
        "                            (pose[int(boneIndices.w)]) * weight4     );" +
        "    	vec4 pos = gl_Vertex * poseBlend;" +
        "    	position = gl_Vertex.xyz;" +
        "    	gl_Position = gl_ModelViewProjectionMatrix * pos;" +
        "	    VNormal.x = dot (gl_Normal, poseBlend[0].xyz);" +
        "    	VNormal.y = dot (gl_Normal, poseBlend[1].xyz);" +
        "    	VNormal.z = dot (gl_Normal, poseBlend[2].xyz);" +
        "  	    ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;" +
        "       position = (gl_ModelViewMatrix * gl_Vertex).xyz;" +
        "}"
    );

    private static final String FragmentSource = new String(
        "varying vec3 ToLight;" +
        "varying vec3 position;" +
        "varying vec3 VNormal;" +
        "uniform sampler2D   BaseDiffuseMapIndex;" +
        "uniform float SpecularComponent;" +
        "uniform float SpecularExponent;" +
        "uniform float ambientPower;" +
        "void main(void)" +
        "{" +
        "    	vec4 texColor          = texture2D(BaseDiffuseMapIndex, gl_TexCoord[0].st);" +
        "    	vec3 normal      = normalize(VNormal);" +
        "	    vec3 lightVector = normalize(ToLight);" +
        "  	    float nxDir = max(0.0, dot(normal, lightVector));" +
        "  	    vec4 diffuse = texColor * (gl_LightSource[0].diffuse * nxDir);" +
        "	    vec4 color = diffuse * (1.0 - ambientPower) + texColor * ambientPower;" +
        "	    color = clamp(color, 0.0, 1.0);" +
        "	    color.a = 1.0;" +
        "       float RDotV = dot(normalize((reflect(-lightVector, normal))), normalize(vec3(-position)));" +
        "       vec4 specular = gl_LightSource[0].specular * pow(max(0.0, RDotV), SpecularExponent);" +
        "    	gl_FragColor = color + (specular * SpecularComponent);" +
        "}"
    );

    public EyeballShader(WorldManager wm)
    {
        this(wm, 0.4f, 32.0f);
    }

    public EyeballShader(WorldManager wm,
                         float fAmbientPower)
    {
        this(wm, fAmbientPower, 32.0f);
    }

    protected EyeballShader(EyeballShader other) {
        super(other);
    }


    public EyeballShader(WorldManager wm,
                         float fAmbientPower,
                         float specularExponent)
    {
        super(wm, VertexSource, FragmentSource);
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "The Eyeball shader."
                ));

        try
        {
            // Put the properties into the property map
            m_propertyMap.put("ambientPower",           new ShaderProperty("ambientPower",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            m_propertyMap.put("SpecularComponent",    new ShaderProperty("SpecularComponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(1.0f)));
            m_propertyMap.put("SpecularExponent",    new ShaderProperty("SpecularExponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(32.0f)));
            m_propertyMap.put("BaseDiffuseMapIndex",    new ShaderProperty("BaseDiffuseMapIndex",
                                GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            // Vertex deformer default
            m_propertyMap.put("pose", new ShaderProperty("pose", GLSLDataType.GLSL_VOID, null));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Override
    public boolean applyToMesh(PPolygonMeshInstance meshInst) {
        if (m_WM == null) // No world manager!
            return false;

        GLSLShaderObjectsState shaderState =
                (GLSLShaderObjectsState) m_WM.getRenderManager().createRendererState(RenderState.RS_GLSL_SHADER_OBJECTS);

        m_bShaderLoaded = false;
        blockUntilLoaded(shaderState);
        // apply uniforms
        ShaderUtils.assignProperties(m_propertyMap.values(), shaderState);

        shaderState.setAttributePointer(
                GLSLDefaultVariables.BoneIndices.getName(), // The name, referenced in the shader code
                4,                                          // Total size of the data
                false,                                      // "Normalized"
                0,                                          // The "stride" (between entries)
                ((PPolygonSkinnedMesh)meshInst.getGeometry()).getBoneIndexBuffer()); // The actual data

        meshInst.setShaderState(shaderState);
        m_bShaderLoaded = false;
        return true;
    }

    public xmlShaderProgram generateShaderProgramDOM() {
        xmlShaderProgram result = new xmlShaderProgram();
        result.setDefaultProgramName(EyeballShader.class.getName());
        return result;
    }
    
    public AbstractShaderProgram duplicate() {
        EyeballShader result = new EyeballShader(this);
        return result;
    }
}
