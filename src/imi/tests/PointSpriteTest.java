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
import imi.scene.camera.behaviors.ThirdPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.ThirdPersonCamState;
import imi.scene.particles.ParticleCollection;
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Testing the point sprites!
 * @author Ronald E Dahlgren
 */
public class PointSpriteTest extends DemoBase
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(COLLADA_CharacterTest.class.getName());

    private ParticleCollection particles = null;

    public PointSpriteTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        PointSpriteTest worldTest = new PointSpriteTest(args);
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
        particles = new ParticleCollection(200, wm);
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
        avatar.makeFist(false, true);
        avatar.makeFist(false, false);
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
