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

import java.io.Serializable;

/**
 * This class represents a GLSL shader variable. This includes a type, a name, 
 * and a value. Subclasses include representatives for uniform variables, varying
 * types, and vertex attributes.
 * @author Ronald E Dahlgren
 */
public class GLSLShaderVariable implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /**
     * Convenience reference to the shader new line string
     */
    protected static String     NL          = GLSLDefaultVariables.ShaderNewline;
    /**
     * The in-code name used for this variable
     */
    protected String            m_name      = null;
    /**
     * The GLSL type of this variable
     */
    protected GLSLDataType      m_dataType  = null;
    /**
     * The value of this variable, the toString of the object should
     * return a value that is meaningful in GLSL shader code. This is 
     * not a problem for most types
     */
    protected Object    m_value     = null;
    
    /**
     * Construct a brand new instance of a GLSLShaderVariable with the specified
     * name and data type
     * @param name The in-code variable name
     * @param type The data type
     */
    public GLSLShaderVariable(String name, GLSLDataType type)
    {
        this(name, type, null);
    }
    
    /**
     * Construct a new GLSLShaderVariable instance with the given name, type,
     * and value.
     * @param name The in-code name for this variable
     * @param type The data type
     * @param value The value of the variable
     */
    public GLSLShaderVariable(String name, GLSLDataType type, Object value)
    {
        m_name = name;
        m_dataType = type;
        m_value = value;
    }
    
    /**
     * Retrieves the in-code variable name
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }
    
    /**
     * Sets the in-code variable name to the string specified.
     * @param newName The new name for this variable
     */
    public void setName(String newName)
    {
        m_name = newName;
    }
    
    /**
     * Retrieve the data type of this variable
     * @return The type
     */
    public GLSLDataType getDataType()
    {
        return m_dataType;
    }
    
    /**
     * Retrieve the value of this variable
     * @return The value
     */
    public Object getValue()
    {
        return m_value;
    }
    
    /**
     * Set the value of this variable.
     * @param value The value
     */
    public void setValue(Object value)
    {
        m_value = value;
    }
    
    /**
     * Returns a string of the form "(name) = (value);\n"
     * @return
     */
    @Override
    public String toString()
    {
        return new String(m_name + " = " + m_value.toString() + ";\n");
    }
    
    /**
     * Returns a string in the form of
     * "type name = rValue;\n"
     * @param rValue
     * @return The assignment string
     */
    public String assign(String rValue)
    {
        return new String(m_dataType.getGLSLString() + " " + m_name + " = " + rValue + ";" + NL);
    }
    
    /**
     * Declares this variable in the following form:
     * (if value != null) "type name = {value.toString()};\n"
     * (if value == null) "type name;\n"
     * @return The declaration string
     */
    public String declare()
    {
        String result = null;
        if (m_value == null)
            result = new String(m_dataType.getGLSLString() + " " + m_name + ";" + NL);
        else
            result = new String(m_dataType.getGLSLString() + " " + m_name + " = " + m_value.toString() + ";" + NL);
        return result;
    }
    /**
     * Craft a string to normalize this in the form:
     * "normalize(name)"
     * Note that there is no newline or semicolon included to allow nesting.
     * @return The normalize string
     */
    public String normalize()
    {
        return new String("normalize(" + m_name + ")");
    }
    
    /**
     * Craft a string to clamp this value in the form of:
     * "clamp(name, min, max)"
     * Note that there is no newline or semicolon included to allow nesting.
     * @param min String representing the minimum, i.e. "0.0"
     * @param max String representing the maximum, i.e. "1.0"
     * @return The clamp string
     */
    public String clamp(String min, String max)
    {
        return new String("clamp(" + m_name + ", " + min + ", " + max + ")");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final GLSLShaderVariable other = (GLSLShaderVariable) obj;
        if (this.m_name != other.m_name && (this.m_name == null || !this.m_name.equals(other.m_name)))
        {
            return false;
        }
        if (this.m_dataType != other.m_dataType)
        {
            return false;
        }
        if (this.m_value != other.m_value && (this.m_value == null || !this.m_value.equals(other.m_value)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
        hash = 31 * hash + (this.m_dataType != null ? this.m_dataType.hashCode() : 0);
        hash = 31 * hash + (this.m_value != null ? this.m_value.hashCode() : 0);
        return hash;
    }
}
