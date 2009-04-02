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
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.LocationNode;
import imi.character.objects.ObjectCollection;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.graph.Connection;
import imi.utils.input.AvatarControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class demonstrates some rudimentary avatar behaviors.
 * After loading, press '1' to have the avatars begin walking around the chairs.
 * Pressing '2' will cause the avatars to switch directions.
 * Pressing '3' stops the music :)
 * Pressing 'Backspace' will cause a random chair to be removed.
 * @author Lou
 */
public class MusicalChairs extends DemoBase
{
    /**
     * Construct and run the test.
     * @param args
     */
    public MusicalChairs(String[] args){
        super(args);
    }

    /**
     * Run the test!
     * @param args
     */
    public static void main(String[] args) {
        // Give ourselves a nice environment
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Garden/Garden.dae" };
        MusicalChairs worldTest = new MusicalChairs(ourArgs);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        int numberOfAvatars = 2;
        float block = 2.0f * numberOfAvatars;
        float halfBlock = 0.5f * numberOfAvatars;
        int numChairs = numberOfAvatars-1;

        // Create one object collection for all to use (for testing)
        ObjectCollection objects = new ObjectCollection("Musical Chairs Objects", wm);
        objects.generateChairs(new Vector3f(halfBlock, 0.0f, halfBlock), halfBlock, numChairs);

        // Create locations for the game
        LocationNode chairGame1 = new LocationNode("Location 1", Vector3f.ZERO, halfBlock, objects);
        LocationNode chairGame2 = new LocationNode("Location 2", Vector3f.UNIT_X.mult(block),  halfBlock, objects);
        LocationNode chairGame3 = new LocationNode("Location 3", new Vector3f(block, 0.0f, block),  halfBlock, objects);
        LocationNode chairGame4 = new LocationNode("Location 4", Vector3f.UNIT_Z.mult(block),  halfBlock, objects);

        // Create graph paths
//        objects.createConnection(chairGame1, chairGame2);
//        objects.createConnection(chairGame2, chairGame3);
//        objects.createConnection(chairGame3, chairGame4);
//        objects.createConnection(chairGame4, chairGame1);

        // Create baked paths
        chairGame1.addBakedConnection("yellowRoom", chairGame2);
        chairGame2.addBakedConnection("yellowRoom", chairGame3);
        chairGame3.addBakedConnection("yellowRoom", chairGame4);
        chairGame4.addBakedConnection("yellowRoom", chairGame1);

        chairGame1.addBakedConnection("lobbyCenter", chairGame4);
        chairGame4.addBakedConnection("lobbyCenter", chairGame3);
        chairGame3.addBakedConnection("lobbyCenter", chairGame2);
        chairGame2.addBakedConnection("lobbyCenter", chairGame1);

        // Create avatar input scheme
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));
        control.setCommandEntireTeam(true);
        control.setObjectCollection(objects);

        // Create avatar
        Avatar avatar = new Avatar(new MaleAvatarAttributes("Avatar", true), wm);
        avatar.selectForInput();
        control.getAvatarTeam().add(avatar);
        avatar.setObjectCollection(objects);

        // Make some more avatars
        float zStep = 5.0f;
        for (int i = 1; i < numberOfAvatars; i++)
        {
            createAvatar(control, objects, wm, 0.0f, 0.0f, zStep);
            zStep += 5.0f;
        }

//        objects.boundingVolumeTest();
    }

    private void createAvatar(AvatarControlScheme control, ObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset)
    {
        boolean male = true; if (Math.random() < 0.25) male = false;
        Avatar avatar;
        if (male)
            avatar = new Avatar(new MaleAvatarAttributes("Avatar", true), wm);
        else // female
            avatar = new Avatar(new FemaleAvatarAttributes("Avatar", true), wm);

        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.getAvatarTeam().add(avatar);
        avatar.setObjectCollection(objects);
    }
}
