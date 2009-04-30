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
    /** Convenience strings of the SkeletonNode groups used by the Character class **/
    public static final String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    /** Collection of skin tone shades**/
    private static final ColorRGBA[] skinTones = new ColorRGBA[]
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
        new ColorRGBA(1, 1, 1, 1), // White, for testing
    };
    /** Collection of eye colors **/
    private static final String[] eyeColors = new String[]
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
        "assets/models/collada/Heads/EyeTextures/eyeColor21.png", // snake
        "assets/models/collada/Heads/EyeTextures/New_eye_hi.png", // new eyeball from ze artisté

    };

    /** Number of preset features **/
    public static final int numberOfFeet  = 3;
    public static final int numberOfLegs  = 3;
    public static final int numberOfTorso = 4;
    public static final int numberOfHair  = 49;
    public static final int numberOfHeads = 10;
    public static final int numberOfSkinTones = skinTones.length;
    public static final int numberOfEyeColors = eyeColors.length;

    /** Used to indicate that the bind pose file has already been added to the
     load instructions **/
    private boolean loadedBind = false;

    /**
     * Construct a new instance!
     * @param name The name of this avatar
     * @param bRandomCustomizations True to get random parts, false for the default bind pose
     */
    public FemaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
        super(name);
        setGender(2);
        setUsePhongLighting(true);
        // Customizations
        if (bRandomCustomizations)
        {
            int preset        = -1;
            
            ArrayList<String[]> load      = new ArrayList<String[]>();
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

    /**
     * Construct a new instance with the specified characteristics. The skin tone
     * and eye color are set to defaults (caucasion and blue)
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     */
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0, 0);
    }

    /**
     * Construct a new instance with the specified characteristics. The eye color
     * defaults to blue.
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     * @param skin
     */
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin)
    {
        this(name, feet, legs, torso, hair, head, skin, 0);
    }

    
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin, int eyeColor)
    {
        this(name, feet, legs, torso, hair, head, skin, eyeColor, true);
    }
    
    /**
     * Excplicitely construct a new instance.
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     * @param skin
     * @param eyeColor
     */
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin, int eyeColor, boolean randomizeColors)
    {
        super(name);
        setGender(2);
        
        if (randomizeColors)
        {
            randomizeHairColor();
            randomizeShirtColor();
        }
        
        ArrayList<String[]> load                  = new ArrayList<String[]>();
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

    /**
     * Randomly set a color for the hair
     */
    public void randomizeHairColor()
    {
        int preset = (int) (Math.random() * 1000000 % skinTones.length);
        float r = skinTones[preset].r * (float)Math.random();
        float g = skinTones[preset].g * (float)Math.random();
        float b = skinTones[preset].b * (float)Math.random();
        setHairColor(r, g, b);
    }

    /**
     * Set the head to one determined by the provided integer
     * @param preset
     */
    private void customizeHead(int preset)
    {
        switch (preset)
        {
            case 0:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.bhf");
                break;
            case 1:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/AsianFemaleHead.bhf");
                break;
            case 2:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleAAHead.bhf");
                break;
            case 3:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleHi.bhf");
                break;
            case 4:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleHi2.bhf");
                break;
            case 5:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleHispanicHead.bhf");
                break;
            case 6:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female_AF_Head02.bhf");
                break;
            case 7:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female01HighPoly.bhf");
                break;
            case 8:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female02HighPoly.bhf");
                break;
            case 9:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_FemaleHead01.bhf");
                break;
            case 10:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_FemaleLowPoly_01.bhf");
                break;
            default:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.bhf");
        }
    }

    /**
     * Set the feet to a set determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeFeetPresets(int preset, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        String[] szFeet = new String[2];

        switch(preset)
        {
            case 0:
            {
                // Closed to dress shoes
                szFeet[0]   = new String("assets/models/collada/Clothing/FemaleClothing/Female_ClosedToeDressShoes.dae");
                add.add(new SkinnedMeshParams("Female_ClosedToeDressShoes_Female_DressClosedToe_ShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Converse shoes
                szFeet[0]   = new String("assets/models/collada/Clothing/FemaleClothing/Female_ConverseShoes.dae");
                add.add(new SkinnedMeshParams("Female_ConverseShoes_Female_ConverseShoeShape", "Feet"));
            }
            break;
            case 2:
            {
                // Flip flops
                szFeet[0]   = new String("assets/models/collada/Clothing/FemaleClothing/FemaleFlipFlops.dae");
                add.add(new SkinnedMeshParams("FlipFlopsFemaleShape", "Feet"));
                add.add(new SkinnedMeshParams("FemaleFeet_NudeShape", "Feet"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szFeet[0]   = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae");
                }
                add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));           
            }
        }

        szFeet[1]   = new String("Feet");

        if (szFeet[0] != null)
            load.add(szFeet);
    }


    /**
     * Set the legs to a set determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeLegsPresets(int preset,  ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        String[] szLegs   = new String[2];

        switch(preset)
        {
            case 0:
            {
                // Jeans
                szLegs[0]   = new String("assets/models/collada/Clothing/FemaleClothing/FemaleJeansStraight.dae");
                add.add(new SkinnedMeshParams("JeansShape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Dress pants
                szLegs[0]   = new String("assets/models/collada/Clothing/FemaleClothing/FemaleDressPants.dae");
                add.add(new SkinnedMeshParams("PantsFemaleShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Shorts
                szLegs[0]   = new String("assets/models/collada/Clothing/FemaleClothing/FemaleShorts.dae");
                add.add(new SkinnedMeshParams("Legs_NudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("ShortsShape", "LowerBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szLegs[0]   = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae");
                }
                add.add(new SkinnedMeshParams("Legs_NudeShape",  "LowerBody"));
            }
        }
        szLegs[1]   = new String("LowerBody");

        if (szLegs[0] != null)
            load.add(szLegs);
    }

    /**
     * Set the torso to one determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    protected void customizeTorsoPresets(int preset, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // Add the hands
        String[] szHands  = new String[2];
        szHands[0]  = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae");
        szHands[1]  = new String("Hands");
        load.add(szHands);
        add.add(new SkinnedMeshParams("Hands_NudeShape",  "Hands"));

        String[] szTorso    = new String[2];

        switch(preset)
        {
            case 0:
            {
                // Dress Shirt
                szTorso[0]  = new String("assets/models/collada/Clothing/FemaleClothing/FemaleDressShirt.dae");
                add.add(new SkinnedMeshParams("ShirtMeshShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Sweater
                szTorso[0]  = new String("assets/models/collada/Clothing/FemaleClothing/FemaleSweaterCrew.dae");
                add.add(new SkinnedMeshParams("SweaterShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Jacket
                szTorso[0]  = new String("assets/models/collada/Clothing/FemaleClothing/FemaleJacket.dae");
                add.add(new SkinnedMeshParams("Jacket1Shape", "UpperBody"));
            }
            break;
            case 3:      
            {
                // Blouse       
                szTorso[0]  = new String("assets/models/collada/Clothing/FemaleClothing/FemaleBlouse.dae");
                add.add(new SkinnedMeshParams("TShirt1Shape", "UpperBody"));
                add.add(new SkinnedMeshParams("Arms_NudeShape", "UpperBody"));
            }
            break; // RED - Yanked T-Shirt on Chad's direction
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szTorso[0]  = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae");
                }
                add.add(new SkinnedMeshParams("Torso_NudeShape",  "UpperBody"));
            }
        }
        szTorso[1]  = new String("UpperBody");
        if (szTorso[0] != null)
            load.add(szTorso);
    }

    /**
     * Load all the defaults for the female avatar
     */
    private void loadDefaultBindPose() {
        ArrayList<String[]> load      = new ArrayList<String[]>();
        String[] szBind   = new String[2];
        String[] szHand   = new String[2];

        szBind[0]   = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae");
        szBind[1]   = new String("Bind");
        szHand[0]   = new String("assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae");
        szHand[1]   = new String("Hands");
        load.add(szBind); // change!
        load.add(szHand);

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("Hands_NudeShape",        "Hands"));
        add.add(new SkinnedMeshParams("FemaleFeet_NudeShape",   "Feet"));
        add.add(new SkinnedMeshParams("Torso_NudeShape",        "UpperBody"));
        add.add(new SkinnedMeshParams("Legs_NudeShape",         "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }

    /**
     * Set the hair to one determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeHairPresets(int preset, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        String[] szHair   = new String[2];

        PMatrix orientation = new PMatrix();
        switch(preset)
        {
            case 0:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("M_PigTailsShape", "Head", orientation, "Hair"));
            }
            break;
            case 1:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("CulyPigTailzShape", "Head", orientation, "Hair"));
            }
            break;
            case 2:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("L_BunShape", "Head", orientation, "Hair"));
            }
            break;
            case 3:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("M_BunShape", "Head", orientation, "Hair"));
            }
            break;
            case 4:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("CurlyPonyTailShape", "Head", orientation, "Hair"));
            }
            break;
            case 5:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("L_PonyTailShape", "Head", orientation, "Hair"));
            }
            break;
            case 6:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("M_PonyTailShape", "Head", orientation, "Hair"));
            }
            break;
            case 7:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Long_W_bangsShape", "Head", orientation, "Hair"));
            }
            break;
            case 8:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Layered_bangShape", "Head", orientation, "Hair"));
            }
            break;
            case 9:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Layered_pt_LShape", "Head", orientation, "Hair"));
            }
            break;
            case 10:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Layered_pt_RShape", "Head", orientation, "Hair"));
            }
            break;
            case 11:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Layered_pt_centerShape", "Head", orientation, "Hair"));
            }
            break;
            case 12:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Curly_bangsShape", "Head", orientation, "Hair"));
            }
            break;
            case 13:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Culry_pt_RightShape", "Head", orientation, "Hair"));
            }
            break;
            case 14:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Culry_pt_LeftShape", "Head", orientation, "Hair"));
            }
            break;
            case 15:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Curly_Mid_PtShape", "Head", orientation, "Hair"));
            }
            break;
            case 16:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Long_DredzShape", "Head", orientation, "Hair"));
            }
            break;
            case 17:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_Pt_BangzShape", "Head", orientation, "Hair"));
            }
            break;
            case 18:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_pt_CenterShape", "Head", orientation, "Hair"));
            }
            break;
            case 19:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_Curly_BangzShape", "Head", orientation, "Hair"));
            }
            break;
            case 20:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_AfricanWBangzShape", "Head", orientation, "Hair"));
            }
            break;
            case 21:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_African_Pt_RShape", "Head", orientation, "Hair"));
            }
            break;
            case 22:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_AfricanPT_CenterShape", "Head", orientation, "Hair"));
            }
            break;
            case 23:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Med_African_Pt_LShape", "Head", orientation, "Hair"));
            }
            break;
            case 24:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Short_African_MessyShape", "Head", orientation, "Hair"));
            }
            break;
            case 25:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("short_AfricanPT_CenterShape", "Head", orientation, "Hair"));
            }
            break;
            case 26:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Short_PT_RShape", "Head", orientation, "Hair"));
            }
            break;
            case 27:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Short_PT_LShape", "Head", orientation, "Hair"));
            }
            break;
            case 28:
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
                attachments.add(new AttachmentParams("Short_PT_CenterShape", "Head", orientation, "Hair"));
            }
            break;
            case 53: // SPECIAL SKINNED HAIR
            {
                szHair[0]   = new String("assets/models/collada/Hair/FemaleHair/FemaleFGHair.dae");
                add.add(new SkinnedMeshParams("HairAShape1", "Head"));
            }
            break;

        }

        szHair[1]   = new String("Hair");
        if (szHair[0] != null)
            load.add(szHair);
    }
}
