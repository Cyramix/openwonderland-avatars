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

import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.AmbientNdotL_Lighting;
import imi.scene.shader.effects.CalculateToLight_Lighting;
import imi.scene.shader.effects.GenerateFragLocalNormal;
import imi.scene.shader.effects.VertexToPosition_Transform;
import imi.scene.shader.effects.UnlitTexturing_Lighting;
import imi.scene.shader.effects.VertexDeformer_Transform;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class performs vertex deformation using the vertex colors as the weights,
 * a boneIndices vertex attribute, and the "pose" uniform. It also uses an ambient
 * lighting model.
 * @author Ronald E Dahlgren
 */
public class VertexDeformer extends GLSLShaderProgram
{
    // The following two strings are the default source code for this effect
    private static final String VertexSource = new String(
        "attribute vec4 boneIndices;" +
        "varying vec3 VNormal;" +
        "varying vec3 ToLight;" +
        "uniform mat4 pose[55];" +
        "mat4 poseBlend;" +
        "vec4 Position;" +
        "void SimpleFTransform_Transfom();" +
        "void VertexDeformer_Transform();" +
        "void UnlitTexturing_Lighting();" +
        "void CalculateToLight_Lighting();" +
        "void main(void)" +
        "{" +
        "        SimpleFTransform_Transfom();" +
        "        VertexDeformer_Transform();" +
        "        UnlitTexturing_Lighting();" +
        "        CalculateToLight_Lighting();" +
        "        gl_Position = gl_ModelViewProjectionMatrix * Position;" +
        "}" +
        "void SimpleFTransform_Transfom()" +
        "{" +
        "        Position = gl_Vertex;" +
        "}" +
        "void VertexDeformer_Transform()" +
        "{" +
        "        vec3 weight = gl_Color.rgb;" +
        "        float weight4 = 1.0 - (weight.x + weight.y + weight.z);" +
        "        mat4 poseBlend = (pose[int(boneIndices.x)]) * weight.x +" +
        "        (pose[int(boneIndices.y)]) * weight.y +" +
        "        (pose[int(boneIndices.z)]) * weight.z +" +
        "        (pose[int(boneIndices.w)]) * weight4;" +
        "        VNormal.x = dot(gl_Normal, poseBlend[0].xyz);" +
        "        VNormal.y = dot(gl_Normal, poseBlend[1].xyz);" +
        "        VNormal.z = dot(gl_Normal, poseBlend[2].xyz);" +
        "        Position = Position * poseBlend;" +
        "}" +
        "void UnlitTexturing_Lighting()" +
        "{" +
        "        gl_TexCoord[0] = gl_MultiTexCoord0;" +
        "}" +
        "void CalculateToLight_Lighting()" +
        "{" +
        "        ToLight = (gl_ModelViewMatrixInverse * (gl_LightSource[0].position - gl_Vertex)).xyz;" +
        "}"
    );
    private static final String FragmentSource = new String(
        "varying vec3 VNormal;" +
        "varying vec3 ToLight;" +
        "uniform sampler2D DiffuseMapIndex;" +
        "uniform float ambientPower;" +
        "vec4 finalColor;" +
        "float NdotL;" +
        "void UnlitTexturing_Lighting();" +
        "void AmbientNdotL_Lighting();" +
        "void main(void)" +
        "{" +
        "        UnlitTexturing_Lighting();" +
        "        AmbientNdotL_Lighting();" +
        "        gl_FragColor = finalColor;" +
        "}" +
        "void UnlitTexturing_Lighting()" +
        "{" +
        "        finalColor = texture2D(DiffuseMapIndex, gl_TexCoord[0].st);" +
        "}" +
        "void AmbientNdotL_Lighting()" +
        "{" +
        "        vec3 lightVec  = normalize(ToLight);" +
        "        NdotL = clamp(dot(VNormal, lightVec), 0.0, 1.0);" +
        "        vec4 afterLighting = finalColor * NdotL;" +
        "        afterLighting *= (1.0 - ambientPower);" +
        "        finalColor = finalColor * ambientPower;" +
        "        finalColor += afterLighting;" +
        "        finalColor.a = 1.0;" +
        "}"
    );
    /**
     * Constructs a new instance defaulting to use 20% ambient contribution
     * @param wm
     */
    public VertexDeformer(WorldManager wm)
    {
        this(wm, 0.2f);
    }
    
    /**
     * Construct a new instance with the specified ambient power
     * @param wm
     * @param fAmbientPower A normalized (0.0 - 1.0) float representing the amount of
     * ambient to use for the final fragment color
     */
    public VertexDeformer(WorldManager wm, float fAmbientPower)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "This program performs vertex deformation as well as ambient" +
                " lighting and single texturing"
                ));
        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new VertexDeformer_Transform()); // Deform those verts!
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new GenerateFragLocalNormal()); // Get the frag normal
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient
        
        try
        {
            // Since this is a well-defined effect initially, we can just set
            // the source string and only require a compile if effects are added
            m_vertAttributes.add(GLSLDefaultVariables.BoneIndices);
            this.synchronizePropertyObjects();
            // or removed
            setSourceStrings(VertexSource, FragmentSource);
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

