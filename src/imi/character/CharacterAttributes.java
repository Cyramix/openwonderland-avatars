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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.character;

import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.serialization.xml.bindings.xmlCharacterAttachmentParameters;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlSkinnedMeshParams;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    /** A string to be applied to the beginning of all paths **/
    private String                  baseURL                 = null;
    /** The location of the bind pose file, relative to the base url**/
    private String                  BindPoseFile            = null;
    /** List of body animations to load **/
    private String[]                animations              = new String[0];
    /** List of facial animations to load **/
    private String[]                facialAnimations        = new String[0];
    /** List of mesh names to delete**/
    private String[]                deleteInstructions      = new String[0];
    /** List of mesh names to be loaded **/
    private String[]                loadInstructions        = new String[0];
    /** List of skinned meshes to be added to the skeleton **/
    //private String[]                addInstructions         = new String[0];
    private SkinnedMeshParams[]     addInstructions = new SkinnedMeshParams[0];
    /** List of meshes to add as attachment nodes on the skeleton **/
    private AttachmentParams[]      attachmentsInstructions = new AttachmentParams[0];
    /** This specifies the gender of the avatar. Its exact meaning is not yet defined **/
    private int                     gender                  = -1;


    // For simple static geometry replacement
    private boolean useSimpleStaticModel    = false; 
    private PMatrix origin                  = null; 
    private PScene  simpleScene             = null;

    /**
     * Construct a new instance with the provided name
     * @param name
     */
    public CharacterAttributes(String name) {
        this.name = name;
    }

    CharacterAttributes(xmlCharacterAttributes attributesDOM)
    {
        applyAttributesDOM(attributesDOM);
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

    public String getBindPoseFile() {
        return BindPoseFile;
    }

    public void setBindPoseFile(String bindPoseFile) {
        BindPoseFile = bindPoseFile;
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

    public String[] getDeleteInstructions() {
        return deleteInstructions;
    }

    public void setDeleteInstructions(String[] deleteInstructions) {
        this.deleteInstructions = deleteInstructions;
    }

    public String[] getLoadInstructions() {
        return loadInstructions;
    }

    public void setLoadInstructions(String[] loadInstructions) {
        this.loadInstructions = loadInstructions;
    }

    public AttachmentParams[] getAttachmentsInstructions() {
        return attachmentsInstructions;
    }

    public void setAttachmentsInstructions(AttachmentParams[] attachmentsInstructions) {
        this.attachmentsInstructions = attachmentsInstructions;
    }
    
    PScene getSimpleScene() {
        return simpleScene;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int sex) {
        gender = sex;
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

        if (BindPoseFile != null)
            result.setBindPoseFile(BindPoseFile);
        else
            Logger.getLogger(CharacterAttributes.class.getName()).log(Level.SEVERE,
                    "No bind pose file specified.");
        
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

        // deletion instructions
        if (deleteInstructions != null)
        {
            stringArray = new ArrayList<String>();
            for (String str : deleteInstructions)
                stringArray.add(str);
            result.setDeletionInstructions(stringArray);
        }
        else
            result.setDeletionInstructions(null);

        // loading instructions
        if (loadInstructions != null)
        {
            stringArray = new ArrayList<String>();
            for (String str : loadInstructions)
                stringArray.add(str);
            result.setLoadingInstructions(stringArray);
        }
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
        this.setBaseURL(attributesDOM.getBaseURL());
        this.setBindPoseFile(attributesDOM.getBindPoseFile());

        this.setDeleteInstructions((String[]) attributesDOM.getDeletionInstructions().toArray(new String[0]));
        this.setAnimations((String[]) attributesDOM.getBodyAnimations().toArray(new String[0]));
        this.setFacialAnimations((String[]) attributesDOM.getFacialAnimations().toArray(new String[0]));
        this.setLoadInstructions((String[]) attributesDOM.getLoadingInstructions().toArray(new String[0]));
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

    }

    public CharacterAttributes() {
    }

    public SkinnedMeshParams createSkinnedMeshParams(String meshName, String subGroupName)
    {
        return new SkinnedMeshParams(meshName, subGroupName);
    }

    /**
     * This class wraps up the data needed to attach a skinned mesh
     */
    public class SkinnedMeshParams
    {
        public String meshName = null;
        public String subGroupName = null;

        public SkinnedMeshParams(xmlSkinnedMeshParams paramsDOM)
        {
            meshName = paramsDOM.getSkinnedMeshName();
            subGroupName = paramsDOM.getSubGroupName();
        }

        public SkinnedMeshParams()
        {
            // Do nothing!
        }
        
        public SkinnedMeshParams(String meshName, String subGroupName)
        {
            set(meshName, subGroupName);
        }

        public void set(String meshName, String subGroupName)
        {
            this.meshName = meshName;
            this.subGroupName = subGroupName;
        }

        private xmlSkinnedMeshParams generateParamsDOM() {
            xmlSkinnedMeshParams result = new xmlSkinnedMeshParams();
            result.setSkinnedMeshName(meshName);
            result.setSubGroupName(subGroupName);
            return result;
        }
    }
}
