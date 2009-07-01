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
package imi.loaders;



import imi.repository.AssetDescriptor;
import imi.repository.Repository;
import imi.repository.RepositoryUser;
import imi.repository.SharedAsset;
import imi.scene.PNode;
import imi.scene.PScene;

import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationGroup;
import imi.scene.SkeletonNode;
import imi.utils.AvatarObjectInputStream;
import imi.utils.AvatarObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;





/**
 * This class is used to wrap the Collada loader and expose higher-level
 * functionality.
 * @author Chris Nagle
 */
public class CharacterLoader implements RepositoryUser
{
    /** Logger ref! **/
    private static final Logger logger = Logger.getLogger(CharacterLoader.class.getName());

    // Temporary, TODO remove this field
    // Provides the root directory for the baf cache, this will be removed
    // once the baf files are deployed into the asset server
    private static String bafCacheURL = System.getProperty("BafCacheDir", null);

    private Repository repository = null;

    /** Used to indicate status of requested assets **/
    private Semaphore m_bWaitingOnAsset = new Semaphore(0);
    private SharedAsset m_asset = null;

    /**
     * Construct a new instance using the provided repository to load from.
     * @param repositoryToUse
     */
    public CharacterLoader(Repository repositoryToUse)
    {
        repository = repositoryToUse;
    }

    /**
     * Load the specified collada file and parse out the skeleton, associate it
     * with the provided PScene, and return the skelton node.
     * @param pScene
     * @param rigLocation
     * @return The completed skeleton node
     */
    public SkeletonNode loadSkeletonRig(URL rigLocation)
    {
        PScene scene = loadCollada(rigLocation);
        PNode skeletonRoot = scene.findChild("skeletonRoot");
        return (SkeletonNode)skeletonRoot.getParent();
    }

    /**
     * Load the geometry in the specified collada file into the provided pscene.
     * @param geometryLocation
     * @return The loaded scene
     */
    public PScene loadGeometry(URL geometryLocation)
    {
        PScene scene = loadCollada(geometryLocation);
        return scene;
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
     * @param bUseBinaryFiles
     * @param reductionFactor 1 for no reduction, >1 for reduction in keyframe density.
     * @return True on success, false otherwise
     */
    public boolean loadAnimation(PScene loadingPScene,
                                SkeletonNode owningSkeleton,
                                URL animationLocation,
                                int mergeToGroup,
                                boolean bUseBinaryFiles,
                                int reductionFactor)
    {
        boolean result = false;
        // check for binary version
        AnimationGroup newGroup = null;
        URL binaryLocation = null;

        // Try to load the binary version if that was specified
        if (bUseBinaryFiles)
        {
            try
            {
                if (bafCacheURL==null)
                    binaryLocation = new URL(animationLocation.toString().substring(0, animationLocation.toString().length() - 3) + "baf");
                else
                    binaryLocation = new URL(bafCacheURL+animationLocation.getFile().toString().substring(0, animationLocation.getFile().toString().length() - 3) + "baf");
                newGroup = loadBinaryAnimation(binaryLocation);
            } catch (Exception ex)
            {
                logger.severe(ex.getMessage());
            }
        }

        if (newGroup != null) // Success loading from binary file?
        {
            // Debugging output
            logger.info("Loaded binary file " + binaryLocation.getFile() + ".");
            owningSkeleton.getAnimationComponent().addGroup(newGroup);
            result = true;
        }
        else // otherwise use the collada loader
        {
            SkeletonNode skeleton = loadSkeletonRig(animationLocation);
            // skeleton may not have loaded any animations
            if (skeleton.getAnimationComponent().getGroupCount() > 0)
            {
                newGroup = skeleton.getAnimationGroup(skeleton.getAnimationComponent().getGroupCount() - 1);
                owningSkeleton.getAnimationComponent().addGroup(newGroup);
                boolean makedir = false;

                // Serialize it for the next round
                if (bafCacheURL != null) {
                    try {
                        // Create the directory
                        File f = new File(binaryLocation.toURI());
                        File dir = f.getParentFile();
                        if (!dir.exists())
                            makedir = dir.mkdirs();

                        if (!makedir) {
                            logger.log(Level.SEVERE, "DIRECTORY CREATION FAILED: cacheing animations");
                        }

                    } catch (URISyntaxException ex) {
                        Logger.getLogger(CharacterLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (bUseBinaryFiles)
                    writeAnimationGroupToDisk(binaryLocation, owningSkeleton);
                result = true;
            }
            else
            {
                logger.severe("Unable to load animation from " + animationLocation.toString());
                newGroup = null;
                result = false; // failure
            }
        }

        // Merge
        if (mergeToGroup >= 0 && newGroup != null)
            mergeLastToAnimationGroup(owningSkeleton, mergeToGroup);
        return result;
    }

    private PScene loadCollada(URL location)
    {
        m_asset = new SharedAsset(repository, new AssetDescriptor(
                SharedAsset.SharedAssetType.COLLADA, location));

        repository.loadSharedAsset(m_asset, this);
        try {
            // Wait for asset to load
            m_bWaitingOnAsset.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(CharacterLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return (PScene)m_asset.getAssetData();
    }

    /**
     * Helper method to load a binary animation file from the specified URL
     * @param location
     * @return
     */
    private AnimationGroup loadBinaryAnimation(URL location)
    {
        AnimationGroup result = null;
        FileInputStream fis = null;
        AvatarObjectInputStream in = null;

        try
        {
            fis = new FileInputStream(location.getFile());
            in = new AvatarObjectInputStream(fis);
            result = (AnimationGroup)in.readObject();
            in.close();
        }
        catch (FileNotFoundException ex) // Not a big deal
        {
            logger.info("Binary animation file \"" + location.toString() + "\" does not exist.");
        }
        catch(IOException ex)
        {
            logger.severe("Error attempting to load binary animation: " + location.toString());
            logger.severe(ex.getMessage());
            ex.printStackTrace();
        }
        catch(ClassNotFoundException ex)
        {
            logger.severe("Error attempting to load binary animation: " + location.toString());
            logger.severe(ex.getMessage());
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
        if (animationComponent.getGroupCount() > groupIndex)
        {
            AnimationGroup group = animationComponent.getGroup(groupIndex);
            AnimationGroup lastGroup = animationComponent.getGroup(animationComponent.getGroupCount() - 1);
            
            if (group != lastGroup)
            {
                group.addCycle(lastGroup.getCycle(0));
                animationComponent.removeGroup(lastGroup);
            }
        } else
            logger.severe("The animation were loaded in the wrong order, facial animation must be loaded last");
    }

    /**
     * Helper method to write the last AnimationGroup in the skeleton to disk.
     * @param outputFileLocation Where the file should be written
     * @param owningSkeleton The skeleton
     */
    private void writeAnimationGroupToDisk(URL outputFileLocation, SkeletonNode owningSkeleton)
    {
        AnimationGroup groupToSerialize = owningSkeleton.getAnimationComponent().getLastGroup();

        FileOutputStream fos = null;
        AvatarObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(outputFileLocation.getFile());
            out = new AvatarObjectOutputStream(fos);
            out.writeObject(groupToSerialize);
            out.close();
        }
        catch(IOException ex)
        {
            logger.severe("Exception while trying to write binary data file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    
    public void receiveAsset(SharedAsset asset) {
        m_asset = asset;
        m_bWaitingOnAsset.release();
    }

}




