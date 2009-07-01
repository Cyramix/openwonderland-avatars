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
 * particular joint in a character skeleton. A transform is provided in order
 * to orient the attached mesh in the desired fashion. This class is immutable
 * and therefore thread safe.
 * @author Lou Hayt
 */
public final class AttachmentParams
{
    /** The name of the mesh being attached **/
    private final String  meshName;
    /** The name of the parentJoint having the mesh attached**/
    private final String  parentJointName;
    /** Orientation **/
    private final PMatrix matrix = new PMatrix();
    /** The name to give the attachment parentJoint **/
    private final String attachmentJointName;

    private final String owningFileName;

    /**
     * Construct a new instance with the specified qualities.
     * <p>
     * All the parameters must be nonnull.
     * </p>
     * @param mesh 
     * @param parentJoint Name of the parentJoint to attach to
     * @param orientation Transform to orient the attachment correctly
     * @throws IllegalArgumentException If any provided parameters are null
     */
    public AttachmentParams(String mesh, String parentJoint, PMatrix orientation, String attachmentJointName, String owningFileName)
    {
        if (mesh == null || parentJoint == null || orientation == null || attachmentJointName == null)
            throw new IllegalArgumentException("Null parameter encountered, Mesh: " + mesh
                    + ", parentJoint: " + parentJoint + ", orientation: " + orientation
                    + "attachmentJointName: " + attachmentJointName + ", owningFileName: " + owningFileName);
        meshName        = mesh;
        parentJointName = parentJoint;
        matrix.set(orientation);
        this.attachmentJointName = attachmentJointName;
        this.owningFileName  = owningFileName;
    }

    /**
     * Package private cons for xml DOM construction.
     *
     * <p>
     * If part of the DOM that is required is not found, an IllegalArgumentException
     * will be thrown. If no transform is found then an identity transform will
     * be assumed.
     * </p>
     * @param paramsDOM A non-null and valid dom
     * @throws IllegalArgumentException If {@code paramsDOM == null} or paramsDOM is invalid.
     */
    AttachmentParams(xmlCharacterAttachmentParameters paramsDOM)
    {
        if (paramsDOM == null)
            throw new IllegalArgumentException("Null DOM provided!");

        meshName            = paramsDOM.getMeshName();
        parentJointName     = paramsDOM.getJointToAttachOn();
        attachmentJointName = paramsDOM.getJointToAttachOn();
        owningFileName      = paramsDOM.getOwningFileName();

        // Check for bad params
        if (meshName == null || parentJointName == null || attachmentJointName == null)
            throw new IllegalArgumentException("Invalid DOM, meshName: " + meshName
                    + ", parentJointName: " + parentJointName
                    + ", attachmentJointName: " + attachmentJointName
                    + ", owningFileName: " + owningFileName);

        if (paramsDOM.getLocalSpaceTransform() != null)
            matrix.set(paramsDOM.getLocalSpaceTransform().getPMatrix());


    }

    AttachmentParams(AttachmentParams param) {
        this(param.meshName, param.parentJointName, param.matrix, param.attachmentJointName, param.owningFileName);
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
        result.setLocalSpaceTransform(transform);
        result.setOwningFileName(owningFileName);

        return result;
    }

    ////////////////////////////////
    //////////////// Public API
    ////////////////////////////////

    /**
     * Retrieve the name of the parent joint.
     * @return Parent joint name
     */
    public String getParentJointName() {
        return parentJointName;
    }

    /**
     * Retrieve the value of the transform associated with this attachment.
     * @param mOut A non-null storage object.
     * @throws NullPointerException If {@code mOut == null}
     */
    public void getMatrix(PMatrix mOut) {
        mOut.set(matrix);
    }

    /**
     * Retrieve the name of the mesh to be attached.
     * @return Mesh name
     */
    public String getMeshName() {
        return meshName;
    }

    /**
     * Get the name of the joint to attach on.
     * @return Attachment joint name
     */
    public String getAttachmentJointName() {
        return attachmentJointName;
    }

    /**
     * Retrieve the file owning this mesh.
     * @return File path
     */
    public String getOwningFileName() {
        return owningFileName;
    }
}
