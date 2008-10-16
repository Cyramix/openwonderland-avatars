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
package org.collada.xml_walker;


import java.util.ArrayList;
import imi.scene.PMatrix;

import org.collada.colladaschema.Node;


/**
 *
 * @author Chris Nagle
 */
public class PColladaNode
{
    PColladaNode                m_pParentNode = null;

    //  Pointer to the collada Node.
    Node                        m_pNode = null;

    String                      m_Name = "";

    PMatrix                     m_Matrix = new PMatrix();
    float                       []m_MatrixFloats = new float[16];

    boolean                     m_bIsJoint;
    String                      m_JointName;

    String                      m_MeshName = "";

    String                      m_InstanceNodeName = "";

    ArrayList                   m_ChildNodes = new ArrayList();

    String                      m_ControllerName = "";

    ArrayList                   m_Skeletons = new ArrayList();

    PColladaMaterialInstance    m_pMaterialInstance = null;




    //  Constructor.
    public PColladaNode()
    {
    }

    public PColladaNode(Node pNode)
    {
        setNode(pNode);
    }



    //  Gets the name.
    public String getName()
    {
        return(m_Name);
    }

    //  Sets the name.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Set pointer to the collada Node.
    public void setNode(Node pNode)
    {
        m_pNode = pNode;
    }



    //  Gets the Matrix.
    public PMatrix getMatrix()
    {
        return(m_Matrix);
    }

    //  Sets the Matrix.
    public void setMatrix(float []pMatrixFloats)
    {
        m_Matrix.set(pMatrixFloats);
        
        for (int a=0; a<16; a++)
            m_MatrixFloats[a] = pMatrixFloats[a];
    }

    //  Gets the MatrixFloats.
    public float []getMatrixFloats()
    {
        return(m_MatrixFloats);
    }



    //  Gets boolean indicating whether the Node is a Joint.
    public boolean isJoint()
    {
        return(m_bIsJoint);
    }

    //  Sets boolean indicating whether the Node is a Joint.
    public void isJoint(boolean bIsJoint)
    {
        m_bIsJoint = bIsJoint;
    }

    //  Finds the ColladaNode with the specified jointName.
    public PColladaNode findJoint(String jointName)
    {
        if (m_JointName != null && m_JointName.equals(jointName))
            return(this);

        int a;
        PColladaNode pColladaNode;
        PColladaNode pFoundColladaNode;

        for (a=0; a<m_ChildNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ChildNodes.get(a);

            pFoundColladaNode = pColladaNode.findJoint(jointName);
            if (pFoundColladaNode != null)
                return(pFoundColladaNode);
        }

        return(null);
    }

    //  Gets the mesh name.
    public String getMeshName()
    {
        return(m_MeshName);
    }

    //  Sets the mesh name.
    public void setMeshName(String meshName)
    {
        m_MeshName = meshName;
    }



    //  Gets the InstanceNodeName.
    public String getInstanceNodeName()
    {
        return(m_InstanceNodeName);
    }

    //  Sets the InstanceNodeName.
    public void setInstanceNodeName(String instanceNodeName)
    {
        m_InstanceNodeName = instanceNodeName;
    }



    //  Adds a child node.
    public void addChildNode(PColladaNode pChildNode)
    {
        if (pChildNode != null)
        {
            pChildNode.m_pParentNode = this;
            m_ChildNodes.add(pChildNode);
        }
    }

    //  Gets the number of child nodes.
    public int getChildNodeCount()
    {
        return(m_ChildNodes.size());
    }

    //  Gets the child node at the specified index.
    public PColladaNode getChildNode(int index)
    {
        return( (PColladaNode)m_ChildNodes.get(index));
    }

    //  Finds the ColladaNode with the specified name.
    public PColladaNode findNode(String nodeName)
    {
        if (m_Name.equals(nodeName))
            return(this);

        int a;
        PColladaNode pColladaNode;
        PColladaNode pFoundColladaNode;

        for (a=0; a<m_ChildNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ChildNodes.get(a);

            pFoundColladaNode = pColladaNode.findNode(nodeName);
            if (pFoundColladaNode != null)
                return(pFoundColladaNode);
        }

        return(null);
    }

    //  Gets the parent ColladaNode.
    public PColladaNode getParentColladaNode()
    {
        return(m_pParentNode);
    }
            


    //  Gets the MaterialInstance.
    public PColladaMaterialInstance getMaterialInstance()
    {
        return(m_pMaterialInstance);
    }
    
    //  Set the MaterialInstance.
    public void setMaterialInstance(PColladaMaterialInstance pMaterialInstance)
    {
        m_pMaterialInstance = pMaterialInstance;
    }


    //  Gets the JointName.
    public String getJointName()
    {
        return(m_JointName);
    }

    //  Sets the JointName.
    public void setJointName(String jointName)
    {
        m_JointName = jointName;
    }

    //  Checks to see if the ColladaNode contains a joint.
    public boolean containsJoint()
    {
        if (m_bIsJoint)
            return(true);

        int a;
        PColladaNode pColladaNode;

        for (a=0; a<getChildNodeCount(); a++)
        {
            pColladaNode = getChildNode(a);

            if (pColladaNode.containsJoint())
                return(true);
        }
        
        return(false);
    }
    
    
    
    
    public String getControllerName()
    {
        return(m_ControllerName);
    }
    
    public void setControllerName(String controllerName)
    {
        m_ControllerName = controllerName;
    }



    //  Adds a skeleton.
    public void addSkeleton(String skeleton)
    {
        m_Skeletons.add(new String(skeleton));
    }

    //  Gets the number of skeletons.
    public int getSkeletonCount()
    {
        return(m_Skeletons.size());
    }

    //  Gets the skeleton at the specified index.
    public String getSkeleton(int index)
    {
        return( (String)m_Skeletons.get(index));
    }

    
    //  Gets the root ColladaNode.
    public PColladaNode getRootColladaNode()
    {
        PColladaNode pRootColladaNode = this;

        while (pRootColladaNode.getParentColladaNode() != null)
        {
            pRootColladaNode = pRootColladaNode.getParentColladaNode();
        }

        return(pRootColladaNode);
    }

    //  Dumps the Node and all it's children.
    public void dump()
    {
        dump("");
    }
    public void dump(String spacing)
    {
        if (m_JointName == null)
            System.out.println(spacing + this.getName());
        else
            System.out.println(spacing + this.getName() + ", " + m_JointName);

        if (this.getChildNodeCount() > 0)
        {
            PColladaNode pChildNode;

            for (int i=0; i<this.getChildNodeCount(); i++)
            {
                pChildNode = this.getChildNode(i);

                pChildNode.dump(spacing + "   ");
            }
        }
    }

    public int getNodeIndex()
    {
        int nodeIndex = 0;

        PColladaNode pParentColladaNode = this;

        while (pParentColladaNode.getParentColladaNode() != null)
        {
            nodeIndex++;
            pParentColladaNode = pParentColladaNode.getParentColladaNode();
        }

        return(nodeIndex);
    }

}   




