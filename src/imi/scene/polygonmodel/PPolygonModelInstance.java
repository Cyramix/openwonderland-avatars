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
import imi.scene.PCube;
import imi.scene.PSphere;
import imi.scene.utils.traverser.BoundingVolumeCollector;
import imi.scene.utils.traverser.TreeTraverser;
import imi.utils.MathUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import javolution.util.FastTable;

/**
 * Used as a grouping node
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PPolygonModelInstance extends PNode implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private PSphere   m_boundingSphere = null; // The overall bounding sphere
    
    public PPolygonModelInstance(String name, PTransform transform, FastTable<PPolygonMeshInstance> meshes)
    {
        super(name, null, null, transform);
        if (meshes != null)
        {
            for (int i = 0; i < meshes.size(); i++)
                addChild(meshes.get(i));
        }
    }

    public PPolygonModelInstance(String name, PMatrix origin, FastTable<PPolygonMeshInstance> meshes)
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
        Vector3f        boundingSphereCenter;
        float           fBoundingSphereRadius = 0.0f;
        float           fLocalBoundingSphereRadius = 0.0f;
        
        // Collect spheres
        BoundingVolumeCollector processor = new BoundingVolumeCollector();
        TreeTraverser.breadthFirst(this, processor);
        FastTable<PSphere> spheres = processor.getSpheres();
        FastTable<PCube> cubes     = processor.getCubes();
        
        // Calculate the overallcenter of the BoundingSphere (approximated).
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        for (PCube cube : cubes)
        {
            MathUtils.min(min, cube.getMin());
            MathUtils.max(max, cube.getMax());
        }
        boundingSphereCenter = new PCube(min, max).getCenter();
        
        // Calculate the overall radius of the BoundingSphere
        for (PSphere sphere : spheres)
        {
            // Calculate the distance between two points.
            fLocalBoundingSphereRadius = boundingSphereCenter.distance(sphere.getCenterRef());
            fLocalBoundingSphereRadius += sphere.getRadius();
            
            if (fLocalBoundingSphereRadius > fBoundingSphereRadius)
                fBoundingSphereRadius = fLocalBoundingSphereRadius;
        }
        
        m_boundingSphere = new PSphere(boundingSphereCenter, fBoundingSphereRadius);
    }   
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }
}
