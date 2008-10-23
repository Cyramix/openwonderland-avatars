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
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.SkinnedAnimationProcessor;
import java.io.File;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author Chris Nagle
 */
public class COLLADAFace extends DemoBase
{
    public COLLADAFace(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        COLLADAFace worldTest = new COLLADAFace(args);
    }
    
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        // position and orient the camera
        
        //m_cameraProcessor.setPosition(new Vector3f(-10.0f, 0.0f, 10.0f));
        //m_cameraProcessor.setRotation(0.0f, 135.0f);
        
        String modelFilename = "assets/models/collada/Head/HeadRig1.dae";
        
        pscene.setUseRepository(false);
        
        ColladaLoaderParams params = new ColladaLoaderParams(true, true, true, false, 4, "ColladaHead", null);
        
        SharedAsset headAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, modelFilename), null, params);
        AssetInitializer headInit = new AssetInitializer() {
            public boolean initialize(Object asset) {
                
                if (((PNode)asset).getChild(0).getChild(0) instanceof PPolygonSkinnedMeshInstance)
                {
                    PPolygonSkinnedMeshInstance skinned = (PPolygonSkinnedMeshInstance)((PNode)asset).getChild(0).getChild(0);
                    skinned.getGeometry().getMaterialRef().setTexture(new File("assets/textures/Head/TestHead1.png"), 0);
                    skinned.setUseGeometryMaterial(true); // refresh.. apply the material... should we expose applyMaterial(), make it public?
                }
                return true;
                
            }
        };
        headAsset.setInitializer(headInit);
        PPolygonModelInstance modelInst = pscene.addModelInstance("Head Model", headAsset, new PMatrix());
        processors.add(new SkinnedAnimationProcessor((SkeletonNode)modelInst.getChild(0)));
    }
}
