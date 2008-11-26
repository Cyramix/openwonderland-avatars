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
package imi.scene.shader.programs;

import com.jme.scene.state.GLSLShaderDataLogic;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.AmbientNdotL_Lighting;
import imi.scene.shader.effects.CalculateToLight_Lighting;
import imi.scene.shader.effects.NormalMapping;
import imi.scene.shader.effects.SpecularMapping_Lighting;
import imi.scene.shader.effects.UnlitTexturing_Lighting;
import imi.scene.shader.effects.VertexDeformer_Transform;
import imi.scene.shader.effects.VertexToPosition_Transform;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 */
public class EyeballShader extends GLSLShaderProgram
{

    public EyeballShader(WorldManager wm)
    {
        this(wm, 0.55f, 32.0f);
    }

    public EyeballShader(WorldManager wm,
                                            float fAmbientPower)
    {
        this(wm, fAmbientPower, 32.0f);
    }

    public EyeballShader(WorldManager wm,
                                        float fAmbientPower,
                                        float specularExponent)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "Eyeball effect"
                ));

        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new VertexDeformer_Transform()); // Deform those verts!
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new NormalMapping());
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient
        addEffect(new SpecularMapping_Lighting());

        try
        {
            this.compile();
            m_vertAttributes.add(GLSLDefaultVariables.Tangents);
            this.synchronizePropertyObjects();
            // Set some defaults
            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            setProperty(new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
            setProperty(new ShaderProperty("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
            setProperty(new ShaderProperty("specularExponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(specularExponent)));
            m_propertyMap.put("pose", new ShaderProperty("pose", GLSLDataType.GLSL_VOID, null));
        }
        catch (NoSuchPropertyException e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
        catch (GLSLCompileException e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
