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
    /** The name of the joint having the mesh attached**/
    private String  jointName  = null;
    /** Orientation **/
    private PMatrix matrix     = null;

    /**
     * Construct a new instance specifying all of the needed information.
     * @param mesh Name of the mesh being attached
     * @param joint Name of the joint to attach to
     * @param orientation Transform to orient the attachment correctly
     */
    public AttachmentParams(String mesh, String joint, PMatrix orientation)
    {
        meshName  = mesh;
        jointName = joint;
        matrix    = orientation;
    }


    public String getJointName() {
        return jointName;
    }

    public void setJointName(String jointName) {
        this.jointName = jointName;
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
    /**
     * Serialize this attachment parameters object
     * @return The DOM representation
     */
    xmlCharacterAttachmentParameters generateParamsDOM()
    {
        xmlCharacterAttachmentParameters result =
                new xmlCharacterAttachmentParameters();

        result.setMeshName(meshName);
        result.setJointToAttachOn(jointName);
        xmlMatrix transform = new xmlMatrix();
        transform.set(matrix);

        return result;
    }
}
