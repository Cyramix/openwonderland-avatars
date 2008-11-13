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
import imi.loaders.collada.Collada;
import imi.scene.animation.AnimationGroup;
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
    public boolean loadAnimation(PScene loadingPScene, SkeletonNode pSkeleton, URL animationLocation, int mergeToGroup)
    {
        //  Load the collada file to the PScene
        m_pCollada = new Collada();
        
        m_pCollada.setLoadFlags(false, false, true);
        m_pCollada.setSkeletonNode(pSkeleton);
        boolean result = m_pCollada.load(loadingPScene, animationLocation);

        if (mergeToGroup >= 0)
            mergeLastToAnimationGroup(pSkeleton, mergeToGroup);

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

    private void mergeLastToAnimationGroup(SkeletonNode pSkeletonNode, int groupIndex)
    {   
        AnimationComponent pAnimationComponent = pSkeletonNode.getAnimationComponent();
        
        //  Append to the end of the AnimationGroup.
        if (pAnimationComponent.getGroups().size() > groupIndex)
        {
            AnimationGroup group = pAnimationComponent.getGroups().get(groupIndex);
            AnimationGroup lastGroup = pAnimationComponent.getGroups().get(pAnimationComponent.getGroups().size() - 1);
            
            if (group != lastGroup)
            {
                group.appendAnimationGroup(lastGroup);
                pAnimationComponent.getGroups().remove(lastGroup);
            }
        }
        else
        {
            System.out.println("The animation where loaded in the wrong order, facial animation must be loaded last");
//            AnimationGroup newGroup = new AnimationGroup();
//            newGroup.addCycle(new AnimationCycle("All Cycles", 0.0f, 0.0f));
//            pAnimationComponent.getGroups().add(newGroup);
//            mergeLastToAnimationGroup(pSkeletonNode, groupIndex);
        }
    }

}




