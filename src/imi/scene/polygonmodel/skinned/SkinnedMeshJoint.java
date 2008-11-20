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
package imi.scene.polygonmodel.skinned;


import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PJoint;
import imi.scene.PNode;
import imi.scene.PTransform;
import java.util.ArrayList;


/**
 *
 * @author Chris Nagle
 */
public class SkinnedMeshJoint extends PJoint
{
    /** The name! **/
    public String           m_ParentJointName = "Skinned Mesh Joint";
    /** The bind transform **/
    private PMatrix          m_bindPoseTransform = null;

    public SkinnedMeshJoint(PTransform transform) 
    {
        super(transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
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
    }
    
    public SkinnedMeshJoint(String name, PNode parent, ArrayList<PNode> children, PTransform transform) 
    {
        super(name, parent, children, transform);
        m_bindPoseTransform = new PMatrix(transform.getLocalMatrix(false));
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
}



