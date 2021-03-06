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

import imi.shader.programs.*;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.jogl.JOGLShaderObjectsState;
import imi.scene.polygonmodel.PMeshMaterialStates;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.utils.FileUtils;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;

/**
 * This base class represents the commonality amongst all shaders
 * in our system. This base class is used in the file-based shader program
 * implementation. This provides convenience methods for loading everything and
 * getting things running.
 * @author Ronald E Dahlgren
 */
public abstract class BaseShaderProgram extends AbstractShaderProgram implements RenderUpdater, Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private static final Logger logger = Logger.getLogger(BaseShaderProgram.class.getName());
    /** These files point to the vertex and fragment source respectively **/
    protected String []       m_shaderSource = new String [2];
    /** The world manager is needed in order to create render states **/
    protected transient WorldManager  m_WM          = null;
    /** Map the relationships between property names and property objects **/
    protected Map<String,ShaderProperty> m_propertyMap  = new HashMap<String,ShaderProperty>();
    /** Used to indicate that the shader state object has finsihed loading**/
    protected boolean         m_bShaderLoaded = false;
    /** The name of this program **/
    private String    m_programName = null;
    /** The description of what this program does **/
    private String    m_programDescription = null;
    /** Boolean to indicate that the boneIndices attribute is used **/
    protected boolean m_needBoneIndices = false;
    
    /**
     * Construct a new instance
     * @param wm The world manager for creating render states
     * @param vertexShader Location of the vertex shader source
     * @param fragmentShader Location of the fragment shader source
     */
    protected BaseShaderProgram(WorldManager wm, String vertexShader, String fragmentShader)
    { 
        setWorldManager(wm);
        m_shaderSource[0] = vertexShader;
        m_shaderSource[1] = fragmentShader;
    }

    protected BaseShaderProgram(BaseShaderProgram other)
    {
        m_shaderSource[0] = other.m_shaderSource[0];
        m_shaderSource[1] = other.m_shaderSource[1];

        setWorldManager(other.m_WM);
        for (ShaderProperty prop : other.getProperties())
            m_propertyMap.put(prop.name, new ShaderProperty(prop));
        
        m_bShaderLoaded = other.m_bShaderLoaded;
        m_programName = other.m_programName;
        m_programDescription = other.m_programDescription;
    }
    
    /**
     * This method should be implemented by subclasses to generate a 
     * GLSLShaderObjectsState instance, set the appropriate uniforms and vertex 
     * attributes for the shader, and set the shader state on the target mesh.
     * ** NOTE ** Because of the current jME shader workaround, the shader object
     * can only have its "load" method called within the update method below.
     * This means that this method should block until the update method has been
     * called and successfully set things up before applying the state to the
     * target mesh.
     * @param meshInst The mesh instance to apply the shader state on
     * @return true if "success"
     */
    @Override
    public boolean applyToRenderStates(PPolygonMeshInstance meshInstance)
    {
        m_bShaderLoaded = false;
        m_WM.addRenderUpdater(this, meshInstance);
        return true;
    }

    public final void setWorldManager(WorldManager wm)
    {
        if (wm == null)
            throw new IllegalArgumentException("Must provide a valid WorldManager!");
        m_WM = wm;
    }
    
    /**
     * This is the current work around to load shader objects on the 
     * render thread. The GLSLShaderObjectsState load method should only
     * be called from this method.
     * @param obj
     */
    @Override
    public final void update(Object obj)
    {
        PPolygonMeshInstance meshInst = (PPolygonMeshInstance)obj;
        PMeshMaterialStates states = meshInst.getMaterialStates();
        GLSLShaderObjectsState shaderState = states.getShaderState();
        shaderState.setEnabled(true);
        shaderState.load(m_shaderSource[0], m_shaderSource[1]);

        // Apply uniforms
        ShaderUtils.assignProperties(m_propertyMap.values(), shaderState);

        shaderState.apply();


        JOGLShaderObjectsState joglShader = (JOGLShaderObjectsState)shaderState;
        if (m_propertyMap.containsKey("pose"))
        {
            final GL2 gl = GLU.getCurrentGL().getGL2();
            joglShader.setHasAttributes(true);
            gl.glBindAttribLocation(joglShader.getProgramIdentifier(), 1, "boneIndices");
        }
        
        meshInst.getSharedMesh().setRenderState(joglShader);
        m_WM.addToUpdateList(meshInst.getSharedMesh());
        // done
        m_bShaderLoaded = true;
    }
    
    /**
     * This method returns a list of all available properties for this shader type
     * @return The array of shader properties.
     */
    public final ShaderProperty[] getProperties()
    {
        Object[] objArray = m_propertyMap.values().toArray();
        ShaderProperty[] result = new ShaderProperty[objArray.length];
        for(int i = 0; i < objArray.length; i++) {
            result[i] = (ShaderProperty)objArray[i];
        }
        return result;
    }
    
    /**
     * Sets the specified property with the specified value. This method will
     * throw a NoSuchPropertyException if the specified property is not valid.
     * @param prop The property to set along with its value
     * @return true on success
     */
    public final boolean setProperty(ShaderProperty prop) throws NoSuchPropertyException
    {
        if (m_propertyMap.containsKey(prop.name) == false)
            throw new NoSuchPropertyException(prop.name + " is not valid in this program.");
        m_propertyMap.get(prop.name).setValue(prop.getValue());
        
        return true;
    }
    /**
     * Retrieve the name of this program
     * @return
     */
    public String getProgramName()
    {
        return m_programName;
    }
    
    /**
     * Set the name of this program
     * @param name
     */
    protected void setProgramName(String name)
    {
        m_programName = name;
    }
    
    /**
     * Retrieve the description of what this program does.
     * @return
     */
    public String getProgramDescription()
    {
        return m_programDescription;
    }
    
    /**
     * Set the description for this program
     * @param description
     */
    protected void setProgramDescription(String description)
    {
        m_programDescription = description;
    }
    
    protected static URL wlaURL(URL prefix, String postfix) {
        try {
            if (prefix!=null && prefix.getProtocol().equalsIgnoreCase("wla")) {
                String str = "wla://"+prefix.getUserInfo()+"@"+prefix.getHost()+":"+prefix.getPort()+"/" + postfix;
//                System.err.println("Attempting to create URL "+str);
                return new URL(str);
            } else {
                return FileUtils.convertRelativePathToFileURL(postfix);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(NormalAndSpecularMapShader.class.getName()).log(Level.SEVERE, "Problem creating Shader URL "+prefix.getHost(), ex);
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(m_programName + "\nDesc: " + m_programDescription);
        for (ShaderProperty prop : getProperties()) {
            result.append("\nProperty: " + prop);
        }
        return result.toString();
    }


    
    //////// SERIALIZATION HELPERS /////////////
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
                                                        ClassNotFoundException
    {
        in.defaultReadObject();
    }

}
