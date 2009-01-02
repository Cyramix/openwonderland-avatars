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
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.animation.AnimationComponent;
import imi.loaders.collada.Collada;
import imi.scene.animation.AnimationGroup;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;





/**
 * This class is used to wrap the Collada loader and expose higher-level
 * functionality.
 * @author Chris Nagle
 */
public class CharacterLoader
{
    private static final Logger logger = Logger.getLogger(CharacterLoader.class.getName());
    /** The collada loader that this class wraps **/
    private final Collada m_colladaLoader = new Collada();

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
                logger.log(Level.SEVERE, "COLLADA Loader returned false!");
        }
        catch (Exception ex)
        {
            logger.severe("Exception occured while loading skeleton.");
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
            logger.severe("Exception occured while loading skeleton.");
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
        boolean result = false;

        // check for binary version
        AnimationGroup newGroup = null;
        URL binaryLocation = null;
        try
        {
            binaryLocation = new URL(animationLocation.toString().substring(0, animationLocation.toString().length() - 3) + "baf");
            newGroup = loadBinaryAnimation(binaryLocation);
        } catch (Exception ex)
        {
            Logger.getLogger(CharacterLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (newGroup != null) // Success!
        {
            // Debugging output
            logger.info("Loaded binary file " + binaryLocation.getFile() + ".");
            owningSkeleton.getAnimationComponent().getGroups().add(newGroup);
        }
        else // otherwise use the collada loader
        {
            m_colladaLoader.clear();
            m_colladaLoader.setLoadFlags(false, false, true);
            m_colladaLoader.setSkeletonNode(owningSkeleton);
            result = m_colladaLoader.load(loadingPScene, animationLocation);
            // Serialize it for the next round
            logger.info("Wrote binary file " + binaryLocation.getFile() + ".");
            writeAnimationGroupToDisk(binaryLocation, owningSkeleton);
        }
        // Merge
        if (mergeToGroup >= 0)
            mergeLastToAnimationGroup(owningSkeleton, mergeToGroup);

        return result;
    }

    private AnimationGroup loadBinaryAnimation(URL location)
    {
        AnimationGroup result = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try
        {
            fis = new FileInputStream(location.getFile());
            in = new ObjectInputStream(fis);
            result = (AnimationGroup)in.readObject();
            in.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
        }
        return result;
    }
    /**
     * This method merges the last animation group within a component with the
     * animation group indicated by the provided index. No action is taken if
     * the specified group index already refers to the last group.
     * @param pSkeletonNode
     * @param groupIndex
     */
    private void mergeLastToAnimationGroup(SkeletonNode skeletonNode, int groupIndex)
    {   
        AnimationComponent animationComponent = skeletonNode.getAnimationComponent();
        
        //  Append to the end of the AnimationGroup.
        if (animationComponent.getGroups().size() > groupIndex)
        {
            AnimationGroup group = animationComponent.getGroups().get(groupIndex);
            AnimationGroup lastGroup = animationComponent.getGroups().get(animationComponent.getGroups().size() - 1);
            
            if (group != lastGroup)
            {
                group.appendAnimationGroup(lastGroup);
                animationComponent.getGroups().remove(lastGroup);
            }
        }
        else
            System.out.println("The animation where loaded in the wrong order, facial animation must be loaded last");
    }

    private void writeAnimationGroupToDisk(URL binaryLocation, SkeletonNode owningSkeleton)
    {
        ArrayList<AnimationGroup> animGroups = owningSkeleton.getAnimationComponent().getGroups();
        AnimationGroup groupToSerialize = animGroups.get(animGroups.size() - 1);

        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(binaryLocation.getFile());
            out = new ObjectOutputStream(fos);
            out.writeObject(groupToSerialize);
            out.close();
        }
        catch(IOException ex)
        {
            logger.severe("Exception while trying to write binary data file!");
            ex.printStackTrace();
        }
    }

}




