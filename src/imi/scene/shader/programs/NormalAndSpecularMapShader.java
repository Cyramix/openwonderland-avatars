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
package imi.scene.shader.programs;

import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.AmbientNdotL_Lighting;
import imi.scene.shader.effects.CalculateToLight_Lighting;
import imi.scene.shader.effects.NormalMapping;
import imi.scene.shader.effects.SpecularMapping_Lighting;
import imi.scene.shader.effects.UnlitTexturing_Lighting;
import imi.scene.shader.effects.VertexToPosition_Transform;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This shader performs normal and specular mapping.
 * @author Ronald E Dahlgren
 */
public class NormalAndSpecularMapShader extends GLSLShaderProgram implements Serializable
{
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public NormalAndSpecularMapShader(WorldManager wm)
    {
        this(wm, 0.2f, 3.25f, 1.0f);
    }

    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public NormalAndSpecularMapShader(WorldManager wm, float fAmbientPower, float specularExponent)
    {
        this(wm, fAmbientPower, specularExponent, 1.0f);
    }

    public NormalAndSpecularMapShader(WorldManager wm, float fAmbientPower, float specularExponent, float specularComponent)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "This program performs normal and specular mapping and ambient lighting with " +
                "single texturing."
                ));
        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
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
            // Set the ambient power and the diffuse map texture unit
            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            setProperty(new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
            setProperty(new ShaderProperty("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
            setProperty(new ShaderProperty("specularExponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(specularExponent)));
            setProperty(new ShaderProperty("specularComponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(specularComponent)));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
