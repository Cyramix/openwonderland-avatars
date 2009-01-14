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
import imi.character.ninja.NinjaAvatar;
import imi.character.ninja.NinjaAvatarAttributes;
import imi.character.ninja.NinjaFemaleAvatarAttributes;
import imi.scene.camera.state.FirstPersonCamState;
import java.net.MalformedURLException;
import org.jdesktop.mtgame.WorldManager;


import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.NinjaControlScheme;
import java.net.URL;
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
        long startTime = System.nanoTime();
        NinjaAvatar avatar = new NinjaAvatar(new NinjaAvatarAttributes("Avatar", true, false), wm);
        float time = (float)((System.nanoTime() - startTime) / 1000000000.0f);
        System.out.println("Constructing the male took: " + time);
        
        avatar.selectForInput();
        control.getNinjaTeam().add(avatar);

        // Get the mouse evets so the verlet arm can be controlled
        control.getMouseEventsFromCamera();
 
        // change camera speed
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.03f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Wait for the avatar to load
        while (!avatar.isInitialized() || avatar.getModelInst() == null)
        {
            try {
            Thread.sleep(25000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(COLLADA_CharacterTest.class.getName()).log(Level.SEVERE, null, ex);
                                                  }
        }

        URL headLocation = null;
        try {
            // Set the avatars head
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Female/Female_Bind.dae");
            headLocation = new URL("file://localhost/work/IMI/sunSVN/assets/models/collada/Heads/MaleAfricanHead/MaleAAHead.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/FemaleCaucasian/FemaleCHead.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/FemaleKlingon/FemaleKlingonHead.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/MaleAsianHead/asiaHeadTwo.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/fourthHeadDIFb/fourthHeadDIFb.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/CaucasianHead/MaleMonkeyHead.dae");
//            headLocation = new URL("file://localhost/work/avatars/assets/models/collada/Heads/CaucasianHead/MaleCHead.dae");
        } catch (MalformedURLException ex) {
            Logger.getLogger(COLLADA_CharacterTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//
////        avatar.HACKMETHODPLEASEREMOVEME("rightEyeGeoShape");
//        avatar.HACKMETHODPLEASEREMOVEME("Legs_LegsNudeShape");
        avatar.installHead(headLocation, "Neck");

//        int rightEyeIndex = avatar.getSkeleton().getSkinnedMeshJointIndex("rightEye");
//        int leftEyeIndex = avatar.getSkeleton().getSkinnedMeshJointIndex("leftEye");
//
//        boolean bHack = true;
        // Uncomment for a tree explorer
//        TreeExplorer te = new TreeExplorer();
//        SceneEssentials se = new SceneEssentials();
//        se.setSceneData(avatar.getJScene(), avatar.getPScene(), avatar, wm, null);
//        te.setExplorer(se);
//        te.setVisible(true);
    }
}
