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


import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.PTransform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastList;


/**
 *
 * @author Chris Nagle
 */
public class SkinnedMeshJoint extends PJoint implements Serializable
{
    /** The name! **/
    public String   m_ParentJointName   = "Skinned Mesh Joint";
    /** The bind transform **/
    private PMatrix m_bindPoseTransform = null;

    /** Package private member for use by the SkeletonNode primarily **/
    transient PMatrix unmodifiedInverseBindPose = new PMatrix();
    /** The flattened matrix **/
    transient PMatrix flattenedMatrix = new PMatrix();

    /**
     * Default construction
     */
    public SkinnedMeshJoint()
    {
        super(new PTransform());
    }

    public SkinnedMeshJoint(PTransform transform) 
    {
        super(transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
        unmodifiedInverseBindPose.set(m_bindPoseTransform.inverse());
    }

    /**
     *  After construction m_children will not be null
     * 
     * @param name      -   can be null
     * @param transform -   can be null
     */
    public SkinnedMeshJoint(String name, PTransform transform) 
    {
        super(name, transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
        unmodifiedInverseBindPose.set(m_bindPoseTransform.inverse());
    }
    
    public SkinnedMeshJoint(String name, PNode parent, ArrayList<PNode> children, PTransform transform) 
    {
        super(name, parent, children, transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
        unmodifiedInverseBindPose.set(m_bindPoseTransform.inverse());
    }

    /**
     * Create a copy of the provided SkinnedMeshJoint
     * @param other
     */
    public SkinnedMeshJoint(SkinnedMeshJoint other) {
        super(other);
        // Copy parent joint name and the bind pose transform
        m_ParentJointName = other.m_ParentJointName;
        if (other.m_bindPoseTransform != null)
            this.m_bindPoseTransform = new PMatrix(other.m_bindPoseTransform);
        this.unmodifiedInverseBindPose.set(other.unmodifiedInverseBindPose);
    }

    public void set(String jointName,
                    String parentJointName,
                    Vector3f translation,
                    Vector3f rotation)
    {
        setName(jointName);
        m_ParentJointName = parentJointName;
        
        getTransform().getLocalMatrix(true).set(rotation, translation, Vector3f.UNIT_XYZ);

    }

    public PMatrix getBindPose()
    {
        return m_bindPoseTransform;
    }

    //  Set the ParentJointName.
    public void setParentJointName(String parentJointName)
    {
        m_ParentJointName = parentJointName;
    }


    
    //  Gets the parent joint.
    public SkinnedMeshJoint getParentJoint()
    {
        if (getParent() instanceof SkinnedMeshJoint)
            return( (SkinnedMeshJoint)getParent());

        return(null);
    }

    // Collect the inverse bind pose transforms of all skinned mesh joint children
    public List<PMatrix> buildIBPStack()
    {
        ArrayList<PMatrix> result = new ArrayList<PMatrix>();
        FastList<PNode> queue = new FastList<PNode>();
        queue.add(this);
        while (queue.isEmpty() == false)
        {
            // process
            PNode current = queue.removeFirst();
            SkinnedMeshJoint currentJoint = null;
            if (current instanceof SkinnedMeshJoint)
                currentJoint = (SkinnedMeshJoint)current;
            else
                continue;

            // Parent meshSpace x current Bind pose, invert and store
            PMatrix parentMeshSpace = null;
            if (currentJoint.getParentJoint() != null)
            {
                parentMeshSpace = currentJoint.getParentJoint().getMeshSpace();
                // Current meshSpace = parentMeshSpace * currentBindPose
                currentJoint.getMeshSpace().fastMul(parentMeshSpace, currentJoint.getBindPose());
                PMatrix stackAddition = new PMatrix();
                // invert and add to the stack
                stackAddition.set(currentJoint.getMeshSpace().inverse());
                result.add(stackAddition);
            }
            else
            {
                currentJoint.getMeshSpace().set(currentJoint.getBindPose());
                PMatrix stackAddition = new PMatrix();
                stackAddition.set(currentJoint.getBindPose().inverse());
                result.add(stackAddition);
            }

            // add children
            queue.addAll(currentJoint.getChildren());
        }
        return result;
    }

    public void resetBindPose()
    {
        m_bindPoseTransform = unmodifiedInverseBindPose.inverse();
    }

    public void setToBindPose()
    {
        getTransform().getLocalMatrix(true).set(m_bindPoseTransform);
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
        unmodifiedInverseBindPose = m_bindPoseTransform.inverse();
    }
}



