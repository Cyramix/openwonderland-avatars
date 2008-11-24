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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.tests;


import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarDressShirt;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.Instruction.InstructionNames;
import imi.loaders.collada.InstructionProcessor;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * 
 * 
 *  VerletArm has its own test now imi.tests.VerletArmTest
 * 
 *  testing loading heads with different bind pose settings
 * 
 * 
 * @author Lou Hayt
 */
public class COLLADA_CharacterTest extends DemoBase
{
    public COLLADA_CharacterTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        Logger.getLogger("com.jme.renderer").setLevel(Level.OFF);
        COLLADA_CharacterTest worldTest = new COLLADA_CharacterTest(args);
    }

    @Override
    protected void createDemoEntities(WorldManager wm) 
    {   
        // Create ninja input scheme
        NinjaControlScheme control = (NinjaControlScheme)((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(new NinjaControlScheme(null));
        
        // Create avatar
        NinjaAvatar avatar = new NinjaAvatarDressShirt("Avatar", wm);
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);
        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
        
        // Load the african head
        String fileProtocol = avatar.getAttributes().getBaseURL();
        if (fileProtocol == null)
            fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");
        InstructionProcessor pProcessor = new InstructionProcessor(wm);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addInstruction(InstructionNames.loadBindPose, fileProtocol + "assets/models/collada/Heads/MaleAfricanHead/AfricanAmericanMaleHead1_Bind.dae");
        //pRootInstruction.addInstruction(InstructionNames.loadBindPose, fileProtocol + "assets/models/collada/Heads/MaleAsianHead/AsianMaleHead1_Bind.dae");
        pProcessor.execute(pRootInstruction, false);
        
        // Wait for the avatar to load
        while (!avatar.isInitialized())
        {
            try {
            Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(COLLADA_CharacterTest.class.getName()).log(Level.SEVERE, null, ex);
                                                  }
        }
        
        // Set the avatars head 
        avatar.installHead(pProcessor.getSkeleton());
    }
}
