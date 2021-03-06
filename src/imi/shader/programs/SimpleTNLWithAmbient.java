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
package imi.shader.programs;

import imi.shader.ShaderProperty;
import imi.shader.dynamic.GLSLDataType;
import imi.shader.dynamic.GLSLShaderProgram;
import imi.shader.effects.AmbientNdotL_Lighting;
import imi.shader.effects.CalculateToLight_Lighting;
import imi.shader.effects.GenerateFragLocalNormal;
import imi.shader.effects.VertexToPosition_Transform;
import imi.shader.effects.UnlitTexturing_Lighting;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class represents the basic transformation and N dot L lighting model
 * with a specified amount of ambient power mixed in to the final color result.
 * @author Ronald E Dahlgren
 */
public class SimpleTNLWithAmbient extends GLSLShaderProgram implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /**
     * Constructs a new instance defaulting to use 35% ambient contribution
     * @param wm
     */
    public SimpleTNLWithAmbient(WorldManager wm)
    {
        this(wm, 0.35f);
    }
    
    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public SimpleTNLWithAmbient(WorldManager wm, float fAmbientPower)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(
                "This program performs basic lighting with a single" +
                " texture plus an ambient component");
        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new GenerateFragLocalNormal()); // Get the frag normal
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient
        
        try
        {
            compile(); // generate the source code
            // Set the ambient power and the diffuse map texture unit
            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
