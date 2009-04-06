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
import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.serialization.xml.bindings.xmlCharacterAttachmentParameters;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlFloatRow;
import imi.serialization.xml.bindings.xmlSkinnedMeshParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class contains all the different attributes of a character. It contains
 * things like the list of animations that need to be loaded as well as geometry
 * substitutions, the bind pose, and other information.
 * @author Lou Hayt
 */
public class CharacterAttributes
{
    /** Name of the character **/
    private String                  name                    = "nameless";
    /** The SharedAsset associated with this character's COLLADA model **/
    private SharedAsset             asset                   = null;
    /** A string to be applied to the beginning of all paths, if null asumming it's a local path **/
    private String                  baseURL                 = null;
    /** List of body animations to load **/
    private String[]                animations              = new String[0];
    /** List of facial animations to load **/
    private String[]                facialAnimations        = new String[0];
    /** List of mesh names to be loaded **/
    private List<String[]>            loadInstructions      = new ArrayList<String[]>();
    /** List of skinned meshes to be added to the skeleton **/
    private SkinnedMeshParams[]     addInstructions = new SkinnedMeshParams[0];
    /** List of meshes to add as attachment nodes on the skeleton **/
    private AttachmentParams[]      attachmentsInstructions = new AttachmentParams[0];
    /** Specify the head the avatar should begin with **/
    private String                  headAttachment          = null;
    /** This specifies the gender of the avatar. Its exact meaning is not yet defined **/
    private int                     gender                  = 1;
    /** Skin tone RGB **/
    private float []                skinTone                = new float [3];
    /** Hair color RGB **/
    private float []                hairColor               = new float [3];
    /** Shirt color RGB **/
    private float []                shirtColor              = new float [3];
    private float []                shirtSpecColor          = new float [3];
    /** Pants color RGB **/
    private float []                pantsColor              = new float [3];
    private float []                pantsSpecColor          = new float [3];
    /** Shoes color RGB **/
    private float []                shoesColor              = new float [3];
    private float []                shoesSpecColor          = new float [3];
    /** Eyeball texture **/
    private String                  eyeballTexture          = null;

    /** Initialization extension **/
    private InitializationInterface initializationObject    = null;

    private Map<Integer, String[]>  m_geomref = null;

    private boolean[]               m_areMeshesAltered      = new boolean[] { false, false, false, false, false, false, false, false, false, false };

    private boolean                 m_bPhongLightingForSkin = false;
    // For simple static geometry replacement
    private boolean useSimpleStaticModel    = false;
    private PMatrix origin                  = new PMatrix(new Vector3f(0,(float)Math.PI,0), Vector3f.UNIT_XYZ, Vector3f.ZERO);
    private PScene  simpleScene             = null;

    /** This boolean controls whether the facial animation will setup **/
    private boolean animateFace = true;

    /**
     * Construct a new instance with the provided name
     * @param name
     */
    public CharacterAttributes(String name) {
        this();
        this.name = name;
    }

    /**
     * Construct a new instance with some default values
     */
    public CharacterAttributes() {
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
     * Construct a new instance reflecting the provided DOM
     * @param attributesDOM
     */
    public CharacterAttributes(xmlCharacterAttributes attributesDOM)
    {
        this();
        applyAttributesDOM(attributesDOM);
    }

    /**
     * Make the shirt color random
     */
    public void randomizeShirtColor()
    {
        shirtColor[0] = (float)Math.random();
        shirtColor[1] = (float)Math.random();
        shirtColor[2] = (float)Math.random();
        float chance  = (float)Math.random();;
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
            shirtSpecColor[0] = 0.0f;
            shirtSpecColor[1] = 0.0f;
            shirtSpecColor[2] = 0.0f;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SharedAsset getAsset() {
        return asset;
    }

    public void setAsset(SharedAsset asset) {
        this.asset = asset;
    }

    public String getHeadAttachment() {
        return headAttachment;
    }

    public void setHeadAttachment(String headLocation) {
        this.headAttachment = headLocation;
    }

    public boolean isUseSimpleStaticModel() {
        return useSimpleStaticModel;
    }

    public void setUseSimpleStaticModel(boolean bUseSimpleSphereModel, PScene pScene) {
        this.useSimpleStaticModel = bUseSimpleSphereModel;
        this.simpleScene = pScene;
    }

    public PMatrix getOrigin() {
        return origin;
    }

    public void setOrigin(PMatrix origin) {
        this.origin = origin;
    }

    /**
     * Get the base URL for these attributes
     * @return
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Set the base URL for these attributes
     * @param baseURL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String[] getAnimations() {
        return animations;
    }

    public void setAnimations(String[] animations) {
        this.animations = animations;
    }

    public String[] getFacialAnimations() {
        return facialAnimations;
    }

    public void setFacialAnimations(String[] facialAnimations) {
        this.facialAnimations = facialAnimations;
    }

    public SkinnedMeshParams[] getAddInstructions() {
        return addInstructions;
    }

    public void setAddInstructions(SkinnedMeshParams[] addInstructions) {
        this.addInstructions = addInstructions;
    }

    public List<String[]> getLoadInstructions() {
        return loadInstructions;
    }

    public void setLoadInstructions(List<String[]> loadInstructions) {
        this.loadInstructions = loadInstructions;
    }

    public List<String[]> getLoadInstructionsBySubGroup(String subGroup) {
        List<String[]> list = new ArrayList<String[]>();
        for (int i = 0; i < loadInstructions.size(); i++) {
            if (loadInstructions.get(i)[1].equalsIgnoreCase(subGroup))
                list.add(loadInstructions.get(i));
        }
        return list;
    }

    public boolean deleteLoadInstructionsBySubGroup(String subGroup) {
        int initialSize = new Integer(loadInstructions.size());
        int finalSize   = 0;

        if (loadInstructions == null)
            return false;

        for (int i = 0; i < loadInstructions.size(); i ++) {
            if (loadInstructions.get(i)[1].equalsIgnoreCase(subGroup))
                loadInstructions.remove(i);
        }
        finalSize   = loadInstructions.size();

        if (initialSize > finalSize)
            return true;
        else
            return false;
    }

    public AttachmentParams[] getAttachmentsInstructions() {
        return attachmentsInstructions;
    }

    public AttachmentParams[] getAttachmentInstructionsBySubGroup(String subGroup) {
        List<AttachmentParams> attatch  = new ArrayList<AttachmentParams>();
        for (int i = 0; i < this.attachmentsInstructions.length; i++) {
            if (this.attachmentsInstructions[i].getAttachmentJointName().equalsIgnoreCase(subGroup))
                attatch.add(this.attachmentsInstructions[i]);
        }
        AttachmentParams[] attachparams = null;
        attatch.toArray(attachparams);
        return attachparams;
    }

    public boolean deleteAttachmentInstructionsBySubGroup(String subGroup) {
        List<AttachmentParams> newAttatchmentInstructions  = new ArrayList<AttachmentParams>();
        if (attachmentsInstructions == null)
            return false;

        for (int i = 0; i < attachmentsInstructions.length; i++) {
            if (attachmentsInstructions[i].getAttachmentJointName().equalsIgnoreCase(subGroup))
                continue;
            else
                newAttatchmentInstructions.add(attachmentsInstructions[i]);
        }

        AttachmentParams[] attachparams = null;
        newAttatchmentInstructions.toArray(attachparams);

        if (attachparams.length < attachmentsInstructions.length) {
            attachmentsInstructions = null;
            attachmentsInstructions = new AttachmentParams[attachparams.length];
            attachmentsInstructions = attachparams;
            return true;
        } else
            return false;
    }

    public void setAttachmentsInstructions(AttachmentParams[] attachmentsInstructions) {
        this.attachmentsInstructions = attachmentsInstructions;
    }

    public InitializationInterface getInitializationObject() {
        return initializationObject;
    }

    public void setInitializationObject(InitializationInterface initializationObject) {
        this.initializationObject = initializationObject;
    }

    public PScene getSimpleScene() {
        return simpleScene;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int sex) {
        gender = sex;
    }

    public void setFlagForAlteredRegion(int region, boolean flag) {
        m_areMeshesAltered[region] = flag;
    }

    public boolean getFlagForAlteredRegion(int region) {
        return m_areMeshesAltered[region];
    }

    public boolean[] getMeshesAlteredArray() {
        return m_areMeshesAltered;
    }

    public Map<Integer, String[]> getGeomRef() {
        return m_geomref;
    }

    public String[] getGeomRefByRegion(int region) {
        return m_geomref.get(region);
    }

    public void setGeomRef(Map<Integer, String[]> geom) {
        m_geomref = geom;
    }

    public void setGeomRefByRegion(int region, String[] geomlist) {
        m_geomref.put(region, geomlist);
    }

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

        // Finished
        return result;
    }

    /**
     * Package private method to apply the provided DOM information to this instance.
     * @param attributesDOM
     */
    void applyAttributesDOM(xmlCharacterAttributes attributesDOM)
    {
        if (attributesDOM == null)
            return;

        this.setName(attributesDOM.getName());
        this.setGender(attributesDOM.getGender());
        if (attributesDOM.getBaseURL() == null ||
                attributesDOM.getBaseURL().length() == 0)
            baseURL = null;
        else
            baseURL = attributesDOM.getName();
        this.setBaseURL(attributesDOM.getBaseURL());
        this.setHeadAttachment(attributesDOM.getHeadAttachment());

        this.setAnimations((String[]) attributesDOM.getBodyAnimations().toArray(new String[0]));
        this.setFacialAnimations((String[]) attributesDOM.getFacialAnimations().toArray(new String[0]));
        List<String[]> list = attributesDOM.getLoadingInstructions();

        this.setLoadInstructions(list);

        // Attachment params
        if (attributesDOM.getAttachments() != null)
        {
            AttachmentParams[] paramArray = new AttachmentParams[attributesDOM.getAttachments().size()];
            int index = 0;
            for (xmlCharacterAttachmentParameters params : attributesDOM.getAttachments())
            {
                paramArray[index] = new AttachmentParams(params);
                index++;
            }
            this.setAttachmentsInstructions(paramArray);
        }
        // Skinned mesh addition params
        if (attributesDOM.getAdditionInstructions() != null)
        {
            SkinnedMeshParams[] paramArray = new SkinnedMeshParams[attributesDOM.getAdditionInstructions().size()];
            int index = 0;
            for (xmlSkinnedMeshParams params : attributesDOM.getAdditionInstructions())
            {
                paramArray[index] = new SkinnedMeshParams(params);
                index++;
            }
            this.setAddInstructions(paramArray);
        }
        // Eyeball texture
        eyeballTexture = attributesDOM.getEyeballTexture();

        // Skintone
        xmlFloatRow skin = attributesDOM.getSkinTone();
        if (skin != null)
        {
            skinTone = new float[3];
            skinTone[0] = skin.getX();
            skinTone[1] = skin.getY();
            skinTone[2] = skin.getZ();
        }
        else
            skinTone = null;


    }

    /**
     * Factory method for creating SkinnedMeshParams objects
     * @param meshName The mesh to attach
     * @param subGroupName Which subgroup it is destined for on the skeleton.
     * @return
     */
    public SkinnedMeshParams createSkinnedMeshParams(String meshName, String subGroupName)
    {
        return new SkinnedMeshParams(meshName, subGroupName);
    }

    public boolean isMale()
    {
        if (gender == 1)
            return true;
        return false;
    }

    public float[] getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(float r, float g, float b) {
        skinTone[0] = r;
        skinTone[1] = g;
        skinTone[2] = b;
    }

    public float[] getHairColor() {
        return hairColor;
    }

    public void setHairColor(float r, float g, float b) {
        hairColor[0] = r;
        hairColor[1] = g;
        hairColor[2] = b;
    }

    public float[] getPantsColor() {
        return pantsColor;
    }

    public void setPantsColor(float r, float g, float b, float specR, float specG, float specB) {
        pantsColor[0] = r;
        pantsColor[1] = g;
        pantsColor[2] = b;
        pantsSpecColor[0] = specR;
        pantsSpecColor[1] = specG;
        pantsSpecColor[2] = specB;
    }

    public float[] getShirtColor() {
        return shirtColor;
    }

    public void setShirtColor(float r, float g, float b, float specR, float specG, float specB) {
        shirtColor[0] = r;
        shirtColor[1] = g;
        shirtColor[2] = b;
        shirtSpecColor[0] = specR;
        shirtSpecColor[1] = specG;
        shirtSpecColor[2] = specB;
    }

    public float[] getShoesColor() {
        return shoesColor;
    }

    public void setShoesColor(float r, float g, float b, float specR, float specG, float specB) {
        shoesColor[0] = r;
        shoesColor[1] = g;
        shoesColor[2] = b;
        shoesSpecColor[0] = specR;
        shoesSpecColor[1] = specG;
        shoesSpecColor[2] = specB;
    }

    public float[] getPantsSpecColor() {
        return pantsSpecColor;
    }

    public float[] getShirtSpecColor() {
        return shirtSpecColor;
    }

    public float[] getShoesSpecColor() {
        return shoesSpecColor;
    }

    public String getEyeballTexture() {
        return eyeballTexture;
    }

    public void setEyeballTexture(String eyeballTexture) {
        this.eyeballTexture = eyeballTexture;
    }

    /**
     * If false, normal maps will be used, otherwise use the phong model.
     * @param usePhong
     */
    public void setUsePhongLighting(boolean usePhong)
    {
        m_bPhongLightingForSkin = usePhong;
    }

    public boolean isUsingPhongLighting()
    {
        return m_bPhongLightingForSkin;
    }

    public boolean isAnimatingFace() {
        return animateFace;
    }

    public void setAnimateFace(boolean animateFace) {
        this.animateFace = animateFace;
    }


    /**
     * This class wraps up the data needed to attach a skinned mesh
     */
    public class SkinnedMeshParams
    {
        public String meshName = null;
        public String subGroupName = null;

        /**
         * Construct a new instance reflecting the provided DOM
         * @param paramsDOM
         */
        public SkinnedMeshParams(xmlSkinnedMeshParams paramsDOM)
        {
            meshName = paramsDOM.getSkinnedMeshName();
            subGroupName = paramsDOM.getSubGroupName();
        }

        /**
         * Construct an empty instance
         */
        public SkinnedMeshParams()
        {
            // Do nothing!
        }

        /**
         * Construct a new instance with the provided data
         * @param meshName Mesh to attach
         * @param subGroupName Subgroup to attach to
         */
        public SkinnedMeshParams(String meshName, String subGroupName)
        {
            set(meshName, subGroupName);
        }

        public void set(String meshName, String subGroupName)
        {
            this.meshName = meshName;
            this.subGroupName = subGroupName;
        }

        /**
         * Create the DOM representation of this object
         * @return
         */
        private xmlSkinnedMeshParams generateParamsDOM() {
            xmlSkinnedMeshParams result = new xmlSkinnedMeshParams();
            result.setSkinnedMeshName(meshName);
            result.setSubGroupName(subGroupName);
            return result;
        }
    }
}
