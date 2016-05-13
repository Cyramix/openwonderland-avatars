/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
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
package imi.shader.programs;

import imi.shader.AbstractShaderProgram;
import imi.shader.BaseShaderProgram;
import imi.shader.ShaderProperty;
import imi.shader.dynamic.GLSLDataType;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This shader is used for clothing. It allows for multitexturing (base and
 * pattern) and color modulation of both layers independently.
 * @author Ronald E Dahlgren
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class ClothingShaderDiffuseAsSpec extends BaseShaderProgram implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private static final Logger logger = Logger.getLogger(ClothingShaderSpecColor.class.getName());
    // The following two strings are the default source code for this effect
    // Consider all the lightsource
    private static final String VertexSource = 
        "attribute vec4 boneIndices;" +
        "uniform mat4 pose[55];" +
        "varying vec3 ToLight; " +
            "varying vec3 ToLight1; " +
            "varying vec3 ToLight2; " +
        "varying vec3 position;" +
        "void main(void)" +
        "{" +
        "    	gl_TexCoord[0] = gl_MultiTexCoord0; " +
        "    	vec3 weight = gl_Color.rgb;" +
        "    	float weight4 = 1.0 - ( weight.x + weight.y + weight.z);" +
        "    	mat4 poseBlend = (  (pose[int(boneIndices.x)]) * weight.x + " +
        "                            (pose[int(boneIndices.y)]) * weight.y + " +
        "                            (pose[int(boneIndices.z)]) * weight.z +" +
        "                            (pose[int(boneIndices.w)]) * weight4     );" +
        "    	vec4 pos = gl_Vertex * poseBlend;" +
        "    	position = gl_Vertex.xyz;" +
        "    	gl_Position = gl_ModelViewProjectionMatrix * pos;" +
        "	    vec3 Normal;" +
        "	    Normal.x = dot (gl_Normal, poseBlend[0].xyz);" +
        "    	Normal.y = dot (gl_Normal, poseBlend[1].xyz);" +
        "    	Normal.z = dot (gl_Normal, poseBlend[2].xyz);" +
        "	    vec3 TangentVec;" +
        "	    TangentVec.x = dot (gl_SecondaryColor.rgb, poseBlend[0].xyz);" +
        "    	TangentVec.y = dot (gl_SecondaryColor.rgb, poseBlend[1].xyz);" +
        "    	TangentVec.z = dot (gl_SecondaryColor.rgb, poseBlend[2].xyz);" +
        " 	    vec3 binormal = normalize(cross(TangentVec, Normal));" +
        "	    mat3 TBNMatrix = mat3(TangentVec, binormal, Normal); " +
        "  	    ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;" +
        "  	    ToLight *= TBNMatrix;  " +
            "  	    ToLight1 = (gl_ModelViewMatrixInverse * gl_LightSource[1].position).xyz - position;" +
        "  	    ToLight1 *= TBNMatrix;  " +
            "  	    ToLight2 = (gl_ModelViewMatrixInverse * gl_LightSource[2].position).xyz - position;" +
        "  	    ToLight2 *= TBNMatrix;  " +
        "       position = (gl_ModelViewMatrix * gl_Vertex).xyz;" +
        "       position *= TBNMatrix;" +
        "}";
    private static final String FragmentSource = 
        "varying vec3 ToLight;" +
            "varying vec3 ToLight1;" +
            "varying vec3 ToLight2;" +
        "varying vec3 position;" +
        "uniform sampler2D   BaseDiffuseMapIndex;" +
        "uniform sampler2D   NormalMapIndex;" +
        "uniform vec3 baseColor;" +
        "uniform float SpecularComponent;" +
        "uniform float SpecularExponent;" +
        "uniform float ambientPower;" +
        "void main(void)" +
        "{" +
        "    	vec4 texColor          = texture2D(BaseDiffuseMapIndex, gl_TexCoord[0].st);" +
        "    	vec4 normalMapValue = texture2D(NormalMapIndex, gl_TexCoord[0].st, 0.5);" +
        "    	vec3 normal      = normalize(normalMapValue.xyz * 2.0 - 1.0);" +
        "	    vec3 lightVector = normalize(ToLight);" +
            "	    vec3 lightVector1 = normalize(ToLight1);" +
            "	    vec3 lightVector2 = normalize(ToLight2);" +
        "  	    float nxDir = max(max(0.0, dot(normal, lightVector)),max(max(0.0, dot(normal, lightVector1)), max(0.0, dot(normal, lightVector2))));" +
        "           if(dot(normal, lightVector1) > dot(normal, lightVector) && dot(normal, lightVector1) > dot(normal, lightVector2)) {" +
        "               nxDir = nxDir * 0.7; } " +
        "       texColor *= vec4(baseColor, 1);" +
        "  	    vec4 diffuse = texColor * (gl_LightSource[0].diffuse * nxDir);" +
        "	    vec4 color = diffuse * (1.0 - ambientPower) + texColor * ambientPower;" +
        "	    color = clamp(color, 0.0, 1.0);" +
        "	    color.a = 1.0;" +
        "       float RDotV = dot(normalize((reflect(-lightVector, normal))), normalize(vec3(-position)));" +
            "       float RDotV1 = dot(normalize((reflect(-lightVector1, normal))), normalize(vec3(-position)));" +
            "       float RDotV2 = dot(normalize((reflect(-lightVector2, normal))), normalize(vec3(-position)));" +
            "RDotV = max(max(RDotV, RDotV1), RDotV2);" +
        "       vec4 specular = texColor * gl_LightSource[0].specular * pow(max(0.0, RDotV), SpecularExponent);" +
        "    	gl_FragColor = color + (specular * SpecularComponent * nxDir);" +
//        "    	gl_FragColor = vec4(ToLight.x,ToLight.y,ToLight.z,1);/**color + (specular * SpecularComponent * nxDir);**/" +
        "}";
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public ClothingShaderDiffuseAsSpec(WorldManager wm)
    {
        this(wm, 0.35f);
    }

    protected ClothingShaderDiffuseAsSpec(ClothingShaderDiffuseAsSpec other)
    {
        super(other);
    }

    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public ClothingShaderDiffuseAsSpec(WorldManager wm, float fAmbientPower)
    {
        super(wm, VertexSource, FragmentSource);
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(
                "The clothing shader. It allows for multitexturing to facilitate " +
                "the concept of a base and a pattern diffuse map for the clothing. " +
                "Furthermore, both of these maps may be modulated by a specified color.");

        float[] whiteColor = new float[] {1, 1, 1 };
        try
        {
            // Put the properties into the property map
            m_propertyMap.put("ambientPower",           new ShaderProperty("ambientPower",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            m_propertyMap.put("SpecularComponent",    new ShaderProperty("SpecularComponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(0.44f)));
            m_propertyMap.put("SpecularExponent",    new ShaderProperty("SpecularExponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(1.8f)));
            m_propertyMap.put("BaseDiffuseMapIndex",    new ShaderProperty("BaseDiffuseMapIndex",
                                GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            m_propertyMap.put("NormalMapIndex",         new ShaderProperty("NormalMapIndex",
                                GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
            m_propertyMap.put("baseColor",              new ShaderProperty("baseColor",
                                GLSLDataType.GLSL_VEC3, whiteColor));
            // Vertex deformer default
            m_propertyMap.put("pose", new ShaderProperty("pose", GLSLDataType.GLSL_VOID, null));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public xmlShaderProgram generateShaderProgramDOM() {
        xmlShaderProgram result = new xmlShaderProgram();
        result.setDefaultProgramName(ClothingShaderSpecColor.class.getName());
        return result;
    }

    public AbstractShaderProgram duplicate()
    {
        ClothingShaderDiffuseAsSpec result = new ClothingShaderDiffuseAsSpec(this);
        return result;
    }

}
