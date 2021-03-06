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
package imi.objects;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.MaterialState.ColorMaterial;
import imi.repository.AssetDescriptor;
import imi.repository.AssetInitializer;
import imi.repository.SharedAsset;
import imi.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PSphere;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.utils.PMeshUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This class is used to represent a "Chair" object in the world. It has properties
 * that are important to avatars and allows for sitting at a specified orientation.
 * @author Lou Hayt
 */

@ExperimentalAPI
public class AvatarChair implements ChairObject
{
    /** This is the shared asset that the chair uses. **/
    private SharedAsset sharedAsset = null;
    /** The model instance for the chair. **/
    protected PPolygonModelInstance modelInst   = null;
    /** The collection that this chai belongs to. **/
    protected ObjectCollectionBase objectCollection = null;
    
    private SpatialObject owner = null;
    /** True if the chair has someone or something sitting in it. **/
    private boolean occupied = false;
    /** Where to spawn this chair at. **/
    private PMatrix initOrigin = null;
    /** Where the goal point is (orientation as well) in chair reference space **/
    private PMatrix goalOffset = new PMatrix();

    private float   goalForwardOffset = 0.5f;

    /** Min-safe distance from other chairs. **/
    private float desiredDistanceFromOtherChairs = 2.0f;

    /**
     * Construct a brand new chair object at the given position and heading
     * using the specified model file.
     * @param position
     * @param heading
     * @param modelFile
     */
    public AvatarChair(Vector3f position, Vector3f heading, Object modelPathOrURL)
    {
        URL modelURL = null;
        if (modelPathOrURL instanceof String)
        {
            try {
                modelURL = new File((String) modelPathOrURL).toURI().toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(AvatarChair.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
            modelURL =(URL)modelPathOrURL;

        goalOffset.buildRotationY((float) Math.toRadians(90));

        // Store the initOrigin
        initOrigin = new PMatrix();
        initOrigin.lookAt(position, position.add(heading), Vector3f.UNIT_Y);
        initOrigin.invert();

        //final Chair me = this;

        sharedAsset = new SharedAsset(null, new AssetDescriptor(SharedAssetType.COLLADA, modelURL));
        AssetInitializer init = new AssetInitializer() {
            public boolean initialize(Object asset) {

//                    System.out.println("init chair " + me); // watch for "freeloaders"
//                    System.out.println(asset);

                // Apply material to all meshes
                FastList<PNode> queue = new FastList<PNode>();
                queue.addAll(((PNode)asset).getChildren());
                while (queue.isEmpty() == false)
                {
                    PNode current = queue.removeFirst();
                    if (current instanceof PPolygonMeshInstance)
                    {
                        PPolygonMeshInstance meshInst = (PPolygonMeshInstance) current;
                        //System.out.println("applying material on " + meshInst);
                        meshInst.applyMaterial();

                        //meshInst.getGeometry().calculateBoundingSphere();
//                            meshInst.getSharedMesh().setModelBound(new BoundingSphere());
//                            meshInst.getSharedMesh().updateModelBound();

//                            if (((BoundingSphere)meshInst.getSharedMesh().getModelBound()).getRadius() == 0.0f)
//                                continue; // for a breakpoint..
                    }
                    // add all the kids
                    queue.addAll(current.getChildren());
                }

                if (modelInst != null)
                    modelInst.calculateBoundingSphere();

                if (objectCollection == null)
                    return false;


                //objectCollection.getPScene().submitTransformsAndGeometry();
                ////objectCollection.getJScene().updateWorldBound();
                //objectCollection.getJScene().updateRenderState();

                return true;
            }
        };
        sharedAsset.setInitializer(init);
    }
    
    /**
     * Adds this chiar to an object collection,
     * the collection will use the chair's geometry for characters to avoid
     * obstacles while using behaviors and to be found to sit on.
     * @param objs
     */
    public void setObjectCollection(ObjectCollectionBase objs)
    {
//        if (objs instanceof ObjectCollection)
//        {
            objectCollection = objs;
            objs.addObject(this);
//        }
//        else
//            System.out.println("Error: chair recieved a none compatible object collection");
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
    
    public Vector3f getPositionRef()
    {
        return modelInst.getTransform().getWorldMatrix(true).getTranslation();
    }
    
    public Vector3f getTargetPositionRef()
    {
//        Vector3f goalOffset = modelInst.getChild(1).getTransform().getWorldMatrix(false).getLocalZ().mult(sittingDistance);
//        return modelInst.getTransform().getWorldMatrix(false).getTranslation().add(goalOffset);
        
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.getChild(0);
        Vector3f result = mesh.getTransform().getWorldMatrix(false).getTranslation();
                
        return result.add(getTargetForwardVector().mult(goalForwardOffset));
        
    }
    
    public Vector3f getRightVector()
    {
        return modelInst.getTransform().getWorldMatrix(false).getLocalX();
    }
    
    public Vector3f getForwardVector()
    {
        return modelInst.getTransform().getWorldMatrix(false).getLocalZ();
    }
    
    public Vector3f getTargetForwardVector()
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
        result.setCenter(modelInst.getTransform().getWorldMatrix(false).getTranslation().add(result.getCenterRef()));
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

    public float getDesiredDistanceFromOtherTargets() {
        return desiredDistanceFromOtherChairs;
    }

    public void setDesiredDistanceFromOtherChairs(float desiredDistanceFromOtherChairs) {
        this.desiredDistanceFromOtherChairs = desiredDistanceFromOtherChairs;
    }

    public void destroy() 
    {
        setOwner(null);
        setOccupied(true);
        objectCollection.removeObject(this);
    }
    
}
