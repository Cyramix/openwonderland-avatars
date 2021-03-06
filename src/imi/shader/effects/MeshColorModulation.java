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
import javolution.util.FastTable;

/**
 *
 * @author Ronald E Dahlgren
 */
public class MeshColorModulation extends GLSLShaderEffect
{
    public MeshColorModulation()
    {
        initializeDefaults();
    }

    private void initializeDefaults()
    {
        // set our name
        m_effectName = "MeshColorModulation";
        // optionally set a description
        m_effectDescription = "This effect uses a provided uniform " +
                "to modulate the final fragment color.";
        // declare uniforms we expose
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = new GLSLShaderUniform("materialColor", GLSLDataType.GLSL_VEC3);
        // declare globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FinalFragmentColor;

        // declare dependencies, modifications, and initializations
        m_FragmentModifications = new FastTable<GLSLShaderVariable>();
        m_FragmentModifications.add(GLSLDefaultVariables.FinalFragmentColor);

        createVertexLogic();
        createFragmentLogic();
    }

    /**
     * Create the fragment source
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " *= vec4(" + m_fragmentUniforms[0].getName() + ",1.0);" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }

    /**
     * Create the vertex portion of the source code
     * TODO: Expose the requested set of texture coordinates
     */
    private void createVertexLogic()
    {
        m_vertexLogic = "";
    }
}
