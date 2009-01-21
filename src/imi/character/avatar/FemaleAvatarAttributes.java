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
        new ColorRGBA(0.4f, 0.8f, 0.8f, 1), // Just as a joke :)
    };
    private boolean loadedBind = false;
    
    public FemaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
        super(name);
        setGender(2);
        customizeHead(0);
        loadDefaultBind();

        int feet  = -1;//(int) (Math.random() * 10000 % 0);
        int legs  = (int) (Math.random() * 10000 % 3);  // 1 and 2 problems
        int torso = (int) (Math.random() * 10000 % 5);  // 3 and 3 problems
        int hair  = (int) (Math.random() * 10000 % 53); // 8 is missing, test til 16
        int head  = (int) (Math.random() * 10000 % 2);
        int skin  = (int) (Math.random() * 10000 % 2);
        setSkinTone(skinTones[skin].r, skinTones[skin].g, skinTones[skin].b);
    }

    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0);
    }
    public FemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skin)
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
        setSkinTone(skinTones[skin].r, skinTones[skin].g, skinTones[skin].b);

        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
        setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
    }

    private void customizeHead(int preset)
    {
        switch (preset)
        {
            default:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.dae");
        }
    }

    private void customizeFeetPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        switch(preset)
        {
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

    private void customizeHairPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {   
        PMatrix orientation = new PMatrix(new Vector3f(0,0,0), new Vector3f(1, 1.05f, 1.06f), Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PigTails", "Head", orientation));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PigTails", "Head", orientation));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CulyPigTailz", "Head", orientation));
            }
            break;
            case 3:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_Bun", "Head", orientation));
            }
            break;
            case 4:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_Bun", "Head", orientation));
            }
            break;
            case 5:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CurlyPonyTail", "Head", orientation));
            }
            break;
            case 6:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PonyTail", "Head", orientation));
            }
            break;
            case 7:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PonyTail", "Head", orientation));
            }
            break;
            case 8:   
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_Center", "Head", orientation));
            }
            break;
            case 9:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_L", "Head", orientation));
            }
            break;
            case 10:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_R", "Head", orientation));
            }
            break;
            case 11:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_center", "Head", orientation));
            }
            break;
            case 12:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_bang", "Head", orientation));
            }
            break;
            case 13:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_L", "Head", orientation));
            }
            break;
            case 14:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_R", "Head", orientation));
            }
            break;
            case 15:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_center", "Head", orientation));
            }
            break;
            case 16:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_bangs", "Head", orientation));
            }
            break;
            case 17:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Right", "Head", orientation));
            }
            break;
            case 18:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Left", "Head", orientation));
            }
            break;
            case 19:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_Mid_Pt", "Head", orientation));
            }
            break;
            case 20:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_right", "Head", orientation));
            }
            break;
            case 21:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_left", "Head", orientation));
            }
            break;
            case 22:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_PT_Center", "Head", orientation));
            }
            break;
            case 23:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_Bangs", "Head", orientation));
            }
            break;
            case 24:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_Dredz", "Head", orientation));
            }
            break;
            case 25:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Bangs", "Head", orientation));
            }
            break;
            case 26:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Bangz", "Head", orientation));
            }
            break;
            case 27:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Center", "Head", orientation));
            }
            break;
            case 28:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_R", "Head", orientation));
            }
            break;
            case 29:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Left", "Head", orientation));
            }
            break;
            case 30:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Bangz", "Head", orientation));
            }
            break;
            case 31:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_L", "Head", orientation));
            }
            break;
            case 32:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_RT", "Head", orientation));
            }
            break;
            case 33:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Center", "Head", orientation));
            }
            break;
            case 34:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfricanWBangz", "Head", orientation));
            }
            break;
            case 35:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_pt_L", "Head", orientation));
            }
            break;
            case 36:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_pt_R", "Head", orientation));
            }
            break;
            case 37:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfriicanPT_Center", "Head", orientation));
            }
            break;
            case 38:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("MedDredzz", "Head", orientation));
            }
            break;
            case 39:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_LBang", "Head", orientation));
            }
            break;
            case 40:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_Messy", "Head", orientation));
            }
            break;
            case 41:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_R", "Head", orientation));
            }
            break;
            case 42:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_L", "Head", orientation));
            }
            break;
            case 43:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_AfricanPT_Center", "Head", orientation));
            }
            break;
            case 44:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortAfrican_BoB", "Head", orientation));
            }
            break;
            case 45:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortDredzz", "Head", orientation));
            }
            break;
            case 46:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Messy", "Head", orientation));
            }
            break;
            case 47:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Long_Bang", "Head", orientation));
            }
            break;
            case 48:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Spikey", "Head", orientation));
            }
            break;
            case 49:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_BOB", "Head", orientation));
            }
            break;
            case 50:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_R", "Head", orientation));
            }
            break;
            case 51:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_L", "Head", orientation));
            }
            break;
            case 52:
            {
                // Missing?
                load.add(new String("assets/models/collada/Hair/FemaleHair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_W_bangs", "Head", orientation));
            }
            break;
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
            case 2:  //  TODO error missing mesh?
            {
                // Shorts
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleShorts.dae"));
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
                add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));
            }
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // Add the hands 
        load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae")); 
        add.add(new SkinnedMeshParams("HandsShape",  "Hands"));

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
            case 3:      //    TODO doesnt show?
            {
                // Blouse
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleBlouse.dae"));
                add.add(new SkinnedMeshParams("TShirtShape", "UpperBody"));
            }
            break;
            case 4:
            {
                // T Shirt            TODO doesnt show?
                load.add(new String("assets/models/collada/Clothing/FeamleClothing/FemaleTShirt.dae"));
                add.add(new SkinnedMeshParams("TShirtShape", "UpperBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
            }
        }   
    }

    private void loadDefaultBind() {
        ArrayList<String> load      = new ArrayList<String>();
        load.add(new String("assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae")); // change!

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("HandsShape",  "Hands"));
        add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));
        add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }
}
