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
//import imi.character.networking.DarkstarClient;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.scene.camera.state.FirstPersonCamState;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lou Hayt
 */
public class VerletArmTest  extends DemoBase
{
    public VerletArmTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        VerletArmTest worldTest = new VerletArmTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        int numberOfAvatars = 1;
        
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Character Test Objects", wm);
        
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Make a chair and let the control the collection so it can delete it
        objects.generateChairs(Vector3f.ZERO, 5.0f, 4);
        control.setObjectCollection(objects);
        
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.02f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
        
        // Create avatar
        int feet  = -1;//(int) (Math.random() * 10000 % 0);
        int legs  = (int) (Math.random() * 10000 % 3);
        int torso = (int) (Math.random() * 10000 % 5);
        int hair  = (int) (Math.random() * 10000 % 53); // 8 is missing, test til 16
        NinjaAvatar avatar = new NinjaAvatar(new NinjaFemaleAvatarAttributes("Avatar", feet, legs, torso, hair), wm);
        //avatar.setUpdateExtension(new DarkstarClient(avatar, true, feet, legs, torso, hair));
        //((DarkstarClient)avatar.getUpdateExtension()).login();
        //NinjaAvatar avatar = new NinjaAvatar(new NinjaFemaleAvatarAttributes("Avatar", true, false), wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        // Get the mouse events to be able to control the arm 
        control.getMouseEventsFromCamera();

        // Make some more avatars
        float zStep = 5.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            cloneAvatar(control, objects, wm, 0.0f, 0.0f, zStep);
            zStep += 5.0f;
        }
    }

    private void cloneAvatar(NinjaControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset) 
    {   
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar Clone " + xOffset+yOffset+zOffset, true, false), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
    
}
