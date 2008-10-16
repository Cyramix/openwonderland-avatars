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


import java.util.ArrayList;

import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationGroup;
import imi.loaders.collada.Collada;
import java.net.URL;





/**
 *
 * @author Chris Nagle
 */
public class CharacterLoader
{
    private PScene          m_pScene = null;
    private Collada         m_pCollada = null;
    private SkeletonNode    m_pSkeleton = null;
    private ArrayList       m_SkinnedMeshes = null;




    //  Loads a rig.
    public SkeletonNode loadSkeletonRig(PScene pScene, URL rigLocation)
    {
        boolean bResult = false;

        m_pScene = pScene;

//        pScene.setUseRepository(false);

        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        PScene colladaScene = new PScene("COLLADA Scene", pScene.getWorldManager());
        colladaScene.setUseRepository(false);
        try
        {
            //  Load only the rig and geometry.
            m_pCollada.setLoadFlags(true, true, false);
            bResult = m_pCollada.load(pScene, rigLocation);
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        }

//        pScene.setUseRepository(true);

        if (bResult)
            setSkeleton(m_pCollada.getSkeletonNode());

        return(m_pSkeleton);
    }


    //  Loads replacement geometry from a file.
    public boolean loadGeometry(PScene pScene, SkeletonNode pSkeleton, URL geometryLocation)
    {
        boolean bResult = false;

        m_pScene = pScene;
        setSkeleton(pSkeleton);

//        pScene.setUseRepository(false);

        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        PScene colladaScene = new PScene("COLLADA Scene", pScene.getWorldManager());
        colladaScene.setUseRepository(false);
        try
        {
            //  Load only the geometry.
            m_pCollada.setLoadFlags(false, true, false);
            m_pCollada.setAddSkinnedMeshesToSkeleton(false);
            m_pCollada.setSkeletonNode(pSkeleton);
            bResult = m_pCollada.load(pScene, geometryLocation);
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        }
        
        return(true);
    }


    //  Loads replacement animation from a file.
    public boolean loadAnimation(PScene pScene, SkeletonNode pSkeleton, URL animationLocation)
    {
        boolean bResult = false;

        m_pScene = pScene;
        setSkeleton(pSkeleton);

//        pScene.setUseRepository(false);

        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        PScene colladaScene = new PScene("COLLADA Scene", pScene.getWorldManager());
        colladaScene.setUseRepository(false);
        
        m_pCollada.setLoadFlags(false, false, true);
        m_pCollada.setSkeletonNode(pSkeleton);
        bResult = m_pCollada.load(pScene, animationLocation);

        mergeMultipleAnimationGroupsIntoOne(pSkeleton);

        return(true);
    }



    //  Sets the Skeleton.
    public void setSkeleton(SkeletonNode pSkeleton)
    {
        m_pSkeleton = pSkeleton;
    }



    //  Deletes a SkinnedMesh from the Skeleton.
    public boolean deleteSkinnedMesh(SkeletonNode pSkeleton, String skinnedMeshName)
    {
        int a;
        PNode pChildNode;

        for (a=0; a<pSkeleton.getChildrenCount(); a++)
        {
            pChildNode = pSkeleton.getChild(a);

            if (pChildNode instanceof PPolygonSkinnedMesh)
            {
                PPolygonSkinnedMesh pSkinnedMesh = (PPolygonSkinnedMesh)pChildNode;
                if (pSkinnedMesh.getName().equals(skinnedMeshName))
                {
                    pSkeleton.removeChild(pChildNode);
                    return(true);
                }
            }
        }
    
        return(false);
    }

    //  Adds a SkinnedMesh to the Skeleton.
    public void addSkinnedMesh(SkeletonNode pSkeleton, String skinnedMeshName)
    {
        //  Find the SkinnedMesh that is the replacement.
        PPolygonSkinnedMesh pSkinnedMesh = m_pCollada.findPolygonSkinnedMesh(skinnedMeshName);
        if (pSkinnedMesh != null)
        {
            System.out.println("Added SkinnedMesh '" + skinnedMeshName + "' to skeleton.");

            //  Link the SkinnedMesh to the Skeleton.
            pSkinnedMesh.linkJointsToSkeletonNode(pSkeleton);

            pSkeleton.addChild(pSkinnedMesh);
        }
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

                if (pChildNode instanceof PPolygonSkinnedMesh)
                {
                    PPolygonSkinnedMesh pSkinnedMesh = (PPolygonSkinnedMesh)pChildNode;
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




