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
package imi.demos;

import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.FemaleAvatarParams;
import imi.character.MaleAvatarParams;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.objects.LocationNode;
import imi.objects.AvatarObjectCollection;
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
        new MusicalChairs(ourArgs);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);
        int numberOfAvatars = 2;
        float block = 2.0f * numberOfAvatars;
        float halfBlock = 0.5f * numberOfAvatars;
        int numChairs = numberOfAvatars-1;

        // Create one object collection for all to use (for testing)
        AvatarObjectCollection objects = new AvatarObjectCollection("Musical Chairs Objects", wm);
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
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);
        control.setCommandEntireTeam(true);
        control.setObjectCollection(objects);
//if (true)
//    return;
        // Create avatar
        Avatar avatar = new Avatar.AvatarBuilder(new MaleAvatarParams("Avatar").build(), wm).build();
        avatar.selectForInput();
        control.addCharacterToTeam(avatar);
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

    private void createAvatar(CharacterControls control, AvatarObjectCollection objects, WorldManager wm, float xOffset, float yOffset, float zOffset)
    {
        boolean male = true; if (Math.random() < 0.25) male = false;
        Avatar avatar;
        if (male)
            avatar = new Avatar.AvatarBuilder(new MaleAvatarParams("Avatar").build(), wm).build();
        else // female
            avatar = new Avatar.AvatarBuilder(new FemaleAvatarParams("Avatar").build(), wm).build();

        avatar.getModelInst().getTransform().getLocalMatrix(true).setTranslation(new Vector3f(xOffset, yOffset, zOffset));
        control.addCharacterToTeam(avatar);
        avatar.setObjectCollection(objects);
    }
}
