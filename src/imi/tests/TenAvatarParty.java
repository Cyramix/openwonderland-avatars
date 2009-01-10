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
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.scene.PMatrix;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.CircleUtil;
import imi.utils.input.NinjaControlScheme;
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
        configFiles[ 8] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
        configFiles[ 9] = fileProtocol + "assets/configurations/SavingTestOutput.xml";
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
        CircleUtil circle = new CircleUtil(numberOfAvatars, 6);
        Vector2f[] displacements = circle.calculatePoints();

        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create testCharacter
        NinjaAvatar testCharacter = null;

        for (int i = 0; i < numberOfAvatars; ++i)
        {
            try {
                testCharacter = new NinjaAvatar(new NinjaAvatarAttributes("Name", true, true), wm);//new NinjaAvatar(new URL(configFiles[i]), wm);
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
                control.getNinjaTeam().add(testCharacter);
                control.getMouseEventsFromCamera();
            }
        }

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
    }
}
