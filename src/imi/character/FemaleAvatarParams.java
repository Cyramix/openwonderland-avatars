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
package imi.character;

import imi.scene.PMatrix;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastList;

/**
 * This class represents concrete attribute settings for the Avatar. It is
 * basically a well-defined CharacterParams starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class FemaleAvatarParams extends CharacterParams
{
    /** number of preset features **/
    private enum PresetNumbers {
        NumberOfFeet(3),
        NumberOfLegs(3),
        NumberOfTorsos(4),
        NumberOfHairs(29),
        NumberOfHeads(7),
        NumberOfSkinTones(skinTones.length),
        NumberOfEyeColors(eyeTextures.length);

        final int count;
        PresetNumbers(int value) {
            count = value;
        }
    }

    /** Used to indicate that the bind pose file has already been added to the
     load instructions **/
    private boolean loadedBind = false;

    /** Used during the building process **/
    private transient ConfigurationContext configContext = null;


    /**
     * You must call DoneBuilding() before using these params!
     * @param name
     */
    public FemaleAvatarParams(String name)
    {
        super(name);
        setGender(2);
    }

    /**
     * Set the head to one determined by the provided integer.
     * Some heads set skin tone (are not compatible with arbitrary skin tone),
     * so calling this method in "order" (not before anything that changes skin tone)
     * might be important.
     * @param preset
     */
    private void customizeHead(int preset)
    {
        switch (preset)
        {
            case 0:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(255.0f / 255.0f, 139.0f / 255.0f, 95.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 1:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/AsianFemaleHead.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(255.0f / 255.0f, 139.0f / 255.0f, 95.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 2:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleAAHead.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(242.0f / 255.0f, 159.0f / 255.0f, 122.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 3:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleHispanicHead.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(242.0f / 255.0f, 159.0f / 255.0f, 122.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 4:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female_AF_Head02.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 5:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_FemaleLowPoly_01.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 6:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_FemaleHead01.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 7:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female02HighPoly.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(129.0f / 255.0f, 65.0f / 255.0f, 38.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            case 8:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FG_Female01HighPoly.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f);
                setApplySkinToneOnHead(false);
                break;
            default:
                setHeadAttachment("assets/models/collada/Heads/FemaleHead/FemaleCHead.bhf");
                setUsePhongLightingForHead(true);
                setSkinTone(255.0f / 255.0f, 139.0f / 255.0f, 95.0f / 255.0f);
                setApplySkinToneOnHead(false);
        }
    }

    /**
     * Set the feet to a set determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeFeetPresets(int preset, List<String> load, List<SkinnedMeshParams> add)
    {
        String footFile = null;

        switch(preset)
        {
            case 0:
            {
                // Closed to dress shoes
                footFile   = "assets/models/collada/Clothing/FemaleClothing/Female_ClosedToeDressShoes.dae";
                load.add(footFile);
                add.add(new SkinnedMeshParams("Female_ClosedToeDressShoes_Female_DressClosedToe_ShoesShape", "Feet", footFile));
            }
            break;
            case 1:
            {
                // Converse shoes
                footFile   = "assets/models/collada/Clothing/FemaleClothing/Female_ConverseShoes.dae";
                load.add(footFile);
                add.add(new SkinnedMeshParams("Female_ConverseShoes_Female_ConverseShoeShape", "Feet", footFile));
            }
            break;
            case 2:
            {
                // Flip flops
                footFile   = "assets/models/collada/Clothing/FemaleClothing/FemaleFlipFlops.dae";
                load.add(footFile);
                add.add(new SkinnedMeshParams("FlipFlopsFemaleShape", "Feet", footFile));
                add.add(new SkinnedMeshParams("FemaleFeet_NudeShape", "Feet", footFile));
            }
            break;
            default:
            {
                footFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(footFile);
                }
                add.add(new SkinnedMeshParams("ShoesShape",  "Feet", footFile));
            }
        }
            
    }

    /**
     * Set the legs to a set determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeLegsPresets(int preset,  List<String> load, List<SkinnedMeshParams> add)
    {
        String legFile = null;

        switch(preset)
        {
            case 0:
            {
                // Jeans
                legFile   = "assets/models/collada/Clothing/FemaleClothing/FemaleJeansStraight.dae";
                load.add(legFile);
                add.add(new SkinnedMeshParams("JeansShape", "LowerBody", legFile));
                setPantsColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
            break;
            case 1:
            {
                // Dress pants
                legFile   = "assets/models/collada/Clothing/FemaleClothing/FemaleDressPants.dae";
                load.add(legFile);
                add.add(new SkinnedMeshParams("PantsFemaleShape", "LowerBody", legFile));
            }
            break;
            case 2:
            {
                // Shorts
                legFile   = "assets/models/collada/Clothing/FemaleClothing/FemaleShorts.dae";
                load.add(legFile);
                add.add(new SkinnedMeshParams("Legs_NudeShape", "LowerBody", legFile));
                add.add(new SkinnedMeshParams("ShortsShape", "LowerBody", legFile));
            }
            break;
            default:
            {
                legFile   = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(legFile);
                }
                add.add(new SkinnedMeshParams("Legs_NudeShape",  "LowerBody", legFile));
            }
        }
            
    }

    /**
     * Set the torso to one determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    protected void customizeTorsoPresets(int preset, List<String> load, List<SkinnedMeshParams> add, List<AttachmentParams> attachments)
    {
        // Add the hands
        String handFile  = "assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae";
        
        load.add(handFile);
        add.add(new SkinnedMeshParams("Hands_NudeShape",  "Hands", handFile));

        String torsoFile = null;

        switch(preset)
        {
            case 0:
            {
                // Dress Shirt
                torsoFile  = "assets/models/collada/Clothing/FemaleClothing/FemaleDressShirt.dae";
                load.add(torsoFile);
                add.add(new SkinnedMeshParams("ShirtMeshShape", "UpperBody", torsoFile));
            }
            break;
            case 1:
            {
                // Sweater
                torsoFile = "assets/models/collada/Clothing/FemaleClothing/FemaleSweaterCrew.dae";
                load.add(torsoFile);
                add.add(new SkinnedMeshParams("SweaterShape", "UpperBody", torsoFile));
            }
            break;
            case 2:
            {
                // Jacket
                torsoFile = "assets/models/collada/Clothing/FemaleClothing/FemaleJacket.dae";
                load.add(torsoFile);
                add.add(new SkinnedMeshParams("Jacket1Shape", "UpperBody", torsoFile));
                setShirtColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
            break;
            case 3:      
            {
                // Blouse       
                torsoFile  = "assets/models/collada/Clothing/FemaleClothing/FemaleBlouse.dae";
                load.add(torsoFile);
                add.add(new SkinnedMeshParams("TShirt1Shape", "UpperBody", torsoFile));
                add.add(new SkinnedMeshParams("Arms_NudeShape", "UpperBody", torsoFile));
                setShirtColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            }
            break; 
            default:
            {
                torsoFile  = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
                if(!loadedBind)
                {
                    loadedBind = true;
                    load.add(torsoFile);
                }
                add.add(new SkinnedMeshParams("Torso_NudeShape",  "UpperBody", torsoFile));
            }
        }
    }

    /**
     * Set the hair to one determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeHairPresets(int preset, List<String> load, List<SkinnedMeshParams> add, List<AttachmentParams> attachments)
    {
        String hairFile = null;

        PMatrix orientation = PMatrix.IDENTITY;
        switch(preset)
        {
            case 0:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("M_PigTailsShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 1:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("CulyPigTailzShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 2:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("L_BunShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 3:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("M_BunShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 4:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("CurlyPonyTailShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 5:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("L_PonyTailShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 6:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("M_PonyTailShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 7:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Long_W_bangsShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 8:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Layered_bangShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 9:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Layered_pt_LShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 10:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Layered_pt_RShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 11:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Layered_pt_centerShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 12:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Curly_bangsShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 13:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Culry_pt_RightShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 14:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Culry_pt_LeftShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 15:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Curly_Mid_PtShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 16:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Long_DredzShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 17:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_Pt_BangzShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 18:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_pt_CenterShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 19:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_Curly_BangzShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 20:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_AfricanWBangzShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 21:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_African_Pt_RShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 22:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_AfricanPT_CenterShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 23:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Med_African_Pt_LShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 24:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Short_African_MessyShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 25:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("short_AfricanPT_CenterShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 26:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Short_PT_RShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 27:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Short_PT_LShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 28:
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae";
                attachments.add(new AttachmentParams("Short_PT_CenterShape", "HairAttach", orientation, "HairAttachmentJoint", hairFile));
            }
            break;
            case 53: // SPECIAL SKINNED HAIR
            {
                hairFile   = "assets/models/collada/Hair/FemaleHair/FemaleFGHair.dae";
                add.add(new SkinnedMeshParams("HairAShape1", "Head", hairFile));
            }
            break;
            default:
                // do nothing
        }
        if (hairFile != null)
            load.add(hairFile);
    }

    /**
     * Load all the defaults for the female avatar
     */
    private void loadDefaultBindPose() {
        String bindPoseFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
        String handFile     = "assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae";
        addLoadInstruction(handFile);
        addLoadInstruction(bindPoseFile);

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("Hands_NudeShape",        "Hands", handFile));
        add.add(new SkinnedMeshParams("FemaleFeet_NudeShape",   "Feet", bindPoseFile));
        add.add(new SkinnedMeshParams("Torso_NudeShape",        "UpperBody", bindPoseFile));
        add.add(new SkinnedMeshParams("Legs_NudeShape",         "LowerBody", bindPoseFile));

        setAddInstructions(add);
    }

    /////////////////////////////////////////////////
    //////////// Helpful builder pattern ////////////
    ////////////////////////////////////////////////

    /**
     * Finalizes the configuration process, if any configurations have not been
     * set until now, it will be set with a random preset.
     * <p>This method must be called at the end of all of the configure* calls!<p>
     * @return
     */
    public FemaleAvatarParams build() {
        return build(true);
    }
    
    /**
     * Finalizes the configuration process, if any configurations have not been
     * set until now, it will be set to preset 0 or a random one.
     * <p>This method must be called at the end of all of the configure* calls!<p>
     * @param randomizeUnasignedElements
     * @return
     */
    public FemaleAvatarParams build(boolean randomizeUnasignedElements) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        if (randomizeUnasignedElements)
        {
            if (!configContext.hairConfigured)
                configureHair((int)((Math.random() * 10000.0f) % PresetNumbers.NumberOfHairs.count));
            if (!configContext.headConfigured)
                configureHead((int)((Math.random() * 10000.0f) % PresetNumbers.NumberOfHeads.count));
            if (!configContext.torsoConfigured)
                configureTorso((int)((Math.random() * 10000.0f) % PresetNumbers.NumberOfTorsos.count));
            if (!configContext.legsConfigured)
                configureLegs((int)((Math.random() * 10000.0f) % PresetNumbers.NumberOfLegs.count));
            if (!configContext.feetConfigured)
                configureFeet((int)((Math.random() * 10000.0f) % PresetNumbers.NumberOfFeet.count));
        }
        else
        {
            if (!configContext.hairConfigured)
                configureHair(0);
            if (!configContext.headConfigured)
                configureHead(0);
            if (!configContext.torsoConfigured)
                configureTorso(0);
            if (!configContext.legsConfigured)
                configureLegs(0);
            if (!configContext.feetConfigured)
                configureFeet(0);
        }
        setLoadInstructions(configContext.load);
        setAddInstructions(configContext.add);
        setAttachmentsInstructions(configContext.attachments);
        return this;
    }

    /**
     * Finalizes the configuration process, if any configurations have not been
     * set then it will not be loaded.
     * @return
     */
    public FemaleAvatarParams buildSpecific() {
        setLoadInstructions(configContext.load);
        setAddInstructions(configContext.add);
        setAttachmentsInstructions(configContext.attachments);
        return this;
    }

    /**
     * Configure the head to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public FemaleAvatarParams configureHead(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeHead(presetValue);
        configContext.headConfigured = true;
        return this;
    }

    /**
     * Configure the hair to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public FemaleAvatarParams configureHair(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeHairPresets(presetValue, configContext.load, configContext.add, configContext.attachments);
        configContext.hairConfigured = true;
        return this;
    }

    /**
     * Configure the feet to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public FemaleAvatarParams configureFeet(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeFeetPresets(presetValue, configContext.load, configContext.add);
        configContext.feetConfigured = true;
        return this;
    }

    /**
     * Configure the leds to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public FemaleAvatarParams configureLegs(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeLegsPresets(presetValue, configContext.load, configContext.add);
        configContext.legsConfigured = true;
        return this;
    }

    /**
     * Configure the torso to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public FemaleAvatarParams configureTorso(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeTorsoPresets(presetValue, configContext.load, configContext.add, configContext.attachments);
        configContext.torsoConfigured = true;
        return this;
    }

    /**
     * Private helper for post-construction config
     */
    private class ConfigurationContext {
        final List<String> load = new FastList<String>();
        final List<SkinnedMeshParams> add = new FastList<SkinnedMeshParams>();
        final List<AttachmentParams> attachments = new FastList<AttachmentParams>();
        boolean hairConfigured  = false;
        boolean headConfigured  = false;
        boolean torsoConfigured = false;
        boolean legsConfigured  = false;
        boolean feetConfigured  = false;
        public ConfigurationContext() {
            randomizeSkinTone();
            randomizeHairColor();
            randomizeShirtColor();
            randomizePantColor();
            randomizeShoesColor();
            randomizeEyeballTexture();
        }
        void clear() {
            load.clear();
            add.clear();
            attachments.clear();
            hairConfigured  = false;
            headConfigured  = false;
            torsoConfigured = false;
            legsConfigured  = false;
            feetConfigured  = false;
        }
    }
}
