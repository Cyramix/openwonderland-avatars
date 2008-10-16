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
    public String           m_ParentJointName = "Skinned Mesh Joint";
    //public int              m_ParentJointIndex = -1;

    // Use the underlying PTransform instead!
    //public Vector3f                m_Translation = new Vector3f();
    //public Vector3f                m_Rotation = new Vector3f();

    //public PMatrix                 m_Relative = new PMatrix();
    //public PMatrix                 m_Absolute = new PMatrix();
    //public PMatrix                 m_RelativeFinal = new PMatrix();
    //public PMatrix                 m_Final = new PMatrix();



    public SkinnedMeshJoint() 
    {
    }

    public SkinnedMeshJoint(PTransform transform) 
    {
        super(transform);
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
    }
    
    public SkinnedMeshJoint(String name, PNode parent, ArrayList<PNode> children, PTransform transform) 
    {
        super(name, parent, children, transform);
    }

    public void set(String jointName,
                    String parentJointName,
                    Vector3f translation,
                    Vector3f rotation)
    {
        setName(jointName);
        m_ParentJointName = parentJointName;
        
        getTransform().getLocalMatrix(true).set(rotation, translation, Vector3f.UNIT_XYZ);

	//m_Translation.set(translation);
	//m_Rotation.set(rotation);
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

    public void dump(String spacing)
    {
        System.out.println(spacing + this.getClass().toString() + " - " + this.getName());
        if (this.getTransform() != null)
        {
            PMatrix localMatrix = this.getTransform().getWorldMatrix(false);
            float []mat = localMatrix.getData();
            
            System.out.println(spacing + "   Matrix:");
            System.out.println(spacing + "      " + mat[0] + ", " + mat[1] + ", " + mat[2] + ", " + mat[3] + ", ");
            System.out.println(spacing + "      " + mat[4] + ", " + mat[5] + ", " + mat[6] + ", " + mat[7] + ", ");
            System.out.println(spacing + "      " + mat[8] + ", " + mat[9] + ", " + mat[10] + ", " + mat[11] + ", ");
            System.out.println(spacing + "      " + mat[12] + ", " + mat[13] + ", " + mat[14] + ", " + mat[15] + ", ");
        }

        dumpChildren(spacing);
    }
/*
    //  Dumps the contents of the PPolygonSkinnedMeshJoint.
    public void dump(String spacing)
    {
        System.out.println(spacing + "PPolygonSkinnedMeshJoint:  '" + getName() + "'");
        System.out.println(spacing + "   ParentJointName:   '" + m_ParentJointName + "'");
        //System.out.println(spacing + "   ParentJointIndex:  " + m_ParentJointIndex);
        //System.out.println(spacing + "   Translation:       (" + m_Translation.x + ", " + m_Translation.y + ", " + m_Translation.z + ")");
        //System.out.println(spacing + "   Rotation:          (" + m_Rotation.x + ", " + m_Rotation.y + ", " + m_Rotation.z + ")");
    }
*/

}



