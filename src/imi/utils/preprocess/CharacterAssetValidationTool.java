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
package imi.utils.preprocess;

import imi.scene.SkeletonNode;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import javax.swing.JOptionPane;
import javolution.util.FastTable;

/**
 * Used to validate assets for characters 
 * @author Lou Hayt
 */
public class CharacterAssetValidationTool
{
    /** These meshes are needed by various systems **/
    private static final String [] meshes = {
        "HeadGeoShape",
        "TongueGeoShape",
        "LowerTeethShape",
        "UpperTeethShape",
        "EyeLashesShape",
        "leftEyeGeoShape",
        "rightEyeGeoShape",
    };

    /** These joints are needed by various systems **/
    private static final String [] joints = {
        "Head",
        "HairAttach",
        "rightEyeLid",
        "leftEyeLid",
        "rightEye",
        "leftEye",
        "rightArm",
        "rightArmRoll",
        "rightForeArm",
        "rightForeArmRoll",
        "rightHand",
        "leftArm",
        "leftArmRoll",
        "leftForeArm",
        "leftForeArmRoll",
        "leftHand",
    };
    
    public static void validateSkeleton(SkeletonNode skeleton)
    {
        int warnings = 0;
        StringBuilder warningsSB = new StringBuilder();
        FastTable<String> fleshMeshes = new FastTable<String>();

        for (String name : joints)
        {
            if (-1 == skeleton.getSkinnedMeshJointIndex(name))
            {
                warnings++;
                warningsSB.append(name + " joint was not found!\n");
            }
        }

        FastTable<PPolygonSkinnedMeshInstance> list = skeleton.getSkinnedMeshInstances();
        for (String name : meshes)
        {
            boolean found = false;
            for (PPolygonSkinnedMeshInstance mesh : list)
            {
                if (mesh.getName().equals(name))
                    found = true;
                if (mesh.getName().toLowerCase().contains("nude"))
                    fleshMeshes.add(mesh.getName());
            }
            if (!found)
            {
                warnings++;
                warningsSB.append(name + " mesh was not found!\n");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("There are " + warnings + " warnings!\n");
        sb.append(warningsSB.toString() + "\n");
        sb.append("The following " + fleshMeshes.size() + " meshes will be applied with the flesh shader:\n");
        for(String name : fleshMeshes)
            sb.append(name + "\n");

        JOptionPane.showMessageDialog(null, sb.toString(), "Character Asset Validation Tool", JOptionPane.WARNING_MESSAGE);
    }
}
