/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import net.java.dev.jnag.sgs.common.LogMessageReception;
import net.java.dev.jnag.sgs.common.LogMethodInvocation;
import net.java.dev.jnag.sgs.common.NoLog;
import net.java.dev.jnag.sgs.common.Remote;

@LogMessageReception // Log when the client receives any message of this class.
@LogMethodInvocation // Log when the server calls any method of this class.
public interface ClientSideUser extends Remote 
{
    public void notifyLogin(int ID, String userName);
    public void notifyMessageOfTheDay(String messageOfTheDay);
    public void notifyLogout(boolean graceful);
  
    public void whisper(String message);
  
    @NoLog public void updatePosition(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ);
    @NoLog public void updatePositionAndArm(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, boolean right, float x, float y, float z);
    @NoLog public void updatePositionAndArms(int userID, float posX, float posY, float posZ, float dirX, float dirY, float dirZ, float rx, float ry, float rz, float lx, float ly, float lz);
    public void trigger(int userID, boolean pressed, int trigger);
    
    public void listPlayers(int [] playerIDs, String [] playerNames, boolean [] male, int [] feet, int [] legs, int [] torso, int [] hair, int [] head, int [] skinTone, int [] eyeColor);
    public void addPlayer(int userID, String playerName, boolean male, int feet, int legs, int torso, int hair, int head, int skinTone, int eyeColor);
    public void removePlayer(int userID);
    
    public void recieveWorldList(String [] worldNames);
}