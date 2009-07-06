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
package imi.demos;

import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.MaleAvatarParams;
import imi.gui.TreeExplorer;
import imi.camera.FirstPersonCamState;
import imi.character.Manipulator;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.SkeletonNode;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.shader.programs.ClothingShaderSpecColor;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.jdesktop.mtgame.WorldManager;

/**
 * Test the functionality of saving and loading characters.
 * @author Ronald E Dahlgren
 */
public class SavingAndLoadingTest extends DemoBase
{
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
        new SavingAndLoadingTest(args);
    }

    
    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        createSimpleFloor(wm);

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);
        // Create testCharacter
        Avatar testCharacter = null;
        //testCharacter = new Avatar.AvatarBuilder(new MaleAvatarParams("SavingAndLoadingTestCharacter", 0, 0, 0, 0, 0, 3, 24), wm).build();
        testCharacter = new Avatar.AvatarBuilder(new MaleAvatarParams("SavingAndLoadingTestCharacter").build(false), wm).build();
        testCharacter.selectForInput();
        control.addCharacterToTeam(testCharacter);

        // change camera speed and position
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.008f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the testCharacter to load
        while (!testCharacter.isInitialized())
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SavingAndLoadingTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Customize and save this guy
        customizeCharacter(testCharacter, wm);

        // Save the configuration
        try {
            testCharacter.saveConfiguration(SaveFile);
        } catch (JAXBException ex) {
            Logger.getLogger(SavingAndLoadingTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SavingAndLoadingTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Then create a new avatar with the same configuration
            Avatar newAvatar = new Avatar.AvatarBuilder(SaveFile.toURI().toURL(), wm).transform(new PMatrix(new Vector3f(1.0f, 0.0f, 0.0f))).build();
            control.addCharacterToTeam(newAvatar);

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
        meshMat.setDefaultShader((ClothingShaderSpecColor)repository.newShader(ClothingShaderSpecColor.class));

        // Grab the shirt and put the new material on it
        PPolygonSkinnedMeshInstance meshInstance = skeleton.getSkinnedMeshInstance("PoloShape"); // Dress shirt
        meshInstance.setMaterial(meshMat);
        meshInstance.applyMaterial();

//        Manipulator.swapNonSkinnedMesh(testCharacter, true, new File("assets/models/collada/Objects/Chairs/ConfChair1.dae"), "Base", "Head", new PMatrix(), "attachment joint test");
//        testCharacter.applyMaterials();
    }

}
