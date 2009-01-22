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
        this(name, feet, legs, torso, hair, head, 0);
    }
    public MaleAvatarAttributes(String name, int feet, int legs, int torso, int hair, int head, int skinTone)
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
        
    }

    private void customizeHead(int preset)
    {
        switch (preset)
        {
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
        PMatrix orientation = new PMatrix(new Vector3f(0,0,0), new Vector3f(1, 1.05f, 1.06f), Vector3f.ZERO);
        switch(preset)
        {
            case 0:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("curly", "Head", orientation));
            }
            break;
            case 1:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("S_pt_right", "Head", orientation));
            }
            break;
            case 2:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("S_pt_left", "Head", orientation));
            }
            break;
            case 3:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_slickedbk", "Head", orientation));
            }
            break;
            case 4:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_pt_Right", "Head", orientation));
            }
            break;
            case 5:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_pt_left", "Head", orientation));
            }
            break;
            case 6:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("M_center", "Head", orientation));
            }
            break;
            case 7:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("LPonyTail", "Head", orientation));
            }
            break;
            case 8:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Spiky", "Head", orientation));
            }
            break;
            case 9:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Faux", "Head", orientation));
            }
            break;
            case 10:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Bald", "Head", orientation));
            }
            break;
            case 11:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Balding", "Head", orientation));
            }
            break;
            case 12:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_ShavedShort", "Head", orientation));
            }
            break;
            case 13:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_Normal", "Head", orientation));
            }
            break;
            case 14:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_ShavedFlatTop", "Head", orientation));
            }
            break;
            case 15:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_AfAmShortCrew", "Head", orientation));
            }
            break;
            case 16:
            {
                load.add(new String("assets/models/collada/Hair/MaleHair/HairPlaceable.dae"));
                attachments.add(new AttachmentParams("Hair_AfAmLngCrew", "Head", orientation));
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
