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
package imi.character.objects;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * This class represents the base 'gadget'. Gadgets are a way to expose
 * configuration options to the user in a non-invasive and immersive manner.
 * Subclasses will include things such as levels, switches, rotating knobs, etc.
 * @author Lou Hayt
 */
public class Gadget implements SpatialObject
{
    /** The object collection that this gadget belongs to. **/
    ObjectCollection      objectCollection = null;
    /** The model instance for this gadget's geometry **/
    PPolygonModelInstance modelInst        = null;
    /** The origin of this gadget **/
    PMatrix     initOrigin  = null;
    /** Shared asset for our geometry **/
    SharedAsset sharedAsset = null;

    /**
     * Construct a new instance of the gadget object at the given position and
     * heading using the provided modelFile.
     * @param position
     * @param heading
     * @param modelFile
     */
    public Gadget(Vector3f position, Vector3f heading, String modelFile)
    {
        if (modelFile != null && modelFile.endsWith(".dae"))
        {
            // Store the initOrigin
            initOrigin = new PMatrix();
            initOrigin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
            initOrigin.invert();

            // TODO !!
            sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA, modelFile));
            sharedAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 4, "slider switch", null));
            AssetInitializer init = new AssetInitializer() {
                public boolean initialize(Object asset) {

//                    if (asset instanceof PNode)
//                    {
//                        // find ever mesh instance and nullify it's color buffer
//                        FastList<PNode> queue = new FastList<PNode>();
//                        queue.add((PNode)asset);
//                        while (!queue.isEmpty())
//                        {
//                            PNode current = queue.removeFirst();
//                            if (current instanceof PPolygonMeshInstance)
//                            {
//                                PPolygonMeshInstance meshInst = (PPolygonMeshInstance)current;
//                                meshInst.getSharedMesh().getTarget().setColorBuffer(null);
//                            }
//                            // add all children
//                            queue.addAll(current.getChildren());
//                        }
//                        
//                    }
                    
                    return true;
                }
            };
            sharedAsset.setInitializer(init);
        }       
    }
    
    /**
     * Adds this object to an object collection
     * @param objs
     */
    public void setObjectCollection(ObjectCollection objs) 
    {
        objectCollection = objs;
        objs.addObject(this);
    }

    /**
     * Set the appropriate data to associate this gadget with the provided
     * pscene.
     * @param scene
     */
    public void setInScene(PScene scene)
    {
        if (sharedAsset != null)
        {
            //scene.setUseRepository(false);
            sharedAsset.setRepository(scene.getRepository());
            modelInst = scene.addModelInstance("Switch Slider", sharedAsset, initOrigin);
        }
        initOrigin = null; // only used to carry constructor's matrix
    }

    /**
     * Find the mesh with the given name and offset it by the provided offset.
     * @param offset Offset to offset the submesh by.
     * @param meshName Name of the mesh to affect.
     */
    public void translateSubMesh(Vector3f offset, String meshName)
    {
        // Watch for the placeholder node! (if the model is being loaded)
        PNode meshNode = modelInst.findChild(meshName);
        if (meshNode == null || !(meshNode instanceof PPolygonMeshInstance))
            return;
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.findChild(meshName);
        if (mesh != null)
            mesh.getTransform().getLocalMatrix(true).setTranslation(mesh.getTransform().getLocalMatrix(true).getTranslation().add(offset));
    }

    /**
     * Rotate the submesh with the provided name to match the provided orientation.
     * @param eulerInRadians
     * @param meshName
     */
    public void setRotationSubMesh(Vector3f eulerInRadians, String meshName)
    {
        // Watch for the placeholder node! (if the model is being loaded)
        PNode meshNode = modelInst.findChild(meshName);
        if (meshNode == null || !(meshNode instanceof PPolygonMeshInstance))
            return;
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.findChild(meshName);
        if (mesh != null)
            mesh.getTransform().getLocalMatrix(true).setRotation(eulerInRadians);
    }
    
    public PPolygonModelInstance getModelInst() {
        return modelInst;
    }

    public PSphere getBoundingSphere() {
        return modelInst.getBoundingSphere();
    }

    public PSphere getNearestObstacleSphere(Vector3f myPosition) {
        return null;
    }

    public Vector3f getPosition() {
        return modelInst.getTransform().getWorldMatrix(false).getTranslation();
    }

    public Quaternion getQuaternion() {
        return modelInst.getTransform().getWorldMatrix(false).getRotation();
    }

    public Vector3f getRightVector() {
        return modelInst.getTransform().getWorldMatrix(false).getLocalX();
    }

    public Vector3f getForwardVector() {
        return modelInst.getTransform().getWorldMatrix(false).getLocalZ();
    }

}
