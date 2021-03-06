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


import imi.shader.dynamic.GLSLDefaultVariables;
import imi.shader.dynamic.GLSLShaderEffect;
import imi.shader.dynamic.GLSLShaderUniform;
import imi.shader.dynamic.GLSLShaderVariable;
import javolution.util.FastTable;

/**
 * This effect performs a simple texture lookup to the Diffuse Map and applies
 * that color to FinalFragmentColor
 * @author Ronald E Dahlgren
 */
public class UnlitTexturing_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a new instance
     */
    public UnlitTexturing_Lighting()
    {
        initializeDefaults();
    }
    
    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = "UnlitTexturing_Lighting";
        // Set a description
        
        // allocate the globals we need
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FinalFragmentColor;
        
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = GLSLDefaultVariables.DiffuseMap;
        
        // declare dependencies, modifications, and initializations
        m_FragmentInitializations = new FastTable<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.FinalFragmentColor);
        
        createVertexLogic();
        createFragmentLogic();
        
        
    }
    
    /**
     * Create the fragment source
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        // initialize frag color
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = texture2D(" + m_fragmentUniforms[0].getName() + ", gl_TexCoord[0].st);" + NL);
        
        m_fragmentLogic = fragmentLogic.toString();
    }
    
    /**
     * Create the vertex portion of the source code
     */
    private void createVertexLogic()
    {
        // generate texture coordinates
        m_vertexLogic = "gl_TexCoord[0] = gl_MultiTexCoord0;" + NL;
    }
}
