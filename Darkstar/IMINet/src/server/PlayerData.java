/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;

/**
 *
 * @author Lou Hayt
 */
public class PlayerData implements Serializable, ManagedObject
{
    private String name = "nameless";
    
    private int ID;
    private float posX;
    private float posY;
    private float posZ;
    private float dirX;
    private float dirY;
    private float dirZ;
    private float rightArmX = 0.0f;
    private float rightArmY = 0.0f;
    private float rightArmZ = 0.0f;
    private float leftArmX  = 0.0f;
    private float leftArmY  = 0.0f;
    private float leftArmZ  = 0.0f;
    private boolean rightArmEnabled = false;;
    private boolean leftArmEnabled  = false;;
    private boolean male = true;
    private int feet  = -1;
    private int legs  = -1;
    private int torso = -1;
    private int hair  = -1;
    
    private boolean playingGame = false;
    private int hitPoints = 0;
    
    public PlayerData(int ID)
    {
        this.ID = ID;
        posX = posZ = dirX = 0.0f;
        dirZ = -1.0f;
    }
    
    public PlayerData(int ID, String name)
    {
        this(ID);
        this.name = name;
    }
    
    public void updatePosition(float posX, float posY, float posZ, float dirX, float dirY, float dirZ) 
    {
        this.posX = posX;
        this.posZ = posZ;
        this.dirX = dirX;
        this.dirZ = dirZ;
    }

    public void updateArm(boolean right, float x, float y, float z) 
    {
        if (right)
        {
            rightArmX = x;
            rightArmY = y;
            rightArmZ = z;
        }
        else
        {
            leftArmX = x;
            leftArmY = y;
            leftArmZ = z;   
        }
    }

    public void enableArm(boolean right, boolean enable)
    {
        if (right)
            rightArmEnabled = enable;
        else 
            leftArmEnabled  = enable;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getID() {
        return ID;
    }

    public float getDirX() {
        return dirX;
    }

    public void setDirX(float dirX) {
        this.dirX = dirX;
    }

    public float getDirZ() {
        return dirZ;
    }

    public void setDirZ(float dirZ) {
        this.dirZ = dirZ;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getDirY() {
        return dirY;
    }

    public void setDirY(float dirY) {
        this.dirY = dirY;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public void setPosZ(float posZ) {
        this.posZ = posZ;
    }

    public float getLeftArmX() {
        return leftArmX;
    }

    public void setLeftArmX(float leftArmX) {
        this.leftArmX = leftArmX;
    }

    public float getLeftArmY() {
        return leftArmY;
    }

    public void setLeftArmY(float leftArmY) {
        this.leftArmY = leftArmY;
    }

    public float getLeftArmZ() {
        return leftArmZ;
    }

    public void setLeftArmZ(float leftArmZ) {
        this.leftArmZ = leftArmZ;
    }

    public float getRightArmX() {
        return rightArmX;
    }

    public void setRightArmX(float rightArmX) {
        this.rightArmX = rightArmX;
    }

    public float getRightArmY() {
        return rightArmY;
    }

    public void setRightArmY(float rightArmY) {
        this.rightArmY = rightArmY;
    }

    public float getRightArmZ() {
        return rightArmZ;
    }

    public void setRightArmZ(float rightArmZ) {
        this.rightArmZ = rightArmZ;
    }

    public boolean isLeftArmEnabled() {
        return leftArmEnabled;
    }

    public boolean isRightArmEnabled() {
        return rightArmEnabled;
    }
    
    public void setAvatarInfo(boolean male, int feet, int legs, int torso, int hair)
    {
        this.male  = male;
        this.feet  = feet;
        this.legs  = legs;
        this.torso = torso;
        this.hair  = hair;
    }

    public boolean isMale() {
        return male;
    }

    public int getFeet() {
        return feet;
    }

    public int getHair() {
        return hair;
    }

    public int getLegs() {
        return legs;
    }

    public int getTorso() {
        return torso;
    }

    public boolean isPlayingGame() {
        return playingGame;
    }

    public void setPlayingGame(boolean playingGame) {
        this.playingGame = playingGame;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }
   
    public void adjustHitPoints(int add) {
        this.hitPoints += add;
    }
}
