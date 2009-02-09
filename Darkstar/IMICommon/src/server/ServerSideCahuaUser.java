/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import net.java.dev.jnag.sgs.common.LogMessageReception;
import net.java.dev.jnag.sgs.common.LogMethodInvocation;
import net.java.dev.jnag.sgs.common.NoLog;
import net.java.dev.jnag.sgs.common.Remote;

/**
 *
 * @author Lou Hayt
 */
@LogMessageReception // Log when the client receives any message of this class.
@LogMethodInvocation // Log when the server calls any method of this class.
public interface ServerSideCahuaUser extends Remote
{
    @NoLog public void updateBalls(float [] x, float [] y, float [] z, float [] velX, float [] velY, float [] velZ);
    public void remoteBallUpdate(int userID, int ballNumber, float x, float y, float z, float velX, float velY, float velZ); 
    public void gotHit(int byUserID, int ballID);
    public void startGame(int hitPoints);
}
