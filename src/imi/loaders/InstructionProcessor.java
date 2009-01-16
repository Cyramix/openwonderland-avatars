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


import imi.loaders.collada.*;
import imi.loaders.Instruction;
import imi.loaders.repository.Repository;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.utils.PModelUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.WorldManager;



/**
 * Framework for applying chains of events to something. Specifically collada
 * characters.
 * @author Chris Nagle
 */
public class InstructionProcessor
{
    /** The WorldManager! **/
    private WorldManager        m_wm = null;
    /** PScene that will be used for loading **/
    private PScene              m_loadingPScene = null;
    /** Used for collada character loading tasks **/
    private CharacterLoader     m_characterLoader = null;
    /** Reference to the character's skeleton **/
    private SkeletonNode        m_skeleton = null;
    /** The repository! **/
    private Repository          m_repository = null;
    /** Logger **/
    protected static final Logger logger = Logger.getLogger(InstructionProcessor.class.getName());

    private boolean m_bUseBinaryFiles = true;
    
    /**
     * The world manager is used to create temporal pscenes for the loading process
     * @param wm
     */
    public InstructionProcessor(WorldManager wm)
    {
        m_wm = wm;
        m_repository = (Repository)m_wm.getUserData(Repository.class);
    }

    /**
     * Executes the rootInstruction tree.
     * @param rootInstruction Root of the rootInstruction tree.
     */
    public void execute(Instruction rootInstruction)
    {   
        execute(rootInstruction, true);
    }
    
    /**
     * Execute the provided rootInstruction tree. If nullMembersWhenFinished is
     * true, then all references kept internally will be set to null (except
     * the world manager reference).
     * @param rootInstruction Root of the rootInstruction tree.
     * @param nullMembersWhenFinished
     */
    public void execute(Instruction rootInstruction, boolean nullMembersWhenFinished)
    {
        // Allocate a new character loader to use in this process.
        m_characterLoader = new CharacterLoader(m_repository);
        // Output the contents of the tree to the logger
        logInstructionTree("", rootInstruction);
        // Create a new pscene for the loading process
        m_loadingPScene = new PScene(m_wm);
        // Actually execute the grouping
        executeInstruction(rootInstruction);

 
        // garbage collection...
        if (nullMembersWhenFinished)
        {
            m_characterLoader = null;
            m_loadingPScene   = null;
            m_skeleton        = null;
        }
    }

    /**
     * Retrieve the skeleton
     * @return
     */
    public SkeletonNode getSkeleton() {
        return m_skeleton;
    }

    public void setUseBinaryFiles(boolean bUseBinaryFiles) {
        m_bUseBinaryFiles = bUseBinaryFiles;
    }

    public boolean isUsingBinaryFiles() {
        return m_bUseBinaryFiles;
    }

    /**
     * Execute the provided instruction and recursively traverse the tree executing
     * grouping along the way.
     * @param instruction
     */
    private void executeInstruction(Instruction instruction)
    {
        try 
        {
            switch(instruction.getInstructionType())
            {
                case addSkinnedMesh:
                {
                    if (!addSkinnedMesh((Object[])instruction.getData()))
                        logger.warning("COLLADA configuration ERROR: was not able to ADD a skinned mesh!");
                }
                break;
                case addAttachment:
                {
                    Object [] array = (Object[])instruction.getData();
                    if (!addAttachment(array))
                        logger.warning("COLLADA configuration ERROR: was not able to ADD an ATTACHMENT!");
                }
                break;
                case deleteSkinnedMesh:
                {
                    String skinnedMeshName = instruction.getDataAsString();
                    if (m_skeleton.findAndRemoveChild(skinnedMeshName) == null)
                        logger.warning("COLLADA configuration ERROR: was not able to DELETE a skinned mesh!");
                }
                break;
                case loadAnimation:
                {
                    URL animationLocation = new URL(instruction.getDataAsString());
                    if (!m_characterLoader.loadAnimation(m_loadingPScene, m_skeleton, animationLocation, 0, m_bUseBinaryFiles))
                        logger.warning("COLLADA configuration ERROR: was not able to LOAD ANIMATION!");
                }
                break;
                case loadFacialAnimation:
                {
                    URL animationLocation = new URL(instruction.getDataAsString());
                    if (!m_characterLoader.loadAnimation(m_loadingPScene, m_skeleton, animationLocation, 1, m_bUseBinaryFiles))
                        logger.warning("COLLADA configuration ERROR: was not able to LOAD FACIAL ANIMATION!");
                }
                break;
                case loadHumanoidAvatarBindPose:
                {
                    URL bindPoseLocation = new URL(instruction.getDataAsString());
                    m_skeleton = m_characterLoader.loadSkeletonRig(bindPoseLocation);
                    if (m_skeleton == null)
                        logger.warning("COLLADA configuration ERROR: was not able to LOAD BIND POSE!");
                    else
                    {
                        // sort the meshes!
                        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
                        {
                            String subGroupName = PModelUtils.getSubGroupNameForMesh(meshInst.getName());
                            if (subGroupName != null)
                                m_skeleton.addToSubGroup(meshInst, subGroupName);
                            else
                                m_skeleton.addChild(meshInst);
                        }
                    }
                }
                break;
                case loadGeometry:
                {
                    URL geometryLocation = new URL(instruction.getDataAsString());
                    PScene resultScene = m_characterLoader.loadGeometry(geometryLocation);
                    if (resultScene == null)
                        logger.warning("COLLADA configuration ERROR: was not able to LOAD GEOMETRY!");
                    else
                        m_loadingPScene.addModelInstance(resultScene, new PMatrix());
                }
                break;
                case setSkeleton:
                {
                    if (instruction.getData() instanceof SkeletonNode)
                        m_skeleton = (SkeletonNode) instruction.getData();
                    else
                        m_skeleton = null;   
                }
                break;
                case loadGeometryToSubgroup:
                {
                    if (!loadGeometryToSubgroup((Object[])instruction.getData()))
                        logger.warning("Unable to add geometry to subgroup!");
                }
            }
        }
        catch (MalformedURLException ex){
            logger.log(Level.SEVERE, null, ex); }
        // Recurse!
        for (PNode kid : instruction.getChildren())
        {
            if (kid instanceof Instruction)
                executeInstruction((Instruction)kid);
        }
    }

    /**
     * Convenience method for adding an attachment to a skeleton
     * @param array
     * @return True on success, false otherwise
     */
    private boolean addAttachment(Object[] array) 
    {
        if (m_skeleton == null)
        {
            logger.severe("Attempted to add an attachment, but there was no skeleton to attach on!");
            return false;
        }

        // Extract the mesh name and find it in the loading pscene
        String meshName = (String)array[0];
        PNode node = m_loadingPScene.findChild(meshName);
        PPolygonMeshInstance mesh = null;
        // Verify that we have the right thing
        if (node instanceof PPolygonMeshInstance)
            mesh = (PPolygonMeshInstance)node;
        else if (node == null)
        {
            logger.severe("Specified attachment was not found in the loading pscene!");
            return false;
        }
        else // Not null,. but has the same name as the mesh we want
        {
            logger.severe("Found a node with the right name, but it was not a mesh instance!");
            return false;
        }
        // Get rid of any residual transform information
        mesh.getTransform().setLocalMatrix(new PMatrix());
        
        // Find the joint
        String jointName = (String)array[1];
        SkinnedMeshJoint joint = m_skeleton.getSkinnedMeshJoint(jointName);
        if (joint == null)
        {
            logger.severe("Specified attachment joint not found!");
            return false;
        }

        // bind the mesh up
        mesh.setPScene(m_loadingPScene);
        // Create new joint
        PJoint newJoint = new PJoint(meshName + " joint", new PTransform((PMatrix)array[2]));
        newJoint.addChild(mesh);
        joint.addChild(newJoint);
        
        return true;
    }

    /**
     * Helper method to add a skinned mesh onto the current skeleton.
     * @param parameters
     * @return true on success, false otherwise
     */
    private boolean addSkinnedMesh(Object[] parameters)
    {
        // Extract the necessary information
        String skinnedMeshName = (String)parameters[0];
        String subGroupName = (String)parameters[1];
        //  Find the SkinnedMesh that is the replacement.
        PPolygonSkinnedMesh skinnedMesh = null;

        PNode mesh = m_loadingPScene.findChild(skinnedMeshName);
        if (mesh == null)
            logger.severe("Requested mesh not found - " + skinnedMeshName);
        else if (mesh instanceof PPolygonSkinnedMesh)
        {
            skinnedMesh = (PPolygonSkinnedMesh) mesh;
            // Make an instance
            PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_loadingPScene.addMeshInstance(skinnedMesh, new PMatrix());
            // Add it to the skeleton
            m_skeleton.addToSubGroup(skinnedMeshInstance, subGroupName);
        }
        else if (mesh instanceof PPolygonSkinnedMeshInstance)
        {
//            logger.log(Level.INFO, "Adding meshinstance, \"" + skinnedMeshName + "\" to subgroup, \"" + subGroupName + "\"");
            PPolygonSkinnedMeshInstance skinMeshInstance = (PPolygonSkinnedMeshInstance)mesh;
            skinnedMesh = (PPolygonSkinnedMesh) skinMeshInstance.getGeometry();
            // Make an instance
            PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_loadingPScene.addMeshInstance(skinnedMesh, new PMatrix());
            // Add it to the skeleton
            m_skeleton.addToSubGroup(skinnedMeshInstance, subGroupName);
        }
        else
            logger.severe("Node with same name found, but not skinned mesh");

        return true;
    }

    /**
     * Load the geometry man!
     * @param params
     * @return
     */
    private boolean loadGeometryToSubgroup(Object[] params)
    {
        boolean result = true;
        if (params.length != 2 ||
            params[0] instanceof URL == false ||
            params[1] instanceof String == false)
        {
            logger.warning("Incorrect arguements to loadGeometryToSubgroup instruction, aborting.");
            result = false;
        }
        else
        {
            // first, load the file up
            URL geometryLocation = (URL)params[0];
            String subGroupName = (String)params[1];
            PScene newScene = m_characterLoader.loadGeometry(geometryLocation);
            if (newScene == null)
                result = false;
            else
            {
                // Success loading, now we need to iterate on the meshes and add them all
                Iterable<PPolygonMesh> list = newScene.getLocalGeometryList();
                for (PPolygonMesh mesh : list)
                {
                        PPolygonSkinnedMesh skinnedMesh = (PPolygonSkinnedMesh) mesh;
                        // Make an instance
                        PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_loadingPScene.addMeshInstance(skinnedMesh, new PMatrix());
                        // Add it to the skeleton
                        m_skeleton.addToSubGroup(skinnedMeshInstance, subGroupName);
                }
                // iterate through all new scene looking for instances
                FastList<PNode> queue = new FastList<PNode>();
                queue.add(newScene.getInstances());
                while (queue.isEmpty() == false)
                {
                    PNode current = queue.removeFirst();
                    if (current instanceof PPolygonSkinnedMeshInstance)
                    {
                        PMatrix transform = new PMatrix(current.getTransform().getLocalMatrix(false));
                        PPolygonSkinnedMeshInstance skinnedMeshInstance =
                                (PPolygonSkinnedMeshInstance) m_loadingPScene.addMeshInstance(current, transform);
                        // Add it to the skeleton
                        m_skeleton.addToSubGroup(skinnedMeshInstance, subGroupName);
                    }
                    // add all children
                    queue.addAll(current.getChildren());

                }
            }
        }
        return result;


    }

    /**
     * Recursively log the rootInstruction tree.
     * @param spacing The spacing to lead the output with
     * @param rootInstruction
     */
    private void logInstructionTree(String spacing, Instruction rootInstruction)
    {
        logger.fine(spacing + rootInstruction.getInstructionType() + " '" + rootInstruction.getDataAsString() + "'");
        for (PNode kid : rootInstruction.getChildren())
        {
            if (kid instanceof Instruction)
                logInstructionTree(spacing + "    ", (Instruction)kid);
        }
    }
}



