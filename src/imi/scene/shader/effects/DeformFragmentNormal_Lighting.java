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
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import java.util.ArrayList;

/**
 * This effect transforms the fragment local normal by the
 * @author Ronald E Dahlgren
 */
class DeformFragmentNormal_Lighting extends GLSLShaderEffect
{
    /**
     * Construct a new instance.
     */
    public DeformFragmentNormal_Lighting()
    {
        initializeDefaults();
    }

    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = "DeformFragmentNormal_Lighting";
        // set the description
        m_effectDescription = new String(
                "This effect deforms the fragment-local normal by the current " +
                "animation pose."
                );
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;

        // declare dependencies, modifications, and initializations
        m_FragmentDependencies = new ArrayList<GLSLShaderVariable>();
        m_FragmentDependencies.add(new GLSLShaderVarying("poseBlend0", GLSLDataType.GLSL_VEC3));
        m_FragmentDependencies.add(new GLSLShaderVarying("poseBlend1", GLSLDataType.GLSL_VEC3));
        m_FragmentDependencies.add(new GLSLShaderVarying("poseBlend2", GLSLDataType.GLSL_VEC3));

        m_FragmentModifications = new ArrayList<GLSLShaderVariable>();
        m_FragmentModifications.add(GLSLDefaultVariables.FragmentLocalNormal);

        // Only fragment logic
        createFragmentLogic();
    }

    /**
     * Create the fragment logic for this piece
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append("vec3 normal = " + m_fragmentGlobals[0].getName() + ";" + NL);
        fragmentLogic.append(m_fragmentGlobals[0].getName() + ".x = dot(normal, " + m_FragmentDependencies.get(0).getName() + ");" + NL);
        fragmentLogic.append(m_fragmentGlobals[0].getName() + ".y = dot(normal, " + m_FragmentDependencies.get(1).getName() + ");" + NL);
        fragmentLogic.append(m_fragmentGlobals[0].getName() + ".z = dot(normal, " + m_FragmentDependencies.get(2).getName() + ");" + NL);
        // transform if a vertex deformer is being used
        m_fragmentLogic = fragmentLogic.toString();
    }
}
