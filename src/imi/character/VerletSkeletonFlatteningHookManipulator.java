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

import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkeletonFlatteningHookManipulator;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 *
 * @author Lou Hayt
 */
public class VerletSkeletonFlatteningHookManipulator implements SkeletonFlatteningHookManipulator
{
    private final int shoulder = 18; // -0.25008845 (bind pose x values)
    private final int upperArm = 37; // -0.38327518 // 0.1331867 distance between shoulder and upperArm
    private final int elbow    = 43; // -0.49928188 // 0.2491934 distance between shoulder and elbow
    private final int foreArm  = 46; // -0.5855795  // 0.0862977 distance between elbow and forArm
    private final int wrist    = 48; // -0.73043364 // 0.1448541 distance between the elbow and the wrist
    
    private VerletArm       arm         = null;
    private SkeletonNode    skeleton    = null;
    
    private boolean manualDriveReachUp = true; // otherwise reaching forward

    public void processSkeletonNode(PNode current) 
    {
        
    }

}
