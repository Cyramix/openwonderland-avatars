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

import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderEffect;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import java.util.ArrayList;

/**
 * This effect calculates the vector from Position (so the order matters!)
 * to the gl_LightSource position specified by the uniform. This effect does
 * not use the TBN matrix
 * @author Ronald E Dahlgren
 */
public class CalculateToLight_Lighting extends GLSLShaderEffect
{
    public CalculateToLight_Lighting()
    {
        initializeDefaults();
    }
    
    
    private void initializeDefaults()
    {
        // set the name
        m_effectName = new String("CalculateToLight_Lighting");
        // allocate the globals we will be using
        m_vertexGlobals = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.Position;
        
        m_varying = new GLSLShaderVarying[1];
        m_varying[0] = GLSLDefaultVariables.ToLight;
        
        // TODO: expose the light number uniform
        
        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new ArrayList<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.Position);
        
        m_VertexInitializations = new ArrayList<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.ToLight);
        
        createVertexLogic();
    }
    
    
    private void createVertexLogic()
    {
        m_vertexLogic = new String(
                m_varying[0].getName() + " = (gl_ModelViewMatrixInverse * (gl_LightSource[" +
                "0" +
                "].position - " + m_vertexGlobals[0].getName() + ")).xyz;" + NL
                //"].position).xyz - " + GLSLDefaultVariables.Position.getName() + ".xyz;" + NL
                );
    }
}
