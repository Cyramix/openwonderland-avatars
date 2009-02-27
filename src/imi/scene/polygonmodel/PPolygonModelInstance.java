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
package imi.scene.polygonmodel;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.utils.PRenderer;
import imi.scene.utils.tree.BoundingVolumeCollector;
import imi.scene.utils.tree.TreeTraverser;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
/**
 *
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PPolygonModelInstance extends PNode implements Serializable
{
    private PSphere   m_boundingSphere = null; // The overall bounding sphere
    
    private transient PSphere [] debugSpheres = new PSphere[2];
    
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
//        if (debugSpheres[0] != null)
//        {
//            renderer.setOrigin(PMatrix.IDENTITY);
//            renderer.drawSphere(debugSpheres[0], 10, 10, false);
//            renderer.drawSphere(debugSpheres[1], 10, 10, false);
//        }
//        if (m_boundingSphere != null)
//        {
//            PMatrix origin       = getTransform().getWorldMatrix(false);
//            renderer.setOrigin(origin);
//            renderer.drawSphere(m_boundingSphere, 6, 6, false);
//            renderer.drawTriangle(Vector3f.UNIT_X.mult(8.0f), Vector3f.UNIT_X.mult(-8.0f), Vector3f.UNIT_Z.mult(8.0f));
//        
//        }
        /////////////////////////////////////////
    }
    
    public void setDebugSphere(PSphere bv, int index) 
    {
        debugSpheres[index] = bv;
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
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
        debugSpheres = new PSphere[2];
    }
}
