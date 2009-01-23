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
import imi.character.avatar.DebugAttributes;
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
import imi.utils.input.AvatarControlScheme;
import imi.utils.instruments.Instrumentation;
import org.jdesktop.mtgame.WorldManager;

/**
 * Test attachments
 * @author Ronald E Dahlgren
 */
public class AttachmentTest extends DemoBase
{
    
    public static void main(String[] args)
    {
        AttachmentTest myTest = new AttachmentTest(args);
    }
    public AttachmentTest(String[] args)
    {
        super(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) {

        // Change camera speed
        FirstPersonCamState camState = (FirstPersonCamState) wm.getUserData(CameraState.class);
        camState.setMovementRate(0.01f);
        // The event processor provides the linkage between AWT events and input controls
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) wm.getUserData(JSceneEventProcessor.class);
        // Set the input scheme that we intend to use
        AvatarControlScheme control = (AvatarControlScheme)eventProcessor.setDefault(new AvatarControlScheme(null));

        Instrumentation instruments2 = (Instrumentation)wm.getUserData(Instrumentation.class);
        instruments2.disableSubsytem(Instrumentation.InstrumentedSubsystem.AnimationSystem);

//        DebugAttributes attributes = new DebugAttributes("Debugger!", false, true, false, false, false, true);
        FemaleAvatarAttributes attributes = new FemaleAvatarAttributes("chica", true);
        Avatar attachmentAvatar = new Avatar(attributes, wm);
        attachmentAvatar.selectForInput();
        control.setavatar(attachmentAvatar);

        // Attach something to the Spine2
        InstructionProcessor processor = new InstructionProcessor(wm);
        Instruction attachmentInstructionSet = new Instruction();
        attachmentInstructionSet.addChildInstruction(Instruction.InstructionType.setSkeleton, attachmentAvatar.getSkeleton());
        String fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
        attachmentInstructionSet.addChildInstruction(Instruction.InstructionType.loadGeometry, fileProtocol + "assets/models/collada/Accessories/accessories.dae");
        attachmentInstructionSet.addAttachmentInstruction("MaleRoundGlasses", "Neck", new PMatrix(new Vector3f(0, -2, 0)));
        attachmentInstructionSet.addAttachmentInstruction("MaleRoundGlasses", "Head", new PMatrix(new Vector3f(0, -2, 0)));
        processor.execute(attachmentInstructionSet);

        // Construct a tree explorer for analyzing the scene graph
        TreeExplorer te = new TreeExplorer();
        SceneEssentials se = new SceneEssentials();
        se.setSceneData(attachmentAvatar.getJScene(), attachmentAvatar.getPScene(), attachmentAvatar, wm, null);
        te.setExplorer(se);
        te.setVisible(true);

        JFrame_InstrumentationGUI instruments = new JFrame_InstrumentationGUI(wm);
        instruments.setVisible(true);

    }

}
