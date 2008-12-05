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
 * $Revision$
 * $Date$
 * $State$
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
 * This effect is used to perform vertex deformation based on the "pose" 
 * uniform (set through a special mechanism).
 * @author Ronald E Dahlgren
 */
public class VertexDeformer_Transform extends GLSLShaderEffect
{
    /**
     * Construct a brand new instance!
     */
    public VertexDeformer_Transform()
    {
        initializeDefaults();
    }
    
    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // give me a name!
        m_effectName = new String("VertexDeformer_Transform");
        // describe the effect
        m_effectDescription = new String(
                "This effect calculates and initializes the PoseBlend" +
                " matrix from the pose uniform and the bone indices  provided" +
                " by BoneIndices. The position and normal are then transformed" +
                " into the appropriate pose."
                );
        
        // allocate attributes
        m_vertAttributes    = new GLSLVertexAttribute[1];
        m_vertAttributes[0] = GLSLDefaultVariables.BoneIndices;
        // allocate uniforms
        m_vertexUniforms    = new GLSLShaderUniform[1];
        m_vertexUniforms[0] = GLSLDefaultVariables.Pose;
        // allocate the globals
        m_vertexGlobals    = new GLSLShaderVariable[2];
        m_vertexGlobals[0] = GLSLDefaultVariables.Position;
        m_vertexGlobals[1] = GLSLDefaultVariables.PoseBlend;
        // allocate variants
        m_varying = new GLSLShaderVarying[1];
        m_varying[0] = GLSLDefaultVariables.VNormal;

        
        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new ArrayList<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.Position);
        
        m_VertexModifications = new ArrayList<GLSLShaderVariable>();
        m_VertexModifications.add(GLSLDefaultVariables.Position);
        m_VertexModifications.add(GLSLDefaultVariables.VNormal);
        
        m_VertexInitializations = new ArrayList<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.PoseBlend);
        m_VertexInitializations.add(GLSLDefaultVariables.VNormal);
        
        // Only vertex logic here
        createVertexLogic();
    }
    
    /**
     * Create the vertex portion of the source code
     */
    private void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        // WARNING
        // references to "pose" are hardcoded here for readability 
        // don't do this in your code! =)
        // Grab weghts
        vertexLogic.append("vec3 weight = gl_Color.rgb;" + NL);
        // calculate 4th weight
        vertexLogic.append("float weight4 = 1.0 - (weight.x + weight.y + weight.z);" + NL);
        // initialize the poseBlend matrix
        vertexLogic.append(m_vertexGlobals[1].getName() + " = " +
                "(pose[int(" + m_vertAttributes[0].getName() + ".x)]) * weight.x +" + NL +
                "(pose[int(" + m_vertAttributes[0].getName() + ".y)]) * weight.y +" + NL +
                "(pose[int(" + m_vertAttributes[0].getName() + ".z)]) * weight.z +" + NL +
                "(pose[int(" + m_vertAttributes[0].getName() + ".w)]) * weight4;" + NL
                );
        // transform normal
        vertexLogic.append(m_varying[0].getName() + ".x = dot(gl_Normal, " + m_vertexGlobals[1].getName() + "[0].xyz);" + NL);
        vertexLogic.append(m_varying[0].getName() + ".y = dot(gl_Normal, " + m_vertexGlobals[1].getName() + "[1].xyz);" + NL);
        vertexLogic.append(m_varying[0].getName() + ".z = dot(gl_Normal, " + m_vertexGlobals[1].getName() + "[2].xyz);" + NL);
        // transform position
        vertexLogic.append(m_vertexGlobals[0].getName() + " = " + m_vertexGlobals[0].getName() + " * " + m_vertexGlobals[1].getName() + ";" + NL);
        m_vertexLogic = vertexLogic.toString();
    }
}
