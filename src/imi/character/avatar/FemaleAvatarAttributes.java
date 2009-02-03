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
package imi.character.avatar;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.character.AttachmentParams;
import imi.character.CharacterAttributes;
import imi.scene.PMatrix;
import java.util.ArrayList;

/**
 * This class represents concrete attribute settings for the avatarAvatar. It is
 * basically a well-defined CharacterAttributes starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class FemaleAvatarAttributes extends CharacterAttributes
{
    public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    /** Collection of skin tone shades**/
    private final ColorRGBA[] skinTones = new ColorRGBA[]
    {
        new ColorRGBA(221.0f / 255.0f,  183.0f / 255.0f, 166.0f / 255.0f, 1),
        new ColorRGBA(203.0f / 255.0f,  142.0f / 255.0f, 114.0f / 255.0f, 1),
        new ColorRGBA(182.0f / 255.0f,  137.0f / 255.0f, 116.0f / 255.0f, 1),
        new ColorRGBA( 224 / 255.0f, 163 / 255.0f, 144 / 255.0f, 1),
        new ColorRGBA( 206 / 255.0f,  153 / 255.0f, 99 / 255.0f, 1),
        new ColorRGBA( 236 / 255.0f,  153 / 255.0f, 99 / 255.0f, 1),
        new ColorRGBA( 175 / 255.0f,  128 / 255.0f, 83 / 255.0f, 1),
        new ColorRGBA( 178 / 255.0f, 123  / 255.0f, 93 / 255.0f, 1),
        new ColorRGBA( 213 / 255.0f, 186  / 255.0f, 193 / 255.0f, 1),
        new ColorRGBA( 238 / 255.0f, 160  / 255.0f, 157 / 255.0f, 1),
        new ColorRGBA( 231 / 255.0f,  216 / 255.0f, 223 / 255.0f, 1),
        new ColorRGBA( 174 / 255.0f, 112  / 255.0f, 112 / 255.0f, 1),
    };
    /** Collection of eye colors **/
    private final String[] eyeColors = new String[]
    {
        "assets/models/collada/Heads/EyeTextures/Blue_Eye.png",
        "assets/models/collada/Heads/EyeTextures/Brown2_Eye.png", // dark brown
        "assets/models/collada/Heads/EyeTextures/Brown_Eye.png",
        "assets/models/collada/Heads/EyeTextures/Green_Eye.png",
        "assets/models/collada/Heads/EyeTextures/eyeColor01.png", // blue and orange in the middle
        "assets/models/collada/Heads/EyeTextures/eyeColor02.png", // light blue, slight orange specs
        "assets/models/collada/Heads/EyeTextures/eyeColor03.png", // green and yellow
        "assets/models/collada/Heads/EyeTextures/eyeColor04.png", // light blue with yellow ring
        "assets/models/collada/Heads/EyeTextures/eyeColor05.png", // light purple
        "assets/models/collada/Heads/EyeTextures/eyeColor06.png", // light aqua with slight orange middle
        "assets/models/collada/Heads/EyeTextures/eyeColor07.png", // dark orange brown
        "assets/models/collada/Heads/EyeTextures/eyeColor08.png", // green small pupil
        "assets/models/collada/Heads/EyeTextures/eyeColor09.png", // dotted orange yellowish
        "assets/models/collada/Heads/EyeTextures/eyeColor10.png", // dotted green orange
        "assets/models/collada/Heads/EyeTextures/eyeColor11.png", // fire orange big pupil
        "assets/models/collada/Heads/EyeTextures/eyeColor12.png", // dark aqua brown middle
        "assets/models/collada/Heads/EyeTextures/eyeColor13.png", // sea blue
        "assets/models/collada/Heads/EyeTextures/eyeColor14.png", // light blue orange middle
        "assets/models/collada/Heads/EyeTextures/eyeColor15.png", // dark blue orange middle
        "assets/models/collada/Heads/EyeTextures/eyeColor16.png", // fire orange yellow
        "assets/models/collada/Heads/EyeTextures/eyeColor17.png", // yellow brown
        "assets/models/collada/Heads/EyeTextures/eyeColor18.png", // light blue and brown middle
        "assets/models/collada/Heads/EyeTextures/eyeColor19.png", // darl blue brown
        "assets/models/collada/Heads/EyeTextures/eyeColor20.png", // bright blue with middle brown
        "assets/models/collada/Heads/EyeTextures/eyeColor21.png"  // snake
    };
    private boolean loadedBind = false;
    
    public FemaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
        super(name);
        setGender(2);
        
        // Customizations
        if (bRandomCustomizations)
        {
            int preset        = -1;
            int numberOfFeet  = 3;
            int numberOfLegs  = 3;
            int numberOfTorso = 4;
            int numberOfHair  = 49;
            int numberOfHeads = 2;
            
            ArrayList<String> load      = new ArrayList<String>();
            ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
            ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();

            preset = (int) (Math.random() * 1000000 % numberOfHeads);
            customizeHead(preset);
            
            preset = (int) (Math.random() * 1000000 % numberOfFeet);
            customizeFeetPresets(preset, load, add, attachments);

            preset = (int) (Math.random() * 1000000 % numberOfLegs);
            customizeLegsPresets(preset, load, add, attachments);

            preset = (int) (Math.random() * 1000000 % skinTones.length);
            setSkinTone(skinTones[preset].r, skinTones[preset].g, skinTones[preset].b);

            preset = (int) (Math.random() * 1000000 % eyeColors.length);
            setEyeballTexture(eyeColors[preset]);

            preset = (int) (Math.random() * 1000000 % numberOfTorso);
            customizeTorsoPresets(preset, load, add, attachments);
            
            preset = (int) (Math.random() * 1000000 % numberOfHair);
            customizeHairPresets(preset, load, add, attachments);

            setLoadInstructions(load);
            setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
            setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
        }
        else
        {
            customizeHead(0);
            loadDefaultBindPose();
        }
    }

    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0, 0);
    }
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin)
    {
        this(name, feet, legs, torso, hair, head, skin, 0);
    }
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin, int eyeColor)
    {
        super(name);
        setGender(2);
        
        ArrayList<String> load                  = new ArrayList<String>();
        ArrayList<SkinnedMeshParams> add        = new ArrayList<SkinnedMeshParams>();
        ArrayList<AttachmentParams> attachments = new ArrayList<AttachmentParams>();

        customizeHead(head);
        customizeFeetPresets(feet,   load, add, attachments);
        customizeLegsPresets(legs,   load, add, attachments);
        customizeTorsoPresets(torso, load, add, attachments);
        customizeHairPresets(hair,   load, add, attachments);
        
        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
        setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
        
        if (eyeColor < eyeColors.length)
            setEyeballTexture(eyeColors[eyeColor]);
        if (skin < skinTones.length)
            setSkinTone(skinTones[skin].r, skinTones[skin].g, skinTones[skin].b);
    }

    public void randomizeHairColor()
    {
//        float r = (float)Math.random();
//        float g = (float)Math.random();
//        float b = (float)Math.random();   
        
        int preset = (int) (Math.random() * 1000000 % skinTones.length);
        float r = skinTones[preset].r * (float)Math.random();
        float g = skinTones[preset].g * (float)Math.random();
        float b = skinTones[preset].b * (float)Math.random();
        setHairColor(r, g, b);
    }
    
    private void customizeHead(int preset)
    {
        switch (preset)
        {
            case 0:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.dae");
                break;
            case 1:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/AsianFemaleHead.dae");
                break;
            case 2:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleAAHead.dae");
                break;
            case 3:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleHispanicHead.dae");
                break;
            default:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.dae");
        }
    }

    private void customizeFeetPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Closed to dress shoes
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/Female_ClosedToeDressShoes.dae"));
                add.add(new SkinnedMeshParams("Female_ClosedToeDressShoes_Female_DressClosedToe_ShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Converse shoes
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/Female_ConverseShoes.dae"));
                add.add(new SkinnedMeshParams("Female_ConverseShoes_Female_ConverseShoeShape", "Feet"));
            }
            break;
            case 2:
            {
                // Flip flops
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleFlipFlops.dae"));
                add.add(new SkinnedMeshParams("FlipFlopsFemaleShape", "Feet"));
                add.add(new SkinnedMeshParams("FemaleFeet_NudeShape", "Feet"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));           
            }
        }  
    }

    private void customizeLegsPresets(int preset,  ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Jeans
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleJeansStraight.dae"));
                add.add(new SkinnedMeshParams("JeansShape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Dress pants
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleDressPants.dae"));
                add.add(new SkinnedMeshParams("PantsFemaleShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Shorts
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleShorts.dae"));
                add.add(new SkinnedMeshParams("Legs_NudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("ShortsShape", "LowerBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("Legs_NudeShape",  "LowerBody"));
            }
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        randomizeShirtColor();
        // Add the hands 
        load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae")); 
        add.add(new SkinnedMeshParams("Hands_NudeShape",  "Hands"));

        switch(preset)
        {
            case 0:
            {
                // Dress Shirt
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleDressShirt.dae"));
                add.add(new SkinnedMeshParams("ShirtMeshShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Sweater
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleSweaterCrew.dae"));
                add.add(new SkinnedMeshParams("SweaterShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Jacket
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleJacket.dae"));
                add.add(new SkinnedMeshParams("Jacket1Shape", "UpperBody"));
            }
            break;
            case 3:      
            {
                // Blouse       
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleBlouse.dae"));
                add.add(new SkinnedMeshParams("TShirt1Shape", "UpperBody"));
                add.add(new SkinnedMeshParams("Arms_NudeShape", "UpperBody"));
            }
            break; // RED - Yanked T-Shirt on Chad's direction
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("Torso_NudeShape",  "UpperBody"));
            }
        }   
    }

    private void loadDefaultBindPose() {
        ArrayList<String> load      = new ArrayList<String>();
        load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); // change!

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("Hands_NudeShape",  "Hands"));
        add.add(new SkinnedMeshParams("FemaleFeet_NudeShape",  "Feet"));
        add.add(new SkinnedMeshParams("Torso_NudeShape",  "UpperBody"));
        add.add(new SkinnedMeshParams("Legs_NudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }

    private void customizeHairPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        randomizeHairColor();
        PMatrix orientation = new PMatrix(new Vector3f((float)Math.toRadians(7.0),0,0), Vector3f.UNIT_XYZ, new Vector3f(0,0.0f,0.03f));
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PigTails", "Head", orientation, "Hair"));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PigTails", "Head", orientation, "Hair"));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CulyPigTailz", "Head", orientation, "Hair"));
            }
            break;
            case 3:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_Bun", "Head", orientation, "Hair"));
            }
            break;
            case 4:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_Bun", "Head", orientation, "Hair"));
            }
            break;
            case 5:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CurlyPonyTail", "Head", orientation, "Hair"));
            }
            break;
            case 6:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PonyTail", "Head", orientation, "Hair"));
            }
            break;
            case 7:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PonyTail", "Head", orientation, "Hair"));
            }
            break;
            case 8:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_Center", "Head", orientation, "Hair"));
            }
            break;
            case 9:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_L", "Head", orientation, "Hair"));
            }
            break;
            case 10:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_R", "Head", orientation, "Hair"));
            }
            break;
            case 11:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_center", "Head", orientation, "Hair"));
            }
            break;
            case 12:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_bang", "Head", orientation, "Hair"));
            }
            break;
            case 13:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_L", "Head", orientation, "Hair"));
            }
            break;
            case 14:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_R", "Head", orientation, "Hair"));
            }
            break;
            case 15:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_center", "Head", orientation, "Hair"));
            }
            break;
            case 16:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_bangs", "Head", orientation, "Hair"));
            }
            break;
            case 17:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Right", "Head", orientation, "Hair"));
            }
            break;
            case 18:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Left", "Head", orientation, "Hair"));
            }
            break;
            case 19:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_Mid_Pt", "Head", orientation, "Hair"));
            }
            break;
            case 20:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_right", "Head", orientation, "Hair"));
            }
            break;
            case 21:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_left", "Head", orientation, "Hair"));
            }
            break;
            case 22:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_PT_Center", "Head", orientation, "Hair"));
            }
            break;
            case 23:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_Bangs", "Head", orientation, "Hair"));
            }
            break;
            case 24:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_Dredz", "Head", orientation, "Hair"));
            }
            break;
            case 25:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Bangs", "Head", orientation, "Hair"));
            }
            break;
            case 26:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Bangz", "Head", orientation, "Hair"));
            }
            break;
            case 27:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_pt_Center", "Head", orientation, "Hair"));
            }
            break;
            case 28:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_pt_R", "Head", orientation, "Hair"));
            }
            break;
            case 29:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_pt_Left", "Head", orientation, "Hair"));
            }
            break;
            case 30:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Bangz", "Head", orientation, "Hair"));
            }
            break;
            case 31:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_L", "Head", orientation, "Hair"));
            }
            break;
            case 32:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_RT", "Head", orientation, "Hair"));
            }
            break;
            case 33:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Center", "Head", orientation, "Hair"));
            }
            break;
            case 34:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfricanWBangz", "Head", orientation, "Hair"));
            }
            break;
            case 35:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("MedDredzz", "Head", orientation, "Hair"));
            }
            break;
            case 36:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_LBang", "Head", orientation, "Hair"));
            }
            break;
            case 37:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_Messy", "Head", orientation, "Hair"));
            }
            break;
            case 38:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_R", "Head", orientation, "Hair"));
            }
            break;
            case 39:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_L", "Head", orientation, "Hair"));
            }
            break;
            case 40:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortAfrican_BoB", "Head", orientation, "Hair"));
            }
            break;
            case 41:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortDredzz", "Head", orientation, "Hair"));
            }
            break;
            case 42:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Messy", "Head", orientation, "Hair"));
            }
            break;
            case 43:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Long_Bang", "Head", orientation, "Hair"));
            }
            break;
            case 44:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Spikey", "Head", orientation, "Hair"));
            }
            break;
            case 45:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_BOB", "Head", orientation, "Hair"));
            }
            break;
            case 46:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_R", "Head", orientation, "Hair"));
            }
            break;
            case 47:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_L", "Head", orientation, "Hair"));
            }
            break;
            case 48:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_W_bangs", "Head", orientation, "Hair"));
            }
            break;
            case 49:
            {
                // Missing?
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("short_AfricanPT_CenterShape", "Head", orientation, "Hair"));
            }
            break;
            case 50:
            {
                // Missing?
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_pt_L", "Head", orientation, "Hair"));
            }
            break;
            case 51:
            {
                // Missing?
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_Pt_RShape", "Head", orientation, "Hair"));
            }
            break;
            case 52:
            {
                // Missing?
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfricanPT_CenterShape", "Head", orientation, "Hair"));
            }
            break;
        }
    }
}
