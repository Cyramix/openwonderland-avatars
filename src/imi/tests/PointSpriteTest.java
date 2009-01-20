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


import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.environments.ColladaEnvironment;
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
    //private static final Logger logger = Logger.getLogger(COLLADA_CharacterTest.class.getName());

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
    protected void createDemoEntities(WorldManager wm)
    {
        ColladaEnvironment world = (ColladaEnvironment)wm.getUserData(ColladaEnvironment.class);
        particles = new ParticleCollection(200, wm, world.getJMENode());
//        world.getJMENode().attachChild(particles.getJMENode());
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        long startTime = System.nanoTime();
        NinjaAvatarAttributes attribs = new NinjaAvatarAttributes("WeirdGuy", 2, 3, 5, 10, 1);
//        NinjaFemaleAvatarAttributes attribs = new NinjaFemaleAvatarAttributes("WeirdChick", 0, 1, 1, 1, 1);
        NinjaAvatar avatar = new NinjaAvatar(attribs, wm);
        particles.setTargetModel(avatar.getModelInst());
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
//        ThirdPersonCamState state = (ThirdPersonCamState)wm.getUserData(CameraState.class);
//        state.setTargetModelInstance(avatar.getModelInst());

//        ThirdPersonCamModel camModel = (ThirdPersonCamModel)m_cameraProcessor.getModel();
//        camModel.setActiveState(state);
//        avatar.getController().addCharacterMotionListener(camModel);
    }
}
