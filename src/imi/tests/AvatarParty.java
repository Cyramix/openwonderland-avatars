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

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.scene.PMatrix;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.CircleUtil;
import imi.utils.input.AvatarControlScheme;
import java.util.logging.Level;
import org.jdesktop.mtgame.WorldManager;

/**
 * Provide a demonstration of the different avatar configurations
 * @author Ronald E Dahlgren
 */
public class AvatarParty extends DemoBase
{
    /** Number of avatars to load **/
    private static final Integer    numberOfAvatars = Integer.valueOf(10);

    public AvatarParty(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        // Give ourselves a nice environment
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Garden/Garden.dae" };
        // Construction does all the work
        AvatarParty worldTest = new AvatarParty(ourArgs);
    }

    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Use CircleUtil to generate a list of points for our avatars to stand
        CircleUtil circle = new CircleUtil(numberOfAvatars, 4);
        Vector2f[] displacements = circle.calculatePoints(); // grab that list

        // Create avatar input scheme and an object collection to use
        AvatarControlScheme control = (AvatarControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new AvatarControlScheme(null));
        ObjectCollection ourCollection = new ObjectCollection("AvatarCollection", wm);
        control.setObjectCollection(ourCollection);


        // Create test characters
        Avatar testCharacter = null;
        float totalLoadTime = 0.0f; // Gather some metrics
        for (int i = 0; i < numberOfAvatars; ++i)
        {
            try {
                long startTime = System.nanoTime(); // time it

                if (Math.random() < 0.5)
                    testCharacter = new Avatar(new MaleAvatarAttributes("Name", true), wm);//new avatarAvatar(new URL(configFiles[i]), wm);
                else
                    testCharacter = new Avatar(new FemaleAvatarAttributes("Name", true), wm);//new avatarAvatar(new URL(configFiles[i]), wm);
                // Calculate and dump some metrics
                long stopTime = System.nanoTime();
                float length = (stopTime - startTime) / 1000000000.0f;
                totalLoadTime += length;
                System.out.println("Loading avatar " + i + " took " + length + " seconds."); // Info!

            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            // If we successfully created this avatar, do some addition configuration
            if (testCharacter != null)
            {
                testCharacter.selectForInput();
                ourCollection.addObject(testCharacter); // Add to the object collection
                // Move to the proper location
                PMatrix localXForm = testCharacter.getModelInst().getTransform().getLocalMatrix(true);
                localXForm.setTranslation(new Vector3f(
                                        displacements[i].x,
                                        0,
                                        displacements[i].y));
                control.getAvatarTeam().add(testCharacter);
                control.getMouseEventsFromCamera(); // Necessary for driving the Verlet arms
            }
        }
        
        System.out.println("Took " + totalLoadTime + " seconds overall to load the avatars."); // Info!
    }
}
