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

import com.jme.image.Texture.ApplyMode;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import java.util.ArrayList;
import org.jdesktop.mtgame.*;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.PMeshUtils;
import java.io.File;

/**
 * This class provides a simple test ground for loading a texture and bump
 * mapping it as well as specular mapping
 * @author Ronald E Dahlgren
 */
public class NormalMappingTest extends DemoBase
{
    
    public NormalMappingTest(String[] args) 
    {
        super(args);
    }
  
    
    public static void main(String[] args) {
        NormalMappingTest worldTest = new NormalMappingTest(args);
    }
    
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        final WorldManager fwm = wm;
        pscene.setUseRepository(true); 
        // Create the beast
        PPolygonModelInstance modelInst = null;
        SharedAsset ninja = new SharedAsset(
                ((Repository)wm.getUserData(Repository.class)),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D,
                "assets/models/ms3d/beast.ms3d"));
        
        ninja.setInitializer(new AssetInitializer() {

            public boolean initialize(Object asset)
            {
                PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");
                meshInst.getGeometry().setUniformTexCoords(true);
                PMeshMaterial meshMat = new PMeshMaterial("BeastMaterial");
                // Textures
                meshMat.setTexture(new File("assets/textures/beast1.jpg"), 0);
                meshMat.setTexture(new File("assets/textures/n_beast.png"), 1);
                meshMat.setTexture(new File("assets/textures/spec_beast.jpg"), 2);
                // Shaders
                //meshInst.buildAnimationJointMapping((SkeletonNode)asset);
                meshMat.setShader(new VertDeformerWithSpecAndNormalMap(fwm));
                //meshMat.setVertShader(new File("assets/shaders/NormalMapSpecMap_Rev2.vert"));
                //meshMat.setFragShader(new File("assets/shaders/NormalMapSpecMap_Rev2.frag"));
                
                meshMat.setTextureMode(1, ApplyMode.Add);
                meshMat.setTextureMode(2, ApplyMode.Modulate);
                
                // now assign it
                meshInst.getGeometry().submit(new PPolygonTriMeshAssembler());
                meshInst.getGeometry().setNumberOfTextures(3);
             

                meshInst.setMaterial(meshMat);
                meshInst.setUseGeometryMaterial(false);

                return true;
            }
        });
        
        
        modelInst = pscene.addModelInstance(ninja, new PMatrix(new Vector3f(0,0,10)));
        
        
        // add an animation processor
        processors.add(new SkinnedAnimationProcessor(modelInst));
        
        // Add in a reference box
        modelInst = pscene.addModelInstance(PMeshUtils.createBox("Box", Vector3f.ZERO, 10, 10, 10, ColorRGBA.blue), new PMatrix(new Vector3f(0,12,12)));
        PPolygonMeshInstance meshInst = (PPolygonMeshInstance)modelInst.getChild(0);
        // make sure the geometry has been built
        meshInst.getGeometry().submit(new PPolygonTriMeshAssembler());
        
        PMeshMaterial testmaterial = new PMeshMaterial("TestMaterial");
        testmaterial.setTexture("assets/textures/checkerboard2.PNG", 0);
        testmaterial.setTexture("assets/textures/normalmap2.jpg", 1);
        testmaterial.setTexture("assets/textures/spec_beast.jpg", 2);
        testmaterial.setShader(new NormalAndSpecularMapShader(wm));
        
        meshInst.getGeometry().setNumberOfTextures(3);
        meshInst.setMaterial(testmaterial);
        meshInst.setUseGeometryMaterial(false);
        

        
    }
    

}
