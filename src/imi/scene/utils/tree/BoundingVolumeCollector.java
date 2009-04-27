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
package imi.scene.utils.tree;

import com.jme.math.Vector3f;
import imi.scene.PNode;
import imi.scene.boundingvolumes.PCube;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import java.util.ArrayList;

/**
 * This class traverses a given tree and 
 * accumulates the bounding volumes.
 * @author Lou
 */
public class BoundingVolumeCollector implements NodeProcessor
{
    // Accumulate herein!
    private ArrayList<PSphere> spheres = new ArrayList<PSphere>();
    private ArrayList<PCube>   cubes   = new ArrayList<PCube>();

    private Vector3f translation = new Vector3f();

    public BoundingVolumeCollector()
    {
    }

    public boolean processNode(PNode currentNode) 
    {
        if (currentNode instanceof PPolygonMeshInstance)
        {
            PPolygonMeshInstance meshInst = (PPolygonMeshInstance)currentNode;
            if (meshInst.isCollidable())
            {
                meshInst.getTransform().getLocalMatrix(false).getTranslation(translation);

                PSphere sphere = new PSphere(translation.add(meshInst.getGeometry().getBoundingSphere().getCenterRef()),
                                             meshInst.getGeometry().getBoundingSphere().getRadius());
                spheres.add(sphere);

                PCube cube = new PCube(meshInst.getGeometry().getBoundingCube(), translation);
                cubes.add(cube);
            }
        }
        
        return true;
    }

    public void clear()
    {
        spheres.clear();
        cubes.clear();
    }
    
    public ArrayList<PSphere> getSpheres()
    {
        return spheres;
    }
    
    public ArrayList<PCube> getCubes()
    {
        return cubes;
    }

}
