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
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.programs.ClothingShader;
import imi.utils.input.NinjaControlScheme;
import java.io.File;
import java.net.MalformedURLException;
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
        NinjaAvatar testCharacter = new NinjaAvatar(new NinjaAvatarAttributes("SavingAndLoadingTestCharacter", false, false), wm);
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
            Thread.sleep(22000);
        } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
        }


        // Uncomment to create a save file
//        customizeCharacter(testCharacter, wm);
//        testCharacter.saveConfiguration(new File("/work/avatars/assets/configurations/SavingTestOutput.xml"));


        // Uncomment to load a save file
        Thread.yield();
        try {
            testCharacter.loadConfiguration(new URL("file://localhost/work/avatars/assets/configurations/SavingTestOutput.xml"));

        } catch (MalformedURLException ex) {
            Logger.getLogger(SavingAndLoadingTest.class.getName()).log(Level.SEVERE, null, ex);
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
        skeleton.displace("Head", new Vector3f(0, 0.08f, -0.04f));
        skeleton.displace("rightEye", new Vector3f(0, 0, -0.018f));
        skeleton.displace("leftEye", new Vector3f(0, 0, -0.018f));

        PMeshMaterial meshMat = new PMeshMaterial("HeadGeoShape");
        meshMat.setTexture(new File("/work/avatars/assets/textures/default.png"), 0); // base diffuse
        meshMat.setTexture(new File("/work/avatars/assets/textures/tgatest.tga"), 1); // pattern diffuse
        meshMat.setTexture(new File("/work/avatars/assets/textures/normalmap2.jpg"), 2); // normal map
        meshMat.setShader(new ClothingShader(wm));

//        Thread.yield();
//        try // last little bit
//        {
//            Thread.sleep(22000);
//        } catch (InterruptedException ex) {
//                logger.log(Level.SEVERE, null, ex);
//        }
//        PPolygonSkinnedMeshInstance meshInstance = skeleton.getSkinnedMeshInstance("HeadGeoShape"); // Dress shirt
//        meshInstance.setMaterial(meshMat);
//        meshInstance.setUseGeometryMaterial(false);

    }
}
