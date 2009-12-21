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
 */
public class HairShader extends BaseShaderProgram implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private static final Logger logger = Logger.getLogger(ClothingShaderSpecColor.class.getName());
    // The following two strings are the default source code for this effect
    private static final String VertexSource = 
        "varying vec3 ToLight; " +
        "varying vec3 position;" +
        "void main(void)" +
        "{" +
        "    	gl_TexCoord[0] = gl_MultiTexCoord0; " +
        "    	position = gl_Vertex.xyz;" +
        "    	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;" +
        "           vec3 sccopy = gl_SecondaryColor.rgb;" +
        " 	    vec3 binormal = normalize(cross(sccopy, gl_Normal));" +
        "	    mat3 TBNMatrix = mat3(gl_SecondaryColor.rgb, binormal, gl_Normal); " +
        "  	    ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;" +
        "  	    ToLight *= TBNMatrix;  " +
        "       position = (gl_ModelViewMatrix * gl_Vertex).xyz;" +
        "       position *= TBNMatrix;" +
        "}";

    private static final String FragmentSource = 
        "varying vec3 ToLight;" +
        "varying vec3 position;" +
        "uniform sampler2D   BaseDiffuseMapIndex;" +
        "uniform sampler2D   NormalMapIndex;" +
        "uniform vec3 materialColor;" +
        "uniform vec3 specColor;" +
        "uniform float SpecularComponent;" +
        "uniform float SpecularExponent;" +
        "uniform float ambientPower;" +
        "void main(void)" +
        "{" +
        "    	vec4 texColor          = texture2D(BaseDiffuseMapIndex, gl_TexCoord[0].st);" +
        "    	vec4 normalMapValue = texture2D(NormalMapIndex, gl_TexCoord[0].st, 0.5);" +
        "    	vec3 normal      = normalize(normalMapValue.xyz * 2.0 - 1.0);" +
        "	    vec3 lightVector = normalize(ToLight);" +
        "  	    float nxDir = max(0.0, dot(normal, lightVector));" +
        "       texColor *= vec4(materialColor, 1);" +
        "  	    vec4 diffuse = texColor * (gl_LightSource[0].diffuse * nxDir);" +
        "	    vec4 color = diffuse * (1.0 - ambientPower) + texColor * ambientPower;" +
        "	    color = clamp(color, 0.0, 1.0);" +
        "	    color.a = 1.0;" +
        "       float RDotV = dot(normalize((reflect(-lightVector, normal))), normalize(vec3(-position)));" +
        "       vec4 specular = diffuse;" + // vec4(specColor, 1);
        "       specular *= gl_LightSource[0].specular * pow(max(0.0, RDotV), SpecularExponent);" +
        "    	gl_FragColor = color + (specular * SpecularComponent);" +
        "}";
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public HairShader(WorldManager wm)
    {
        this(wm, 0.38f);
    }

    protected HairShader(HairShader other)
    {
        super(other);
    }

    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public HairShader(WorldManager wm, float fAmbientPower)
    {
        this(wm, fAmbientPower, 0.8f, 3.4f);
    }

    /**
     * Construct a new instance!
     * @param wm
     * @param fAmbientPower
     * @param fSpecularContribution
     * @param fSpecularExponent
     */
    public HairShader(WorldManager wm, float fAmbientPower, float fSpecularContribution, float fSpecularExponent)
    {
        super(wm, VertexSource, FragmentSource);
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(
                "The hair shader. Performs basic normal mapping with a specular" +
                "effect. A specular color may be set.");

        float[] whiteColor = new float[] {1, 1, 1 };
        float[] specColor = new float[] {150 / 255.0f, 75 / 255.0f, 0 };
        try
        {
            // Put the properties into the property map
            m_propertyMap.put("ambientPower",           new ShaderProperty("ambientPower",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            m_propertyMap.put("SpecularComponent",    new ShaderProperty("SpecularComponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fSpecularContribution)));
            m_propertyMap.put("SpecularExponent",    new ShaderProperty("SpecularExponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fSpecularExponent)));
            m_propertyMap.put("BaseDiffuseMapIndex",    new ShaderProperty("BaseDiffuseMapIndex",
                                GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            m_propertyMap.put("NormalMapIndex",         new ShaderProperty("NormalMapIndex",
                                GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
            m_propertyMap.put("materialColor",              new ShaderProperty("materialColor",
                                GLSLDataType.GLSL_VEC3, whiteColor));
            m_propertyMap.put("specColor",           new ShaderProperty("specColor",
                                GLSLDataType.GLSL_VEC3, specColor));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }


    @Override
    public xmlShaderProgram generateShaderProgramDOM() {
        xmlShaderProgram result = new xmlShaderProgram();
        result.setDefaultProgramName(HairShader.class.getName());
        return result;
    }

    @Override
    public AbstractShaderProgram duplicate()
    {
        HairShader result = new HairShader(this);
        return result;
    }

}
