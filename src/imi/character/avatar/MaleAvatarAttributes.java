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
public class MaleAvatarAttributes extends CharacterAttributes
{
    /** Convenience reference of the SkeletonNode groups the Character class uses.**/
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
        new ColorRGBA(1, 1, 1, 1), // Whitey, for testing
        new ColorRGBA(0.7137255f, 0.5372549f, 0.45490196f, 1)
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
        "assets/models/collada/Heads/EyeTextures/eyeColor21.png"  // snakes!
    };

    /** number of preset features **/
    public static final int numberOfFeet  = 4;
    public static final int numberOfLegs  = 4;
    public static final int numberOfTorso = 6;
    public static final int numberOfHair  = 17;
    public static final int numberOfHeads = 6;
    public static final int numberOfSkinTones = skinTones.length;
    public static final int numberOfEyeColors = eyeColors.length;

    /** Used to indicate that the bind pose file has already been added to the load instructions **/
    private boolean loadedBind = false;
    
    /**
     * Construct a new attributes instance.
     * @param name The name of the avatar
     * @param bRandomCustomizations If false, avatar starts in the bind pose, if true then random clothing will be applied
     */
    public MaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
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
     * Construct a new attributes object with the specified characteristics.
     * Skin and eyecolor are set to defaults (caucasian and blue, respectively)
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     */
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0, 0);
    }

    /**
     * Construct a new attributes object with the specified characteristics.
     * Eyecolor defaults to blue.
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     * @param skinTone
     */
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone)
    {
        this(name, feet, legs, torso, hair, head, skinTone, 0);
    }

    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor)
    {
        this(name, feet, legs, torso, hair, head, skinTone, eyeColor, true);
    }
    /**
     * Explicitely construct a new instance.
     * @param name
     * @param feet
     * @param legs
     * @param torso
     * @param hair
     * @param head
     * @param skinTone
     * @param eyeColor
     */
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor, boolean randomizeColors)
    {
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
        if (skinTone < skinTones.length)
            setSkinTone(skinTones[skinTone].r, skinTones[skinTone].g, skinTones[skinTone].b);
    }

    /**
     * Sets a random hair color
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
                setHeadAttachment("assets/models/collada/Heads/MaleHead/MaleCHead.bhf");
                break;
            case 1:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/blackHead.bhf");
                break;
            case 2:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/FaceGenMaleHi.bhf");
                break;
            case 3:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/FG_Male02MedPoly.bhf");
                break;
            case 4:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/FG_MaleHead02Medium.bhf");
                break;
            case 5:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/FG_MaleLowPoly_01.bhf");
                break;
            case 6:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/FG_Obama_HeadMedPoly.bhf");
                break;
            case 7:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/AsianHeadMale.bhf");
                break;
            default:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/MaleCHead.bhf");
        }
    }

    /**
     * Set the feet to a pair determined by the provided integer
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
                // Tennis shoes
                szFeet[0]   = new String("assets/models/collada/Clothing/MaleClothing/MaleTennisShoes.dae");
                add.add(new SkinnedMeshParams("TennisShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Flip flops
                szFeet[0]   = new String("assets/models/collada/Clothing/MaleClothing/FlipFlopsFeet.dae");
                add.add(new SkinnedMeshParams("LFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("RFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("LFlipFlopShape", "Feet"));
                add.add(new SkinnedMeshParams("RFlipFlopShape", "Feet"));
            }
            break;
            case 2:
            {
                // Dress shoes
                szFeet[0]   = new String("assets/models/collada/Clothing/MaleClothing/MaleDressShoes.dae");
                add.add(new SkinnedMeshParams("polySurfaceShape3", "Feet"));
            }
            break;
            case 3:
            {
                // Cowboy boots
                szFeet[0]   = new String("assets/models/collada/Clothing/MaleClothing/CowBoyBoot_CUT.dae");
                add.add(new SkinnedMeshParams("CowBoyBootsShape", "Feet"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szFeet[0]   = new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae");
                }
                add.add(new SkinnedMeshParams("RFootNudeShape",  "Feet"));
                add.add(new SkinnedMeshParams("LFootNudeShape",  "Feet"));
            }
        }
        szFeet[1]   = new String("Feet");

        if (szFeet[0] != null)
            load.add(szFeet);
    }

    /**
     * Set the hair to one determined by the provided integer.
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeHairPresets(int preset, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
//        load.add(new String("assets/models/collada/Hair/Hair01.dae"));
//        attachments.add(new AttachmentParams("Hair0Shape1", "Head", new PMatrix(), "Hair"));
//
//        if (true)
//            return;

        String[] szHair = new String[2];

        PMatrix orientation = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_PonyTailShape", "Head", orientation, "Hair"));
            }
            break;
            case 1:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ChinLengthPartLeftShape", "Head", orientation, "Hair"));
            }
            break;
            case 2:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_SlickedBackShape", "Head", orientation, "Hair"));
            }
            break;
            case 3:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_PartLeftShape", "Head", orientation, "Hair"));
            }
            break;
            case 4:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ShortMessyShape", "Head", orientation, "Hair"));
            }
            break;
            case 5:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_FlatTopShape", "Head", orientation, "Hair"));
            }
            break;
            case 6:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ShortTopUpShape", "Head", orientation, "Hair"));
            }
            break;
            case 7:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ShortBasicShape", "Head", orientation, "Hair"));
            }
            break;
            case 8:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ShortRecedShape", "Head", orientation, "Hair"));
            }
            break;
            case 9:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_Balding1Shape", "Head", orientation, "Hair"));
            }
            break;
            case 10:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_BaldPiccardShape", "Head", orientation, "Hair"));
            }
            break;
            case 11:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_SuperFauxHawkShape", "Head", orientation, "Hair"));
            }
            break;
            case 12:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_SpikeyShape", "Head", orientation, "Hair"));
            }
            break;
            case 13:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_ChinLengthPartRightShape", "Head", orientation, "Hair"));
            }
            break;
            case 14:
            {
                szHair[0]   = new String("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
                attachments.add(new AttachmentParams("Male_PartRightShape", "Head", orientation, "Hair"));
            }
            break;

        }
        szHair[1]   = new String("Hair");
        if (szHair[0] != null)
            load.add(szHair);
    }

    /**
     * Set the legs to one determined by the provided integer.
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeLegsPresets(int preset, ArrayList<String[]> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        String[] szLegs = new String[2];

        switch(preset)
        {
            case 0:
            {
                // Jeans
                szLegs[0]   = new String("assets/models/collada/Clothing/MaleClothing/Jeans.dae");
                add.add(new SkinnedMeshParams("polySurface3Shape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Shorts
                szLegs[0]   = new String("assets/models/collada/Clothing/MaleClothing/Shorts.dae");
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("MaleShortsShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Dress pants
                szLegs[0]   = new String("assets/models/collada/Clothing/MaleClothing/MaleDressPants1.dae");
                add.add(new SkinnedMeshParams("MaleDressPantsShape", "LowerBody"));
            }
            break;
            case 3:
            {
                // Suite pants
                szLegs[0]   = new String("assets/models/collada/Clothing/MaleClothing/Slacks.dae");
                add.add(new SkinnedMeshParams("SuitPantsShape", "LowerBody"));
            }
            break;
            case 4:
            {
                // Meso
                szLegs[0]   = new String("assets/models/collada/Clothing/MaleClothing/MaleMesoBottom.dae");
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("polySurfaceShape6", "LowerBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szLegs[0]   = new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae");
                }
                add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));
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
        // Add the hands in either way
        String[] szHands  = new String[2];
        szHands[0]  = new String("assets/models/collada/Avatars/MaleAvatar/Male_Hands.dae");
        szHands[1]  = new String("Hands");
        load.add(szHands); // change!
        add.add(new SkinnedMeshParams("RHandShape",  m_regions[1]));
        add.add(new SkinnedMeshParams("LHandShape",  m_regions[1]));

        String[] szTorso    = new String[2];

        switch(preset)
        {
            case 0:
            {
                // T Shirt
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/MaleTShirt.dae");
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("ArmsShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Polo Strips
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/MalePolo.dae");
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Dress shirt
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/MaleDressShirt.dae");
                add.add(new SkinnedMeshParams("DressShirtShape", "UpperBody"));
            }
            break;
            case 3:
            {
                // Sweater
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/MaleSweater.dae");
                add.add(new SkinnedMeshParams("SweaterMaleShape", "UpperBody"));
            }
            break;
            case 4:
            {
                // Dress shirt for suit
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae");
                add.add(new SkinnedMeshParams("SuitShirtShape", "UpperBody"));
            }
            break;
            case 5:
            {
                // Suit Jacket
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/SuitJacket.dae");
                add.add(new SkinnedMeshParams("SuitJacketShape", "UpperBody"));
                szTorso[1]  = new String("UpperBody");
                load.add(szTorso);
                // Put something under that jacket!
                // Dress shirt for suit
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae");
                add.add(new SkinnedMeshParams("SuitShirtShape", "UpperBody"));
            }
            break;
            case 6:
            {
                // Meso
                szTorso[0]  = new String("assets/models/collada/Clothing/MaleClothing/MaleMesoTop.dae");
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
                add.add(new SkinnedMeshParams("polySurfaceShape2", "UpperBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    szTorso[0]  = new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae");
                }
                add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
            }
        }
        szTorso[1]  = new String("UpperBody");

        if (szTorso[0] != null)
            load.add(szTorso);
    }

    /**
     * Load up all of the defaults for the male avatar
     */
    protected void loadDefaultBindPose()
    {
        ArrayList<String[]> load      = new ArrayList<String[]>();
        String[] szBind = new String[2];
        szBind[0]   = new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae");
        szBind[1]   = new String("Bind");
        load.add(szBind);
        loadedBind = true;

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("RHandShape",     "Hands"));
        add.add(new SkinnedMeshParams("LHandShape",     "Hands"));
        add.add(new SkinnedMeshParams("RFootNudeShape", "Feet"));
        add.add(new SkinnedMeshParams("LFootNudeShape", "Feet"));
        add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }
}
