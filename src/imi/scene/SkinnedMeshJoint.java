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
package imi.scene;


import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import javolution.util.FastList;
import javolution.util.FastTable;


/**
 * A joint used for skinning
 * @author Chris Nagle
 * @author Lou Hayt
 * @author Ronald E. Dahlgren
 */
public class SkinnedMeshJoint extends PJoint implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    /** The bind transform **/
    private PMatrix m_bindPoseTransform = null;

    /** Package private member for use by the SkeletonNode primarily **/
    private transient PMatrix unmodifiedInverseBindPose = new PMatrix();

    /**
     * Default construction
     */
    public SkinnedMeshJoint()
    {
        super(new PTransform());
    }

    /**
     * Constructor with a transform
     * @param transform
     */
    public SkinnedMeshJoint(PTransform transform) 
    {
        super(transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
        unmodifiedInverseBindPose.set(m_bindPoseTransform.inverse());
    }

    /**
     * Create with a name and a transform
     * @param name      -   can be null
     * @param transform -   can be null (will be set to identity)
     */
    public SkinnedMeshJoint(String name, PTransform transform) 
    {
        super(name, transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
        unmodifiedInverseBindPose.set(m_bindPoseTransform.inverse());
    }

    /**
     * Constructor
     * @param name
     * @param parent
     * @param children
     * @param transform
     */
    public SkinnedMeshJoint(String name, PNode parent, FastTable<PNode> children, PTransform transform)
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
        if (other.m_bindPoseTransform != null)
            this.m_bindPoseTransform = new PMatrix(other.m_bindPoseTransform);
        this.unmodifiedInverseBindPose.set(other.unmodifiedInverseBindPose);
    }

    /**
     * Get a reference to the bind pose
     * @return
     */
    public PMatrix getBindPoseRef()
    {
        return m_bindPoseTransform;
    }

    /**
     * Get the parent if it is a SkinnedMeshJoint
     * @return - may be null
     */
    public SkinnedMeshJoint getParentJoint()
    {
        if (getParent() instanceof SkinnedMeshJoint)
            return( (SkinnedMeshJoint)getParent());

        return(null);
    }

    /**
     * Collect the inverse bind pose transforms of all skinned mesh joint children
     * @return
     */
    List<PMatrix> buildIBPStack()
    {
        FastTable<PMatrix> result = new FastTable<PMatrix>();
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
                currentJoint.getMeshSpace().fastMul(parentMeshSpace, currentJoint.getBindPoseRef());
                PMatrix stackAddition = new PMatrix();
                // invert and add to the stack
                stackAddition.set(currentJoint.getMeshSpace().inverse());
                result.add(stackAddition);
            }
            else
            {
                currentJoint.getMeshSpace().set(currentJoint.getBindPoseRef());
                PMatrix stackAddition = new PMatrix();
                stackAddition.set(currentJoint.getBindPoseRef().inverse());
                result.add(stackAddition);
            }

            // add children
            queue.addAll(currentJoint.getChildren());
        }
        return result;
    }

    public PMatrix getUnmodifiedInverseBindPoseRef()
    {
        return unmodifiedInverseBindPose;
    }
    
    public void resetBindPose()
    {
        m_bindPoseTransform = unmodifiedInverseBindPose.inverse();
    }

    public void reset() {
        resetBindPose();
        getTransform().getLocalMatrix(true).set(m_bindPoseTransform);
    }

    public void setToBindPose()
    {
        getTransform().getLocalMatrix(true).set(m_bindPoseTransform);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Local: " + getTransform().getLocalMatrix(false).toString() + "\n");
        result.append("World: " + getTransform().getWorldMatrix(false).toString() + "\n");
        result.append("Bind: " + m_bindPoseTransform.toString() + "\n");
        result.append("LocalModifier: " + getLocalModifierMatrix().toString() + "\n");
        result.append("MeshSpace: " + getMeshSpace().toString() + "\n");
        return result.toString();
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



