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
package imi.scene.shader.programs;

import imi.scene.shader.BaseShaderProgram;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.utils.FileUtils;
import java.net.URL;
import org.jdesktop.mtgame.WorldManager;

/**
 * This shader performs normal mapping using the texture in the specified
 * texture unit as the normal map.
 * @author Ronald E Dahlgren
 */
public class NormalAndSpecularMapShader extends BaseShaderProgram
{
    /**
     * Default constructor
     */
    public NormalAndSpecularMapShader(WorldManager wm)
    {
        super(
                wm,
                FileUtils.convertRelativePathToFileURL("assets/shaders/NormalAndSpecularMapping.vert"),
                FileUtils.convertRelativePathToFileURL("assets/shaders/NormalAndSpecularMapping.frag")
             );
        
        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String("This program performs normal and specular mapping"));
        // initialize shader properties
        m_propertyMap.put("DiffuseMapIndex",  new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
        m_propertyMap.put("NormalMapIndex",   new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
        m_propertyMap.put("SpecularMapIndex", new ShaderProperty("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));
        
        m_propertyMap.put("SpecularPower", new ShaderProperty("SpecularPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(32.0f)));
    }


    /**
     * Load the shaders relative to the colladaFileURL. The current implementation
     * assumes the wla url structure and uses the prefix of just the module name.
     *
     * @param wm
     * @param colladaFileURL
     */
    public NormalAndSpecularMapShader(WorldManager wm, URL colladaFileURL)
    {
        super(
                wm,
                wlaURL(colladaFileURL, "assets/shaders/NormalAndSpecularMapping.vert"),
                wlaURL(colladaFileURL, "assets/shaders/NormalAndSpecularMapping.frag")
             );

        setProgramName(this.getClass().getSimpleName());
        setProgramDescription(new String("This program performs normal and specular mapping"));
        // initialize shader properties
        m_propertyMap.put("DiffuseMapIndex",  new ShaderProperty("DiffuseMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(0)));
        m_propertyMap.put("NormalMapIndex",   new ShaderProperty("NormalMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(1)));
        m_propertyMap.put("SpecularMapIndex", new ShaderProperty("SpecularMapIndex", GLSLDataType.GLSL_SAMPLER2D, Integer.valueOf(2)));

        m_propertyMap.put("SpecularPower", new ShaderProperty("SpecularPower", GLSLDataType.GLSL_FLOAT, Float.valueOf(8.0f)));
    }

    
    /**
     * Apply the shader to this mesh.
     * @param meshInst The mesh to apply the shader on
     * @return true on success
     */
    @Override
    public boolean applyToMesh(PPolygonMeshInstance meshInst)
    {
        GLSLShaderObjectsState shaderState = 
                (GLSLShaderObjectsState) m_WM.getRenderManager().createRendererState(RenderState.RS_GLSL_SHADER_OBJECTS);
        // Apply uniforms and vertex attributes as needed
        blockUntilLoaded(shaderState);
        
        shaderState.setAttributePointer(
                                "tangent",  // The name, referenced in the shader code
                                3, // Total size of the data
                                false,                                    // "Normalized"
                                0,                                        // The "stride" (between entries)
                                meshInst.getGeometry().getGeometry().getTangentBuffer());    // The actual data   
        setProperties(shaderState);
        
//        shaderState.setUniform("diffuseMap", m_nDiffuseMapIndex);
//        shaderState.setUniform("normalMap", m_nNormalMapTextureUnitIndex);
//        shaderState.setUniform("specularMap", m_nSpecularMapTextureUnitIndex);
        
        meshInst.setShaderState(shaderState);
        
        return true;
    }
}
