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

package imi.collision;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import imi.character.Character;

/**
 *
 * @author Lou Hayt
 */
@ExperimentalAPI
public interface TransformUpdateManager
{
    public void update(float deltaTime);
    public void transformUpdate(Character character, PMatrix transform, Vector3f newPosition, PMatrix newOrientation);
    
}
