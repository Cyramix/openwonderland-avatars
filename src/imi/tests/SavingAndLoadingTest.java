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
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.programs.ClothingShaderSpecColor;
import imi.utils.input.AvatarControlScheme;
import java.io.File;
import java.net.MalformedURLException;
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
    /** Tree explorer if needed **/
    private TreeExplorer te = null;
    /** Point this to where you want the file to go **/
    private static File SaveFile = new File("assets/configurations/SavingTest.xml");

    /**
     * Construct a new instance!
     * @param args
     */
    public SavingAndLoadingTest(String[] args)
    {
        super(args);
    }

    /**
     * Run the test!
     * @param args
     */
    public static void main(String[] args)
    {
        SavingAndLoadingTest worldTest = new SavingAndLoadingTest(args);
    }

    
    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));
        // Create testCharacter
        Avatar testCharacter = null;
        testCharacter = new Avatar(new MaleAvatarAttributes("SavingAndLoadingTestCharacter", 0, 0, 0, 0, 0, 3, 24), wm);
        testCharacter.selectForInput();
        control.getAvatarTeam().add(testCharacter);
        control.getMouseEventsFromCamera();

        // change camera speed and position
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.008f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the testCharacter to load
        while (!testCharacter.isInitialized())
            Thread.yield();


        // Customize and save this guy
        customizeCharacter(testCharacter, wm);
        testCharacter.saveConfiguration(SaveFile);
        
        try {
            // Then create a new avatar with the same configuration
            Avatar newAvatar = new Avatar(SaveFile.toURI().toURL(), wm);
            newAvatar.applyMaterials();

        } catch (MalformedURLException ex) {
            logger.severe("Unable to create URL to configuration file.");
        }
    }

    private void customizeCharacter(Avatar testCharacter, WorldManager wm) {
        // tweak it!
        SkeletonNode skeleton = testCharacter.getSkeleton();
        skeleton.displaceJoint("Head", new Vector3f(0, 0.08f, -0.04f));
        skeleton.displaceJoint("rightEye", new Vector3f(0, 0, -0.018f));
        skeleton.displaceJoint("leftEye", new Vector3f(0, 0, -0.018f));

        // Clothing material mods
        PMeshMaterial meshMat = new PMeshMaterial("Clothing");
        meshMat.setTexture(new File("assets/textures/checkerboard2.PNG"), 0); // base diffuse
        meshMat.setTexture(new File("assets/textures/normal.jpg"), 1); // normal map
        meshMat.setTexture(new File("assets/textures/tgatest.tga"), 2); // pattern diffuse
        meshMat.setShader((ClothingShaderSpecColor)repository.newShader(ClothingShaderSpecColor.class));

        // Grab the shirt and put the new material on it
        PPolygonSkinnedMeshInstance meshInstance = skeleton.getSkinnedMeshInstance("PoloShape"); // Dress shirt
        meshInstance.setMaterial(meshMat);
        meshInstance.applyMaterial();
    }
}
