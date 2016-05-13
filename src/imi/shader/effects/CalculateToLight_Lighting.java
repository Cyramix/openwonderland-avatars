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

import imi.shader.dynamic.GLSLDefaultVariables;
import imi.shader.dynamic.GLSLShaderEffect;
import imi.shader.dynamic.GLSLShaderVariable;
import imi.shader.dynamic.GLSLShaderVarying;
import javolution.util.FastTable;

/**
 * This effect calculates the vector from Position (so the order matters!)
 * to the gl_LightSource position specified by the uniform. This effect does
 * not use the TBN matrix
 * @author Ronald E Dahlgren
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
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
        m_effectName = "CalculateToLight_Lighting";
        // allocate the globals we will be using
        m_vertexGlobals = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.Position;
        
        m_varying = new GLSLShaderVarying[4];
        m_varying[0] = GLSLDefaultVariables.ToLight;
        m_varying[1] = GLSLDefaultVariables.VNormal;
        m_varying[2] = GLSLDefaultVariables.ToLight1;
        m_varying[3] = GLSLDefaultVariables.ToLight2;
        
        // TODO: expose the light number uniform
        
        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new FastTable<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.Position);
        
        m_VertexInitializations = new FastTable<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.ToLight);
        
        createVertexLogic();
    }
    
    // Consider all the lightsource
    private void createVertexLogic()
    {
        m_vertexLogic = m_varying[0].getName() + " = normalize(vec3((gl_ModelViewMatrixInverse * gl_LightSource[0].position) - "
                + m_vertexGlobals[0].getName() + "));" + NL;
        m_vertexLogic = m_vertexLogic + m_varying[2].getName() + " = normalize(vec3((gl_ModelViewMatrixInverse * gl_LightSource[1].position) - "
                + m_vertexGlobals[0].getName() + "));" + NL;
        m_vertexLogic = m_vertexLogic + m_varying[3].getName() + " = normalize(vec3((gl_ModelViewMatrixInverse * gl_LightSource[2].position) - "
                + m_vertexGlobals[0].getName() + "));" + NL;
    }
}
