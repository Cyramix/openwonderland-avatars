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
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.MaterialState.ColorMaterial;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.utils.PMeshUtils;

/**
 *
 * @author Lou
 */
public class Chair implements SpatialObject
{
    private SharedAsset sharedAsset = null;
    
    protected PPolygonModelInstance modelInst   = null;
    
    protected ObjectCollection objectCollection = null;
    
    private SpatialObject owner = null;
    private boolean occupied = false;
    
    private PMatrix origin = null;
    
    private PMatrix goalOffset = new PMatrix();
    private float   goalForwardOffset = 0.5f;
    
    public Chair(Vector3f position, Vector3f heading, String modelFile)
    {
        if (modelFile != null && modelFile.endsWith(".dae"))
        {
            goalOffset.buildRotationY((float) Math.toRadians(90));
            
            // Store the origin
            origin = new PMatrix();
            origin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
            origin.invert();
            
            sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA_Model, modelFile));
            sharedAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 4, "name", null));
            AssetInitializer init = new AssetInitializer() {
                public boolean initialize(Object asset) {

                    if (asset instanceof PNode)
                    {
                        //System.out.println(origin2);
//                        
//                        // Set position
//                        PNode mesh = (PNode)asset;
//                        mesh.getTransform().setLocalMatrix(((ColladaLoaderParams)sharedAsset.getUserData()).getOrigin());
                        
//                        mesh.getParent().setDirty(true, true);
//                        mesh.getParent().buildFlattenedHierarchy();
//                        ((PScene)mesh.getParent().getParent()).submitTransformsAndGeometry();
                    }
                    
                    return true;

                }
            };
            sharedAsset.setInitializer(init);
            
        }
        else
        {

            origin = new PMatrix();
            origin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
            origin.invert();
            modelInst = new PPolygonModelInstance("Chair", origin);

            PMeshMaterial geometryMaterial = new PMeshMaterial();
            geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
            geometryMaterial.setDiffuse(ColorRGBA.white);

            PPolygonMesh sphereMesh;
            PPolygonTriMeshAssembler assembler = new PPolygonTriMeshAssembler();

            sphereMesh = PMeshUtils.createSphere("Chair Mesh", Vector3f.ZERO, 1.0f, 6, 6, ColorRGBA.red);
            sphereMesh.setMaterial(geometryMaterial);
            sphereMesh.submit(assembler);
            //sphereMesh.getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(2.0f));
            modelInst.addChild(sphereMesh);    
        }        
    }
    
    /**
     * Adds this chiar to an object collection,
     * the collection will use the chair's geometry for characters to avoid
     * obstacles while sing steering behaviors and to be found to sit on.
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
            scene.setUseRepository(false);
            sharedAsset.setRepository(scene.getRepository());
            modelInst = scene.addModelInstance("Chair", sharedAsset, null);
        }
        else
        {
            if (modelInst.getParent()!= null)
                modelInst.getParent().removeChild(modelInst);
            modelInst = scene.addModelInstance(modelInst);
        }
    }
    
    public void setPosition(Vector3f position)
    {
        modelInst.getTransform().getLocalMatrix(true).setTranslation(position);
        modelInst.setDirty(true, true);
    }
    
    public Vector3f getPosition()
    {
        return modelInst.getTransform().getWorldMatrix(false).getTranslation();
    }
    
    public Vector3f getGoalPosition()
    {
//        Vector3f goalOffset = modelInst.getChild(1).getTransform().getWorldMatrix(false).getLocalZ().mult(sittingDistance);
//        return modelInst.getTransform().getWorldMatrix(false).getTranslation().add(goalOffset);
        
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.getChild(0);
        Vector3f result = mesh.getTransform().getWorldMatrix(false).getTranslation();
                
        return result.add(getGoalForwardVector().mult(goalForwardOffset));
        
    }
    
    public Vector3f getRightVector()
    {
        return modelInst.getTransform().getWorldMatrix(false).getLocalX();
    }
    
    public Vector3f getForwardVector()
    {
        return modelInst.getTransform().getWorldMatrix(false).getLocalZ();
    }
    
    public Vector3f getGoalForwardVector()
    {
         Vector3f normal = modelInst.getTransform().getWorldMatrix(false).getLocalZ();
        
        goalOffset.transformNormal(normal);
        
        return normal;
        
        
//        Vector3f goalPos = getGoalPosition();
//        Vector3f pos = getPosition();
//        return goalPos.subtract(pos).normalize();
    }
    
    public PSphere getNearestObstacleSphere(Vector3f myPosition)
    {
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.getChild(0);
        PSphere bv = mesh.getGeometry().getBoundingSphere();
        PSphere result = new PSphere();
        result.set(mesh.getTransform().getWorldMatrix(false).getTranslation(), bv.getRadius());
        return result;
    }
    
    public PSphere getBoundingSphere() 
    {
//        if (modelInst.getBoundingSphere() == null)
//            modelInst.calculateBoundingSphere();
//        return modelInst.getBoundingSphere();
        
        if (modelInst.getBoundingSphere() == null)
            modelInst.calculateBoundingSphere();
        PSphere result = new PSphere(modelInst.getBoundingSphere());
        result.setCenter(modelInst.getTransform().getWorldMatrix(false).getTranslation().add(result.getCenter()));
        return result;
    }

    public PPolygonModelInstance getModelInst() {
        return modelInst;
    }

    public Quaternion getQuaternion() {
        return modelInst.getTransform().getWorldMatrix(false).getRotationJME();
    }

    public boolean isOccupied() 
    {
        return occupied;
    }
    
    public boolean isOccupied(boolean occupiedMatters) 
    {
        if (occupiedMatters)
            return occupied;
        return false;
    }
    
    public void setOccupied(boolean yes)
    {
        occupied = yes;
    }

    public void setOwner(SpatialObject occupied) {
        owner = occupied;
    }
    public SpatialObject getOwner()
    {
        return owner;
    }

    void setInOrigin() 
    {
        modelInst.getTransform().setLocalMatrix(origin);
    }

}
