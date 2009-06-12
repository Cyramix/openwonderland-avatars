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
import java.util.ArrayList;

/**
 *
 * @author Ronald E Dahlgren
 */
public class DecalTexture extends GLSLShaderEffect
{
    private static int TextureCoordSet = 0;
    public DecalTexture()
    {
        initializeDefaults();
    }

    private void initializeDefaults()
    {
        // set our name
        m_effectName = "DecalTexture";
        // optionally set a description
        m_effectDescription = "This effect overlays the texture " +
                              "loaded into the specified texture unit as a decal";
        // declare globals we intend to use
        m_fragmentGlobals = new GLSLShaderVariable[1];
        m_fragmentGlobals[0] = GLSLDefaultVariables.FinalFragmentColor;
        // declare uniforms we use or expose
        m_fragmentUniforms = new GLSLShaderUniform[1];
        m_fragmentUniforms[0] = new GLSLShaderUniform("decalTexture", GLSLDataType.GLSL_SAMPLER2D);

        // declare dependencies, modifications, and initializations
        m_FragmentModifications = new ArrayList<GLSLShaderVariable>();
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
        fragmentLogic.append("vec4 decalColor = texture2D(" + m_fragmentUniforms[0].getName() + ", gl_TexCoord[" + TextureCoordSet + "].st);" + NL);
        fragmentLogic.append("vec3 temp = mix(" + m_fragmentGlobals[0].getName() + ".rgb, decalColor.rgb, decalColor.a);" + NL);
        fragmentLogic.append(m_fragmentGlobals[0].getName() + " = vec4(temp, " + m_fragmentGlobals[0].getName() + ".a);" + NL);
        m_fragmentLogic = fragmentLogic.toString();
    }

    /**
     * Create the vertex portion of the source code
     * TODO: Expose the requested set of texture coordinates
     */
    private void createVertexLogic()
    {
        // generate texture coordinates
        m_vertexLogic = "gl_TexCoord[" + TextureCoordSet + "] = gl_MultiTexCoord0;" + NL;
    }
}
