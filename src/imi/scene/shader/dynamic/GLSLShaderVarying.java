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
 * This class represents a "varying" used in a shader program. These types are
 * declared in both the vertex and the shader program's resultant source code.
 * They made me modified and set in the vertex logic, but are read only in the
 * fragment logic.
 * @author Ronald E Dahlgren
 */
public class GLSLShaderVarying extends GLSLShaderVariable implements Serializable
{
    /**
     * Construct a new instance with the specified name and type
     * @param name
     * @param type
     */
    public GLSLShaderVarying(String name, GLSLDataType type)
    {
        super(name, type, null);
        m_value = null;
    }

    /**
     * Return a string of the form "varying (name) = (value);\n"
     * @return
     */
    @Override
    public String toString()
    {
        return new String("varying " + super.toString());
    }

    /**
     * Returns a declaration string of the form "varying (type) (name); (ShaderNewline)"
     * @return
     */
   @Override
    public String declare()
    {
        return new String("varying " + m_dataType.getGLSLString() + " " + m_name + ";" + NL);
    }
    
}
