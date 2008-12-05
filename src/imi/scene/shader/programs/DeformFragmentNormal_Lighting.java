/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.scene.shader.programs;

import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderEffect;
import imi.scene.shader.dynamic.GLSLShaderUniform;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import imi.scene.shader.dynamic.GLSLVertexAttribute;
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
        m_effectName = new String("DeformFragmentNormal_Lighting");
        // set the description
        m_effectDescription = new String(
                "This effect deforms the fragment-local normal by the current " +
                "animation pose."
                );
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;

        // declare dependencies, modifications, and initializations
        m_vertAttributes = new GLSLVertexAttribute[1];
        m_vertAttributes[0] = GLSLDefaultVariables.Tangents;

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
