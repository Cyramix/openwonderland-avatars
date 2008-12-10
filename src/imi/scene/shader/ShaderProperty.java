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
package imi.scene.shader;

import imi.scene.shader.dynamic.GLSLDataType;

/**
 * This class represents a property of a shader. A property is defined as a 
 * class member that is exposed through well-defined getter and setter methods.
 * The exposed properties change the behavior of the owning shader.
 * @author Ronald E Dahlgren
 */
public class ShaderProperty 
{
    /**
     * This is the name of the property as referenced in the shader code
     */
    public  String          name    = null;
    /**
     * This is the data type for this particular property
     */
    public  GLSLDataType    type    = null;
    /**
     * The value. The class of this object should match the specified java type
     * specified by the enumeration above
     */
    private Object          m_value = null;

    /**
     * Construct a new instance based on the value of the other
     * @param other The property to copy (including its value)
     */
    public ShaderProperty(ShaderProperty other)
    {
        name = new String(other.name);
        type = other.type;
        setValue(other.m_value);
    }

    /**
     * Construct a new instance with the specified name
     * @param name The name of this property
     */
    public ShaderProperty(String name)
    {
        this(name, null, null);
    }
    
    /**
     * Construct a new instance.
     * @param name The name for this property
     * @param type GLSL Data type
     * @param value The value, null is acceptable
     */
    public ShaderProperty(String name, GLSLDataType type, Object value)
    {
        this.name = name;
        this.type = type;
        setValue(value);
    }
    
    /**
     * Sets the GLSL data type specifying member of this instance
     * @param type The data type
     */
    public void setType(GLSLDataType type)
    {
        this.type = type;
    }
    
    /**
     * Retrieves the value of this property
     * @return The value, may be null
     */
    public Object getValue()
    {
        return m_value;
    }
    
    /**
     * Assigns a reference to the passed in parameter as the
     * value of this property
     * @param value The new value
     * @return true on success, false if there is a type mismatch
     */
    public boolean setValue(Object value)
    {
        if (value == null)
        {
            m_value = null;
            return true;
        }
        else if (value.getClass() == type.getJavaType())
        {
            m_value = value;
            return true;
        }
        else
            return false;
    }
}
