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
    /** Logger ref **/
    private final static Logger logger = Logger.getLogger(CharacterLoader.class.getName());
    /** The collada loader that this class wraps **/
    private final Collada m_colladaLoader = new Collada();

    /**
     * Load the specified collada file and parse out the skeleton, associate it
     * with the provided PScene, and return the skelton node.
     * @param pscene
     * @param rigLocation
     * @return The completed skeleton node
     */
    public SkeletonNode loadSkeletonRig(PScene pscene, URL rigLocation)
    {
        //  Load the collada file to the PScene
        m_colladaLoader.clear();
        try
        {
            //  Load only the rig and geometry.
            m_colladaLoader.setLoadFlags(true, true, false);
            if (m_colladaLoader.load(pscene, rigLocation) == false) // uh oh
                logger.log(Level.SEVERE, "COLLADA Loader returned false!");
        }
        catch (Exception ex)
        {
            logger.warning("Exception occured while loading skeleton: " + ex.getMessage());
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
            logger.warning("Exception occured while loading skeleton: " + ex.getMessage());
            ex.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * This method parses the collada file at the specified location using the
     * provided skeleton node and pscene. If mergeToGroupIndex is a non-negative value,
     * it is used as the index of the animation group that the newly loaded data
     * should be merged with (typically index zero).
     * @param loadingPScene The pscene to use for loading
     * @param owningSkeleton The skeleton that will have animations added
     * @param animationLocation Location of the collada file containing animation info
     * @param mergeToGroupIndex Index of the group to merge newly loaded data with
     * @return True on success, false otherwise
     */
    public boolean loadAnimation(PScene loadingPScene, SkeletonNode owningSkeleton, URL animationLocation, int mergeToGroupIndex)
    {
        //  Load the collada file to the PScene
        m_colladaLoader.clear();
        
        m_colladaLoader.setLoadFlags(false, false, true);
        m_colladaLoader.setSkeletonNode(owningSkeleton);

        boolean result = m_colladaLoader.load(loadingPScene, animationLocation);

        if (mergeToGroupIndex >= 0)
            mergeLastToAnimationGroup(owningSkeleton, mergeToGroupIndex);

        return result;
    }

    /**
     * This method merges the last animation group within a component with the
     * animation group indicated by the provided index. No action is taken if
     * the specified group index already refers to the last group.
     * @param skeletonNode
     * @param groupIndex
     */
    private void mergeLastToAnimationGroup(SkeletonNode skeletonNode, int groupIndex)
    {   
        AnimationComponent pAnimationComponent = skeletonNode.getAnimationComponent();
        
        //  Append to the end of the AnimationGroup.
        if (groupIndex >= pAnimationComponent.getGroups().size())
            logger.warning("The provided group index is out of bounds.");
        else
        {
            AnimationGroup group = pAnimationComponent.getGroups().get(groupIndex);
            AnimationGroup lastGroup = pAnimationComponent.getGroups().get(pAnimationComponent.getGroups().size() - 1);

            if (group == lastGroup) // Same instance?
                logger.warning("Provided group index IS the last animation group.");
            else
            {
                group.appendAnimationGroup(lastGroup);
                pAnimationComponent.getGroups().remove(lastGroup);
            }
        }
    }

}




