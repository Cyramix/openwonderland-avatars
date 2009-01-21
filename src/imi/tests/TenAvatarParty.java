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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.scene.PMatrix;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.CircleUtil;
import imi.utils.input.AvatarControlScheme;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Provide a demonstration of the different avatar configurations
 * @author Ronald E Dahlgren
 */
public class TenAvatarParty extends DemoBase
{
    private static final Logger     logger = Logger.getLogger(TenAvatarParty.class.getName());

    private static final Integer    numberOfAvatars = Integer.valueOf(10);
    private static final String[]   configFiles     = new String[numberOfAvatars];

    static {
        String fileProtocol = "file://localhost" + System.getProperty("user.dir") + "/";
        configFiles[ 0] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 1] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 2] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 3] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 4] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 5] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 6] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 7] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
//        configFiles[ 8] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
//        configFiles[ 9] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
    }

    public TenAvatarParty(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        TenAvatarParty worldTest = new TenAvatarParty(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        CircleUtil circle = new CircleUtil(numberOfAvatars, 4);
        Vector2f[] displacements = circle.calculatePoints();

        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));

        // Create testCharacter
        Avatar testCharacter = null;
        float totalLoadTime = 0.0f;
        for (int i = 0; i < numberOfAvatars; ++i)
        {
            try {
                long startTime = System.nanoTime();
                testCharacter = new Avatar(new MaleAvatarAttributes("Name", true), wm);//new avatarAvatar(new URL(configFiles[i]), wm);
                long stopTime = System.nanoTime();
                float length = (stopTime - startTime) / 1000000000.0f;
                totalLoadTime += length;
                System.out.println("Loading avatar " + i + " took " + length + " seconds.");
                
            } catch (Exception ex) {
                Logger.getLogger(SavingAndLoadingTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (testCharacter != null)
            {
                testCharacter.selectForInput();
                PMatrix localXForm = testCharacter.getModelInst().getTransform().getLocalMatrix(true);
                localXForm.setTranslation(new Vector3f(
                                        displacements[i].x,
                                        0,
                                        displacements[i].y));
                control.getAvatarTeam().add(testCharacter);
                control.getMouseEventsFromCamera();
            }
        }
        System.out.println("Took " + totalLoadTime + " seconds overall to load the avatars.");

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
    }
}
