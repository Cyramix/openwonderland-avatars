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
import javolution.util.FastList;

/**
 * This class is used to represent a "Chair" object in the world. It has properties
 * that are important to avatars and allows for sitting at a specified orientation.
 * @author Lou Hayt
 */
public class Chair implements SpatialObject
{
    /** This is the shared asset that the chair uses. **/
    private SharedAsset sharedAsset = null;
    /** The model instance for the chair. **/
    protected PPolygonModelInstance modelInst   = null;
    /** The collection that this chai belongs to. **/
    protected ObjectCollection objectCollection = null;
    
    private SpatialObject owner = null;
    /** True if the chair has someone or something sitting in it. **/
    private boolean occupied = false;
    /** Where to spawn this chair at. **/
    private PMatrix initOrigin = null;
    /** Where the goal point is (orientation as well) in chair reference space **/
    private PMatrix goalOffset = new PMatrix();

    private float   goalForwardOffset = 0.5f;

    /** Min-safe distance from other chairs. **/
    private float desiredDistanceFromOtherChairs = 1.5f;

    /**
     * Construct a brand new chair object at the given position and heading
     * using the specified model file.
     * @param position
     * @param heading
     * @param modelFile
     */
    public Chair(Vector3f position, Vector3f heading, String modelFile)
    {
        if (modelFile != null && modelFile.endsWith(".dae"))
        {
            goalOffset.buildRotationY((float) Math.toRadians(90));
            
            // Store the initOrigin
            initOrigin = new PMatrix();
            initOrigin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
            initOrigin.invert();
            
            sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA_Model, modelFile));
            ColladaLoaderParams loaderParams = new ColladaLoaderParams(false, true, false, true, false, 4, "name", null);
            loaderParams.setUsingSkeleton(false);
            sharedAsset.setUserData(loaderParams);
            AssetInitializer init = new AssetInitializer() {
                public boolean initialize(Object asset) {

                    if (asset instanceof PNode)
                    {
                        // find ever mesh instance and nullify it's color buffer
                        FastList<PNode> queue = new FastList<PNode>();
                        queue.add((PNode)asset);
                        while (!queue.isEmpty())
                        {
                            PNode current = queue.removeFirst();
                            if (current instanceof PPolygonMeshInstance)
                            {
                                PPolygonMeshInstance meshInst = (PPolygonMeshInstance)current;
                                meshInst.getSharedMesh().getTarget().setColorBuffer(null);
                            }
                            // add all children
                            queue.addAll(current.getChildren());
                            //((PPolygonModelInstance)asset).calculateBoundingSphere();
                        }
                    }
                    return true;
                }
            };
            sharedAsset.setInitializer(init);
        }
        else
        {

            PMatrix origin = new PMatrix();
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

    /**
     * Set the appropriate data to associate this chair with the provided pscene.
     * @param scene
     */
    public void setInScene(PScene scene)
    {
        if (sharedAsset != null)
        {
            //scene.setUseRepository(false);
            sharedAsset.setRepository(scene.getRepository());
            modelInst = scene.addModelInstance("Chair", sharedAsset, initOrigin);
        }
        else
        {
            if (modelInst.getParent()!= null)
                modelInst.getParent().removeChild(modelInst);
            modelInst = scene.addModelInstance(modelInst);
        }
        initOrigin = null; // only used to carry constructor's matrix
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
//        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.getChild(0);
//        PSphere bv = mesh.getGeometry().getBoundingSphere();
//        PSphere result = new PSphere();
//        result.set(mesh.getTransform().getWorldMatrix(false).getTranslation(), bv.getRadius());
//        return result;
        
        return getBoundingSphere();
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

    public float getDesiredDistanceFromOtherChairs() {
        return desiredDistanceFromOtherChairs;
    }

    public void setDesiredDistanceFromOtherChairs(float desiredDistanceFromOtherChairs) {
        this.desiredDistanceFromOtherChairs = desiredDistanceFromOtherChairs;
    }
    
}
