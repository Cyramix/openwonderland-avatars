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

/**
 *
 * @author Lou Hayt
 */
public class CharacterAttributes 
{
    private String      name              = "nameless";
    private SharedAsset asset             = null;
    private String   baseURL              = null;
    private String   BindPoseFile         = null;
    private String[] animations           = new String[0];
    private String[] facialAnimations     = new String[0];
    private String[] deleteInstructions   = new String[0];
    private String[] loadInstructions     = new String[0];
    private String[] addInstructions      = new String[0];
    private AttachmentParams[] attachmentsInstructions = new AttachmentParams[0];

    // For simple static geometry replacement
    private boolean useSimpleStaticModel    = false; 
    private PMatrix origin                  = null; 
    private PScene  simpleScene             = null;

    public CharacterAttributes(String name) {
        this.name = name;
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

    public String[] getAddInstructions() {
        return addInstructions;
    }

    public void setAddInstructions(String[] addInstructions) {
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
    
}
