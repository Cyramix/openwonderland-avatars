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
import javolution.util.FastTable;

/**
 * Creates a random texture. The noise function may cause shader fallbacks on
 * NVidia hardware. Because of this, a different approach is used.
 * ********** THIS CLASS IS INCOMPLETE - DO NOT USE *****************8
 * @author Ronald E Dahlgren
 */
public class NoiseTexturing extends GLSLShaderEffect
{
    public NoiseTexturing()
    {
        initializeDefaults();
    }

    private void initializeDefaults()
    {
        // set our name
        m_effectName = "NoiseTexturing";
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FinalFragmentColor;
        // allocate uniforms we use or expose
        // declare dependencies, modifications, and initializations
        m_FragmentInitializations = new FastTable<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.FinalFragmentColor);

        createVertexLogic();
        createFragmentLogic();
    }

    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = vec4(noise3(), 1.0);" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
    
    private void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        m_vertexLogic = vertexLogic.toString();
    }
}
