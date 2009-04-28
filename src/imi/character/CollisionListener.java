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
import javolution.util.FastTable;

/**
 *
 * @author Lou Hayt
 */
public interface CollisionListener
{
    public void colliding(Vector3f projection);
    public void picked(Class source, Object messageData, Vector3f origin, Vector3f direction, FastTable<Vector3f> hitList);
}
