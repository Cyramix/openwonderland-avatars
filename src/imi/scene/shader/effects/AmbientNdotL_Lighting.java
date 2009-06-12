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
package imi.scene.shader.effects;


import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderEffect;
import imi.scene.shader.dynamic.GLSLShaderUniform;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import java.util.ArrayList;

/**
 * This effect calculates light intensity based on the dot product of the
 * normal and a vector to the light. This is used to modulate the color, and
 * then an ambient contribution is mixed in.
 * @author Ronald E Dahlgren
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
        m_effectDescription = new String(
                "This effect calculates the light intensity for a given fragment" +
                " using the surface normal and a vector to the light source. This" +
                " effect has a dependency to FragmentLocalNormal in order to play" +
                " nicely with other effects that may modify it from the varying" +
                " input, such as normal mapping."
                );
                
        // allocate the globals we need
        m_fragmentGlobals = new GLSLShaderVariable[3];
        m_fragmentGlobals[0] = GLSLDefaultVariables.NdotL;
        m_fragmentGlobals[1] = GLSLDefaultVariables.FinalFragmentColor;
        m_fragmentGlobals[2] = GLSLDefaultVariables.FragmentLocalNormal;
        
        // uniforms we expose
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = new GLSLShaderUniform("ambientPower", GLSLDataType.GLSL_FLOAT);
        
        // variants that are referenced
        m_varying = new GLSLShaderVarying[2];
        m_varying[0] = GLSLDefaultVariables.ToLight;
        m_varying[1] = GLSLDefaultVariables.VNormal;
        
        // declare dependencies, modifications, and initializations
        m_FragmentDependencies = new ArrayList<GLSLShaderVariable>();
        m_FragmentDependencies.add(GLSLDefaultVariables.FinalFragmentColor);
        m_FragmentDependencies.add(GLSLDefaultVariables.ToLight);
        m_FragmentDependencies.add(GLSLDefaultVariables.VNormal); // just in case
        m_FragmentDependencies.add(GLSLDefaultVariables.FragmentLocalNormal);
        
        
        m_FragmentModifications = new ArrayList<GLSLShaderVariable>();
        m_FragmentModifications.add(GLSLDefaultVariables.FinalFragmentColor);
        
        m_FragmentInitializations = new ArrayList<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.NdotL);
        
        // this effect only has a fragment portion
        createFragmentLogic();
        
        
    }
    
    /**
     * Build the text of the fragment source, always using indirection to get
     * variable names rather than explicitely writing them.
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append("float alpha = " + m_fragmentGlobals[1].getName() + ".a;" + NL);
        // normalize ToLight and VNormal
        fragmentLogic.append("vec3 lightVec  = " + m_varying[0].normalize() + ";" + NL);
        //fragmentLogic.append("vec3 normalVec = " + m_varying[1].normalize() + ";" + NL); <-- this is necessary but currently problematic
        // calculate NdotL 
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = clamp(dot(" + m_fragmentGlobals[2].getName() + ", lightVec), 0.0, 1.0);" + NL);
        //fragmentLogic.append(m_fragmentGlobals[0].getName() + " = max(0.0, dot(normalVec.xyz, lightVec.xyz));" + NL); <-- this is the correct way
        // modify final frag color
        fragmentLogic.append("vec4 afterLighting = " + m_fragmentGlobals[1].getName() + " * " + m_fragmentGlobals[0].getName() + ";" + NL);
        fragmentLogic.append("afterLighting *= (1.0 - " + m_fragmentUniforms[0].getName() + ");" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + " = " + m_fragmentGlobals[1].getName() + " * " + m_fragmentUniforms[0].getName() + ";" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + " += afterLighting;" + NL);
        fragmentLogic.append(m_fragmentGlobals[1].getName() + ".a = alpha;" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
}
