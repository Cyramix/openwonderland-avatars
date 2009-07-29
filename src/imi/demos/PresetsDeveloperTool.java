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
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.scene.PMatrix;
import imi.utils.MaterialMeshUtils.ShaderType;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
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
        // Print instruction
        System.out.println("\n\nWellcome to the amazing developer tool\n" +
                "PageUp/Down    -   Toggle Male/Female control\n" +
                "A,D,W,S,Q,E    -   Camera movement\n" +
                "<>             -   Cycle animations\n" +
                "Ctrl           -   Perform currently selected animation\n" +
                "0,9,8          -   Facial animations\n" +
                "Up/Down        -   Cycle body parts for mesh swaping\n" +
                "Right/Left     -   Swap current body part preset mesh\n" +
                "I              -   Shut eyes\n" +
                "P              -   Take a screen shot\n" +
                "T              -   Wireframe toggle\n" +
                "R              -   Skeleton debug render\n" +
                "Enter          -   Print current presets\n" +
                "\n\n");

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
        maleParams = new MaleAvatarParams("Avatar");
        maleParams.clearColorsToWhite();
        male = new Avatar.AvatarBuilder(maleParams.build(false), wm).transform(mat).build();
        control.addCharacterToTeam(male);
        // Create female avatar
//        femaleParams = new FemaleAvatarParams("Avatar");
//        femaleParams.clearColorsToWhite();
//        mat.setTranslation(new Vector3f(1.0f, 0.0f, 0.0f));
//        female = new Avatar.AvatarBuilder(femaleParams.build(false), wm).transform(mat).build();
//        control.addCharacterToTeam(female);

        // WTF
        control.set(male, female, maleParams, femaleParams);
    }

    public static enum Part {
        Hair,
        Head,
        Torso,
        Legs,
        Feet
    }
    public static class controls extends DefaultCharacterControls
    {
        Part current = Part.Hair;
        int maleHair    = 0;
        int maleHead    = 0;
        int maleTorso   = 0;
        int maleLegs    = 0;
        int maleFeet    = 0;
        int femaleHair  = 0;
        int femaleHead  = 0;
        int femaleTorso = 0;
        int femaleLegs  = 0;
        int femaleFeet  = 0;
        MaleAvatarParams maleParams = null;
        FemaleAvatarParams femaleParams = null;
        Avatar male = null;
        Avatar female = null;
        public controls(WorldManager worldManager) {
            super(worldManager);
        }
        @Override
        public void processKeyEvent(KeyEvent ke)
        {
            if (    ke.getKeyCode() != KeyEvent.VK_Q &&
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

                // Swap mesh preset++
                if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
                {
                    switch (current)
                    {
                        case Hair:
                        {
                            if (isMale)
                            {
                                maleHair++;
                                if (maleHair >= maleParams.getNumberOfHairPresets())
                                    maleHair = 0;
                            }
                            else
                            {
//                                femaleHair++;
//                                if (femaleHair >= femaleParams.getNumberofHairPresets())
//                                    femaleHair = 0;
                            }
                            setHair(isMale);
                        }
                        break;
                        case Head:
                        {
                            if (isMale)
                            {
                                maleHead++;
                                if (maleHead >= maleParams.getNumberOfHeadPresets())
                                    maleHead = 0;
                            }
                            else
                            {
//                                femaleHead++;
//                                if (femaleHead >= femaleParams.getNumberofHeadPresets())
//                                    femaleHead = 0;
                            }
                            setHead(isMale);
                        }
                        break;
                    }
                }
                
                // Swap mesh preset--
                if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    switch (current)
                    {
                        case Hair:
                        {
                            if (isMale)
                            {
                                maleHair--;
                                if (maleHair < 0)
                                    maleHair = maleParams.getNumberOfHairPresets()-1;
                            }
                            else
                            {
//                                femaleHair--;
//                                if (femaleHair < 0)
//                                    femaleHair = femaleParams.getNumberofHairPresets()-1;
                            }
                            setHair(isMale);
                        }
                        break;
                        case Head:
                        {
                            if (isMale)
                            {
                                maleHead--;
                                if (maleHead < 0)
                                    maleHead = maleParams.getNumberOfHeadPresets()-1;
                            }
                            else
                            {
//                                femaleHead--;
//                                if (femaleHead < 0)
//                                    femaleHead = femaleParams.getNumberofHeadPresets()-1;
                            }
                            setHead(isMale);
                        }
                        break;
                    }
                }

                // Change mesh region
                if (ke.getKeyCode() == KeyEvent.VK_UP)
                {
                    int ord = current.ordinal();
                    ord++;
                    if (ord >= current.values().length)
                        ord = 0;
                    current = current.values()[ord];
                    System.out.println("Current mesh region: " + current.toString());
                }

                // Change mesh region
                if (ke.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    int ord = current.ordinal();
                    ord--;
                    if (ord < 0)
                        ord = current.values().length-1;
                    current = current.values()[ord];
                    System.out.println("Current mesh region: " + current.toString());
                }
            }
        }

        private void set(Avatar male, Avatar female, MaleAvatarParams maleParams, FemaleAvatarParams femaleParams) {
            this.male = male;
            this.female = female;
            this.maleParams = maleParams;
            this.femaleParams = femaleParams;
        }
        
        void setHair(boolean isMale)
        {
            if (isMale)
            {
                String fileName = maleParams.getHairPresetsColladaFileNames().get(maleHair);
                String meshName = maleParams.getHairPresetsMeshNames().get(maleHair);
                Manipulator.swapHairMesh(male, true, new File(fileName), meshName);
                System.out.println("Current hair preset: " + maleHair + " mesh name: " + meshName + " file: " + fileName);
            }
            else
            {
//                String fileName = femaleParams.getHairPresetsColladaFileNames().get(femaleHair);
//                String meshName = femaleParams.getHairPresetsMeshNames().get(femaleHair);
//                Manipulator.swapHairMesh(female, true, new File(fileName), meshName);
//                System.out.println("Current hair preset: " + femaleHair + " mesh name: " + meshName + " file: " + fileName);
            }
        }

        void setHead(boolean isMale)
        {
            if (isMale)
            {
                String headFileName = maleParams.getHeadPresetsFileNames().get(maleHead);
                ColorRGBA skint = maleParams.getHeadPresetsSkinTone().get(maleHead);
                if (skint != null)
                {
                    Manipulator.setSkinTone(male, new Color(skint.r, skint.g, skint.b));
                    maleParams.setApplySkinToneOnHead(false);
                }
                else
                    maleParams.setApplySkinToneOnHead(true);
                if (headFileName.equals("assets/models/collada/Heads/Binary/blackHead.bhf") ||
                        headFileName.equals("assets/models/collada/Heads/Binary/AsianHeadMale.bhf"))
                    maleParams.setAnimateFace(false); // no facial animations for this head (that work!) - this will not do anything on run time, just works on load
                boolean phong = maleParams.getHeadPresetsPhongLighting().get(maleHead);
                if (phong)
                    Manipulator.swapHeadMesh(male, true, new File(headFileName), ShaderType.PhongFleshShader);
                else
                    Manipulator.swapHeadMesh(male, true, new File(headFileName), ShaderType.FleshShader);
                System.out.println("Current head preset: " + maleHead + " file: " + headFileName + " phong: " + phong + " skin tone: " + skint);
            }
        }

    }
}
