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
package imi.scene.polygonmodel.parts.skinned;

import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.animation.Animated;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.shader.AbstractShaderProgram;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * This node represents a skinned mesh skeleton. Any skinned meshes affected
 * by this skeleton are attached as children.
 * @author Ronald E Dahlgren
 */
public class SkeletonNode extends PNode implements Animated
{
    // This is an array list with references to each skinned mesh joint's
    // world matrix in the order of a breadth first traversal
    private ArrayList<SkinnedMeshJoint> m_BFTSkeleton = new ArrayList<SkinnedMeshJoint>();
    private ArrayList<PMatrix> m_BFTSkeletonLocalModifiers = new ArrayList<PMatrix>();
    private ArrayList<PMatrix> m_BFTInverseBindPose = new ArrayList<PMatrix>();
    private ArrayList<String>  m_jointNames = new ArrayList<String>();
    
    //////////////////////////////
    //  Animation Data Follows  //
    //////////////////////////////
    // A collection of animation states is needed in order to track the state of
    // simultaneous animations that may be playing.
    // For instance, tracking the head as transitioning from smiling to laughing
    // while maintaining the current state of the walk animation as it transitions
    // to run.
    private ArrayList<AnimationState>   m_animationStates = new ArrayList<AnimationState>(); 
    // This component maintains a list of all the animation groups that this
    // skeleton uses.
    private AnimationComponent          m_animationComponent = new AnimationComponent();
    
    
    
    public SkeletonNode(String name)
    {
        super(name, new PTransform());
        // add an animation state
        m_animationStates.add(new AnimationState());
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

    public AnimationComponent getAnimationComponent()
    {
        return m_animationComponent;
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
        //m_initialInverseTransform.set(getTransform().getWorldMatrix(false).inverse());
        m_jointNames.clear();
        
        m_BFTSkeleton.clear();
        m_BFTSkeletonLocalModifiers.clear();
        m_BFTInverseBindPose.clear();
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


                    PMatrix[] matArray = kid.buildInverseFlattenedSkinnedMeshJointHierarchy();
                    for (PMatrix matrix : matArray)
                    {
                        PMatrix newMatrix = new PMatrix(matrix);
                        m_BFTInverseBindPose.add(newMatrix);
                    }
                }
                
            }
        }
    }
    
    /**
     * This method builds and returns an array of the world
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
     * This method builds and returns an array of references to the world transforms
     * of the inverse bind pose for this skeleton as indicated by the indices
     * requested.
     * @param indices
     * @return The resultant list
     */
    public PMatrix[] getInverseBindPose(int[] indices)
    {
        PMatrix[] result = new PMatrix[indices.length];
        for (int i = 0; i < indices.length; ++i)
            result[i] = m_BFTInverseBindPose.get(indices[i]);
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
            if (kid instanceof PPolygonSkinnedMeshInstance)
                result.add((PPolygonSkinnedMeshInstance)kid);
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
     * @param pSkinnedMeshInstance
     */
    public void addSkinnedMeshInstance(PPolygonSkinnedMeshInstance pSkinnedMeshInstance)
    {
        addChild(pSkinnedMeshInstance);
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

    //  Assigns the specified shader to all SkinnedMeshes.
    public void setShader(AbstractShaderProgram shader)
    {
        for (PNode kid : getChildren())
        {
            if (kid instanceof PPolygonSkinnedMesh)
            {
                PPolygonSkinnedMesh pSkinnedMesh = (PPolygonSkinnedMesh)kid;
                
                pSkinnedMesh.getMaterialRef().setShader(shader);
            }
            else if (kid instanceof PPolygonSkinnedMeshInstance)
            {
                PPolygonSkinnedMeshInstance pSkinnedMeshInstance = (PPolygonSkinnedMeshInstance)kid;
                
                PMeshMaterial mat = pSkinnedMeshInstance.getMaterialRef().getMaterial();
                mat.setShader(shader);
                pSkinnedMeshInstance.setMaterial(mat);
                pSkinnedMeshInstance.setUseGeometryMaterial(false);
            }
        }
     }

    public AnimationState getAnimationState()
    {
        return getAnimationState(0);
    }

    public PJoint getJoint(String jointName)
    {
        return findSkinnedMeshJoint(jointName);
    }

    public AnimationState getAnimationState(int index)
    {
        if (index < 0 || index > m_animationStates.size())
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
    
}
