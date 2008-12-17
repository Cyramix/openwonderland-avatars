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
import imi.scene.shader.dynamic.GLSLShaderUniform;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import imi.scene.shader.dynamic.GLSLVertexAttribute;
import java.util.ArrayList;

/**
 * This effect exposes the globally available poseBlend matrix to initialize
 * the vectors used for transforming normals.
 * @author Ronald E Dahlgren
 */
class ExposePoseBlendAsVarying_Accessor extends GLSLShaderEffect
{
    /**
     * Construct a brand new instance!
     */
    public ExposePoseBlendAsVarying_Accessor()
    {
        initializeDefaults();
    }

    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // give me a name!
        m_effectName = new String("ExposePoseBlendAsVarying_Accessor");
        // describe the effect
        m_effectDescription = new String(
                "This effect exposes the upper 3x3 of the poseBlend matrix " +
                "as a varying in order to provide access to it within fragment " +
                "shaders that find it necessary."
                );

        // allocate attributes
        m_vertAttributes    = new GLSLVertexAttribute[1];
        m_vertAttributes[0] = GLSLDefaultVariables.BoneIndices;
        // allocate uniforms
        m_vertexUniforms    = new GLSLShaderUniform[1];
        m_vertexUniforms[0] = GLSLDefaultVariables.Pose;
        // allocate the globals
        m_vertexGlobals    = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.PoseBlend;

        // allocate variants
        m_varying = new GLSLShaderVarying[3];
        m_varying[0] = new GLSLShaderVarying("poseBlend0", GLSLDataType.GLSL_VEC3);
        m_varying[1] = new GLSLShaderVarying("poseBlend1", GLSLDataType.GLSL_VEC3);
        m_varying[2] = new GLSLShaderVarying("poseBlend2", GLSLDataType.GLSL_VEC3);


        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new ArrayList<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.PoseBlend);

        m_VertexInitializations = new ArrayList<GLSLShaderVariable>();
        m_VertexInitializations.add(m_varying[0]);
        m_VertexInitializations.add(m_varying[1]);
        m_VertexInitializations.add(m_varying[2]);

        // Only vertex logic here
        createVertexLogic();
    }
    void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        // set up the varying
        vertexLogic.append(m_varying[0].getName() + "= " + m_vertexGlobals[0].getName() + "[0].xyz;" + NL);
        vertexLogic.append(m_varying[1].getName() + "= " + m_vertexGlobals[0].getName() + "[1].xyz;" + NL);
        vertexLogic.append(m_varying[2].getName() + "= " + m_vertexGlobals[0].getName() + "[2].xyz;" + NL);

        m_vertexLogic = vertexLogic.toString();
    }

}
