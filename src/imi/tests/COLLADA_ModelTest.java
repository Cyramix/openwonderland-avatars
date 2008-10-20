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
package imi.tests;


import java.net.MalformedURLException;
import java.util.ArrayList;

import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import java.net.URL;


/**
 *
 * @author Chris Nagle
 */
public class COLLADA_ModelTest extends DemoBase
{
   
    
    public COLLADA_ModelTest(String[] args)
    {
        super(args);
    }
    
    public static void main(String[] args)
    {
        COLLADA_ModelTest modelTest = new COLLADA_ModelTest(args);
    }


    //  Loads a Skeleton.
    public SkeletonNode loadSkeleton(PScene pScene, URL modelLocation)
    {
        boolean bResult = false;
        SkeletonNode pSkeletonNode = null;

//        pScene.setUseRepository(false);

        //  Load the collada file to the PScene
        Collada colladaLoader = new Collada();
        PScene colladaScene = new PScene("COLLADA Scene", pScene.getWorldManager());
        colladaScene.setUseRepository(false);
        try
        {
            //  Load only the Skeleton (Geometry and Rig).
            colladaLoader.setLoadFlags(true, true, false);
            bResult = colladaLoader.load(colladaScene, modelLocation);
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        } 

//        pScene.setUseRepository(true);

        if (bResult)
            pSkeletonNode = colladaLoader.getSkeletonNode();
        
        return(pSkeletonNode);
    }

    //  Loads a Skeleton animation.
    public boolean loadSkeletonAnimation(PScene pScene, SkeletonNode pSkeletonNode, URL animationLocation)
    {
        boolean bResult = false;

//        pScene.setUseRepository(false);

        //  Load the collada file to the PScene
        Collada colladaLoader = new Collada();
        PScene colladaScene = new PScene("COLLADA Scene", pScene.getWorldManager());
        colladaScene.setUseRepository(false);
        colladaLoader.setSkeletonNode(pSkeletonNode);

        try
        {
            //  Load only the Skeleton (Geometry and Rig).
            colladaLoader.setLoadFlags(false, false, true);
            bResult = colladaLoader.load(colladaScene, animationLocation);
        }
        catch (Exception ex)
        {
            System.out.println("Exception occured while loading skeleton.");
            ex.printStackTrace();
        } 

//        pScene.setUseRepository(true);

        return(bResult);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        URL modelLocation = null;
        try
        {
            modelLocation = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/FlipFlopsFeet.dae");
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        SharedAsset colladaAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, modelLocation));
        colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 3, "FlipFlops", null));
        PPolygonModelInstance modelInst = pscene.addModelInstance("Collada Model", colladaAsset, new PMatrix());

        modelInst.dump();

/*
        if (modelInst.getChild(0) instanceof SkeletonNode)
        {
            SkeletonNode pSkeletonNode = (SkeletonNode)modelInst.getChild(0);

//            pSkeletonNode.dump();

            ArrayList<PPolygonSkinnedMeshInstance> skinnedMeshInstances = pSkeletonNode.getSkinnedMeshInstances();
            PPolygonSkinnedMeshInstance pSkinnedMeshInstance;

            String textureFilename = imi.utils.FileUtils.findTextureFile("MaleCHeadCLR.png");
  //          String[] textures = new String[1];
  //          textures[0] = textureFilename;
  //          PMeshMaterial mat = new PMeshMaterial("MyName!");
  //          mat.setTextures(textures);
//            mat.setShader(new VertexDeformer(wm));

            for (int i=0; i<skinnedMeshInstances.size(); i++)
            {
                pSkinnedMeshInstance = skinnedMeshInstances.get(i);

                pSkinnedMeshInstance.getMaterialRef();
//                pSkinnedMeshInstance.setMaterial(mat);
//                pSkinnedMeshInstance.setUseGeometryMaterial(false);

                pSkinnedMeshInstance.buildAnimationJointMapping(pSkeletonNode);   
            }

//            pscene.addModelInstance(pSkeletonNode, new PMatrix());

        
            //  Assign the specified shader to all SkinnedMeshes.
            pSkeletonNode.setShader(new VertexDeformer(wm));
        }
*/

//        modelInst.getTransform().getLocalMatrix(true).setScale(3.0f);
//        modelInst.getTransform().getLocalMatrix(true).setTranslation(new Vector3f(4.95f, 1.3f, 0.3f));

        pscene.setDirty(true, true);
    }



    private void createSkinnedAnimationProcessors(PNode pNode, ArrayList<ProcessorComponent> processors)
    {
        if (pNode instanceof PPolygonSkinnedMeshInstance)
        {
            PPolygonSkinnedMeshInstance pSkinnedMeshInstance = (PPolygonSkinnedMeshInstance)pNode;
//            ((PPolygonSkinnedMeshInstance)pNode).getAnimationState().setPauseAnimation(true);

/*
    LHandShape-skin
    *** LegsNudeShape-skin
    rightEyeGeoShape-skin
    leftEyeGeoShape-skin
    *** TorsoNudeShape-skin
    LFootNudeShape-skin***
    HeadGeoShape-skin
    TongueGeoShape-skin
    LowerTeethShape-skin
    UpperTeethShape-skin
    RHandShape-skin
    RFootNudeShape-skin
*/
                    
//            if (pSkinnedMeshInstance.getName().equals("LFootNudeShape"))
//            if (pSkinnedMeshInstance.getName().equals("TorsoNudeShape"))
//            if (pSkinnedMeshInstance.getName().equals("LHandShape"))
            //processors.add(new SkinnedAnimationProcessor(pSkinnedMeshInstance));
        }

        if (pNode.getChildrenCount() > 0)
        {
            int a;
            PNode pChildNode;
            
            for (a=0; a<pNode.getChildrenCount(); a++)
            {
                pChildNode = pNode.getChild(a);
                
                createSkinnedAnimationProcessors(pChildNode, processors);
            }
        }
    }


    //  Gets the SkinnedMesh with the specified name.
    private PPolygonSkinnedMesh getSkinnedMeshInNodeHiearchy(PNode pNode, String meshName)
    {
        if (pNode instanceof PPolygonSkinnedMeshInstance)
        {
            PPolygonSkinnedMeshInstance pSkinnedMeshInstance = (PPolygonSkinnedMeshInstance)pNode;

            PPolygonSkinnedMesh pSkinnedMesh = (PPolygonSkinnedMesh)pSkinnedMeshInstance.getGeometry();

            if (pSkinnedMesh.getName().equals(meshName))
                return(pSkinnedMesh);
        }

        if (pNode.getChildrenCount() > 0)
        {
            int a;
            PNode pChildNode;
            PPolygonSkinnedMesh pTheSkinnedMesh;
            
            for (a=0; a<pNode.getChildrenCount(); a++)
            {
                pChildNode = pNode.getChild(a);
                
                pTheSkinnedMesh = getSkinnedMeshInNodeHiearchy(pChildNode, meshName);
                if (pTheSkinnedMesh != null)
                    return(pTheSkinnedMesh);
            }
        }

        return(null);
    }

    
    
    private void dumpSkinnedMeshMatrices(PNode pNode)
    {
        if (pNode instanceof PPolygonSkinnedMeshInstance)
        {
            PPolygonSkinnedMeshInstance pSkinnedMeshInstance = (PPolygonSkinnedMeshInstance)pNode;
            PMatrix pMatrix = pNode.getTransform().getLocalMatrix(false);
            float []values = pMatrix.getFloatArray();
            
            System.out.println("SkinnedMeshInstance:  " + pNode.getName());
            System.out.println("   SkinnedMesh:  " + pSkinnedMeshInstance.getGeometry().getName());
            System.out.println("   " + values[0] + ", " + values[1] + ", " + values[2] + ", " + values[3]);
            System.out.println("   " + values[4] + ", " + values[5] + ", " + values[6] + ", " + values[7]);
            System.out.println("   " + values[8] + ", " + values[9] + ", " + values[10] + ", " + values[11]);
            System.out.println("   " + values[12] + ", " + values[13] + ", " + values[14] + ", " + values[15]);
        }

        if (pNode.getChildrenCount() > 0)
        {
            int a;
            PNode pChildNode;
            
            for (a=0; a<pNode.getChildrenCount(); a++)
            {
                pChildNode = pNode.getChild(a);
                
                dumpSkinnedMeshMatrices(pChildNode);
            }
        }
    }

}

