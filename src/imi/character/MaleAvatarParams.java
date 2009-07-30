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
 * This class represents concrete attribute settings for the avatarAvatar. It is
 * basically a well-defined CharacterParams starting point for using the
 * primary avatar geometry and animations.
 * @author Lou Hayt
 */
public class MaleAvatarParams extends CharacterParams
{
    /** number of preset features **/
    public enum PresetNumbers {
        NumberOfSkinTones(skinTones.length),
        NumberOfEyeColors(eyeTextures.length);
        public int count;
        PresetNumbers(int value) {
            count = value;
        }
    }

    /** Used to indicate that the bind pose file has already been added to the load instructions **/
    private boolean loadedBind = false;

    /** Used during building **/
    private transient ConfigurationContext configContext = null;

    /** Presets **/
    FastTable<String> hairPresetsFileNames = new FastTable<String>();
    FastTable<String> hairPresetsMeshNames = new FastTable<String>();

    FastTable<String> headPresetsFileNames = new FastTable<String>();
    FastTable<Boolean> headPresetsPhongLighting = new FastTable<Boolean>();
    FastTable<ColorRGBA> headPresetsSkinTone = new FastTable<ColorRGBA>();

    FastTable<String> torsoPresetsFileNames = new FastTable<String>();
    FastTable<FastTable<String>> torsoPresetsMeshNames = new FastTable<FastTable<String>>();
    FastTable<Boolean> torsoPresetsDisableColorModulation = new FastTable<Boolean>();

    FastTable<String> legsPresetsFileNames = new FastTable<String>();
    FastTable<FastTable<String>> legsPresetsMeshNames = new FastTable<FastTable<String>>();
    FastTable<Boolean> legsPresetsDisableColorModulation = new FastTable<Boolean>();

    FastTable<String> feetPresetsFileNames = new FastTable<String>();
    FastTable<FastTable<String>> feetPresetsMeshNames = new FastTable<FastTable<String>>();
    FastTable<Boolean> feetPresetsDisableColorModulation = new FastTable<Boolean>();

    /**
     * You must call DoneBuilding() before using these params!
     * @param name
     */
    public MaleAvatarParams(String name)
    {
        super(name);

        /////////// Hair default presets //////////////

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_PonyTailShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ChinLengthPartLeftShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_SlickedBackShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_PartLeftShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ShortMessyShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_FlatTopShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ShortTopUpShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ShortBasicShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ShortRecedShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_Balding1Shape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_BaldPiccardShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_SuperFauxHawkShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_SpikeyShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_ChinLengthPartRightShape");

        hairPresetsFileNames.add("assets/models/collada/Hair/MaleHair/FG_Male01HairDefaults.dae");
        hairPresetsMeshNames.add("Male_PartRightShape");

        /////////// Head default presets //////////////

        headPresetsFileNames.add("assets/models/collada/Heads/Binary/MaleCHead.bhf");
        headPresetsPhongLighting.add(false);
        headPresetsSkinTone.add(null);
        
        headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_MaleHead02Medium.bhf");
        headPresetsPhongLighting.add(true);
        headPresetsSkinTone.add(new ColorRGBA(177.0f / 255.0f, 84.0f / 255.0f, 24.0f / 255.0f, 1.0f));
        
        headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_MaleLowPoly_01.bhf");
        headPresetsPhongLighting.add(true);
        headPresetsSkinTone.add(null);
        
//        headPresetsFileNames.add("assets/models/collada/Heads/Binary/FG_Obama_HeadMedPoly.bhf");
//        headPresetsPhongLighting.add(true);
//        headPresetsSkinTone.add(new ColorRGBA(186.0f / 255.0f, 107.0f / 255.0f, 62.0f / 255.0f, 1.0f));
//
//        headPresetsFileNames.add("assets/models/collada/Heads/Binary/blackHead.bhf"); // no facial animations
//        headPresetsPhongLighting.add(true);
//        headPresetsSkinTone.add(new ColorRGBA(213.0f / 255.0f, 152.0f / 255.0f, 128.0f / 255.0f, 1.0f));
//
//        headPresetsFileNames.add("assets/models/collada/Heads/Binary/AsianHeadMale.bhf"); // no facial animations
//        headPresetsPhongLighting.add(true);
//        headPresetsSkinTone.add(new ColorRGBA(241.0f / 255.0f, 172.0f / 255.0f, 126.0f / 255.0f, 1.0f));

        /////////// Torso default presets //////////////
        FastTable<String> meshnames;

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleTShirt.dae");
        meshnames = new FastTable<String>();
        meshnames.add("PoloShape");
        meshnames.add("ArmsShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MalePolo.dae");
        meshnames = new FastTable<String>();
        meshnames.add("PoloShape");
        meshnames.add("TorsoNudeShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleDressShirt.dae");
        meshnames = new FastTable<String>();
        meshnames.add("DressShirtShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleSweater.dae");
        meshnames = new FastTable<String>();
        meshnames.add("SweaterMaleShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae");
        meshnames = new FastTable<String>();
        meshnames.add("SuitShirtShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/SuitJacket.dae"); // Requires SuitDressShirt under the jacket
        meshnames = new FastTable<String>();
        meshnames.add("SuitJacketShape");
        torsoPresetsMeshNames.add(meshnames);
        torsoPresetsDisableColorModulation.add(false);

//        torsoPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleMesoTop.dae");
//        meshnames = new FastTable<String>();
//        meshnames.add("TorsoNudeShape");
//        meshnames.add("polySurfaceShape2");
//        torsoPresetsMeshNames.add(meshnames);
//        torsoPresetsDisableColorModulation.add(true);

        /////////// Legs default presets //////////////

        legsPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/Jeans.dae");
        meshnames = new FastTable<String>();
        meshnames.add("polySurface3Shape");
        legsPresetsMeshNames.add(meshnames);
        legsPresetsDisableColorModulation.add(true);

        legsPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/Shorts.dae");
        meshnames = new FastTable<String>();
        meshnames.add("LegsNudeShape");
        meshnames.add("MaleShortsShape");
        legsPresetsMeshNames.add(meshnames);
        legsPresetsDisableColorModulation.add(true);

        legsPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleDressPants1.dae");
        meshnames = new FastTable<String>();
        meshnames.add("MaleDressPantsShape");
        legsPresetsMeshNames.add(meshnames);
        legsPresetsDisableColorModulation.add(false);

        legsPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/Slacks.dae");
        meshnames = new FastTable<String>();
        meshnames.add("SuitPantsShape");
        legsPresetsMeshNames.add(meshnames);
        legsPresetsDisableColorModulation.add(false);

//        legsPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleMesoBottom.dae");
//        meshnames = new FastTable<String>();
//        meshnames.add("LegsNudeShape");
//        meshnames.add("polySurfaceShape6");
//        legsPresetsMeshNames.add(meshnames);
//        legsPresetsDisableColorModulation.add(true);

        /////////// Feet default presets //////////////

        feetPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleTennisShoes.dae");
        meshnames = new FastTable<String>();
        meshnames.add("TennisShoesShape");
        feetPresetsMeshNames.add(meshnames);
        feetPresetsDisableColorModulation.add(false);
        
        feetPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/FlipFlopsFeet.dae");
        meshnames = new FastTable<String>();
        meshnames.add("LFootNudeShape");
        meshnames.add("RFootNudeShape");
        meshnames.add("LFlipFlopShape");
        meshnames.add("RFlipFlopShape");
        feetPresetsMeshNames.add(meshnames);
        feetPresetsDisableColorModulation.add(true);
        
        feetPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/MaleDressShoes.dae");
        meshnames = new FastTable<String>();
        meshnames.add("polySurfaceShape3");
        feetPresetsMeshNames.add(meshnames);
        feetPresetsDisableColorModulation.add(false);
        
        feetPresetsFileNames.add("assets/models/collada/Clothing/MaleClothing/CowBoyBoot_CUT.dae");
        meshnames = new FastTable<String>();
        meshnames.add("CowBoyBootsShape");
        feetPresetsMeshNames.add(meshnames);
        feetPresetsDisableColorModulation.add(true);
    }

    /**
     * Set the head to one determined by the provided integer
     * Some heads set skin tone (are not compatible with arbitrary skin tone),
     * so calling this method in "order" (not before anything that changes skin tone)
     * might be important.
     * Some heads might not have facial animations - it will be disabled.
     * @param preset
     */
    private void customizeHead(int preset)
    {
        if (preset < headPresetsFileNames.size() && preset < headPresetsPhongLighting.size() && preset < headPresetsSkinTone.size() && preset >= 0)
        {
            String headFile = headPresetsFileNames.get(preset);
            setHeadAttachment(headFile);
            setUsePhongLightingForHead(headPresetsPhongLighting.get(preset));
            ColorRGBA skint = headPresetsSkinTone.get(preset);
            if (skint != null)
            {
                setSkinTone(skint.r, skint.g, skint.b);
                setApplySkinToneOnHead(false);
            }
            // Special case for a couple of heads that lack facial animations
            if (headFile.equals("assets/models/collada/Heads/Binary/blackHead.bhf") ||
                    headFile.equals("assets/models/collada/Heads/Binary/AsianHeadMale.bhf"))
                setAnimateFace(false); // no facial animations for this head (that work! hehe)
        }
        else
            throw new RuntimeException("Invalid preset " + preset);
    }

    /**
     * Set the feet to a pair determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeFeetPresets(int preset, List<String> load, List<SkinnedMeshParams> add)
    {
        if (preset < feetPresetsFileNames.size() && preset < feetPresetsMeshNames.size() && preset < feetPresetsDisableColorModulation.size() && preset >= 0)
        {
            String feetFile = feetPresetsFileNames.get(preset);
            load.add(feetFile);
            for (int i = 0; i < feetPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(feetPresetsMeshNames.get(preset).get(i), "Feet", feetFile));
            if (feetPresetsDisableColorModulation.get(preset))
                setShoesColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);

//            String feetFile = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(feetFile);
//            }
//            add.add(new SkinnedMeshParams("RFootNudeShape",  "Feet", feetFile));
//            add.add(new SkinnedMeshParams("LFootNudeShape",  "Feet", feetFile));
        }
    }

    /**
     * Set the hair to one determined by the provided integer.
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeHairPresets(int preset, List<String> load, List<AttachmentParams> attachments)
    {
        if (preset < hairPresetsFileNames.size() && preset < hairPresetsMeshNames.size() && preset >= 0)
        {
            String hairFile = hairPresetsFileNames.get(preset);
            load.add(hairFile);
            attachments.add(new AttachmentParams(hairPresetsMeshNames.get(preset), "HairAttach", PMatrix.IDENTITY, "HairAttachmentJoint", hairFile));
        }
        else
            throw new RuntimeException("Invalid preset " + preset);
    }

    /**
     * Set the legs to one determined by the provided integer.
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeLegsPresets(int preset, List<String> load, List<SkinnedMeshParams> add)
    {
        if (preset < legsPresetsFileNames.size() && preset < legsPresetsMeshNames.size() && preset < legsPresetsDisableColorModulation.size() && preset >= 0)
        {
            String legsFile = legsPresetsFileNames.get(preset);
            load.add(legsFile);
            for (int i = 0; i < legsPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(legsPresetsMeshNames.get(preset).get(i), "LowerBody", legsFile));
            if (legsPresetsDisableColorModulation.get(preset))
                setShoesColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);

//            String legFile = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(legFile);
//            }
//            add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody",legFile));
        }
    }

    /**
     * Set the torso to one determined by the provided integer
     * @param preset
     * @param load
     * @param add
     * @param attachments
     */
    private void customizeTorsoPresets(int preset, List<String> load, List<SkinnedMeshParams> add, List<AttachmentParams> attachments)
    {
        // Add the hands in either way
        String handFile = "assets/models/collada/Avatars/MaleAvatar/Male_Hands.dae";
        load.add(handFile); // change!
        add.add(new SkinnedMeshParams("RHandShape",  "Hands", handFile));
        add.add(new SkinnedMeshParams("LHandShape",  "Hands", handFile));

        if (preset < torsoPresetsFileNames.size() && preset < torsoPresetsMeshNames.size() && preset < torsoPresetsDisableColorModulation.size() && preset >= 0)
        {
            String torsoFile = torsoPresetsFileNames.get(preset);
            load.add(torsoFile);
            for (int i = 0; i < torsoPresetsMeshNames.get(preset).size(); i++)
                add.add(new SkinnedMeshParams(torsoPresetsMeshNames.get(preset).get(i), "UpperBody", torsoFile));
            if (torsoPresetsDisableColorModulation.get(preset))
                setShirtColor(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);

            // Special case for the jacket, add the shirt underneath
            if (torsoFile.equals("assets/models/collada/Clothing/MaleClothing/SuitJacket.dae"))
            {
                torsoFile  = "assets/models/collada/Clothing/MaleClothing/SuitDressShirt.dae";
                load.add(torsoFile);
                add.add(new SkinnedMeshParams("SuitShirtShape", "UpperBody", torsoFile));
            }
        }
        else
        {
            throw new RuntimeException("Invalid preset " + preset);
//            String torsoFile = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
//            if(!loadedBind)
//            {
//                loadedBind = true;
//                load.add(torsoFile);
//            }
//            add.add(new SkinnedMeshParams("TorsoNudeShape",  "UpperBody", torsoFile));
        }
    }

    /**
     * Load up all of the defaults for the male avatar
     */
    private void loadDefaultBindPose()
    {
        String bindFileName = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
        clearLoadInstructions();
        addLoadInstruction(bindFileName);
        loadedBind = true;

        ArrayList<SkinnedMeshParams> add       = new ArrayList<SkinnedMeshParams>();
        add.add(new SkinnedMeshParams("RHandShape",     "Hands", bindFileName));
        add.add(new SkinnedMeshParams("LHandShape",     "Hands", bindFileName));
        add.add(new SkinnedMeshParams("RFootNudeShape", "Feet", bindFileName));
        add.add(new SkinnedMeshParams("LFootNudeShape", "Feet", bindFileName));
        add.add(new SkinnedMeshParams("TorsoNudeShape", "UpperBody", bindFileName));
        add.add(new SkinnedMeshParams("LegsNudeShape",  "LowerBody", bindFileName));

        setAddInstructions(add);
    }

    public FastTable<String> getHairPresetsFileNames() {
        return hairPresetsFileNames;
    }

    public FastTable<String> getHairPresetsMeshNames() {
        return hairPresetsMeshNames;
    }

    public int getNumberOfHairPresets() {
        return hairPresetsMeshNames.size();
    }

    public FastTable<String> getHeadPresetsFileNames() {
        return headPresetsFileNames;
    }

    public FastTable<Boolean> getHeadPresetsPhongLighting() {
        return headPresetsPhongLighting;
    }

    public FastTable<ColorRGBA> getHeadPresetsSkinTone() {
        return headPresetsSkinTone;
    }

    public int getNumberOfHeadPresets() {
        return headPresetsFileNames.size();
    }

    public FastTable<String> getTorsoPresetsFileNames() {
        return torsoPresetsFileNames;
    }

    public FastTable<FastTable<String>> getTorsoPresetsMeshNames() {
        return torsoPresetsMeshNames;
    }

    public int getNumberOfTorsoPresets() {
        return torsoPresetsFileNames.size();
    }

    public FastTable<Boolean> getFeetPresetsDisableColorModulation() {
        return feetPresetsDisableColorModulation;
    }

    public FastTable<String> getFeetPresetsFileNames() {
        return feetPresetsFileNames;
    }

    public FastTable<FastTable<String>> getFeetPresetsMeshNames() {
        return feetPresetsMeshNames;
    }

    public FastTable<String> getHairPresetsColladaFileNames() {
        return hairPresetsFileNames;
    }

    public FastTable<Boolean> getLegsPresetsDisableColorModulation() {
        return legsPresetsDisableColorModulation;
    }

    public FastTable<String> getLegsPresetsFileNames() {
        return legsPresetsFileNames;
    }

    public FastTable<FastTable<String>> getLegsPresetsMeshNames() {
        return legsPresetsMeshNames;
    }

    public FastTable<Boolean> getTorsoPresetsDisableColorModulation() {
        return torsoPresetsDisableColorModulation;
    }

    public int getNumberOfLegsPresets() {
        return legsPresetsFileNames.size();
    }

    public int getNumberOfFeetPresets() {
        return feetPresetsFileNames.size();
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
    public MaleAvatarParams build() {
        return build(true);
    }

    /**
     * Finalizes the configuration process, if any configurations have not been
     * set until now, it will be set to preset 0 or a random one.
     * <p>This method must be called at the end of all of the configure* calls!<p>
     * @param randomizeUnasignedElements
     * @return
     */
    public MaleAvatarParams build(boolean randomizeUnasignedElements) {
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
    public MaleAvatarParams buildSpecific() {
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
    public MaleAvatarParams configureHead(int presetValue) {
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
    public MaleAvatarParams configureHair(int presetValue) {
        if (configContext == null)
            configContext = new ConfigurationContext();
        customizeHairPresets(presetValue, configContext.load, configContext.attachments);
        configContext.hairConfigured = true;
        return this;
    }

    /**
     * Configure the feet to the listed value.
     * @param presetValue
     * @return this
     * @throws IllegalStateException If startBuilding() was not called previously
     */
    public MaleAvatarParams configureFeet(int presetValue) {
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
    public MaleAvatarParams configureLegs(int presetValue) {
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
    public MaleAvatarParams configureTorso(int presetValue) {
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
