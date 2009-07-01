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
import imi.shader.dynamic.GLSLDefaultVariables;
import imi.shader.dynamic.GLSLShaderProgram;
import imi.shader.effects.AmbientNdotL_Lighting;
import imi.shader.effects.CalculateToLight_Lighting;
import imi.shader.effects.NormalMapping;
import imi.shader.effects.UnlitTexturing_Lighting;
import imi.shader.effects.VertexToPosition_Transform;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This shader performs normal mapping using the texture in the specified
 * texture unit as the normal map.
 * @author Ronald E Dahlgren
 */
public class NormalMapShader extends GLSLShaderProgram implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    // The following two strings are the default source code for this effect
    private static final String VertexSource = 
        "varying vec3 VNormal;" +
        "varying vec3 ToLight;" +
        "vec4 Position;" +
        "void main(void)" +
        "{" +
        "        VNormal = gl_Normal;" +
        "        Position = gl_Vertex;" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "        ToLight = (gl_ModelViewMatrixInverse * (gl_LightSource[0].position - gl_Vertex)).xyz;" +
        "        vec3 binormal = normalize(cross(gl_SecondaryColor.rgb, gl_Normal));" +
        "        mat3 TBNMatrix = mat3(gl_SecondaryColor.rgb, binormal, gl_Normal);" +
        "        ToLight *= TBNMatrix;" +
        "        gl_Position = gl_ModelViewProjectionMatrix * Position;" +
        "}";

    private static final String FragmentSource = 
        "varying vec3 VNormal;" +
        "varying vec3 ToLight;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform float ambientPower;" +
        "uniform sampler2D NormalMapIndex;" +
        "vec3 FragLocalNormal;" +
        "vec4 finalColor;" +
        "float NdotL;" +
        "mat3 TBNMatrix;" +
        "void UnlitTexturing_Lighting();" +
        "void NormalMapping();" +
        "void AmbientNdotL_Lighting();" +
        "void main(void)" +
        "{" +
        "        UnlitTexturing_Lighting();" +
        "        NormalMapping();" +
        "        AmbientNdotL_Lighting();" +
        "        gl_FragColor = finalColor;" +
        "}" +
        "void UnlitTexturing_Lighting()" +
        "{" +
        "        finalColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);" +
        "}" +
        "void NormalMapping()" +
        "{" +
        "        FragLocalNormal = normalize(texture2D(NormalMapIndex, gl_TexCoord[0].st).xyz * 2.0 - 1.0);" +
        "}" +
        "void AmbientNdotL_Lighting()" +
        "{" +
        "        vec3 lightVec  = normalize(ToLight);" +
        "        NdotL = clamp(dot(FragLocalNormal, lightVec), 0.0, 1.0);" +
        "        vec4 afterLighting = finalColor * NdotL;" +
        "        afterLighting *= (1.0 - ambientPower);" +
        "        finalColor = finalColor * ambientPower;" +
        "        finalColor += afterLighting;" +
        "        finalColor.a = 1.0;" +
        "}";

    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public NormalMapShader(WorldManager wm)
    {
        this(wm, 0.2f);
    }

    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public NormalMapShader(WorldManager wm, float fAmbientPower)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(
                "This program performs normal mapping and ambient lighting with " +
                "single texturing.");
        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new NormalMapping());
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient

        try
        {
            // Since this is a well-defined effect initially, we can just set
            // the source string and only require a compile if effects are added
            this.synchronizePropertyObjects();
            // or removed
            setSourceStrings(VertexSource, FragmentSource);
            // Set the ambient power and the diffuse map texture unit
            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            setProperty(new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
        }
        catch (Exception e)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Caught " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
