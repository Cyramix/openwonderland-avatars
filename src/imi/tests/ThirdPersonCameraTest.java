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
import imi.character.Character;
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.camera.behaviors.ThirdPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.camera.state.ThirdPersonCamState;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.processors.FlexibleCameraProcessor;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;



/**
 * Developing a third person camera.
 * @author Ronald E Dahlgren
 */
public class ThirdPersonCameraTest extends DemoBase
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CustomizationExample.class.getName());

    public ThirdPersonCameraTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        ThirdPersonCameraTest worldTest = new ThirdPersonCameraTest(args);
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
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        long startTime = System.nanoTime();
        NinjaAvatarAttributes attribs = new NinjaAvatarAttributes("WeirdGuy", 2, 3, 5, 10, 1);
//        NinjaFemaleAvatarAttributes attribs = new NinjaFemaleAvatarAttributes("WeirdChick", 0, 1, 1, 1, 1);
        NinjaAvatar avatar = new NinjaAvatar(attribs, wm);
        float time = (float)((System.nanoTime() - startTime) / 1000000000.0f);
        System.out.println("Constructing the male took: " + time);

        avatar.selectForInput();
        avatar.setBigHeadMode(2.0f);
        avatar.setBeerBelly(1.17f);
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();

        // Hook the camera up to the avatar
        ThirdPersonCamState state = (ThirdPersonCamState)wm.getUserData(CameraState.class);
        state.setTargetModelInstance(avatar.getModelInst());

        ThirdPersonCamModel camModel = (ThirdPersonCamModel)m_cameraProcessor.getModel();
        camModel.setActiveState(state);
        avatar.getController().addCharacterMotionListener(camModel);
    }

    
}
