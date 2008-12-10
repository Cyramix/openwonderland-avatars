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
import java.util.ArrayList;

/**
 * This effect assigns Position to gl_Vertex, it is essentially the same as the
 * default initialization code for Position.
 * @author Ronald E Dahlgren
 */
public class VertexToPosition_Transform extends GLSLShaderEffect
{
    /**
     * Create a new instance
     */
    public VertexToPosition_Transform()
    {
        initializeDefaults();
    }
    
    /**
     * Set up the default behaviors
     */
    private void initializeDefaults()
    {
        // give me a name!
        m_effectName = new String("SimpleFTransform_Transfom");
        // and the description
        m_effectDescription = new String(
                "This effect initializes the position to gl_Vertex."
                );
        
        // allocate the globals
        m_vertexGlobals = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.Position;
        
        // declare dependencies, modifications, and initializations
        m_VertexInitializations = new ArrayList<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.Position);
        
        // Only vertex logic here
        createVertexLogic();
    }
    
    /**
     * Create the vertex source
     */
    private void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        vertexLogic.append(m_vertexGlobals[0].getName() + " = gl_Vertex;" + NL);
        m_vertexLogic = vertexLogic.toString();
    }
}
