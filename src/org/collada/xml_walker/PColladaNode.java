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

    Node                        m_pNode = null;

    String                      m_Name = null;

    PMatrix                     m_Matrix = new PMatrix();
    float                       []m_MatrixFloats = new float[16];

    boolean                     m_bIsJoint;
    String                      m_JointName;

    String                      m_MeshName = null;
    String                      m_MeshURL = null;

    String                      m_InstanceNodeName = null;

    ArrayList<PColladaNode>     m_ChildNodes = new ArrayList<PColladaNode>();

    String                      m_ControllerName = null;

    ArrayList                   m_Skeletons = new ArrayList();

    PColladaMaterialInstance    m_pMaterialInstance = null;




    /**
     * Default constructor.
     */
    public PColladaNode()
    {
    }

    /**
     * Constructor.
     * 
     * @param pNode - Pointer to collada Node.
     */
    public PColladaNode(Node pNode)
    {
        setNode(pNode);
    }



    /**
     * Gets the name of the PColladaNode.
     * 
     * @return
     */
    public String getName()
    {
        return(m_Name);
    }

    /**
     * Sets the name of the PColladaNode.
     * 
     * @param name
     */
    public void setName(String name)
    {
        m_Name = name;
    }



    /**
     * Set the pointer to the collada Node.
     * 
     * @param pNode
     */
    public void setNode(Node pNode)
    {
        m_pNode = pNode;
    }



    /**
     * Gets the Matrix assigned to the PColladaNode.
     * 
     * @return
     */
    public PMatrix getMatrix()
    {
        return(m_Matrix);
    }

    /**
     * Sets the Matrix of the PColladaNode.
     * 
     * @param pMatrixFloats
     */
    public void setMatrix(float []pMatrixFloats)
    {
        m_Matrix.set(pMatrixFloats);
        
        for (int a=0; a<16; a++)
            m_MatrixFloats[a] = pMatrixFloats[a];
    }

    /**
     * Gets the float[] reference that contains the Matrix data.
     * 
     * @return float[]
     */
    public float []getMatrixFloats()
    {
        return(m_MatrixFloats);
    }



    /**
     * Gets boolean indicating whether the PColladaNode is a joint.
     * 
     * @return
     */
    public boolean isJoint()
    {
        return(m_bIsJoint);
    }

    /**
     * Sets boolean indicating whether the Node is a joint.
     * 
     * @param bIsJoint
     */
    public void isJoint(boolean bIsJoint)
    {
        m_bIsJoint = bIsJoint;
    }

    /**
     * Finds the ColladaNode with the specified jointName.
     * Grandchildren will be searched over too.
     * 
     * @param jointName
     * @return
     */
    public PColladaNode findJoint(String jointName)
    {
        if (m_JointName != null && m_JointName.equals(jointName))
            return(this);

        //  Iterate through the children.
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

    /**
     * Gets the mesh name.
     * 
     * @return String
     */
    public String getMeshName()
    {
        return(m_MeshName);
    }

    /**
     * Sets the mesh name.
     * 
     * @param meshName
     */
    public void setMeshName(String meshName)
    {
        m_MeshName = meshName;
    }



    /**
     * Gets name fo the InstanceNode.
     * 
     * @return String
     */
    public String getInstanceNodeName()
    {
        return(m_InstanceNodeName);
    }

    /**
     * Set the name of the InstanceNode.
     * 
     * @param String instanceNodeName
     */
    public void setInstanceNodeName(String instanceNodeName)
    {
        m_InstanceNodeName = instanceNodeName;
    }



    /**
     * Adds a child node.
     * 
     * @param PColladaNode pChildNode
     */
    public void addChildNode(PColladaNode pChildNode)
    {
        if (pChildNode != null)
        {
            pChildNode.m_pParentNode = this;
            m_ChildNodes.add(pChildNode);
        }
    }

    /**
     * Gets the number of child nodes.
     * 
     * @return int
     */
    public int getChildNodeCount()
    {
        return(m_ChildNodes.size());
    }

    /**
     * Gets the child node at the specified index.
     * 
     * @param index
     * @return
     */
    public PColladaNode getChildNode(int index)
    {
        return( (PColladaNode)m_ChildNodes.get(index));
    }

    /**
     * Finds the PColladaNode with the specified name.
     * 
     * @param String nodeName
     * @return PColladaNode
     */
    public PColladaNode findNode(String nodeName)
    {
        if (m_Name.equals(nodeName))
            return this;

        PColladaNode pFoundColladaNode = null;

        for (PColladaNode node : m_ChildNodes)
        {
            pFoundColladaNode = node.findNode(nodeName);
            if (pFoundColladaNode != null)
                return pFoundColladaNode;
        }

        return null;
    }

    /**
     * Gets the parent PColladaNode.
     * 
     * @return PColladaNode
     */
    public PColladaNode getParentColladaNode()
    {
        return(m_pParentNode);
    }
            
    public String getMeshURL()
    {
        return m_MeshURL;
    }

    public void setMeshURL(String meshURL)
    {
        m_MeshURL = meshURL;
    }

    /**
     * Gets the MaterialInstance.
     * 
     * @return PColladaMaterialInstance
     */
    public PColladaMaterialInstance getMaterialInstance()
    {
        return(m_pMaterialInstance);
    }
    
    /**
     * Sets the MaterialInstance.
     * 
     * @param PColladaMaterialInstance pMaterialInstance
     */
    public void setMaterialInstance(PColladaMaterialInstance pMaterialInstance)
    {
        m_pMaterialInstance = pMaterialInstance;
    }


    /**
     * Gets the joint name.
     * 
     * If the ColladaNode is a joint, it's jointName might be different that
     * the PColladaNode's name itself.
     * 
     * @return String
     */
    public String getJointName()
    {
        return(m_JointName);
    }

    /**
     * Sets the joint name.
     * 
     * If the ColladaNode is a joint, it's jointName might be different that
     * the PColladaNode's name itself.
     * 
     * @param String jointName
     */
    public void setJointName(String jointName)
    {
        m_JointName = jointName;
    }

    /**
     * Checks to see if the PColladaNode contains a joint.
     * 
     * @return boolean
     */
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
    
    
    
    /**
     * Gets the controller name.
     * 
     * @return String
     */
    public String getControllerName()
    {
        return(m_ControllerName);
    }
    
    /**
     * Set the controller name.
     * 
     * @param String controllerName
     */
    public void setControllerName(String controllerName)
    {
        m_ControllerName = controllerName;
    }



    /**
     * Adds a skeleton.
     * A PColladaNode might contain multiple skeletons.
     * 
     * @param String skeleton
     */
    public void addSkeleton(String skeleton)
    {
        m_Skeletons.add(new String(skeleton));
    }

    /**
     * Gets the number of skeletons.
     *
     * @return int
     */
    public int getSkeletonCount()
    {
        return(m_Skeletons.size());
    }

    /**
     * Gets the skeleton at the specified index.
     * 
     * @param index
     * @return String
     */
    public String getSkeleton(int index)
    {
        return( (String)m_Skeletons.get(index));
    }

    
    /**
     * Gets the root PColladaNode.
     * 
     * @return PColladaNode.
     */
    public PColladaNode getRootColladaNode()
    {
        PColladaNode pRootColladaNode = this;

        while (pRootColladaNode.getParentColladaNode() != null)
        {
            pRootColladaNode = pRootColladaNode.getParentColladaNode();
        }

        return(pRootColladaNode);
    }



    /**
     * Dumps the node and all it's children.
     */
    public void dump()
    {
        dump("");
    }

    /**
     * Dumps the node and all it's children.
     * 
     * @param String spacing
     */
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

    /**
     * Gets the index of the Node.
     * 
     * @return int
     */
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




