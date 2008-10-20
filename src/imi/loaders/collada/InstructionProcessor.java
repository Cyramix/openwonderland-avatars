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
package imi.loaders.collada;


import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Framework for applying chains of events to something.
 * TODO: Delegate execution logic
 * @author Chris Nagle
 */
public class InstructionProcessor extends PNode
{
    CharacterLoader     m_pCharacterLoader = null;
    SkeletonNode        m_pSkeleton = null;



    //  Constructor.
    public InstructionProcessor()
    {
    }


    //  Gets the Skeleton.
    public SkeletonNode getSkeleton()
    {
        return(m_pSkeleton);
    }



    //  Executes all instructions.
    public void execute(PScene pScene, Instruction pRootInstruction)
    {
        m_pCharacterLoader = new CharacterLoader();
        
        printInstruction("", pRootInstruction);


        executeInstruction(pScene, pRootInstruction);
        
        
        m_pCharacterLoader = null;
    }

    public void printInstruction(String spacing, Instruction pInstruction)
    {
        System.out.println(spacing + pInstruction.getInstruction() + " '" + pInstruction.getDataAsString() + "'");

        if (pInstruction.getChildrenCount() > 0)
        {
            int a;
            Instruction pChildInstruction;
            
            for (a=0; a<pInstruction.getChildrenCount(); a++)
            {
                pChildInstruction = (Instruction)pInstruction.getChild(a);
                
                printInstruction(spacing + "   ", pChildInstruction);
            }
        }
    }

    public void executeInstruction(PScene pScene, Instruction pInstruction)
    {
        if (pInstruction.getInstruction().equals("loadBindPose"))
        {
            URL bindPoseLocation = null;
            try
            {
                bindPoseLocation = new URL(pInstruction.getDataAsString());
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(InstructionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            m_pSkeleton = m_pCharacterLoader.loadSkeletonRig(pScene, bindPoseLocation);
        }
        else if (pInstruction.getInstruction().equals("loadGeometry"))
        {
            URL geometryLocation = null;
            try
            {
                geometryLocation = new URL(pInstruction.getDataAsString());
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(InstructionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            m_pCharacterLoader.loadGeometry(pScene, m_pSkeleton, geometryLocation);
        }

        else if (pInstruction.getInstruction().equals("deleteSkinnedMesh"))
        {
            String skinnedMeshName = pInstruction.getDataAsString();
            
            m_pCharacterLoader.deleteSkinnedMesh(m_pSkeleton, skinnedMeshName);
        }
        else if (pInstruction.getInstruction().equals("addSkinnedMesh"))
        {
            String skinnedMeshName = pInstruction.getDataAsString();
            
            m_pCharacterLoader.addSkinnedMesh(m_pSkeleton, skinnedMeshName);
        }
        else if (pInstruction.getInstruction().equals("loadAnimation"))
        {
            URL animationLocation = null;
            try
            {
                animationLocation = new URL(pInstruction.getDataAsString());
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(InstructionProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            m_pCharacterLoader.loadAnimation(pScene, m_pSkeleton, animationLocation);
        }
        else if (pInstruction.getInstruction().equals("setSkeleton"))
        {
            if (pInstruction.getData() instanceof SkeletonNode)
                m_pSkeleton = (SkeletonNode) pInstruction.getData();
            else
                m_pSkeleton = null;
        }

    
        if (pInstruction.getChildrenCount() > 0)
        {
            int a;
            Instruction pChildInstruction;

            for (a=0; a<pInstruction.getChildrenCount(); a++)
            {
                pChildInstruction = (Instruction)pInstruction.getChild(a);

                executeInstruction(pScene, pChildInstruction);
            }
        }
    }

}



