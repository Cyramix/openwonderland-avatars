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
package imi.shader;

import com.jme.math.Matrix3f;
import com.jme.math.Matrix4f;
import com.jme.scene.state.GLSLShaderObjectsState;
import imi.repository.Repository;
import imi.shader.dynamic.GLSLDataType;
import imi.shader.dynamic.GLSLShaderEffect;
import imi.shader.dynamic.GLSLShaderProgram;
import imi.shader.effects.AmbientNdotL_Lighting;
import imi.shader.effects.CalculateToLight_Lighting;
import imi.shader.effects.GenerateFragLocalNormal;
import imi.shader.effects.MeshColorModulation;
import imi.shader.effects.NormalMapping;
import imi.shader.effects.VertexToPosition_Transform;
import imi.shader.effects.SimpleNdotL_Lighting;
import imi.shader.effects.SpecularMapping_Lighting;
import imi.shader.effects.UnlitTexturing_Lighting;
import imi.shader.effects.VertexDeformer_Transform;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import javolution.util.FastTable;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The following provides a collection of static convenience methods for
 * dealing with shader processing.
 * @author Ronald E Dahlgren
 */
public class ShaderUtils 
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(ShaderUtils.class.getName());
    /** This string is used to indicate the package that contains premade programs **/
    public static final String  shaderProgramPackage = "imi.shader.programs";
    
    /**
     * This method calls the appropriate "setUniform" overloads to set each of
     * the values specified in the properties collection on the specified
     * shaderState. This method assumes that any safety checking required has 
     * already been performed by the caller.
     * @param props The collection of properties 
     * @param shaderState The shader state to apply them too
     */
    public static void assignProperties(Iterable<ShaderProperty> props,
                                        GLSLShaderObjectsState shaderState)
    {
        float[] fArray = null; // Recycled references
        int[]   iArray = null;
        // For each property
        for (ShaderProperty prop : props)
        {
            // Determine the type and set the uniform accordingly
            switch(prop.type)
            {
                case GLSL_VOID:
                    if (prop.name.equals("pose"))
                        break; // Pose is a special case. 
                    // nonsense uniform
                    logger.info("ShaderUtils.java:assignProperties: Uniform of type \"void\" cannot be set.");
                    break;
                case GLSL_BOOL:
                    shaderState.setUniform(prop.name, !((Integer)prop.getValue()).equals(Integer.valueOf(0)));
                    break;
                case GLSL_INT:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                case GLSL_FLOAT:
                    shaderState.setUniform(prop.name, (Float)prop.getValue());
                    break;
                case GLSL_VEC2:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name, 
                            fArray[0],  // X
                            fArray[1]); // Y
                    break;
                case GLSL_VEC3:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name, 
                            fArray[0], //X
                            fArray[1], //Y
                            fArray[2]);//Z
                    break;
                case GLSL_VEC4:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name, 
                            fArray[0], //X
                            fArray[1], //Y
                            fArray[2], //Z
                            fArray[3]);//W
                    break;
                case GLSL_BVEC2:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name, 
                            !(fArray[0] == 0.0f), 
                            !(fArray[1] == 0.0f));
                    break;
                case GLSL_BVEC3:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name,
                            !(fArray[0] == 0.0f),
                            !(fArray[1] == 0.0f),
                            !(fArray[2] == 0.0f));
                    break;
                case GLSL_BVEC4:
                    fArray = (float[])prop.getValue();
                    shaderState.setUniform(prop.name,
                            !(fArray[0] == 0.0f),
                            !(fArray[1] == 0.0f),
                            !(fArray[2] == 0.0f),
                            !(fArray[3] == 0.0f));
                    break;
                case GLSL_IVEC2:
                    iArray = (int[])prop.getValue();
                    shaderState.setUniform(prop.name,
                            iArray[0],
                            iArray[1]);
                    break;
                case GLSL_IVEC3:
                    iArray = (int[])prop.getValue();
                    shaderState.setUniform(prop.name,
                            iArray[0],
                            iArray[1],
                            iArray[2]);
                    break;
                case GLSL_IVEC4:
                    iArray = (int[])prop.getValue();
                    shaderState.setUniform(prop.name,
                            iArray[0],
                            iArray[1],
                            iArray[2],
                            iArray[3]);
                    break;
                case GLSL_MAT2X2:
                case GLSL_MAT2:
                    fArray = (float[])prop.getValue();
                    // apparently this is the overload for a 2x2 matrix
                    shaderState.setUniform(prop.name, fArray, true);
                    break;
                case GLSL_MAT3X3:
                case GLSL_MAT3:
                    fArray = (float[])prop.getValue();
                    Matrix3f mat3 = new Matrix3f(
                            fArray[0], fArray[1], fArray[2],
                            fArray[3], fArray[4], fArray[5],
                            fArray[6], fArray[7], fArray[8]);
                    shaderState.setUniform(prop.name, mat3, true);
                    break;
                case GLSL_MAT4X4:
                case GLSL_MAT4:
                    fArray = (float[])prop.getValue();
                    Matrix4f mat4 = new Matrix4f(
                            fArray[ 0], fArray[ 1], fArray[ 2], fArray[ 3],
                            fArray[ 4], fArray[ 5], fArray[ 6], fArray[ 7],
                            fArray[ 8], fArray[ 9], fArray[10], fArray[11],
                            fArray[12], fArray[13], fArray[14], fArray[15]);
                    shaderState.setUniform(prop.name, mat4, true);
                    break;
                    ////////////////////////////////////////////////////////////
                    // Non-square matrices are not currently supported via
                    // jME. Support could be forced (i.e. hacked) in, but
                    // I'll just wait until this is needed before doing that.
                    ////////////////////////////////////////////////////////////
                    
                case GLSL_MAT2X3:
                case GLSL_MAT2X4:
                case GLSL_MAT3X2:
                case GLSL_MAT3X4:
                case GLSL_MAT4X2:
                case GLSL_MAT4X3:
                    System.out.println("Unsupported uniform type: " + prop.type.toString());
                    break;

                case GLSL_SAMPLER1D:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                case GLSL_SAMPLER2D:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                case GLSL_SAMPLER3D:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                case GLSL_SAMPLERCUBE:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;

                case GLSL_SAMPLER1DSHADOW:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                case GLSL_SAMPLER2DSHADOW:
                    shaderState.setUniform(prop.name, (Integer)prop.getValue());
                    break;
                default:
                    System.out.println("Unknown property type: " + prop.type.toString());
            }
        }
    }
    
    /**
     * This list contains instances of each of the types of shader effects. This
     * list may be explicit because the well-defined default effects will not change.
     */
    private static FastTable<GLSLShaderEffect> GloballyAvailableEffects = null;
    // Static initializer block for the effect list
    static
    {
        
    }
    
    /**
     * Provides access to a global list of the default shader effects that are
     * available for use.
     * @return
     */
    public static GLSLShaderEffect[] getDefaultEffects()
    {
        GLSLShaderEffect[] result = new GLSLShaderEffect[0];
        if (GloballyAvailableEffects == null) {
            GloballyAvailableEffects = new FastTable<GLSLShaderEffect>();
            // TODO Convert to external loading for this, still determining the type
            GloballyAvailableEffects.add(new AmbientNdotL_Lighting());
            GloballyAvailableEffects.add(new CalculateToLight_Lighting());
            GloballyAvailableEffects.add(new VertexToPosition_Transform());
            GloballyAvailableEffects.add(new SimpleNdotL_Lighting());
            GloballyAvailableEffects.add(new UnlitTexturing_Lighting());
            GloballyAvailableEffects.add(new VertexDeformer_Transform());
            GloballyAvailableEffects.add(new NormalMapping());
            GloballyAvailableEffects.add(new GenerateFragLocalNormal());
            GloballyAvailableEffects.add(new SpecularMapping_Lighting());
            GloballyAvailableEffects.add(new MeshColorModulation());
        }
        return GloballyAvailableEffects.toArray(result);
    }
    
    /**
     * Load a GLSLShaderEffect from an effect configuration file
     * @param xml Points to the configuration file
     * @return The loaded shader effect
     * @throws java.io.FileNotFoundException
     */
    public static GLSLShaderEffect loadShaderEffect(File xml) throws FileNotFoundException
    {
        // TODO  
        // 1- Firstly, define an XML schema to represent the shader effect
        // 2- Create the logic to generate an XML configuration file from
        //      an effect
        // 3- Test generation of xml code from existing pre-fabs
        // 4- Load one of the successssssssful test files
        // 5- Test
        // 6- Incorporate into shader composer widget
        
        throw new UnsupportedOperationException("Not yet implemented!");
    }
    
    /**
     * Load an <code>AbstractShader</code> from the specified configuration
     * @param xml Points to the configuration file
     * @return The loaded shader
     * @throws java.io.FileNotFoundException
     */
    public static AbstractShaderProgram loadShaderProgram(File xml) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Factory method to aid in shader reconstitution.
     * @param shaderDOM
     * @return
     */
    @InternalAPI
    public static AbstractShaderProgram createShader(xmlShaderProgram shaderDOM,
                                                    WorldManager wm)
    {
        AbstractShaderProgram result = null;
        // Could be a regular shader class
        if (shaderDOM.getDefaultProgramName() != null) // Easy case
        {
            Repository repo = (Repository)wm.getUserData(Repository.class);
            try
            {
                Class classz = Class.forName(shaderDOM.getDefaultProgramName());
                result = repo.newShader(classz);
            }
            catch (Exception ex)
            {
                logger.log(Level.SEVERE, "Error recreating default shader:", ex);
            }

        }
        else // manually generate
        {
            GLSLShaderProgram program = new GLSLShaderProgram(wm);
            for (String effectName : shaderDOM.getListOfEffects())
            {
                try
                {
                    Class classz = Class.forName("imi.shader.effects." + effectName);
                    Constructor ctor = classz.getConstructor();
                    program.addEffect((GLSLShaderEffect) ctor.newInstance());
                }
                catch (Exception ex)
                {
                    logger.log(Level.SEVERE, "Error creating shader effect!", ex);
                }
            }
            try {
                program.compile();
            }
            catch (Exception ex)
            {
                logger.log(Level.SEVERE, "Error compiling shader!", ex);
            }
            result = program;
        }
        return result;
    }

    /**
     * Parse a given bit of text and return the correct derived type
     * @param value
     * @return
     */
    public static Object parseStringValue(String value, GLSLDataType type) {
        String trimmedValue = value.trim();
        Object result = null;
        FastTable arrayCollector = new FastTable();
        
        if (type.getJavaType() == float[].class)
        {
            StringTokenizer tokenizer = new StringTokenizer (trimmedValue);
            while (tokenizer.hasMoreTokens())
                arrayCollector.add(Float.valueOf(tokenizer.nextToken()));
            result = new float[arrayCollector.size()];

            for (int i = 0; i < arrayCollector.size(); i++)
                ((float[])result)[i] = ((Float)arrayCollector.get(i)).floatValue();
        }
        else if (type.getJavaType() == int[].class)
        {
            StringTokenizer tokenizer = new StringTokenizer (trimmedValue);
            while (tokenizer.hasMoreTokens())
                arrayCollector.add(Integer.valueOf(tokenizer.nextToken()));

            result = new int[arrayCollector.size()];
            for (int i = 0; i < arrayCollector.size(); i++)
                ((int[])result)[i] = ((Integer)arrayCollector.get(i)).intValue();
        }
        else if (type.getJavaType() == Float.class)
            result = Float.valueOf(trimmedValue);
        else if (type.getJavaType() == Integer.class)
            result = Integer.valueOf(trimmedValue);
        else // weirdness
            Logger.getLogger(ShaderUtils.class.getName()).log(Level.WARNING, "Unknown data type!");

        return result;
    }
}
