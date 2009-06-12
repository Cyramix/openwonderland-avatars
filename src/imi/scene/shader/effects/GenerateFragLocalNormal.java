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
 * This efefct calculates the FragLocalNormal vector by simple assigning the
 * value of VNormal. The reason for wrapping such a micro bit of code into an
 * effect is to express the dependency on the varying VNormal in order to generate
 * a normal at the fragment level
 * @author Ronald E Dahlgren
 */
public class GenerateFragLocalNormal extends GLSLShaderEffect
{
    public GenerateFragLocalNormal()
    {
        initializeDefaults();
    }

    private void initializeDefaults()
    {
        // set our name
        m_effectName = "GenerateFragLocalNormal";
        // description
        m_effectDescription = "Assign the value of VNormal to FragLocalNormal";
        // declare globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;
        
        m_varying = new GLSLShaderVarying[1];
        m_varying[0] = GLSLDefaultVariables.VNormal;
        // declare uniforms we use or expose
        // declare dependencies, modifications, and initializations
        m_FragmentInitializations = new ArrayList<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.FragmentLocalNormal);
        
        m_FragmentDependencies = new ArrayList<GLSLShaderVariable>();
        m_FragmentDependencies.add(GLSLDefaultVariables.VNormal);

        createFragmentLogic();
    }
    
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = normalize(" + m_varying[0].getName() + ");" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
}
