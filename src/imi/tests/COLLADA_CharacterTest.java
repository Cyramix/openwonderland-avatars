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
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.scene.camera.state.FirstPersonCamState;
import java.net.MalformedURLException;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * 
 * 
 *  VerletArm has its own test now imi.tests.VerletArmTest
 * 
 *  testing loading heads with different bind pose settings
 * 
 * 
 * @author Lou Hayt
 */
public class COLLADA_CharacterTest extends DemoBase
{
    public COLLADA_CharacterTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        COLLADA_CharacterTest worldTest = new COLLADA_CharacterTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        long startTime = System.nanoTime();
        NinjaAvatar avatar = new NinjaAvatar(new NinjaFemaleAvatarAttributes("Avatar", true, false), wm);
        float time = (float)((System.nanoTime() - startTime) / 1000000000.0f);
        System.out.println("Constructing the male took: " + time);
        
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
 
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
    }
}
