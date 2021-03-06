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

import imi.shader.AbstractShaderProgram;
import imi.shader.NoSuchPropertyException;
import imi.shader.ShaderProperty;
import imi.shader.dynamic.GLSLCompileException;
import imi.shader.dynamic.GLSLDataType;
import imi.shader.dynamic.GLSLShaderProgram;
import imi.shader.effects.AmbientNdotL_Lighting;
import imi.shader.effects.CalculateToLight_Lighting;
import imi.shader.effects.DiffuseAsSpecular_Lighting;
import imi.shader.effects.GenerateFragLocalNormal;
import imi.shader.effects.MeshColorModulation;
import imi.shader.effects.UnlitTexturing_Lighting;
import imi.shader.effects.VertexDeformer_Transform;
import imi.shader.effects.VertexToPosition_Transform;
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
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;


    public PhongFleshShader(WorldManager wm)
    {
        this(wm, 0.45f, 8, 1.2f);
    }

    public PhongFleshShader(WorldManager wm,
                                            float fAmbientPower)
    {
        this(wm, fAmbientPower, 8, 1.2f);
    }

    public PhongFleshShader(WorldManager wm,
                                        float fAmbientPower,
                                        float specularExponent)
    {
        this(wm, fAmbientPower, specularExponent, 1.2f);
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
        setProgramDescription("It puts the GLSL on its skin or else it gets...");

        // add the effects in
        addEffect(new VertexToPosition_Transform()); // Set up the position
        addEffect(new VertexDeformer_Transform()); // Deform those verts!
        //addEffect(new ExposePoseBlendAsVarying_Accessor()); // Let the frag shader have access to the upper 3x3 of the poseblend matrix
        addEffect(new UnlitTexturing_Lighting()); // Grab pixel color from the diffuse map
        addEffect(new MeshColorModulation()); // Modulate by a skin color of some sort
        addEffect(new CalculateToLight_Lighting()); // Determine the vector to the light source (gl_LightSource[0])
        addEffect(new GenerateFragLocalNormal());
        addEffect(new DiffuseAsSpecular_Lighting());
        addEffect(new AmbientNdotL_Lighting()); // Calculate N * L and modulate the final color by it, mixing in a ratio of ambient
        

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
