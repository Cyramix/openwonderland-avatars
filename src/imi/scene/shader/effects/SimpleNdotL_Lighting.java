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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.scene.shader.effects;


import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderEffect;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import java.util.ArrayList;

/**
 * This is a simple N dot L lighting model that uses the value of the dot
 * product between the normal and a vector to the light to determine the
 * light intensity. This light intensity then modulates the color.
 * @author Ronald E Dahlgren
 */
public class SimpleNdotL_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a brand new instance!
     */
    public SimpleNdotL_Lighting()
    {
        initializeDefaults();
    }
    
    /**
     * Set up the default behaviro
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = new String("SimpleNdotL_Lighting");
        m_effectDescription = new String(
                "This effect uses the ToLight and VNormal vectors to calculate" +
                " the value of NdotL. This value is then used to modulate the" +
                " FinalFragmentColor. No ambient contribution is made."
                );
        
        // allocate the globals we need
        m_fragmentGlobals = new GLSLShaderVariable[3];
        m_fragmentGlobals[0] = GLSLDefaultVariables.NdotL;
        m_fragmentGlobals[1] = GLSLDefaultVariables.FinalFragmentColor;
        m_fragmentGlobals[2] = GLSLDefaultVariables.FragmentLocalNormal;
        
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
     * Create the fragment portion of the source code.
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        // normalize ToLight and VNormal
        fragmentLogic.append("vec3 lightVec  = " + m_varying[0].normalize() + ";" + NL);
        //fragmentLogic.append("vec3 normalVec = " + m_varying[1].normalize() + ";" + NL); <---- this is needed
        // calculate NdotL 
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = max(dot(" + m_fragmentGlobals[2].getName() + ", lightVec.xyz), 0.0);" + NL);
        // modify final frag color
        fragmentLogic.append(m_fragmentGlobals[1].getName() + " = clamp(" + m_fragmentGlobals[1].getName() + " * " + m_fragmentGlobals[0].getName() + ", 0.0, 1.0);" + NL);        
        m_fragmentLogic = fragmentLogic.toString();
    }
}
