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


import javolution.util.FastTable;
import imi.scene.PMatrix;



/**
 *
 * @author Chris Nagle
 */
public class PColladaSkin
{
    private String                          m_Name          = null;
    private String                          m_MeshName      = null;
    private PMatrix                         m_BindMatrix    = new PMatrix();

    private FastTable<String>               m_BoneNames     = new FastTable<String>();
    private FastTable<String>               m_JointNames    = new FastTable<String>();
    private FastTable<PMatrix>              m_BindMatrices  = new FastTable<PMatrix>();
    private FastTable<PColladaSkinWeight>   m_SkinWeights   = new FastTable<PColladaSkinWeight>();



    /**
     * Default constructor.
     */
    public PColladaSkin()
    {
    }

    /**
     * Constructor.
     * 
     * @param String name - The name of the Skin.
     */
    public PColladaSkin(String name)
    {
        m_Name = name;
    }



    /**
     * Gets the name of the PColladaSkin.
     * 
     * @return String
     */
    public String getName()
    {
        return m_Name;
    }

    /**
     * Sets the name of the PColladaSkin.
     * 
     * @param String name
     */
    public void setName(String name)
    {
        m_Name = name;
    }



    /**
     * Gets the MeshName of the Skin.
     * 
     * @return String
     */
    public String getMeshName()
    {
        return m_MeshName;
    }

    /**
     * Sets the MeshName of the Skin.
     * 
     * @param String meshName
     */
    public void setMeshName(String meshName)
    {
        m_MeshName = meshName;
    }



    /**
     * Sets the BindMatrix of the Skin.
     * 
     * @param PMatrix pBindMatrix
     */
    public void setBindMatrix(PMatrix pBindMatrix)
    {
        m_BindMatrix.set(pBindMatrix);
    }



    /**
     * Adds a bone name that the Skin references.
     * 
     * @param String boneName
     */
    public void addBoneName(String boneName)
    {
        if (boneName != null)
            m_BoneNames.add(boneName);
    }

    /**
     * Checks to see if the Skin contains a Bone with the specified name. 
     * 
     * @param String boneName
     * @return boolean
     */
    public boolean containsBoneName(String boneName)
    {
        for (String name : m_BoneNames)
        {
            if (boneName.equals(name))
                return true;
        }
        return false;
    }

    /**
     * Gets the root BoneName.
     * @return String
     */
    public String getRootBoneName()
    {
        if (m_BoneNames.size() == 0)
            return null;
        
        return m_BoneNames.get(0);
    }



    /**
     * Adds a joint name.
     *
     * @param String jointName
     */
    public void addJointName(String jointName)
    {
        m_JointNames.add(jointName);
    }

    /**
     * Gets the number of joint names.
     * @return int
     */
    public int getJointNameCount()
    {
        return m_JointNames.size();
    }

    /**
     * Gets the joint name at the specified index.
     * 
     * @param int Index
     * @return String
     */
    public String getJointName(int Index)
    {
        return m_JointNames.get(Index);
    }

    /**
     * Checks to see if the Skin contains a Joint with the specified name. 
     * 
     * @param String jointName
     * @return boolean
     */
    public boolean containsJointName(String jointName)
    {
        for (String name : m_JointNames)
        {
            if (jointName.equals(name))
                return true;
        }
        return false;
    }



    /**
     * Adds a bind matrix.
     * 
     * @param PMatrix pBindMatrix
     */
    public void addBindMatrix(PMatrix pBindMatrix)
    {
        m_BindMatrices.add(pBindMatrix);
    }
    
    /**
     * Gets the number of bind matrices.
     * 
     * @return int
     */
    public int getBindMatrixCount()
    {
        return(m_BindMatrices.size());
    }

    /**
     * Gets the bind matrix at the specified index.
     * 
     * @param index
     * @return PMatrix
     */
    public PMatrix getBindMatrix(int index)
    {
        return(m_BindMatrices.get(index));
    }



    /**
     * Adds a skin weight.
     * 
     * @param PColladaSkinWeight pSkinWeight
     */
    public void addSkinWeight(PColladaSkinWeight pSkinWeight)
    {
        m_SkinWeights.add(pSkinWeight);
    }

    /**
     * Gets the number of skin weights.
     * 
     * @return int
     */
    public int getSkinWeightCount()
    {
        return(m_SkinWeights.size());
    }

    /**
     * Gets the skin weight at the specified index.
     * 
     * @param index
     * @return PColladaSkinWeight
     */
    public PColladaSkinWeight getSkinWeight(int index)
    {
        return(m_SkinWeights.get(index));
    }

    Iterable<String> getJointNames() {
        return m_JointNames;
    }

}



