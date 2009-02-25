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

import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.AmbientNdotL_Lighting;
import imi.scene.shader.effects.CalculateToLight_Lighting;
import imi.scene.shader.effects.DiffuseAsSpecular_Lighting;
import imi.scene.shader.effects.GenerateFragLocalNormal;
import imi.scene.shader.effects.MeshColorModulation;
import imi.scene.shader.effects.UnlitTexturing_Lighting;
import imi.scene.shader.effects.VertexDeformer_Transform;
import imi.scene.shader.effects.VertexToPosition_Transform;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Ronald E Dahlgren
 */
public class PhongFleshShader extends GLSLShaderProgram implements Serializable
{

    public PhongFleshShader(WorldManager wm)
    {
        this(wm, 0.35f, 3.25f, 1.0f);
    }

    public PhongFleshShader(WorldManager wm,
                                            float fAmbientPower)
    {
        this(wm, fAmbientPower, 3.25f, 1.0f);
    }

    public PhongFleshShader(WorldManager wm,
                                        float fAmbientPower,
                                        float specularExponent)
    {
        this(wm, fAmbientPower, specularExponent, 1.0f);
    }


    protected PhongFleshShader(PhongFleshShader other) {
        super(other);
    }

    public PhongFleshShader(WorldManager wm,
                                        float fAmbientPower,
                                        float specularExponent,
                                        float specularComponent)
    {
        super(wm, true); // Use default initializers
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String(
                "It puts the GLSL on its skin or else it gets..."
                ));

        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new VertexDeformer_Transform()); // Deform those verts!
        //addEffect(new ExposePoseBlendAsVarying_Accessor()); // Let the frag shader have access to the upper 3x3 of the poseblend matrix
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new MeshColorModulation()); // Modulate by a skin color of some sort
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new GenerateFragLocalNormal());
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient
        addEffect(new DiffuseAsSpecular_Lighting());

        try
        {
            this.compile();
            this.synchronizePropertyObjects();
            // Set some defaults
            setProperty(new ShaderProperty("specularExponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(specularExponent)));
            setProperty(new ShaderProperty("specularComponent", GLSLDataType.GLSL_FLOAT, Float.valueOf(specularComponent)));
            setProperty(new ShaderProperty("ambientPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(fAmbientPower)));
            setProperty(new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
            // Color
            float[] whiteColor = { 1.0f, 1.0f, 1.0f };
            setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, whiteColor));
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


    @Override
    public AbstractShaderProgram duplicate()
    {
        PhongFleshShader result = new PhongFleshShader(this);
        return result;
    }
}
