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
 * This class represents a uniform variable. Uniforms, although deriving from
 * GLSLShaderVariable, may not have values associated with them. This is due to
 * the very nature of the GLSL uniform, which is set external to the shader 
 * program. Any uniforms listed in an effect are exposed through the AbstractShaderProgram
 * interface as shader properties. These properties are then manipulated to cause
 * changes to the uniform values.
 * @author Ronald E Dahlgren
 */
public class GLSLShaderUniform extends GLSLShaderVariable
{
    /**
     * Construct a brand new instance of a GLSLShaderUniform
     * @param name The in-code name of this variable
     * @param type The data type of this uniform
     */
    public GLSLShaderUniform(String name, GLSLDataType type)
    {
        super(name, type, null);
    }
    
    /**
     * This method is not supported by uniforms, they have their
     * own mechanism for setting values.
     * @return N/A
     * @throws java.lang.UnsupportedOperationException
     */
    @Deprecated
    @Override
    public Object getValue() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Uniform values should be set through their own mechanisms");
    }
    
    /**
     * This method is not supported by uniforms, they have their
     * own mechanism for retrieving values.
     * @return N/A
     * @throws java.lang.UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setValue(Object value) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Uniform values should be set through their own mechanisms");
    }

    /**
     * Return a string of the form "uniform (name) = (value);\n"
     * @return
     */
    @Override
    public String toString()
    {
        return new String("uniform " + super.toString());
    }
    
    /**
     * This method returns a string in the form of "uniform name;\n"
     * @return The declaration string
     */
    @Override
    public String declare()
    {
        return new String("uniform " + super.declare());
    }
}
