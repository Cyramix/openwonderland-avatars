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



/**
 *
 * @author Chris Nagle
 */
public class PColladaSkin
{
    String                          m_Name = "";
    String                          m_MeshName = "";
    PMatrix                         m_BindMatrix = new PMatrix();

    ArrayList<String>               m_BoneNames = new ArrayList<String>();
    ArrayList<String>               m_JointNames = new ArrayList<String>();
    ArrayList<PMatrix>              m_BindMatrices = new ArrayList<PMatrix>();
    ArrayList<PColladaSkinWeight>   m_SkinWeights = new ArrayList<PColladaSkinWeight>();



    //  Constructor.
    public PColladaSkin()
    {
    }
    public PColladaSkin(String name)
    {
        m_Name = name;
    }



    //  Gets the name of the ColladaSkin.
    public String getName()
    {
        return(m_Name);
    }

    //  Sets the name of the ColladaSkin.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Gets the MeshName.
    public String getMeshName()
    {
        return(m_MeshName);
    }

    //  Sets the MeshName.
    public void setMeshName(String meshName)
    {
        m_MeshName = meshName;
    }



    //  Sets the BindMatrix.
    public void setBindMatrix(PMatrix pBindMatrix)
    {
        m_BindMatrix.set(pBindMatrix);
    }



    //  Adds a BoneName.
    public void addBoneName(String boneName)
    {
        m_BoneNames.add(boneName);
    }

    //  Checks to see if the Skin contains a Bone with the specified name. 
    public boolean containsBoneName(String boneName)
    {
        int a;
        String theBoneName;
        
        for (a=0; a<m_BoneNames.size(); a++)
        {
            theBoneName = m_BoneNames.get(a);
            
            if (theBoneName.equals(boneName))
                return(true);
        }

        return(false);
    }

    //  Gets the root BoneName.
    public String getRootBoneName()
    {
        if (m_BoneNames.size() == 0)
            return("");
        
        return(m_BoneNames.get(0));
    }



    //  Adds a JointName.
    public void addJointName(String jointName)
    {
        m_JointNames.add(jointName);
    }

    //  Gets the number of JointNames.
    public int getJointNameCount()
    {
        return(m_JointNames.size());
    }

    //  Gets the JointName at the specified index.
    public String getJointName(int Index)
    {
        return(m_JointNames.get(Index));
    }

    //  Checks to see if the Skin contains a Joint with the specified name. 
    public boolean containsJointName(String jointName)
    {
        for (int a=0; a<m_JointNames.size(); a++)
        {
            if (m_JointNames.get(a).equals(jointName))
                return(true);
        }

        return(false);
    }



    //  Adds a BindMatrix.
    public void addBindMatrix(PMatrix pBindMatrix)
    {
        m_BindMatrices.add(pBindMatrix);
    }
    
    //  Gets the number of BindMatrices.
    public int getBindMatrixCount()
    {
        return(m_BindMatrices.size());
    }

    //  Gets the BindMatrix at the specified index.
    public PMatrix getBindMatrix(int index)
    {
        return(m_BindMatrices.get(index));
    }



    //  Adds a SkinWeight.
    public void addSkinWeight(PColladaSkinWeight pSkinWeight)
    {
        m_SkinWeights.add(pSkinWeight);
    }

    //  Gets the number of SkinWeights.
    public int getSkinWeightCount()
    {
        return(m_SkinWeights.size());
    }

    //  Gets the SkinWeight at the specified index.
    public PColladaSkinWeight getSkinWeight(int index)
    {
        return(m_SkinWeights.get(index));
    }

}



