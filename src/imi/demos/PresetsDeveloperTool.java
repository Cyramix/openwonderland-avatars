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
import com.jme.renderer.ColorRGBA;
import imi.camera.FirstPersonCamState;
import imi.character.Character;
import imi.character.FemaleAvatarParams;
import imi.character.MaleAvatarParams;
import imi.character.Manipulator;
import imi.character.avatar.Avatar;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.repository.CacheBehavior;
import imi.scene.PMatrix;
import imi.utils.MaterialMeshUtils.ShaderType;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class PresetsDeveloperTool extends DemoBase
{
    public PresetsDeveloperTool(String[] args) {
        super(args);
    }
    public static void main(String[] args) {
        PresetsDeveloperTool main = new PresetsDeveloperTool(args);
    }
    MaleAvatarParams maleParams = null;
    FemaleAvatarParams femaleParams = null;
    Avatar male = null;
    Avatar female = null;
    @Override
    protected void createApplicationEntities(WorldManager wm)
    {
        // Load the binary cache nugget into the repo
        try {
            FileInputStream fis = new FileInputStream(new File("assets/models/binary/ClothingHairAccessories.car"));
            repository.loadCacheState(fis);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        // Create simple floor
        createSimpleFloor(wm, 50.0f, 50.0f, 10.0f, Vector3f.ZERO, null);

        // Tweak the camera a bit
        FirstPersonCamState camState = (FirstPersonCamState)m_cameraProcessor.getState();
        camState.setMovementRate(0.1f);
        camState.setCameraPosition(new Vector3f(0.0f, 1.8f, -3.0f));
        camState.setLeftKeyCode(KeyEvent.VK_A);
        camState.setRightKeyCode(KeyEvent.VK_D);
        camState.setAscendKeyCode(KeyEvent.VK_E);
        camState.setDescendKeyCode(KeyEvent.VK_Q);
        camState.setForwardKeyCode(KeyEvent.VK_W);
        camState.setBackwardKeyCode(KeyEvent.VK_S);

        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        controls control = new controls(wm);
        ime.addInputClient(control);

        // Create male avatar
        PMatrix mat = new PMatrix();
        mat.setRotation(new Vector3f(0.0f, (float)Math.toRadians(180), 0.0f));
        maleParams = new MaleAvatarParams("Avatar").build(false);
        maleParams.clearColorsToWhite();
        male = new Avatar.AvatarBuilder(maleParams, wm).transform(mat).build();
        control.addCharacterToTeam(male);
        // Create female avatar
        femaleParams = new FemaleAvatarParams("Avatar").build(false);
        femaleParams.clearColorsToWhite();
        mat.setTranslation(new Vector3f(1.0f, 0.0f, 0.0f));
        female = new Avatar.AvatarBuilder(femaleParams, wm).transform(mat).build();
        control.addCharacterToTeam(female);

        // TEST head
//        male.setEnableFacialAnimation(false);
//        Manipulator.swapHeadMesh(male, true, new File("assets/models/collada/Heads/MaleHead/FG_Male02LowPoly.dae"), ShaderType.PhongFleshShader);

        // TEST hair
//        String fileName = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
//        String meshName = "Short_PT_RShape";// "Curly_bangsShape";
//        Manipulator.swapHairMesh(female, true, new File(fileName), meshName);

        // WTF
        control.set(male, female, maleParams, femaleParams, camState);
    }

    public static enum Regions {
        Hair,
        Head,
        Torso,
        Legs,
        Feet
    }
    public static class controls extends DefaultCharacterControls
    {
        FirstPersonCamState camState = null;
        Regions region = Regions.Hair;
        int [] maleCurrentPresets    = new int[Regions.values().length];
        int [] femaleCurrentPresets  = new int[Regions.values().length];
        int [] maleNumberOfPresets   = new int[Regions.values().length];
        int [] femaleNumberOfPresets = new int[Regions.values().length];
        MaleAvatarParams maleParams = null;
        FemaleAvatarParams femaleParams = null;
        Avatar male = null;
        Avatar female = null;
        boolean AvatarMovementOn = false;
        public controls(WorldManager worldManager) {
            super(worldManager);
            for (int i = 0; i < Regions.values().length; i++)
                maleCurrentPresets[i] = femaleCurrentPresets[i] = 0;
        }
        @Override
        public void processKeyEvent(KeyEvent ke)
        {
            if (AvatarMovementOn)
                super.processKeyEvent(ke);
            else if (   ke.getKeyCode() != KeyEvent.VK_Q &&
                        ke.getKeyCode() != KeyEvent.VK_E &&
                        ke.getKeyCode() != KeyEvent.VK_W &&
                        ke.getKeyCode() != KeyEvent.VK_S &&
                        ke.getKeyCode() != KeyEvent.VK_A &&
                        ke.getKeyCode() != KeyEvent.VK_D    )
                    super.processKeyEvent(ke);

            if (ke.getID() == KeyEvent.KEY_PRESSED)
            {
                Character avatar = getCurrentlySelectedCharacter();
                boolean isMale = avatar == male;

                if (ke.getID() == KeyEvent.VK_PAGE_DOWN || ke.getID() == KeyEvent.VK_PAGE_UP)
                {
                    if (isMale)
                        System.out.println("Male selected");
                    else
                        System.out.println("Female selected");
                }

                // Swap mesh
                if (ke.getKeyCode() == KeyEvent.VK_RIGHT || ke.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    int index = region.ordinal();
                    if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
                    {
                        if (isMale)
                        {
                            maleCurrentPresets[index]++;
                            if (maleCurrentPresets[index] >= maleNumberOfPresets[index])
                                maleCurrentPresets[index] = 0;
                        }
                        else
                        {
                            femaleCurrentPresets[index]++;
                            if (femaleCurrentPresets[index] >= femaleNumberOfPresets[index])
                                femaleCurrentPresets[index] = 0;
                        }
                    }
                    else// if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                    {
                        if (isMale)
                        {
                            maleCurrentPresets[index]--;
                            if (maleCurrentPresets[index] < 0)
                                maleCurrentPresets[index] = maleNumberOfPresets[index]-1;
                        }
                        else
                        {
                            femaleCurrentPresets[index]--;
                            if (femaleCurrentPresets[index] < 0)
                                femaleCurrentPresets[index] = femaleNumberOfPresets[index]-1;
                        }
                    }

                    switch (region)
                    {
                        case Hair:
                            setHair(isMale);
                        break;
                        case Head:
                            setHead(isMale);
                        break;
                        case Torso:
                            setTorso(isMale);
                        break;
                        case Legs:
                            setLegs(isMale);
                        break;
                        case Feet:
                            setFeet(isMale);
                        break;
                    }
                }

                // Change mesh region
                if (ke.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    int ord = region.ordinal();
                    ord++;
                    if (ord >= region.values().length)
                        ord = 0;
                    region = region.values()[ord];
                    if (isMale)
                        System.out.println("Current mesh region: " + region.toString() + ". Male is selected.");
                    else
                        System.out.println("Current mesh region: " + region.toString() + ". Female is selected.");
                }

                // Change mesh region
                if (ke.getKeyCode() == KeyEvent.VK_UP)
                {
                    int ord = region.ordinal();
                    ord--;
                    if (ord < 0)
                        ord = region.values().length-1;
                    region = region.values()[ord];
                    if (isMale)
                        System.out.println("Current mesh region: " + region.toString() + ". Male is selected.");
                    else
                        System.out.println("Current mesh region: " + region.toString() + ". Female is selected.");
                }

                // Print help
                if (ke.getKeyCode() == KeyEvent.VK_H || ke.getKeyCode() == KeyEvent.VK_BACK_QUOTE)
                {
                    printHelp();
                }

                // Toggle camera/avatar movement
                if (ke.getKeyCode() == KeyEvent.VK_CAPS_LOCK ||
                        ke.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                {
                    AvatarMovementOn = !AvatarMovementOn;
                    if (AvatarMovementOn)
                    {
                        camState.setLeftKeyCode(KeyEvent.VK_NUMPAD4);
                        camState.setRightKeyCode(KeyEvent.VK_NUMPAD6);
                        camState.setAscendKeyCode(KeyEvent.VK_NUMPAD9);
                        camState.setDescendKeyCode(KeyEvent.VK_NUMPAD7);
                        camState.setForwardKeyCode(KeyEvent.VK_NUMPAD8);
                        camState.setBackwardKeyCode(KeyEvent.VK_NUMPAD5);
                    }
                    else
                    {
                        camState.setLeftKeyCode(KeyEvent.VK_A);
                        camState.setRightKeyCode(KeyEvent.VK_D);
                        camState.setAscendKeyCode(KeyEvent.VK_E);
                        camState.setDescendKeyCode(KeyEvent.VK_Q);
                        camState.setForwardKeyCode(KeyEvent.VK_W);
                        camState.setBackwardKeyCode(KeyEvent.VK_S);
                    }
                }

                // Dump current presets
                if (ke.getKeyCode() == KeyEvent.VK_SPACE)
                {
                    Regions[] regions = Regions.values();
                    if(isMale)
                    {
                        System.out.println("Male presets:");
                        for (int i = 0; i < regions.length; i++) {
                            int preset = maleCurrentPresets[regions[i].ordinal()];
                            System.out.println(regions[i] + ": " + preset);
                            // Dahlgren: Adding file names to the output
                            String fileName = null;
                            switch (regions[i])
                            {
                                case Feet:
                                    fileName = maleParams.getFeetPresetsFileNames().get(preset);
                                    break;
                                case Hair:
                                    fileName = maleParams.getHairPresetsFileNames().get(preset);
                                    break;
                                case Head:
                                    fileName = maleParams.getHeadPresetsFileNames().get(preset);
                                    break;
                                case Legs:
                                    fileName = maleParams.getLegsPresetsFileNames().get(preset);
                                    break;
                                case Torso:
                                    fileName = maleParams.getTorsoPresetsFileNames().get(preset);
                                    break;
                            }
                            System.out.println("Filename for " + regions[i] + ", " + fileName);
                        }
                    }
                    else
                    {
                        System.out.println("Female presets:");
                        for (int i = 0; i < regions.length; i++) {
                            int preset = femaleCurrentPresets[regions[i].ordinal()];
                            System.out.println(regions[i] + ": " + preset);
                            // Dahlgren: Adding file names to the output
                            String fileName = null;
                            switch (regions[i])
                            {
                                case Feet:
                                    fileName = femaleParams.getFeetPresetsFileNames().get(preset);
                                    break;
                                case Hair:
                                    fileName = femaleParams.getHairPresetsFileNames().get(preset);
                                    break;
                                case Head:
                                    fileName = femaleParams.getHeadPresetsFileNames().get(preset);
                                    break;
                                case Legs:
                                    fileName = femaleParams.getLegsPresetsFileNames().get(preset);
                                    break;
                                case Torso:
                                    fileName = femaleParams.getTorsoPresetsFileNames().get(preset);
                                    break;
                            }
                            System.out.println("Filename for " + regions[i] + ", " + fileName);
                        }
                    }
                }
            }
        }

        private void set(Avatar male, Avatar female, MaleAvatarParams maleParams, FemaleAvatarParams femaleParams, FirstPersonCamState camState) {
            this.camState = camState;
            this.male = male;
            this.female = female;
            this.maleParams = maleParams;
            this.femaleParams = femaleParams;
            maleNumberOfPresets[Regions.Hair.ordinal()]  = maleParams.getNumberOfHairPresets();
            maleNumberOfPresets[Regions.Head.ordinal()]  = maleParams.getNumberOfHeadPresets();
            maleNumberOfPresets[Regions.Torso.ordinal()] = maleParams.getNumberOfTorsoPresets();
            maleNumberOfPresets[Regions.Legs.ordinal()]  = maleParams.getNumberOfLegsPresets();
            maleNumberOfPresets[Regions.Feet.ordinal()]  = maleParams.getNumberOfFeetPresets();
            femaleNumberOfPresets[Regions.Hair.ordinal()]  = femaleParams.getNumberOfHairPresets();
            femaleNumberOfPresets[Regions.Head.ordinal()]  = femaleParams.getNumberOfHeadPresets();
            femaleNumberOfPresets[Regions.Torso.ordinal()] = femaleParams.getNumberOfTorsoPresets();
            femaleNumberOfPresets[Regions.Legs.ordinal()]  = femaleParams.getNumberOfLegsPresets();
            femaleNumberOfPresets[Regions.Feet.ordinal()]  = femaleParams.getNumberOfFeetPresets();
            printHelp();
        }

        public void printHelp() {
            System.out.println("\n\nWellcome to the amazing developer tool\n" +
                "PageUp/Down            -   Toggle Male/Female control\n" +
                "CapsLock, Backspace    -   Toggle camera/avatar controls\n" +
                "A,D,W,S,Q,E            -   Camera/avatar movement\n" +
                "<>                     -   Cycle animations\n" +
                "Ctrl                   -   Perform currently selected animation\n" +
                "0,9,8                  -   Facial animations\n" +
                "Up/Down                -   Cycle body parts for mesh swaping\n" +
                "Right/Left             -   Swap current body part preset mesh\n" +
                "I                      -   Shut eyes\n" +
                "P                      -   Take a screen shot\n" +
                "T                      -   Wireframe toggle\n" +
                "R                      -   Skeleton debug render\n" +
                "Space                  -   Print current presets\n" +
                "~,h                    -   Print Help\n" +
                "\n\n");
        }

        void setHair(boolean isMale)
        {
            if (isMale)
            {
                int maleHair = maleCurrentPresets[Regions.Hair.ordinal()];
                String fileName = maleParams.getHairPresetsFileNames().get(maleHair);
                String meshName = maleParams.getHairPresetsMeshNames().get(maleHair);
                Manipulator.swapHairMesh(male, true, new File(fileName), meshName);
                System.out.println("Current hair preset: " + maleHair + " mesh name: " + meshName + " file: " + fileName);
            }
            else
            {
                int femaleHair = femaleCurrentPresets[Regions.Hair.ordinal()];
                String fileName = femaleParams.getHairPresetsColladaFileNames().get(femaleHair);
                String meshName = femaleParams.getHairPresetsMeshNames().get(femaleHair);
                // Special case for the skinned hair
                if (meshName.equals("HairAShape1"))
                {
                    //Manipulator.swapSkinnedMesh(female, true, new File(fileName), "Hair");
                    System.out.println("Special case skinned hair! The tool does not support it at the moment.");
                }
                else
                {
                    Manipulator.swapHairMesh(female, true, new File(fileName), meshName);
                    System.out.println("Current hair preset: " + femaleHair + " mesh name: " + meshName + " file: " + fileName);
                }
            }
        }

        void setHead(boolean isMale)
        {
            if (isMale)
            {
                int maleHead = maleCurrentPresets[Regions.Head.ordinal()];
                String headFileName = maleParams.getHeadPresetsFileNames().get(maleHead);
                ColorRGBA skint = maleParams.getHeadPresetsSkinTone().get(maleHead);
                if (skint != null)
                {
                    Manipulator.setSkinTone(male, new Color(skint.r, skint.g, skint.b));
                    maleParams.setApplySkinToneOnHead(false);
                }
                else
                {
                    maleParams.setApplySkinToneOnHead(true);
                    maleParams.randomizeSkinTone();
                }
                if (headFileName.equals("assets/models/collada/Heads/Binary/blackHead.bhf") ||
                        headFileName.equals("assets/models/collada/Heads/Binary/AsianHeadMale.bhf"))
                    maleParams.setAnimateFace(false); // no facial animations for this head (that work!) - this will not do anything on run time, just works on load
                boolean phong = maleParams.getHeadPresetsPhongLighting().get(maleHead);
                maleParams.setUsePhongLightingForHead(phong);
                if (phong)
                    Manipulator.swapHeadMesh(male, true, new File(headFileName), ShaderType.PhongFleshShader);
                else
                    Manipulator.swapHeadMesh(male, true, new File(headFileName), ShaderType.FleshShader);
                System.out.println("Current head preset: " + maleHead + " file: " + headFileName + " phong: " + phong + " skin tone: " + skint);
            }
            else
            {
                int femaleHead = femaleCurrentPresets[Regions.Head.ordinal()];
                String headFileName = femaleParams.getHeadPresetsFileNames().get(femaleHead);
                ColorRGBA skint = femaleParams.getHeadPresetsSkinTone().get(femaleHead);
                if (skint != null)
                {
                    Manipulator.setSkinTone(female, new Color(skint.r, skint.g, skint.b));
                    femaleParams.setApplySkinToneOnHead(false);
                }
                else
                {
                    femaleParams.setApplySkinToneOnHead(true);
                    femaleParams.randomizeSkinTone();
                }
                if (headFileName.equals("assets/models/collada/Heads/Binary/blackHead.bhf") ||
                        headFileName.equals("assets/models/collada/Heads/Binary/AsianHeadMale.bhf"))
                    femaleParams.setAnimateFace(false); // no facial animations for this head (that work!) - this will not do anything on run time, just works on load
                boolean phong = femaleParams.getHeadPresetsPhongLighting().get(femaleHead);
                if (phong)
                    Manipulator.swapHeadMesh(female, true, new File(headFileName), ShaderType.PhongFleshShader);
                else
                    Manipulator.swapHeadMesh(female, true, new File(headFileName), ShaderType.FleshShader);
                System.out.println("Current head preset: " + femaleHead + " file: " + headFileName + " phong: " + phong + " skin tone: " + skint);
            }
        }

        void setTorso(boolean isMale)
        {
            if (isMale)
            {
                int maleTorso = maleCurrentPresets[Regions.Torso.ordinal()];
                String fileName = maleParams.getTorsoPresetsFileNames().get(maleTorso);
                // Special case for the jacket, add the shirt underneath
                if (fileName.equals("assets/models/collada/Clothing/MaleClothing/SuitJacket.dae"))
                {
                    String shirtUnderneath = "assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae";
                    Manipulator.swapShirtMesh(male, true, new File(shirtUnderneath));
                    Manipulator.swapJacketMesh(male, true, new File(fileName));
                    System.out.println("Current torso preset: " + maleTorso + " shirt underneath file: " + shirtUnderneath);
                }
                else
                {
                    Manipulator.clearSubGroup(male, true, "Jackets");
                    Manipulator.swapShirtMesh(male, true, new File(fileName));
                }
                System.out.println("Current torso preset: " + maleTorso + " file: " + fileName);
            }
            else
            {
                int femaleTorso = femaleCurrentPresets[Regions.Torso.ordinal()];
                String fileName = femaleParams.getTorsoPresetsFileNames().get(femaleTorso);
                Manipulator.swapShirtMesh(female, true, new File(fileName));
                System.out.println("Current torso preset: " + femaleTorso + " file: " + fileName);
            }
        }

        void setLegs(boolean isMale)
        {
            if (isMale)
            {
                int maleLegs = maleCurrentPresets[Regions.Legs.ordinal()];
                String fileName = maleParams.getLegsPresetsFileNames().get(maleLegs);
                Manipulator.swapPantsMesh(male, true, new File(fileName));
                System.out.println("Current legs preset: " + maleLegs + " file: " + fileName);
            }
            else
            {
                int femaleLegs = femaleCurrentPresets[Regions.Legs.ordinal()];
                String fileName = femaleParams.getLegsPresetsFileNames().get(femaleLegs);
                Manipulator.swapPantsMesh(female, true, new File(fileName));
                System.out.println("Current legs preset: " + femaleLegs + " file: " + fileName);
            }
        }

        void setFeet(boolean isMale)
        {
            if (isMale)
            {
                int maleFeet = maleCurrentPresets[Regions.Feet.ordinal()];
                String fileName = maleParams.getFeetPresetsFileNames().get(maleFeet);
                Manipulator.swapShoesMesh(male, true, new File(fileName));
                System.out.println("Current feet preset: " + maleFeet + " file: " + fileName);
            }
            else
            {
                int femaleFeet = femaleCurrentPresets[Regions.Feet.ordinal()];
                String fileName = femaleParams.getFeetPresetsFileNames().get(femaleFeet);
                Manipulator.swapShoesMesh(female, true, new File(fileName));
                System.out.println("Current feet preset: " + femaleFeet + " file: " + fileName);
            }
        }
    }
}
