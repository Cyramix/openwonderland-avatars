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
import imi.scene.shader.dynamic.GLSLShaderUniform;
import imi.scene.shader.dynamic.GLSLShaderVariable;
import imi.scene.shader.dynamic.GLSLShaderVarying;
import imi.scene.shader.dynamic.GLSLVertexAttribute;
import java.util.ArrayList;

/**
 * This effect is responsible for performing normal mapping, given a normal map
 * and the tangent vertex attribute.
 * @author Ronald E Dahlgren
 */
public class NormalMapping extends GLSLShaderEffect
{
    /**
     * Construct a new instance.
     */
    public NormalMapping()
    {
        initializeDefaults();
    }

    /**
     * Set up the default behavior
     */
    private void initializeDefaults()
    {
        // set our name
        m_effectName = "NormalMapping";
        // set the description
        m_effectDescription = new String(
                "This effect generates the Tangent-Binormal-Normal (TBN) matrix" +
                " using the tangent vertex attribute and gl_Normal. The ToLight" +
                " vector is then transformed into texture space before being" +
                " sent to the fragment shader. On the fragment side of things" +
                " the FragmentLocalNormal is initialized to the normal retrieved" +
                " from the normal map texture."
                );
        // allocate globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FragmentLocalNormal;

        m_vertexGlobals = new GLSLShaderVariable[1];
        m_vertexGlobals[0] = GLSLDefaultVariables.TBNMatrix;

        m_varying = new GLSLShaderVarying[1];
        m_varying[0] = GLSLDefaultVariables.VNormal;
        
        // allocate uniforms we use or expose
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = GLSLDefaultVariables.NormalMap;
        
        // declare dependencies, modifications, and initializations
        m_VertexDependencies = new ArrayList<GLSLShaderVariable>();
        m_VertexDependencies.add(GLSLDefaultVariables.ToLight);
        
        m_VertexModifications = new ArrayList<GLSLShaderVariable>();
        m_VertexModifications.add(GLSLDefaultVariables.ToLight);
        
        m_VertexInitializations = new ArrayList<GLSLShaderVariable>();
        m_VertexInitializations.add(GLSLDefaultVariables.TBNMatrix);
        
        m_FragmentInitializations = new ArrayList<GLSLShaderVariable>();
        m_FragmentInitializations.add(GLSLDefaultVariables.FragmentLocalNormal);

        createVertexLogic();
        createFragmentLogic();
    }

    /**
     * Create the fragment logic for this piece
     */
    private void createFragmentLogic()
    {
        StringBuilder fragmentLogic = new StringBuilder();
        fragmentLogic.append(m_fragmentGlobals[0].getName() + "= normalize(texture2D(" + m_fragmentUniforms[0].getName() + ", gl_TexCoord[0].st).xyz * 2.0 - 1.0);" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }
    
    /**
     * Create the vertex logic for this piece
     */
    private void createVertexLogic()
    {
        StringBuilder vertexLogic = new StringBuilder();
        vertexLogic.append("vec3 binormal = normalize(cross(gl_SecondaryColor.rgb, " + m_varying[0].getName() + "));" + NL);
        vertexLogic.append(m_vertexGlobals[0].getName() + " = mat3(gl_SecondaryColor.rgb, binormal, " + m_varying[0].getName() + ");" + NL);
        // transform the ToLight vector
        vertexLogic.append(m_VertexModifications.get(0).getName() + " *= " + m_vertexGlobals[0].getName() + ";" + NL);
        m_vertexLogic = vertexLogic.toString();
    }
}
