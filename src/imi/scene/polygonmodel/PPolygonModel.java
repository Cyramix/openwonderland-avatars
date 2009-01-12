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


import imi.utils.PMathUtils;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.boundingvolumes.PCube;
import com.jme.math.Vector3f;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.utils.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javolution.util.FastList;

/**
 *
 * Note : ! this class was not used in a long time and is probably
 * going to be deprecated soon! - for reference only!
 * 
 * @author Chris Nagle
 */
public class PPolygonModel extends PNode implements Serializable
{
    private PCube                       m_BoundingCube      = new PCube();
    private PSphere                     m_BoundingSphere    = new PSphere();

    private transient boolean   m_bInBatch = false;

    //  Constructor.
    public PPolygonModel()
    {
        setName("Untitled");
        setTransform(new PTransform());
    }
    
    public PPolygonModel(String name)
    {
        setName(name);
        setTransform(new PTransform());
    }

    @Override
    public void draw(PRenderer renderer)
    {
//        if (getTransform() != null)
//        {
//             // Set world origin
//            PMatrix origin       = getTransform().getWorldMatrix(false);
//            Quaternion rotation  = new Quaternion();
//            Vector3f translation = new Vector3f();
//            Vector3f scale       = new Vector3f();
//            origin.getRotation(rotation);
//            origin.getTranslation(translation);
//            origin.getScale(scale);
//            renderer.setOrigin(rotation, translation, scale);
//        }
                
        renderer.drawPPolygonModel(this);
    }
    
    /**
     * Flipping the normals will make this model and all of its meshes dirty
     */
    public void flipNormals()
    {
        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode kid = getChild(i);
            if (kid instanceof PPolygonMesh)
                ((PPolygonMesh)kid).flipNormals();
        }
        setDirty(true, true);
    }

    /**
     * Build the JME geometry according to our geometry
     */
    public void submitGeometry(PPolygonTriMeshAssembler assembler) 
    {
        FastList<PNode> list = new FastList<PNode>();
        list.addAll(getChildren());
        
        while(!list.isEmpty())
        {
            PNode current = list.removeFirst();
            if (current instanceof PPolygonMesh)
                ((PPolygonMesh)current).submit(assembler);
            
            list.addAll(current.getChildren());    
        }
    }
    
    /**
     * Toggling smooth normals will make this model and all of its meshes dirty
     */
    public void toggleSmoothNormals()
    {
        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode kid = getChild(i);
            if (kid instanceof PPolygonMesh)
                ((PPolygonMesh)kid).setSmoothNormals(!((PPolygonMesh)kid).getSmoothNormals());
        }
        setDirty(true, true);
    }
    
    /**
     * use this function to set a single material to all immediate kid meshes\models.
     */
    public void setMaterial(PMeshMaterial pMaterial, int numberOfTextures)
    {
        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode kid = getChild(i);
            if (kid instanceof PPolygonMesh)
            {
                ((PPolygonMesh)kid).setMaterial(pMaterial);
                ((PPolygonMesh)kid).setNumberOfTextures(numberOfTextures);
            }
            else if (kid instanceof PPolygonModel)
                ((PPolygonModel)kid).setMaterial(pMaterial, numberOfTextures);
        }
    }
    
    // screws up culling... less draw calls, maybe useless
    public void combinePolygonMesh(PPolygonMesh Mesh)
    {
        // Note : old function, needs testing
        
        // First, determine if this mesh matches the material of any
        // existing meshes
        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode kid = getChild(i);
            if (kid instanceof PPolygonMesh)
            {
                if ( ((PPolygonMesh)kid).getMaterialCopy().hashCode() == Mesh.getMaterialCopy().hashCode() ) // Found a match, combine!
                {
                    ((PPolygonMesh)kid).combinePolygonMesh(Mesh, true); // Done! 
                    return;
                }
                else
                {
                    // Just add it
                    addChild(new PPolygonMesh(Mesh));
                    return;
                }
            }   
        }
    }
    
    /**
     * Returns an <code>ArrayList</code> of all the immediate kid meshes using the specified material.
     * @param material The material to match with
     * @return ArrayList of all <code>PPolygonMesh</code> objects using <code>material</code>
     */
    public ArrayList<PPolygonMesh> getMeshsByMaterial(PMeshMaterial material)
    {
        ArrayList<PPolygonMesh> meshes = new ArrayList<PPolygonMesh>();

        for (int i = 0; i < getChildrenCount(); i++)
        {
            PNode kid = getChild(i);
            if (kid instanceof PPolygonMesh)
            {
                if (((PPolygonMesh)kid).getMaterialCopy().hashCode() == material.hashCode()) // Dahlgren - Changed to use hashcode rather than equals
                    meshes.add(((PPolygonMesh)kid));
            }
        }
        
        return meshes;
    }
  
    //  Begins a Batch.
    public void beginBatch()
    {
        m_bInBatch = true;
    }

    //  Ends a Batch.
    public void endBatch()
    {
        m_bInBatch = false;
        
        // Calculate the bounding cube surrounding all the sub PolygonMeshes.
        calculateBoundingCube();
        
        
        
        // Calculate the bounding sphere surrounding all the sub PolygonMeshes.
        calculateBoundingSphere();
    }

    //  Retrieves boolean indicating whether we're in a Batch.
    public boolean inBatch()
    {
        return m_bInBatch;
    } 
    
    //  Calculates the bounding cube surrounding all the sub PolygonMeshes.
    protected void calculateBoundingCube()
    {
        PPolygonMesh    pPolygonMesh;
        Vector3f        MinCorner           =   new Vector3f(Vector3f.ZERO);
        Vector3f        MaxCorner           =   new Vector3f(Vector3f.ZERO);
        
        for (int i = 0; i < getChildrenCount(); i++)
        {
            if (!(getChild(i) instanceof PPolygonMesh))
                continue;
            
           pPolygonMesh = (PPolygonMesh)getChild(i);
           
           if (i == 0)
           {
               MinCorner = pPolygonMesh.getBoundingCube().getMin();
               MaxCorner = pPolygonMesh.getBoundingCube().getMax();
           }
           else
           {
               PMathUtils.min(MinCorner, pPolygonMesh.getBoundingCube().getMin());
               PMathUtils.max(MaxCorner, pPolygonMesh.getBoundingCube().getMax());
           }
        }
        
        m_BoundingCube.set(MinCorner, MaxCorner);
    }

    //  Calculates the bounding sphere surrounding all the sub PolygonMeshes.
    protected void calculateBoundingSphere()
    {
        PPolygonMesh    pPolygonMesh;
        Vector3f        boundingSphereCenter = new Vector3f(Vector3f.ZERO);
        float           fBoundingSphereRadius = 0.0f;
        float           flocalBoundingSphereRadius = 0.0f;
        
        // Calculate the overallcenter of the BoundingSphere.
        int centers = 0;
        for (int i = 0; i < getChildrenCount(); i++)
        {
            if (!(getChild(i) instanceof PPolygonMesh))
                continue;
            
            pPolygonMesh = (PPolygonMesh)getChild(i);
            boundingSphereCenter = boundingSphereCenter.add(pPolygonMesh.getBoundingSphere().getCenter());
            centers++;
        }
        
        boundingSphereCenter = boundingSphereCenter.divide((float)centers);
        
        // Calculate the overall radius of the BoundingSphere
        for (int i = 0; i < getChildrenCount(); i++)
        {
            if (!(getChild(i) instanceof PPolygonMesh))
                continue;
            
            pPolygonMesh = (PPolygonMesh)getChild(i);
            
            // Calculate the distance between two points.
            flocalBoundingSphereRadius = boundingSphereCenter.distance(pPolygonMesh.getBoundingSphere().getCenter());
            flocalBoundingSphereRadius += pPolygonMesh.getBoundingSphere().getRadius();
            
            if (flocalBoundingSphereRadius > fBoundingSphereRadius)
                fBoundingSphereRadius = flocalBoundingSphereRadius;
        }
        
        m_BoundingSphere.set(boundingSphereCenter, fBoundingSphereRadius);
    }
        
    //  Gets the BoundingCube.
    public PCube getBoundingCube()
    {
        return m_BoundingCube;
    }

    //  Sets the BoundingCube.
    public void setBoundingCube(PCube pBoundingCube)
    {
        if(pBoundingCube != null)
            m_BoundingCube.set(pBoundingCube.getMin(), pBoundingCube.getMax());
    }

    //  Gets the BoundingSphere.
    public PSphere getBoundingSphere()
    {
        return m_BoundingSphere;
    }

    //  Sets the BoundingSphere.
    public void setBoundingSphere(PSphere pBoundingSphere)
    {
        if (pBoundingSphere != null)
            m_BoundingSphere.set(pBoundingSphere.getCenter(), pBoundingSphere.getRadius());
    }
    
    /**
     * Combines two <code>PPolygonModel</code>s. If desired, meshes with similar
     * materials can be combined to optimize the model. This will destroy any 
     * hierarchies dependant on those meshes however (the transforms will be flattened).
     * @param other The mesh to combine with.
     * @param bCombineSimilarMeshes If true, merge all meshes with similar 
     * materials. If false, simply add the new meshes.
     */
    public void combinePolygonModel(PPolygonModel other, boolean bCombineSimilarMeshes)
    {   
        // Note : old function, needs testing
        
        ArrayList<PPolygonMesh> remaining = new ArrayList<PPolygonMesh>();
        // If no combining is desired, just do it the easy way =)
        if (bCombineSimilarMeshes == false)
        {
            for (int i = 0; i < other.getChildrenCount(); ++i)
                addChild(other.getChild(i));
        }
        else // Combine similar meshes
        {
            for (int i = 0; i < getChildrenCount(); i++)
            {
                if (!(getChild(i) instanceof PPolygonMesh))
                    continue;

                PPolygonMesh pPolygonMesh = (PPolygonMesh)getChild(i);

                for (int j = 0; j < other.getChildrenCount(); j++)
                {
                    if (!(other.getChild(i) instanceof PPolygonMesh))
                        continue;

                    PPolygonMesh pOtherPolygonMesh = (PPolygonMesh)other.getChild(i);

                    if (pPolygonMesh.getMaterialCopy().equals(pOtherPolygonMesh.getMaterialCopy()))
                        pPolygonMesh.combinePolygonMesh(pOtherPolygonMesh, false);
                    else
                        remaining.add(pOtherPolygonMesh);
                }
            }
            // Check for similar materials in the newly added meshes
            for (int i = 0; i < remaining.size(); i++)
                combinePolygonMesh(remaining.get(i));
        }
    }
    
    public void dump()
    {
        System.out.println("PolygonModel:  " + getName());
    }
    
    /**
     * Compile the models meshes into as few meshes as possible
     * by combining meshes that are using the same material. 
     * This helps to optimize the meshes and prevent unnecessary state changes.
     * <b>Warning!</b> This compilation will potentially ruin a 
     * hierarchy if this model is being used for animation or 
     * any other sort of heirarchical transformation scheme.
     */
    public void compile()
    {
        // Note : old function, needs testing
        
        ArrayList<PPolygonMesh> Bucket;
        // Sort all the meshes into a hashtable
        Hashtable MaterialHash = new Hashtable<PMeshMaterial, ArrayList<PPolygonMesh>>();
        for (int i = 0; i < getChildrenCount(); i++) // For each mesh
        {
            if (!(getChild(i) instanceof PPolygonMesh))
                continue;

            PPolygonMesh mesh = (PPolygonMesh)getChild(i);
            
            if (MaterialHash.containsKey(mesh.getMaterialCopy())) // Already have a "bucket" for this material
            {
                Bucket = (ArrayList<PPolygonMesh>)MaterialHash.get(mesh.getMaterialCopy());
                Bucket.add(mesh);
            }
            else
            {
                // Allocate the key
                Bucket = new ArrayList<PPolygonMesh>();
                Bucket.add(mesh);
                MaterialHash.put(mesh.getMaterialCopy(), Bucket);
            }
        }
        // Sorted, now nuke the data members
        removeAllChildren(); // ? really?
        // Now go through and sort all the contained meshes
        Set<PMeshMaterial> keys = MaterialHash.keySet();
        Iterator<PMeshMaterial> iter = keys.iterator();
        while (iter.hasNext())
        {
            PMeshMaterial currentMaterial = iter.next();
            Bucket = (ArrayList<PPolygonMesh>) MaterialHash.get(currentMaterial);
            PPolygonMesh bufferMesh = new PPolygonMesh();
            bufferMesh.setMaterial(currentMaterial);
            
            // For each mesh in this bucket
            for (PPolygonMesh mesh : Bucket)
                bufferMesh.combinePolygonMesh(mesh, true);
                
            addChild(bufferMesh);
        }
        
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
        m_bInBatch = false;
    }
}
