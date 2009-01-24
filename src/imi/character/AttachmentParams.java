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
import imi.serialization.xml.bindings.xmlCharacterAttachmentParameters;
import imi.serialization.xml.bindings.xmlMatrix;

/**
 * This class specifies the necessary information for attaching a mesh to a
 * particular joiny in a skeleton. A transform is provided in order to orient
 * the attached mesh in the desired fashion.
 * @author Lou Hayt
 */
public class AttachmentParams
{
    /** The name of the mesh being attached **/
    private String  meshName   = null;
    /** The name of the parentJoint having the mesh attached**/
    private String  parentJointName  = null;
    /** Orientation **/
    private PMatrix matrix     = null;
    /** The name to give the attachment parentJoint **/
    private String attachmentJointName = null;

    /**
     * Construct a new instance specifying all of the needed information.
     * @param mesh Name of the mesh being attached
     * @param parentJoint Name of the parentJoint to attach to
     * @param orientation Transform to orient the attachment correctly
     */
    public AttachmentParams(String mesh, String parentJoint, PMatrix orientation, String attachmentJointName)
    {
        meshName  = mesh;
        parentJointName = parentJoint;
        matrix    = orientation;
        this.attachmentJointName = attachmentJointName;
    }

    /**
     * Package private cons
     * @param paramsDOM
     */
    AttachmentParams(xmlCharacterAttachmentParameters paramsDOM)
    {
        applyParamsDOM(paramsDOM);
    }


    public String getParentJointName() {
        return parentJointName;
    }

    public void setParentJointName(String jointName) {
        this.parentJointName = jointName;
    }
    public PMatrix getMatrix() {
        return matrix;
    }
    public void setMatrix(PMatrix matrix) {
        this.matrix = matrix;
    }
    public String getMeshName() {
        return meshName;
    }
    public void setMeshName(String meshName) {
        this.meshName = meshName;
    }

    public String getAttachmentJointName() {
        return attachmentJointName;
    }

    public void setAttachmentJointName(String attachmentJointName) {
        this.attachmentJointName = attachmentJointName;
    }
    
    /**
     * Serialize this attachment parameters object
     * @return The DOM representation
     */
    xmlCharacterAttachmentParameters generateParamsDOM()
    {
        xmlCharacterAttachmentParameters result =
                new xmlCharacterAttachmentParameters();

        result.setMeshName(meshName);
        result.setJointToAttachOn(parentJointName);
        xmlMatrix transform = new xmlMatrix();
        transform.set(matrix);

        return result;
    }

    /**
     * Package private method to apply the provided DOM to this instance
     * @param paramsDOM
     */
    void applyParamsDOM(xmlCharacterAttachmentParameters paramsDOM)
    {
        if (paramsDOM == null)
            return;
        setMeshName(paramsDOM.getMeshName());
        setParentJointName(paramsDOM.getJointToAttachOn());
        if (paramsDOM.getLocalSpaceTransform() != null)
            setMatrix(paramsDOM.getLocalSpaceTransform().getPMatrix());
    }
}
