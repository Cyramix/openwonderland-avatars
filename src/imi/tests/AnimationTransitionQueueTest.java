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
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollection;
import imi.scene.animation.TransitionQueue;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.graph.Connection;
import imi.utils.graph.Connection.ConnectionDirection;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * Test the animation queue!
 * @author Ronald E Dahlgren
 */
public class AnimationTransitionQueueTest extends DemoBase
{
    public AnimationTransitionQueueTest(String[] args){
        super(args);
    }

    public static void main(String[] args) {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        AnimationTransitionQueueTest worldTest = new AnimationTransitionQueueTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Musical Chairs Objects", wm);

        // Create locations for the game
        LocationNode chairGame1 = new LocationNode("Location 1", Vector3f.ZERO, 30.0f, wm, objects);
        chairGame1.generateChairs(3);
        LocationNode chairGame2 = new LocationNode("Location 2", Vector3f.UNIT_X.mult(30.0f),  30.0f, wm, objects);
        chairGame2.generateChairs(3);
        LocationNode chairGame3 = new LocationNode("Location 3", Vector3f.UNIT_Z.mult(30.0f),  30.0f, wm, objects);
        chairGame3.generateChairs(3);

        // Create paths
        chairGame1.addConnection(new Connection("Location 3", chairGame1, chairGame2, ConnectionDirection.OneWay));
        chairGame1.addConnection(new Connection("Location 2", chairGame1, chairGame2, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("Location 3", chairGame2, chairGame3, ConnectionDirection.OneWay));
        chairGame2.addConnection(new Connection("Location 1", chairGame2, chairGame1, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("Location 1", chairGame3, chairGame2, ConnectionDirection.OneWay));
        chairGame3.addConnection(new Connection("Location 2", chairGame3, chairGame2, ConnectionDirection.OneWay));

        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));

        // Create avatar
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar", true, false), wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);

        TransitionQueue trannyQueue = new TransitionQueue();
        while (avatar.getSkeleton() == null || avatar.getSkeleton().getAnimationState() == null)
        {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AnimationTransitionQueueTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // just to make sure
        try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
                Logger.getLogger(AnimationTransitionQueueTest.class.getName()).log(Level.SEVERE, null, ex);
            }

        // play some stuff!
//        trannyQueue.setTarget(avatar.getSkeleton(), 0);
//        trannyQueue.addTransition(new TransitionCommand(1, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(2, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(3, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(4, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(5, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(6, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(7, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(8, 10.0f, PlaybackMode.Oscillate));
//        trannyQueue.addTransition(new TransitionCommand(9, 8.0f));
//        trannyQueue.addTransition(new TransitionCommand(10, 8.0f));
        // Make some avatars
        //cloneAvatars(control, objects, wm);

    }

    private void cloneAvatars(NinjaControlScheme control, ObjectCollection objects, WorldManager wm)
    {
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar Clone", true, false), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-5.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);

        avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar Clone", true, false), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-10.0f));
        control.getNinjaTeam().add(avatar);
        avatar.setObjectCollection(objects);

//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-15.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-20.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-25.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-30.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-35.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-40.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);
//
//        avatar = new NinjaAvatar("Avatar Clone", wm);
//        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(Vector3f.UNIT_Z.mult(-45.0f));
//        control.getNinjaTeam().add(avatar);
//        avatar.setObjectCollection(objects);

    }
}
