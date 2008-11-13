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


import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;



/**
 * Framework for applying chains of events to something.
 * TODO: Delegate execution logic
 * @author Chris Nagle
 */
public class InstructionProcessor
{
    private WorldManager        m_wm = null;
    private PScene              m_loadingPScene = null;
    private CharacterLoader     m_characterLoader = null;
    private SkeletonNode        m_skeleton = null;
    
    /**
     * The world manager is used to create temporal pscenes for the loading process
     * @param wm
     */
    public InstructionProcessor(WorldManager wm)
    {
        m_wm = wm;
    }

    //  Executes all instructions.
    public void execute(Instruction pRootInstruction)
    {   
        m_characterLoader = new CharacterLoader();
        
        printInstruction("", pRootInstruction);

        m_loadingPScene = new PScene(m_wm);
        executeInstruction(m_loadingPScene, pRootInstruction);
        
        // garbage collection...
        m_characterLoader = null;
        m_loadingPScene   = null;
        m_skeleton        = null;
    }

    public SkeletonNode getSkeleton() {
        return m_skeleton;
    }

    private void printInstruction(String spacing, Instruction pInstruction)
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

    private void executeInstruction(PScene loadingPScene, Instruction pInstruction)
    {
        try 
        {
            switch(pInstruction.getInstruction())
            {
                case addSkinnedMesh:
                {
                    String skinnedMeshName = pInstruction.getDataAsString();
                    if (!addSkinnedMesh(skinnedMeshName))
                        System.out.println("COLLADA configuration ERROR: was not able to ADD a skinned mesh!");
                }
                break;
                case addAttachment:
                {
                    Object [] array = (Object[])pInstruction.getData();
                    if (!addAttachment(array))
                        System.out.println("COLLADA configuration ERROR: was not able to ADD an ATTACHMENT!");
                }
                break;
                case deleteSkinnedMesh:
                {
                    String skinnedMeshName = pInstruction.getDataAsString();
                    if (!m_characterLoader.deleteSkinnedMesh(m_skeleton, skinnedMeshName))
                        System.out.println("COLLADA configuration ERROR: was not able to DELETE a skinned mesh!");
                }
                break;
                case loadAnimation:
                {
                    URL animationLocation = new URL(pInstruction.getDataAsString());
                    if (!m_characterLoader.loadAnimation(loadingPScene, m_skeleton, animationLocation, 0))
                        System.out.println("COLLADA configuration ERROR: was not able to LOAD ANIMATION!");
                }
                break;
                case loadFacialAnimation:
                {
                    URL animationLocation = new URL(pInstruction.getDataAsString());
                    if (!m_characterLoader.loadAnimation(loadingPScene, m_skeleton, animationLocation, 1))
                        System.out.println("COLLADA configuration ERROR: was not able to LOAD FACIAL ANIMATION!");
                }
                break;
                case loadBindPose:
                {
                    URL bindPoseLocation = new URL(pInstruction.getDataAsString());
                    m_skeleton = m_characterLoader.loadSkeletonRig(loadingPScene, bindPoseLocation);
                    if (m_skeleton == null)
                        System.out.println("COLLADA configuration ERROR: was not able to LOAD BIND POSE!");
                }
                break;
                case loadGeometry:
                {
                    URL geometryLocation = new URL(pInstruction.getDataAsString());
                    if (!m_characterLoader.loadGeometry(loadingPScene, geometryLocation))
                        System.out.println("COLLADA configuration ERROR: was not able to LOAD GEOMETRY!");
                }
                break;
                case setSkeleton:
                {
                    if (pInstruction.getData() instanceof SkeletonNode)
                        m_skeleton = (SkeletonNode) pInstruction.getData();
                    else
                        m_skeleton = null;   
                }
            }
        }
        catch (MalformedURLException ex){
            Logger.getLogger(InstructionProcessor.class.getName()).log(Level.SEVERE, null, ex); }
        
        if (pInstruction.getChildrenCount() > 0)
        {
            int a;
            Instruction pChildInstruction;

            for (a=0; a<pInstruction.getChildrenCount(); a++)
            {
                pChildInstruction = (Instruction)pInstruction.getChild(a);

                executeInstruction(loadingPScene, pChildInstruction);
            }
        }
    }
    
    private boolean addAttachment(Object[] array) 
    {
        if (m_skeleton == null)
            return false;

        // Find the mesh
        String meshName = (String)array[0];
        PNode node = m_loadingPScene.findChild(meshName);
        PPolygonMeshInstance mesh = null;
        if (node instanceof PPolygonMeshInstance)
            mesh = (PPolygonMeshInstance)node;
        if (mesh == null)
            return false;
        mesh.getTransform().setLocalMatrix(new PMatrix());
        
        // Find the joint
        String jointName = (String)array[1];
        SkinnedMeshJoint joint = m_skeleton.findSkinnedMeshJoint(jointName);
        if (joint == null)
            return false;

        // Create new joint
        PJoint newJoint = new PJoint(meshName + " joint", new PTransform((PMatrix)array[2]));
        newJoint.addChild(mesh);
        joint.addChild(newJoint);
        
        return true;
    }
    
    private boolean addSkinnedMesh(String skinnedMeshName) 
    {
        //  Find the SkinnedMesh that is the replacement.
        PPolygonSkinnedMesh pSkinnedMesh = null;
        
        List<PPolygonMesh> list = m_loadingPScene.getLocalGeometryList();
        Iterator<PPolygonMesh> mesh = list.iterator();
        while(mesh.hasNext())
        {
            PPolygonMesh it = mesh.next();
            if (it.getName().equals(skinnedMeshName))
            {
                pSkinnedMesh = (PPolygonSkinnedMesh) it;
                break;
            }
        }
        
        if (pSkinnedMesh != null)
        {
            // Make an instance
            PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_loadingPScene.addMeshInstance(pSkinnedMesh, new PMatrix());
            
            System.out.println("Added SkinnedMesh '" + skinnedMeshName + "' to skeleton.");

            //  Link the SkinnedMesh to the Skeleton.
            skinnedMeshInstance.setSkeletonNode(m_skeleton);
            skinnedMeshInstance.linkJointsToSkeletonNode(m_skeleton);
            // Add it to the skeleton
            m_skeleton.addChild(skinnedMeshInstance);
            
            return true;
        }
        return false;
    }

}



