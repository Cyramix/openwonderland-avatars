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
package imi.loaders.collada;



import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.animation.AnimationComponent;
import imi.loaders.collada.Collada;
import imi.scene.animation.AnimationGroup;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;





/**
 * This class is used to wrap the Collada loader and expose higher-level
 * functionality.
 * @author Chris Nagle
 */
public class CharacterLoader
{
    /** The collada loader that this class wraps **/
    private Collada m_colladaLoader = new Collada();

    /**
     * Load the specified collada file and parse out the skeleton, associate it
     * with the provided PScene, and return the skelton node.
     * @param pScene
     * @param rigLocation
     * @return The completed skeleton node
     */
    public SkeletonNode loadSkeletonRig(PScene pScene, URL rigLocation)
    {
        //  Load the collada file to the PScene
        m_colladaLoader.clear();
        try
        {
            //  Load only the rig and geometry.
            m_colladaLoader.setLoadFlags(true, true, false);
            if (m_colladaLoader.load(pScene, rigLocation) == false) // uh oh
                Logger.getLogger(CharacterLoader.class.toString()).log(Level.SEVERE, "COLLADA Loader returned false!");
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        }

        return m_colladaLoader.getSkeletonNode();
    }

    /**
     * Load the geometry in the specified collada file into the provided pscene.
     * @param loadingPScene
     * @param geometryLocation
     * @return True on success, false otherwise.
     */
    public boolean loadGeometry(PScene loadingPScene, URL geometryLocation)
    {
        boolean result = false;
        //  Load the collada file to the PScene
        m_colladaLoader.clear();
        try
        {
            //  Load only the geometry.
            m_colladaLoader.setLoadFlags(false, true, false);
            m_colladaLoader.setAddSkinnedMeshesToSkeleton(false);
            m_colladaLoader.load(loadingPScene, geometryLocation);
            result = true;
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * This method parses the collada file at the specified location using the
     * provided skeleton node and pscene. If mergeToGroup is a non-negative value,
     * it is used as the index of the animation group that the newly loaded data
     * should be merged with.
     * @param loadingPScene
     * @param owningSkeleton
     * @param animationLocation
     * @param mergeToGroup
     * @return True on success, false otherwise
     */
    public boolean loadAnimation(PScene loadingPScene, SkeletonNode owningSkeleton, URL animationLocation, int mergeToGroup)
    {
        //  Load the collada file to the PScene
        m_colladaLoader.clear();
        
        m_colladaLoader.setLoadFlags(false, false, true);
        m_colladaLoader.setSkeletonNode(owningSkeleton);
        boolean result = m_colladaLoader.load(loadingPScene, animationLocation);

        if (mergeToGroup >= 0)
            mergeLastToAnimationGroup(owningSkeleton, mergeToGroup);

        return result;
    }

    /**
     * This method merges the last animation group within a component with the
     * animation group indicated by the provided index. No action is taken if
     * the specified group index already refers to the last group.
     * @param pSkeletonNode
     * @param groupIndex
     */
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
            System.out.println("The animation where loaded in the wrong order, facial animation must be loaded last");
    }

}




