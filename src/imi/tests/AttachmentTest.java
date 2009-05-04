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
import imi.gui.JFrame_InstrumentationGUI;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.DemoAvatarControlScheme;
import imi.utils.instruments.Instrumentation;
import org.jdesktop.mtgame.WorldManager;

/**
 * Test attaching meshes to the avatar's skeleton
 * @author Ronald E Dahlgren
 */
public class AttachmentTest extends DemoBase
{

    /**
     * Run the test!
     * @param args
     */
    public static void main(String[] args)
    {
        AttachmentTest myTest = new AttachmentTest(args);
    }

    /**
     * Construct and run
     * @param args
     */
    public AttachmentTest(String[] args)
    {
        super(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) {

        // Change camera position
        FirstPersonCamState camState = (FirstPersonCamState) wm.getUserData(CameraState.class);
        camState.setCameraPosition(new Vector3f(0,1.5f,-2));
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        // Set the input scheme that we intend to use
        DemoAvatarControlScheme control = (DemoAvatarControlScheme)eventProcessor.setDefault(new DemoAvatarControlScheme(null));

        // Create an avatar to test on
        FemaleAvatarAttributes attributes = new FemaleAvatarAttributes("AttachmentGirl", true);
        Avatar attachmentAvatar = new Avatar(attributes, wm);
        // Hook up the avatar to the control scheme
        control.setAvatar(attachmentAvatar);
        attachmentAvatar.selectForInput();

        // Create the instructions needed for loading and attaching a mesh
        InstructionProcessor processor = new InstructionProcessor(wm);
        Instruction attachmentInstructionSet = new Instruction();
        // Set the skeleton to our avatar's skeleton
        attachmentInstructionSet.addChildInstruction(Instruction.InstructionType.setSkeleton, attachmentAvatar.getSkeleton());
        String fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
        // Load this file
        attachmentInstructionSet.addChildInstruction(Instruction.InstructionType.loadGeometry,
                fileProtocol + "assets/models/collada/Objects/Interface/InterfaceKnobPlate.dae");
        // Attach something
        attachmentInstructionSet.addAttachmentInstruction("BackPlate1", // Name of geometry in the file
                                                        "Head", // Joint to attach to
                                                        new PMatrix(new Vector3f(0, 0, 0.1f)), // Attachment transform
                                                        "plateJoint"); // name for the attachment joint (this is created by this instruction)
        // Do all that stuff!
        processor.execute(attachmentInstructionSet);
        // refresh the materials on our avatar (this will apply the material
        // to the newly loaded geometry). This can also be done to the mesh
        // specifically
        attachmentAvatar.applyMaterials();

        // Construct a tree explorer for analyzing the scene graph
        TreeExplorer te = new TreeExplorer();
        SceneEssentials se = new SceneEssentials();
        se.setSceneData(attachmentAvatar.getJScene(), attachmentAvatar.getPScene(), attachmentAvatar, wm, null);
        te.setExplorer(se);
        te.setVisible(true);
    }

}
