/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.tests;

import com.jme.math.Vector3f;
import imi.character.networking.DarkstarClient;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.scene.camera.behaviors.ThirdPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.camera.state.ThirdPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
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
        
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Make a chair and let the control the collection so it can delete it
        //objects.generateChairs(Vector3f.ZERO, 5.0f, 4);
        //control.setObjectCollection(objects);
        
//        // change camera speed and position it
//        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
//        camState.setMovementRate(0.03f);
//        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
        
        boolean male = true;
        NinjaAvatar avatar;
        int feet, legs, torso, hair;
        
        if (male)
        {
            // Create male avatar
            feet  = 1;//(int) (Math.random() * 10000 % 4);
            legs  = 4;//(int) (Math.random() * 10000 % 4);
            torso = 4;//(int) (Math.random() * 10000 % 6);
            hair  = -1;//(int) (Math.random() * 10000 % 17);
            avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);
        }
        else // female
        {
            // Create female avatar
            feet  = -1;//(int) (Math.random() * 10000 % 0);
            legs  = 0;//(int) (Math.random() * 10000 % 3);  // 1 and 2 problems
            torso = (int) (Math.random() * 10000 % 3);  // % 5.... 3 and 4 problems
            hair  = (int) (Math.random() * 10000 % 53); // 8 is missing, tested til 16
            avatar = new NinjaAvatar(new NinjaFemaleAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);
        }
        
        // Create darkstar client and login
        avatar.setUpdateExtension(new DarkstarClient(avatar, male, feet, legs, torso, hair));
        ((DarkstarClient)avatar.getUpdateExtension()).login();
        
        avatar.setBigHeadMode(2.0f);
        avatar.makeFist(false, true);
        //avatar.makeFist(false, false);
        
        // Select the avatar for input and set the object collection
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
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
