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
package imi.tests;

import com.jme.math.Vector3f;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.gui.TreeExplorer;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.programs.ClothingShader;
import imi.utils.input.NinjaControlScheme;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Test the functionality of saving and loading characters.
 * @author Ronald E Dahlgren
 */
public class SavingAndLoadingTest extends DemoBase
{
    /** Logger reference **/
    private static final Logger logger = Logger.getLogger(SavingAndLoadingTest.class.getName());

    public SavingAndLoadingTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        SavingAndLoadingTest worldTest = new SavingAndLoadingTest(args);
    }

    private TreeExplorer te = null;
    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create testCharacter
        NinjaAvatar testCharacter = new NinjaAvatar(new NinjaAvatarAttributes("SavingAndLoadingTestCharacter", true, true), wm);
        testCharacter.selectForInput();
        control.getNinjaTeam().add(testCharacter);
        control.getMouseEventsFromCamera();

        // change camera speed and position
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.008f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the testCharacter to load
        while (!testCharacter.isInitialized())
        {
            try
            {
                Thread.sleep(12000);
            } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
            }
        }

        try // last little bit
        {
            Thread.sleep(28000);
        } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
        }


        // Uncomment to create a save file
        customizeCharacter(testCharacter, wm);
//        testCharacter.saveConfiguration(new File("/work/IMI/sunSVN/assets/configurations/SavingAndLoadingOutput.xml"));


        // Uncomment to load a save file
        Thread.yield();
        try {
            testCharacter.loadConfiguration(new URL("file://localhost/work/IMI/sunSVN/assets/configurations/SavingAndLoadingOutput.xml"));
        }
        catch (Exception ex)
        {
            logger.severe("Exception  : " + ex.getMessage());
            ex.printStackTrace();
        }
        // Uncomment to get a pscene explorer------
//        te = new TreeExplorer();
//        SceneEssentials se = new SceneEssentials();
//        se.setPScene(testCharacter.getPScene());
//        se.setWM(wm);
//        te.setExplorer(se);
//        te.setVisible(true);
    }

    private void customizeCharacter(NinjaAvatar testCharacter, WorldManager wm) {
        // tweak it!
        SkeletonNode skeleton = testCharacter.getSkeleton();
        skeleton.displaceJoint("Head", new Vector3f(0, 0.08f, -0.04f));
        skeleton.displaceJoint("rightEye", new Vector3f(0, 0, -0.018f));
        skeleton.displaceJoint("leftEye", new Vector3f(0, 0, -0.018f));

        // Clothing
        PMeshMaterial meshMat = new PMeshMaterial("Clothing");
        meshMat.setTexture(new File("/work/avatars/assets/textures/checkerboard2.PNG"), 0); // base diffuse
        meshMat.setTexture(new File("/work/avatars/assets/textures/normal.jpg"), 1); // normal map
        //meshMat.setTexture(new File("/work/avatars/assets/textures/tgatest.tga"), 2); // pattern diffuse
        meshMat.setShader(new ClothingShader(wm));

        // eyeballs
//        PMeshMaterial meshMat = new PMeshMaterial("eyeballs!");
//        meshMat.setTexture(new File("/work/avatars/assets/models/collada/Avatars/Male/Blue_Eye.png"), 0); // base diffuse
//        meshMat.setShader(new EyeballShader(wm));

        Thread.yield();
        try // last little bit
        {
            Thread.sleep(8000);
        } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
        }
        PPolygonSkinnedMeshInstance meshInstance = skeleton.getSkinnedMeshInstance("DressShirtShape"); // Dress shirt
//        PPolygonSkinnedMeshInstance meshInstance = skeleton.getSkinnedMeshInstance("rightEyeGeoShape"); // Eyes
        meshInstance.setMaterial(meshMat);
        meshInstance.setUseGeometryMaterial(false);

//        meshInstance = skeleton.getSkinnedMeshInstance("leftEyeGeoShape"); // Eyes
//        meshInstance.setMaterial(meshMat);
//        meshInstance.setUseGeometryMaterial(false);

    }
}
