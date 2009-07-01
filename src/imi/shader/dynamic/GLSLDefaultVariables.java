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
package imi.shader.dynamic;

import java.util.HashMap;

/**
 * This class contains static references to many of the variables
 * that are of interest to multiple shader effect routines. These should
 * be dereferenced in shader effect logic blocks rather than directly
 * embedding the names. This will keep your shader code somewhat generalized
 * and help prevent brittle code failings.
 * @author Ronald E Dahlgren
 */
public class GLSLDefaultVariables 
{
    /**
     * This string is used throughout the intermediary code to represent a newline.
     * The use of a placeholder newline is to incorporate code formatting. This
     * should be a unique string, otherwise the behavior is undefined
     */
    public static final String ShaderNewline = "%OMGNEWLINE%";
    
    //////////////////////////////////////////////////////////////////////////
    //  UNIFORMS - This section contains the uniforms commonly referenced   //
    //////////////////////////////////////////////////////////////////////////
    /**
     * This uniform sets which texture unit the "Diffuse Map" is sampled from.
     */
    public static final GLSLShaderUniform DiffuseMap = 
            new GLSLShaderUniform("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D);
    /**
     * This uniform sets which texture unit the "Normal Map" is sampled from.
     */
    public static final GLSLShaderUniform NormalMap = 
            new GLSLShaderUniform("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D);
    /**
     * This uniform sets which texture unit the "Specular Map" is sampled from.
     */
    public static final GLSLShaderUniform SpecularMap = 
            new GLSLShaderUniform("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D);
    /**
     * This uniform sets which texture unit the "Shadow Map" is sampled from.
     */
    public static final GLSLShaderUniform ShadowMap = 
            new GLSLShaderUniform("ShadowMapIndex", GLSLDataType.GLSL_SAMPLER2D);
    /**
     * This uniform is used by the skinning system to pass its current pose to the
     * vertex deforming effect
     */
    public static final GLSLShaderUniform Pose 
            = new GLSLShaderUniform("pose", GLSLDataType.GLSL_VOID); // Special case
    
    ///////////////////////////////////////////////////////////////////////////
    //  VERTEX ATTRIBUTES - This section contains common vertex attributes   //
    ///////////////////////////////////////////////////////////////////////////
    /**
     * This vertex attribute provides indices for transform matrices in the
     * pose uniform. Weights are passed via the vertex colors
     */
    public static final GLSLVertexAttribute BoneIndices = 
            new GLSLVertexAttribute("boneIndices", GLSLDataType.GLSL_VEC4);
    
    //////////////////////////////////////////////////////////////////////////
    //  GLOBALS - This section contains global variables used throughout    //
    //////////////////////////////////////////////////////////////////////////
    /**
     * This is the color that is assign to gl_FragColor at the end of the fragment
     * shader
     */
    public static final GLSLShaderVariable FinalFragmentColor = 
            new GLSLShaderVariable("finalColor", GLSLDataType.GLSL_VEC4);
    /**
     * This is used to set gl_Position as the last step of the vertex shader
     * in the following form:
     * <code>gl_Position = gl_ModelViewProjectionMatrix * Position;</code>
     */
    public static final GLSLShaderVariable Position = 
            new GLSLShaderVariable("Position",  GLSLDataType.GLSL_VEC4);
    /**
     * This value is the dot product of the surface normal (be that from
     * a vertex attribute or some other source) and the vector to the light.
     */
    public static final GLSLShaderVariable NdotL = 
            new GLSLShaderVariable("NdotL", GLSLDataType.GLSL_FLOAT);
    /**
     * This vector is the normalized version of the tangent vertex attribute.
     */
    public static final GLSLShaderVariable TangentVec =
            new GLSLShaderVariable("TangentVec", GLSLDataType.GLSL_VEC3);
    /**
     * This is the matrix used to transform from model space to texture 
     * space.
     */
    public static final GLSLShaderVariable TBNMatrix =
            new GLSLShaderVariable("TBNMatrix", GLSLDataType.GLSL_MAT3);
    /**
     * This is the ARRAY of current transform matrices. This is used
     * to transform things into the appropriate animation pose.
     */
    public static final GLSLShaderVariable PoseBlend =
            new GLSLShaderVariable("poseBlend", GLSLDataType.GLSL_MAT4);
    
    /**
     * This variable is used as a mutable reference to the VNormal varying.
     * This has the implied dependency on that varying, and will generate
     * uncompilable code without it. Because of this, any code referencing 
     * FragLocalNormal should also express a dependency on VNormal
     */
    public static final GLSLShaderVariable FragmentLocalNormal =
            new GLSLShaderVariable("FragLocalNormal", GLSLDataType.GLSL_VEC3);
    
    //////////////////////////////////////////////////////////////////////////
    //  VARYING - This section contains varying parameters                  //
    //////////////////////////////////////////////////////////////////////////
    /**
     * This is the normal that is lerped between vertices for individual 
     * pixels. Any fragment shaders referencing this should ensure that it
     * has been properly calculated for the given circumstances.
     */
    public static final GLSLShaderVarying VNormal = 
            new GLSLShaderVarying("VNormal", GLSLDataType.GLSL_VEC3);
    /**
     * This is the vector from the vertex "the" (a?) given light source. It
     * is interpolated between verts to provide per-pixel approximations.
     */
    public static final GLSLShaderVarying ToLight = 
            new GLSLShaderVarying("ToLight", GLSLDataType.GLSL_VEC3);
    /**
     * This is the vector that points to the camera from the current position.
     * As a varying, it should always be normalized prior to use in fragment
     * shader logic.
     */
    public static final GLSLShaderVarying ToCamera =
            new GLSLShaderVarying("ToCamera", GLSLDataType.GLSL_VEC3);
    
    /**
     * This enumeration is used to indicate which parts of the shader pipeline
     * some thing is localized to. The meanings are use-specific.
     */
    public enum Locality
    {
        VERTEX,
        FRAGMENT,
        NONE,
        BOTH,
    }
    
    /**
     * This map acts as the source for default initialization logic. If the 
     * shader program is set to use default initializers this map will be
     * consulted to handle any variables that need to be used but were not
     * yet initialized to any meaningful value.
     */
    public static final HashMap<String,String> DefaultInitializers = new HashMap<String, String>();
    // static initializer for default initialization mapping initialization of the 
    // initializer strings used in initialization initially.
    static
    {
        DefaultInitializers.put("finalColor", "finalColor = gl_Color;" + ShaderNewline);
        DefaultInitializers.put("NdotL", "NdotL = 1.0;" + ShaderNewline);
        DefaultInitializers.put("ToLight", "ToLight = vec3(0,0,0);" + ShaderNewline);
        DefaultInitializers.put("VNormal", "VNormal = gl_Normal;" + ShaderNewline);
        DefaultInitializers.put("Position", "Position = gl_Vertex;" + ShaderNewline);
    }
}
