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
import imi.shader.effects.CalculateToLight_Lighting;
import imi.shader.effects.GenerateFragLocalNormal;
import imi.shader.effects.VertexToPosition_Transform;
import imi.shader.effects.SimpleNdotL_Lighting;
import imi.shader.effects.UnlitTexturing_Lighting;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class represents the basic transform and lighting
 * shader
 * @author Ronald E Dahlgren
 */
public class SimpleTNLShader extends GLSLShaderProgram implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /**
     * Construct a brand new instance!
     * @param wm
     */
    public SimpleTNLShader(WorldManager wm)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription("This program performs basic lighting with a single texture");
        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new GenerateFragLocalNormal()); // Get the frag normal
        addEffect(new SimpleNdotL_Lighting()); // Calculate N * L and modulate the final color by it
        
        try
        {
            compile(); // generate the code
             // set the diffuse map to tex unit 0
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
