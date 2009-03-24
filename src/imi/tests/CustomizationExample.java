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
import imi.gui.JFrame_InstrumentationGUI;
import imi.gui.JPanel_Animations;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.scene.PMatrix;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationState;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JFrame;



/**
 * This test demonstrates how to instantiate an avatar and provide customization
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
        String[] ourArgs = new String[] { "-env:assets/models/collada/Environments/Arena/Arena.dae" };
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

        // Slow the camera down
        FirstPersonCamState camState = (FirstPersonCamState) wm.getUserData(CameraState.class);
        camState.setMovementRate(0.01f);
        // Create an attributes object describing the avatar
        // We will use random customizations for this one
        MaleAvatarAttributes maleAttributes = new MaleAvatarAttributes("RobertTheTestGuy",
                                                                        0, // Feet
                                                                        0, // Legs
                                                                        2, // Torso
                                                                        -1, // Hair = bald
                                                                        6, // Head
                                                                        12, // Skin
                                                                        0); // Eye color
        maleAttributes.setUsePhongLighting(true);
        // Put him over to the left a bit
        maleAttributes.setOrigin(new PMatrix(new Vector3f(0,3.14f,0), Vector3f.UNIT_XYZ, Vector3f.ZERO));
        Avatar maleAvatar = new Avatar(maleAttributes, wm);
        // Now let's make a female using a specific configuration
        FemaleAvatarAttributes femaleAttributes =
                new FemaleAvatarAttributes("LizTheTestGal",
                                                 0, // Feet
                                                 2, // Legs
                                                 2, // Torso
                                                 -1, // Hair = bald
                                                 8, // Head
                                                 12, // Skin
                                                 25); // Eye color
        femaleAttributes.setUsePhongLighting(true);
//        // Put her over to the right a bit
        femaleAttributes.setOrigin(new PMatrix(new Vector3f(-1, 0, 1)));
        Avatar femaleAvatar = new Avatar(femaleAttributes, wm);

        // Select the male and add them both to the input team (collection of controllable avatars)
        maleAvatar.selectForInput();
        control.getAvatarTeam().add(maleAvatar);
        control.getAvatarTeam().add(femaleAvatar);
        femaleAvatar.selectForInput();

        // Hook the control scheme up the the camera in order to receieve input
        // events. We need this in order to control the Verlet arm ('Q' and 'E' to engage)
        control.getMouseEventsFromCamera();

        SceneEssentials scene = new SceneEssentials();
        scene.setAvatar(femaleAvatar);
        scene.setSceneData(femaleAvatar.getJScene(), femaleAvatar.getPScene(), repository, wm, null);

        JPanel_Animations anim = new JPanel_Animations();
        anim.setPanel(scene);
        JFrame newFrame = new JFrame();
        newFrame.add(anim);
        newFrame.pack();
        newFrame.setVisible(true);

        TreeExplorer te = new TreeExplorer();
        te.setExplorer(scene);
        te.setVisible(true);
    }

    private void attachSkinnedHair(Avatar target, URL geometryLocation)
    {
        // load the skinned mesh
        InstructionProcessor processor = new InstructionProcessor(worldManager);
        Instruction rootInstruction = new Instruction();
        rootInstruction.addInstruction(new Instruction(Instruction.InstructionType.setSkeleton, target.getSkeleton()));
        rootInstruction.addLoadGeometryToSubgroupInstruction(geometryLocation, "Head");
        processor.execute(rootInstruction);

        target.setDefaultShaders();
        target.applyMaterials();
    }

    private void waitUntilAvatarIsInitialized(Avatar avatar)
    {
        try {
            while (avatar.isInitialized() == false)
                Thread.sleep(300);
        }
        catch (InterruptedException ex)
        {
            System.out.println("Interrupted whilst sleeping!");
        }
    }

    private void useAvatarAnimationSystem(Avatar avatar)
    {
        avatar.initiateFacialAnimation(0, 0.2f, 2.0f); // Smile
        avatar.initiateFacialAnimation(1, 0.2f, 2.0f); // Frown
        avatar.initiateFacialAnimation(2, 0.2f, 2.0f); // Anger
        avatar.initiateFacialAnimation(0, 0.2f, 2.0f); // Smile
        avatar.initiateFacialAnimation(1, 0.2f, 2.0f); // Frown
        avatar.initiateFacialAnimation(2, 0.2f, 2.0f); // Anger
    }
    private void playFacialAnimationChain(Avatar avatar) throws InterruptedException
    {
        float smileLength = avatar.getSkeleton().getAnimationGroup(1).getCycle(0).getDuration();
        float frownLength = avatar.getSkeleton().getAnimationGroup(1).getCycle(1).getDuration();
        float scornLength = avatar.getSkeleton().getAnimationGroup(1).getCycle(2).getDuration();
        // Grab a ref to the facial animation state
        AnimationState avatarAnimState = avatar.getSkeleton().getAnimationState(1);

        // Set up some constants
        avatarAnimState.setTransitionDuration(0.2f); // Quick transition
        avatarAnimState.setAnimationSpeed(0.12f); // Slow speed

        // Proceed to enact a chain of facial animations
        System.out.println("Playing Animation One, smile.");
        avatarAnimState.setTransitionCycle(0);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(0); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(false);
        Thread.sleep(4000);

        System.out.println("Playing Animation Two, smile Reverse.");
        avatarAnimState.setTransitionCycle(0);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(smileLength); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(true);
        Thread.sleep(4000);

        System.out.println("Playing Animation Three, angry.");
        avatarAnimState.setTransitionCycle(2);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(0); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(false);
        Thread.sleep(4000);

        System.out.println("Playing Animation Four, angry reverse.");
        avatarAnimState.setTransitionCycle(2);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(scornLength); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(true);
        Thread.sleep(4000);

        System.out.println("Playing Animation Five, frown.");
        avatarAnimState.setTransitionCycle(1);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(0); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(false);
        Thread.sleep(4000);

        System.out.println("Playing Animation Six, frown reverse.");
        avatarAnimState.setTransitionCycle(1);
        avatarAnimState.setTimeInTransition(0);
        avatarAnimState.setCycleMode(PlaybackMode.PlayOnce);
        avatarAnimState.setTransitionCycleTime(frownLength); // This will need to be different for reverse
        avatarAnimState.setTransitionReverseAnimation(true);
        Thread.sleep(7000);
    }
}
