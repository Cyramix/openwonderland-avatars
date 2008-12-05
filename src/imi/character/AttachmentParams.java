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

/**
 *
 * @author Lou Hayt
 */
public class AttachmentParams
{
    private String  meshName   = null;
    private String  jointName  = null;
    private PMatrix matrix     = null;
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
}
