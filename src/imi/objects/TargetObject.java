/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.objects;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *
 * @author Lou Hayt
 */
@ExperimentalAPI
public interface TargetObject extends SpatialObject
{
    public float getDesiredDistanceFromOtherTargets();

    //public Vector3f getTargetPosition();
    public Vector3f getTargetPositionRef();
    public Vector3f getTargetForwardVector();

//    public int getTargetCount();
//    public Vector3f getTargetPosition(int index);
//    public Vector3f getTargetPositionRef(int index);
//    public Vector3f getTargetForwardVector(int index);

    public SpatialObject getOwner();
    public void setOwner(SpatialObject object);

    public boolean isOccupied();
    public boolean isOccupied(boolean occupiedMatters);
    public void setOccupied(boolean isOccupied);
}