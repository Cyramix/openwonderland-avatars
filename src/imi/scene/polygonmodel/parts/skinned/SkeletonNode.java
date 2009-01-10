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
package imi.scene.polygonmodel.parts.skinned;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.SharedMesh;
import imi.gui.GUI_Enums.m_sliderControl;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.animation.Animated;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.shader.AbstractShaderProgram;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;



/**
 * This node represents a skinned mesh skeleton. Any skinned meshes affected
 * by this skeleton are attached as children.
 * @author Ronald E Dahlgren
 */
public class SkeletonNode extends PNode implements Animated, Serializable
{
    // This is an array list with references to each skinned mesh joint's
    // world matrix in the order of a breadth first traversal
    private transient ArrayList<SkinnedMeshJoint> m_BFTSkeleton = new ArrayList<SkinnedMeshJoint>();
    private transient ArrayList<PMatrix> m_BFTFlattenedInverseBindPose = new ArrayList<PMatrix>();
    private transient ArrayList<String>  m_jointNames = new ArrayList<String>();
    /**
     * This field is marked as transient because the default heirarchies that will
     * be serialized should not have any local modifiers. This will shrink the size
     * of the save files as this can be regenerated during recreation.
     */
    private transient ArrayList<PMatrix> m_BFTSkeletonLocalModifiers = new ArrayList<PMatrix>();

    
    //////////////////////////////
    //  Animation Data Follows  //
    //////////////////////////////
    // A collection of animation states is needed in order to track the state of
    // simultaneous animations that may be playing.
    // For instance, tracking the head as transitioning from smiling to laughing
    // while maintaining the current state of the walk animation as it transitions
    // to run.
    private transient ArrayList<AnimationState>   m_animationStates = new ArrayList<AnimationState>();
    // This component maintains a list of all the animation groups that this
    // skeleton uses.
    private AnimationComponent  m_animationComponent = new AnimationComponent();
    
    /** Enables a callback during the flatenning of the skeleton hierarchy for 
     *  manipulations that need to have cascading affect down the hierarchy */
    private transient SkeletonFlatteningManipulator m_flatteningHook = null;
    
    public SkeletonNode(String name)
    {
        super(name, new PTransform());
        // add an animation state
        m_animationStates.add(new AnimationState(0));
    }
    
    /**
     * This constructor recreates the skeleton node. No attached
     * meshes are copied over, only the name and transform are copied.
     * @param other
     */
    public SkeletonNode(SkeletonNode other)
    {
        super(other.getName(), new PTransform(other.getTransform()));
        // copy over animation stuff 
        m_animationComponent = new AnimationComponent(other.getAnimationComponent());
        
        for (AnimationState animState : other.m_animationStates)
            m_animationStates.add(new AnimationState(animState));
        
    }

    @Override
    public AnimationComponent getAnimationComponent()
    {
        return m_animationComponent;
    }

    public Iterable<AnimationState> getAnimationStates() {
        return m_animationStates;
    }

    public Iterable<String> getJointNames() {
        return m_jointNames;
    }

    /**
     * This method remaps the skeleton's internal data representations to
     * the currently attached hierarchy of skinned mesh joints.
     */
    public void refresh()
    {
        mapSkinnedMeshJointIndices();
    }
    
    /**
     * This method should be called to indicate to the skeleton node
     * where the skeleton it is wrapping begins. This node will have
     * it's name changed. If that is unacceptable, let me know and I
     * will change it. This method also performs the remapping for a
     * hierarchy that the root node sets atop.
     * @param root The root node
     */
    public void setSkeletonRoot(PNode root)
    {
        addChild(root);
        root.setName("skeletonRoot");
        mapSkinnedMeshJointIndices();
    }
    
    /**
     * Retrieve and return the root of this skeleton if found
     * @return The root, or null if no root has been set
     */
    public PNode getSkeletonRoot()
    {
        return getChild("skeletonRoot");
    }
    
    /**
     * This method attempts to retrieve the skinned mesh joint named
     * "name" from the skeleton.
     * @param name The name of the joint to retrieve
     * @return The requested joint, or null if not found
     */
    public SkinnedMeshJoint findSkinnedMeshJoint(String name)
    {
        SkinnedMeshJoint result = null;
        PNode skeletonRoot = getChild("skeletonRoot");
        if (skeletonRoot != null)
        {
            try
            {
                result = (SkinnedMeshJoint)skeletonRoot.findChild(name);
            }
            catch (ClassCastException e)
            {
                result = null;
            }
        }
        return result;
    }
   
    /**
     * Retrieve the index of this joint from the skeleton
     * @param name The name of the joint
     * @return The index, or -1 if not found
     */
    public int getSkinnedMeshJointIndex(String name)
    {
        return m_jointNames.indexOf(name);
    }
    
    /**
     * Retrieve the index of this joint from the skeleton
     * @param joint The joint to locate
     * @return The index, or -1 if not found
     */
    public int getSkinnedMeshJointIndex(SkinnedMeshJoint joint)
    {
        return getSkinnedMeshJointIndex(joint.getName());
    }
    
    /**
     *  This method adds the specified joint to the skeleton. If a parent name
     * is supplied, that joint will attempt to be located before adding the new
     * joint. If the parent joint is not found, then the joint is added to the 
     * root of the skeleton. If no root node has been assigned yet, a default is
     * created and assigned.
     * @param parentJointName The name of the parent joint
     * @param pJoint The new joint to emplace in the skeleton
     * @return True if the parent joint was successfully located by name
     */
    public boolean addSkinnedMeshJoint(String parentJointName, SkinnedMeshJoint pJoint)
    {
        PNode root = getSkeletonRoot();
        
        if (root == null)
        {
            // add a regular old node as the root
            setSkeletonRoot(new PNode(new PTransform()));
            root = getSkeletonRoot();
        }
        
        PNode pParentJointNode = root.findChild(parentJointName);
        if (pParentJointNode != null)
        {
            pParentJointNode.addChild(pJoint);
            return true;
        }
        else
        {
            //  Add the Joint directly to 'm_BindPoseTransformHierarchy'.
            root.addChild(pJoint);
            return false;
        }
    }
    
    /**
     * Call this method once to generate the mapping used to answer
     * "getMatrixStack" queries.
     */
    private void mapSkinnedMeshJointIndices()
    {
        m_jointNames.clear();
        m_BFTSkeleton.clear();
        m_BFTSkeletonLocalModifiers.clear();
        m_BFTFlattenedInverseBindPose.clear();
        if (getChildrenCount() > 0)
        {
            PNode skeletonRoot = getChild("skeletonRoot");
            if (skeletonRoot != null && skeletonRoot.getChildrenCount() > 0)
            {
                // for each child
                for (PNode kid : skeletonRoot.getChildren())
                {
                    m_BFTSkeleton.addAll(kid.generateSkinnedMeshJointReferences());
                    m_BFTSkeletonLocalModifiers.addAll(kid.generateSkinnedMeshLocalModifierReferences());
                    m_jointNames.addAll(kid.generateSkinnedMeshJointNames());


                    List<PMatrix> ibpList = ((SkinnedMeshJoint)kid).buildIBPStack();
                    for (PMatrix matrix : ibpList)
                    {
                        PMatrix newMatrix = new PMatrix(matrix);
                        m_BFTFlattenedInverseBindPose.add(newMatrix);
                    }
                }
                
            }
        }
    }
    
    /**
     * This method builds and returns an array of the mesh space
     * transforms of the skinned mesh joints indicated by each 
     * index in indices multiplied by its correspondind local 
     * modifier.
     * @param indices A list of joint indices to query
     * @return The resulting list of PMatrix references
     */
    public PMatrix[] getPose(int[] indices)
    {
        PMatrix[] result = new PMatrix[indices.length];
        for (int i = 0; i < indices.length; ++i)
        {
            result[i] = new PMatrix(m_BFTSkeleton.get(indices[i]).getMeshSpace());
            result[i].mul(m_BFTSkeletonLocalModifiers.get(indices[i]));
        }
        return result;
    }
    
    
    
    /**
     * This method calculates the joint matrix after 
     * multiplying it with the local modifier (used for things such as body fat)
     * @param BFTJointIndex
     * @return
     */
    public PMatrix getModifiedJointMatrix(int BFTJointIndex)
    {
        PMatrix result = new PMatrix(m_BFTSkeleton.get(BFTJointIndex).getMeshSpace());
        result.mul(m_BFTSkeletonLocalModifiers.get(BFTJointIndex));
        return result;
    }
    
    public PMatrix getJointLocalModifier(int BFTJointIndex)
    {
        return m_BFTSkeletonLocalModifiers.get(BFTJointIndex);
    }
    
    /**
     * This method builds and returns an array of references to the world transforms
     * of the inverse bind pose for this skeleton as indicated by the indices
     * requested.
     * @param indices
     * @return The resultant list
     */
    public PMatrix[] getFlattenedInverseBindPose(int[] indices)
    {
        PMatrix[] result = new PMatrix[indices.length];
        for (int i = 0; i < indices.length; ++i)
            result[i] = m_BFTFlattenedInverseBindPose.get(indices[i]);
        return result;
    }
    
    /**
     * This method traverses all children of this skeleton node and accumulates
     * skinned mesh instances. These collected instances are then returned
     * @return The collection of skinned mesh instances found as children
     */
    public ArrayList<PPolygonSkinnedMeshInstance> getSkinnedMeshInstances()
    {
        ArrayList<PPolygonSkinnedMeshInstance> result = new ArrayList<PPolygonSkinnedMeshInstance>();
        for (PNode kid : getChildren())
        {
            if (kid instanceof PPolygonSkinnedMeshInstance)
                result.add((PPolygonSkinnedMeshInstance)kid);
            else if (kid.getName().equals("skeletonRoot") == false) // grouping node
            {
                for (PNode grandKid : kid.getChildren())
                    result.add((PPolygonSkinnedMeshInstance)grandKid);
            }
        }
        return result;
    }
    
    /**
     * This method retrieves a skinned mesh instance with the given name from 
     * the collection of skinned mesh instance children this skeleton node has.
     * @param name The name of the requested skinned mesh instance
     * @return The instance, or null if not found
     */
    public PPolygonSkinnedMeshInstance getSkinnedMeshInstance(String name)
    {
        PPolygonSkinnedMeshInstance result = null;
        
        ArrayList<PPolygonSkinnedMeshInstance> meshes = getSkinnedMeshInstances();
        if (meshes.size() > 0)
        {
            for (PPolygonSkinnedMeshInstance meshInst : meshes)
                if (meshInst.getName().equals(name))
                {
                    result = meshInst;
                    break;
                }
        }
        return result;
    }

    /**
     * Adds the specified skinned mesh instance as a child to this skeleton. This
     * method was created in order to explicitely indicate the intended result.
     * @param meshInst
     * @param subGroup
     */
    public void addToSubGroup(PPolygonSkinnedMeshInstance meshInstance, String subGroup)
    {
        PNode groupNode = getChild(subGroup);
        if (groupNode == null)
        {
            groupNode = new PNode(subGroup);
            addChild(groupNode);
        }

        groupNode.addChild(meshInstance);
        meshInstance.setAndLinkSkeletonNode(this);
    }

    /**
     * Retrieves the name of the meshes in the subgroup if it exists otherwise it returns null
     * @param subGroup
     * @return String[] of mesh names
     */
    public String[] getMeshNamesBySubGroup(String subGroup) {
        String[] meshes = null;
        PNode groupNode = getChild(subGroup);
        if (groupNode == null)
            return meshes;

        meshes = new String[groupNode.getChildrenCount()];
        for (int i = 0; i < meshes.length; i++) {
            meshes[i] = groupNode.getChild(i).getName();
        }

        return meshes;
    }

    /**
     * 
     * @param meshName
     * @return
     */
    public PPolygonSkinnedMesh getPolygonSkinnedMesh(String meshName)
    {
        for (PNode kid : getChildren())
        {
            if (kid instanceof PPolygonSkinnedMesh)
            {
                if (kid.getName().equals(meshName))
                    return( (PPolygonSkinnedMesh)kid);
            }
        }
        
        return(null);
    }

    /**
     * Retrieve the list of meshes from the specified subGroup, if the subGroup
     * is not found, null is returned. If the subGroup is found but it has no
     * children, an empty list is returned
     * @param subGroup Name of the subGroupto inspect
     * @return
     */
    public List<PPolygonSkinnedMeshInstance> retrieveSkinnedMeshes(String subGroup)
    {
        ArrayList<PPolygonSkinnedMeshInstance> result = null;
        final PNode groupNode = getChild(subGroup);

        if (groupNode != null)
        {
            result = new ArrayList<PPolygonSkinnedMeshInstance>();
            for (PNode kid : groupNode.getChildren())
            {
                if (kid instanceof PPolygonSkinnedMeshInstance)
                    result.add((PPolygonSkinnedMeshInstance)kid);
            }
        }

        return result;
    }

    /**
     * Clear all skinned meshes from the specified subGroup
     * @param subGroupName
     * @return true if cleared, false if the subgroup was not found
     */
    public boolean clearSubGroup(String subGroupName)
    {
        boolean result = false;
        PNode groupNode = getChild(subGroupName);
        if (groupNode != null)
        {
            groupNode.removeAllChildren();
            result = true;
        }
        return result;
    }
    /**
     * Set the provided shader on all the skinned meshes that are direct children
     * of this skeleton node
     * @param shader
     * @return true on success, false otherwise (Concurrent modification for instance)
     */
    public boolean setShaderOnSkinnedMeshes(AbstractShaderProgram shader)
    {
        boolean result = false;
        try
        {
            for (PPolygonSkinnedMeshInstance meshInst : getSkinnedMeshInstances())
            {
                    PMeshMaterial mat = meshInst.getMaterialRef().getMaterial();
                    mat.setShader(shader);
                    meshInst.applyShader();
            }
            result = true;
        }
        catch (ConcurrentModificationException ex) // Someone else could be messing with our children
        {
            Logger.getLogger(SkeletonNode.class.getName()).log(Level.WARNING,
                    "Concurrent Modification exception caught while attempting to apply" +
                    "shaders to this SkeletonNode's skinned mesh instances.");
            result = true;
        }
        finally
        {
            return result;
        }
     }

    /**
     * Set the provided shader on all meshes (non-skinned) found under the
     * skeleton node in the graph.
     * @param shader
     */
    public void setShaderOnMeshes(AbstractShaderProgram shader)
    {
        FastList<PNode> queue = new FastList<PNode>();
        queue.addAll(getChildren());

        while (queue.isEmpty() == false)
        {
            PNode kid = queue.removeFirst();
            if (kid instanceof PPolygonMesh && !(kid instanceof PPolygonSkinnedMesh))
            {
                PPolygonMesh mesh = (PPolygonMesh)kid;

                mesh.getMaterialRef().setShader(shader);
            }
            else if (kid instanceof PPolygonMeshInstance && !(kid instanceof PPolygonSkinnedMeshInstance))
            {
                PPolygonMeshInstance meshInst = (PPolygonMeshInstance)kid;

                PMeshMaterial mat = meshInst.getMaterialRef().getMaterial();
                mat.setShader(shader);
                meshInst.applyShader();
            }
            for (int i = 0; i < kid.getChildrenCount(); ++i)
                queue.add(kid.getChild(i));
        }
     }

    @Override
    public AnimationState getAnimationState()
    {
        return getAnimationState(0);
    }

    @Override
    public PJoint getJoint(String jointName)
    {
        return findSkinnedMeshJoint(jointName);
    }
    
    @Override
    public int addAnimationState(AnimationState newState)
    {
        m_animationStates.add(newState);
        return m_animationStates.size() - 1;
    }

    @Override
    public AnimationState getAnimationState(int index)
    {
        if (index < 0 || index >= m_animationStates.size())
        {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Invalid animation state requested, index was " + index);
            return null;   
        }
        else
            return m_animationStates.get(index);
    }
    
    public AnimationGroup getAnimationGroup() 
    {
        return m_animationComponent.getGroup();
    }
    
    /**
     * Retrieve an animation group
     * @return AnimationGroup at index i 
     */
    @Override
    public AnimationGroup getAnimationGroup(int index)
    {
        try {
        return m_animationComponent.getGroups().get(index);}
        catch (IndexOutOfBoundsException ex){
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public boolean transitionTo(String cycleName, boolean bReverse)
    {
        return m_animationComponent.transitionTo(cycleName, getAnimationState(0), bReverse);
    }
    
    /**
     * Retrieve a reference to the joint indicated by the breadth first traversal
     * index.
     * @param BFTIndex A breadth first index
     * @return The specified skinned mesh joint
     */
    public SkinnedMeshJoint getSkinnedMeshJoint(int BFTIndex)
    {
        return m_BFTSkeleton.get(BFTIndex);
    }

    public int getSkinnedMeshJointCount()
    {
        return m_BFTSkeleton.size();
    }

    public List<SharedMesh> collectSharedMeshes()
    {
        ArrayList<SharedMesh> result = new ArrayList<SharedMesh>();

        // first thing, traverse and flatten the skeleton, collecting sharedmeshes along the way
        FastList<PNode> queue = new FastList<PNode>();
        queue.add(getSkeletonRoot());
        while (queue.isEmpty() == false)
        {
            PNode current = queue.removeFirst();
            PNode parent = current.getParent();

            // Get the parent's world matrix
            PTransform parentTransform = null;
            if (parent == null || parent.getTransform() == null)
                parentTransform = new PTransform();
            else
                parentTransform = parent.getTransform();

            if (current.getTransform() != null) // If this node has any sort of transform
            {
                // Build the world matrix for the current instance
                if (current.getTransform().isDirtyWorldMat() || current.isDirty())
                {
                    current.getTransform().buildWorldMatrix(parentTransform.getWorldMatrix(false));
                    // Now we are clean!
                    current.setDirty(false, false);
                }

                // handle MeshSpace transform for the SkinnedMeshJoints
                if (current instanceof SkinnedMeshJoint)
                {
                    if (parent instanceof SkinnedMeshJoint)
                    {
                        // Grab some references
                        SkinnedMeshJoint curJoint = (SkinnedMeshJoint)current;
                        // Multiply chain: ParentMeshSpace * modifiedBindPose * originalInverseBind * AnimatedPose
                        PMatrix meshSpace = curJoint.getMeshSpace();
                        meshSpace.set(((SkinnedMeshJoint)parent).getMeshSpace());
                        meshSpace.mul(curJoint.getBindPose());
                        meshSpace.mul(curJoint.unmodifiedInverseBindPose);
                        meshSpace.mul(curJoint.getTransform().getLocalMatrix(false));
                    }
                    else // First joint in the skeleton; mesh space is local space
                    {
                        // This may fail in some circumstances where the root joint is modified and animated.
                        ((SkinnedMeshJoint)current).setMeshSpace(current.getTransform().getLocalMatrix(false));
                    }
                }
            }
            
            // Flatenning Hook - used for e.g. by physical overwrites such as the verlet arm
            if (m_flatteningHook != null)
                m_flatteningHook.processSkeletonNode(current);
            
            if (current instanceof PPolygonMeshInstance)
            {
                // ensure we have indices
                if (((PPolygonMeshInstance)current).getGeometry().getGeometry().getMaxIndex() >= 0) // If no indices, don't attach this mesh.
                    result.add(((PPolygonMeshInstance)current).updateSharedMesh());
            }

            // special case for the skeleton node (see this method... haha)
            if (current instanceof SkeletonNode)
            {
                result.addAll(((SkeletonNode)current).collectSharedMeshes());
                continue;
            }

            // add all the kids
            for (PNode kid : current.getChildren())
                queue.add(kid);

        }

        // then get skinned mesh meshes
        for (PPolygonSkinnedMeshInstance meshInst : getSkinnedMeshInstances())
        {
            meshInst.getTransform().buildWorldMatrix(this.getTransform().getWorldMatrix(false));
            result.add(meshInst.updateSharedMesh());
        }
        return result;
    }

    /**
     * This method is used to generate skeleton modifiers as deltas from the
     * provided base skeleton and assign the initial base skeleton data as the
     * transform for each joint.
     * @param baseSkeleton 
     */
    public void remapSkeleton(SkeletonNode baseSkeleton)
    {
        // Gather the joints and set the new local modifiers
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.addAll(getSkeletonRoot().getChildren());
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();

            // Process him! If not a skinned mesh joint skip and prune
            if (current instanceof SkinnedMeshJoint)
            {
                SkinnedMeshJoint ourJoint = (SkinnedMeshJoint)current;
                SkinnedMeshJoint baseJoint = baseSkeleton.findSkinnedMeshJoint(ourJoint.getName());

                ourJoint.unmodifiedInverseBindPose.set(baseJoint.getBindPose().inverse());
                // "prime the pump"
                ourJoint.getTransform().setLocalMatrix(baseJoint.getBindPose());
            }
            else
                continue; // Prune (kids are not added to the list)
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        // Rebuild, among other things, the collection of inverseBindPoses
        mapSkinnedMeshJointIndices();
        // go through each mesh and nullify the cached inverse bind pose reference
        invalidateCachedBindPoses();
    }

    public SkeletonFlatteningManipulator getFlatteningHook() {
        return m_flatteningHook;
    }

    public void setFlatteningHook(SkeletonFlatteningManipulator flatteningHook) {
        this.m_flatteningHook = flatteningHook;
    }

    // Convenience Methods for joint manipulation - These are all relatively heavyweight operations

    /**
     * This method shifts the specified joint by the given offset.
     * @param jointName Name of the joint to modify
     * @param offset The displacement vector (in joint local space)
     * @return True if joint found, false otherwise
     */
    public boolean displaceJoint(String jointName, Vector3f offset)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            Vector3f actualOffset = new Vector3f(offset);
            joint.getBindPose().transformNormal(actualOffset);
            joint.getBindPose().setTranslation(joint.getBindPose().getTranslation().add(actualOffset));
        }
        return result;
    }

    /**
     * Sets the joint's local space position to the provided one.
     * @param jointName Name of the joint to modify
     * @param localSpaceTranslation The new local space location
     * @return True if the joint was found, false otherwise.
     */
    public boolean setJointPosition(String jointName, Vector3f localSpaceTranslation)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            // Apply it!
            Vector3f transformedVec = new Vector3f(localSpaceTranslation.subtract(joint.getBindPose().getTranslation()));
            joint.getBindPose().transformNormal(transformedVec);
            joint.getBindPose().setTranslation(joint.getBindPose().getTranslation().add(transformedVec));
        }
        return result;
    }

    /**
     * This method sets the specified joint's rotation
     * @param jointName The joint to manipulate
     * @param rotation
     * @return true if the joint was found, false otherwise
     */
    public boolean setJointRotation(String jointName, Quaternion rotation)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            // Apply it!
            joint.getBindPose().setRotation(rotation);
        }
        return result;
    }

    /**
     * Rotate the specified joint around the given axis by the given angle
     * @param jointName The joint to manipulate
     * @param axis The rotational axis
     * @param angle Amount of rotation (radians)
     * @return True if the joint was found, false otherwise
     */
    public boolean rotateJoint(String jointName, Vector3f axis, float angle)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            Quaternion quat = new Quaternion();
            quat.fromAngleAxis(angle, axis);
            PMatrix rotationMatrix = new PMatrix();
            rotationMatrix.setRotation(quat);
            joint.getBindPose().mul(rotationMatrix);
        }
        return result;
    }

    /**
     * Change the specified joint's bind pose to match its original, unmodified
     * bind pose.
     * @param jointName
     */
    public boolean resetJointBindPose(String jointName)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            joint.resetBindPose();
        }
        return result;
    }

    /**
     * Reset the joint's local transform to it's current bind pose.
     * @param jointName
     */
    public boolean resetJointToBindPose(String jointName)
    {
        boolean result = false;
        SkinnedMeshJoint joint = findSkinnedMeshJoint(jointName);
        if (joint != null)
        {
            result = true;
            joint.setToBindPose();
        }
        return result;
    }

    public void invalidateCachedBindPoses()
    {
        for (PPolygonSkinnedMeshInstance meshInst : getSkinnedMeshInstances())
            meshInst.setInverseBindPose(null);
    }

    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
        m_BFTSkeleton = new ArrayList<SkinnedMeshJoint>();
        m_BFTFlattenedInverseBindPose = new ArrayList<PMatrix>();
        m_jointNames = new ArrayList<String>();
        m_BFTSkeletonLocalModifiers = new ArrayList<PMatrix>();
        m_animationStates = new ArrayList<AnimationState>();
        // Remap the joint refs
        mapSkinnedMeshJointIndices();
        // Now create a new animation state for each group read in
        for (int i = 0; i < m_animationComponent.getGroups().size(); i++)
            m_animationStates.add(new AnimationState(i));
    }
}
