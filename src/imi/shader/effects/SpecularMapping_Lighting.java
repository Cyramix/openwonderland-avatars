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
package imi.shader.effects;

import imi.shader.dynamic.GLSLDataType;
import imi.shader.dynamic.GLSLDefaultVariables;
import imi.shader.dynamic.GLSLShaderEffect;
import imi.shader.dynamic.GLSLShaderUniform;
import imi.shader.dynamic.GLSLShaderVariable;
import imi.shader.dynamic.GLSLShaderVarying;
import javolution.util.FastTable;

/**
 * This effect is responsible for performing specular mapping, given a specular map
 * and an exponent value.
 * @author Ronald E Dahlgren
 */
public class SpecularMapping_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a new instance.
     */
    public SpecularMapping_Lighting()
    {
        initializeDefaults();
    }

    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = "SpecularMapping_Lighting";
        // set the description
        m_effectDescription = 
                "This effect generates a ToCamera vector that is used in the " +
                "fragment portion to calculate the specular contribution. Specular " +
                "intensity is determined by the (required) specular map.";
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[2];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;
        m_fragmentGlobals[1] = GLSLDefaultVariables.FinalFragmentColor;
        

        // allocate uniforms we use or expose
        m_fragmentUniforms = new GLSLShaderUniform[3];
        m_fragmentUniforms[0] = GLSLDefaultVariables.SpecularMap;
        m_fragmentUniforms[1] = new GLSLShaderUniform("specularExponent", GLSLDataType.GLSL_FLOAT);
        m_fragmentUniforms[2] = new GLSLShaderUniform("specularComponent", GLSLDataType.GLSL_FLOAT);
        
        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new FastTable<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.TBNMatrix);

        m_VertexInitializations = new FastTable<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.ToCamera);
        
        m_varying = new GLSLShaderVarying[2];
        m_varying[0] = GLSLDefaultVariables.ToCamera;
        m_varying[1] = GLSLDefaultVariables.ToLight;

        m_FragmentModifications = new FastTable<GLSLShaderVariable>();
        m_FragmentModifications.add(GLSLDefaultVariables.FinalFragmentColor);

        m_FragmentDependencies = new FastTable<GLSLShaderVariable>();
        m_FragmentDependencies.add(GLSLDefaultVariables.FragmentLocalNormal);
        m_FragmentDependencies.add(GLSLDefaultVariables.NdotL);

        createVertexLogic();
        createFragmentLogic();
    }

    /**
     * Create the vertex logic for this piece
     */
    private void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        /**
         * ToCamera = (gl_ModelViewProjectionMatrix * vec4(0,0,1,1)).xyz
         * // Transform into texture space
         * ToCamera *= TBNMatrix;
         */
        vertexLogic.append(m_VertexInitializations.get(0).getName() + " = (gl_ModelViewProjectionMatrix * vec4(0,0,1,1)).xyz;" + NL);
        vertexLogic.append(m_VertexInitializations.get(0).getName() + " *= " + m_VertexDependencies.get(0).getName() + ";" + NL);
        m_vertexLogic = vertexLogic.toString();
    }

    /**
     * Create the fragment logic for this piece
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        /**
        // compute the specular term if NdotL is  larger than zero
        if (NdotL > 0.0) {

            // normalize the half-vector, and then compute the
            // cosine (dot product) with the normal
            NdotHV = max(dot(normal, gl_LightSource[0].halfVector.xyz),0.0);
            specular = gl_FrontMaterial.specular * gl_LightSource[0].specular *
                    pow(NdotHV,gl_FrontMaterial.shininess);
        } */
        fragmentLogic.append("float NdotHV;");
        fragmentLogic.append("if (NdotL > 0.0) {" +
                " NdotHV = max(dot(" + m_FragmentDependencies.get(0).getName() + ", gl_LightSource[0].halfVector.xyz), 0.0);" + NL +
                " vec4 specularComponent = gl_FrontMaterial.specular * gl_LightSource[0].specular *" + NL +
                "       pow(NdotHV, gl_FrontMaterial.shininess);" + NL +
                "};" + NL);
//        /**
//         * vec4 specColor     = texture2D(SpecularMapIndex, gl_TexCoord[0].st);
//         * vec3 camVector = normalize(ToCamera);
//         * vec3 reflection = normalize(reflect(lightVector , normal));
//         * vec4 specular = (gl_LightSource[0].specular * pow(max(dot(reflection, camVector),0.0), SpecularPower));
//         * specColor *=  specular;
//         * finalFragColor += specColor
//         */
//        fragmentLogic.append("vec3 camVector = normalize(" + m_varying[0].getName() + ");" + NL);
//        fragmentLogic.append("vec3 lightVec  = " + m_varying[1].normalize() + ";" + NL);
//        fragmentLogic.append("vec3 reflectionVector = normalize(reflect(lightVec, " + m_FragmentDependencies.get(0).getName() + "));" + NL);
//        fragmentLogic.append("vec4 specularComponent = gl_LightSource[0].specular * pow(max(dot(reflectionVector, camVector), 0.0), " + m_fragmentUniforms[1].getName() + ");" + NL);
//        fragmentLogic.append("specularComponent *= texture2D(" + m_fragmentUniforms[0].getName() + ", gl_TexCoord[0].st);" + NL);
//        fragmentLogic.append(m_FragmentModifications.get(0).getName() + " += (specularComponent * " + m_fragmentUniforms[2].getName() + ");" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
}
