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
package imi.loaders.collada;



import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationGroup;
import imi.loaders.collada.Collada;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import java.net.URL;





/**
 *
 * @author Chris Nagle
 */
public class CharacterLoader
{
    private Collada         m_pCollada = null;

    //  Loads a rig.
    public SkeletonNode loadSkeletonRig(PScene pScene, URL rigLocation)
    {
        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        try
        {
            //  Load only the rig and geometry.
            m_pCollada.setLoadFlags(true, true, false);
            m_pCollada.load(pScene, rigLocation);
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        }

        return m_pCollada.getSkeletonNode();
    }

    //  Loads replacement geometry from a file.
    public boolean loadGeometry(PScene loadingPScene, URL geometryLocation)
    {
        boolean result = false;
        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        try
        {
            //  Load only the geometry.
            m_pCollada.setLoadFlags(false, true, false);
            m_pCollada.setAddSkinnedMeshesToSkeleton(false);
            m_pCollada.load(loadingPScene, geometryLocation);
            result = true;
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        }
        return result;
    }

    //  Loads replacement animation from a file.
    public boolean loadAnimation(PScene loadingPScene, SkeletonNode pSkeleton, URL animationLocation)
    {
        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        
        m_pCollada.setLoadFlags(false, false, true);
        m_pCollada.setSkeletonNode(pSkeleton);
        boolean result = m_pCollada.load(loadingPScene, animationLocation);

        mergeMultipleAnimationGroupsIntoOne(pSkeleton);

        return result;
    }

    //  Deletes a SkinnedMesh from the Skeleton.
    public boolean deleteSkinnedMesh(SkeletonNode pSkeleton, String skinnedMeshName)
    {
        int a;
        PNode pChildNode;

        for (a=0; a<pSkeleton.getChildrenCount(); a++)
        {
            pChildNode = pSkeleton.getChild(a);

            if (pChildNode instanceof PPolygonSkinnedMeshInstance)
            {
                PPolygonSkinnedMeshInstance pSkinnedMesh = (PPolygonSkinnedMeshInstance)pChildNode;
                if (pSkinnedMesh.getName().equals(skinnedMeshName))
                {
                    pSkeleton.removeChild(pChildNode);
                    return(true);
                }
            }
        }
    
        return(false);
    }

    void mergeMultipleAnimationGroupsIntoOne(SkeletonNode pSkeletonNode)
    {
        AnimationComponent pAnimationComponent = pSkeletonNode.getAnimationComponent();
        
        if (pAnimationComponent.getGroups().size() > 1)
        {
            AnimationGroup pFirstAnimationGroup = pAnimationComponent.getGroups().get(0);
            AnimationGroup pAnimationGroupToMerge;

            while (pAnimationComponent.getGroups().size() > 1)
            {
                pAnimationGroupToMerge = pAnimationComponent.getGroups().get(1);
                pAnimationComponent.getGroups().remove(1);

                //  Append pAnimationGroupToMerge onto the end of the first AnimationGroup.
                pFirstAnimationGroup.appendAnimationGroup(pAnimationGroupToMerge);
            }
        }
    }

    void removeAllSkinnedMeshesExcept(SkeletonNode pSkeleton, String skinnedMeshName)
    {
        int a;
        PNode pChildNode;
        boolean bMeshRemoved = true;

        do
        {
            bMeshRemoved = false;
            for (a=0; a<pSkeleton.getChildrenCount(); a++)
            {
                pChildNode = pSkeleton.getChild(a);

                if (pChildNode instanceof PPolygonSkinnedMeshInstance)
                {
                    PPolygonSkinnedMeshInstance pSkinnedMesh = (PPolygonSkinnedMeshInstance)pChildNode;
                    if (!pSkinnedMesh.getName().equals(skinnedMeshName))
                    {
                        pSkeleton.removeChild(pChildNode);
                        bMeshRemoved = true;
                        break;
                    }
                }
            }
            
        } while (bMeshRemoved);
    }

}




