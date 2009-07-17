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

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.SkeletonNode;
import imi.serialization.xml.bindings.xmlCharacterAttachmentParameters;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlFloatRow;
import imi.serialization.xml.bindings.xmlMetaData;
import imi.serialization.xml.bindings.xmlSkinnedMeshParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;

/**
 * This class contains all the different attributes of a character.
 * <p>It contains things like the list of animations that need to be loaded as
 * well as geometry substitutions, and other information. Because
 * this is a necessarily data intensive operation, default values are used throughout
 * (where possible) and the mutators have been built to return a reference to their
 * instance. This allows chaining the calls for convenience, for instance:</p>
 * <pre>
 * {@code CharacterParams params = new CharacterParams("name")
 *                                      .setGender(...)
 *                                      .addSkinnedMeshParams(...)
 *                                      .setOrigin(...)}</pre>
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class CharacterParams
{
    /** Name of the character **/
    private String                  name                    = "Character";
    /** The SharedAsset associated with this character's COLLADA model **/
    private SharedAsset             asset                   = null;
    /** A string to be applied to the beginning of all paths, if null asumming it's a local path **/
    private String                  baseURL                 = null;
    /** List of body animations to load **/
    private final List<String> animations       = new FastList<String>();
    /** List of facial animations to load **/
    private final List<String> facialAnimations = new FastList<String>();
    /** List of mesh names to be loaded **/
    private final List<String> loadInstructions = new FastList<String>();
    /** List of skinned meshes to be added to the skeleton **/
    private final List<SkinnedMeshParams> addInstructions = new FastList<SkinnedMeshParams>();
    /** List of meshes to add as attachment nodes on the skeleton **/
    private final List<AttachmentParams>  attachmentsInstructions = new FastList<AttachmentParams>();
    /** Specify the head the avatar should begin with **/
    private String                  headAttachment          = null;
    /** This specifies the gender of the avatar. 1 == male, 2 == female, 3... **/
    private int                     gender                  = 1;
    /** Eyeball texture **/
    private String                  eyeballTexture          = "assets/models/collada/Heads/EyeTextures/Brown_Eye.png";
    /** True if the head mesh will be applied with the skine tone (otherwise just white to keep the original texture's color) **/
    private boolean                 applySkinToneOnHead     = true;
    /** True if valid **/
    private boolean                 valid = false;
    /** Key-value Metadata Map */
    private Map<String, String> metadataMap = new HashMap();

    private static final float f255 = 255.0f; // need all the colors to be brighter?
    static final ColorRGBA[] clothesColors = new ColorRGBA[]
    {
        new ColorRGBA(233.0f / f255,  235.0f / f255, 235.0f  / f255, 1), // Gray
        new ColorRGBA(244.0f / f255,  236.0f / f255, 83.0f  / f255, 1), // Yellow
        new ColorRGBA(235.0f / f255,  233.0f / f255, 160.0f  / f255, 1), // Light Orange
        new ColorRGBA(253.0f / f255,  183.0f / f255, 87.0f  / f255, 1), // Orange
        new ColorRGBA(220.0f / f255,  94.0f  / f255, 10.0f  / f255, 1), // Dark Orange
        new ColorRGBA(242.0f / f255,  103.0f / f255, 79.0f  / f255, 1), // Light Red
        new ColorRGBA(233.0f / f255,   31.0f / f255, 51.0f  / f255, 1), // Red
        new ColorRGBA(190.0f / f255,  112.0f / f255, 73.0f  / f255, 1), // Light Brown
        new ColorRGBA(115.0f / f255,   65.0f / f255, 40.0f  / f255, 1), // Dark Brown
        new ColorRGBA(114.0f / f255,   99.0f / f255, 65.0f  / f255, 1), // Dirty Brown
        new ColorRGBA( 98.0f / f255,  202.0f / f255,  4.0f  / f255, 1), // Light Green
        new ColorRGBA( 27.0f / f255,  150.0f / f255, 36.0f  / f255, 1), // Dark Green
        new ColorRGBA( 69.0f / f255, 167.0f  / f255, 133.0f / f255, 1), // Blueish
        new ColorRGBA(83.0f  / f255,  154.0f / f255, 157.0f / f255, 1), // Light Blue
        new ColorRGBA(14.0f  / f255,  210.0f / f255, 226.0f / f255, 1), // Cyan
        new ColorRGBA(29.0f  / f255,  140.0f / f255, 222.0f / f255, 1), // Blue
        new ColorRGBA(46.0f  / f255,  35.0f  / f255, 248.0f / f255, 1), // Dark Blue
        new ColorRGBA(104.0f / f255,  68.0f  / f255, 213.0f / f255, 1), // Purple
        new ColorRGBA(146.0f / f255,  90.0f  / f255, 190.0f / f255, 1), // Light Purple
        new ColorRGBA(226.0f / f255,  57.0f  / f255, 240.0f / f255, 1), // Pink
        new ColorRGBA(206.0f / f255,  91.0f  / f255, 120.0f / f255, 1), // Red Pink
        new ColorRGBA(38.0f  / f255,  45.0f  / f255, 44.0f  / f255, 1), // Dark
        new ColorRGBA(182.0f / f255,  192.0f / f255, 191.0f / f255, 1), // Off White
    };
    
    /** Collection of skin tone shades**/
    static final ColorRGBA[] skinTones = new ColorRGBA[]
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
    static final String[] eyeTextures = new String[]
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
        //"assets/models/collada/Heads/EyeTextures/eyeColor21.png"// snakes!
    };
    
    /////////////////////////////////////////
    //////  Not Saved in XML format /////////
    /////////////////////////////////////////

    /** Skin tone RGB **/
    private final float []  skinTone        = new float [3];
    /** Hair color RGB **/
    private final float []  hairColor       = new float [3];
    /** Shirt color RGB **/
    private final float []  shirtColor      = new float [3];
    private final float []  shirtSpecColor  = new float [3];
    /** Pants color RGB **/
    private final float []  pantsColor      = new float [3];
    private final float []  pantsSpecColor  = new float [3];
    /** Shoes color RGB **/
    private final float []  shoesColor      = new float [3];
    private final float []  shoesSpecColor  = new float [3];


    /** Initialization extension **/
    private CharacterInitializationInterface initializationObject    = null;
    /** True if the phong lighting (no color modulation) should be used. **/
    private boolean m_bPhongLightingForHead = false;

    // For simple static geometry replacement
    private boolean useSimpleStaticModel    = false;
    private PScene  simpleScene             = null;
    private final PMatrix origin = new PMatrix();

    /** Whether the facial animation will play **/
    private boolean animateFace = true;
    /** Whether the animation processor starts enabled **/
    private boolean animateBody = true;

    /**
     * Construct a new instance with the provided name
     * @param name A non-null name
     * @throws IllegalArgumentException If {@code name == null}
     */
    public CharacterParams(String name) {
        this();
        if (name == null)
            throw new IllegalArgumentException("Null name provided");
        this.name = name;
    }

    /**
     * Construct a new instance a default skin color and white as the default
     * clothing colors.
     */
    public CharacterParams() {
        skinTone[0] = 230.0f/255.0f;
        skinTone[1] = 197.0f/255.0f;
        skinTone[2] = 190.0f/255.0f;
        for (int i = 0; i < 3; i++)
        {
            hairColor[i]      = 1.0f;
            shirtColor[i]     = 1.0f;
            shirtSpecColor[i] = 1.0f;
            pantsColor[i]     = 1.0f;
            pantsSpecColor[i] = 1.0f;
            shoesColor[i]     = 1.0f;
            shoesSpecColor[i] = 1.0f;
        }
    }

    /**
     * This method removes all SkinnedMeshParam objects used for adding skinned
     * meshes to the specified subgroup.
     * @param subgroup A non-null subgroup identifier
     * @see SkeletonNode
     * @throws IllegalArgumentException If {@code subgroup == null}
     */
    public void removeSkinnedMeshesForSubgroup(String subgroup) {
        if (subgroup == null)
            throw new IllegalArgumentException("Null subgroup specified");
        for (SkinnedMeshParams smParam : addInstructions)
            if (smParam.subGroupName.equalsIgnoreCase(subgroup))
                addInstructions.remove(smParam);
    }


    /**
     * Factory method for creating SkinnedMeshParams objects
     * @param meshName The mesh to attach
     * @param subGroupName Which subgroup it is destined for on the skeleton.
     * @param owningFileName The file that the mesh came from (matches its loading instruction)
     * @throws IllegalArgumentException If any parameter is null
     * @return
     */
    public SkinnedMeshParams createSkinnedMeshParams(String meshName, String subGroupName, String owningFileName)
    {
        if (meshName == null || subGroupName == null || owningFileName == null)
            throw new IllegalArgumentException("Null parameter encountered!" +
                    " meshName: " + meshName + ", subGroupName: " + subGroupName +
                    ", owningFileName: " + owningFileName);
        return new SkinnedMeshParams(meshName, subGroupName, owningFileName);
    }

    /**
     * Add a copy of the provided attachment params to the collection.
     * @param attachmentParams The (non-null) attachment params to add
     * @throws IllegalArgumentException If {@code attachmentParams == null}
     */
    public CharacterParams addAttachmentInstruction(AttachmentParams attachmentParams) {
        if (attachmentParams == null)
            throw new IllegalArgumentException("Null params!");
        attachmentsInstructions.add(new AttachmentParams(attachmentParams)); // defensive copy
        return this;
    }

    /**
     * Add a copy of the provided skinned mesh param to the collection.
     * @param params A non-null params object to add
     * @throws IllegalArgumentException If {@code params == null}
     */
    public CharacterParams addSkinnedMeshParams(SkinnedMeshParams params) {
        if (params == null)
            throw new IllegalArgumentException("Null params!");
        addInstructions.add(new SkinnedMeshParams(params));// defensive copy
        return this;
    }

    /**
     * Add the specified body animation to the collection.
     * @param relativePath A non-null path
     * @return this
     * @throws IllegalArgumentException If {@code relativePath == null}
     */
    public CharacterParams addBodyAnimation(String relativePath) {
        if (relativePath == null)
            throw new IllegalArgumentException("Null param!");
        animations.add(relativePath);
        return this;
    }

    /**
     * Add the specified facial animation to the collection.
     * @param relativePath A non-null path
     * @return this
     * @throws IllegalArgumentException If {@code relativePath == null}
     */
    public CharacterParams addFacialAnimation(String relativePath) {
        if (relativePath == null)
            throw new IllegalArgumentException("Null param!");
        facialAnimations.add(relativePath);
        return this;
    }

    /**
     * Add the specified file to the collection of loaded files.
     * @param pathToFile A non-null path
     * @return this
     * @throws IllegalArgumentException If {@code pathToFile == null}
     */
    public CharacterParams addLoadInstruction(String pathToFile) {
        if (pathToFile == null)
            throw new IllegalArgumentException("Null param!");
        loadInstructions.add(pathToFile);
        return this;
    }

    /**
     * Clears the collection of attachment instructions.
     */
    public void clearAttachmentInstructions() {
        attachmentsInstructions.clear();
    }

    /**
     * Clear the collection of skinned meshes.
     */
    public void clearSkinnedMeshParams() {
        addInstructions.clear();
    }

    /**
     * Clears the collection of body animations.
     */
    public void clearBodyAnimations() {
        animations.clear();
    }

    /**
     * Clears the collection of facial animations.
     */
    public void clearFacialAnimations() {
        facialAnimations.clear();
    }

    /**
     * Clears the collection of load instructions.
     */
    public void clearLoadInstructions() {
        loadInstructions.clear();
    }

    /**
     * Adds a key-value pair to the meta data Map.
     * @param key The key
     * @param value The value
     */
    public void putMetaData(String key, String value) {
         metadataMap.put(key, value);
    }

    /**
     * Clears the meta data Map.
     */
    public void clearMetaData() {
        metadataMap.clear();
    }

    ////////////////////////////////////////////////////////////////////////
    //////////////////// ACCESSORS HOOOOOOOOOOO!!!!!!!! ////////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve the name.
     * @return A name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the key-value meta-data map.
     * @return The metadata Map
     */
    public Map<String, String> getMetaData() {
        return metadataMap;
    }

    /**
     * Retrieve the shared asset associated with these params; may be null.
     * @return Shared asset
     * @see SharedAsset
     */
    public SharedAsset getAsset() {
        return asset;
    }
    
    /**
     * Retrieve the relative path to the head attachment as a string.
     * <p>For instance, if the head attachment uses a file named headAttach.bhf,
     * then the string will be (for example) {@literal assets/models/Collada/Heads/headAttach.bhf}.
     * @return The relative path to the head attachment, may be null
     */
    public String getHeadAttachment() {
        return headAttachment;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * Determine if the params are using a simple model case.
     * @return True if using a simple static model
     */
    public boolean isUseSimpleStaticModel() {
        return useSimpleStaticModel;
    }

    /**
     * Retrieve the transform for these params.
     * @param mOut A non- null storage object
     * @throws IllegalArgumentException If {@code mOut == null}
     */
    public void getOrigin(PMatrix mOut) {
        if (mOut == null)
            throw new IllegalArgumentException("Null storage object provided");
        mOut.set(origin);
    }

    /**
     * Get the base URL for these attributes.
     * <p>If this value is non-null, it will be pre-pended to all relative paths
     * provided by this instance.</p>
     * @return A the base url, or null if none is set
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Retrieve an iterable view of the animation string collection.
     * <p>The collection is of strings that represent relative paths to animation
     * files. These relative paths may be prefixed with getBaseURL() if that value
     * is valid.</p>
     * @return An iterable view of the animation collection
     */
    public Iterable<String> getAnimations() {
        return animations;
    }

    /**
     * Retrieve an iterable view of the facial animation collection.
     * <p>The collection is of strings that represent relative paths to animation
     * files. These relative paths may be prefixed with getBaseURL() if that value
     * is valid.</p>
     * @return An iterable view of facial animation paths
     */
    public Iterable<String> getFacialAnimations() {
        return facialAnimations;
    }

    /**
     * Retrieve an iterable view of the skinned mesh attaching objects.
     * @return List of {@code SkinnedMeshParam} objects
     */
    public Iterable<SkinnedMeshParams> getSkinnedMeshInstructions() {
        return addInstructions;
    }
    
    /**
     * Retrieve an iterable view of the loading instructions.
     * <p>This is a collection of relative paths to collada files that should
     * be loaded to provided attached meshes. The paths may be pre-pended with
     * {@code getBaseURL()}, if that value if non-null.
     * @return Iterable view of load paths
     */
    public Iterable<String> getLoadInstructions() {
        return loadInstructions;
    }
    
    /**
     * Retrieve an iterable view of the attachment params being used.
     * @return Iterable view of attachments
     */
    public Iterable<AttachmentParams> getAttachmentsInstructions() {
        return attachmentsInstructions;
    }

    
    /**
     * Retrieve the currently set initializer object.
     * @return The initialization object, may be null if unset
     */
    public CharacterInitializationInterface getInitializationObject() {
        return initializationObject;
    }

    /**
     * Retrieve the scene graph for use in the simple model case.
     * @return The simple scene graph, or null if unused
     */
    public PScene getSimpleScene() {
        return simpleScene;
    }

    /**
     * Retrieve the gender integer.
     * <p>Currently the system maps 1 to male, and 2 to female.</p>
     * @return A non-negative gender integer
     */
    public int getGender() {
        return gender;
    }

    /**
     * Returns true if these params represent a male of the species.
     * @return
     */
    public boolean isMale()
    {
        if (gender == 1)
            return true;
        return false;
    }

    /**
     * Retrieve the skin tone value
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getSkinTone(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = skinTone[0];
        rgbOut[1] = skinTone[1];
        rgbOut[2] = skinTone[2];
    }

    /**
     * Retrieve the hair color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getHairColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = hairColor[0];
        rgbOut[1] = hairColor[1];
        rgbOut[2] = hairColor[2];
    }


    /**
     * Retrieve the pants color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getPantsColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = pantsColor[0];
        rgbOut[1] = pantsColor[1];
        rgbOut[2] = pantsColor[2];
    }

    /**
     * Retrieve the shirt color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getShirtColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = shirtColor[0];
        rgbOut[1] = shirtColor[1];
        rgbOut[2] = shirtColor[2];
    }

    /**
     * Retrieve the shoe color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getShoesColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = shoesColor[0];
        rgbOut[1] = shoesColor[1];
        rgbOut[2] = shoesColor[2];
    }

    /**
     * Retrieve the pant's specular component color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getPantsSpecColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = pantsSpecColor[0];
        rgbOut[1] = pantsSpecColor[1];
        rgbOut[2] = pantsSpecColor[2];
    }

    /**
     * Retrieve the shirt's specular component color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getShirtSpecColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = shirtSpecColor[0];
        rgbOut[1] = shirtSpecColor[1];
        rgbOut[2] = shirtSpecColor[2];
    }

    /**
     * Retrieve the shoe's specular component color.
     * @param rgbOut A non-null array of at least size three
     * @throws IllegalArgumentException If <pre>{@code rgbOut == null || rgbOut.length < 3}</pre>
     */
    public void getShoesSpecColor(float[] rgbOut) {
        if (rgbOut == null || rgbOut.length < 3)
            throw new IllegalArgumentException("Unacceptable array provided!");
        rgbOut[0] = shoesSpecColor[0];
        rgbOut[1] = shoesSpecColor[1];
        rgbOut[2] = shoesSpecColor[2];
    }

    /**
     * Retrieve the relative path to the eyeball texture.
     * @return
     */
    public String getEyeballTexture() {
        return eyeballTexture;
    }

    /**
     * Determine if the phong lighting model is being used (rather than normal maps) for the head
     * @return True if using phone lighting
     */
    public boolean isUsingPhongLightingForHead()
    {
        return m_bPhongLightingForHead;
    }

    /**
     * Set true to enable simple phong lighting on the head without using normal maps.
     * @param enable
     */
    public void setUsePhongLightingForHead(boolean enable) {
        this.m_bPhongLightingForHead = enable;
    }

    /**
     * Determine if the face is being animated.
     * @return True if the face will be animated
     */
    public boolean isAnimatingFace() {
        return animateFace;
    }

    /**
     * Determine if the body is being animated.
     * @return True if body animations will play
     */
    public boolean isAnimateBody() {
        return animateBody;
    }

    ////////////////////////////////////////////
    //////////// MUTATORS!!!! //////////////////
    ////////////////////////////////////////////

    /**
     * Sets the name.
     * @param name A non-null name
     * @throws IllegalArgumentException If {@code name == null}
     */
    public CharacterParams setName(String name) {
        if (name == null)
            throw new IllegalArgumentException("Null name provided");
        this.name = name;
        return this;
    }

    /**
     * Set the shared asset to be associated with these params.
     * @param asset The shared asset to use
     */
    public CharacterParams setAsset(SharedAsset asset) {
        this.asset = asset;
        return this;
    }

    /**
     * Set the relative path to the head attachment as a string.
     * <p>For instance, if the head attachment uses a file named headAttach.bhf,
     * then the string should be (for example) {@literal assets/models/Collada/Heads/headAttach.bhf}.
     * @param headLocation A relative path string
     */
    public CharacterParams setHeadAttachment(String headLocation) {
        this.headAttachment = headLocation;
        return this;
    }

    /**
     * Sets the static model flag, as well as the scene graph to use for this model.
     * <p>If bUseSimpleSphereModel is true, then the provided PScene is used as the
     * base for the scene graph of this simple model. The provided graph should be
     * 'complete', that is, it should have all the required material properties set
     * as the character loading path takes a lot of shortcuts when this mode is
     * enabled.
     * @param bUseSimpleSphereModel True to use a simple model.
     * @param pscene Scene graph to use, if bUseSimpleSphereModel is true, pscene must be non-null
     * @throws IllegalArgumentException If {@code (buseSimpleSphereModel == true && pscene == null)}
     */
    public CharacterParams setUseSimpleStaticModel(boolean bUseSimpleSphereModel, PScene pscene) {
        this.useSimpleStaticModel = bUseSimpleSphereModel;
        if (bUseSimpleSphereModel)
            if (pscene == null)
                throw new IllegalArgumentException("Null pscene graph provided");
        this.simpleScene = pscene;
        return this;
    }

    /**
     * Sets the origin to the provided transform.
     * @param origin A non-null transformation matrix
     * @throws IllegalArgumentException If {@code origin == null}
     */
    public CharacterParams setOrigin(PMatrix origin) {
        if (origin == null)
            throw new IllegalArgumentException("Null transform provided");
        this.origin.set(origin);
        return this;
    }

    /**
     * Set the base URL for these attributes.
     * <p>If this value is non-null, it will be pre-pended to all relative paths
     * provided by this instance.</p>
     * @param baseURL A base url string, or null to unset it
     */
    public CharacterParams setBaseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    /**
     * This method will clear the current contents of the animation collection
     * and fill it with the provided strings.
     * <p>The collection is of strings that represent relative paths to animation
     * files. These relative paths may be prefixed with getBaseURL() if that value
     * is valid.</p>
     * @param animations A non-null collection of animations (may be empty)
     * @throws NullPointerException If {@code animations == null}
     */
    public CharacterParams setAnimations(Iterable<String> animations) {
        this.animations.clear();
        for (String anim : animations) // NPE if null
            this.animations.add(anim);
        return this;
    }

    /**
     * This method clears the contents of the facial animtion collection and fills
     * it with the provided values.
     * <p>The collection is of strings that represent relative paths to animation
     * files. These relative paths may be prefixed with getBaseURL() if that value
     * is valid.</p>
     * @param facialAnimations A non-null collection of animation paths
     * @throws NullPointerException If {@code facialAnimations == null}
     */
    public CharacterParams setFacialAnimations(Iterable<String> facialAnimations) {
        this.facialAnimations.clear();
        for (String faceAnim : facialAnimations) // NPE if null
            this.facialAnimations.add(faceAnim);
        return this;
    }

    /**
     * Clears the current collection of skinned mesh attachment and fills it with
     * the provided collection's contents.
     * @param addInstructions A non-null collection of instructions (may be empty)
     * @throws NullPointerException If {@code addInstructions == null)
     */
    public CharacterParams setAddInstructions(Iterable<SkinnedMeshParams> addInstructions) {
        this.addInstructions.clear();
        for (SkinnedMeshParams smParam : addInstructions) // NPE if null
            this.addInstructions.add(new SkinnedMeshParams(smParam)); // Defensive copy
        return this;
    }

    /**
     * This method clears the collection of loading instructions and sets it to
     * the contents of the provided collection.
     * @param loadInstructions A non-null collection (may be empty)
     * @throws NullPointerException If {@code loadInstructions == null}
     * @throws IllegalArgumentException If loadInstructions contains null members
     */
    public CharacterParams setLoadInstructions(Iterable<String> loadInstructions) {
        this.loadInstructions.clear();
        for (String load : loadInstructions)
        {
            if (load == null)
                throw new IllegalArgumentException("Null string encountered!");
            this.loadInstructions.add(load);
        }
        return this;
    }

    /**
     * This method clears the collection of attachment params and fills it with
     * copies of the contents of the provided collection.
     * @param attachmentsInstructions A non-null collection, may be empty
     * @throws NullPointerException If {@code attachmentInstructions == null}
     */
    public CharacterParams setAttachmentsInstructions(Iterable<AttachmentParams> attachmentsInstructions) {
        this.attachmentsInstructions.clear();
        for (AttachmentParams param : attachmentsInstructions) // NPE if null
            this.attachmentsInstructions.add(new AttachmentParams(param));
        return this;
    }

    /**
     * Sets the initialization object.
     * @param initializationObject Initializer, or null to unset
     */
    public CharacterParams setInitializationObject(CharacterInitializationInterface initializationObject) {
        this.initializationObject = initializationObject;
        return this;
    }

    /**
     * Set the gender integer. 
     * <p>Currently the system maps 1 to male, and 2 to female.</p>
     * @param sex A non-negative integer
     * @throws IllegalArgumentException If sex is negative 
     */
    public CharacterParams setGender(int sex) {
        if (sex < 0)
            throw new IllegalArgumentException("Negative sex! Oh no!");
        gender = sex;
        return this;
    }

    /**
     * Choose a random skin tone preset
     * @return
     */
    public CharacterParams randomizeSkinTone()
    {
        int preset = (int) (Math.random() * 1000000 % skinTones.length);
        setSkinTonePreset(preset);
        return this;
    }

    public CharacterParams setSkinTonePreset(int preset)
    {
        skinTone[0] = skinTones[preset].r;
        skinTone[1] = skinTones[preset].g;
        skinTone[2] = skinTones[preset].b;
        return this;
    }

    /**
     * Sets the skin tone to specified color.
     * <p>The values should be normalized, but this is not enforced.</p>
     * @param r Red component
     * @param g Green component
     * @param b Blue component
     */
    public CharacterParams setSkinTone(float r, float g, float b) {
        skinTone[0] = r;
        skinTone[1] = g;
        skinTone[2] = b;
        return this;
    }

    /**
     * Randomly set a color for the hair from the skin tone presets
     */
    public CharacterParams randomizeHairColor()
    {
        int preset = (int) (Math.random() * 1000000 % skinTones.length);
        setHairColorPreset(preset);
        return this;
    }

    /**
     * Set a skin tone preset as a base for a hair color preset plus some randome values
     * @param preset
     * @return
     */
    public CharacterParams setHairColorPreset(int skinToneBasePreset)
    {
        float r = skinTones[skinToneBasePreset].r * (float)Math.random();
        float g = skinTones[skinToneBasePreset].g * (float)Math.random();
        float b = skinTones[skinToneBasePreset].b * (float)Math.random();
        setHairColor(r, g, b);
        return this;
    }

    /**
     * Sets the hair color to specified color.
     * <p>The values should be normalized, but this is not enforced.</p>
     * @param r Red component
     * @param g Green component
     * @param b Blue component
     */
    public CharacterParams setHairColor(float r, float g, float b) {
        hairColor[0] = r;
        hairColor[1] = g;
        hairColor[2] = b;
        return this;
    }

    /**
     * Randomize the pants diffuse and specular colors.
     * A random preset is chosen.
     */
    public CharacterParams randomizePantColor()
    {
        int preset = (int) ((Math.random() * 100000) % clothesColors.length);
        setPantsColorPreset(preset, true);
        return this;
    }

    /**
     * Set pants color preset, specular will be somewhat random.
     * @param preset
     * @return
     */
    public CharacterParams setPantsColorPreset(int preset, boolean randomSpecular)
    {
        pantsColor[0] = clothesColors[preset].r;
        pantsColor[1] = clothesColors[preset].g;
        pantsColor[2] = clothesColors[preset].b;
        if (randomSpecular)
        {
            float chance  = (float)Math.random();
            if (chance < 0.2f)
            {
                pantsSpecColor[0] = (float)Math.random();
                pantsSpecColor[1] = (float)Math.random();
                pantsSpecColor[2] = (float)Math.random();
            }
            else if (chance < 0.4f)
            {
                pantsSpecColor[0] = shirtColor[0];
                pantsSpecColor[1] = shirtColor[1];
                pantsSpecColor[2] = shirtColor[2];
            }
            else
            {
                pantsSpecColor[0] = 1.0f;
                pantsSpecColor[1] = 1.0f;
                pantsSpecColor[2] = 1.0f;
            }
        }
        else
        {
            pantsSpecColor[0] = 1.0f;
            pantsSpecColor[1] = 1.0f;
            pantsSpecColor[2] = 1.0f;
        }

        return this;
    }

    /**
     * Set the color values for the pants material.
     * <p>These values should be normalized (0-1), but this is not strictly enforced</p>
     * @param r
     * @param g
     * @param b
     * @param specR
     * @param specG
     * @param specB
     */
    public CharacterParams setPantsColor(float r, float g, float b, float specR, float specG, float specB) {
        pantsColor[0] = r;
        pantsColor[1] = g;
        pantsColor[2] = b;
        pantsSpecColor[0] = specR;
        pantsSpecColor[1] = specG;
        pantsSpecColor[2] = specB;
        return this;
    }

    /**
     * Randomize the shirt diffuse and specular colors.
     * A random preset is chosen.
     */
    public CharacterParams randomizeShirtColor()
    {
        int preset = (int) ((Math.random() * 100000) % clothesColors.length);
        setShirtColorPreset(preset, true);
        return this;
    }

    /**
     * Set shirt color preset, specular will be somewhat random.
     * @param preset
     * @return
     */
    public CharacterParams setShirtColorPreset(int preset, boolean randomSpec)
    {
        shirtColor[0] = clothesColors[preset].r;
        shirtColor[1] = clothesColors[preset].g;
        shirtColor[2] = clothesColors[preset].b;
        if (randomSpec)
        {
            float chance  = (float)Math.random();
            if (chance < 0.2f)
            {
                shirtSpecColor[0] = (float)Math.random();
                shirtSpecColor[1] = (float)Math.random();
                shirtSpecColor[2] = (float)Math.random();
            }
            else if (chance < 0.4f)
            {
                shirtSpecColor[0] = shirtColor[0];
                shirtSpecColor[1] = shirtColor[1];
                shirtSpecColor[2] = shirtColor[2];
            }
            else
            {
                shirtSpecColor[0] = 1.0f;
                shirtSpecColor[1] = 1.0f;
                shirtSpecColor[2] = 1.0f;
            }
        }
        else
        {
            shirtSpecColor[0] = 1.0f;
            shirtSpecColor[1] = 1.0f;
            shirtSpecColor[2] = 1.0f;
        }
        return this;
    }

    /**
     * Sets the color values used for the shirt's material
     * <p>These values should be normalized (0-1), but this is not strictly enforced</p>
     * @param r
     * @param g
     * @param b
     * @param specR
     * @param specG
     * @param specB
     */
    public CharacterParams setShirtColor(float r, float g, float b, float specR, float specG, float specB) {
        shirtColor[0] = r;
        shirtColor[1] = g;
        shirtColor[2] = b;
        shirtSpecColor[0] = specR;
        shirtSpecColor[1] = specG;
        shirtSpecColor[2] = specB;
        return this;
    }

    /**
     * Randomize the shoes diffuse and specular colors.
     * A random preset is chosen.
     */
    public CharacterParams randomizeShoesColor()
    {
        int preset = (int) ((Math.random() * 100000) % clothesColors.length);
        setShoesColorPreset(preset, true);
        return this;
    }

    /**
     * Set shoes color preset, specular will be somewhat random.
     * @param preset
     * @return
     */
    public CharacterParams setShoesColorPreset(int preset, boolean randomSpecular)
    {
        shoesColor[0] = clothesColors[preset].r;
        shoesColor[1] = clothesColors[preset].g;
        shoesColor[2] = clothesColors[preset].b;
        if (randomSpecular)
        {
            float chance  = (float)Math.random();
            if (chance < 0.2f)
            {
                shoesSpecColor[0] = (float)Math.random();
                shoesSpecColor[1] = (float)Math.random();
                shoesSpecColor[2] = (float)Math.random();
            }
            else if (chance < 0.4f)
            {
                shoesSpecColor[0] = shirtColor[0];
                shoesSpecColor[1] = shirtColor[1];
                shoesSpecColor[2] = shirtColor[2];
            }
            else
            {
                shoesSpecColor[0] = 1.0f;
                shoesSpecColor[1] = 1.0f;
                shoesSpecColor[2] = 1.0f;
            }
        }
        else
        {
            shoesSpecColor[0] = 1.0f;
            shoesSpecColor[1] = 1.0f;
            shoesSpecColor[2] = 1.0f;
        }
        return this;
    }

    /**
     * Sets the color values used for the shoe's material.
     * <p>These values should be normalized (0-1), but this is not strictly enforced</p>
     * @param r
     * @param g
     * @param b
     * @param specR
     * @param specG
     * @param specB
     */
    public CharacterParams setShoesColor(float r, float g, float b, float specR, float specG, float specB) {
        shoesColor[0] = r;
        shoesColor[1] = g;
        shoesColor[2] = b;
        shoesSpecColor[0] = specR;
        shoesSpecColor[1] = specG;
        shoesSpecColor[2] = specB;
        return this;
    }

    /**
     * Choose a random eyeball texture
     * @return
     */
    public CharacterParams randomizeEyeballTexture() {
        int preset = (int) ((Math.random() * 100000) % eyeTextures.length);
        setEyeballTexturePreset(preset);
        return this;
    }

    /**
     * Choose an eyeball texture preset
     * @param preset
     * @return
     */
    public CharacterParams setEyeballTexturePreset(int preset) {
        if (preset < 0 || preset >= eyeTextures.length)
            throw new IllegalArgumentException("Invalid eyeball texture preset!");
        this.eyeballTexture = eyeTextures[preset];
        return this;
    }

    /**
     * Set the eyeball texture's relative path.
     * @param eyeballTexture A non-null path
     * @throws IllegalArgumentException If {@code eyeballTexture == null}
     */
    public CharacterParams setEyeballTexture(String eyeballTexture) {
        if (eyeballTexture == null)
            throw new IllegalArgumentException("Null eyeball texture!");
        this.eyeballTexture = eyeballTexture;
        return this;
    }

    /**
     * Enable / Disable facial animations.
     * @param animateFace True to animate the face
     */
    public CharacterParams setAnimateFace(boolean animateFace) {
        this.animateFace = animateFace;
        return this;
    }

    /**
     * Enable / Disable playing of body animations
     * @param animateBody True to enable
     */
    public CharacterParams setAnimateBody(boolean animateBody) {
        this.animateBody = animateBody;
        return this;
    }

    ///////////////////////////////////////////////////////
    /////////// XML / DOM Plumbing code ///////////////////
    ///////////////////////////////////////////////////////

    /**
     * Package private method for serializing this object
     * @return The DOM representation of this object
     */
    xmlCharacterAttributes generateAttributesDOM()
    {
        xmlCharacterAttributes result = new xmlCharacterAttributes();
        // load her up!
        if (name != null)
            result.setName(name);
        else
            result.setName("Seymour Slizzle");

        if (baseURL != null)
            result.setBaseURL(baseURL);
        else
            result.setBaseURL(null);

        if (headAttachment != null)
            result.setHeadAttachment(headAttachment);
        else
            result.setHeadAttachment(null);

        // Body animations
        ArrayList<String> stringArray = new ArrayList<String>();
        if (animations != null)
        {
            for (String str : animations)
                stringArray.add(str);
            result.setBodyAnimations(stringArray);
        }
        else
            result.setBodyAnimations(null);

        // facial animations
        if (facialAnimations != null)
        {
            stringArray = new ArrayList<String>();
            for (String str : facialAnimations)
                stringArray.add(str);
            result.setFacialAnimations(stringArray);
        }
        else
            result.setFacialAnimations(null);

        // loading instructions
        if (loadInstructions != null)
            result.setLoadingInstructions(loadInstructions);
        else
            result.setLoadingInstructions(null);

        // addition instructions
        if (addInstructions != null)
        {
            for (SkinnedMeshParams params : addInstructions)
                result.addAdditionInstruction(params.generateParamsDOM());
        }
        else
            result.setAdditionInstructions(null);

        // load up all the attachment params
        if (attachmentsInstructions != null)
        {
            for (AttachmentParams param : attachmentsInstructions)
                result.addAttachment(param.generateParamsDOM());
        }
        else
            result.setAttachments(null);

        // save the sex integer
        result.setGender(gender);
        // eye color
        result.setEyeballTexture(eyeballTexture);
        // skin tone
        if (skinTone != null)
        {
            xmlFloatRow skin = new xmlFloatRow();
            skin.setX(skinTone[0]);
            skin.setY(skinTone[1]);
            skin.setZ(skinTone[2]);
            result.setSkinTone(skin);
        }
        else
            result.setSkinTone(null);

        result.setAnimateFace(animateFace);
        result.setApplySkinToneOnHead(applySkinToneOnHead);

        // meta data
        if (metadataMap != null)
        {
            for (String key : metadataMap.keySet())
            {
                xmlMetaData xmd = new xmlMetaData();
                xmd.setValues(key, metadataMap.get(key));
                result.getMetaData().add(xmd);
            }
        }

        // Finished
        return result;
    }


    /**
     * Construct a new instance reflecting the provided DOM
     * @param attributesDOM
     */
    CharacterParams(xmlCharacterAttributes attributesDOM)
    {
        this();
        applyAttributesDOM(attributesDOM);
    }
    
    /**
     * Package private method to apply the provided DOM information to this instance.
     * @param attributesDOM
     */
    void applyAttributesDOM(xmlCharacterAttributes attributesDOM)
    {
        if (attributesDOM == null)
            throw new IllegalArgumentException("Null DOM provided!");

        this.setName(attributesDOM.getName());
        this.setGender(attributesDOM.getGender());
        if (attributesDOM.getBaseURL() == null ||
                attributesDOM.getBaseURL().length() == 0)
            baseURL = null;
        else
            baseURL = attributesDOM.getName();
        this.setBaseURL(attributesDOM.getBaseURL());
        this.setHeadAttachment(attributesDOM.getHeadAttachment());
        this.setAnimations(attributesDOM.getBodyAnimations());
        this.setFacialAnimations(attributesDOM.getFacialAnimations());
        this.setLoadInstructions(attributesDOM.getLoadingInstructions());


        for (xmlCharacterAttachmentParameters params : attributesDOM.getAttachments())
            this.addAttachmentInstruction(new AttachmentParams(params));

        // Skinned mesh addition params
        for (xmlSkinnedMeshParams params : attributesDOM.getAdditionInstructions())
            this.addSkinnedMeshParams(new SkinnedMeshParams(params));
        // Eyeball texture
        if (attributesDOM.getEyeballTexture() != null)
            eyeballTexture = attributesDOM.getEyeballTexture();

        // Skintone
        xmlFloatRow skin = attributesDOM.getSkinTone();
        if (skin != null)
        {
            skinTone[0] = skin.getX();
            skinTone[1] = skin.getY();
            skinTone[2] = skin.getZ();
        }

        this.animateFace = attributesDOM.isAnimateFace();
        this.applySkinToneOnHead = attributesDOM.isApplySkinToneOnHead();

        List<String> correctCrap = new FastList<String>();
        for (String loadString : loadInstructions)
        {
            String stringToAdd = loadString.trim();
            if (stringToAdd == null)
                throw new RuntimeException("Encountered null string!");
            correctCrap.add(stringToAdd);
        }
        clearLoadInstructions();
        setLoadInstructions(correctCrap);

        // Metadata map
        for (xmlMetaData xmd : attributesDOM.getMetaData())
        {
            this.getMetaData().put(xmd.getKey(), xmd.getValue());
        }
    }

    /**
     * True if the head mesh is to be applied with the skin tone color modulation
     * @return
     */
    public boolean isApplySkinToneOnHead() {
        return applySkinToneOnHead;
    }

    /**
     * Set if the head mesh is to be applied with the skin tone color modulation
     * @param applySkinToneOnHead
     */
    public void setApplySkinToneOnHead(boolean applySkinToneOnHead) {
        this.applySkinToneOnHead = applySkinToneOnHead;
    }

    /**
     *
     */
    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * This class wraps up the data needed to attach a skinned mesh.
     */
    public static final class SkinnedMeshParams
    {
        private String meshName = null;
        private String subGroupName = null;
        private String owningFileName = null;

        /**
         * Construct a new instance reflecting the provided DOM
         * @param paramsDOM
         */
        SkinnedMeshParams(xmlSkinnedMeshParams paramsDOM)
        {
            set(    paramsDOM.getSkinnedMeshName(),
                    paramsDOM.getSubGroupName(),
                    paramsDOM.getOwningFileName());
        }

        protected SkinnedMeshParams(SkinnedMeshParams other)
        {
            set(other.meshName, other.subGroupName, other.owningFileName);
        }

        /**
         * Construct a new instance with the provided data
         * @param meshName Mesh to attach
         * @param subGroupName Subgroup to attach to
         */
        public SkinnedMeshParams(String meshName, String subGroupName, String owningFileName)
        {
            set(meshName, subGroupName, owningFileName);
        }

        public void set(String meshName, String subGroupName, String owningFileName)
        {
            if (meshName == null || subGroupName == null)
                throw new IllegalArgumentException("Null param, meshName: " + meshName +
                        ", subGroupName: " + subGroupName + ", owningFileName: " + owningFileName);
            if (owningFileName == null) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Owning file not specified!");
            }

            this.meshName = meshName;
            this.subGroupName = subGroupName;
            this.owningFileName = owningFileName;
        }

        /**
         * Retrieve the mesh name that is being attached.
         * @return Mesh name
         */
        public String getMeshName() {
            return meshName;
        }

        /**
         * Retrieve the subgroup this skinned mesh will be attached to.
         * @return Subgroup name
         */
        public String getSubgroup() {
            return subGroupName;
        }

        /**
         * Retrieve the file owning this mesh.
         * @return File path
         */
        public String getOwningFileName() {
            return owningFileName;
        }

        /**
         * Create the DOM representation of this object
         * @return
         */
        private xmlSkinnedMeshParams generateParamsDOM() {
            xmlSkinnedMeshParams result = new xmlSkinnedMeshParams();
            result.setSkinnedMeshName(meshName);
            result.setSubGroupName(subGroupName);
            result.setOwningFileName(owningFileName);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SkinnedMeshParams other = (SkinnedMeshParams) obj;
            if ((this.meshName == null) ? (other.meshName != null) : !this.meshName.equals(other.meshName)) {
                return false;
            }
            if ((this.subGroupName == null) ? (other.subGroupName != null) : !this.subGroupName.equals(other.subGroupName)) {
                return false;
            }
            if ((this.owningFileName == null) ? (other.owningFileName != null) : !this.owningFileName.equals(other.owningFileName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + (this.meshName != null ? this.meshName.hashCode() : 0);
            hash = 71 * hash + (this.subGroupName != null ? this.subGroupName.hashCode() : 0);
            hash = 71 * hash + (this.owningFileName != null ? this.owningFileName.hashCode() : 0);
            return hash;
        }

    }
}
