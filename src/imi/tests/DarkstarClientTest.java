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
import imi.networking.CharacterClientExtension;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.networking.Client;
import imi.networking.Client.ClientAvatar;
import imi.character.objects.ObjectCollection;
import imi.scene.camera.behaviors.ThirdPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.ThirdPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DemoAvatarControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class DarkstarClientTest extends DemoBase
{

    public DarkstarClientTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        DarkstarClientTest worldTest = new DarkstarClientTest(args);
    }

    @Override
    protected void assignCameraType(WorldManager wm)
    {
        ThirdPersonCamState state = new ThirdPersonCamState(null);
        state.setOffsetFromCharacter(new Vector3f(0, 1.8f, 0));
        state.setCameraPosition(new Vector3f(0, 3.5f, -7));
        state.setTargetFocalPoint(new Vector3f(0,1.8f,0));
        state.setToCamera(new Vector3f(0, 2.5f, -4));
        ThirdPersonCamModel model = new ThirdPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Character Test Objects", wm);
        
        // Create avatar input scheme
        DemoAvatarControlScheme control = (DemoAvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new DemoAvatarControlScheme(null));
        
        // Make a chair and let the control the collection so it can delete it
        //objects.generateChairs(Vector3f.ZERO, 5.0f, 4);
        //control.setObjectCollection(objects);
        
//        // change camera speed and position it
//        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
//        camState.setMovementRate(0.03f);
//        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
        
        boolean male = true; if (Math.random() < 0.25) male = false;
        Avatar avatar;
        int feet, legs, torso, hair, head, skinTone, eyeColor;

        if (male)
        {
            // Create male avatar
            feet      = (int) (Math.random() * 10000 % 4);
            legs      = (int) (Math.random() * 10000 % 4);
            torso     = (int) (Math.random() * 10000 % 6);
            hair      = (int) (Math.random() * 10000 % 17);
            head      = 0;//(int) (Math.random() * 10000 % 4);
            skinTone  = (int) (Math.random() * 10000 % 12);
            eyeColor  = (int) (Math.random() * 10000 % 25);
            avatar = new Avatar(new MaleAvatarAttributes("Avatar", feet, legs, torso, hair, head, skinTone, eyeColor, false), wm);
        }
        else // female
        {
            // Create female avatar
            feet      = (int) (Math.random() * 10000 % 3);
            legs      = (int) (Math.random() * 10000 % 3);
            torso     = (int) (Math.random() * 10000 % 4); 
            hair      = (int) (Math.random() * 10000 % 49);
            head      = 0;//(int) (Math.random() * 10000 % 4);
            skinTone  = (int) (Math.random() * 10000 % 12);
            eyeColor  = (int) (Math.random() * 10000 % 25); 
            avatar = new Avatar(new FemaleAvatarAttributes("Avatar", feet, legs, torso, hair, head, skinTone, eyeColor, false), wm);
        }
        
        // Create darkstar client and login
        avatar.setUpdateExtension(new Client(avatar, male, feet, legs, torso, hair, head, skinTone, eyeColor));
        ((Client)avatar.getUpdateExtension()).login();
        
        //avatar.setBigHeadMode(2.0f);
        
        float x = 2.0f + (float)Math.random() % 5.0f; if (Math.random() < 0.5) x*= -1.0f;
        float z = 2.0f + (float)Math.random() % 5.0f; if (Math.random() < 0.5) z*= -1.0f;
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(x, 0.0f, z));
        
        // Select the avatar for input and set the object collection
        avatar.selectForInput();
        control.getAvatarTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        // Get the mouse events to be able to control the arm 
        control.getMouseEventsFromCamera();
        
        // Hook the camera up to the avatar
        ThirdPersonCamState state = (ThirdPersonCamState)wm.getUserData(CameraState.class);
        state.setTargetModelInstance(avatar.getModelInst());

        ThirdPersonCamModel camModel = (ThirdPersonCamModel)m_cameraProcessor.getModel();
        camModel.setActiveState(state);
        avatar.getController().addCharacterMotionListener(camModel);
    }

}
