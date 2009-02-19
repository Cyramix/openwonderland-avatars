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
import imi.gui.JPanel_Animations;
import imi.gui.SceneEssentials;
import imi.scene.PMatrix;
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
import javax.swing.JFrame;



/**
 * This test demonstrates how to instantiate an maleAvatar and provide customization
 * through a CharacterAttributes object.
 * @see DemoBase For information about the freebies that class provides
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class CustomizationExample extends DemoBase
{
    /**
     * Construct a new instance. This method must be defined to subclass
     * DemoBase.
     * @param args Command-line arguments
     */
    public CustomizationExample(String[] args)
    {
        super(args);
    }

    /**
     * Run this file!
     * @param args
     */
    public static void main(String[] args)
    {
        // Give ourselves a nice environment
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Garden/Garden.dae" };
        // Construction does all the work
        CustomizationExample test = new CustomizationExample(ourArgs);
    }

    /**
     * This is the overrride point at which the framework has already been set up.
     * Entities can be created and added to the provided world manager at this point.
     * @param wm
     */
    @Override
    protected void createDemoEntities(WorldManager wm)
    {
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        // Set the input scheme that we intend to use
        AvatarControlScheme control = (AvatarControlScheme)eventProcessor.setDefault(new AvatarControlScheme(null));

        // Create an attributes object describing the maleAvatar
        // We will use random customizations for this one
        MaleAvatarAttributes maleAttributes = new MaleAvatarAttributes("RobertTheTestGuy", true);
        // Put him over to the left a bit
        maleAttributes.setOrigin(new PMatrix(new Vector3f(1, 0, 1)));
        Avatar maleAvatar = new Avatar(maleAttributes, wm);
        // Now let's make a female using a specific configuration
        FemaleAvatarAttributes femaleAttributes =
                new FemaleAvatarAttributes("LizTheTestGal",
                                                 0, // Feet
                                                 1, // Legs
                                                 1, // Torso
                                                 1, // Hair
                                                 1, // Head
                                                 3); // Skin
        // Put her over to the right a bit
        femaleAttributes.setOrigin(new PMatrix(new Vector3f(-1, 0, 1)));
//        Avatar femaleAvatar = new Avatar(femaleAttributes, wm);

        // Select the male and add them both to the input team (collection of controllable avatars)
        maleAvatar.selectForInput();
        control.getAvatarTeam().add(maleAvatar);
//        control.getAvatarTeam().add(femaleAvatar);

        // Hook the control scheme up the the camera in order to receieve input
        // events. We need this in order to control the Verlet arm ('Q' and 'E' to engage)
        control.getMouseEventsFromCamera();

        SceneEssentials scenecrap = new SceneEssentials();
        scenecrap.setSceneData(maleAvatar.getJScene(), maleAvatar.getPScene(), repository, wm, null);
        scenecrap.setAvatar(maleAvatar);
        JFrame frame = new JFrame();
        JPanel_Animations animPanel = new JPanel_Animations();
        animPanel.setPanel(scenecrap);
        animPanel.startTimer();
        animPanel.setVisible(true);
        frame.add(animPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
