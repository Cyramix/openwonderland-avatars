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
 *
 * @author Lou Hayt
 */
public class Gadget implements SpatialObject
{
    ObjectCollection      objectCollection = null;
    PPolygonModelInstance modelInst        = null;
    
    PMatrix     initOrigin  = null;
    SharedAsset sharedAsset = null;
    
    public Gadget(Vector3f position, Vector3f heading, String modelFile)
    {
        if (modelFile != null && modelFile.endsWith(".dae"))
        {
            // Store the initOrigin
            initOrigin = new PMatrix();
            initOrigin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
            initOrigin.invert();
            
            sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA_Model, modelFile));
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
    
    public void translateSubMesh(Vector3f move, String meshName)
    {
        // Watch for the placeholder node!
        PNode meshNode = modelInst.findChild(meshName);
        if (meshNode == null || !(meshNode instanceof PPolygonMeshInstance))
            return;
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.findChild(meshName);
        mesh.getTransform().getLocalMatrix(true).setTranslation(mesh.getTransform().getLocalMatrix(true).getTranslation().add(move));
//                
//        modelInst.setDirty(true, true);
//        modelInst.buildFlattenedHierarchy();
//        //modelInst.submitTransformsAndGeometry();;
        
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
