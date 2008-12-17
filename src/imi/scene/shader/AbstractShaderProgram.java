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

import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.serialization.xml.bindings.xmlShaderProgram;

/**
 * This interface provides common access and modification mechanisms for all
 * shaders used throughout the system. The primary purpose of this interface
 * is to provide compatibility between the different shader implementations.
 * @author Ronald E Dahlgren
 */
public interface AbstractShaderProgram 
{
/**
     * This method should be implemented by subclasses to generate a 
     * GLSLShaderObjectsState instance, set the appropriate uniforms and vertex 
     * attributes for the shader, and set the shader state on the target mesh.
     * @param meshInst The mesh instance to apply the shader state on
     * @return true if "success"
     */
    public boolean applyToMesh(PPolygonMeshInstance meshInst);
    
    /**
     * This is the current work around to load shader objects on the 
     * render thread. The GLSLShaderObjectsState load method should only
     * be called from this method.
     * @param obj
     */
    public void update(Object obj);
    
    /**
     * This method returns a list of all available properties for this shader type
     * @return The array of shader properties.
     */
    public ShaderProperty[] getProperties();
    
    /**
     * Sets the specified property with the specified value. If the property is
     * not valid for this shader program, a NoSuchPropertyException will be thrown
     * to indicate this condition.
     * @param prop The property to set, along with its value
     * @return True on "success"
     */
    public boolean setProperty(ShaderProperty prop) throws NoSuchPropertyException;
    
    /**
     * Retrieve the name for this program.
     * @return
     */
    public String getProgramName();
    
    /**
     * Retrieve the description of this program.
     * @return
     */
    public String getProgramDescription();

    /**
     * Build and return an xmlShaderProgram object.
     * @return The filled out object.
     */
    public xmlShaderProgram generateShaderProgramDOM();
}
