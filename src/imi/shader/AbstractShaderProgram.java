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

import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.serialization.xml.bindings.xmlShaderProgram;
import java.io.Serializable;

/**
 * This class provides common access and modification mechanisms for all
 * shaders used throughout the system. The primary purpose of this interface
 * is to provide compatibility between the different shader implementations.
 * @author Ronald E Dahlgren
 */
public abstract class AbstractShaderProgram implements Serializable
{
    /**
     * This method should be implemented by subclasses to generate a 
     * GLSLShaderObjectsState instance, set the appropriate uniforms and vertex 
     * attributes for the shader, and set the shader state on the target mesh.
     * @param meshInst The mesh instance to apply the shader state on
     * @return true if "success"
     */
    public abstract boolean applyToRenderStates(PPolygonMeshInstance mesh);
    
    /**
     * This method returns a list of all available properties for this shader type
     * @return The array of shader properties.
     */
    public abstract ShaderProperty[] getProperties();
    
    /**
     * Sets the specified property with the specified value. If the property is
     * not valid for this shader program, a NoSuchPropertyException will be thrown
     * to indicate this condition.
     * @param prop The property to set, along with its value
     * @return True on "success"
     */
    public abstract boolean setProperty(ShaderProperty prop) throws NoSuchPropertyException;
    
    /**
     * Retrieve the name for this program.
     * @return
     */
    public abstract String getProgramName();
    
    /**
     * Retrieve the description of this program.
     * @return
     */
    public abstract String getProgramDescription();

    /**
     * Build and return an xmlShaderProgram object.
     * @return The filled out object.
     */
    public abstract xmlShaderProgram generateShaderProgramDOM();

    /**
     * Return a new instance that represents an exact duplicate of this instance.
     * @return
     */
    public abstract AbstractShaderProgram duplicate();
}
