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
package imi.scene.polygonmodel;

import com.jme.math.Vector3f;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.loaders.scenebindings.sbBaseNode;
import imi.loaders.scenebindings.sbConfigurationData;
import imi.loaders.scenebindings.sbJointNode;
import imi.loaders.scenebindings.sbMatrix;
import imi.loaders.scenebindings.sbMeshNode;
import imi.loaders.scenebindings.sbModelFile;
import imi.loaders.scenebindings.sbTexture;
import imi.loaders.scenebindings.sbMaterial;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.utils.PRenderer;
import imi.scene.utils.tree.BoundingVolumeCollector;
import imi.scene.utils.tree.SkinnedMeshHookGenerator;
import imi.scene.utils.tree.TreeTraverser;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
/**
 *
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PPolygonModelInstance extends PNode
{
    PScene      m_owningScene = null; // The pscene that owns this instance, used for loading
    
    PSphere     m_boundingSphere = null; // The overall bounding sphere
        
    public PPolygonModelInstance(String name, PTransform transform, ArrayList<PPolygonMeshInstance> meshes) 
    {
        super(name, null, null, transform);
        if (meshes != null)
        {
            for (int i = 0; i < meshes.size(); i++)
                addChild(meshes.get(i));
        }
    }

    public PPolygonModelInstance(String name, PMatrix origin, ArrayList<PPolygonMeshInstance> meshes) 
    {
        this(name, new PTransform(origin), meshes);
    }

    public PPolygonModelInstance(String name, PMatrix origin) 
    {
        this(name, new PTransform(origin), null);
    }
    
    public PPolygonModelInstance(String name) 
    {
        this(name, new PTransform(), null);
    }

    @Override
    public void draw(PRenderer renderer)
    {
        for (int i = 0; i < getChildrenCount(); i++) 
            getChild(i).drawAll(renderer);
        
        // Draw model bounding sphere test
        /////////////////////////////////////////
        if (m_boundingSphere != null)
        {
            PMatrix origin       = getTransform().getWorldMatrix(false);
            renderer.setOrigin(origin);
            renderer.drawSphere(m_boundingSphere, 6, 6, false);
            //renderer.drawTriangle(Vector3f.UNIT_X.mult(8.0f), Vector3f.UNIT_X.mult(-8.0f), Vector3f.UNIT_Z.mult(8.0f));
        
        }
        /////////////////////////////////////////
    }

    /**
     * Adjusting the reference counts for all geometry being used by meshes of this model hierarchy
     */
    public void removeCleanUp() 
    {
        LinkedList<PNode> list = new LinkedList<PNode>(); 
        list.add(this);
       
        while(!list.isEmpty())
        {
            PNode current = list.poll();
         
            // Submit the SharedMesh to the jscene
            if (current instanceof PPolygonMeshInstance)
                ((PPolygonMeshInstance)current).decrementGeometryReference();
            
            // Add to the list all the kids
            for (int i = 0; i < current.getChildrenCount(); i++)
                list.add(current.getChild(i));
        }        
    }

    public sbBaseNode load_buildSceneBindings(PNode current) 
    {
        sbBaseNode result = null;
        
        if (current instanceof PJoint)
        {
            result = new sbJointNode();
            result.setName(current.getName());
            result.setTransform(current.getTransform().getLocalMatrix(false));
            ((sbJointNode)result).setModifier(((PJoint)current).getLocalModifierMatrix());
        }
        else if (current instanceof PPolygonMeshInstance)
        {
            sbMeshNode meshNode = new sbMeshNode();
            
            meshNode.setName(current.getName());
            meshNode.setTransform(current.getTransform().getLocalMatrix(false));
            
            PPolygonMesh geometry = ((PPolygonMeshInstance)current).getGeometry();
            
            if (geometry.getSharedAsset() == null)
            {
                // If this is procedural geometry
                meshNode.setGeometryFile(geometry.createProceduralGeometryFile());    // TODO
            }
            else
            {
                // If this geometry was loaded from a file
                meshNode.setGeometryFile(geometry.getSharedAsset().getDescriptor().getLocation().getPath());
            }
            
            // Save geometry material
            PMeshMaterial geoMat = ((PPolygonMeshInstance)current).getGeometry().getMaterialRef();
            if (geoMat != null)
            {
                sbMaterial sbGeoMat = new sbMaterial();
                
                // Textures
                List<sbTexture> textureList = new ArrayList<sbTexture>();
                
                URL[] textures = geoMat.getTextures();
                for (int i = 0; i < 8; i++)
                {
                    if (textures[i] != null)
                    {
                        sbTexture texture = new sbTexture();
                        texture.setTextureUnit(i);
                        texture.setPath(textures[i].getPath());
                        textureList.add(texture);
                    }
                    else
                        break;
                }
                if (!textureList.isEmpty())
                    sbGeoMat.setTextureFiles(textureList);
                
                meshNode.setGeometryMaterial(sbGeoMat);
            }
            
            // Save configuration
            sbConfigurationData config = ((PPolygonMeshInstance)current).load_createConfigurationData();
            if (config != null)
                meshNode.setConfiguration(config);
            
            if (current instanceof PPolygonSkinnedMeshInstance)
            {
                // if this is a skinned mesh we will parse the skeleton for any
                // none-skeleton children and save them as skinning hooks in a list
                if (current.getChild("m_TransformHierarchy").getChild(0) != null)
                {
                    SkinnedMeshHookGenerator hook = new SkinnedMeshHookGenerator();
                    TreeTraverser.breadthFirst(current.getChild("m_TransformHierarchy").getChild(0), hook);

                    meshNode.setSkinningHooks(hook.getAttachments());
                }
            }
            
            result = meshNode;
        }
        else if(current.getName().equals("m_TransformHierarchy"))
        {
            // prone the branch (no need to save skeletons, attached nodes are saved as skinning hooks)
            //System.out.println("node... m_TransformHierarchy                                 ==========");// test
            return null;
        }
        else
        {
            result = new sbBaseNode();
            result.setName(current.getName());
            //System.out.println("base node... " + result.getName() + "                       ==========");// test
            if (current.getTransform() != null)
                result.setTransform(current.getTransform().getLocalMatrix(false));
        }
        
        List<sbBaseNode> childrenList = new ArrayList<sbBaseNode>();
        
        // Handle the node's children
        for (PNode kid : current.getChildren())
        {
            // The infant will be null if it is a skeleton
            sbBaseNode infant = load_buildSceneBindings(kid);
            if (infant != null)
                childrenList.add(infant);
        }
        
        // Add the kids on to the current node
        result.setKids(childrenList);
        
        // Finished processing, return it!
        return result;
    }
    
    public sbModelFile load_buildModelFile()
    {
        sbModelFile modelFile = new sbModelFile();
        
        // Build the model instance's node
        sbBaseNode node = new sbBaseNode();
        modelFile.setBaseNode(node);
        node.setName(getName());

        if (getTransform() != null)
            node.setTransform(getTransform().getLocalMatrix(false));
        
        List<sbBaseNode> list = new ArrayList<sbBaseNode>();
        for(PNode current : getChildren())
        {
            list.add(load_buildSceneBindings(current));
        }
        node.setKids(list);
        
        return modelFile;
    }
    
    public void saveModel(File xml)
    {
        try {
            JAXBContext jc = JAXBContext.newInstance("imi.loaders.scenebindings");
            Marshaller m = jc.createMarshaller();
            //m.marshal(load_buildModelFile(), xml); <-- What JAXB version does this work with?
        } catch (JAXBException ex) {
            Logger.getLogger(PPolygonModelInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean loadModel(File xml, PScene scene)
    {
        m_owningScene = scene;
        sbModelFile modelFile = null;
        
        try {
            JAXBContext jc = JAXBContext.newInstance("imi.loaders.scenebindings");
            Unmarshaller u = jc.createUnmarshaller();
            modelFile = (sbModelFile)u.unmarshal(xml);
        } catch (JAXBException ex) {
            Logger.getLogger(PPolygonModelInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (modelFile != null)
        {
            load_processModelFile(modelFile.getBaseNode());
            this.buildFlattenedHierarchy();
            this.buildFlattenedSkinnedMeshJointHierarchy();
            return true;
        }
        
        return false;
    }

    private void load_processModelFile(sbBaseNode node) 
    {
        setName(node.getName());
        setTransform(new PTransform(node.getTransform().asPMatrix()));
        
        List<sbBaseNode> kids = node.getKids();
        
        if (kids != null)
        {
            for (sbBaseNode current : kids)
            {
                if (current instanceof sbJointNode)
                    load_processJointNode(this, current);
                else if (current instanceof sbMeshNode)
                    load_processMeshNode(this, current);
                else
                    load_processBaseNode(this, current);
            }
        }
    }
    
    private void load_processJointNode(PNode parent, sbBaseNode current) 
    {
        PJoint joint = new PJoint(current.getName(), new PTransform(current.getTransform().asPMatrix()));
        joint.setLocalModifierMatrix(((sbJointNode)current).getModifer().asPMatrix());
        parent.addChild(joint);
        
        List<sbBaseNode> kids = current.getKids();
        
        if (kids != null)
        {
            for (sbBaseNode currentKid : kids)
            {
                if (currentKid instanceof sbJointNode)
                    load_processJointNode(joint, currentKid);
                else if (currentKid instanceof sbMeshNode)
                    load_processMeshNode(joint, currentKid);
                else
                    load_processBaseNode(joint, currentKid);
            }
        }
    }
    
    private void load_processMeshNode(PNode parent, sbBaseNode current) 
    {
        // local reference safe for storing
        final sbMeshNode meshNode = (sbMeshNode) current;
        
        // Grab the geometry file and make a shared asset request for the
        // appropriate type
        String geometryFile = meshNode.getGeometryFile();
        SharedAsset meshAsset = null;
        if (geometryFile.endsWith("ms3d")) // milkshape
        {
            meshAsset = new SharedAsset(m_owningScene.getRepository(), 
                                                new AssetDescriptor(SharedAssetType.MS3D, new File(geometryFile)));
        }
        else if (geometryFile.endsWith("dae")) // COLLADA
        {
            ColladaLoaderParams params = new ColladaLoaderParams(true, true, true, false, 4, "Nonintrusive", null);
            meshAsset = new SharedAsset(m_owningScene.getRepository(), 
                                                new AssetDescriptor(SharedAssetType.COLLADA_Mesh, new File(geometryFile)),
                                                null, params);
        }
        else // currently unsupported
            return;

        // Now we will make an initializer that handles applying the transforms
        // and ultimately applying the config file
        AssetInitializer initializer = new AssetInitializer()
            {
                public PMatrix          m_transform     = meshNode.getTransform().asPMatrix();
                
                public String           m_name          = meshNode.getName();
                
                public sbConfigurationData m_config     = meshNode.getConfiguration();
                
                public List<sbBaseNode> m_hooks         = meshNode.getSkinningHooks();
                
                public boolean initialize(Object asset)
                {
                    PPolygonMeshInstance myInstance = (PPolygonMeshInstance)asset;
                    // set the name
                    myInstance.setName(m_name);
                    // Apply the transform
                    myInstance.getTransform().getLocalMatrix(true).set(m_transform);
                    // apply the configuration file
                    if (m_config != null)
                    {
                        myInstance.setUseGeometryMaterial(false);
                        myInstance.load_processConfiguration(m_config);
                    }
                    // attach skinning hooks
                    if (m_hooks != null)
                    {
                        for(sbBaseNode hook : m_hooks)
                        {
                            PNode bone = myInstance.findChild(hook.getParentName());

                            if (hook instanceof sbJointNode)
                                load_processJointNode(bone, hook);
                            else if (hook instanceof sbMeshNode)
                                load_processMeshNode(bone, hook);
                            else
                                load_processBaseNode(bone, hook);
                        }
                    }
                    // set the geometry's material
                    if (meshNode.getGeometryMaterial() != null)
                    {
                        sbMaterial sbGeoMat  = meshNode.getGeometryMaterial();
                        PMeshMaterial geoMat = null;
                        
//                        if (sbGeoMat.getShaderPair() != null)
//                        {
//                            geoMat = new PMeshMaterial("Geometry Material for" + meshNode.getName());
//                            geoMat.setVertShader(new File(sbGeoMat.getShaderPair().getVertexShaderPath()));
//                            geoMat.setFragShader(new File(sbGeoMat.getShaderPair().getFragmentShaderPath()));
//                        }
                        if (sbGeoMat.getTextureFiles() != null)
                        {
                            if (geoMat == null)
                                geoMat = new PMeshMaterial("Geometry Material for" + meshNode.getName());

                            for (sbTexture tex : sbGeoMat.getTextureFiles())
                                geoMat.setTexture(tex.getPath(), tex.getTextureUnit());
                        }
                        if (geoMat != null)
                            myInstance.getGeometry().setMaterial(geoMat);
                    }
                    
                    // done
                    //myInstance.buildFlattenedHierarchy();
                    myInstance.buildFlattenedSkinnedMeshJointHierarchy();
                    return true;
                }
            };
            
        // set the initializer
        meshAsset.setInitializer(initializer);
        
        // add the result to this node, may be a placeholder, may be the actual mesh instance.
        PNode mesh = m_owningScene.addMeshInstance(meshNode.getName() , meshAsset);
        parent.addChild(mesh);

        // Process the children
        List<sbBaseNode> kids = current.getKids();
        
        if (kids != null)
        {
            for (sbBaseNode currentKid : kids)
            {
                if (currentKid instanceof sbJointNode)
                    load_processJointNode(mesh, currentKid);
                else if (currentKid instanceof sbMeshNode)
                    load_processMeshNode(mesh, currentKid);
                else
                    load_processBaseNode(mesh, currentKid);
            }
        }
    }

    private void load_processBaseNode(PNode parent, sbBaseNode current) 
    {
        PMatrix localTransform = null;
        sbMatrix local = current.getTransform();
        if (local != null)
            localTransform = local.asPMatrix();
        else
            localTransform = new PMatrix();
        PNode node = new PNode(current.getName(), new PTransform(localTransform));
        parent.addChild(node);

        // Process the children
        List<sbBaseNode> kids = current.getKids();
        if (kids != null)
        {
            for (sbBaseNode currentKid : kids)
            {
                if (currentKid instanceof sbJointNode)
                    load_processJointNode(node, currentKid);
                else if (currentKid instanceof sbMeshNode)
                    load_processMeshNode(node, currentKid);
                else
                    load_processBaseNode(node, currentKid);
            }
        }
    }

    public PSphere getBoundingSphere() {
        return m_boundingSphere;
    }

    /**
     *  Calculates the bounding sphere surrounding all the sub meshes
     */
    public void calculateBoundingSphere()
    {
        Vector3f        boundingSphereCenter = new Vector3f(Vector3f.ZERO);
        float           fBoundingSphereRadius = 0.0f;
        float           flocalBoundingSphereRadius = 0.0f;
        
        // Collect spheres
        BoundingVolumeCollector processor = new BoundingVolumeCollector();
        TreeTraverser.breadthFirst(this, processor);
        ArrayList<PSphere> spheres = processor.getSpheres();
        
        // Calculate the overallcenter of the BoundingSphere.
        int centers = 0;
        for (PSphere sphere : spheres)
        {
            boundingSphereCenter = boundingSphereCenter.add(sphere.getCenter());
            centers++;
        }
        
        boundingSphereCenter = boundingSphereCenter.divide((float)centers);
        
        // Calculate the overall radius of the BoundingSphere
        for (PSphere sphere : spheres)
        {
            // Calculate the distance between two points.
            flocalBoundingSphereRadius = boundingSphereCenter.distance(sphere.getCenter());
            flocalBoundingSphereRadius += sphere.getRadius();
            
            if (flocalBoundingSphereRadius > fBoundingSphereRadius)
                fBoundingSphereRadius = flocalBoundingSphereRadius;
        }
        
        m_boundingSphere = new PSphere(boundingSphereCenter, fBoundingSphereRadius);
    }
}
