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
    private PColladaNode                m_pParentNode = null;
    private String                      m_Name = null;
    private PMatrix                     m_Matrix = null;
    private boolean                     m_bIsJoint = false;

    private String                      m_JointName = null;
    private String                      m_MeshName = null;
    private String                      m_MeshURL = null;
    private String                      m_InstanceNodeName = null;
    private String                      m_ControllerName = null;
    private ArrayList<PColladaNode>     m_ChildNodes = null;
    private ArrayList<String>           m_skeletonNames = null;
    //private PColladaMaterialInstance    m_pMaterialInstance = null;
    private ColladaMaterial             m_colladaMaterial = null;




    /**
     * Default constructor.
     */
    public PColladaNode()
    {
    }

    public Iterable<PColladaNode> getChildren() {
        return m_ChildNodes;
    }


    /**
     * Gets the name of the PColladaNode.
     * 
     * @return
     */
    public String getName()
    {
        return m_Name;
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
     * Gets the Matrix assigned to the PColladaNode.
     * 
     * @return
     */
    public PMatrix getMatrix()
    {
        return m_Matrix;
    }

    /**
     * Sets the Matrix of the PColladaNode.
     * 
     * @param pMatrixFloats
     */
    public void setMatrix(float []pMatrixFloats)
    {
        if (m_Matrix == null)
            m_Matrix = new PMatrix(pMatrixFloats);
        else
            m_Matrix.set(pMatrixFloats);
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
        // It could be us!
        if (m_JointName != null && m_JointName.equals(jointName))
            return this;
        // make sure we even have children
        if (m_ChildNodes == null || m_ChildNodes.size() == 0)
            return null;

        PColladaNode result = null;
        for (PColladaNode kid : m_ChildNodes)
        {
            result = kid.findJoint(jointName);
            if (result != null)
                break;
        }

        return result;
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
        return m_InstanceNodeName;
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
        if (m_ChildNodes == null)
            m_ChildNodes = new ArrayList<PColladaNode>();

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
        if (m_ChildNodes != null)
            return(m_ChildNodes.size());
        else
            return 0;
    }

    /**
     * Gets the child node at the specified index.
     * 
     * @param index
     * @return
     */
    public PColladaNode getChildNode(int index)
    {
        if (m_ChildNodes != null)
            return m_ChildNodes.get(index);
        else return null;
    }

    /**
     * Finds the PColladaNode with the specified name.
     * 
     * @param String nodeName
     * @return PColladaNode
     */
    public PColladaNode findNode(String nodeName)
    {
        // Could be us!
        if (m_Name.equals(nodeName))
            return this;
        // make sure we have children
        if (m_ChildNodes == null || m_ChildNodes.size() == 0)
            return null;

        PColladaNode result = null;

        for (PColladaNode node : m_ChildNodes)
        {
            result = node.findNode(nodeName);
            if (result != null)
                break;
        }

        return result;
    }

    /**
     * Gets the parent PColladaNode.
     * 
     * @return PColladaNode
     */
    public PColladaNode getParentColladaNode()
    {
        return m_pParentNode;
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
    public ColladaMaterial getMaterial()
    {
        return m_colladaMaterial;
    }
    
    /**
     * Sets the MaterialInstance.
     * 
     * @param PColladaMaterialInstance pMaterialInstance
     */
    public void setMaterial(ColladaMaterial material)
    {
        m_colladaMaterial = material;
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
        return m_JointName;
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
     * Checks to see if the PColladaNode contains OR IS a joint.
     * 
     * @return boolean
     */
    public boolean containsJoint()
    {
        boolean result = false;
        if (m_bIsJoint == true)
            result = true;
        else
        {
            for (PColladaNode node : m_ChildNodes)
            {
                if (node.containsJoint() == true)
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
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
        if (m_skeletonNames == null)
            m_skeletonNames = new ArrayList<String>();
        m_skeletonNames.add(skeleton);
    }

    /**
     * Gets the number of skeletons.
     *
     * @return int
     */
    public int getSkeletonCount()
    {
        if (m_skeletonNames != null)
            return m_skeletonNames.size();
        else
            return 0;
    }

    /**
     * Gets the skeleton at the specified index.
     * 
     * @param index
     * @return String
     */
    public String getSkeleton(int index)
    {
        if (m_skeletonNames != null)
            return m_skeletonNames.get(index);
        else
            return null;
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
}   




