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
import com.jme.renderer.ColorRGBA;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.character.objects.AvatarObjectCollection;
import imi.character.objects.LocationNode;
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
import org.jdesktop.mtgame.WorldManager;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DemoAvatarControlScheme;
import java.io.File;
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
        String[] ourArgs = new String[] {  };
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
        DemoAvatarControlScheme control = (DemoAvatarControlScheme)eventProcessor.setDefault(new DemoAvatarControlScheme(null));

        // Slow the camera down
        FirstPersonCamState camState = (FirstPersonCamState) wm.getUserData(CameraState.class);
        camState.setMovementRate(0.03f);
        try
        {
            loadConfigFiles(control, setUpNavigationNodes(control));
        }
        catch (MalformedURLException ex)
        {

        }
        // Hook the control scheme up the the camera in order to receieve input
        // events. We need this in order to control the Verlet arm ('Q' and 'E' to engage)
        control.getMouseEventsFromCamera();
    }

    private void loadConfigFiles(DemoAvatarControlScheme controller, AvatarObjectCollection collection) throws MalformedURLException
    {
        String base = "file:///" + System.getProperty("user.dir") + File.separatorChar;
        URL[] configFiles = new URL[] {
//            new URL(base + "assets/configurations/FemaleD_CA_00.xml"),
//            new URL(base + "assets/configurations/FemaleD_AZ_00.xml"),
//            new URL(base + "assets/configurations/FemaleFG_CA_00.xml"),
//            new URL(base + "assets/configurations/FemaleFG_AA_01.xml"),
//            new URL(base + "assets/configurations/MaleFG_AA_00_white.xml"),
//            new URL(base + "assets/configurations/MaleFG_AA_01_white.xml"),
//            new URL(base + "assets/configurations/MaleFG_CA_00_white.xml"),
//            new URL(base + "assets/configurations/MaleFG_CA_01_white.xml"),
//            new URL(base + "assets/configurations/MaleFG_CA_02_white.xml"),
//            new URL(base + "assets/configurations/MaleFG_CA_03.xml"),
//            new URL(base + "assets/configurations/MaleFG_CA_04.xml"),
//            new URL(base + "assets/configurations/FemaleFG_AA_02.xml"),
//            new URL(base + "assets/configurations/MaleD_CA_01_bin.xml"),
        };
        Vector3f translationVec = new Vector3f(4, 0, -4);
        PMatrix xform = new PMatrix(new Vector3f(0, 3.34f, 0), Vector3f.UNIT_XYZ, translationVec);
        Avatar avatar = new Avatar(new MaleAvatarAttributes("hey folks", false), worldManager);

//        for (URL configFile : configFiles)
//        {
//            avatar = new Avatar(configFile, worldManager, null, xform);
            avatar.setObjectCollection(collection);
            avatar.selectForInput();
            controller.getAvatarTeam().add(avatar);
//            translationVec.x -= 1;
//            xform.setTranslation(translationVec);
//        }

        try {
            Thread.sleep(4000);
        }
        catch (InterruptedException ex)
        {

        }
        avatar.setSkinTone(new ColorRGBA().magenta);

        showInstruments(avatar, worldManager);
    }

    private AvatarObjectCollection setUpNavigationNodes(DemoAvatarControlScheme controller)
    {
        AvatarObjectCollection objects = new AvatarObjectCollection("Objest!", worldManager);
        float block = 10.0f;
        float halfBlock = 5f;
        // Create locations for the game
        LocationNode chairGame1 = new LocationNode("Location 1", Vector3f.ZERO, halfBlock, objects);
        LocationNode chairGame2 = new LocationNode("Location 2", Vector3f.UNIT_X.mult(block), halfBlock, objects);
        LocationNode chairGame3 = new LocationNode("Location 3", new Vector3f(block, 0.0f, block), halfBlock, objects);
        LocationNode chairGame4 = new LocationNode("Location 4", Vector3f.UNIT_Z.mult(block), halfBlock, objects);
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
        controller.setCommandEntireTeam(true);
        controller.setObjectCollection(objects);
        return objects;
    }

    private void showInstruments(Avatar targetAvatar, WorldManager wm)
    {
        // "scene" data
        SceneEssentials scene = new SceneEssentials();
        scene.setAvatar(targetAvatar);
        scene.setSceneData(targetAvatar.getJScene(), targetAvatar.getPScene(), repository, wm, null);
        // Animation panel
        JPanel_Animations anim = new JPanel_Animations();
        anim.setPanel(scene);
        JFrame newFrame = new JFrame();
        newFrame.add(anim);
        newFrame.pack();
        newFrame.setVisible(true);
        // Node explorer
        TreeExplorer te = new TreeExplorer();
        te.setExplorer(scene);
        te.setVisible(true);
        // Instrumentation controller
        JFrame_InstrumentationGUI instruments = new JFrame_InstrumentationGUI(wm);
        instruments.setVisible(true);
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
