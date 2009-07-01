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

import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import imi.scene.PJoint;
import javolution.util.FastTable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * @author Lou Hayt
 */
@ExperimentalAPI
public interface PickResult
{
    public void addPickedTriangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f loc);
    public void addPickedSphere(BoundingSphere jmeSphere);
    public void addPickedJoint(PJoint joint);

    public FastTable<Vector3f> getPickedTriangles();
    public FastTable<Vector3f> getPickedTriangleLocations();
    public FastTable<PJoint>   getPickedJoints();

    public void getPickedSphere(BoundingSphere jmeSphere);
    public BoundingSphere getPickedSphere();
    public FastTable<BoundingSphere> getPickedSpheres();

    public float getClosestRange();
}
