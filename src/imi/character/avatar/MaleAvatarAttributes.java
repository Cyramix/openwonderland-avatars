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
import java.net.URL;
import java.util.ArrayList;

/**
 * This class represents concrete attribute settings for the avatarAvatar. It is
 * basically a well-defined CharacterAttributes starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class MaleAvatarAttributes extends CharacterAttributes
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
    
    /**
     * Construct a new attributes instance.
     * @param name The name of the avatar
     * @param bRandomCustomizations If false, avatar starts in the bind pose, if true then random clothing will be applied
     */
    public MaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
        // Customizations
        if (bRandomCustomizations)
        {
            int preset        = -1;
            int numberOfFeet  = 4;
            int numberOfLegs  = 4;
            int numberOfTorso = 6;
            int numberOfHair  = 17;
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
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0, 0);
    }
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone)
    {
        this(name, feet, legs, torso, hair, head, skinTone, 0);
    }
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor)
    {
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
        if (skinTone < skinTones.length)
            setSkinTone(skinTones[skinTone].r, skinTones[skinTone].g, skinTones[skinTone].b);
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
//            case 0:
//                setHeadAttachment("assets/models/collada/Heads/MaleHead/MaleCHead.dae");
//                break;
//            case 1:
//                setHeadAttachment("assets/models/collada/Heads/MaleHead/blackHead.dae");
//                break;
//            case 2:
//                setHeadAttachment("assets/models/collada/Heads/MaleHead/midAgeGuy.dae");
//                break;
//            case 3:
//                setHeadAttachment("assets/models/collada/Heads/MaleHead/asiaHead.dae");
//                break;
            default:
                setHeadAttachment("assets/models/collada/Heads/MaleHead/MaleCHead.dae");
        }
    }

    private void customizeFeetPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Tennis shoes
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleTennisShoes.dae"));
                add.add(new SkinnedMeshParams("TennisShoesShape", "Feet"));
            }
            break;
            case 1:
            {
                // Flip flops
                load.add(new String("assets/models/collada/Clothing/MaleClothing/FlipFlopsFeet.dae"));
                add.add(new SkinnedMeshParams("LFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("RFootNudeShape", "Feet"));
                add.add(new SkinnedMeshParams("LFlipFlopShape", "Feet"));
                add.add(new SkinnedMeshParams("RFlipFlopShape", "Feet"));
            }
            break;
            case 2:
            {
                // Dress shoes
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleDressShoes.dae"));
                add.add(new SkinnedMeshParams("polySurfaceShape3", "Feet"));
            }
            break;
            case 3:
            {
                // Cowboy boots
                load.add(new String("assets/models/collada/Clothing/MaleClothing/CowBoyBoot_CUT.dae"));
                add.add(new SkinnedMeshParams("CowBoyBootsShape", "Feet"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae"));
                }
                add.add(new SkinnedMeshParams("RFootNudeShape",  "Feet"));
                add.add(new SkinnedMeshParams("LFootNudeShape",  "Feet"));
            }
        }   
    }

    private void customizeHairPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        randomizeHairColor();
        PMatrix orientation = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("curly", "Head", orientation, "Hair"));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("S_pt_right", "Head", orientation, "Hair"));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("S_pt_left", "Head", orientation, "Hair"));
            }
            break;
            case 3:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_slickedbk", "Head", orientation, "Hair"));
            }
            break;
            case 4:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_pt_Right", "Head", orientation, "Hair"));
            }
            break;
            case 5:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_pt_left", "Head", orientation, "Hair"));
            }
            break;
            case 6:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_center", "Head", orientation, "Hair"));
            }
            break;
            case 7:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("LPonyTail", "Head", orientation, "Hair"));
            }
            break;
            case 8:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Spiky", "Head", orientation, "Hair"));
            }
            break;
            case 9:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Faux", "Head", orientation, "Hair"));
            }
            break;
            case 10:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Bald", "Head", orientation, "Hair"));
            }
            break;
            case 11:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Balding", "Head", orientation, "Hair"));
            }
            break;
            case 12:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_ShavedShort", "Head", orientation, "Hair"));
            }
            break;
            case 13:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Normal", "Head", orientation, "Hair"));
            }
            break;
            case 14:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", orientation, "Hair"));
            }
            break;
            case 15:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_AfAmShortCrew", "Head", orientation, "Hair"));
            }
            break;
            case 16:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_AfAmLngCrew", "Head", orientation, "Hair"));
            }
            break;
        }   
    }

    private void customizeLegsPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
            case 0:
            {
                // Jeans
                load.add(new String("assets/models/collada/Clothing/MaleClothing/Jeans.dae"));
                add.add(new SkinnedMeshParams("polySurface3Shape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Shorts
                load.add(new String("assets/models/collada/Clothing/MaleClothing/Shorts.dae"));
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("MaleShortsShape", "LowerBody"));
            }
            break;
            case 2:
            {
                // Dress pants
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleDressPants1.dae"));
                add.add(new SkinnedMeshParams("Legs_LegsNudeShape", "LowerBody"));
            }
            break;
            case 3:
            {
                // Suite pants
                load.add(new String("assets/models/collada/Clothing/MaleClothing/Slacks.dae"));
                add.add(new SkinnedMeshParams("SuitPantsShape", "LowerBody"));
            }
            break;
            case 4:
            {
                // Meso
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleMesoBottom.dae"));
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
                add.add(new SkinnedMeshParams("polySurfaceShape6", "LowerBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae"));
                }
                add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));
            }
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        randomizeShirtColor();
        // Add the hands in either way
        load.add(new String("assets/models/collada/Avatars/MaleAvatar/Male_Hands.dae")); // change!
        add.add(new SkinnedMeshParams("RHandShape",  m_regions[1]));
        add.add(new SkinnedMeshParams("LHandShape",  m_regions[1]));

        switch(preset)
        {
            case 0:
            {
                // T Shirt
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleTShirt.dae"));
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("ArmsShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Polo Strips
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MalePolo.dae"));
                add.add(new SkinnedMeshParams("PoloShape", "UpperBody"));
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Dress shirt
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleDressShirt.dae"));
                add.add(new SkinnedMeshParams("DressShirtShape", "UpperBody"));
            }
            break;
            case 3:
            {
                // Sweater
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleSweater.dae"));
                add.add(new SkinnedMeshParams("SweaterMaleShape", "UpperBody"));
            }
            break;
            case 4:
            {
                // Dress shirt for suit
                load.add(new String("assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae"));
                add.add(new SkinnedMeshParams("SuitShirtShape", "UpperBody"));
            }
            break;
            case 5:
            {
                // Suit Jacket
                load.add(new String("assets/models/collada/Clothing/MaleClothing/SuitJacket.dae"));
                add.add(new SkinnedMeshParams("SuitJacketShape", "UpperBody"));
            }
            break;
            case 6:
            {
                // Meso
                load.add(new String("assets/models/collada/Clothing/MaleClothing/MaleMesoTop.dae"));
                add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody"));
                add.add(new SkinnedMeshParams("polySurfaceShape2", "UpperBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae"));
                }
                add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
            }
        }   
    }

    protected void loadDefaultBindPose()
    {
        ArrayList<String> load      = new ArrayList<String>();
        load.add(new String("assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae"));
        loadedBind = true;

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("RHandShape",  "Hands"));
        add.add(new SkinnedMeshParams("LHandShape",  "Hands"));
        add.add(new SkinnedMeshParams("RFootNudeShape",  "Feet"));
        add.add(new SkinnedMeshParams("LFootNudeShape",  "Feet"));
        add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }
}
