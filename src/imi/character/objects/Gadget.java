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
import java.net.URL;

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
    public Gadget(Vector3f position, Vector3f heading, URL location)
    {
        // Store the initOrigin
        initOrigin = new PMatrix();
        initOrigin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
        initOrigin.invert();

        sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA, location));
        AssetInitializer init = new AssetInitializer() {

                
                public boolean initialize(Object asset) {
                    // Initialize some stuffs
                    return true;
                }
            };
        sharedAsset.setInitializer(init);
    }
    
    /**
     * Adds this object to an object collection
     * @param objs
     */
    public void setObjectCollection(ObjectCollectionBase objs)
    {
        if (objs instanceof ObjectCollection)
        {
            objectCollection = (ObjectCollection)objs;
            objs.addObject(this);
        }
        else
            System.out.println("Error: gadget recieved a none compatible object collection");
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

    public void destroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
