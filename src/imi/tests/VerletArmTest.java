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
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.scene.camera.state.FirstPersonCamState;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
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
        
        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));
        
        // Make a chair and let the control the collection so it can delete it
        objects.generateChairs(Vector3f.ZERO, 5.0f, 4);
        control.setObjectCollection(objects);
        
        // change camera speed and position it
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));
        
        boolean male = false;
        Avatar avatar;
        int feet, legs, torso, hair;
        
        if (male)
        {
            // Create male avatar
            feet  = (int) (Math.random() * 10000 % 4);
            legs  = (int) (Math.random() * 10000 % 4);
            torso = (int) (Math.random() * 10000 % 6);
            hair  = (int) (Math.random() * 10000 % 17);
            avatar = new Avatar(new MaleAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);
        }
        else // female
        {
            // Create female avatar
            feet  = (int) (Math.random() * 10000 % 2);
            legs  = (int) (Math.random() * 10000 % 3);
            torso = (int) (Math.random() * 10000 % 3);  // % 5.... 3 and 4 problems
            hair  = (int) (Math.random() * 10000 % 53); // tested til 15
            avatar = new Avatar(new FemaleAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);
        }
        
        //new Avatar(new MaleAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);//new Avatar(new MaleAvatarAttributes("Avatar", feet, legs, torso, hair, 0), wm);
        //avatar.setBigHeadMode(2.0f);
                
        // Select the avatar for input and set the object collection
        avatar.selectForInput();
        control.getAvatarTeam().add(avatar);
        avatar.setObjectCollection(objects);
        
        // Get the mouse events to be able to control the arm 
        control.getMouseEventsFromCamera();

//        // Construct a tree explorer for analyzing the scene graph
//        TreeExplorer te = new TreeExplorer();
//        SceneEssentials se = new SceneEssentials();
//        se.setSceneData(avatar.getJScene(), avatar.getPScene(), avatar, wm, null);
//        te.setExplorer(se);
//        te.setVisible(true);
        
        // Make some more avatars
        float zStep = 5.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            cloneAvatar(control, objects, wm, 0.0f, 0.0f, zStep);
            zStep += 5.0f;
        }
    }

    private void cloneAvatar(AvatarControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset) 
    {   
        Avatar avatar = new Avatar(new MaleAvatarAttributes("Avatar Clone " + xOffset+yOffset+zOffset, true), wm);
        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getAvatarTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
    
}
