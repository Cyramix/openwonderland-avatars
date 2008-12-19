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
import imi.scene.shader.dynamic.GLSLVertexAttribute;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Vector4f;
import org.jdesktop.mtgame.WorldManager;

/**
 * This shader is used for clothing. It allows for multitexturing (base and
 * pattern) and color modulation of both layers independently.
 * @author Ronald E Dahlgren
 */
public class ClothingShader extends BaseShaderProgram implements AbstractShaderProgram
{
    private static final Logger logger = Logger.getLogger(ClothingShader.class.getName());
    // The following two strings are the default source code for this effect
    private static final String VertexSource = new String(
        "attribute vec4 boneIndices;" +
        "attribute vec3 tangent;" +
        "uniform mat4 pose[55];" +
        "varying vec3 ToLight; " +
        "varying vec3 position;" +
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
        "	vec3 Normal;" +
        "	Normal.x = dot (gl_Normal, poseBlend[0].xyz);" +
        "    	Normal.y = dot (gl_Normal, poseBlend[1].xyz);" +
        "    	Normal.z = dot (gl_Normal, poseBlend[2].xyz);" +
        "	vec3 TangentVec;" +
        "	TangentVec.x = dot (tangent, poseBlend[0].xyz);" +
        "    	TangentVec.y = dot (tangent, poseBlend[1].xyz);" +
        "    	TangentVec.z = dot (tangent, poseBlend[2].xyz);" +
        " 	vec3 binormal = normalize(cross(TangentVec, Normal));" +
        "	mat3 TBNMatrix = mat3(TangentVec, binormal, Normal); " +
        "  	ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;" +
        "  	ToLight *= TBNMatrix;  " +
        "}"
    );
    private static final String FragmentSource = new String(
        "varying vec3 ToLight;" +
        "varying vec3 position;" +
        "uniform sampler2D   BaseDiffuseMapIndex;" +
        "uniform sampler2D   PatternDiffuseMapIndex;" +
        "uniform sampler2D   NormalMapIndex;" +
        "uniform sampler2D   SpecularMapIndex;" +
        "uniform vec3 baseColor;" +
        "uniform vec3 patternColor;" +
        "uniform float SpecularPower;" +
        "void main(void)" +
        "{" +
        "    	vec4 texColor       = texture2D(BaseDiffuseMapIndex, gl_TexCoord[0].st);" +
        "       vec4 patternTexColor   = texture2D(PatternDiffuseMapIndex, gl_TexCoord[0].st);" +
        "       patternTexColor *= vec4(patternColor, 1);" +
        "       patternTexColor *= patternTexColor.a;" +
        "    	vec4 normalMapValue = texture2D(NormalMapIndex, gl_TexCoord[0].st, 0.5);" +
        "    	vec3 normal      = normalize(normalMapValue.xyz * 2.0 - 1.0);" +
        "	    vec3 lightVector = normalize(ToLight);" +
        "  	    float nxDir = max(0.0, dot(normal, lightVector));" +
        "  	    vec4 diffuse = texColor * (gl_LightSource[0].diffuse * nxDir);" +
        "       diffuse *= vec4(baseColor, 1);" +
        "       diffuse += patternTexColor;" +
        "	    vec4 color = diffuse * 0.75 + texColor * 0.25;" +
        "	    color = clamp(color, 0.0, 1.0);" +
        "	    color.a = 1.0;" +
        "    	gl_FragColor = vec4(0,1,0,1);//color;" +
        "}"
    );
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public ClothingShader(WorldManager wm)
    {
        this(wm, 0.2f);
    }

    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public ClothingShader(WorldManager wm, float fAmbientPower)
    {
        super(wm, VertexSource, FragmentSource);
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "The clothing shader. It allows for multitexturing to facilitate " +
                "the concept of a base and a pattern diffuse map for the clothing. " +
                "Furthermore, both of these maps may be modulated by a specified color."
                ));

        float[] redColor = new float[] {1, 0.3f, 0.3f};
        float[] blueColor = new float[] {0.3f, 0.3f, 1};
        try
        {
            // Put the properties into the property map
            m_propertyMap.put("ambientPower", new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            m_propertyMap.put("BaseDiffuseMapIndex", new ShaderProperty("BaseDiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            m_propertyMap.put("PatternDiffuseMapIndex", new ShaderProperty("PatternDiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
            m_propertyMap.put("NormalMapIndex", new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
            m_propertyMap.put("baseColor", new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, redColor));
            m_propertyMap.put("patternColor", new ShaderProperty("patternColor", GLSLDataType.GLSL_VEC3, blueColor));
            
            // Set the ambient power and the diffuse map texture unit
            // May not need the stuff below here since we included values above
//            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
//            setProperty(new ShaderProperty("BaseDiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
//            setProperty(new ShaderProperty("PatternDiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
//            setProperty(new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
//            setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC4, new Vector4f()));
//            setProperty(new ShaderProperty("patternColor", GLSLDataType.GLSL_VEC4, Integer.valueOf(2)));
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

        shaderState.setAttributePointer(
                    GLSLDefaultVariables.Tangents.getName(),// The name, referenced in the shader code
                    3,                                      // Total size of the data
                    false,                                  // "Normalized"
                    0,                                      // The "stride" (between entries)
                    meshInst.getGeometry().getGeometry().getTangentBuffer()); // The actual data
        
        meshInst.setShaderState(shaderState);
        m_bShaderLoaded = false;
        return true;
    }

    public xmlShaderProgram generateShaderProgramDOM() {
        xmlShaderProgram result = new xmlShaderProgram();
        result.setDefaultProgramName(ClothingShader.class.getName());
        return result;
    }

    

}
