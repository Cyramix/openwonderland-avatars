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
package imi.loaders.repository;

import com.jme.image.Texture;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import com.jme.util.TextureManager;
import imi.loaders.collada.Collada;
import imi.loaders.ms3d.SkinnedMesh_MS3D_Importer;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 * This class is the internal representation of a loaded piece of data and its
 * corresponding descriptor. The Repository manages things it has loaded through
 * RepositoryAsset objects.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class RepositoryAsset extends ProcessorComponent
{
    // the maximum number of references per (deep) copy
    static final int m_referenceThreshHold   = 100; //  TODO ? how many
    
    private int      m_referenceCount        = 0;
    
    // description
    private AssetDescriptor     m_descriptor = null;
   
    // data (doesn't need to be sequential in memory when multiple threads access the members)
    // in the future additional members will be added with (deep) copies of the original 
    // if the threshhold has been reached, to relieve frequent hits on the same memory spot. 
    // see : getDataReference() 
    private LinkedList<Object>   m_data       = null;
    
    private Repository           m_home       = null;

    public RepositoryAsset(AssetDescriptor description, boolean shaderCase, Repository home) 
    {
        m_home         = home;
        m_descriptor   = description;
    }

    private void loadSelf() 
    {
        if (m_data != null)
            return;
        m_data = new LinkedList<Object>();
        switch (m_descriptor.getType())
        {
            case Mesh:
            {
                if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
                {
                    // TODO: Refactor when the nonskinned MS3D loader is implemented              
//                    SkinnedMesh_MS3D_Importer importer = new SkinnedMesh_MS3D_Importer();
//                    importer.load(skeleton, m_descriptor.getFile().getPath());
//                    
//                    mesh.submit(new PPolygonTriMeshAssembler());
//                    m_data.add(mesh);
                }
                else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
                {
                    // TODO: Get COLLADA working through the repository
//                    PPolygonMesh mesh = null;
//                    Collada pColladaLoader = new Collada();
//                    boolean bResult = pColladaLoader.load(m_descriptor.getFile());
//                    if (bResult)
//                    {
//                        mesh = pColladaLoader.getPolygonMesh(0);
//                        mesh.submit(new PPolygonTriMeshAssembler());
//                        m_data.add(mesh);
//                    }
                }
            }
            break;
            case SkinnedMesh:
            {
                if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
                {
                     SkeletonNode skeleton = new SkeletonNode(m_descriptor.getLocation().getFile() + " skeleton");
                     SkinnedMesh_MS3D_Importer importer = new SkinnedMesh_MS3D_Importer();

                     importer.load(skeleton , m_descriptor.getLocation());

                     m_data.add(skeleton);
                }
                else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
                {
                    // TODO: Get COLLADA working through the repository
//                    PPolygonSkinnedMesh mesh = null;
//                    Collada pColladaLoader = new Collada();
//                    boolean bResult = pColladaLoader.load(m_descriptor.getFile());
//                    if (bResult)
//                    {
//                        mesh = pColladaLoader.getPolygonSkinnedMesh(0);
//                        mesh.submit(new PPolygonTriMeshAssembler());
//                        m_data.add(mesh);
//                    }
                }
            }
            break;
            case MS3D:  // Will assume to be skinned
            {
                SkinnedMesh_MS3D_Importer importer = new SkinnedMesh_MS3D_Importer();
                
                SkeletonNode skeleton = importer.loadMS3D(m_descriptor.getLocation());
                
                m_data.add(skeleton);
            }
            break;
            case COLLADA:
            {
                // Create a PScene and set it to m_data
                PScene scene = new PScene(m_descriptor.getType().toString() + " : " + m_descriptor.getLocation().getFile(), m_home.getWorldManager());
                scene.setUseRepository(false);
                // Load the collada file to the PScene
                Collada colladaLoader = new Collada();
                try {
                    boolean bResult = colladaLoader.load(scene, m_descriptor.getLocation());
                } catch (Exception ex) {
                    m_data.clear();
                    Logger.getLogger(RepositoryAsset.class.getName()).log(Level.SEVERE, null, ex);
                } 
                m_data.add(scene);
            }
            break;
            case Model:
            {
                // This will load models that are a composit of meshes and skinned meshes (potentially in a hierarchy of joints)
                
                // Load and parse a pscene
            }
            break;
            case ShaderPair:
            {
                Logger.getLogger(this.getClass().toString()).log(Level.WARNING,
                        "ShaderPair type assets are not currently supported for repository loading");
                // This method of loading shaders is deprecated until a way is
                // found to share shaders and the render thread iss is resolved.
            }
            break;
            case Texture:
            {
                // TODO expose texture configuration settings
                Texture tex = null;
               
                tex = TextureManager.loadTexture(m_descriptor.getLocation(),
                                                Texture.MinificationFilter.Trilinear,
                                                Texture.MagnificationFilter.Bilinear);
                

                if (tex != null)
                {
                    tex.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                    tex.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
                }

                //tex.setWrap(Texture.WM_WRAP_S_WRAP_T);  // TODO need configs
                m_data.add(tex);
            }
            break;
        }
        // finished loading, remove ourselves from the update pool
        m_home.removeProcessor(this);
        // THIS IS THE ENTRY POINT INTO CORRECTIONS MODE!
        setArmingCondition(new ProcessorArmingCollection(this));
    }
    
    private Object getDataReference() 
    {
        // TODO: Implement reference sharing system
        m_referenceCount++;
        return m_data.get(0);
    }
    
    public boolean loadData(SharedAsset asset)
    {
        if (m_data != null && !m_data.isEmpty())
        {
            asset.setData(getDataReference());
            return true;
        }
        
        return false; // didn't finish loading in loadSelf()
    }
    
    public int decrementReferenceCount()
    {
        // The repository should manage the case of references <= 0
        return --m_referenceCount;
    }

    /**
     * ProcessorComponent overload.
     * @param collection
     */
    @Override
    public void compute(ProcessorArmingCollection collection)
    {
        loadSelf();         
    }

    
    /**
     * ProcessorComponent overload.
     * @param collection
     */
    @Override
    public void commit(ProcessorArmingCollection collection) 
    {
       
    }

    
    /**
     * ProcessorComponent overload.
     */
    @Override
    public void initialize() 
    {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection); 
    }
}
