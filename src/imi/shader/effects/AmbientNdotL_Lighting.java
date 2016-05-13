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
 * This effect calculates light intensity based on the dot product of the
 * normal and a vector to the light. This is used to modulate the color, and
 * then an ambient contribution is mixed in.
 * @author Ronald E Dahlgren
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class AmbientNdotL_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a new instance
     */
    public AmbientNdotL_Lighting()
    {
        initializeDefaults();
    }
    
    /**
     * Load up the default values for this effect
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = "AmbientNdotL_Lighting";
        // set the description
        m_effectDescription = 
                "This effect calculates the light intensity for a given fragment" +
                " using the surface normal and a vector to the light source. This" +
                " effect has a dependency to FragmentLocalNormal in order to play" +
                " nicely with other effects that may modify it from the varying" +
                " input, such as normal mapping.";
                
        // allocate the globals we need
        m_fragmentGlobals = new GLSLShaderVariable[3];
        m_fragmentGlobals[0] = GLSLDefaultVariables.NdotL;
        m_fragmentGlobals[1] = GLSLDefaultVariables.FinalFragmentColor;
        m_fragmentGlobals[2] = GLSLDefaultVariables.FragmentLocalNormal;
        
        // uniforms we expose
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = new GLSLShaderUniform("ambientPower", GLSLDataType.GLSL_FLOAT);
        
        // variants that are referenced
        m_varying = new GLSLShaderVarying[4];
        m_varying[0] = GLSLDefaultVariables.ToLight;
        m_varying[1] = GLSLDefaultVariables.VNormal;
        m_varying[2] = GLSLDefaultVariables.ToLight1;
        m_varying[3] = GLSLDefaultVariables.ToLight2;
        
        m_vertexGlobals = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.Position;
        
        // declare dependencies, modifications, and initializations
        m_FragmentDependencies = new FastTable<GLSLShaderVariable>();
        m_FragmentDependencies.add(GLSLDefaultVariables.FinalFragmentColor);
        m_FragmentDependencies.add(GLSLDefaultVariables.ToLight);
        m_FragmentDependencies.add(GLSLDefaultVariables.VNormal); // just in case
        m_FragmentDependencies.add(GLSLDefaultVariables.FragmentLocalNormal);
        
        
        m_FragmentModifications = new FastTable<GLSLShaderVariable>();
        m_FragmentModifications.add(GLSLDefaultVariables.FinalFragmentColor);
        
        m_FragmentInitializations = new FastTable<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.NdotL);
        
        // this effect only has a fragment portion
        createFragmentLogic();
        
        
    }
    
    /**
     * Build the text of the fragment source, always using indirection to get
     * variable names rather than explicitely writing them.
     * Consider all the lightsource
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append("float alpha = " + m_fragmentGlobals[1].getName() + ".a;" + NL);
        // normalize ToLight and VNormal
        fragmentLogic.append("vec3 lightVec  = " + m_varying[0].normalize() + ";" + NL);
        fragmentLogic.append("vec3 lightVec1  = " + m_varying[2].normalize() + ";" + NL);
        fragmentLogic.append("vec3 lightVec2  = " + m_varying[3].normalize() + ";" + NL);
        fragmentLogic.append("vec3 normalVec = " + m_varying[1].normalize() + ";" + NL);
        // calculate NdotL 
//        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = clamp(dot(" + m_fragmentGlobals[2].getName() + ", lightVec), 0.0, 1.0);" + NL);
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = max(max(0.0, dot(normalVec.xyz, lightVec.xyz)),max(max(0.0, dot(normalVec.xyz, lightVec1.xyz)),max(0.0, dot(normalVec.xyz, lightVec2.xyz))));" + NL);
//        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = " + m_fragmentGlobals[0].getName() + " + max(0.0, dot(normalVec.xyz, lightVec1.xyz));" + NL);
//        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = " + m_fragmentGlobals[0].getName() + " + max(0.0, dot(normalVec.xyz, lightVec2.xyz));" + NL);
        // modify final frag color
        fragmentLogic.append("vec4 afterLighting = " + m_fragmentGlobals[1].getName() + " * " + m_fragmentGlobals[0].getName() + ";" + NL);
        fragmentLogic.append("afterLighting *= (1.0 - " + m_fragmentUniforms[0].getName() + ");" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + " = " + m_fragmentGlobals[1].getName() + " * " + m_fragmentUniforms[0].getName() + ";" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + " += afterLighting;" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + ".a = alpha;" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
}
