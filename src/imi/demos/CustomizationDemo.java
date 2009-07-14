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
import imi.camera.FirstPersonCamState;
import imi.character.MaleAvatarParams;
import imi.character.Manipulator;
import imi.character.avatar.Avatar;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import java.awt.Color;
import org.jdesktop.mtgame.WorldManager;

/**
 * Shows how to use the character manipulator static methods to customzie
 * an avatar with API calls.
 * @author Lou Hayt
 */
public class CustomizationDemo extends DemoBase
{
    public CustomizationDemo(String[] args) {
        super(args);
    }
    public static void main(String[] args) {
        new CustomizationDemo(args);
    }

    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -2.0f));

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);

        // Create an avatar
        //FemaleAvatarParams params = new FemaleAvatarParams("Avatar", 0, 0, 0, 0, 0);
        MaleAvatarParams params = new MaleAvatarParams("Avatar").build(false);
        Avatar avatar = new Avatar.AvatarBuilder(params, wm).build();
        control.addCharacterToTeam(avatar);

        // Custimize!

//        Manipulator.adjustHeadPosition(avatar, new Vector3f(0.0f, 0.2f, 0.0f));
//        Manipulator.adjustHeadScale(avatar, new Vector3f(2.0f, 1.5f, 1.0f));
//        Manipulator.adjustLeftEarPosition(avatar, new Vector3f(0.0f, 0.5f, 0.0f));
//        Manipulator.adjustLeftEarScale(avatar, new Vector3f(1.0f, 1.0f, 1.0f));
//        Manipulator.adjustLeftEyePosition(avatar, new Vector3f(0.0f, 0.05f, 0.0f));
//        Manipulator.adjustLeftEyeScale(avatar, new Vector3f(0.0f, 1.5f, 0.0f));
//        Manipulator.adjustRightEarPosition(avatar, new Vector3f(0.0f, 0.5f, 0.0f));
//        Manipulator.adjustRightEarScale(avatar, new Vector3f(1.0f, 1.0f, 1.0f));
//        Manipulator.adjustRightEyePosition(avatar, new Vector3f(0.0f, 0.05f, 0.0f));
//        Manipulator.adjustRightEyeScale(avatar, new Vector3f(0.0f, 1.5f, 0.0f));

//        Manipulator.adjustLipsWidth(avatar, new Vector3f(0.0f, 0.01f, 0.02f));
//        Manipulator.adjustNosePosition(avatar, new Vector3f(0.0f, -0.03f, 0.0f));
//        Manipulator.adjustNoseScale(avatar, new Vector3f(0.3f, 0.3f, 0.3f));
////
//        Manipulator.adjustLeftCalfLength(avatar, new Vector3f(0.0f, 0.3f, 0.0f));
//        Manipulator.adjustLeftCalfScale(avatar, new Vector3f(0.8f, 0.0f, 0.2f));
//        Manipulator.adjustLeftFootLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftFootScale(avatar, new Vector3f(0.0f, 0.5f, 1.0f));
//        Manipulator.adjustLeftForearmLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftForearmScale(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftHandLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftHandScale(avatar, new Vector3f(0.35f, 0.05f, 0.1f));
//        Manipulator.adjustLeftThighLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftThighScale(avatar, new Vector3f(0.45f, 0.05f, 0.1f));
//        Manipulator.adjustLeftUpperarmLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustLeftUpperarmScale(avatar, new Vector3f(0.15f, 0.05f, 0.1f));
//
//        Manipulator.adjustRightCalfLength(avatar, new Vector3f(0.0f, 0.3f, 0.0f));
//        Manipulator.adjustRightCalfScale(avatar, new Vector3f(0.8f, 0.0f, 0.2f));
//        Manipulator.adjustRightFootLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightFootScale(avatar, new Vector3f(0.0f, 0.5f, 1.0f));
//        Manipulator.adjustRightForearmLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightForearmScale(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightHandLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightHandScale(avatar, new Vector3f(0.35f, 0.05f, 0.1f));
//        Manipulator.adjustRightThighLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightThighScale(avatar, new Vector3f(0.45f, 0.05f, 0.1f));
//        Manipulator.adjustRightUpperarmLength(avatar, new Vector3f(0.05f, 0.05f, 0.1f));
//        Manipulator.adjustRightUpperarmScale(avatar, new Vector3f(0.15f, 0.05f, 0.1f));
//
//Manipulator.adjustShoulderBroadness(avatar, displacement);
//Manipulator.adjustStomacheRoundness(avatar, scale);
//Manipulator.adjustTorsoLength(avatar, displacement);
//Manipulator.adjustTorsoScale(avatar, scale);
//Manipulator.adjustUpperLipScale(avatar, scale);



    Manipulator.setShirtColor(avatar, Color.RED);
    Manipulator.setPantsColor(avatar, Color.yellow);
    Manipulator.setHairColor(avatar, Color.CYAN);
        // Stuff that doesn't work
        //Manipulator.adjustBodyScale(avatar, new Vector3f(3.0f, 3.0f, 3.0f));
        //Manipulator.adjustLowerLipScale(avatar, new Vector3f(0.0f, 0.5f, 0.0f));
    }
}

