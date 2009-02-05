/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import net.java.dev.jnag.sgs.common.LogMessageReception;
import net.java.dev.jnag.sgs.common.LogMethodInvocation;
import net.java.dev.jnag.sgs.common.NoLog;
import net.java.dev.jnag.sgs.common.Remote;

@LogMessageReception // Log when the server receives any message of this class.
@LogMethodInvocation // Log when the server calls any message of this class.
public interface ServerSideUser extends Remote 
{
    public void setAvatarInfo(boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor);
    public void serverCommand(String string);
    @NoLog public void updatePosition(float posX, float posY, float posZ, float dirX, float dirY, float dirZ);
    @NoLog public void updatePositionAndArm(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, boolean right, float x, float y, float z);
    @NoLog public void updatePositionAndArms(float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float rx, float ry, float rz, float lx, float ly, float lz);
    public void enableArm(boolean right, boolean enable); // required to throtel messages
    //public void setHandGesture(boolean right, int gestureID); // can be a trigger
    public void trigger(boolean pressed, int trigger);
    public void enterWorld(String worldName);
    public void requestWorldList();
}