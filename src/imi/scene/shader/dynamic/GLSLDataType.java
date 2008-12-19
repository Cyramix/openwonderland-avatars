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
package imi.scene.shader.dynamic;

/**
 * This file contains the enumerations for all of the GLSL built-in types. Each 
 * type has a string associated with it. This string is the textual GLSL equivalent.
 * @author Ronald E Dahlgren
 */
public enum GLSLDataType 
{
    /**
     * The following maps in-code GLSL strings with enumerated types, as well
     * as associating the data type that the shader system would like the value
     * in. It may seem arbitrary, but this mapping was chosen in order to keep
     * the number of class types low while still covering all the needed
     * functionality.
     */
    // The types 
    GLSL_VOID   ("void",    null),
    GLSL_BOOL   ("bool",    Integer.class),
    GLSL_INT    ("int",     Integer.class),
    GLSL_FLOAT  ("float",   Float.class),
    GLSL_VEC2   ("vec2",    float[].class),
    GLSL_VEC3   ("vec3",    float[].class),
    GLSL_VEC4   ("vec4",    float[].class),
    GLSL_BVEC2  ("bvec2",   float[].class),
    GLSL_BVEC3  ("bvec3",   float[].class),
    GLSL_BVEC4  ("bvec4",   float[].class),
    GLSL_IVEC2  ("ivec2",   int[].class),
    GLSL_IVEC3  ("ivec3",   int[].class),
    GLSL_IVEC4  ("ivec4",   int[].class),
    GLSL_MAT2   ("mat2",    float[].class),
    GLSL_MAT3   ("mat3",    float[].class),
    GLSL_MAT4   ("mat4",    float[].class),
    GLSL_MAT2X2 ("mat2x2",  float[].class),
    GLSL_MAT2X3 ("mat2x3",  float[].class),
    GLSL_MAT2X4 ("mat2x4",  float[].class),
    GLSL_MAT3X2 ("mat3x2",  float[].class),
    GLSL_MAT3X3 ("mat3x3",  float[].class),
    GLSL_MAT3X4 ("mat3x4",  float[].class),
    GLSL_MAT4X2 ("mat4x2",  float[].class),
    GLSL_MAT4X3 ("mat4x3",  float[].class),
    GLSL_MAT4X4 ("mat4x4",  float[].class),
    
    GLSL_SAMPLER1D      ("sampler1D",   Integer.class),
    GLSL_SAMPLER2D      ("sampler2D",   Integer.class),
    GLSL_SAMPLER3D      ("sampler3D",   Integer.class),
    GLSL_SAMPLERCUBE    ("samplerCube", Integer.class),
    
    GLSL_SAMPLER1DSHADOW ("sampler1DShadow", Integer.class),
    GLSL_SAMPLER2DSHADOW ("sampler2DShadow", Integer.class);
   
    /** This is the in-code name used to represent the type **/
    private final String glslText;
    /** The associated java type that is used to represent to value **/
    private final Class  javaType;
    
    /**
     * Construct a new instance
     * @param text The in-code representation
     * @param associatedJavaType The java representation
     */
    private GLSLDataType(String text, Class associatedJavaType)
    {
        glslText = text;
        javaType = associatedJavaType;
    }
    
    /**
     * Return the associated GLSL text
     * @return
     */
    public String getGLSLString()
    {
        return this.glslText;
    }
    /**
     * Retrieve the java class used to represent this type.
     * @return
     */
    public Class getJavaType()
    {
        return javaType;
    }
    
    
}


