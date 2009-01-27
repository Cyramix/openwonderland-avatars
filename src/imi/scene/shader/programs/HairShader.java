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
 * This shader is used for clothing. It allows for multitexturing (base and
 * pattern) and color modulation of both layers independently.
 * @author Ronald E Dahlgren
 */
public class HairShader extends BaseShaderProgram implements AbstractShaderProgram, Serializable
{
    private static final Logger logger = Logger.getLogger(ClothingShaderSpecColor.class.getName());
    // The following two strings are the default source code for this effect
    private static final String VertexSource = new String(
        "attribute vec3 tangent;" +
        "varying vec3 ToLight; " +
        "varying vec3 position;" +
        "void main(void)" +
        "{" +
        "    	gl_TexCoord[0] = gl_MultiTexCoord0; " +
        "    	position = gl_Vertex.xyz;" +
        "    	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;" +
        " 	    vec3 binormal = normalize(cross(tangent, gl_Normal));" +
        "	    mat3 TBNMatrix = mat3(tangent, binormal, gl_Normal); " +
        "  	    ToLight = (gl_ModelViewMatrixInverse * gl_LightSource[0].position).xyz - position;" +
        "  	    ToLight *= TBNMatrix;  " +
        "       position = (gl_ModelViewMatrix * gl_Vertex).xyz;" +
        "       position *= TBNMatrix;" +
        "}"
    );
    private static final String FragmentSource = new String(
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
        "       vec4 specular = vec4(specColor, 1.0);" +
        "       specular *= gl_LightSource[0].specular * pow(max(0.0, RDotV), SpecularExponent);" +
        "    	gl_FragColor = color + (specular * SpecularComponent);" +
        "}"
    );
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public HairShader(WorldManager wm)
    {
        this(wm, 0.2f);
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
        super(wm, VertexSource, FragmentSource);
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "The clothing shader. It allows for multitexturing to facilitate " +
                "the concept of a base and a pattern diffuse map for the clothing. " +
                "Furthermore, both of these maps may be modulated by a specified color."
                ));

        float[] whiteColor = new float[] {1, 1, 1 };
        float[] specColor = new float[] {1, 1, 1 };
        try
        {
            // Put the properties into the property map
            m_propertyMap.put("ambientPower",           new ShaderProperty("ambientPower",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            m_propertyMap.put("SpecularComponent",    new ShaderProperty("SpecularComponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(0.24f)));
            m_propertyMap.put("SpecularExponent",    new ShaderProperty("SpecularExponent",
                                GLSLDataType.GLSL_FLOAT, Float.valueOf(1.8f)));
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
                    GLSLDefaultVariables.Tangents.getName(),// The name, referenced in the shader code
                    3,                                      // Total size of the data
                    false,                                  // "Normalized"
                    0,                                      // The "stride" (between entries)
                    meshInst.getGeometry().getGeometry().getTangentBuffer()); // The actual data

        meshInst.setShaderState(shaderState);
        m_bShaderLoaded = false;
        return true;
    }

    @Override
    public xmlShaderProgram generateShaderProgramDOM() {
        xmlShaderProgram result = new xmlShaderProgram();
        result.setDefaultProgramName(ClothingShaderSpecColor.class.getName());
        return result;
    }

    @Override
    public AbstractShaderProgram duplicate()
    {
        HairShader result = new HairShader(this);
        return result;
    }

}
