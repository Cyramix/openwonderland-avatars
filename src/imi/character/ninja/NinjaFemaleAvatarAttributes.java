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
package imi.character.ninja;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.character.AttachmentParams;
import imi.character.CharacterAttributes;
import imi.scene.PMatrix;
import java.util.ArrayList;

/**
 * This class represents concrete attribute settings for the NinjaAvatar. It is
 * basically a well-defined CharacterAttributes starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class NinjaFemaleAvatarAttributes extends CharacterAttributes
{
    public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    /** Collection of available default skin-tones **/
    private final ColorRGBA[] skinTones = new ColorRGBA[]
    {
        new ColorRGBA(221.0f / 255.0f,  183.0f / 255.0f, 166.0f / 255.0f, 1),
        new ColorRGBA(203.0f / 255.0f,  142.0f / 255.0f, 114.0f / 255.0f, 1),
        new ColorRGBA(182.0f / 255.0f,  137.0f / 255.0f, 116.0f / 255.0f, 1),
        new ColorRGBA(0.4f, 0.8f, 0.8f, 1), // Just as a joke :)
    };
    private boolean loadedBind = false;
    
    public NinjaFemaleAvatarAttributes(String name, boolean bRandomCustomizations) 
    {
        super(name);
        setGender(2);
        loadDefaultBind();
        
        int feet  = -1;//(int) (Math.random() * 10000 % 0);
        int legs  = (int) (Math.random() * 10000 % 3);  // 1 and 2 problems
        int torso = (int) (Math.random() * 10000 % 5);  // 3 and 3 problems
        int hair  = (int) (Math.random() * 10000 % 53); // 8 is missing, test til 16
        int head  = (int) (Math.random() * 10000 % 2);
        int skin = (int) (Math.random() * 1000000 % skinTones.length);
        setSkinTone(skinTones[skin].r, skinTones[skin].g, skinTones[skin].b);
    }

    public NinjaFemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head)
    {
        this(name, feet, legs, torso, hair, head, 0);
    }
    public NinjaFemaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone)
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
        setSkinTone(skinTones[skinTone].r, skinTones[skinTone].g, skinTones[skinTone].b);
        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
        setAttachmentsInstructions(attachments.toArray(new AttachmentParams[attachments.size()]));
    }

    private void customizeHead(int preset)
    {
        switch (preset)
        {
            default:
                setHeadAttachment("assets/models/collada/Heads/FemaleCaucasian/FemaleCHead.dae");
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
                    load.add(new String("assets/models/collada/Avatars/Female/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));           
            }
        }  
    }

    private void customizeHairPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // load the head, eyes, teeth, and tongue
        load.add(new String("assets/models/collada/Avatars/Female/FemaleCHead.dae"));
        add.add(new SkinnedMeshParams("rightEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("leftEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("UpperTeethShape", "Head"));
        add.add(new SkinnedMeshParams("LowerTeethShape", "Head"));
        add.add(new SkinnedMeshParams("TongueGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HeadGeoShape", "Head"));
        
        PMatrix oreintation = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.05f, 1.05f, 1.05f), Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PigTails", "Head", oreintation));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PigTails", "Head", oreintation));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CulyPigTailz", "Head", oreintation));
            }
            break;
            case 3:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_Bun", "Head", oreintation));
            }
            break;
            case 4:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_Bun", "Head", oreintation));
            }
            break;
            case 5:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("CurlyPonyTail", "Head", oreintation));
            }
            break;
            case 6:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("L_PonyTail", "Head", oreintation));
            }
            break;
            case 7:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_PonyTail", "Head", oreintation));
            }
            break;
            case 8:   // Missing?
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_W_bangs", "Head", oreintation));
            }
            break;
            case 9:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_L", "Head", oreintation));
            }
            break;
            case 10:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_R", "Head", oreintation));
            }
            break;
            case 11:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_pt_center", "Head", oreintation));
            }
            break;
            case 12:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_bang", "Head", oreintation));
            }
            break;
            case 13:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_L", "Head", oreintation));
            }
            break;
            case 14:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_R", "Head", oreintation));
            }
            break;
            case 15:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Layered_pt_center", "Head", oreintation));
            }
            break;
            case 16:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_bangs", "Head", oreintation));
            }
            break;
            case 17:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Right", "Head", oreintation));
            }
            break;
            case 18:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_pt_Left", "Head", oreintation));
            }
            break;
            case 19:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Curly_Mid_Pt", "Head", oreintation));
            }
            break;
            case 20:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_right", "Head", oreintation));
            }
            break;
            case 21:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_pt_left", "Head", oreintation));
            }
            break;
            case 22:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_PT_Center", "Head", oreintation));
            }
            break;
            case 23:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("African_Bangs", "Head", oreintation));
            }
            break;
            case 24:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Long_Dredz", "Head", oreintation));
            }
            break;
            case 25:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Bangs", "Head", oreintation));
            }
            break;
            case 26:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Bangz", "Head", oreintation));
            }
            break;
            case 27:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Center", "Head", oreintation));
            }
            break;
            case 28:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_R", "Head", oreintation));
            }
            break;
            case 29:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Pt_Left", "Head", oreintation));
            }
            break;
            case 30:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Bangz", "Head", oreintation));
            }
            break;
            case 31:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_L", "Head", oreintation));
            }
            break;
            case 32:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_RT", "Head", oreintation));
            }
            break;
            case 33:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_Curly_Center", "Head", oreintation));
            }
            break;
            case 34:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfricanWBangz", "Head", oreintation));
            }
            break;
            case 35:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_pt_L", "Head", oreintation));
            }
            break;
            case 36:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_African_pt_R", "Head", oreintation));
            }
            break;
            case 37:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Med_AfriicanPT_Center", "Head", oreintation));
            }
            break;
            case 38:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("MedDredzz", "Head", oreintation));
            }
            break;
            case 39:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_LBang", "Head", oreintation));
            }
            break;
            case 40:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_Messy", "Head", oreintation));
            }
            break;
            case 41:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_R", "Head", oreintation));
            }
            break;
            case 42:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_African_pt_L", "Head", oreintation));
            }
            break;
            case 43:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_AfricanPT_Center", "Head", oreintation));
            }
            break;
            case 44:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortAfrican_BoB", "Head", oreintation));
            }
            break;
            case 45:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("ShortDredzz", "Head", oreintation));
            }
            break;
            case 46:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Messy", "Head", oreintation));
            }
            break;
            case 47:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Long_Bang", "Head", oreintation));
            }
            break;
            case 48:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_Spikey", "Head", oreintation));
            }
            break;
            case 49:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_BOB", "Head", oreintation));
            }
            break;
            case 50:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_R", "Head", oreintation));
            }
            break;
            case 51:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_L", "Head", oreintation));
            }
            break;
            case 52:
            {
                load.add(new String("assets/models/collada/Hair/FemaleHairPlaceable.dae"));
                attachments.add(new AttachmentParams("Short_PT_Center", "Head", oreintation));
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
                load.add(new String("assets/models/collada/Clothes/Female/FemaleJeansStraight.dae"));
                add.add(new SkinnedMeshParams("JeansShape", "LowerBody"));
            }
            break;
            case 1:
            {
                // Dress pants
                // Name collsiion... need to delete before the load?
                load.add(new String("assets/models/collada/Clothes/Female/FemaleDressPants.dae"));
                add.add(new SkinnedMeshParams("LegsNudeShape", "LowerBody"));
            }
            break;
            case 2:  //  TODO error missing mesh?
            {
                // Shorts
                load.add(new String("assets/models/collada/Clothes/Female/FemaleShorts.dae"));
                add.add(new SkinnedMeshParams("ShortsShape", "LowerBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/Female/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));
            }
        }   
    }

    protected void customizeTorsoPresets(int preset, ArrayList<String> load, ArrayList<SkinnedMeshParams> add, ArrayList<AttachmentParams> attachments)
    {
        // Add the hands 
        load.add(new String("assets/models/collada/Avatars/Female/Female_Hands.dae")); 
        add.add(new SkinnedMeshParams("HandsShape",  "Hands"));

        switch(preset)
        {
            case 0:
            {
                // Dress Shirt
                load.add(new String("assets/models/collada/Clothes/Female/FemaleDressShirt.dae"));
                add.add(new SkinnedMeshParams("ShirtMeshShape", "UpperBody"));
            }
            break;
            case 1:
            {
                // Sweater
                load.add(new String("assets/models/collada/Clothes/Female/FemaleSweaterCrew.dae"));
                add.add(new SkinnedMeshParams("SweaterShape", "UpperBody"));
            }
            break;
            case 2:
            {
                // Jacket
                load.add(new String("assets/models/collada/Clothes/Female/FemaleJacket.dae"));
                add.add(new SkinnedMeshParams("Jacket1Shape", "UpperBody"));
            }
            break;
            case 3:      //    TODO doesnt show?
            {
                // Blouse
                load.add(new String("assets/models/collada/Clothes/Female/FemaleBlouse.dae"));
                add.add(new SkinnedMeshParams("TShirtShape", "UpperBody"));
            }
            break;
            case 4:
            {
                // T Shirt            TODO doesnt show?
                load.add(new String("assets/models/collada/Clothes/Female/FemaleTShirt.dae"));
                add.add(new SkinnedMeshParams("TShirtShape", "UpperBody"));
            }
            break;
            default:
            {
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(new String("assets/models/collada/Avatars/Female/Female_Bind.dae")); 
                }
                add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
            }
        }   
    }

    private void loadDefaultBind() {
        ArrayList<String> load      = new ArrayList<String>();
        load.add(new String("assets/models/collada/Avatars/Female/Female_Bind.dae")); // change!

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("rightEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("leftEyeGeoShape", "Head"));
        add.add(new SkinnedMeshParams("UpperTeethShape", "Head"));
        add.add(new SkinnedMeshParams("LowerTeethShape", "Head"));
        add.add(new SkinnedMeshParams("TongueGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HeadGeoShape", "Head"));
        add.add(new SkinnedMeshParams("HandsShape",  "Hands"));
        add.add(new SkinnedMeshParams("ShoesShape",  "Feet"));
        add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody"));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody"));


        setLoadInstructions(load);
        setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
    }
}
