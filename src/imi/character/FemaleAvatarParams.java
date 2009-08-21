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

import com.jme.renderer.ColorRGBA;
import imi.scene.PMatrix;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastList;
import javolution.util.FastTable;

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
        if (preset < FemaleDefaults.headPresetsFileNames.size() && preset < FemaleDefaults.headPresetsPhongLighting.size() && preset < FemaleDefaults.headPresetsSkinTone.size() && preset >= 0)
        {
            String headFile = FemaleDefaults.headPresetsFileNames.get(preset);
            setHeadAttachment(headFile);
            setUsePhongLightingForHead(FemaleDefaults.headPresetsPhongLighting.get(preset));
            ColorRGBA skint = FemaleDefaults.headPresetsSkinTone.get(preset);
            if (skint != null)
            {
                setSkinTone(skint.r, skint.g, skint.b);
                setApplySkinToneOnHead(false);
            }
        }
        else
            throw new RuntimeException("Invalid preset " + preset);
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
        if (preset < FemaleDefaults.feetPresetsFileNames.size() && preset < FemaleDefaults.feetPresetsMeshNames.size() && preset < FemaleDefaults.feetPresetsDisableColorModulation.size() && preset >= 0)
        {
            String feetFile = FemaleDefaults.feetPresetsFileNames.get(preset);
            load.add(feetFile);
            for (int i = 0; i < FemaleDefaults.feetPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(FemaleDefaults.feetPresetsMeshNames.get(preset).get(i), "Feet", feetFile));
            if (FemaleDefaults.feetPresetsDisableColorModulation.get(preset))
                setShoesColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);

//            String footFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(footFile);
//            }
//            add.add(new SkinnedMeshParams("ShoesShape",  "Feet", footFile));
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
        if (preset < FemaleDefaults.legsPresetsFileNames.size() && preset < FemaleDefaults.legsPresetsMeshNames.size() && preset < FemaleDefaults.legsPresetsDisableColorModulation.size() && preset >= 0)
        {
            String legsFile = FemaleDefaults.legsPresetsFileNames.get(preset);
            load.add(legsFile);
            for (int i = 0; i < FemaleDefaults.legsPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(FemaleDefaults.legsPresetsMeshNames.get(preset).get(i), "LowerBody", legsFile));
            if (FemaleDefaults.legsPresetsDisableColorModulation.get(preset))
                setShoesColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);

//            String legFile   = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(legFile);
//            }
//            add.add(new SkinnedMeshParams("Legs_NudeShape",  "LowerBody", legFile));
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

        if (preset < FemaleDefaults.torsoPresetsFileNames.size() && preset < FemaleDefaults.torsoPresetsMeshNames.size() && preset < FemaleDefaults.torsoPresetsDisableColorModulation.size() && preset >= 0)
        {
            String torsoFile = FemaleDefaults.torsoPresetsFileNames.get(preset);
            load.add(torsoFile);
            for (int i = 0; i < FemaleDefaults.torsoPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(FemaleDefaults.torsoPresetsMeshNames.get(preset).get(i), "UpperBody", torsoFile));
            if (FemaleDefaults.torsoPresetsDisableColorModulation.get(preset))
                setShirtColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);

//            String torsoFile  = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(torsoFile);
//            }
//            add.add(new SkinnedMeshParams("Torso_NudeShape",  "UpperBody", torsoFile));
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
        if (preset < FemaleDefaults.hairPresetsFileNames.size() && preset < FemaleDefaults.hairPresetsMeshNames.size() && preset >= 0)
        {
            String hairFile = FemaleDefaults.hairPresetsFileNames.get(preset);
            String meshName = FemaleDefaults.hairPresetsMeshNames.get(preset);
            load.add(hairFile);
            // Special case for the skinned hair
            if (meshName.equals("HairAShape1"))
                add.add(new SkinnedMeshParams(meshName, "Head", hairFile));
            else
                attachments.add(new AttachmentParams(meshName, "HairAttach", PMatrix.IDENTITY, "HairAttachmentJoint", hairFile));
        }
        else
            throw new RuntimeException("Invalid preset " + preset);
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
                configureHair((int)((Math.random() * 10000.0f) % getNumberOfHairPresets()));
            if (!configContext.headConfigured)
                configureHead((int)((Math.random() * 10000.0f) % getNumberOfHeadPresets()));
            if (!configContext.torsoConfigured)
                configureTorso((int)((Math.random() * 10000.0f) % getNumberOfTorsoPresets()));
            if (!configContext.legsConfigured)
                configureLegs((int)((Math.random() * 10000.0f) % getNumberOfLegsPresets()));
            if (!configContext.feetConfigured)
                configureFeet((int)((Math.random() * 10000.0f) % getNumberOfFeetPresets()));
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
        setValid(true);
        return this;
    }

    /**
     * Finalizes the configuration process, if any configurations have not been
     * set then it will not be loaded.
     * @return
     */
    public FemaleAvatarParams buildSpecific() {
        if (configContext != null) {
            setLoadInstructions(configContext.load);
            setAddInstructions(configContext.add);
            setAttachmentsInstructions(configContext.attachments);
        }
        setValid(true);
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

    public FastTable<String> getHairPresetsFileNames() {
        return FemaleDefaults.hairPresetsFileNames;
    }

    public FastTable<String> getHairPresetsMeshNames() {
        return FemaleDefaults.hairPresetsMeshNames;
    }

    public int getNumberOfHairPresets() {
        return FemaleDefaults.hairPresetsMeshNames.size();
    }

    public FastTable<String> getHeadPresetsFileNames() {
        return FemaleDefaults.headPresetsFileNames;
    }

    public FastTable<Boolean> getHeadPresetsPhongLighting() {
        return FemaleDefaults.headPresetsPhongLighting;
    }

    public FastTable<ColorRGBA> getHeadPresetsSkinTone() {
        return FemaleDefaults.headPresetsSkinTone;
    }

    public int getNumberOfHeadPresets() {
        return FemaleDefaults.headPresetsFileNames.size();
    }

    public FastTable<String> getTorsoPresetsFileNames() {
        return FemaleDefaults.torsoPresetsFileNames;
    }

    public FastTable<FastTable<String>> getTorsoPresetsMeshNames() {
        return FemaleDefaults.torsoPresetsMeshNames;
    }

    public int getNumberOfTorsoPresets() {
        return FemaleDefaults.torsoPresetsFileNames.size();
    }

    public FastTable<Boolean> getFeetPresetsDisableColorModulation() {
        return FemaleDefaults.feetPresetsDisableColorModulation;
    }

    public FastTable<String> getFeetPresetsFileNames() {
        return FemaleDefaults.feetPresetsFileNames;
    }

    public FastTable<FastTable<String>> getFeetPresetsMeshNames() {
        return FemaleDefaults.feetPresetsMeshNames;
    }

    public FastTable<String> getHairPresetsColladaFileNames() {
        return FemaleDefaults.hairPresetsFileNames;
    }

    public FastTable<Boolean> getLegsPresetsDisableColorModulation() {
        return FemaleDefaults.legsPresetsDisableColorModulation;
    }

    public FastTable<String> getLegsPresetsFileNames() {
        return FemaleDefaults.legsPresetsFileNames;
    }

    public FastTable<FastTable<String>> getLegsPresetsMeshNames() {
        return FemaleDefaults.legsPresetsMeshNames;
    }

    public FastTable<Boolean> getTorsoPresetsDisableColorModulation() {
        return FemaleDefaults.torsoPresetsDisableColorModulation;
    }

    public int getNumberOfLegsPresets() {
        return FemaleDefaults.legsPresetsFileNames.size();
    }

    public int getNumberOfFeetPresets() {
        return FemaleDefaults.feetPresetsFileNames.size();
    }

    // holder for the defaults
    public static class FemaleDefaults
    {
        public static final FastTable<String> hairPresetsFileNames = new FastTable<String>();
        public static final FastTable<String> hairPresetsMeshNames = new FastTable<String>();

        public static final FastTable<String> headPresetsFileNames = new FastTable<String>();
        public static final FastTable<Boolean> headPresetsPhongLighting = new FastTable<Boolean>();
        public static final FastTable<ColorRGBA> headPresetsSkinTone = new FastTable<ColorRGBA>();

        public static final FastTable<String> torsoPresetsFileNames = new FastTable<String>();
        public static final FastTable<FastTable<String>> torsoPresetsMeshNames = new FastTable<FastTable<String>>();
        public static final FastTable<Boolean> torsoPresetsDisableColorModulation = new FastTable<Boolean>();

        public static final FastTable<String> legsPresetsFileNames = new FastTable<String>();
        public static final FastTable<FastTable<String>> legsPresetsMeshNames = new FastTable<FastTable<String>>();
        public static final FastTable<Boolean> legsPresetsDisableColorModulation = new FastTable<Boolean>();

        public static final FastTable<String> feetPresetsFileNames = new FastTable<String>();
        public static final FastTable<FastTable<String>> feetPresetsMeshNames = new FastTable<FastTable<String>>();
        public static final FastTable<Boolean> feetPresetsDisableColorModulation = new FastTable<Boolean>();

        static
        {
            /////////// Hair default presets //////////////

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("M_PigTailsShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("CulyPigTailzShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("L_BunShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("M_BunShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("CurlyPonyTailShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("L_PonyTailShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("M_PonyTailShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Long_W_bangsShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Layered_bangShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Layered_pt_LShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Layered_pt_RShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Layered_pt_centerShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Curly_bangsShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Culry_pt_RightShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Culry_pt_LeftShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Curly_Mid_PtShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Long_DredzShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_Pt_BangzShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_pt_CenterShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_Curly_BangzShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_AfricanWBangzShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_African_Pt_RShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_AfricanPT_CenterShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Med_African_Pt_LShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Short_African_MessyShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Short_PT_RShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Short_PT_LShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae");
            hairPresetsMeshNames.add("Short_PT_CenterShape");

            hairPresetsFileNames.add("assets/models/collada/Hair/FemaleHair/FG_Female01DefaultHair.dae"); // Special skinned on "Head" joint, not attachment
            hairPresetsMeshNames.add("HairAShape1");

            /////////// Head default presets //////////////

            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FemaleCHead.bhf");
            headPresetsPhongLighting.add(true);
            headPresetsSkinTone.add(null);
            //headPresetsSkinTone.add(new ColorRGBA(255.0f / 255.0f, 139.0f / 255.0f, 95.0f / 255.0f, 1.0f));

            headPresetsFileNames.add("assets/models/collada/Heads/Binary/AsianFemaleHead.bhf");
            headPresetsPhongLighting.add(true);
            headPresetsSkinTone.add(null);
            //headPresetsSkinTone.add(new ColorRGBA(255.0f / 255.0f, 139.0f / 255.0f, 95.0f / 255.0f, 1.0f));

            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FemaleAAHead.bhf");
            headPresetsPhongLighting.add(true);
            headPresetsSkinTone.add(null);
            //headPresetsSkinTone.add(new ColorRGBA(242.0f / 255.0f, 159.0f / 255.0f, 122.0f / 255.0f, 1.0f));

            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FemaleHispanicHead.bhf");
            headPresetsPhongLighting.add(true);
            headPresetsSkinTone.add(null);
            //headPresetsSkinTone.add(new ColorRGBA(242.0f / 255.0f, 159.0f / 255.0f, 122.0f / 255.0f, 1.0f));

            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_Female01LowPoly.bhf");
            headPresetsPhongLighting.add(true);
            headPresetsSkinTone.add(null);
            //headPresetsSkinTone.add(new ColorRGBA(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f, 1.0f));

//            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_Female02HighPoly.bhf");
//            headPresetsPhongLighting.add(true);
//            headPresetsSkinTone.add(new ColorRGBA(129.0f / 255.0f, 65.0f / 255.0f, 38.0f / 255.0f, 1.0f));
//
//            headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_Female01HighPoly.bhf");
//            headPresetsPhongLighting.add(true);
//            headPresetsSkinTone.add(new ColorRGBA(238.0f / 255.0f, 161.0f / 255.0f, 134.0f / 255.0f, 1.0f));

            /////////// Torso default presets //////////////
            FastTable<String> meshnames;

            torsoPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleDressShirt.dae");
            meshnames = new FastTable<String>();
            meshnames.add("ShirtMeshShape");
            torsoPresetsMeshNames.add(meshnames);
            torsoPresetsDisableColorModulation.add(false);
            
            torsoPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleSweaterCrew.dae");
            meshnames = new FastTable<String>();
            meshnames.add("SweaterShape");
            torsoPresetsMeshNames.add(meshnames);
            torsoPresetsDisableColorModulation.add(false);
            
            torsoPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleJacket.dae");
            meshnames = new FastTable<String>();
            meshnames.add("Jacket1Shape");
            torsoPresetsMeshNames.add(meshnames);
            torsoPresetsDisableColorModulation.add(false);
            
            torsoPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleBlouse.dae");
            meshnames = new FastTable<String>();
            meshnames.add("TShirt1Shape");
            meshnames.add("Arms_NudeShape");
            torsoPresetsMeshNames.add(meshnames);
            torsoPresetsDisableColorModulation.add(false);
            
            /////////// Legs default presets //////////////

            legsPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleJeansStraight.dae");
            meshnames = new FastTable<String>();
            meshnames.add("JeansShape");
            legsPresetsMeshNames.add(meshnames);
            legsPresetsDisableColorModulation.add(false);

            legsPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleDressPants.dae");
            meshnames = new FastTable<String>();
            meshnames.add("PantsFemaleShape");
            legsPresetsMeshNames.add(meshnames);
            legsPresetsDisableColorModulation.add(false);

            legsPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleShorts.dae");
            meshnames = new FastTable<String>();
            meshnames.add("Legs_NudeShape");
            meshnames.add("ShortsShape");
            legsPresetsMeshNames.add(meshnames);
            legsPresetsDisableColorModulation.add(false);

            /////////// Feet default presets //////////////

            feetPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/Female_ClosedToeDressShoes.dae");
            meshnames = new FastTable<String>();
            meshnames.add("Female_ClosedToeDressShoes_Female_DressClosedToe_ShoesShape");
            feetPresetsMeshNames.add(meshnames);
            feetPresetsDisableColorModulation.add(false);
            
            feetPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/Female_ConverseShoes.dae");
            meshnames = new FastTable<String>();
            meshnames.add("Female_ConverseShoes_Female_ConverseShoeShape");
            feetPresetsMeshNames.add(meshnames);
            feetPresetsDisableColorModulation.add(false);
            
            feetPresetsFileNames.add("assets/models/collada/Clothing/FemaleClothing/FemaleFlipFlops.dae");
            meshnames = new FastTable<String>();
            meshnames.add("FlipFlopsFemaleShape");
            meshnames.add("FemaleFeet_NudeShape");
            feetPresetsMeshNames.add(meshnames);
            feetPresetsDisableColorModulation.add(false);
            
        }
    }
}
