/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.character.objects;

import com.jme.math.Vector3f;

/**
 *
 * @author Lou Hayt
 */
public interface TargetObject extends SpatialObject
{
    public Vector3f getTargetPositionRef();
    public Vector3f getTargetForwardVector();

//    public int getTargetCount();
//    public Vector3f getTargetPosition(int index);
//    public Vector3f getTargetForwardVector(int index);

    public SpatialObject getOwner();
    public void setOwner(SpatialObject object);

    public boolean isOccupied();
    public void setOccupied(boolean isOccupied);
}