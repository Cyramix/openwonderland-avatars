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
import imi.scene.PMatrix;
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
    protected PPolygonModelInstance modelInst   = null;
    
    protected ObjectCollection objectCollection = null;
        
    private float sittingDistance = 1.0f;
    
    public Chair(Vector3f position, Vector3f heading)
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
        
        sphereMesh = PMeshUtils.createSphere("Right Chair Obstacle Sphere", Vector3f.ZERO, 1.0f, 6, 6, ColorRGBA.red);
        sphereMesh.setMaterial(geometryMaterial);
        sphereMesh.submit(assembler);
        sphereMesh.getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(2.0f));
        modelInst.addChild(sphereMesh);
        
//        sphereMesh = PMeshUtils.createSphere("Left Chair Obstacle Sphere", Vector3f.ZERO, 1.0f, 6, 6, ColorRGBA.red);
//        sphereMesh.setMaterial(geometryMaterial);
//        sphereMesh.submit(assembler);
//        sphereMesh.getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_X.mult(-2.0f));
//        modelInst.addChild(sphereMesh);
        
        sphereMesh = PMeshUtils.createSphere("Sitting Point Chair Sphere", Vector3f.ZERO, 0.45f, 6, 6, ColorRGBA.green);
        sphereMesh.setMaterial(geometryMaterial);
        sphereMesh.submit(assembler);
        sphereMesh.getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(sittingDistance));
        modelInst.addChild(sphereMesh);
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
        Vector3f goalOffset = modelInst.getChild(1).getTransform().getWorldMatrix(false).getLocalZ().mult(sittingDistance);
        return modelInst.getTransform().getWorldMatrix(false).getTranslation().add(goalOffset);
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
        return modelInst.getTransform().getWorldMatrix(false).getLocalZ();
//        Vector3f goalPos = getGoalPosition();
//        Vector3f pos = getPosition();
//        return goalPos.subtract(pos).normalize();
    }
    
    public PSphere getNearestObstacleSphere(Vector3f myPosition)
    {
//        Vector3f obs0 = modelInst.getChild(0).getTransform().getWorldMatrix(false).getTranslation();
//        Vector3f obs1 = modelInst.getChild(1).getTransform().getWorldMatrix(false).getTranslation();
//        if (obs0.distanceSquared(myPosition) > obs1.distanceSquared(myPosition))
//            return obs1;
//        return obs0;
        
        PPolygonMeshInstance mesh = (PPolygonMeshInstance) modelInst.getChild(0);
        PSphere bv = mesh.getGeometry().getBoundingSphere();
        PSphere result = new PSphere();
        result.set(mesh.getTransform().getWorldMatrix(false).getTranslation(), bv.getRadius());
        return result;
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
        if (modelInst.getParent()!= null)
            modelInst.getParent().removeChild(modelInst);
        modelInst = scene.addModelInstance(modelInst);
    }

    public PSphere getBoundingSphere() 
    {
//        if (modelInst.getBoundingSphere() == null)
//            modelInst.calculateBoundingSphere();
//        return modelInst.getBoundingSphere();
        
        if (modelInst.getBoundingSphere() == null)
            modelInst.calculateBoundingSphere();
        PSphere result = new PSphere(modelInst.getBoundingSphere());
        result.setCenter(modelInst.getTransform().getWorldMatrix(false).getTranslation());
        return result;
    }

    public PPolygonModelInstance getModelInst() {
        return modelInst;
    }

    public Quaternion getQuaternion() {
        return modelInst.getTransform().getWorldMatrix(false).getRotationJME();
    }

}
