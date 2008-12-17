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
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.io.File;
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

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create testCharacter
        NinjaAvatar testCharacter = new NinjaAvatar(new NinjaAvatarAttributes("SavingAndLoadingTestCharacter", true, false), wm);
        testCharacter.selectForInput();
        control.getNinjaTeam().add(testCharacter);
        control.getMouseEventsFromCamera();

        // change camera speed and position
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.08f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the testCharacter to load
        while (!testCharacter.isInitialized())
        {
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
            }
        }

        // Perform some customizations
        customizeCharacter(testCharacter);

        // Save the file
        testCharacter.saveConfiguration(new File("/work/avatars/assets/configurations/SavingTestOutput.xml"));
    }

    private void customizeCharacter(NinjaAvatar testCharacter) {
        // tweak it!
        SkeletonNode skeleton = testCharacter.getSkeleton();
        skeleton.displace("Head", new Vector3f(0, 0.08f, -0.04f));
        skeleton.displace("rightEye", new Vector3f(0, 0, -0.018f));
        skeleton.displace("leftEye", new Vector3f(0, 0, -0.018f));
    }
}
