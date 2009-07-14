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
import imi.gui.TreeExplorer;
import imi.loaders.Instruction;
import imi.loaders.InstructionProcessor;
import imi.scene.PMatrix;
import imi.camera.AbstractCameraState;
import imi.camera.FirstPersonCamState;
import imi.character.Manipulator;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import java.io.File;
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
    public static void main(final String[] args)
    {
        new AttachmentTest(args);
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
    protected void createApplicationEntities(WorldManager wm) {

        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);
        // Change camera position
        FirstPersonCamState camState = (FirstPersonCamState) wm.getUserData(AbstractCameraState.class);
        camState.setCameraPosition(new Vector3f(0,1.5f,-2));

        // Create an object collection and generate a chair
//        AvatarObjectCollection objects = new AvatarObjectCollection("Chairs Objects", wm);
//        objects.generateChairs(new Vector3f(3.0f, 0.0f, 3.0f), 3.0f, 1);

        // Add the input client we would like to use
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        DefaultCharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);
        
        // Create an avatar to test on
        FemaleAvatarParams attributes = new FemaleAvatarParams("AttachmentGirl");
        Avatar attachmentAvatar = new Avatar.AvatarBuilder(attributes.build(), wm).build();
        // Hook up the avatar to the control scheme
        control.setCharacter(attachmentAvatar);
        attachmentAvatar.selectForInput();

        // Link it up.... press the key '3' to "sit on chair" hehe
        //attachmentAvatar.setObjectCollection(objects);

        Manipulator.swapNonSkinnedMesh(attachmentAvatar, true, new File("assets/models/collada/Objects/Chairs/ConfChair1.dae"), "Base", "Head", new PMatrix(), "attachment joint test");
        attachmentAvatar.applyMaterials();

        // Construct a tree explorer for analyzing the scene graph
        TreeExplorer te = new TreeExplorer();
        te.setExplorer(worldManager, attachmentAvatar.getPScene());
        te.setVisible(true);
    }
}
