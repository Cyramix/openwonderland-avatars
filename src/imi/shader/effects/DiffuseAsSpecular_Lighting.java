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
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class DiffuseAsSpecular_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a new instance.
     */
    public DiffuseAsSpecular_Lighting()
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
                "fragment portion to calculate the specular contribution.";
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[2];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;
        m_fragmentGlobals[1] = GLSLDefaultVariables.FinalFragmentColor;


        // allocate uniforms we use or expose
        m_fragmentUniforms = new GLSLShaderUniform[3];
        m_fragmentUniforms[0] = new GLSLShaderUniform("specularExponent", GLSLDataType.GLSL_FLOAT);
        m_fragmentUniforms[1] = new GLSLShaderUniform("specularComponent", GLSLDataType.GLSL_FLOAT);
        m_fragmentUniforms[2] = GLSLDefaultVariables.DiffuseMap;

        // declare dependencies, modifications, and initializations
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
         */
        vertexLogic.append(m_VertexInitializations.get(0).getName() + " = (gl_ModelViewProjectionMatrix * vec4(0,0,1,1)).xyz;" + NL);
        m_vertexLogic = vertexLogic.toString();
    }

    /**
     * Create the fragment logic for this piece
     * Consider all the lightsource
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();

        fragmentLogic.append("float NdotHV;");
        fragmentLogic.append("vec4 diffuseColor = texture2D(" + m_fragmentUniforms[2].getName() + ", gl_TexCoord[0].st);" + NL);
        fragmentLogic.append("if (NdotL > 0.0) {" +
                " NdotHV = max(dot(" + m_FragmentDependencies.get(0).getName() + ", gl_LightSource[0].halfVector.xyz), 0.0);" + NL +
                " NdotHV = max(NdotHV, max(dot(" + m_FragmentDependencies.get(0).getName() + ", gl_LightSource[1].halfVector.xyz), 0.0));" + NL +
                " NdotHV = max(NdotHV, max(dot(" + m_FragmentDependencies.get(0).getName() + ", gl_LightSource[2].halfVector.xyz), 0.0));" + NL +
                " vec4 specularComponent = diffuseColor * gl_LightSource[0].specular *" + NL +
                "       pow(NdotHV, " + m_fragmentUniforms[0].getName() +");" + NL +
                "};" + NL);
//        fragmentLogic.append("vec3 camVector = normalize(" + m_varying[0].getName() + ");" + NL);
//        fragmentLogic.append("vec3 lightVec  = " + m_varying[1].normalize() + ";" + NL);
//        fragmentLogic.append("vec3 reflectionVector = normalize(reflect(lightVec, " + m_FragmentDependencies.get(0).getName() + "));" + NL);
//        fragmentLogic.append("vec4 specularComponent = gl_LightSource[0].specular * pow(max(dot(reflectionVector, camVector), 0.0), " + m_fragmentUniforms[1].getName() + ");" + NL);
//        fragmentLogic.append("specularComponent *= texture2D(" + m_fragmentUniforms[2].getName() + ", gl_TexCoord[0].st);" + NL);
//        fragmentLogic.append(m_FragmentModifications.get(0).getName() + " += (specularComponent * " + m_fragmentUniforms[1].getName() + ");" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
}
