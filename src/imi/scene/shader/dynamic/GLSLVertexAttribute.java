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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.scene.shader.dynamic;

/**
 * This class represents a vertex attribute. Vertex attributes are read-only and
 * may only be accessed in the vertex logic. 
 * @author Ronald E Dahlgren
 */
public class GLSLVertexAttribute extends GLSLShaderVariable 
{
    /**
     * Construct a new GLSLVertexAttribute instance with the specified
     * name and data type
     * @param name
     * @param type
     */
    public GLSLVertexAttribute(String name, GLSLDataType type)
    {
        super(name, type, null);
        m_value = null;
    }

    /**
     * Return a string in the form "attribute (name) = (value);\n"
     * @return
     */
    @Override
    public String toString()
    {
        return new String("attribute " + super.toString());
    }

    /**
     * This method is not supported for vertex attributes
     * @param rValue
     * @return N/A
     * @throws java.lang.UnsupportedOperationException Not supported for vertex 
     * attributes
     */
    @Override
    public String assign(String rValue) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Not supported on vertex attributes!");
    }

    /**
     * This method is not supported for vertex attributes
     * @return N/A
     * @throws java.lang.UnsupportedOperationException Not supported for vertex 
     * attributes
     */
    @Override
    public Object getValue()
    {
        throw new UnsupportedOperationException("Not supported on vertex attributes!");
    }

    /**
     * This method is not supported for vertex attributes
     * @param value
     * @return N/A
     * @throws java.lang.UnsupportedOperationException Not supported for vertex 
     * attributes
     */
    @Override
    public void setValue(Object value)
    {
        throw new UnsupportedOperationException("Not supported on vertex attributes!");
    }
    
    /**
     * This method is not supported for vertex attributes
     * @return N/A
     * @throws java.lang.UnsupportedOperationException Not supported for vertex 
     * attributes
     */

    @Override
    public String declare()
    {
        return new String("attribute " + super.declare());
    }
    
    
}
