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


import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.net.MalformedURLException;
import java.util.ArrayList;

import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

import imi.loaders.collada.ColladaLoaderParams;
import imi.scene.camera.behaviors.TumbleObjectCamModel;
import imi.scene.camera.state.TumbleObjectCamState;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.utils.PMeshUtils;
import java.io.File;
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
    
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        // load a model from one of the listed locations below
        URL modelLocation = null;
        try
        {
            //modelLocation = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/FlipFlopsFeet.dae");
            //modelLocation = new File("assets/models/collada/Environments/Milan/DSI.dae").toURI().toURL();
            modelLocation = new File("assets/models/collada/Avatars/MaleZip/MaleBind.dae").toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        SharedAsset colladaAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelLocation));
        //colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 3, "FlipFlops", null));
        colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 3, "Milan", null));
        PPolygonModelInstance modelInst = pscene.addModelInstance("Collada Model", colladaAsset, new PMatrix());
        // create and add a standard cube to see the default lighting
        PPolygonMesh cube = PMeshUtils.createBox("Cube!", Vector3f.ZERO, 2, 2, 2, ColorRGBA.cyan);
        // add it to the pscene
        PPolygonModelInstance modelInst2 = pscene.addModelInstance(cube, new PMatrix(Vector3f.UNIT_Z.mult(5.0f)));
        // grab the mesh instance
        PPolygonMeshInstance meshInst = (PPolygonMeshInstance) modelInst2.getChild(0);
        // assign a texture to the mesh instance
        PMeshMaterial material = new PMeshMaterial("cubeTex", "assets/textures/checkerboard.png");
        meshInst.setMaterial(material);
        meshInst.setUseGeometryMaterial(false);

        // NEW CAMERA MODEL TEST CODE
        TumbleObjectCamModel tumbleModel = new TumbleObjectCamModel();
        TumbleObjectCamState tumbleState = new TumbleObjectCamState(modelInst);
        tumbleState.setCameraPosition(new Vector3f(1,2,-6));
        m_cameraProcessor.setCameraBehavior(tumbleModel, tumbleState);
        
        // test setting a different focal point, say at the sphere
        tumbleState.setTargetFocalPoint(modelInst2.getTransform().getWorldMatrix(false).getTranslation());
        
        pscene.setDirty(true, true);
    }



    private void createSkinnedAnimationProcessors(PNode pNode, ArrayList<ProcessorComponent> processors)
    {
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

}

