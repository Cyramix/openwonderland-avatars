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

import java.io.Serializable;
import java.util.List;
import javolution.util.FastTable;

/**
 * This class represents a single atomic shader effect. They are assembled
 * together to form the net shader visual output. Shader effects have several
 * pieces of metadata associated with them beyond the actual code that is 
 * associated. This metadata describes relationships to globals, uniforms, and
 * vertex attributes within the code segments. These relationships include
 * modifications made to values, initializations, and dependencies. 
 *
 * Modifications are defined as a change made to a variable. If this modification
 * is a change to an existing value, then a matching dependency should also be
 * declared.
 * 
 * Initializations are defined as setting a value without depending on its state
 * prior to the assignment. Initializations satisfy dependencies, so be sure to
 * declare any that happen.
 * 
 * Dependencies are defined as a reliance on the value of a given variable to be
 * in a usable state when the effect is executed. These dependencies are satisfied
 * by initializations and must be declared in order to have compilable code.
 * 
 * Beyond the aforementioned metadata, there are also optional strings to aid
 * users in micro-shader. This includes a human-friendly description string that
 * tells what the effect is supposed to do. It could also serve to better describe
 * some of the dependencies if there are special circumstances around it.
 * @author Ronald E Dahlgren
 */
public class GLSLShaderEffect implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /** This is a convenience reference to the ShaderNewLine string **/
    protected static final String   NL                  = GLSLDefaultVariables.ShaderNewline;
    
    /** The name of the effect; used as a function name for the effect. **/
    protected String                m_effectName        = null;
    
    /** An optional, human-friendly textual description of the effect **/
    protected String                m_effectDescription = null;
    
    
    //////////////////////////////////////////////////////////
    //              REFERENCED DATA PROFILE                 //
    //////////////////////////////////////////////////////////
    /**
     * The following section is used to declare any globals, uniforms, etc that
     * are referenced by this particular effect. These are needed by the compile
     * process. Furthermore, names of these variables should be dereferenced
     * in the logic, not literally used in order to build some flexability.
     */
    // Uniforms used by this effect
    protected GLSLShaderUniform[]   m_vertexUniforms    = null;
    protected GLSLShaderUniform[]   m_fragmentUniforms  = null;
    // vertex attributes used by this effect
    protected GLSLVertexAttribute[] m_vertAttributes    = null;
    // any varying variables used by this effect
    protected GLSLShaderVarying[]   m_varying           = null;
    // list of globals used by this effect
    protected GLSLShaderVariable[]  m_vertexGlobals     = null;
    protected GLSLShaderVariable[]  m_fragmentGlobals   = null;
    
    //////////////////////////////////////////////////////////
    //           INITIALIZATION  DECLARATIONS               //
    //////////////////////////////////////////////////////////
    /**
     * This section is used to list the variables that are initialized
     * by this shader effect. Since the current compilation model
     * implemented here makes use of globals primarily, marking a
     * global as initialized by this effect allows for other effects to
     * modify those globals while assuming they have valid data already.
     */
    protected FastTable<GLSLShaderVariable>  m_FragmentInitializations   = null;
    protected FastTable<GLSLShaderVariable>  m_VertexInitializations     = null;
    
    //////////////////////////////////////////////////////////
    //             MODIFICATION DECLARATIONS                //
    //////////////////////////////////////////////////////////
    /**
     * This section lists variables that are modified in some way by the 
     * execution of this effect. A simple example would be the modification
     * of FinalFragmentColor by the SimpleNdotL_Lighting effect. If this
     * modification makes assumptions about the state of the data in the
     * variable it modifies, that variable should be added as a dependency
     * to assure that it is properly initialized before this section 
     * executes.
     */
    protected FastTable<GLSLShaderVariable>  m_FragmentModifications   = null;
    protected FastTable<GLSLShaderVariable>  m_VertexModifications     = null;
    
    //////////////////////////////////////////////////////////
    //              DEPENDENCY DATA PROFILE                 //
    //////////////////////////////////////////////////////////
    /**
     * This sections lists variables that must be initialized before this
     * effect can successfully execute. An example of this relationship
     * would be the SimpleNdotL_Lighting effect's dependency on the
     * varying vector to the light source. Without this variable being
     * properly initialized, the lighting logic cannot be expected to
     * behave as designed.
     */
    protected FastTable<GLSLShaderVariable>  m_FragmentDependencies   = null;
    protected FastTable<GLSLShaderVariable>  m_VertexDependencies     = null;
    
    //////////////////////////////////////////////////////////
    //                EFFECT LOGIC SECTION                  //
    //////////////////////////////////////////////////////////
    /**
     * This section contains the actual logic contained within 
     * strings. These strings should be populated with the code
     * that this effect executes. 
     */
    protected String                m_vertexLogic       = null;
    protected String                m_fragmentLogic     = null;
    
    /**
     * COnstruct a new, completely empty GLSLShaderEffect
     */
    public GLSLShaderEffect()
    {
        // do nothing
    }
    
    /**
     * Accessors and Mutators follow in the section below.
     */  
    public final String getEffectName()        { return m_effectName;       }
    public void setEffectName(String name)     { m_effectName = name;       }
    public final String getEffectDescription() { return m_effectDescription;}
    public void setEffectDescription(String description)
    {
        m_effectDescription = description;
    }
    public final String getVertexLogic()       { return m_vertexLogic;      }
    public void setVertexLogic(String logic)   { m_vertexLogic = logic;     }
    public final String getFragmentLogic()     { return m_fragmentLogic;    }
    public void setFragmentLogic(String logic) { m_fragmentLogic = logic;   }
    
    /**
     * Accessor
     * @return The array of globals referenced in the vertex logic
     */
    final GLSLShaderVariable[] getVertexGlobals()
    {
        return m_vertexGlobals;
    }
    
    /**
     * Mutator
     * @param vertexGlobals The new collection of globals referenced in the vertex logic
     */
    final void setVertexGlobals(GLSLShaderVariable[] vertexGlobals)
    {
        m_vertexGlobals = vertexGlobals;
    }
    
    /**
     * Accessor
     * @return Array of globals referenced in the fragment logic
     */
    final GLSLShaderVariable[] getFragmentGlobals()
    {
        return m_fragmentGlobals;
    }
    
    /**
     * mutator
     * @param fragmentGlobals Array of globals referenced in the fragment logic
     */
    final void setFragmentGlobals(GLSLShaderVariable[] fragmentGlobals)
    {
        m_fragmentGlobals = fragmentGlobals;
    }
    
    /**
     * Accessor
     * @return Array of vertex attributes referenced in this effect
     */
    final GLSLVertexAttribute[] getVertexAttributes()
    {
        return m_vertAttributes;
    }
    
    /**
     * Mutator
     * @param vertAttributes Array of vertex attributes neede
     */
    final void setVertexAttributes(GLSLVertexAttribute[] vertAttributes)
    {
        m_vertAttributes = vertAttributes;
    }
    
    /**
     * Accessor
     * @return Array of uniforms needed by the vertex shader
     */
    final GLSLShaderUniform[] getVertexUniforms()
    {
        return m_vertexUniforms;
    }
    
    /**
     * Mutator
     * @param vertUniforms Array of uniforms needed by the vertex shader
     */
    final void setVertexUniforms(GLSLShaderUniform[] vertUniforms)
    {
        m_vertexUniforms = vertUniforms;
    }
    
    /**
     * Accessor
     * @return Array of uniforms referenced in the fragment shader
     */
    final GLSLShaderUniform[] getFragmentUniforms()
    {
        return m_fragmentUniforms;
    }
    
    /**
     * Mutator
     * @param fragUniforms Array of uniforms referenced in the fragment code
     */
    final void setFragmentUniforms(GLSLShaderUniform[] fragUniforms)
    {
        m_fragmentUniforms = fragUniforms;
    }
    
    /**
     * Accessor
     * @return Array of varying types referenced by this effect
     */
    GLSLShaderVarying[] getVariants()
    {
        return m_varying;
    }
    
    /**
     * Mutator
     * @param variants Array of varying types referenced in the effect logic
     */
    void setVariants(GLSLShaderVarying[] variants)
    {
        m_varying = variants;
    }
    
    /**
     * Accessor
     * @return List of variables that are initialized in the fragment logic
     */
    List<GLSLShaderVariable> getFragmentInitializations()
    {
        return m_FragmentInitializations;
    }
    
    /**
     * Mutator
     * @param fragInit list of variables initialized in the fragment logic
     */
    void setFragmentInitializations(FastTable<GLSLShaderVariable> fragInit)
    {
        m_FragmentInitializations = fragInit;
    }

    /**
     * Accessor
     * @return The list of dependencies for the fragment logic
     */
    List<GLSLShaderVariable> getFragmentDependencies()
    {
        return m_FragmentDependencies;
    }

    /**
     * Mutator
     * @param FragmentDependencies the list of dependencies for the fragment logic
     */
    void setFragmentDependencies(FastTable<GLSLShaderVariable> FragmentDependencies)
    {
        this.m_FragmentDependencies = FragmentDependencies;
    }

    /**
     *  Accessor
     * @return List of variables changed by the fragment logic
     */
    List<GLSLShaderVariable> getFragmentModifications()
    {
        return m_FragmentModifications;
    }

    /**
     * Mutator
     * @param FragmentModifications List of variables modified in the fragment logic
     */
    void setFragmentModifications(FastTable<GLSLShaderVariable> FragmentModifications)
    {
        this.m_FragmentModifications = FragmentModifications;
    }

    /**
     * Accessor
     * @return List of dependencies for the vertex logic
     */
    List<GLSLShaderVariable> getVertexDependencies()
    {
        return m_VertexDependencies;
    }
    
    /**
     * Mutator
     * @param VertexDependencies List of dependencies for the vertex logic
     */
    void setVertexDependencies(FastTable<GLSLShaderVariable> VertexDependencies)
    {
        this.m_VertexDependencies = VertexDependencies;
    }

    /**
     * Accessor
     * @return List of variables initialized in the vertex logic
     */
    List<GLSLShaderVariable> getVertexInitializations()
    {
        return m_VertexInitializations;
    }

    /**
     * Mutator
     * @param VertexInitializations List of variables initialized in the vertex logic
     */
    void setVertexInitializations(FastTable<GLSLShaderVariable> VertexInitializations)
    {
        this.m_VertexInitializations = VertexInitializations;
    }

    /**
     * Accessor
     * @return The list of variables modified in the vertex logic
     */
    List<GLSLShaderVariable> getVertexModifications()
    {
        return m_VertexModifications;
    }

    /**
     * Mutator
     * @param VertexModifications The list of variables modified in the vertex logic
     */
    void setVertexModifications(FastTable<GLSLShaderVariable> VertexModifications)
    {
        this.m_VertexModifications = VertexModifications;
    }
    
    /**
     * Returns the effect name 
     * @return
     */
    @Override
    public String toString()
    {
        return m_effectName;
    }
}

